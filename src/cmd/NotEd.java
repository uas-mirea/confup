package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;
import java.util.Scanner;

public final class NotEd implements Command {
    public static final String NAME = "noted";

    public String getArgTemplate() {
        return "<file>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length < 2) {
            stdout.println("Please provide a file");
            return 1;
        }

        VirtualFs fs = state.getFs();
        VirtualFs.File file;
        try {
            file = fs.getFileByRelativePath(state.getCwd(), args[1]);
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }
        if (!file.isFile()) {
            stdout.println("The provided file is not a file");
            return 1;
        }

        Scanner sc = state.getStdinScanner();
        StringBuilder wbuf = new StringBuilder();

        for (;;) {
            stdout.print("> ");
            String editorCommand = sc.nextLine().strip();

            switch (editorCommand) {
                case "write":
                    wbuf.append(sc.nextLine());
                    break;

                case "writeln":
                    wbuf.append(sc.nextLine());
                    wbuf.append('\n');
                    break;

                case "save":
                    try {
                        file.writeString(wbuf.toString());
                    } catch (VirtualFs.VfsException e) {
                        stdout.println("""
                                An error occurred while trying
                                to write to output file""");
                        stdout.println(e.getMessage());
                    }
                    break;

                case "done":
                    return 0;

                default:
                    stdout.println("I do not know this command");
                    break;
            }
        }
    }
}
