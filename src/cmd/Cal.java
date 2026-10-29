package cmd;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public final class Cal implements Command {
    public static final String NAME = "cal";

    private static final String HEADER = "Su Mo Tu We Th Fr Sa";

    public String getArgTemplate() {
        return "";
    }

    public int run(String[] args, State state) {
        PrintStream stdout = state.getStdout();

        YearMonth month = YearMonth.now();
        Locale locale = Locale.US;

        String monthName = month.getMonth()
                .getDisplayName(TextStyle.FULL, locale) + " "
                + month.getYear();
        stdout.print(" ".repeat((HEADER.length() - monthName.length()) / 2));
        stdout.println(monthName);
        stdout.println(HEADER);

        LocalDate firstDay = month.atDay(1);
        int offset = firstDay.getDayOfWeek().getValue() % 7;
        stdout.print("   ".repeat(offset));
        int day = 1;
        for (; day <= month.lengthOfMonth(); day++) {
            stdout.printf("%2d ", day);
            if ((offset + day) % 7 == 0) {
                stdout.println();
            }
        }
        if ((day + offset - 1) % 7 != 0) {
            stdout.println();
        }

        return 0;
    }
}
