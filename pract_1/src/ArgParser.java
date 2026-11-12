import java.util.ArrayList;

public final class ArgParser {
    private enum ParseMode {
        NORMAL, ESCAPED, QUOTED, SKIPPING
    }

    public static String[] parseArgs(String command) {
        ArrayList<String> parsedArgs = new ArrayList<String>();
        StringBuilder currentArg = new StringBuilder();
        ParseMode parseMode = ParseMode.SKIPPING;
        ParseMode retParseMode = ParseMode.NORMAL;

        for (int i = 0; i < command.length(); ) {
            char c = command.charAt(i);

            switch (parseMode) {
                case NORMAL:
                    switch (c) {
                        case ' ':
                            parsedArgs.add(currentArg.toString());
                            currentArg = new StringBuilder();
                            parseMode = ParseMode.SKIPPING;
                            break;

                        case '\\':
                            retParseMode = parseMode;
                            parseMode = ParseMode.ESCAPED;
                            i++;
                            continue;

                        default:
                            currentArg.append(c);
                            i++;
                            continue;
                    }
                    continue;

                case ESCAPED:
                    switch (c) {
                        case '\\':
                            currentArg.append('\\');
                            break;
                        case 'n':
                            currentArg.append('\n');
                            break;
                        case 't':
                            currentArg.append('\t');
                            break;
                        case '"':
                            currentArg.append('"');
                            break;
                        case '\'':
                            currentArg.append('\'');
                            break;
                        case ' ':
                            currentArg.append(' ');
                            break;
                        case '\n':
                            break;
                        default:
                            break;
                    }
                    parseMode = retParseMode;
                    i++;
                    continue;

                case QUOTED:
                    switch (c) {
                        case '\\':
                            retParseMode = parseMode;
                            parseMode = ParseMode.ESCAPED;
                            i++;
                            continue;

                        case '"':
                            parseMode = ParseMode.SKIPPING;
                            i++;
                            break;

                        default:
                            currentArg.append(c);
                            i++;
                            continue;
                    }
                    break;

                case SKIPPING:
                    if (c == ' ') {
                        i++;
                    } else if (c == '"') {
                        parseMode = ParseMode.QUOTED;
                        i++;
                    } else {
                        parseMode = ParseMode.NORMAL;
                    }
                    continue;
            }

            parsedArgs.add(currentArg.toString());
            currentArg = new StringBuilder();
        }

        if (currentArg.length() != 0) {
            parsedArgs.add(currentArg.toString());
        }

        String[] arr = parsedArgs.toArray(new String[parsedArgs.size()]);
        return arr;
    }
}