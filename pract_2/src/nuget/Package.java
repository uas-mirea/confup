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
        ZipEntry specEntry = findSpecEntry(zipFile);

        try (InputStream inputStream = zipFile.getInputStream(specEntry)) {
            XMLStreamReader reader = createXmlReader(inputStream);

            try {
                return parseSpec(reader);
            } finally {
                reader.close();
            }
        } catch (IOException | XMLStreamException e) {
            throw new PackageException(e);
        }
    }

    private static ZipEntry findSpecEntry(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".nuspec")
                    && !entry.isDirectory()) {
                return entry;
            }
        }

        throw new PackageException("Invalid package");
    }

    private static XMLStreamReader createXmlReader(InputStream inputStream)
            throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();

        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(
                "javax.xml.stream.isSupportingExternalEntities",
                false
        );

        factory.setXMLResolver((publicID, systemID, baseURI, namespace) -> {
            throw new XMLStreamException(
                    "External XML entities are forbidden"
            );
        });

        return factory.createXMLStreamReader(inputStream);
    }

    private static Package parseSpec(XMLStreamReader reader)
            throws XMLStreamException {
        validatePackageStructure(reader);

        Map<String, String> metadata = new HashMap<>();
        Map<String, List<Dependency>> dependencies = new HashMap<>();

        parseMetadata(reader, metadata, dependencies);

        validateMetadata(metadata);

        return new Package(
                metadata.get("id"),
                metadata.get("version"),
                metadata.get("title"),
                metadata.get("authors"),
                metadata.get("description"),
                makeDependenciesImmutable(dependencies)
        );
    }

    private static void validatePackageStructure(XMLStreamReader reader)
            throws XMLStreamException {
        boolean foundPackage = false;

        while (reader.hasNext()) {
            if (reader.next() != XMLStreamReader.START_ELEMENT) {
                continue;
            }

            String element = reader.getLocalName();

            if (element.equals("package") && !foundPackage) {
                foundPackage = true;
                continue;
            }

            if (element.equals("metadata") && foundPackage) {
                return;
            }

            throw new PackageException("Invalid nuspec structure");
        }

        throw new PackageException("Missing package metadata");
    }

    private static void parseMetadata(
            XMLStreamReader reader,
            Map<String, String> metadata,
            Map<String, List<Dependency>> dependencies
    ) throws XMLStreamException {
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamReader.END_ELEMENT
                    && reader.getLocalName().equals("metadata")) {
                return;
            }

            if (reader.getEventType() != XMLStreamReader.START_ELEMENT) {
                continue;
            }

            String element = reader.getLocalName();

            if (isMetadataField(element)) {
                metadata.put(element, reader.getElementText().trim());
            } else if (element.equals("dependencies")) {
                parseDependencies(reader, dependencies);
            }
        }
    }

    private static boolean isMetadataField(String element) {
        return switch (element) {
            case "id", "version", "title", "authors", "description" -> true;
            default -> false;
        };
    }

    private static void validateMetadata(Map<String, String> metadata) {
        require(metadata, "id");
        require(metadata, "version");
        require(metadata, "authors");
        require(metadata, "description");
    }

    private static Map<String, List<Dependency>> makeDependenciesImmutable(
            Map<String, List<Dependency>> dependencies) {
        Map<String, List<Dependency>> result = new HashMap<>();

        dependencies.forEach((framework, list) -> result.put(framework,
                Collections.unmodifiableList(new ArrayList<>(list))
        ));

        return Collections.unmodifiableMap(result);
    }

    private static void parseDependencies(
            XMLStreamReader reader,
            Map<String, List<Dependency>> dependencies
    ) throws XMLStreamException {
        String framework = "any";

        while (reader.hasNext()) {
            int event = reader.next();

            if (isDependenciesEnd(reader, event)) {
                return;
            }

            if (event != XMLStreamReader.START_ELEMENT) {
                continue;
            }

            framework = processDependencyElement(
                    reader,
                    dependencies,
                    framework
            );
        }

        throw new PackageException("Unclosed dependencies element");
    }

    private static boolean isDependenciesEnd(
            XMLStreamReader reader,
            int event
    ) {
        return event == XMLStreamReader.END_ELEMENT
                && reader.getLocalName().equals("dependencies");
    }

    private static String processDependencyElement(
            XMLStreamReader reader,
            Map<String, List<Dependency>> dependencies,
            String framework
    ) throws XMLStreamException {
        return switch (reader.getLocalName()) {
            case "group" -> getFramework(reader);
            case "dependency" -> {
                addDependency(reader, dependencies, framework);
                yield framework;
            }
            default -> framework;
        };
    }

    private static String getFramework(XMLStreamReader reader) {
        String framework = reader.getAttributeValue(null, "targetFramework");
        return framework == null || framework.isBlank()
                ? "any"
                : framework;
    }

    private static void addDependency(
            XMLStreamReader reader,
            Map<String, List<Dependency>> dependencies,
            String framework
    ) throws XMLStreamException {
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
