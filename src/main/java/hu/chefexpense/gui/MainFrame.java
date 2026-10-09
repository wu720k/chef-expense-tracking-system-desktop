package hu.chefexpense.gui;

import hu.chefexpense.data.ExpenseRepository;
import hu.chefexpense.model.Expense;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/** Main window: expense table on top, "new expense" form below (layout follows the reference images). */
public class MainFrame extends JFrame {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final int[] COLUMN_WIDTHS = {50, 130, 125, 125, 125, 250};

    private final ExpenseRepository repository;
    private final ExpenseTableModel tableModel;
    private final JTable table;
    private final JTextField chefField = new JTextField();
    private final JSpinner dateSpinner;
    private final JComboBox<String> categoryBox = new JComboBox<>(Expense.CATEGORIES.toArray(new String[0]));
    private final JTextField amountField = new JTextField();
    private final JTextArea commentArea = new JTextArea();

    public MainFrame(ExpenseRepository repository) {
        super("ChefExpenses");
        this.repository = repository;
        this.tableModel = new ExpenseTableModel(repository.getExpenses());
        this.table = new JTable(tableModel);
        this.dateSpinner = createDateSpinner();

        chefField.setName("chefField");
        dateSpinner.setName("dateSpinner");
        categoryBox.setName("categoryBox");
        amountField.setName("amountField");
        commentArea.setName("commentArea");
        table.setName("expenseTable");
        categoryBox.setSelectedIndex(-1);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        content.add(createTablePane(), BorderLayout.CENTER);
        content.add(createFormPanel(), BorderLayout.SOUTH);
        setContentPane(content);
        setSize(920, 800);
        setMinimumSize(new Dimension(720, 640));
        setLocationRelativeTo(null);
    }

    private JScrollPane createTablePane() {
        table.setRowHeight(32);
        table.setGridColor(java.awt.Color.GRAY);
        table.setShowGrid(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTHS[i]);
            column.setCellRenderer(left);
        }
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(800, 330));
        pane.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
        pane.getViewport().setBackground(java.awt.Color.GRAY);
        return pane;
    }

    private static JSpinner createDateSpinner() {
        LocalDate today = LocalDate.now();
        LocalDate initial = today.getYear() == ExpenseRepository.YEAR ? today
                : LocalDate.of(ExpenseRepository.YEAR, 12, 31);
        Date start = toDate(LocalDate.of(ExpenseRepository.YEAR, 1, 1));
        Date end = toDate(LocalDate.of(ExpenseRepository.YEAR, 12, 31));
        JSpinner spinner = new JSpinner(new SpinnerDateModel(toDate(initial), start, end,
                java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy. MMMM d., EEEE"));
        return spinner;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        Dimension fieldSize = new Dimension(255, 26);
        chefField.setPreferredSize(fieldSize);
        dateSpinner.setPreferredSize(fieldSize);
        categoryBox.setPreferredSize(fieldSize);
        amountField.setPreferredSize(fieldSize);

        addRow(form, 0, "Séf neve:", chefField);
        addRow(form, 1, "Dátum:", dateSpinner);
        addRow(form, 2, "Kategória:", categoryBox);
        addRow(form, 3, "Összeg (EUR):", amountField);

        JButton addButton = new JButton("Hozzáadás");
        addButton.setName("addButton");
        addButton.setPreferredSize(new Dimension(240, 72));
        addButton.addActionListener(e -> onAdd());
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 3;
        b.gridy = 2;
        b.gridheight = 2;
        b.anchor = GridBagConstraints.NORTHEAST;
        b.insets = new Insets(0, 10, 0, 0);
        form.add(addButton, b);

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 2;
        spacer.gridy = 0;
        spacer.weightx = 1;
        form.add(new JPanel(), spacer);

        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = 4;
        l.anchor = GridBagConstraints.NORTHWEST;
        l.insets = new Insets(8, 0, 0, 8);
        form.add(new JLabel("Megjegyzés:"), l);

        commentArea.setFont(chefField.getFont());
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentPane = new JScrollPane(commentArea);
        commentPane.setPreferredSize(new Dimension(500, 130));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 3;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(8, 0, 0, 0);
        form.add(commentPane, c);
        return form;
    }

    private static void addRow(JPanel form, int row, String label, java.awt.Component field) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = row;
        l.anchor = GridBagConstraints.WEST;
        l.insets = new Insets(10, 0, 10, 8);
        form.add(new JLabel(label), l);
        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 1;
        f.gridy = row;
        f.anchor = GridBagConstraints.WEST;
        f.insets = new Insets(10, 0, 10, 0);
        form.add(field, f);
    }

    private void onAdd() {
        LocalDate date = ((Date) dateSpinner.getValue()).toInstant().atZone(ZONE).toLocalDate();
        try {
            repository.add(chefField.getText(), date, (String) categoryBox.getSelectedItem(),
                    amountField.getText(), commentArea.getText());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Hibás adat", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.rowAdded();
        int last = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(last, last);
        table.scrollRectToVisible(table.getCellRect(last, 0, true));
        resetForm();
    }

    private void resetForm() {
        chefField.setText("");
        categoryBox.setSelectedIndex(-1);
        amountField.setText("");
        commentArea.setText("");
        chefField.requestFocusInWindow();
    }

    private static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZONE).toInstant());
    }
}
