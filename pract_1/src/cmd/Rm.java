package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class Rm implements Command {
    public static final String NAME = "rm";

    private static final int REQUIRED_SPAGHETTI = 2;

    public String getArgTemplate() {
        return "<file>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length != REQUIRED_SPAGHETTI) {
            return 1;
        }

        try {
            VirtualFs.File file = state.getFs()
                    .getFileByRelativePath(state.getCwd(), args[1]);

            if (file.isDirectory()) {
                stdout.println("Cannot remove a directory");
                return 1;
            }

            file.delete();
            return 0;
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }
    }
}