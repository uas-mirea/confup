import argparse.ArgParser;
import deptree.DependencyTree;
import deptree.DiagramBuilder;
import deptree.FakeNuget;
import nuget.Downloader;
import nuget.NugetClient;
import nuget.Package;

import java.io.IOException;

public class Main {

    private record Args(
            String packageName,
            String packageUrl,
            String packageFile,
            String dotnetVersion,
            int depth,
            boolean printTree,
            boolean buildDiagram,
            String fakeNugetPath
    ) {
    }

    public static void main(String[] args) throws IOException {
        Args parsedArgs = parseArgs(args);
        if (parsedArgs == null) {
            return;
        }
        int counter = (parsedArgs.packageName != null ? 1 : 0)
                + (parsedArgs.packageUrl != null ? 1 : 0)
                + (parsedArgs.packageFile != null ? 1 : 0);
        if (counter != 1) {
            System.out.println(
                    "Must specify only package name, URL or file");
            return;
        }

        if (parsedArgs.fakeNugetPath != null) {
            runFakeNuget(parsedArgs);
            return;
        }

        runRealNuget(parsedArgs);
    }

    private static Args parseArgs(String[] args) {
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
            return null;
        }

        return new Args(
                argParser.getStringArg("package-name"),
                argParser.getStringArg("package-url"),
                argParser.getStringArg("package-file"),
                argParser.getStringArg("dotnet-version"),
                argParser.getIntegerArg("depth"),
                argParser.getBooleanArg("print-tree"),
                argParser.getBooleanArg("build-diagram"),
                argParser.getStringArg("fake-nuget")
        );
    }

    private static void runFakeNuget(Args parsedArgs) {
        if (parsedArgs.packageName == null) {
            System.out.println(
                    "Only \"package-name\" is supported with fake NuGet");
            return;
        }

        FakeNuget fake = new FakeNuget(parsedArgs.fakeNugetPath);
        Package fakePkg = fake.getPackage(parsedArgs.packageName);
        DependencyTree dependencyTree = DependencyTree.buildFromRoot(
                fakePkg, parsedArgs.depth, "any", fake);
        if (parsedArgs.printTree) {
            dependencyTree.prettyPrint();
        }

        if (parsedArgs.buildDiagram) {
            DiagramBuilder diagramBuilder = new DiagramBuilder();
            System.out.println(diagramBuilder.buildDiagram(dependencyTree));
        }
    }

    private static void runRealNuget(Args parsedArgs) {
        Package pkg;
        if (parsedArgs.packageName != null ||
                parsedArgs.packageUrl != null) {
            try {
                if (parsedArgs.packageName != null) {
                    System.out.println("Downloading root from NuGet Gallery");
                    pkg = NugetClient.downloadByName(parsedArgs.packageName);
                } else {
                    System.out.println("Downloading root from provided URL");
                    pkg = NugetClient.downloadByUrl(parsedArgs.packageUrl);
                }
            } catch (Downloader.DownloaderException |
                     Package.PackageException e) {
                System.out.println("Error during download!");
                System.out.println(e.getMessage());
                return;
            }
        } else {
            pkg = NugetClient.loadByPath(parsedArgs.packageFile);
        }
        System.out.println("Getting info about dependencies...");
        DependencyTree dependencyTree;
        try {
            dependencyTree = DependencyTree.buildFromRoot(
                    pkg, parsedArgs.depth, parsedArgs.dotnetVersion);
        } catch (Downloader.DownloaderException e) {
            System.out.println("Error during download!");
            System.out.println(e.getMessage());
            return;
        }
        if (parsedArgs.printTree) {
            dependencyTree.prettyPrint();
        }
        if (parsedArgs.buildDiagram) {
            DiagramBuilder diagramBuilder = new DiagramBuilder();
            System.out.println(diagramBuilder.buildDiagram(dependencyTree));
        }
    }
}
