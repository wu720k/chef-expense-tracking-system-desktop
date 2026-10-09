package hu.chefexpense.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One chef expense (one row of chef_koltsegek_2025.csv). */
public record Expense(int id, String chefName, LocalDate date, String category, BigDecimal amount, String comment) {

    /** Supported expense categories (values exactly as in the assignment). */
    public static final List<String> CATEGORIES =
            List.of("Travel", "Ingredients", "Accommodation", "Equipment", "Other");
}
