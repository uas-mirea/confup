package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class Touch implements Command {
    public static final String NAME = "touch";

    private static final int REQUIRED_SPAGHETTI = 2;

    public String getArgTemplate() {
        return "<file>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length != REQUIRED_SPAGHETTI) {
            stdout.println("Please provide path");
            return 1;
        }

        try {
            VirtualFs fs = state.getFs();
            String path = args[1];

            fs.createFile(state.getCwd(), path, "");
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }

        return 0;
    }
}