package cmd;

import java.util.HashMap;
import java.util.Set;

public class CommandMap {
    private static final HashMap<String, Command> commands =
            new HashMap<String, Command>();

    static {
        commands.put(Help.NAME, new Help());
        commands.put(TestCommand.NAME, new TestCommand());
        commands.put(Cd.NAME, new Cd());
        commands.put(Ls.NAME, new Ls());
        commands.put(Rm.NAME, new Rm());
        commands.put(RmDir.NAME, new RmDir());
        commands.put(Touch.NAME, new Touch());
        commands.put(Cat.NAME, new Cat());
        commands.put(Cp.NAME, new Cp());
        commands.put(NotEd.NAME, new NotEd());
        commands.put(MkDir.NAME, new MkDir());
    }

    public static Command get(String name) {
        return commands.get(name);
    }

    public static Set<String> getSet() {
        return commands.keySet();
    }
}