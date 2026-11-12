package cmd;

import dirfs.VirtualFs;

import java.io.PrintStream;

public final class Ls implements Command {
    public static final String NAME = "ls";

    public String getArgTemplate() {
        return "[directory]";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        VirtualFs.File dir = state.getCwd();
        if (args.length >= 2) {
            try {
                dir = state.getFs()
                        .getFileByRelativePath(state.getCwd(), args[1]);
            } catch (VirtualFs.VfsException e) {
                stdout.printf("File \"%s\" not found\n", args[1]);
                return 1;
            } catch (Exception e) {
                stdout.println(e.getMessage());
                return 2;
            }
        }

        try {
            String[] files = dir.list();
            for (String file : files) {
                stdout.println(file);
            }
        } catch (VirtualFs.VfsException e) {
            stdout.println(e.getMessage());
            return 3;
        }
        return 0;
    }
}