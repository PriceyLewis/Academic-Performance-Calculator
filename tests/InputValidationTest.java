import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InputValidationTest {
    @Test
    void acceptsFiniteNumericValues() {
        assertEquals(72.5, MainWindow.parseDouble("72.5", "Grade"));
        assertEquals(-4.25, MainWindow.parseDouble(" -4.25 ", "Value"));
    }

    @Test
    void rejectsNaNAndInfinity() {
        assertThrows(IllegalArgumentException.class,
            () -> MainWindow.parseDouble("NaN", "Grade"));
        assertThrows(IllegalArgumentException.class,
            () -> MainWindow.parseDouble("Infinity", "Grade"));
        assertThrows(IllegalArgumentException.class,
            () -> MainWindow.parseDouble("-Infinity", "Grade"));
    }

    @Test
    void rejectsNonNumericText() {
        assertThrows(IllegalArgumentException.class,
            () -> MainWindow.parseDouble("not-a-number", "Grade"));
    }
}
