import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Shared, deliberately lightweight styling for the Swing portfolio application. */
final class UiTheme {
    static final Color PRIMARY = new Color(37, 99, 235);
    static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    static final Color DANGER = new Color(220, 38, 38);
    static final Color SUCCESS = new Color(5, 150, 105);
    static final Color TEXT = new Color(15, 23, 42);
    static final Color MUTED = new Color(100, 116, 139);
    static final Color BACKGROUND = new Color(241, 245, 249);
    static final Color SURFACE = Color.WHITE;
    static final Color BORDER = new Color(203, 213, 225);
    static final Color DARK_BACKGROUND = new Color(15, 23, 42);
    static final Color DARK_SURFACE = new Color(30, 41, 59);
    static final Color DARK_BORDER = new Color(71, 85, 105);

    private UiTheme() {
    }

    static void installDefaults() {
        Font body = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("Label.font", body);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Menu.font", body);
        UIManager.put("MenuItem.font", body);
        UIManager.put("Table.font", body);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("ToolTip.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("OptionPane.messageFont", body);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
    }

    static void styleTable(JTable table, boolean dark) {
        Color surface = dark ? DARK_SURFACE : SURFACE;
        Color foreground = dark ? Color.WHITE : TEXT;
        table.setBackground(surface);
        table.setForeground(foreground);
        table.setSelectionBackground(dark ? new Color(30, 64, 175) : new Color(219, 234, 254));
        table.setSelectionForeground(foreground);
        table.setGridColor(dark ? DARK_BORDER : new Color(226, 232, 240));
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(dark ? new Color(51, 65, 85) : new Color(248, 250, 252));
        table.getTableHeader().setForeground(foreground);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
    }

    static void styleButton(JButton button, String variant) {
        button.putClientProperty("visualVariant", variant);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new java.awt.Insets(8, 14, 8, 14));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 38));
        paintButton(button, variant, false);
    }

    static Border cardBorder(int padding) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(padding, padding, padding, padding)
        );
    }

    static void applyTheme(Component component, boolean dark) {
        Color background = dark ? DARK_BACKGROUND : BACKGROUND;
        Color surface = dark ? DARK_SURFACE : SURFACE;
        Color foreground = dark ? Color.WHITE : TEXT;

        if (component instanceof JButton button) {
            Object value = button.getClientProperty("visualVariant");
            paintButton(button, value == null ? "secondary" : value.toString(), dark);
        } else if (component instanceof JTable table) {
            styleTable(table, dark);
        } else if (component instanceof JTextField || component instanceof JTextArea) {
            component.setBackground(surface);
            component.setForeground(foreground);
        } else if (component instanceof JPanel || component instanceof JScrollPane || component instanceof JMenuBar) {
            component.setBackground(background);
            component.setForeground(foreground);
        } else if (component instanceof JLabel || component instanceof JCheckBox) {
            component.setForeground(foreground);
            component.setBackground(background);
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyTheme(child, dark);
            }
        }
    }

    private static void paintButton(JButton button, String variant, boolean dark) {
        Color fill;
        Color foreground;
        Color border;
        switch (variant) {
            case "primary" -> {
                fill = PRIMARY;
                foreground = Color.WHITE;
                border = PRIMARY;
            }
            case "danger" -> {
                fill = dark ? new Color(127, 29, 29) : new Color(254, 242, 242);
                foreground = dark ? new Color(254, 202, 202) : DANGER;
                border = dark ? new Color(185, 28, 28) : new Color(254, 202, 202);
            }
            default -> {
                fill = dark ? DARK_SURFACE : SURFACE;
                foreground = dark ? Color.WHITE : TEXT;
                border = dark ? DARK_BORDER : BORDER;
            }
        }
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(fill);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border),
            BorderFactory.createEmptyBorder(7, 13, 7, 13)
        ));
    }
}
