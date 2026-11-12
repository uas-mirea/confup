package deptree;

import nuget.NugetClient;
import nuget.Package;

import java.util.*;

public class DependencyTree {

    public static class Leaf {
        public Package pkg;
        public List<Leaf> dependencies = new ArrayList<>();

        private Leaf(Package pkg) {
            this.pkg = pkg;
        }
    }

    private record QueueEntry(Leaf leaf, int depth) {
    }

    private Leaf root;

    public static DependencyTree buildFromRoot(
            Package root, int depth, String dotnetVersion) {
        return buildFromRoot(root, depth, dotnetVersion, null);
    }

    public static DependencyTree buildFromRoot(
            Package root, int depth, String dotnetVersion,
            FakeNuget fakeNuget) {

        DependencyTree tree = new DependencyTree();
        tree.root = new Leaf(root);

        // Stores one Leaf per package ID.
        Map<String, Leaf> discovered = new HashMap<>();
        discovered.put(root.id, tree.root);

        // Tracks packages whose dependencies have already been expanded.
        Set<String> expanded = new HashSet<>();

        Queue<QueueEntry> queue = new ArrayDeque<>();
        queue.add(new QueueEntry(tree.root, 0));

        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            Leaf current = entry.leaf();
            if (entry.depth() >= depth) {
                continue;
            }
            if (!expanded.add(current.pkg.id)) {
                continue;
            }

            List<Package.Dependency> dependencies =
                    current.pkg.dependencies.get(dotnetVersion);
            if (dependencies == null) {
                dependencies = current.pkg.dependencies.get("any");
            }
            if (dependencies == null) {
                continue;
            }

            for (Package.Dependency dependency : dependencies) {
                Leaf child = discovered.get(dependency.id());
                if (child == null) {
                    Package pkg;
                    if (fakeNuget == null) {
                        pkg = NugetClient.downloadByName(dependency.id());
                    } else {
                        pkg = fakeNuget.getPackage(dependency.id());
                    }
                    child = new Leaf(pkg);
                    discovered.put(pkg.id, child);
                }
                current.dependencies.add(child);

                if (!expanded.contains(child.pkg.id)) {
                    queue.add(new QueueEntry(
                            child, entry.depth() + 1));
                }
            }
        }

        return tree;
    }

    public Leaf getRoot() {
        return root;
    }

    public void prettyPrint() {
        if (root == null) {
            return;
        }
        prettyPrint(root, 0, new HashSet<>());
    }

    private void prettyPrint(
            Leaf leaf, int depth, Set<String> visited) {
        System.out.println("> ".repeat(depth) + leaf.pkg.id
                + " (" + leaf.pkg.version + ")");
        if (!visited.add(leaf.pkg.id)) {
            return;
        }
        for (Leaf child : leaf.dependencies) {
            prettyPrint(child, depth + 1, visited);
        }
    }
}
