package cmd;

import java.io.PrintStream;

public final class TestCommand implements Command {
    public static final String NAME = "test";

    public String getArgTemplate() {
        return "[arg]...";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        stdout.printf("Name: %s\n", args[0]);
        for (int i = 1; i < args.length; i++) {
            stdout.printf("Arg %d: %s\n", i, args[i]);
        }
        return 0;
    }
}