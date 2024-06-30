package account.utils;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static List<String> breachedPassword = List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    public static String getMonthName(String period) {
        String monthNumber = period.split("-")[0];
        int month = Integer.parseInt(monthNumber);
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month number must be between 01 and 12");
        }
        Month monthEnum = Month.of(month);
        return monthEnum.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static String getYear(String period) {
        return period.split("-")[1];
    }

    public static String getConcatenatedPeriod(String period) {
        return getMonthName(period) + "-" + getYear(period);
    }

    public static String convertCentsToDollarsAndCents(Long salary) {
        int dollars = salary.intValue() / 100;
        int cents = salary.intValue() % 100;
        return String.format("%d dollar(s) %02d cent(s)", dollars, cents);
    }
}
