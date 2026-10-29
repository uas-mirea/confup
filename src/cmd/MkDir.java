package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class MkDir implements Command {
    public static final String NAME = "mkdir";

    public String getArgTemplate() {
        return "<directory>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length != 2) {
            stdout.println("Usage: mkdir <directory>");
            return 1;
        }

        try {
            VirtualFs fs = state.getFs();
            fs.createDirectory(state.getCwd(), args[1]);
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }
        return 0;
    }
}
