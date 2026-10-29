package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class Cp implements Command {
    public static final String NAME = "cp";

    public String getArgTemplate() {
        return "<from_file> <to_file>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length < 3) {
            stdout.println("Not enough arguments");
            return 1;
        }

        try {
            VirtualFs fs = state.getFs();
            VirtualFs.File src = fs.getFileByRelativePath(
                    state.getCwd(), args[1]);
            if (!src.isFile()) {
                stdout.println("Source must be a file");
                return 1;
            }
            fs.createFile(state.getCwd(), args[2], src.readBytes());
            return 0;
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }
    }
}