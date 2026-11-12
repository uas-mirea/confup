package dirfs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class VirtualFs {
    public static class VfsException extends IOException {
        public VfsException(String message) {
            super(message);
        }

        public VfsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private abstract static class Node {
        final String name;
        final DirNode parent;

        Node(String name, DirNode parent) {
            this.name = name;
            this.parent = parent;
        }
    }

    private static final class FileNode extends Node {
        byte[] content;

        FileNode(String name, DirNode parent, byte[] content) {
            super(name, parent);
            this.content = content.clone();
        }
    }

    private static final class DirNode extends Node {
        final Map<String, Node> children = new LinkedHashMap<>();

        DirNode(String name, DirNode parent) {
            super(name, parent);
        }
    }

    private static final class LinkNode extends Node {
        final String target;

        LinkNode(String name, DirNode parent, String target) {
            super(name, parent);
            this.target = target;
        }
    }

    public final class File {
        private final Node node;

        private File(Node node) {
            this.node = node;
        }

        public boolean isDirectory() {
            return node instanceof DirNode;
        }

        public boolean isFile() {
            return node instanceof FileNode;
        }

        public boolean isLink() {
            return node instanceof LinkNode;
        }

        public String getName() {
            return node.name;
        }

        public String getPath() {
            if (node == root) {
                return "/";
            }

            StringBuilder path = new StringBuilder();
            Node current = node;
            while (current != root) {
                path.insert(0, "/" + current.name);
                current = current.parent;
            }
            return path.toString();
        }

        public String[] list() throws VfsException {
            DirNode dir = requireDirectory(node);
            return dir.children.keySet().toArray(new String[0]);
        }

        public byte[] readBytes() throws VfsException {
            FileNode file = requireFile(node);
            return file.content.clone();
        }

        public String readString() throws VfsException {
            return new String(readBytes(), StandardCharsets.UTF_8);
        }

        public void writeBytes(byte[] content) throws VfsException {
            FileNode file = requireFile(node);
            if (content == null) {
                throw new VfsException("Content cannot be null");
            }
            file.content = content.clone();
        }

        public void writeString(String content) throws VfsException {
            if (content == null) {
                throw new VfsException("Content cannot be null");
            }
            writeBytes(content.getBytes(StandardCharsets.UTF_8));
        }

        public String getLinkTarget() throws VfsException {
            if (!(node instanceof LinkNode)) {
                throw new VfsException("Entry is not a symbolic link");
            }
            return ((LinkNode) node).target;
        }

        public File createFile(String name, byte[] content)
                throws VfsException {
            DirNode dir = requireDirectory(node);
            validateName(name);

            if (content == null) {
                throw new VfsException("Content cannot be null");
            }

            if (dir.children.containsKey(name)) {
                throw new VfsException("Entry already exists: " + name);
            }

            FileNode file = new FileNode(name, dir, content);
            dir.children.put(name, file);

            return new File(file);
        }

        public File createFile(String name, String content)
                throws VfsException {
            if (content == null) {
                throw new VfsException("Content cannot be null");
            }
            return createFile(name, content.getBytes(StandardCharsets.UTF_8));
        }

        public File createDirectory(String name) throws VfsException {
            DirNode dir = requireDirectory(node);
            validateName(name);

            if (dir.children.containsKey(name)) {
                throw new VfsException("Entry already exists: " + name);
            }

            DirNode child = new DirNode(name, dir);
            dir.children.put(name, child);

            return new File(child);
        }

        // Deletes a file, link, or empty directory.
        public void delete() throws VfsException {
            if (node == root) {
                throw new VfsException("Cannot delete the VFS root");
            }
            if (node instanceof DirNode &&
                    !((DirNode) node).children.isEmpty()) {
                throw new VfsException("Directory is not empty: " + getPath());
            }
            node.parent.children.remove(node.name);
        }

        // Removes a file, link, or an entire directory subtree.
        public void deleteRecursively() throws VfsException {
            if (node == root) {
                throw new VfsException("Cannot delete the VFS root");
            }
            node.parent.children.remove(node.name);
        }
    }

    private record ParentAndName(DirNode parent, String name) {
    }

    private final String fsName;
    private final DirNode root;

    public VirtualFs(String path) throws VfsException {
        if (path == null || path.isBlank()) {
            throw new VfsException("VFS root path cannot be empty");
        }

        Path rootPath = Path.of(path).toAbsolutePath().normalize();
        fsName = rootPath.getName(rootPath.getNameCount() - 1).toString();

        if (!Files.isDirectory(rootPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new VfsException("VFS root must be a directory: " + rootPath);
        }

        root = new DirNode("/", null);

        try {
            loadDirectory(rootPath, root);
        } catch (IOException e) {
            throw new VfsException("Failed to load VFS from: " + rootPath, e);
        }
    }

    private void loadDirectory(Path source, DirNode target) throws IOException {
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(source)) {
            for (Path entry : entries) {
                String name = entry.getFileName().toString();

                if (Files.isSymbolicLink(entry)) {
                    Path link = Files.readSymbolicLink(entry);
                    target.children.put(
                            name, new LinkNode(name, target, link.toString()));
                } else if (
                        Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                    DirNode dir = new DirNode(name, target);
                    target.children.put(name, dir);
                    loadDirectory(entry, dir);
                } else if (
                        Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)) {
                    byte[] content = Files.readAllBytes(entry);
                    target.children
                            .put(name, new FileNode(name, target, content));
                } else {
                    throw new IOException(
                            "Unsupported filesystem entry: " + entry);
                }
            }
        }
    }

    public File getRoot() {
        return new File(root);
    }

    public String getFsName() {
        return fsName;
    }

    // Path lookup

    public File getFileByAbsolutePath(String path) throws VfsException {
        return new File(resolve(root, path));
    }

    public File getFileByRelativePath(File relativeFile, String path)
            throws VfsException {
        validateOwner(relativeFile);
        return new File(resolve(relativeFile.node, path));
    }

    private Node resolve(Node base, String path) throws VfsException {
        if (path == null || path.isBlank()) {
            throw new VfsException("Invalid path");
        }
        Node current = path.startsWith("/") ? root : base;

        for (String part : path.replace('\\', '/').split("/")) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            }

            if (part.equals("..")) {
                if (current == root) {
                    throw new VfsException("Path escapes VFS root");
                }
                current = current.parent;
                continue;
            }

            DirNode dir = requireDirectory(current);
            Node child = dir.children.get(part);

            if (child == null) {
                throw new VfsException("Entry not found: " + path);
            }
            current = child;
        }

        return current;
    }

    // Creation

    public File createFile(
            File relativeFile, String path, byte[] content)
            throws VfsException {
        validateOwner(relativeFile);

        if (path == null || path.isBlank()) {
            throw new VfsException("Invalid path");
        }
        if (content == null) {
            throw new VfsException("Content cannot be null");
        }

        String normalized = path.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');

        String name = normalized.substring(separator + 1);
        String parentPath = separator <= 0 ?
                "." : normalized.substring(0, separator);
        validateName(name);

        Node parent = resolve(path.startsWith("/") ?
                root : relativeFile.node, parentPath);
        return new File(parent).createFile(name, content);
    }

    public File createFile(File relativeFile, String path, String content)
            throws VfsException {
        if (content == null) {
            throw new VfsException("Content cannot be null");
        }
        return createFile(relativeFile, path, content.getBytes());
    }

    public File createDirectory(File relativeFile, String path)
            throws VfsException {
        validateOwner(relativeFile);

        if (path == null || path.isBlank()) {
            throw new VfsException("Invalid path");
        }

        String normalized = path.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');

        String name = normalized.substring(separator + 1);
        String parentPath = separator <= 0 ?
                "." : normalized.substring(0, separator);

        validateName(name);
        Node parent = resolve(path.startsWith("/") ?
                root : relativeFile.node, parentPath);

        return new File(parent).createDirectory(name);
    }

    // Validation

    private void validateOwner(File file) throws VfsException {
        if (file == null) {
            throw new VfsException("File cannot be null");
        }

        Node current = file.node;
        while (current.parent != null) {
            current = current.parent;
        }

        if (current != root) {
            throw new VfsException("Entry does not belong to this VFS");
        }
    }

    private static void validateName(String name) throws VfsException {
        if (name == null || name.isBlank()
                || name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\")
                || name.indexOf('\0') >= 0) {
            throw new VfsException("Invalid entry name: " + name);
        }
    }

    private static DirNode requireDirectory(Node node) throws VfsException {
        if (!(node instanceof DirNode)) {
            throw new VfsException("Not a directory: " + node.name);
        }
        return (DirNode) node;
    }

    private static FileNode requireFile(Node node) throws VfsException {
        if (!(node instanceof FileNode)) {
            throw new VfsException("Not a regular file: " + node.name);
        }
        return (FileNode) node;
    }
}