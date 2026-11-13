import java.util.ArrayList;

public final class ArgParser {
    private enum ParseMode {
        NORMAL, ESCAPED, QUOTED, SKIPPING
    }

    private static class ParseState {
        ArrayList<String> parsedArgs = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        ParseMode parseMode = ParseMode.SKIPPING;
        ParseMode retParseMode = ParseMode.NORMAL;
    }

    public static String[] parseArgs(String command) {
        ParseState state = new ParseState();

        for (int i = 0; i < command.length();) {
            i = switch (state.parseMode) {
                case NORMAL -> parseNormal(command, i, state);
                case ESCAPED -> parseEscaped(command, i, state);
                case QUOTED -> parseQuoted(command, i, state);
                case SKIPPING -> parseSkipping(command, i, state);
            };
        }

        if (!state.currentArg.isEmpty()) {
            state.parsedArgs.add(state.currentArg.toString());
        }

        return state.parsedArgs.toArray(new String[0]);
    }

    private static int parseNormal(String command, int i, ParseState state) {
        char c = command.charAt(i);

        if (c == ' ') {
            state.parsedArgs.add(state.currentArg.toString());
            state.currentArg = new StringBuilder();
            state.parseMode = ParseMode.SKIPPING;
            return i;
        }

        if (c == '\\') {
            state.retParseMode = ParseMode.NORMAL;
            state.parseMode = ParseMode.ESCAPED;
            return i + 1;
        }

        state.currentArg.append(c);
        return i + 1;
    }

    private static int parseEscaped(String command, int i, ParseState state) {
        char c = command.charAt(i);

        switch (c) {
            case '\\': state.currentArg.append('\\'); break;
            case 'n':  state.currentArg.append('\n'); break;
            case 't':  state.currentArg.append('\t'); break;
            case '"':  state.currentArg.append('"'); break;
            case '\'': state.currentArg.append('\''); break;
            case ' ':  state.currentArg.append(' '); break;
        }

        state.parseMode = state.retParseMode;
        return i + 1;
    }

    private static int parseQuoted(String command, int i, ParseState state) {
        char c = command.charAt(i);

        if (c == '\\') {
            state.retParseMode = ParseMode.QUOTED;
            state.parseMode = ParseMode.ESCAPED;
            return i + 1;
        }

        if (c == '"') {
            state.parseMode = ParseMode.SKIPPING;
            finishArg(state);
            return i + 1;
        }

        state.currentArg.append(c);
        return i + 1;
    }

    private static int parseSkipping(String command, int i, ParseState state) {
        char c = command.charAt(i);

        if (c == ' ') {
            return i + 1;
        }

        if (c == '"') {
            state.parseMode = ParseMode.QUOTED;
            return i + 1;
        }

        state.parseMode = ParseMode.NORMAL;
        return i;
    }

    private static void finishArg(ParseState state) {
        state.parsedArgs.add(state.currentArg.toString());
        state.currentArg = new StringBuilder();
    }
}
