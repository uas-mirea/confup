import argparse.ArgParser;
import nuget.Downloader;
import nuget.NugetClient;
import nuget.Package;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        ArgParser argParser = new ArgParser();

        argParser.addArg("package-name", ArgParser.ParseMode.STRING);
        argParser.addArg("package-url", ArgParser.ParseMode.STRING);
        argParser.addArg("package-file", ArgParser.ParseMode.STRING);

        try {
            argParser.parse(args);
        } catch (ArgParser.ArgParseException e) {
            System.out.println("Failure parsing args!");
            System.out.println(e.getMessage());
            return;
        }

        String packageName = argParser.getStringArg("package-name");
        String packageUrl = argParser.getStringArg("package-url");
        String packageFile = argParser.getStringArg("package-file");

        int counter = (packageName != null ? 1 : 0)
                + (packageUrl != null ? 1 : 0)
                + (packageFile != null ? 1 : 0);

        if (counter != 1) {
            System.out.println(
                    "Must specify only package name, URL or file");
            return;
        }

        Package pkg;

        if (packageName != null || packageUrl != null) {
            try {
                if (packageName != null) {
                    System.out.println("Downloading from NuGet Gallery");
                    pkg = NugetClient.downloadByName(packageName);
                } else {
                    System.out.println("Downloading from provided URL");
                    pkg = NugetClient.downloadByUrl(packageUrl);
                }
            } catch (Downloader.DownloaderException |
                     Package.PackageException e) {
                System.out.println("Error during download!");
                System.out.println(e.getMessage());
                return;
            }
            System.out.println("Download finished");
        } else {
            pkg = NugetClient.loadByPath(packageFile);
        }

        System.out.printf("id: %s\n", pkg.id);
        System.out.printf("version: %s\n", pkg.version);
        System.out.printf("title: %s\n", pkg.title);
        System.out.printf("authors: %s\n", pkg.authors);
        System.out.printf("description: %s\n\n", pkg.description);

        for (Map.Entry<String, List<Package.Dependency>> entry :
                pkg.dependencies.entrySet()) {
            System.out.printf("dependencies for %s:\n", entry.getKey());
            for (Package.Dependency dep : entry.getValue()) {
                System.out.printf("* %s, version: %s\n",
                        dep.id(), dep.version());
            }
            System.out.print('\n');
        }
    }
}
