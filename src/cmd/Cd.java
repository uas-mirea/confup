package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class Cd implements Command {
    public static final String NAME = "cd";

    public String getArgTemplate() {
        return "[directory]";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length < 2) {
            stdout.println("No directory provided");
            return 1;
        }

        try {
            VirtualFs.File newDir = state.getFs()
                    .getFileByRelativePath(state.getCwd(), args[1]);
            if (!newDir.isDirectory()) {
                stdout.println("Cannot enter a non-directory");
                return 4;
            }
            state.setCwd(newDir);
        } catch (VirtualFs.VfsException e) {
            stdout.printf("File \"%s\" not found\n", args[1]);
            return 2;
        } catch (Exception e) {
            stdout.println(e.getMessage());
            return 3;
        }

        return 0;
    }
}