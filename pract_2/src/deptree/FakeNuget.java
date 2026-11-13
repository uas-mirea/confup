
package deptree;

import nuget.Package;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeNuget {

    private final Map<String, Package> packages = new HashMap<>();

    public FakeNuget(String filePath) {
        load(filePath);
    }

    private void load(String filePath) {
        Map<String, List<Package.Dependency>> dependencies =
                parsePackages(readLines(filePath));

        validateDependencies(dependencies);
        createPackages(dependencies);
    }

    private List<String> readLines(String filePath) {
        try {
            return Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Cannot read fake NuGet repository \""
                            + filePath + "\"", e);
        }
    }

    private Map<String, List<Package.Dependency>> parsePackages(
            List<String> lines) {
        Map<String, List<Package.Dependency>> dependencies =
                new HashMap<>();

        String currentPackage = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith(">")) {
                addDependency(dependencies, currentPackage, line);
                continue;
            }

            currentPackage = line;
            addPackage(dependencies, currentPackage);
        }

        return dependencies;
    }

    private void addPackage(
            Map<String, List<Package.Dependency>> dependencies,
            String packageName) {
        if (dependencies.containsKey(packageName)) {
            throw new IllegalArgumentException(
                    "Duplicate package \"" + packageName + "\"");
        }

        dependencies.put(packageName, new ArrayList<>());
    }

    private void addDependency(
            Map<String, List<Package.Dependency>> dependencies,
            String currentPackage,
            String line) {
        if (currentPackage == null) {
            throw new IllegalArgumentException(
                    "Dependency exists without a package");
        }

        String dependency = line.substring(1).trim();

        dependencies.get(currentPackage).add(
                new Package.Dependency(dependency, "1.0.0"));
    }

    private void validateDependencies(
            Map<String, List<Package.Dependency>> dependencies) {
        for (Map.Entry<String, List<Package.Dependency>> entry
                : dependencies.entrySet()) {

            for (Package.Dependency dependency : entry.getValue()) {
                validateDependency(
                        dependencies, entry.getKey(), dependency);
            }
        }
    }

    private void validateDependency(
            Map<String, List<Package.Dependency>> dependencies,
            String packageName,
            Package.Dependency dependency) {
        if (dependencies.containsKey(dependency.id())) {
            return;
        }

        throw new IllegalArgumentException(
                "Package \"" + packageName
                        + "\" depends on missing package \""
                        + dependency.id() + "\"");
    }

    private void createPackages(
            Map<String, List<Package.Dependency>> dependencies) {
        for (Map.Entry<String, List<Package.Dependency>> entry
                : dependencies.entrySet()) {
            String name = entry.getKey();

            packages.put(name, Package.createFake(
                    name, entry.getValue()));
        }
    }

    public Package getPackage(String name) {
        Package pkg = packages.get(name);

        if (pkg == null) {
            throw new IllegalArgumentException(
                    "Package not found in fake NuGet: " + name);
        }

        return pkg;
    }
}