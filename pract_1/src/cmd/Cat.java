package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class Cat implements Command {
    public static final String NAME = "cat";

    public String getArgTemplate() {
        return "<file>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length != 2) {
            return 1;
        }
        try {
            VirtualFs.File file = state.getFs()
                    .getFileByRelativePath(state.getCwd(), args[1]);
            stdout.print(file.readString());
            return 0;
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }
    }
}