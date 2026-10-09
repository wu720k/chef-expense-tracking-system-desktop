package hu.chefexpense.data;

import hu.chefexpense.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Converts expenses to and from CSV lines (id;chefname;datum;kategoria;osszeg;megjegyzes).
 * Fields are never quoted: chef names and comments containing a semicolon are rejected
 * before they get here, so a line always has exactly 6 fields.
 */
final class ExpenseCsv {
    static final String HEADER = "id;chefname;datum;kategoria;osszeg;megjegyzes";
    static final char SEPARATOR = ';';
    /**
     * yyyy-MM-dd with SMART resolving: a day past the end of the month (the source data contains
     * 2025-02-29) becomes the last day of that month; anything else malformed is still rejected.
     */
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    private ExpenseCsv() { }

    static Expense parse(String line) {
        String[] f = line.split(String.valueOf(SEPARATOR), -1);
        if (f.length != 6) {
            throw new IllegalArgumentException("6 oszlop helyett " + f.length + " található");
        }
        return new Expense(Integer.parseInt(f[0].trim()), f[1].trim(), LocalDate.parse(f[2].trim(), DATE),
                f[3].trim(), new BigDecimal(f[4].trim()), f[5].trim());
    }

    static String format(Expense e) {
        return e.id() + ";" + e.chefName() + ";" + e.date() + ";" + e.category() + ";"
                + e.amount().toPlainString() + ";" + e.comment();
    }
}
