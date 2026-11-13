import argparse.ArgParser;
import deptree.DependencyTree;
import deptree.DiagramBuilder;
import deptree.FakeNuget;
import nuget.Downloader;
import nuget.NugetClient;
import nuget.Package;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ArgParser argParser = new ArgParser();

        argParser.addArg("package-name", ArgParser.ParseMode.STRING);
        argParser.addArg("package-url", ArgParser.ParseMode.STRING);
        argParser.addArg("package-file", ArgParser.ParseMode.STRING);
        argParser.addArgWithDefault("dotnet-version", "net10.0");
        argParser.addArgWithDefault("depth", 5);
        argParser.addArg("fake-nuget", ArgParser.ParseMode.STRING);
        argParser.addArgWithDefault("print-tree", false);
        argParser.addArgWithDefault("build-diagram", true);

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
        String dotnetVersion = argParser.getStringArg("dotnet-version");
        int depth = argParser.getIntegerArg("depth");
        boolean printTree = argParser.getBooleanArg("print-tree");
        boolean buildDiagram = argParser.getBooleanArg("build-diagram");

        int counter = (packageName != null ? 1 : 0)
                + (packageUrl != null ? 1 : 0)
                + (packageFile != null ? 1 : 0);

        if (counter != 1) {
            System.out.println(
                    "Must specify only package name, URL or file");
            return;
        }

        String fakeNugetPath = argParser.getStringArg("fake-nuget");
        if (fakeNugetPath != null) {
            if (packageName == null) {
                System.out.println(
                        "Only \"package-name\" is supported with fake NuGet");
            }

            FakeNuget fake = new FakeNuget(fakeNugetPath);
            Package fakePkg = fake.getPackage(packageName);
            DependencyTree dependencyTree = DependencyTree.buildFromRoot(
                    fakePkg, depth, "any", fake);
            if (printTree) {
                dependencyTree.prettyPrint();
            }

            if (buildDiagram) {
                DiagramBuilder diagramBuilder = new DiagramBuilder();
                System.out.println(diagramBuilder.buildDiagram(dependencyTree));
            }

            return;
        }

        Package pkg;

        if (packageName != null || packageUrl != null) {
            try {
                if (packageName != null) {
                    System.out.println("Downloading root from NuGet Gallery");
                    pkg = NugetClient.downloadByName(packageName);
                } else {
                    System.out.println("Downloading root from provided URL");
                    pkg = NugetClient.downloadByUrl(packageUrl);
                }
            } catch (Downloader.DownloaderException |
                     Package.PackageException e) {
                System.out.println("Error during download!");
                System.out.println(e.getMessage());
                return;
            }
        } else {
            pkg = NugetClient.loadByPath(packageFile);
        }

        System.out.println("Getting info about dependencies...");
        DependencyTree dependencyTree;
        try {
            dependencyTree = DependencyTree.buildFromRoot(
                    pkg, depth, dotnetVersion);
        } catch (Downloader.DownloaderException e) {
            System.out.println("Error during download!");
            System.out.println(e.getMessage());
            return;
        }
        if (printTree) {
            dependencyTree.prettyPrint();
        }

        if(buildDiagram) {
            DiagramBuilder diagramBuilder = new DiagramBuilder();
            System.out.println(diagramBuilder.buildDiagram(dependencyTree));
        }
    }
}
