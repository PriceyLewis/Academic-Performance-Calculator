import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChartLabelTest {
    @Test
    void keepsShortLabelsUnchanged() {
        assertEquals("Databases", MainWindow.shortenChartLabel("Databases", 12));
    }

    @Test
    void trimsLongLabelsToPanelSafeLength() {
        assertEquals("Programming…", MainWindow.shortenChartLabel("Programming Fundamentals", 12));
        assertEquals(12, MainWindow.shortenChartLabel("Programming Fundamentals", 12).length());
    }

    @Test
    void handlesNullAndWhitespace() {
        assertEquals("", MainWindow.shortenChartLabel(null, 12));
        assertEquals("Networks", MainWindow.shortenChartLabel("  Networks  ", 12));
    }
}
