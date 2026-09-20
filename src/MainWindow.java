import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MainWindow {
    private static final String[] COLUMN_NAMES = {
        "Module Name", "Credits", "Grade", "Year", "Attendance", "Hours Studied", "Semester"
    };
    private static final int TIMEOUT = 5 * 60 * 1000;
    private static final Color COLOR_FIRST = new Color(0, 128, 0);
    private static final Color COLOR_UPPER_SECOND = new Color(0, 102, 204);
    private static final Color COLOR_LOWER_SECOND = new Color(255, 153, 0);
    private static final Color COLOR_FAIL = new Color(204, 51, 51);
    private static final String SAMPLE_DATA_FLAG = "sample-data-loaded.flag";
    private static final boolean BROWSER_DEMO = Boolean.getBoolean("portfolio.browser");

    private final JFrame frame;
    private final DefaultTableModel model;
    private final JTable table;
    private final JPanel graphPanel;
    private final JLabel finalGradeLabel;
    private final Vector<Vector<Object>> backupData = new Vector<>();
    private final AWTEventListener idleActivityListener;
    private Timer idleTimer;
    private boolean isDataModified = false;
    private boolean showingBarChart = true;
    private boolean isDarkMode = false;

    public MainWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        frame = new JFrame("Academic Performance Calculator");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.setIconImage(loadScaledIcon("App Icon.png", 64, 64).getImage());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptExit();
            }
        });

        model = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowSorter(new TableRowSorter<>(model));
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setGridColor(Color.LIGHT_GRAY);

        graphPanel = new JPanel(new BorderLayout());
        finalGradeLabel = new JLabel("Predicted Final Grade: N/A");
        idleActivityListener = event -> {
            if (idleTimer != null && idleTimer.isRunning()) {
                idleTimer.restart();
            }
        };

        buildLayout();
        loadInitialData();
        refreshDashboard();
        setupIdleLogout();
        frame.setVisible(true);
        BrowserBridge.signalReady("dashboard");
    }

    private void buildLayout() {
        frame.setJMenuBar(buildMenuBar());
        frame.add(buildSidebar(), BorderLayout.WEST);
        frame.add(buildCenterPanel(), BorderLayout.CENTER);
        graphPanel.setPreferredSize(new Dimension(360, 0));
        graphPanel.setBorder(BorderFactory.createTitledBorder("Visual Dashboard"));
        frame.add(graphPanel, BorderLayout.EAST);
        frame.add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem loadDatabaseItem = new JMenuItem("Load Database");
        JMenuItem saveDatabaseItem = new JMenuItem("Save Database");
        JMenuItem exportItem = new JMenuItem("Export CSV");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadDatabaseItem.addActionListener(e -> loadModulesFromDatabaseWithFeedback());
        saveDatabaseItem.addActionListener(e -> overwriteDatabase());
        exportItem.addActionListener(e -> exportModulesToCSV());
        exitItem.addActionListener(e -> attemptExit());

        if (BROWSER_DEMO) {
            loadDatabaseItem.setEnabled(false);
            saveDatabaseItem.setEnabled(false);
            loadDatabaseItem.setToolTipText("Microsoft Access persistence is available in the desktop build.");
            saveDatabaseItem.setToolTipText("Microsoft Access persistence is available in the desktop build.");
        }

        fileMenu.add(loadDatabaseItem);
        fileMenu.add(saveDatabaseItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem howToUseItem = new JMenuItem("How to Use");
        JMenuItem aboutItem = new JMenuItem("About");
        howToUseItem.addActionListener(e -> showHowToUse());
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(howToUseItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(245, 247, 250));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel logoLabel = new JLabel(loadScaledIcon("App Icon.png", 64, 64));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logoLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton btnHome = createSidebarButton("Home", "Home.png");
        JButton btnFeedback = createSidebarButton("Feedback", "Feedback.png");
        JButton btnModules = createSidebarButton("View Modules", "Module.png");
        JButton btnGraph = createSidebarButton("Full Graph", "Graph.png");
        JButton btnPieChart = createSidebarButton("Pie Chart", "Piechart.png");
        JButton btnSettings = createSidebarButton("Settings", "Settings.gif");
        JButton btnHelp = createSidebarButton("Help", "Help.gif");
        JButton btnLogout = createSidebarButton("Logout", "Logout.gif");

        btnHome.addActionListener(e -> refreshHome());
        btnFeedback.addActionListener(e -> collectFeedback());
        btnModules.addActionListener(e -> showModulesDialog());
        btnGraph.addActionListener(e -> showFullGraph());
        btnPieChart.addActionListener(e -> showGradePieChart());
        btnSettings.addActionListener(e -> showSettingsDialog());
        btnHelp.addActionListener(e -> showHelpDialog());
        btnLogout.addActionListener(e -> logout());

        addSidebarButton(sidebar, btnHome);
        addSidebarButton(sidebar, btnFeedback);
        addSidebarButton(sidebar, btnModules);
        addSidebarButton(sidebar, btnGraph);
        addSidebarButton(sidebar, btnPieChart);
        addSidebarButton(sidebar, btnSettings);
        addSidebarButton(sidebar, btnHelp);
        addSidebarButton(sidebar, btnLogout);
        return sidebar;
    }

    private JPanel buildCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Your Modules");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JTextArea intro = new JTextArea(
            BROWSER_DEMO
                ? "Browser demo: sample data is loaded automatically. Add, remove, chart and predict directly in the browser; Access persistence stays in the desktop build."
                : "Use the sample data for an immediate demo, or load records from the Access database or a CSV file."
        );
        intro.setWrapStyleWord(true);
        intro.setLineWrap(true);
        intro.setEditable(false);
        intro.setOpaque(false);
        intro.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(intro, BorderLayout.CENTER);

        centerPanel.add(heading, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton addButton = new JButton("Add Module");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Table");
        JButton undoButton = new JButton("Undo Clear");
        JButton loadDatabaseButton = new JButton("Load Database");
        JButton saveDatabaseButton = new JButton("Save Database");
        JButton exportButton = new JButton("Export CSV");
        JButton importButton = new JButton("Load CSV");
        JButton toggleGraphButton = new JButton("Toggle Graph");
        JButton openGraphButton = new JButton("Open Full Graph");
        JButton pieChartButton = new JButton("Pie Chart");
        JButton predictButton = new JButton("Predict Outcome");

        addButton.addActionListener(e -> addModule());
        deleteButton.addActionListener(e -> deleteSelectedModule());
        clearButton.addActionListener(e -> clearTable());
        undoButton.addActionListener(e -> undoClear());
        loadDatabaseButton.addActionListener(e -> loadModulesFromDatabaseWithFeedback());
        saveDatabaseButton.addActionListener(e -> overwriteDatabase());
        exportButton.addActionListener(e -> exportModulesToCSV());
        importButton.addActionListener(e -> loadModulesFromCSV());
        toggleGraphButton.addActionListener(e -> toggleGraph());

        if (BROWSER_DEMO) {
            loadDatabaseButton.setEnabled(false);
            saveDatabaseButton.setEnabled(false);
            importButton.setEnabled(false);
            loadDatabaseButton.setToolTipText("Desktop-only: loads the bundled Microsoft Access database.");
            saveDatabaseButton.setToolTipText("Desktop-only: writes to the bundled Microsoft Access database.");
            importButton.setToolTipText("Desktop-only in this portfolio browser build.");
            exportButton.setToolTipText("Downloads the current module table as CSV.");
        }
        openGraphButton.addActionListener(e -> showFullGraph());
        pieChartButton.addActionListener(e -> showGradePieChart());
        predictButton.addActionListener(e -> showPredictionDialog());

        actions.add(addButton);
        actions.add(deleteButton);
        actions.add(clearButton);
        actions.add(undoButton);
        actions.add(loadDatabaseButton);
        actions.add(saveDatabaseButton);
        actions.add(exportButton);
        actions.add(importButton);
        actions.add(toggleGraphButton);
        actions.add(openGraphButton);
        actions.add(pieChartButton);
        actions.add(predictButton);

        finalGradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(actions, BorderLayout.CENTER);
        bottomPanel.add(finalGradeLabel, BorderLayout.SOUTH);
        return bottomPanel;
    }

    private void loadInitialData() {
        if (BROWSER_DEMO) {
            loadSampleModules();
            return;
        }

        if (hasDatabaseContent()) {
            try {
                loadModulesFromDatabase(false);
                return;
            } catch (SQLException ignored) {
            }
        }

        loadSampleModules();
        if (!Files.exists(DBConnector.resolveProjectPath(SAMPLE_DATA_FLAG))) {
            JOptionPane.showMessageDialog(frame, "Sample data has been loaded so the dashboard is ready to demonstrate immediately.", "Demo Data Ready", JOptionPane.INFORMATION_MESSAGE);
            try {
                Files.writeString(DBConnector.resolveProjectPath(SAMPLE_DATA_FLAG), "shown", StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
        }
    }

    private void refreshDashboard() {
        updateFinalGrade();
        if (showingBarChart) {
            updateGraph();
        } else {
            updatePieGraph();
        }
    }

    private void refreshHome() {
        refreshDashboard();
        JOptionPane.showMessageDialog(frame, "Dashboard refreshed.", "Home", JOptionPane.INFORMATION_MESSAGE);
    }

    private void toggleGraph() {
        showingBarChart = !showingBarChart;
        refreshDashboard();
    }

    private void updateGraph() {
        graphPanel.removeAll();
        graphPanel.add(new BarChartView(false), BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }

    private void updatePieGraph() {
        graphPanel.removeAll();
        graphPanel.add(new PieChartView(), BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }

    private void showFullGraph() {
        JFrame graphWindow = new JFrame("Full Graph View");
        graphWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphWindow.setSize(900, 600);
        graphWindow.setLocationRelativeTo(frame);

        graphWindow.add(new BarChartView(true));
        graphWindow.setVisible(true);
    }

    private void showGradePieChart() {
        JFrame pieFrame = new JFrame("Grade Distribution");
        pieFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pieFrame.setSize(680, 420);
        pieFrame.setLocationRelativeTo(frame);
        pieFrame.add(new PieChartView());
        pieFrame.setVisible(true);
    }

    private void addModule() {
        JTextField nameField = new JTextField();
        JTextField creditsField = new JTextField();
        JTextField gradeField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField attendanceField = new JTextField();
        JTextField hoursStudiedField = new JTextField();
        JTextField semesterField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel("Module Name"));
        panel.add(nameField);
        panel.add(new JLabel("Credits"));
        panel.add(creditsField);
        panel.add(new JLabel("Grade (0-100)"));
        panel.add(gradeField);
        panel.add(new JLabel("Year"));
        panel.add(yearField);
        panel.add(new JLabel("Attendance (0-100)"));
        panel.add(attendanceField);
        panel.add(new JLabel("Hours Studied"));
        panel.add(hoursStudiedField);
        panel.add(new JLabel("Semester"));
        panel.add(semesterField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Module", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            int credits = parseInteger(creditsField.getText(), "Credits");
            double grade = parseDouble(gradeField.getText(), "Grade");
            int year = parseInteger(yearField.getText(), "Year");
            int attendance = parseInteger(attendanceField.getText(), "Attendance");
            int hoursStudied = parseInteger(hoursStudiedField.getText(), "Hours Studied");
            int semester = parseInteger(semesterField.getText(), "Semester");

            validateModuleInput(name, credits, grade, year, attendance, hoursStudied, semester);
            model.addRow(new Object[] { name, credits, grade, year, attendance, hoursStudied, semester });
            isDataModified = true;
            refreshDashboard();
            JOptionPane.showMessageDialog(frame, "Module added successfully.", "Module Added", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedModule() {
        int selectedViewRow = table.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a module to delete.", "Delete Module", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int selectedModelRow = table.convertRowIndexToModel(selectedViewRow);
        String moduleName = String.valueOf(model.getValueAt(selectedModelRow, 0));
        if (JOptionPane.showConfirmDialog(frame, "Delete \"" + moduleName + "\" from the table?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        model.removeRow(selectedModelRow);
        isDataModified = true;
        refreshDashboard();
        JOptionPane.showMessageDialog(frame, "Module removed from the table.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearTable() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "The table is already empty.", "Clear Table", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        backupData.clear();
        for (int row = 0; row < model.getRowCount(); row++) {
            Vector<Object> backupRow = new Vector<>();
            for (int col = 0; col < model.getColumnCount(); col++) {
                backupRow.add(model.getValueAt(row, col));
            }
            backupData.add(backupRow);
        }

        model.setRowCount(0);
        isDataModified = true;
        refreshDashboard();
        JOptionPane.showMessageDialog(frame, "Table cleared. Use Undo Clear to restore the previous state.", "Table Cleared", JOptionPane.INFORMATION_MESSAGE);
    }

    private void undoClear() {
        if (backupData.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Nothing is available to restore.", "Undo Clear", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        model.setRowCount(0);
        for (Vector<Object> row : backupData) {
            model.addRow(row);
        }
        backupData.clear();
        isDataModified = true;
        refreshDashboard();
        JOptionPane.showMessageDialog(frame, "Previous rows restored.", "Undo Clear", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadModulesFromDatabaseWithFeedback() {
        try {
            loadModulesFromDatabase(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Unable to load modules from the database.\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadModulesFromDatabase(boolean showSuccessMessage) throws SQLException {
        ModuleDAO dao = new ModuleDAO();
        List<Module> modules = dao.getAllModules();

        model.setRowCount(0);
        for (Module module : modules) {
            model.addRow(new Object[] {
                module.getName(),
                module.getCredits(),
                module.getGrade(),
                module.getYearUndertakingModule(),
                module.getAttendance(),
                module.getHoursStudied(),
                module.getSemester()
            });
        }

        isDataModified = false;
        refreshDashboard();

        if (showSuccessMessage) {
            String message = modules.isEmpty() ? "The database is available but does not contain any modules yet." : "Loaded " + modules.size() + " module(s) from the database.";
            JOptionPane.showMessageDialog(frame, message, "Database Load", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void overwriteDatabase() {
        try {
            ModuleDAO dao = new ModuleDAO();
            dao.clearModules();
            for (int row = 0; row < model.getRowCount(); row++) {
                Module module = new Module(
                    String.valueOf(model.getValueAt(row, 0)),
                    Integer.parseInt(String.valueOf(model.getValueAt(row, 1))),
                    Double.parseDouble(String.valueOf(model.getValueAt(row, 2))),
                    Integer.parseInt(String.valueOf(model.getValueAt(row, 3))),
                    Integer.parseInt(String.valueOf(model.getValueAt(row, 6))),
                    Integer.parseInt(String.valueOf(model.getValueAt(row, 4))),
                    Integer.parseInt(String.valueOf(model.getValueAt(row, 5)))
                );
                dao.addModule(module);
            }
            isDataModified = false;
            JOptionPane.showMessageDialog(frame, "Database updated successfully.", "Database Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Unable to save modules to the database.\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportModulesToCSV() {
        File fileToSave;
        if (BROWSER_DEMO) {
            fileToSave = new File("/files/downloads/academic-performance-modules.csv");
        } else {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Modules to CSV");
            if (fileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }
        }

        try (PrintWriter writer = new PrintWriter(fileToSave, StandardCharsets.UTF_8)) {
            writer.println(String.join(",", COLUMN_NAMES));
            for (int row = 0; row < model.getRowCount(); row++) {
                List<String> values = new ArrayList<>();
                for (int col = 0; col < model.getColumnCount(); col++) {
                    values.add(escapeCsv(String.valueOf(model.getValueAt(row, col))));
                }
                writer.println(String.join(",", values));
            }
            String exportMessage = BROWSER_DEMO
                ? "CSV created. Your browser should download academic-performance-modules.csv automatically."
                : "CSV exported to:\n" + fileToSave.getAbsolutePath();
            JOptionPane.showMessageDialog(frame, exportMessage, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to export CSV.\n" + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadModulesFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Modules from CSV");
        if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        List<Object[]> importedRows = new ArrayList<>();
        try (Scanner scanner = new Scanner(fileChooser.getSelectedFile(), StandardCharsets.UTF_8)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                List<String> data = parseCsvLine(line);
                if (data.size() != COLUMN_NAMES.length) {
                    throw new IllegalArgumentException("CSV row does not match the expected format.");
                }

                String name = data.get(0).trim();
                int credits = parseInteger(data.get(1), "Credits");
                double grade = parseDouble(data.get(2), "Grade");
                int year = parseInteger(data.get(3), "Year");
                int attendance = parseInteger(data.get(4), "Attendance");
                int hoursStudied = parseInteger(data.get(5), "Hours Studied");
                int semester = parseInteger(data.get(6), "Semester");
                validateModuleInput(name, credits, grade, year, attendance, hoursStudied, semester);

                importedRows.add(new Object[] { name, credits, grade, year, attendance, hoursStudied, semester });
            }

            model.setRowCount(0);
            for (Object[] row : importedRows) {
                model.addRow(row);
            }
            isDataModified = true;
            refreshDashboard();
            JOptionPane.showMessageDialog(frame, "Loaded " + importedRows.size() + " module(s) from CSV.", "CSV Import", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Failed to load CSV.\n" + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFinalGrade() {
        double totalPoints = 0;
        double totalCredits = 0;

        for (int row = 0; row < model.getRowCount(); row++) {
            try {
                double grade = Double.parseDouble(String.valueOf(model.getValueAt(row, 2)));
                double credits = Double.parseDouble(String.valueOf(model.getValueAt(row, 1)));
                totalPoints += grade * credits;
                totalCredits += credits;
            } catch (NumberFormatException ignored) {
            }
        }

        if (totalCredits <= 0) {
            finalGradeLabel.setText("Predicted Final Grade: N/A");
            finalGradeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
            return;
        }

        double predictedGrade = totalPoints / totalCredits;
        finalGradeLabel.setText(String.format("Predicted Final Grade: %.2f", predictedGrade));
        finalGradeLabel.setForeground(classificationColor(predictedGrade));
    }

    private void showPredictionDialog() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Add or load some modules before running a prediction.", "Prediction", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField attendanceField = new JTextField("82");
        JTextField hoursField = new JTextField("120");
        JTextField gradeField = new JTextField("66");
        JTextField creditsField = new JTextField("15");

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.add(new JLabel("Attendance (%)"));
        panel.add(attendanceField);
        panel.add(new JLabel("Hours Studied"));
        panel.add(hoursField);
        panel.add(new JLabel("Current Grade (%)"));
        panel.add(gradeField);
        panel.add(new JLabel("Module Credits"));
        panel.add(creditsField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Predict Final Classification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double attendance = parseDouble(attendanceField.getText(), "Attendance");
            double hoursStudied = parseDouble(hoursField.getText(), "Hours Studied");
            double grade = parseDouble(gradeField.getText(), "Current Grade");
            double credits = parseDouble(creditsField.getText(), "Module Credits");
            if (attendance < 0 || attendance > 100 || grade < 0 || grade > 100 || hoursStudied < 0 || credits <= 0) {
                throw new IllegalArgumentException("Enter realistic positive values. Attendance and grade must be between 0 and 100.");
            }

            RandomForestModel predictor = new RandomForestModel();
            predictor.train(buildTrainingData());
            RandomForestModel.Prediction predictionResult = predictor.predictWithConfidence(
                attendance, hoursStudied, grade, credits
            );

            String finalPrediction = predictionResult.classification();
            int confidence = predictionResult.confidencePercent();
            JTextArea textArea = new JTextArea(buildPredictionReport(finalPrediction, confidence, attendance, hoursStudied, grade, credits));
            textArea.setEditable(false);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(520, 380));
            JOptionPane.showMessageDialog(frame, scrollPane, "Performance Report", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Prediction Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildPredictionReport(String prediction, int confidence, double attendance, double hoursStudied, double grade, double credits) {
        StringBuilder report = new StringBuilder();
        report.append("ACADEMIC PERFORMANCE SCORECARD\n");
        report.append("-----------------------------------\n\n");
        report.append("Predicted Final Classification: ").append(prediction).append("\n");
        report.append("Model Vote Share: ").append(confidence).append("%\n\n");
        report.append("Input Summary\n");
        report.append("- Attendance: ").append(String.format("%.1f", attendance)).append("%\n");
        report.append("- Hours Studied: ").append(String.format("%.1f", hoursStudied)).append("\n");
        report.append("- Current Grade: ").append(String.format("%.1f", grade)).append("%\n");
        report.append("- Module Credits: ").append(String.format("%.1f", credits)).append("\n\n");
        report.append("Advice\n");

        if (attendance < 60) {
            report.append("- Attendance is critically low. Improving consistency will likely help the most.\n");
        } else if (attendance < 75) {
            report.append("- Attendance is acceptable but still leaves room for improvement.\n");
        } else {
            report.append("- Attendance is strong. Maintain that baseline.\n");
        }

        if (hoursStudied < 50) {
            report.append("- Study time is low for a strong finish. Increase weekly revision time.\n");
        } else if (hoursStudied < 100) {
            report.append("- Study volume is reasonable, but more structured practice could push the result higher.\n");
        } else {
            report.append("- Study effort is high. Focus on quality and exam technique now.\n");
        }

        if (grade < 50) {
            report.append("- Current grade is below pass level. Prioritise intervention immediately.\n");
        } else if (grade < 60) {
            report.append("- You are within reach of a higher classification with steady gains.\n");
        } else if (grade < 70) {
            report.append("- Current performance is solid. A modest increase could move this into first-class territory.\n");
        } else {
            report.append("- Current performance is already excellent. Keep the pace and avoid regression.\n");
        }

        return report.toString();
    }

    private List<double[]> buildTrainingData() {
        List<double[]> trainingData = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            double attendance = Double.parseDouble(String.valueOf(model.getValueAt(row, 4)));
            double hoursStudied = Double.parseDouble(String.valueOf(model.getValueAt(row, 5)));
            double grade = Double.parseDouble(String.valueOf(model.getValueAt(row, 2)));
            double credits = Double.parseDouble(String.valueOf(model.getValueAt(row, 1)));
            double classification = grade >= 70 ? 3 : grade >= 60 ? 2 : grade >= 50 ? 1 : 0;
            trainingData.add(new double[] { attendance, hoursStudied, grade, credits, classification });
        }
        return trainingData;
    }

    private List<ModuleGrade> buildModuleGrades() {
        List<ModuleGrade> grades = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            try {
                grades.add(new ModuleGrade(
                    String.valueOf(model.getValueAt(row, 0)),
                    Double.parseDouble(String.valueOf(model.getValueAt(row, 2)))
                ));
            } catch (NumberFormatException ignored) {
            }
        }
        return grades;
    }

    private GradeBuckets buildGradeBuckets() {
        int first = 0;
        int upperSecond = 0;
        int lowerSecond = 0;
        int fail = 0;
        for (ModuleGrade moduleGrade : buildModuleGrades()) {
            double grade = moduleGrade.grade();
            if (grade >= 70) {
                first++;
            } else if (grade >= 60) {
                upperSecond++;
            } else if (grade >= 50) {
                lowerSecond++;
            } else {
                fail++;
            }
        }

        return new GradeBuckets(first, upperSecond, lowerSecond, fail);
    }

    private void showModulesDialog() {
        JDialog dialog = new JDialog(frame, "All Modules", true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.add(new JScrollPane(new JTable(model)));
        dialog.setVisible(true);
    }

    private void showSettingsDialog() {
        JDialog dialog = new JDialog(frame, "Settings", true);
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridLayout(0, 1, 10, 10));

        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton toggleThemeButton = new JButton(isDarkMode ? "Switch to Light Mode" : "Switch to Dark Mode");
        JCheckBox idleLogoutCheckbox = new JCheckBox("Enable idle timeout", idleTimer != null && idleTimer.isRunning());
        JButton closeButton = new JButton("Close");

        toggleThemeButton.addActionListener(e -> {
            toggleDarkMode();
            toggleThemeButton.setText(isDarkMode ? "Switch to Light Mode" : "Switch to Dark Mode");
        });
        idleLogoutCheckbox.addActionListener(e -> {
            if (idleLogoutCheckbox.isSelected()) {
                setupIdleLogout();
            } else if (idleTimer != null) {
                idleTimer.stop();
            }
        });
        closeButton.addActionListener(e -> dialog.dispose());

        dialog.add(title);
        dialog.add(toggleThemeButton);
        dialog.add(idleLogoutCheckbox);
        dialog.add(closeButton);
        dialog.setVisible(true);
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        Color backgroundColor = isDarkMode ? new Color(45, 45, 45) : Color.WHITE;
        Color foregroundColor = isDarkMode ? Color.WHITE : Color.BLACK;
        applyTheme(frame.getContentPane(), backgroundColor, foregroundColor);
        table.setBackground(backgroundColor);
        table.setForeground(foregroundColor);
        table.getTableHeader().setBackground(isDarkMode ? new Color(65, 65, 65) : new Color(240, 240, 240));
        table.getTableHeader().setForeground(foregroundColor);
        table.setGridColor(isDarkMode ? new Color(85, 85, 85) : Color.LIGHT_GRAY);
        refreshDashboard();
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void applyTheme(Component component, Color background, Color foreground) {
        if (!(component instanceof ChartViewPanel)) {
            component.setBackground(background);
            component.setForeground(foreground);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyTheme(child, background, foreground);
            }
        }
    }

    private void setupIdleLogout() {
        if (BROWSER_DEMO) {
            return;
        }

        if (idleTimer != null) {
            idleTimer.stop();
        }

        Toolkit.getDefaultToolkit().removeAWTEventListener(idleActivityListener);

        idleTimer = new Timer(TIMEOUT, e -> {
            int choice = JOptionPane.showConfirmDialog(frame, "You have been inactive for a while. Stay signed in?", "Idle Timeout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                idleTimer.restart();
            } else {
                frame.dispose();
                new LoginWindow();
            }
        });
        idleTimer.setRepeats(false);
        idleTimer.start();

        Toolkit.getDefaultToolkit().addAWTEventListener(idleActivityListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void collectFeedback() {
        String feedback = JOptionPane.showInputDialog(frame, "Leave a short note about the demo experience or sample data.");
        if (feedback == null || feedback.trim().isEmpty()) {
            return;
        }

        if (BROWSER_DEMO) {
            JOptionPane.showMessageDialog(
                frame,
                "Thanks for the feedback. Browser-demo feedback is intentionally not stored.",
                "Feedback",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Path feedbackPath = DBConnector.resolveProjectPath("UserFeedback.txt");
        try {
            Files.writeString(
                feedbackPath,
                "Feedback: " + feedback.trim() + System.lineSeparator()
                    + "Submitted at: " + new java.util.Date() + System.lineSeparator()
                    + "-----------------------------" + System.lineSeparator(),
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
            );
            JOptionPane.showMessageDialog(frame, "Feedback saved to " + feedbackPath.getFileName() + ".", "Feedback", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Unable to save feedback.\n" + ex.getMessage(), "Feedback Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHelpDialog() {
        JOptionPane.showMessageDialog(
            frame,
            "Home refreshes the dashboard.\n"
                + "View Modules opens the full module table.\n"
                + "Full Graph and Pie Chart expand the visualisations.\n"
                + (BROWSER_DEMO
                    ? "The browser demo uses disposable sample data; Access load/save remains available in the desktop build.\n"
                    : "Save Database writes the current table to the Access database.\nLoad Database restores saved records.\n")
                + "Export CSV downloads the current table.\n"
                + "Predict Outcome generates a concise performance summary from the current inputs.",
            "Feature Guide",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showHowToUse() {
        JOptionPane.showMessageDialog(
            frame,
            "1. Sign in with the demo credentials.\n"
                + "2. Review the sample modules or add your own module.\n"
                + "3. Demonstrate the weighted final grade, bar chart, and pie chart views.\n"
                + (BROWSER_DEMO
                    ? "4. Export the current table to CSV; the browser demo keeps its data disposable.\n"
                    : "4. Save the current state to the Access database or export it to CSV.\n")
                + "5. Use Predict Outcome to generate a short forecasting summary.",
            "Demo Walkthrough",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            frame,
            "Academic Performance Calculator\nVersion 2.1 Portfolio Build\n"
                + (BROWSER_DEMO ? "Running in browser-demo mode via CheerpJ.\n" : "")
                + "Java Swing portfolio application for module tracking, grade visualisation, and academic performance forecasting.",
            "About",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void logout() {
        if (!confirmDiscardChanges("Return to the login screen?")) {
            return;
        }
        frame.dispose();
        new LoginWindow();
    }

    private void attemptExit() {
        if (!confirmDiscardChanges(BROWSER_DEMO ? "Close the browser demo window?" : "Exit the application?")) {
            return;
        }
        if (BROWSER_DEMO) {
            frame.dispose();
            return;
        }
        System.exit(0);
    }

    private boolean confirmDiscardChanges(String actionText) {
        String message = isDataModified ? "You have unsaved changes.\n" + actionText : actionText;
        return JOptionPane.showConfirmDialog(frame, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private JButton createSidebarButton(String text, String iconName) {
        JButton button = new JButton(text, loadScaledIcon(iconName, 24, 24));
        button.setMaximumSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        addHoverEffect(button, button.getBackground(), new Color(220, 232, 246));
        return button;
    }

    private void addSidebarButton(JPanel sidebar, JButton button) {
        sidebar.add(button);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void addHoverEffect(JButton button, Color normal, Color hover) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }

    private ImageIcon loadScaledIcon(String iconFileName, int width, int height) {
        URL resource = MainWindow.class.getResource("/Icons/" + iconFileName);
        ImageIcon original;
        if (resource != null) {
            original = new ImageIcon(resource);
        } else {
            Path iconPath = DBConnector.resolveProjectPath("src", "Icons", iconFileName);
            if (!Files.exists(iconPath)) {
                return new ImageIcon();
            }
            original = new ImageIcon(iconPath.toString());
        }

        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private Color classificationColor(double grade) {
        if (grade >= 70) {
            return COLOR_FIRST;
        }
        if (grade >= 60) {
            return COLOR_UPPER_SECOND;
        }
        if (grade >= 50) {
            return COLOR_LOWER_SECOND;
        }
        return COLOR_FAIL;
    }

    private void loadSampleModules() {
        model.setRowCount(0);
        model.addRow(new Object[] { "Programming Fundamentals", 15, 75.0, 1, 90, 150, 1 });
        model.addRow(new Object[] { "Database Systems", 15, 65.0, 1, 85, 140, 2 });
        model.addRow(new Object[] { "Computer Networks", 15, 55.0, 2, 80, 120, 1 });
        model.addRow(new Object[] { "Software Engineering", 30, 72.0, 2, 88, 200, 2 });
        model.addRow(new Object[] { "Web Development", 15, 48.0, 3, 75, 110, 1 });
        isDataModified = false;
    }

    private boolean hasDatabaseContent() {
        try {
            return !new ModuleDAO().getAllModules().isEmpty();
        } catch (Exception ex) {
            return false;
        }
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(fieldName + " must be numeric.");
        }
    }

    private void validateModuleInput(String name, int credits, double grade, int year, int attendance, int hoursStudied, int semester) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Module name cannot be empty.");
        }
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be positive.");
        }
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100.");
        }
        if (attendance < 0 || attendance > 100) {
            throw new IllegalArgumentException("Attendance must be between 0 and 100.");
        }
        if (hoursStudied < 0) {
            throw new IllegalArgumentException("Hours studied cannot be negative.");
        }
        if (year < 1 || year > 10) {
            throw new IllegalArgumentException("Year must be between 1 and 10.");
        }
        if (semester < 1 || semester > 3) {
            throw new IllegalArgumentException("Semester must be between 1 and 3.");
        }
    }

    private abstract class ChartViewPanel extends JPanel {
        ChartViewPanel() {
            setOpaque(true);
            setBackground(isDarkMode ? new Color(55, 55, 55) : Color.WHITE);
        }

        void prepare(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private final class BarChartView extends ChartViewPanel {
        private final boolean fullSize;

        private BarChartView(boolean fullSize) {
            this.fullSize = fullSize;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            prepare(g2);

            List<ModuleGrade> grades = buildModuleGrades();
            if (grades.isEmpty()) {
                drawEmptyState(g2, "No grade data to display.");
                g2.dispose();
                return;
            }

            int width = getWidth();
            int height = getHeight();
            int left = fullSize ? 70 : 50;
            int right = 20;
            int top = 30;
            int bottom = fullSize ? 80 : 60;
            int chartWidth = Math.max(1, width - left - right);
            int chartHeight = Math.max(1, height - top - bottom);

            g2.setColor(isDarkMode ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            g2.drawLine(left, top, left, top + chartHeight);
            g2.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);
            g2.setFont(new Font("Segoe UI", Font.BOLD, fullSize ? 15 : 13));
            g2.drawString(fullSize ? "Module Grades" : "Grades", left, 20);

            int count = grades.size();
            int slotWidth = Math.max(1, chartWidth / count);
            int barWidth = Math.max(16, (int) (slotWidth * 0.6));

            for (int i = 0; i < grades.size(); i++) {
                ModuleGrade grade = grades.get(i);
                int barHeight = (int) Math.round((Math.max(0, Math.min(100, grade.grade())) / 100.0) * chartHeight);
                int x = left + i * slotWidth + Math.max(0, (slotWidth - barWidth) / 2);
                int y = top + chartHeight - barHeight;

                g2.setColor(classificationColor(grade.grade()));
                g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

                g2.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, fullSize ? 12 : 11));
                g2.drawString(String.format("%.0f", grade.grade()), x + 2, y - 6);
                drawRotatedLabel(g2, grade.name(), x + (barWidth / 2), top + chartHeight + 8);
            }

            g2.dispose();
        }
    }

    private final class PieChartView extends ChartViewPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            prepare(g2);

            GradeBuckets buckets = buildGradeBuckets();
            int total = buckets.total();
            if (total == 0) {
                drawEmptyState(g2, "No grade distribution to display.");
                g2.dispose();
                return;
            }

            int diameter = Math.min(getWidth() - 180, getHeight() - 60);
            diameter = Math.max(160, diameter);
            int x = 30;
            int y = Math.max(20, (getHeight() - diameter) / 2);

            Object[][] slices = {
                { "First (70%+)", buckets.first(), COLOR_FIRST },
                { "2:1 (60-69%)", buckets.upperSecond(), COLOR_UPPER_SECOND },
                { "2:2 (50-59%)", buckets.lowerSecond(), COLOR_LOWER_SECOND },
                { "Fail (<50%)", buckets.fail(), COLOR_FAIL }
            };

            double startAngle = 0;
            for (Object[] slice : slices) {
                int value = (Integer) slice[1];
                if (value == 0) {
                    continue;
                }

                double arc = (value * 360.0) / total;
                g2.setColor((Color) slice[2]);
                g2.fillArc(x, y, diameter, diameter, (int) Math.round(startAngle), (int) Math.ceil(arc));
                startAngle += arc;
            }

            g2.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("Grade Distribution", 30, 20);

            int legendX = x + diameter + 25;
            int legendY = y + 20;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            for (Object[] slice : slices) {
                g2.setColor((Color) slice[2]);
                g2.fillRect(legendX, legendY, 14, 14);
                g2.setColor(isDarkMode ? Color.WHITE : Color.BLACK);
                g2.drawString(slice[0] + ": " + slice[1], legendX + 24, legendY + 12);
                legendY += 26;
            }

            g2.dispose();
        }
    }

    private void drawEmptyState(Graphics2D g2, String text) {
        g2.setColor(isDarkMode ? Color.WHITE : Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.drawString(text, 20, 30);
    }

    private void drawRotatedLabel(Graphics2D g2, String text, int centerX, int baselineY) {
        Graphics2D copy = (Graphics2D) g2.create();
        copy.translate(centerX, baselineY);
        copy.rotate(-Math.PI / 4);
        copy.drawString(text, 0, 0);
        copy.dispose();
    }

    private record ModuleGrade(String name, double grade) {
    }

    private record GradeBuckets(int first, int upperSecond, int lowerSecond, int fail) {
        private int total() {
            return first + upperSecond + lowerSecond + fail;
        }
    }

    private String escapeCsv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("CSV contains an unmatched quote.");
        }

        values.add(current.toString());
        return values;
    }
}
