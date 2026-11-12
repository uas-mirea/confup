import argparse.ArgParser;

public class Main {
    public static void main(String[] args) {
        ArgParser argParser = new ArgParser();

        argParser.addArg("package-name", ArgParser.ParseMode.STRING);
        argParser.addArg("package-url", ArgParser.ParseMode.STRING);
        argParser.addArg("mode", ArgParser.ParseMode.STRING);
        argParser.addArgWithDefault("print-tree", true);
        argParser.addArg("max-depth", ArgParser.ParseMode.INTEGER);

        try {
            argParser.parse(args);
        } catch (ArgParser.ArgParseException e) {
            System.out.println("Couldn't parse command line arguments!");
            System.out.println(e.getMessage());
            return;
        }

        System.out.printf(
                "package-name: %s\n", argParser.getStringArg("package-name"));
        System.out.printf(
                "package-url: %s\n", argParser.getStringArg("package-url"));
        System.out.printf(
                "mode: %s\n", argParser.getStringArg("mode"));
        System.out.printf(
                "print-tree: %b\n", argParser.getBooleanArg("print-tree"));
        System.out.printf(
                "max-depth: %d\n", argParser.getIntegerArg("max-depth"));
    }
}
