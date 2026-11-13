package nuget;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Package {

    public static class PackageException extends RuntimeException {

        public PackageException() {
            super();
        }

        public PackageException(String message) {
            super(message);
        }

        public PackageException(Exception exception) {
            super(exception);
        }
    }

    public record Dependency(String id, String version) {
    }

    public final String id;
    public final String title;
    public final String version;
    public final String authors;
    public final String description;

    public final Map<String, List<Dependency>> dependencies;

    private Package(
            String id,
            String version,
            String title,
            String authors,
            String description,
            Map<String, List<Dependency>> dependencies
    ) {
        this.id = id;
        this.version = version;
        this.title = title;
        this.authors = authors;
        this.description = description;
        this.dependencies = Collections.unmodifiableMap(dependencies);
    }

    public static Package fromZipFile(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        if (!entries.hasMoreElements()) {
            throw new PackageException("Invalid package");
        }

        ZipEntry specEntry = null;

        ZipEntry entry = entries.nextElement();
        while (entry != null) {
            if (entry.getName().endsWith(".nuspec")) {
                if (entry.isDirectory()) {
                    break;
                }
                specEntry = entry;
                break;
            }

            if (!entries.hasMoreElements()) {
                break;
            }
            entry = entries.nextElement();
        }

        if (specEntry == null) {
            throw new PackageException("Invalid package");
        }

        try {
            InputStream inputStream = zipFile.getInputStream(specEntry);

            XMLInputFactory factory = XMLInputFactory.newInstance();

            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(
                    "javax.xml.stream.isSupportingExternalEntities",
                    false
            );
            factory.setXMLResolver((publicID, systemID, baseURI, namespace) -> {
                throw new XMLStreamException(
                        "External XML entities are forbidden");
            });

            XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

            Package pk = parseSpec(reader);
            reader.close();

            return pk;
        } catch (IOException | XMLStreamException e) {
            throw new PackageException(e);
        }
    }

    private static Package parseSpec(XMLStreamReader reader)
            throws XMLStreamException {

        Map<String, String> metadata = new HashMap<>();
        Map<String, List<Dependency>> dependencies = new HashMap<>();

        boolean foundPackage = false;
        boolean foundMetadata = false;

        while (reader.hasNext()) {
            int event = reader.next();

            if (event != XMLStreamReader.START_ELEMENT) {
                continue;
            }

            if (reader.getLocalName().equals("package")) {
                foundPackage = true;
            } else if (reader.getLocalName().equals("metadata")) {
                if (!foundPackage) {
                    throw new PackageException("Invalid nuspec structure");
                }

                foundMetadata = true;
                break;
            } else {
                throw new PackageException("Invalid nuspec structure");
            }
        }

        if (!foundPackage || !foundMetadata) {
            throw new PackageException("Missing package metadata");
        }

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.END_ELEMENT
                    && reader.getLocalName().equals("metadata")) {
                break;
            }

            if (event != XMLStreamReader.START_ELEMENT) {
                continue;
            }

            String element = reader.getLocalName();

            switch (element) {
                case "id":
                case "version":
                case "title":
                case "authors":
                case "description":
                    metadata.put(element, reader.getElementText().trim());
                    break;

                case "dependencies":
                    parseDependencies(reader, dependencies);
                    break;

                default:
                    break;
            }
        }

        require(metadata, "id");
        require(metadata, "version");
        require(metadata, "authors");
        require(metadata, "description");

        Map<String, List<Dependency>> immutableDependencies = new HashMap<>();

        dependencies.forEach((framework, list) ->
                immutableDependencies.put(
                        framework,
                        Collections.unmodifiableList(new ArrayList<>(list))
                )
        );

        metadata.get("id");
        metadata.get("version");
        metadata.get("title");
        metadata.get("authors");
        metadata.get("description");

        return new Package(
                metadata.get("id"),
                metadata.get("version"),
                metadata.get("title"),
                metadata.get("authors"),
                metadata.get("description"),
                Collections.unmodifiableMap(immutableDependencies)
        );
    }

    private static void parseDependencies(
            XMLStreamReader reader,
            Map<String, List<Dependency>> dependencies
    ) throws XMLStreamException {

        String framework = "any";

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.END_ELEMENT
                    && reader.getLocalName().equals("dependencies")) {
                return;
            }

            if (event != XMLStreamReader.START_ELEMENT) {
                continue;
            }

            String element = reader.getLocalName();

            if (element.equals("group")) {
                String targetFramework = reader.getAttributeValue(
                        null, "targetFramework"
                );

                framework = targetFramework == null || targetFramework.isBlank()
                        ? "any"
                        : targetFramework;

            } else if (element.equals("dependency")) {
                String id = reader.getAttributeValue(null, "id");
                String version = reader.getAttributeValue(null, "version");

                if (id == null || id.isBlank()) {
                    throw new PackageException("Dependency is missing its id");
                }

                dependencies.computeIfAbsent(
                        framework,
                        ignored -> new ArrayList<>()
                ).add(new Dependency(id, version));

                reader.nextTag();
            }
        }

        throw new PackageException("Unclosed dependencies element");
    }

    private static void require(
            Map<String, String> metadata,
            String field
    ) {
        String value = metadata.get(field);

        if (value == null || value.isBlank()) {
            throw new PackageException(
                    "Missing required metadata \"" + field + "\"");
        }
    }

    public static Package createFake(
            String id, List<Dependency> dependencies) {
        Map<String, List<Dependency>> dependencyMap = new HashMap<>();
        dependencyMap.put("any",
                Collections.unmodifiableList(new ArrayList<>(dependencies))
        );

        return new Package(
                id,
                "0.0.0+fake",
                id,
                "uas-mirea",
                "I AM NOT REAL I AM NOT REAL AAAA",
                dependencyMap
        );
    }

}
