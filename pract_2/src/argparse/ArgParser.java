package argparse;

import java.util.HashMap;

public class ArgParser {
    public static class ArgParseException extends RuntimeException {

        public ArgParseException() {
            super();
        }

        public ArgParseException(String message) {
            super(message);
        }

        public ArgParseException(Exception exception) {
            super(exception);
        }
    }

    public enum ParseMode {
        INTEGER,
        STRING,
        BOOLEAN
    }

    private enum ParserState {
        SEARCHING_FOR_ARG,
        PARSING_ARG,
        PARSING_BOOLEAN_ARG
    }


    private final HashMap<String, ParseMode> argNames = new HashMap<>();
    private final HashMap<String, Object> defaultArgs = new HashMap<>();
    private final HashMap<String, Object> args = new HashMap<>();

    private ParserState state = ParserState.SEARCHING_FOR_ARG;
    private int current = 0;
    private String argName = null;
    private String[] argsArray = null;

    public void parse(String[] argsArray) {
        state = ParserState.SEARCHING_FOR_ARG;
        current = 0;
        argName = null;
        this.argsArray = argsArray;

        while (current < argsArray.length) {
            parseLoop();
        }

        if (state == ParserState.PARSING_BOOLEAN_ARG) {
            args.put(argName, true);
        } else if (state == ParserState.PARSING_ARG) {
            throw new ArgParseException(
                    "Failed to parse argument \"" + argName + "\"");
        }
    }

    private void parseLoop() {
        switch (state) {
            case SEARCHING_FOR_ARG:
                modeSearchingForArg();
                break;

            case PARSING_ARG:
                modeParsingArg();
                break;

            case PARSING_BOOLEAN_ARG:
                modeParsingBooleanArg();
                break;
        }
    }

    private void modeSearchingForArg() {
        String part = argsArray[current];
        if (!part.startsWith("-")) {
            throw new ArgParseException(
                    "Unexpected word \"" + part + "\"");
        }

        int equalsIndex = part.indexOf('=');
        String argValue = null;
        if (equalsIndex == -1) {
            argName = part.substring(1);
        } else {
            argName = part.substring(1, equalsIndex);
            argValue = part.substring(equalsIndex + 1);
        }

        if (!argNames.containsKey(argName)) {
            throw new ArgParseException(
                    "Unknown argument \"" + argName + "\"");
        }

        if (argValue != null) {
            args.put(argName, parseCorrectly(argName, argValue));
        } else {
            state = ParserState.PARSING_ARG;
            if (argNames.get(argName) == ParseMode.BOOLEAN) {
                state = ParserState.PARSING_BOOLEAN_ARG;
            }
        }
        current++;
    }

    private void modeParsingArg() {
        args.put(argName,
                parseCorrectly(argName, argsArray[current]));
        current++;
        state = ParserState.SEARCHING_FOR_ARG;
    }

    private void modeParsingBooleanArg() {
        String currentPart = argsArray[current];
        if (currentPart.startsWith("-")) {
            args.put(argName, true);
        } else {
            args.put(argName,
                    parseCorrectly(argName, argsArray[current]));
            current++;
        }
        state = ParserState.SEARCHING_FOR_ARG;
    }

    private Object parseCorrectly(String name, String value) {
        ParseMode parseMode = argNames.get(name);
        return switch (parseMode) {
            case STRING -> value;
            case INTEGER -> Integer.parseInt(value);
            case BOOLEAN -> {
                if (value.isEmpty() || value.equals("0") ||
                        value.equalsIgnoreCase("false")) {
                    yield false;
                }
                yield true;
            }
        };
    }

    public void addArg(String name, ParseMode parseMode) {
        if (name == null || name.isBlank()) {
            throw new ArgParseException("Name cannot be empty or missing");
        }

        if (argNames.containsKey(name)) {
            throw new ArgParseException("Argument already exists");
        }
        argNames.put(name, parseMode);
    }

    public void addArgWithDefault(String name, Object defaultValue) {
        if (name == null || name.isBlank()) {
            throw new ArgParseException("Name cannot be empty or missing");
        }

        if (defaultValue == null) {
            throw new ArgParseException(
                    "Default value cannot be empty or missing");
        }

        ParseMode parseMode = switch (defaultValue) {
            case String s -> ParseMode.STRING;
            case Integer i -> ParseMode.INTEGER;
            case Boolean b -> ParseMode.BOOLEAN;
            default -> {
                throw new ArgParseException("Unknown default value type");
            }
        };

        if (argNames.containsKey(name)) {
            throw new ArgParseException("Argument already exists");
        }
        argNames.put(name, parseMode);
        defaultArgs.put(name, defaultValue);
    }

    public Object getArg(String name) {
        if (args.containsKey(name)) {
            return args.get(name);
        }
        return defaultArgs.get(name);
    }

    public String getStringArg(String name) {
        Object o = getArg(name);
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }

    public Integer getIntegerArg(String name) {
        Object o = getArg(name);
        if (o instanceof Integer) {
            return (Integer) o;
        }
        return null;
    }

    public Boolean getBooleanArg(String name) {
        Object o = getArg(name);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return null;
    }
}
