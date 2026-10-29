package cmd;

import java.io.PrintStream;
import java.util.Set;

public final class Help implements Command {
    public static final String NAME = "help";

    public String getArgTemplate() {
        return "";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        stdout.println("System commands:");
        Set<String> cmdSet = CommandMap.getSet();
        for (String command : cmdSet) {
            Command cmd = CommandMap.get(command);
            stdout.printf("%s %s\n", command, cmd.getArgTemplate());
        }

        return 0;
    }
}
