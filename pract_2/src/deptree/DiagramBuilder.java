
package deptree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiagramBuilder {
    private record PackageDependencyPair(String id, String dependency) {
    }

    private List<PackageDependencyPair> pairs = new ArrayList<>();
    private Set<String> visited = new HashSet<>();

    public DiagramBuilder() {
    }

    public String buildDiagram(DependencyTree tree) {
        pairs = new ArrayList<>();
        visited = new HashSet<>();

        if (tree.getRoot() != null) {
            addDeps(tree.getRoot());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@startuml\n");
        for (PackageDependencyPair pair : pairs) {
            stringBuilder.append(String.format("[%s] --> [%s]\n",
                    pair.id, pair.dependency));
        }
        stringBuilder.append("@enduml\n");

        return stringBuilder.toString();
    }

    private void addDeps(DependencyTree.Leaf leaf) {
        if (!visited.add(leaf.pkg.id)) {
            return;
        }
        for (DependencyTree.Leaf dependency : leaf.dependencies) {
            pairs.add(new PackageDependencyPair(
                    leaf.pkg.id, dependency.pkg.id));
            addDeps(dependency);
        }
    }
}
