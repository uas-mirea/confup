package nuget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipFile;

public class NugetClient {

    public static class NugetClientException extends RuntimeException {

        public NugetClientException() {
            super();
        }

        public NugetClientException(String message) {
            super(message);
        }

        public NugetClientException(Exception exception) {
            super(exception);
        }
    }

    final static String tempFolder =
            System.getProperty("java.io.tmpdir");

    public static Package downloadByName(String packageName) {
        String pathToPackage = randomFilename();
        try (FileOutputStream outputStream =
                     new FileOutputStream(pathToPackage)) {
            Downloader.downloadByPackageName(packageName, outputStream);
        } catch (IOException e) {
            throw new NugetClientException(e);
        }
        Package pkg = loadByPath(pathToPackage);
        new File(pathToPackage).delete();
        return pkg;
    }

    public static Package downloadByUrl(String packageUrl) {
        String pathToPackage = randomFilename();
        try (FileOutputStream outputStream =
                     new FileOutputStream(pathToPackage)) {
            Downloader.downloadByUrlString(packageUrl, outputStream);
        } catch (IOException e) {
            throw new NugetClientException(e);
        }
        Package pkg = loadByPath(pathToPackage);
        new File(pathToPackage).delete();
        return pkg;
    }

    public static Package loadByPath(String pathToPackage) {
        try (ZipFile zipFile = new ZipFile(pathToPackage)) {
            return Package.fromZipFile(zipFile);
        } catch (IOException e) {
            throw new NugetClientException(e);
        }
    }

    private static String randomFilename() {
        String randomName = UUID.randomUUID().toString();
        Path joinedPath = Paths.get(tempFolder,
                randomName + "_confup.nupkg");
        return joinedPath.toString();
    }
}
