package cmd;

import java.util.HashMap;
import java.util.Set;

public class CommandMap {
    private static final HashMap<String, Command> commands =
            new HashMap<String, Command>();

    static {
        commands.put(Help.NAME, new Help());
        commands.put(TestCommand.NAME, new TestCommand());
    }

    public static Command get(String name) {
        return commands.get(name);
    }

    public static Set<String> getSet() {
        return commands.keySet();
    }
}