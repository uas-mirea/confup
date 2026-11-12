package nuget;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class Downloader {

    public static class DownloaderException extends RuntimeException {

        public DownloaderException() {
            super();
        }

        public DownloaderException(String message) {
            super(message);
        }

        public DownloaderException(Exception exception) {
            super(exception);
        }
    }

    public static class PackageNotFoundException extends DownloaderException {

        public PackageNotFoundException() {
            super();
        }

        public PackageNotFoundException(String message) {
            super(message);
        }

        public PackageNotFoundException(Exception exception) {
            super(exception);
        }
    }

    private static final String NUGET_GALLERY_DOWNLOAD_API =
            "https://www.nuget.org/api/v2/package/";

    public static void downloadByUrlString(
            String urlString, OutputStream destination) {
        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            throw new DownloaderException(e);
        }

        downloadByUri(uri, destination);
    }

    public static void downloadByUri(URI uri, OutputStream destination) {
        String scheme = uri.getScheme();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new DownloaderException("Unsupported protocol");
        }

        HttpURLConnection connection;
        InputStream inputStream;

        try {
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int status = connection.getResponseCode();
            if (status < 200 || status > 299) {
                if (status == 404) {
                    throw new PackageNotFoundException("Package not found");
                } else if (status == 403) {
                    throw new DownloaderException("Access denied");
                } else if (status >= 500 && status <= 599) {
                    throw new DownloaderException("Internal server error");
                } else {
                    throw new DownloaderException(
                            "Unexpected HTTP status code " + status);
                }
            }
            inputStream = connection.getInputStream();
            inputStream.transferTo(destination);
            inputStream.close();
        } catch (IOException e) {
            throw new DownloaderException(e);
        }
    }

    public static void downloadByPackageName(
            String packageName, OutputStream destination) {
        if (packageName == null ||
                packageName.isBlank() ||
                packageName.contains("/") ||
                packageName.contains("\\") ||
                packageName.contains("..")) {
            throw new DownloaderException("Invalid package name");
        }

        downloadByUrlString(
                NUGET_GALLERY_DOWNLOAD_API + packageName, destination);
    }
}
