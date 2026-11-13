import java.util.ArrayList;

public final class ArgParser {
    private enum ParseMode {
        NORMAL, ESCAPED, QUOTED, SKIPPING
    }

    private static class ParseState {
        public ArrayList<String> parsedArgs = new ArrayList<String>();
        public StringBuilder currentArg = new StringBuilder();
        public ParseMode parseMode = ParseMode.SKIPPING;
        public ParseMode retParseMode = ParseMode.NORMAL;

        public ParseState() {
        }
    }

    public static String[] parseArgs(String command) {
        ParseState state = new ParseState();

        for (int i = 0; i < command.length(); ) {
            char c = command.charAt(i);

            switch (state.parseMode) {
                case NORMAL:
                    switch (c) {
                        case ' ':
                            state.parsedArgs.add(state.currentArg.toString());
                            state.currentArg = new StringBuilder();
                            state.parseMode = ParseMode.SKIPPING;
                            break;

                        case '\\':
                            state.retParseMode = state.parseMode;
                            state.parseMode = ParseMode.ESCAPED;
                            i++;
                            continue;

                        default:
                            state.currentArg.append(c);
                            i++;
                            continue;
                    }
                    continue;

                case ESCAPED:
                    switch (c) {
                        case '\\':
                            state.currentArg.append('\\');
                            break;
                        case 'n':
                            state.currentArg.append('\n');
                            break;
                        case 't':
                            state.currentArg.append('\t');
                            break;
                        case '"':
                            state.currentArg.append('"');
                            break;
                        case '\'':
                            state.currentArg.append('\'');
                            break;
                        case ' ':
                            state.currentArg.append(' ');
                            break;
                        case '\n':
                            break;
                        default:
                            break;
                    }
                    state.parseMode = state.retParseMode;
                    i++;
                    continue;

                case QUOTED:
                    switch (c) {
                        case '\\':
                            state.retParseMode = state.parseMode;
                            state.parseMode = ParseMode.ESCAPED;
                            i++;
                            continue;

                        case '"':
                            state.parseMode = ParseMode.SKIPPING;
                            i++;
                            break;

                        default:
                            state.currentArg.append(c);
                            i++;
                            continue;
                    }
                    break;

                case SKIPPING:
                    if (c == ' ') {
                        i++;
                    } else if (c == '"') {
                        state.parseMode = ParseMode.QUOTED;
                        i++;
                    } else {
                        state.parseMode = ParseMode.NORMAL;
                    }
                    continue;
            }

            state.parsedArgs.add(state.currentArg.toString());
            state.currentArg = new StringBuilder();
        }

        if (!state.currentArg.isEmpty()) {
            state.parsedArgs.add(state.currentArg.toString());
        }

        return state.parsedArgs.toArray(
                new String[state.parsedArgs.size()]);
    }
}