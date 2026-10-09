package hu.chefexpense.data;

import hu.chefexpense.model.Expense;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Holds the in-memory expense list and keeps it in sync with the shared CSV file (UTF-8, ';'). */
public class ExpenseRepository {
    public static final String FILE_NAME = "chef_koltsegek_2025.csv";
    public static final int YEAR = 2025;
    private static final Path RESOURCE_DIR = Path.of("src", "main", "resources");
    private static final String PROJECT_DIR = "Chef_Expense_Tracking_System_Desktop";
    private static final Pattern AMOUNT = Pattern.compile("\\d{1,9}([.,]\\d{1,2})?");

    private final Path file;
    private final List<Expense> expenses = new ArrayList<>();

    public ExpenseRepository(Path file) {
        this.file = file;
    }

    /**
     * Finds the embedded src/main/resources/chef_koltsegek_2025.csv of this project. The search walks up
     * from the compiled classes (target/classes or the jar) and from the working directory, so it works
     * from the project folder, the workspace root, IntelliJ, VS Code, Maven and java -jar alike.
     * The source file is used (not the copy in target/classes) so additions survive a rebuild.
     */
    public static Path locate() {
        List<Path> starts = new ArrayList<>();
        try {
            starts.add(Path.of(ExpenseRepository.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception ignored) {
            // no usable code source: fall back to the working directory only
        }
        starts.add(Path.of("").toAbsolutePath());
        for (Path start : starts) {
            for (Path dir = start; dir != null; dir = dir.getParent()) {
                for (Path candidate : List.of(dir.resolve(RESOURCE_DIR).resolve(FILE_NAME),
                        dir.resolve(PROJECT_DIR).resolve(RESOURCE_DIR).resolve(FILE_NAME))) {
                    if (Files.isRegularFile(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        throw new IllegalStateException("A " + RESOURCE_DIR + "/" + FILE_NAME
                + " fájl nem található. Indítsa a programot a projekt- vagy a workspace mappából,"
                + " vagy adja meg a CSV elérési útját az első argumentumként.");
    }

    public Path getFile() {
        return file;
    }

    /** Unmodifiable live view of the loaded expenses. */
    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    /** Reads the file as UTF-8; the first row is the header and is not data. */
    public void load() throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("A CSV fájl nem található: " + file.toAbsolutePath());
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.get(0).replace("﻿", "").trim().equals(ExpenseCsv.HEADER)) {
            throw new IOException("Váratlan CSV fejléc, a várt fejléc: " + ExpenseCsv.HEADER);
        }
        List<Expense> loaded = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                Expense e = ExpenseCsv.parse(line);
                if (!ids.add(e.id())) {
                    throw new IllegalArgumentException("ismétlődő azonosító: " + e.id());
                }
                loaded.add(e);
            } catch (RuntimeException ex) {
                throw new IOException("Érvénytelen CSV sor (" + (i + 1) + ". sor): " + line
                        + " (" + ex.getMessage() + ")", ex);
            }
        }
        expenses.clear();
        expenses.addAll(loaded);
    }

    /**
     * Validates the input, creates the expense with the next id (max id + 1), rewrites the CSV and
     * only then adds it to the in-memory list. If saving fails the list stays unchanged.
     *
     * @throws IllegalArgumentException (Hungarian message) when the input is invalid
     * @throws IOException              (Hungarian message) when the file cannot be written
     */
    public Expense add(String chefName, LocalDate date, String category, String amountText, String comment)
            throws IOException {
        String name = chefName == null ? "" : chefName.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("A séf nevének megadása kötelező.");
        }
        if (containsForbidden(name)) {
            throw new IllegalArgumentException("A séf neve nem tartalmazhat pontosvesszőt vagy sortörést.");
        }
        if (date == null) {
            throw new IllegalArgumentException("A dátum megadása kötelező.");
        }
        if (date.getYear() != YEAR) {
            throw new IllegalArgumentException("A dátumnak " + YEAR + "-ben kell lennie.");
        }
        if (category == null || !Expense.CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Válasszon kategóriát a listából.");
        }
        BigDecimal amount = parseAmount(amountText);
        // Line breaks typed into the multi-line comment box become spaces so a row stays on one line.
        String note = comment == null ? "" : comment.replaceAll("\\s*[\\r\\n]+\\s*", " ").trim();
        if (note.indexOf(ExpenseCsv.SEPARATOR) >= 0) {
            throw new IllegalArgumentException("A megjegyzés nem tartalmazhat pontosvesszőt.");
        }

        int nextId = expenses.stream().mapToInt(Expense::id).max().orElse(0) + 1;
        Expense created = new Expense(nextId, name, date, category, amount, note);

        List<Expense> updated = new ArrayList<>(expenses);
        updated.add(created);
        try {
            save(updated);
        } catch (IOException e) {
            throw new IOException("A CSV fájl mentése nem sikerült, a költség nem lett hozzáadva: "
                    + e.getMessage(), e);
        }
        expenses.add(created);
        return created;
    }

    private static boolean containsForbidden(String s) {
        return s.indexOf(ExpenseCsv.SEPARATOR) >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    }

    private static BigDecimal parseAmount(String text) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Az összeg megadása kötelező.");
        }
        if (!AMOUNT.matcher(t).matches()) {
            throw new IllegalArgumentException(
                    "Az összeg érvénytelen. Pozitív számot adjon meg, legfeljebb két tizedesjeggyel.");
        }
        BigDecimal amount = new BigDecimal(t.replace(',', '.')).stripTrailingZeros();
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Az összegnek nagyobbnak kell lennie nullánál.");
        }
        return amount.scale() < 0 ? amount.setScale(0) : amount;
    }

    /** Rewrites the whole file (header + rows) via a temp file so a failure cannot truncate the CSV. */
    private void save(List<Expense> rows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(ExpenseCsv.HEADER);
        rows.forEach(e -> lines.add(ExpenseCsv.format(e)));
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.writeString(tmp, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
