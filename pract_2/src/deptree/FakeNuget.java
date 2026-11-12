
package deptree;

import nuget.Package;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class FakeNuget {

    private final Map<String, Package> packages = new HashMap<>();

    public FakeNuget(String filePath) {
        load(filePath);
    }

    private void load(String filePath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Cannot read fake NuGet repository \""
                            + filePath + "\"", e);
        }
        String currentPackage = null;
        Map<String, List<Package.Dependency>> dependencies =
                new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(">")) {
                if (currentPackage == null) {
                    throw new IllegalArgumentException(
                            "Dependency exists without a package");
                }
                String dependency = line.substring(1).trim();
                dependencies.get(currentPackage).add(
                        new Package.Dependency(dependency, "1.0.0"));

            } else {
                if (packages.containsKey(line)
                        || dependencies.containsKey(line)) {
                    throw new IllegalArgumentException(
                            "Duplicate package \"" + line + "\"");
                }
                currentPackage = line;
                dependencies.put(currentPackage, new ArrayList<>());
            }
        }

        for (Map.Entry<String, List<Package.Dependency>> entry
                : dependencies.entrySet()) {

            for (Package.Dependency dependency : entry.getValue()) {
                if (!dependencies.containsKey(dependency.id())) {
                    throw new IllegalArgumentException(
                            "Package \"" + entry.getKey()
                                    + "\" depends on missing package \""
                                    + dependency.id() + "\"");
                }
            }
        }

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
