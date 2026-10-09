package hu.chefexpense.gui;

import hu.chefexpense.model.Expense;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Table model over the repository's expense list; column headings follow the reference screenshots. */
class ExpenseTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Id", "ChefName", "Datum", "Kategoria", "Osszeg", "Megjegyzes"};
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    private final List<Expense> expenses;

    ExpenseTableModel(List<Expense> expenses) {
        this.expenses = expenses;
    }

    @Override
    public int getRowCount() {
        return expenses.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Expense e = expenses.get(row);
        return switch (column) {
            case 0 -> e.id();
            case 1 -> e.chefName();
            case 2 -> e.date().format(DATE);
            case 3 -> e.category();
            case 4 -> e.amount().toPlainString();
            default -> e.comment();
        };
    }

    /** Call after an expense was appended to the underlying list. */
    void rowAdded() {
        int last = expenses.size() - 1;
        fireTableRowsInserted(last, last);
    }
}
