package cmd;

public interface Command {
    String getArgTemplate();

    int run(String[] args, State state);
}