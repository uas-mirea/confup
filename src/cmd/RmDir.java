package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class RmDir implements Command {
    public static final String NAME = "rmdir";

    public String getArgTemplate() {
        return "<directory>";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        if (args.length != 2) {
            return 1;
        }

        try {
            VirtualFs.File dir = state.getFs()
                    .getFileByRelativePath(state.getCwd(), args[1]);

            if (!dir.isDirectory()) {
                stdout.println("Not a directory");
                return 1;
            }

            dir.deleteRecursively();
            return 0;
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 1;
        }
    }
}