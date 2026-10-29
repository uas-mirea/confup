package cmd;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Date implements Command {
    public static final String NAME = "date";

    public String getArgTemplate() {
        return "";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEE MMM dd HH:mm:ss z yyyy",
                Locale.US
        );
        String time = ZonedDateTime.now(ZoneId.systemDefault())
                .format(formatter);
        stdout.println(time);
        return 0;
    }
}
