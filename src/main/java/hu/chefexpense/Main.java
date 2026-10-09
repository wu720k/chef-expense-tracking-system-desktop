package hu.chefexpense;

import hu.chefexpense.data.ExpenseRepository;
import hu.chefexpense.gui.MainFrame;

import java.nio.file.Path;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("hu-HU"));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // the default look and feel is fine as a fallback
        }
        SwingUtilities.invokeLater(() -> {
            try {
                Path csv = args.length > 0 ? Path.of(args[0]) : ExpenseRepository.locate();
                ExpenseRepository repository = new ExpenseRepository(csv);
                repository.load();
                new MainFrame(repository).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Az alkalmazás nem indítható",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
