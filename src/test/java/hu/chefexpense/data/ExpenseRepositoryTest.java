package hu.chefexpense.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExpenseRepositoryTest {
    private static final String SEED = ExpenseCsv.HEADER + "\n1;Anna Kovács;2025-01-05;Travel;120;\n"
            + "7;Éva Tóth;2025-02-10;Other;19.5;Árvíztűrő tükörfúrógép\n";

    @TempDir
    Path dir;
    Path file;
    ExpenseRepository repo;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("chef_koltsegek_2025.csv");
        Files.writeString(file, SEED, StandardCharsets.UTF_8);
        repo = new ExpenseRepository(file);
        repo.load();
    }

    @Test
    void loadSkipsHeaderAndReadsUtf8() {
        assertEquals(2, repo.getExpenses().size());
        assertEquals("Éva Tóth", repo.getExpenses().get(1).chefName());
        assertEquals("Árvíztűrő tükörfúrógép", repo.getExpenses().get(1).comment());
        assertEquals("", repo.getExpenses().get(0).comment());
    }

    @Test
    void addGeneratesMaxPlusOneAndRewritesFile() throws IOException {
        var e = repo.add(" Zoltán Szabó ", LocalDate.of(2025, 5, 3), "Equipment", "1000,50", "Két\nsor");
        assertEquals(8, e.id());
        assertEquals(3, repo.getExpenses().size());
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(ExpenseCsv.HEADER, lines.get(0));
        assertEquals(4, lines.size());
        assertEquals("8;Zoltán Szabó;2025-05-03;Equipment;1000.5;Két sor", lines.get(3));
        ExpenseRepository reloaded = new ExpenseRepository(file);
        reloaded.load();
        assertEquals(3, reloaded.getExpenses().size());
    }

    @Test
    void rejectsInvalidInputWithoutTouchingFile() throws IOException {
        LocalDate d = LocalDate.of(2025, 5, 3);
        assertThrows(IllegalArgumentException.class, () -> repo.add("", d, "Travel", "10", ""));
        assertThrows(IllegalArgumentException.class, () -> repo.add("A;B", d, "Travel", "10", ""));
        assertThrows(IllegalArgumentException.class, () -> repo.add("A", d, "Travel", "10", "x;y"));
        assertThrows(IllegalArgumentException.class, () -> repo.add("A", null, "Travel", "10", ""));
        assertThrows(IllegalArgumentException.class, () -> repo.add("A", LocalDate.of(2024, 5, 3), "Travel", "10", ""));
        assertThrows(IllegalArgumentException.class, () -> repo.add("A", d, null, "10", ""));
        assertThrows(IllegalArgumentException.class, () -> repo.add("A", d, "Food", "10", ""));
        for (String bad : new String[]{"", "abc", "-5", "0", "0,00", "1.234", "1e3"}) {
            assertThrows(IllegalArgumentException.class, () -> repo.add("A", d, "Travel", bad, ""), bad);
        }
        assertEquals(2, repo.getExpenses().size());
        assertEquals(SEED, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void failedSaveLeavesListUnchanged() throws IOException {
        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("blocker"), "x");
        IOException ex = assertThrows(IOException.class,
                () -> repo.add("A", LocalDate.of(2025, 1, 1), "Other", "5", ""));
        assertTrue(ex.getMessage().startsWith("A CSV fájl mentése nem sikerült"));
        assertEquals(2, repo.getExpenses().size());
    }

    @Test
    void locateFindsEmbeddedSourceCsv() throws IOException {
        Path found = ExpenseRepository.locate();
        assertTrue(found.endsWith(Path.of("src", "main", "resources", ExpenseRepository.FILE_NAME)), found.toString());
        ExpenseRepository embedded = new ExpenseRepository(found);
        embedded.load();
        assertEquals(100, embedded.getExpenses().size());
        assertTrue(embedded.getExpenses().stream().allMatch(e -> e.date().getYear() == ExpenseRepository.YEAR));
    }
}
