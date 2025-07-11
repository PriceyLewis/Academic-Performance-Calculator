
import java.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.renderer.category.StandardBarPainter;
import javax.swing.Timer;
/**
 * MainWindow.java
 * Academic Performance Calculator - Main Dashboard Window
 * 
 * Author: Lewis Price
 * University: Edge Hill University
 * Student ID: 25328841
 * 
 * Features:
 * - View, Add, Delete modules
 * - Predict final grades 
 * - graph visualisation (Bar, Pie)
 * - Export to CSV
 * - Save and load from database
 * - Settings (Dark Mode Option)
 * - Idle timeout logout
 */
public class MainWindow {

    // Fields (GUI Components) //

    private JFrame frame;
    private JTable table; 
    private DefaultTableModel model;
    private JPanel graphPanel;
    private boolean isDataModified = false;
    private JLabel FinalGradelabel;
    private boolean showingBarChart = true;
    private Timer idletimer;
    private static final int TIMEOUT = 5 * 60 * 1000;
    private boolean isDarkMode = false;
    private Vector<Vector<Object>> backupData = new Vector<>();
    private Stack<Vector<Object>> undoAddStack = new Stack<>();
    private Stack<Vector<Object>> undoDeleteStack = new Stack<>();
    private JTextField searchField;


    //--- Constructor---//
    public MainWindow(boolean loadSample) {
        
        frame = new JFrame("Academic Performance Calculator");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
        if (isDataModified) {
            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "You have unsaved changes. Would you like to save before exiting?",
                "Save Before Exit",
                JOptionPane.YES_NO_CANCEL_OPTION
            );

            if (confirm == JOptionPane.CANCEL_OPTION) return;
            if (confirm == JOptionPane.YES_OPTION) saveChanges();
        }
        frame.dispose();
        }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
frame.setSize((int)(screenSize.width * 0.9), (int)(screenSize.height * 0.9));
frame.setLocationRelativeTo(null);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
       
        

        // Initialising Navigation Bar, Menus and MenuItems //
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Modules To Database");
        JMenuItem exportItem = new JMenuItem("Export");
        JMenuItem ExitItem = new JMenuItem("Exit");
        JMenu helpMenu = new JMenu("Help");
        JMenuItem howToUseItem = new JMenuItem("How to Use");
        JMenuItem aboutItem = new JMenuItem("About");

        // Adding Items into navigation bar //
        fileMenu.add(saveItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(ExitItem);
        helpMenu.add(howToUseItem);
        helpMenu.add(aboutItem);
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        // Sidebar //
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setLayout(new BoxLayout(sidebar,BoxLayout. Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));

        // logo at the top of the side bar //
        ImageIcon logoIcon = resizeIcon("src/icons/App Icon.png", 64, 64);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // resizing Home Icon //
        ImageIcon originalHomeIcon = new ImageIcon("src/Icons/home.png");
        Image img = originalHomeIcon.getImage();
        Image newimg = img.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
        ImageIcon homeIcon = new ImageIcon(newimg);
         
        // resizing Feedback Icon //
        ImageIcon originalFeedbackIcon = new ImageIcon("src/Icons/feedback.png");
        Image feedbackImg = originalFeedbackIcon.getImage();
        Image feedbImage = feedbackImg.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
        ImageIcon feedbackIcon = new ImageIcon(feedbImage);

        // Resizing and Loading sidebar Icons //
        ImageIcon ModulesIcon = resizeIcon("src/Icons/Module.png", 24, 24);
        ImageIcon graphIcon = resizeIcon("src/Icons/Graph.png", 24, 24);
        ImageIcon pieIcon = resizeIcon("src/Icons/Piechart.png", 24, 24);
        ImageIcon settingsIcon = resizeIcon("src/Icons/Settings.png", 24, 24);
        ImageIcon helpIcon = resizeIcon("src/Icons/Help.png", 24, 24);
        ImageIcon logoutIcon = resizeIcon("src/Icons/Logout.png", 24, 24);

        // Button with Icons //
        JButton btnHome = new JButton("Home", homeIcon);
        JButton btnFeedback = new JButton("Feedback", feedbackIcon);
        JButton btnModules = new JButton("View Modules", ModulesIcon);
        JButton btnGraph = new JButton("Full Graph", graphIcon);
        JButton btnPieChart = new JButton("Grade Pie Chart", pieIcon);
        JButton btnSettings = new JButton("Settings", settingsIcon);
        JButton btnHelp = new JButton("Help", helpIcon);
        JButton btnLogout = new JButton("Logout", logoutIcon);


        
        // Button Size //
        Dimension sidebarButtonSize = new Dimension(180, 40);

        btnHome.setMaximumSize(sidebarButtonSize);
        btnFeedback.setMaximumSize(sidebarButtonSize);
        btnModules.setMaximumSize(sidebarButtonSize);
        btnGraph.setMaximumSize(sidebarButtonSize);
        btnPieChart.setMaximumSize(sidebarButtonSize);
        btnSettings.setMaximumSize(sidebarButtonSize);
        btnHelp.setMaximumSize(sidebarButtonSize);
        btnLogout.setMaximumSize(sidebarButtonSize);

        // Tooltips for Sidebar //
        btnHome.setToolTipText("Go to Dashboard Home");
        btnFeedback.setToolTipText("Leave Feedback");
        btnModules.setToolTipText("View all your Modules");
        btnGraph.setToolTipText("See Full Graph for Grades");
        btnPieChart.setToolTipText("View Grade Distribution Piechart");
        btnSettings.setToolTipText("Adjust Settings");
        btnHelp.setToolTipText("Help and Instructions");
        btnLogout.setToolTipText("Logout and return to Login");

        // Hover Effects //
        Color defaultColor = btnHome.getBackground();
        Color hoverColor = new Color(173, 216,230);
        Color lightNormal = new Color(240, 240, 240);
        Color lightHover = new Color(173, 216, 230);
        Color darkNormal = new Color(45, 45, 45);
        Color darkHover = new Color(70, 70, 70); // Darker grey for hover

        addHoverEffect(btnHome, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnFeedback, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnModules, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnGraph, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnPieChart, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnSettings, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnHelp, lightNormal, lightHover, darkNormal, darkHover);
        addHoverEffect(btnLogout, lightNormal, lightHover, darkNormal, darkHover);

        // Adding Items Buttons to the Sidebar (formatting button sizes) //
        sidebar.add(btnHome);
        sidebar.add(Box.createRigidArea(new Dimension(0,10)));
        sidebar.add(btnModules);
        sidebar.add(Box.createRigidArea(new Dimension(0,10)));
        sidebar.add(btnGraph);
        sidebar.add(Box.createRigidArea(new Dimension(0,10)));
        sidebar.add(btnPieChart);
        sidebar.add(Box.createRigidArea(new Dimension(0,10)));
        sidebar.add(btnSettings);
        sidebar.add(Box.createRigidArea(new Dimension(0,10)));
        sidebar.add(btnHelp);
        sidebar.add(Box.createRigidArea(new Dimension(0,10)));
        sidebar.add(btnLogout);

        // adding sidebar to the left of the application //
        frame.add(sidebar, BorderLayout.WEST);

        // Table Center and Creation/Sorting of Database (table) //
        model = new DefaultTableModel(new String[]{"Module Name", "Credits", "Grade", "YearUndertakingModule", "Attendance", "HoursStudied", "Semester"}, 0);
        table = new JTable(model);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        JLabel headerLabel = (JLabel) super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        
        headerLabel.setBackground(isDarkMode ? new Color(45, 45, 45) : Color.WHITE);
        headerLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setOpaque(true);
        return headerLabel;
    }
});

        FinalGradelabel = new JLabel("Final Grade: N/A");
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setAutoCreateRowSorter(true);
        
        // Table Title and Format //
        JLabel tableTitle = new JLabel("Your Modules");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table scroll pane //
        JScrollPane scrollPane = new JScrollPane(table);

        // Create centerpanel, adding title of the table and scrollpane to the center //
        JPanel centerPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.setToolTipText("Search Modules...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JPanel topSearchPanel = new JPanel(new BorderLayout());
        topSearchPanel.add(tableTitle, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        topSearchPanel.add(searchPanel, BorderLayout.SOUTH);

        centerPanel.add(topSearchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Adding CenterPanel to frame (Application) //
        frame.add(centerPanel, BorderLayout.CENTER);
        if (loadSample) {
            loadSampleModules();
            JOptionPane.showMessageDialog(frame, "Sample modules loaded...");
        } else {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Would you like to load your saved modules from the database?",
                "Load Saved Data",
                JOptionPane.YES_NO_OPTION
            );
        
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    loadModules(); // ✅ model is now initialized
                    JOptionPane.showMessageDialog(frame, "Saved modules loaded successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to load saved data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // Graph Panel (Rightside) //
        graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBorder(BorderFactory.createTitledBorder("Visual Dashboard"));
        
        JPanel innerGraphHolder = new JPanel();
        innerGraphHolder.setLayout(new BorderLayout());
        graphPanel.add(innerGraphHolder, BorderLayout.CENTER);
        
        // Placeholder //
        innerGraphHolder.add(new JLabel("Graph Preview Will Show Here", SwingConstants.CENTER), BorderLayout.CENTER);
        
        // Add directly (no scroll pane) //
        frame.add(graphPanel, BorderLayout.EAST);
        


        // Refreshes Table, Graph and Final Grade when changes are made //
        if (loadSample) {
            loadSampleModules();
            JOptionPane.showMessageDialog(frame, "Sample modules have loaded Successfully\nYou can add your own modules or clear the table.", "Sample Modules Loaded...", JOptionPane.INFORMATION_MESSAGE);
        }
        UpdateFinalgrade();
        updateGraph();
        

        // Bottom Buttons //
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Module Controls"));


        JButton btnViewPieChart = new JButton("View Grade Pie Chart");
        JButton btnADD = new JButton("Add Module");
        JButton btnUndoAdd = new JButton("Undo Add");
        JButton btnUndoDelete = new JButton("Undo Delete");
        JButton btnOpenGraph = new JButton("Open Full Bar Chart");
        JButton btnDelete = new JButton("Delete Selected Module");
        JButton btnsaveAllModulesToDatabase = new JButton("Backup (No Overwrite)");
        JButton btnClear = new JButton("Clear Table");
        JButton btnExport = new JButton("Export Modules to CSV");
        JButton btnloadCSVButton = new JButton("Load from CSV");
        JButton btnToggleGraph = new JButton("Toggle Graph");
        JButton saveChangesButton = new JButton("Save and Overwrite Database");
        JButton btnUndoClear = new JButton("Undo Clear");
        JButton btnForestPredict = new JButton("AI Grade Prediction");
        JButton loadButton = new JButton("Load Modules");

        // Bottom Buttons - Group 1: Table Management //
        JPanel tableControlPanel = new JPanel(new FlowLayout());
        tableControlPanel.add(btnADD);
        tableControlPanel.add(btnDelete); 
        tableControlPanel.add(btnClear);  
        tableControlPanel.add(btnUndoClear);
        tableControlPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        tableControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableControlPanel.add(btnUndoAdd);
        tableControlPanel.add(btnUndoDelete);


        // Bottom Buttons - Group 2: Save & loading Management //
        JPanel saveloadPanel = new JPanel(new FlowLayout());
        saveloadPanel.add(btnsaveAllModulesToDatabase);
        saveloadPanel.add(btnExport);
        saveloadPanel.add(btnloadCSVButton);
        saveloadPanel.add(saveChangesButton);
        saveloadPanel.add(loadButton);
        saveloadPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        saveloadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bottom Buttons - Group 3: Graph and Prediction Management //
        JPanel analyticsPanel = new JPanel(new FlowLayout());
        analyticsPanel.add(btnForestPredict);
        analyticsPanel.add(btnOpenGraph);
        analyticsPanel.add(btnToggleGraph);
        analyticsPanel.add(btnViewPieChart);
        analyticsPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        analyticsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Adding Bottom Buttons Groups to Bottom Panel //
        bottomPanel.add(tableControlPanel);
        bottomPanel.add(saveloadPanel);
        bottomPanel.add(analyticsPanel);

        bottomPanel.add(FinalGradelabel);

        // Adding Bottom Panel to Application (frame) //
        JScrollPane bottomScrollPane = new JScrollPane(bottomPanel);
        bottomScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        bottomScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.add(bottomScrollPane, BorderLayout.SOUTH);


        // All Button Actions //
        btnHelp.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame,
                "Academic Performance Calculator Help:\n\n"
              + "Home: Reload your dashboard and data\n"
              + "View Modules: See all your saved module entries\n"
              + "Add Module: Opens a form to add new module details\n"
              + "Delete: Removes a selected module\n"
              + "Undo Add/Delete: Reverts your last add or delete action\n"
              + "Full Graph: Opens full view bar chart of all grades\n"
              + "Pie Chart: Visualize grade distribution by classification\n"
              + "Export / Load: Save or import your modules to/from CSV\n"
              + "Backup: Save current modules to database (no overwrite)\n"
              + "Save & Overwrite: Save all current modules, replacing old ones\n"
              + "AI Grade Prediction: Predicts your classification using machine learning\n"
              + "Settings: Toggle between light and dark mode\n"
              + "Logout: Return to login screen", 
              "Help",
              JOptionPane.INFORMATION_MESSAGE);
        });
        
        
        btnsaveAllModulesToDatabase.addActionListener(e -> saveAllModulesToDatabase());
        btnExport.addActionListener(e -> exportModulesToCSV());

        saveItem.addActionListener(e -> saveAllModulesToDatabase());
        
        
        btnADD.addActionListener(e -> addModule());

        saveChangesButton.addActionListener(e -> saveChanges());

        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Academic Performance Calculator\nVersion 1.0\nDeveloped by Lewis Price (25328841)\nEdge Hill University", "About",
            JOptionPane.INFORMATION_MESSAGE);
});

        loadButton.addActionListener(e -> {
            try {
                loadModules();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error loading modules: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnloadCSVButton.addActionListener(e -> loadModulesFromCSV());

        btnToggleGraph.addActionListener(e -> {
            if (showingBarChart) {
                updatePieGraph();  
            } else {
                updateGraph();     
            }
            showingBarChart = !showingBarChart;
        });
        btnUndoAdd.addActionListener(e -> {
            if (!undoAddStack.isEmpty()) {
                Vector<Object> lastAdded = undoAddStack.pop();
                for (int i = model.getRowCount() - 1; i >= 0; i--) {
                    boolean match = true;
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        if (!model.getValueAt(i, j).toString().equals(lastAdded.get(j).toString())) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        model.removeRow(i);
                        updateGraph();
                        UpdateFinalgrade();
                        JOptionPane.showMessageDialog(frame, "Last added module has been removed.");
                        return;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nothing to undo.");
            }
        });
        
        btnModules.addActionListener(e -> {
            JDialog viewDialog = new JDialog(frame, "All Modules", true);
            viewDialog.setSize(600,400);
            viewDialog.setLocationRelativeTo(frame);
        
            JTable viewTable = new JTable(model);
            JScrollPane viewScrollPane = new JScrollPane(viewTable);

            viewDialog.add(viewScrollPane);
            viewDialog.setVisible(true);
        });

        btnSettings.addActionListener(e -> {
            JDialog settingsDialog = new JDialog(frame, "Settings", true);
            applyTheme(settingsDialog.getContentPane(), isDarkMode ? new Color(45, 45, 45) : Color.WHITE, isDarkMode ? Color.WHITE : Color.BLACK);
            settingsDialog.setSize(400, 300);
            settingsDialog.setLocationRelativeTo(frame);
            settingsDialog.setLayout(new GridLayout(4, 1, 10, 10));
        
            JLabel settingsTitle = new JLabel("Settings", SwingConstants.CENTER);
            settingsTitle.setFont(new Font("Arial", Font.BOLD,18));
        
            JButton changeThemeButton = new JButton("Toggle Dark Mode");
            changeThemeButton.addActionListener(ev -> toggleDarkMode());
            JButton changePasswordButton = new JButton("Change Password");
            JButton closeButton = new JButton("Close");
        
            settingsDialog.add(settingsTitle);
            settingsDialog.add(changeThemeButton);
            settingsDialog.add(changePasswordButton);
            
            settingsDialog.add(closeButton);
        
            closeButton.addActionListener(ev -> settingsDialog.dispose());
            changePasswordButton.addActionListener(ev -> {
                JOptionPane.showMessageDialog(settingsDialog,
                    "Change password functionality coming soon!",
                    "Change Password",
                    JOptionPane.INFORMATION_MESSAGE);
            });
            settingsDialog.setVisible(true);
        });

        btnForestPredict.addActionListener(e -> {
            try {
                // Ask user which mode they want to use //
                int choice = JOptionPane.showOptionDialog(
                        frame,
                        "Choose prediction method:",
                        "Prediction Mode",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Manual Entry (with blanks)", "Use Table Data"},
                        "Manual Entry");
        
                // Mode 1: Manual Entry //
                if (choice == JOptionPane.YES_OPTION) {
                    // Ask user for each input (allow blanks)
                    String attInput = JOptionPane.showInputDialog("Enter Attendance % (leave blank if unknown):");
                    String hrsInput = JOptionPane.showInputDialog("Enter Hours Studied (leave blank if unknown):");
                    String gradeInput = JOptionPane.showInputDialog("Enter Current Grade % (leave blank if unknown):");
                    String credInput = JOptionPane.showInputDialog("Enter Module Credits (leave blank if unknown):");
        
                    Double attendance = attInput.isEmpty() ? null : Double.parseDouble(attInput);
                    Double hoursStudied = hrsInput.isEmpty() ? null : Double.parseDouble(hrsInput);
                    Double grade = gradeInput.isEmpty() ? null : Double.parseDouble(gradeInput);
                    Double credits = credInput.isEmpty() ? null : Double.parseDouble(credInput);
        
                    // Prepare training data //
                    ArrayList<double[]> trainingData = new ArrayList<>();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        try {
                            double a = Double.parseDouble(model.getValueAt(i, 4).toString());
                            double h = Double.parseDouble(model.getValueAt(i, 5).toString());
                            double g = Double.parseDouble(model.getValueAt(i, 2).toString());
                            double c = Double.parseDouble(model.getValueAt(i, 1).toString());
                            double classification = (g >= 70) ? 3 : (g >= 60) ? 2 : (g >= 50) ? 1 : 0;
                            trainingData.add(new double[]{a, h, g, c, classification});
                        } catch (Exception ignored) {}
                    }
        
                    // Train regressor to fill in any missing values //
                    RandomForestRegressor regressor = new RandomForestRegressor();
                    ArrayList<double[]> regData = new ArrayList<>();
                    for (double[] row : trainingData) {
                        regData.add(new double[]{row[0], row[1], row[2], row[3]});
                    }
                    regressor.train(regData);
        
                    if (attendance == null && hoursStudied != null && grade != null)
                        attendance = regressor.predict(hoursStudied, grade);
                    if (hoursStudied == null && attendance != null && grade != null)
                        hoursStudied = regressor.predict(attendance, grade);
                    if (grade == null && attendance != null && hoursStudied != null)
                        grade = regressor.predict(attendance, hoursStudied);
                    if (credits == null) {
                        double sum = 0;
                        for (double[] row : trainingData) sum += row[3];
                        credits = sum / trainingData.size();
                    }
        
                    // Trains classifier //
                    RandomForestModel classifier = new RandomForestModel();
                    classifier.train(trainingData);
        
                    // Voting logic for stability //
                    Map<String, Integer> votes = new HashMap<>();
                    for (int i = 0; i < 100; i++) {
                        String prediction = classifier.predict(
                                attendance + Math.random() * 2 - 1,
                                hoursStudied + Math.random() * 2 - 1,
                                grade + Math.random() * 2 - 1,
                                credits + Math.random() * 2 - 1
                        );
                        votes.put(prediction, votes.getOrDefault(prediction, 0) + 1);
                    }
        
                    String finalPrediction = votes.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
                    int confidence = votes.get(finalPrediction);
        
                    // Build feedback report //
                    StringBuilder report = new StringBuilder();
                    report.append("MANUAL PREDICTION RESULT\n");
                    report.append("-----------------------------\n");
                    report.append("Predicted Classification: ").append(finalPrediction).append("\n");
                    report.append("Prediction Confidence: ").append(confidence).append("%\n\n");
        
                    report.append("Used Values:\n");
                    report.append("- Attendance: ").append(String.format("%.1f", attendance)).append("%\n");
                    report.append("- Hours Studied: ").append(String.format("%.1f", hoursStudied)).append("\n");
                    report.append("- Current Grade: ").append(String.format("%.1f", grade)).append("%\n");
                    report.append("- Module Credits: ").append(String.format("%.1f", credits)).append("\n\n");
        
                    // Feedback section //
                    report.append("🎓 Feedback & Advice:\n");
                    if (attendance < 60) report.append("- Low attendance. Increase lecture participation.\n");
                    else if (attendance < 75) report.append("- Attendance is okay. Aim for >75%.\n");
                    else report.append("- Excellent attendance. Keep it up!\n");
        
                    if (hoursStudied < 50) report.append("- Low study hours. Consider a study schedule.\n");
                    else if (hoursStudied < 100) report.append("- Good effort. A few more hours could help.\n");
                    else report.append("- Great study discipline!\n");
        
                    if (grade < 50) report.append("- Current grade is failing. Seek academic support.\n");
                    else if (grade < 60) report.append("- Grade is near 2:2. Stay consistent!\n");
                    else if (grade < 70) report.append("- Solid grade. You're on track for a 2:1.\n");
                    else report.append("- First-class level! Aim to maintain this.\n");
        
                    JOptionPane.showMessageDialog(frame, new JScrollPane(new JTextArea(report.toString(), 12, 60)));
        
                // Mode 2: Prediction Table //
                } else {
                    ArrayList<double[]> training = new ArrayList<>();
                    ArrayList<double[]> incomplete = new ArrayList<>();
        
                    for (int i = 0; i < model.getRowCount(); i++) {
                        try {
                            double a = Double.parseDouble(model.getValueAt(i, 4).toString());
                            double h = Double.parseDouble(model.getValueAt(i, 5).toString());
                            double g = Double.parseDouble(model.getValueAt(i, 2).toString());
                            double c = Double.parseDouble(model.getValueAt(i, 1).toString());
                            double cls = (g >= 70) ? 3 : (g >= 60) ? 2 : (g >= 50) ? 1 : 0;
                            training.add(new double[]{a, h, g, c, cls});
                        } catch (Exception e1) {
                            try {
                                double a = Double.parseDouble(model.getValueAt(i, 4).toString());
                                double h = Double.parseDouble(model.getValueAt(i, 5).toString());
                                double c = Double.parseDouble(model.getValueAt(i, 1).toString());
                                incomplete.add(new double[]{a, h, c});
                            } catch (Exception ignore) {}
                        }
                    }
        
                    // Trains regressor //
                    RandomForestRegressor regressor = new RandomForestRegressor();
                    ArrayList<double[]> regData = new ArrayList<>();
                    for (double[] r : training) regData.add(new double[]{r[0], r[1], r[2]});
                    regressor.train(regData);
        
                    for (double[] row : incomplete) {
                        double predictedGrade = regressor.predict(row[0], row[1]);
                        double cls = (predictedGrade >= 70) ? 3 : (predictedGrade >= 60) ? 2 : (predictedGrade >= 50) ? 1 : 0;
                        training.add(new double[]{row[0], row[1], predictedGrade, row[2], cls});
                    }
        
                    // Trains classifier //
                    RandomForestModel classifier = new RandomForestModel();
                    classifier.train(training);
        
                    double sumA = 0, sumH = 0, sumG = 0, sumC = 0;
                    for (double[] r : training) {
                        sumA += r[0]; sumH += r[1]; sumG += r[2]; sumC += r[3];
                    }
                    int size = training.size();
                    double avgA = sumA / size, avgH = sumH / size, avgG = sumG / size, avgC = sumC / size;
        
                    String predicted = classifier.predict(avgA, avgH, avgG, avgC);
        
                    StringBuilder summary = new StringBuilder();
                    summary.append("TABLE-WIDE PREDICTION\n");
                    summary.append("-----------------------------\n");
                    summary.append("Predicted Classification: ").append(predicted).append("\n\n");
                    summary.append("Averaged Inputs from All Modules:\n");
                    summary.append("- Attendance: ").append(String.format("%.1f", avgA)).append("%\n");
                    summary.append("- Hours Studied: ").append(String.format("%.1f", avgH)).append("\n");
                    summary.append("- Grade: ").append(String.format("%.1f", avgG)).append("%\n");
                    summary.append("- Credits: ").append(String.format("%.1f", avgC)).append("\n");
        
                    JOptionPane.showMessageDialog(frame, new JScrollPane(new JTextArea(summary.toString(), 12, 60)));
                }
        
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });
        
        
        // Resumes Providing Functions to Buttons Via ActionListener//
        btnClear.addActionListener(e -> {
            // Backups current data //
            backupData.clear();
            for (int i = 0; i < model.getRowCount(); i++) {
                Vector<Object> row = new Vector<>();
                for (int j = 0; j < model.getColumnCount(); j++) {
                    row.add(model.getValueAt(i, j));
                }
                backupData.add(row);
            }
        
            // Clears the table //
            model.setRowCount(0);
            updateGraph();
            UpdateFinalgrade();
            JOptionPane.showMessageDialog(frame, "Table Cleared. You can Undo from the Undo button!");
        });
        

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to return to the Login Screen", "Confirm Logout", JOptionPane.YES_NO_OPTION);

            if(confirm ==JOptionPane.YES_NO_OPTION) {
                frame.dispose();
                new LoginWindow();
            }
        });


        btnViewPieChart.addActionListener(e -> showGradePieChart());

        btnHome.addActionListener(e -> {
            try {
                loadModules();
                updateGraph();
                JOptionPane.showMessageDialog(frame, "Refreshed Home...");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error loading modules: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnGraph.addActionListener(e -> showFullgraph());
        btnPieChart.addActionListener(e -> showGradePieChart());

        btnUndoClear.addActionListener(e -> {
            if (!backupData.isEmpty()) {
                model.setRowCount(0);
                for (Vector<Object> row : backupData) {
                    model.addRow(row);
                }
                updateGraph();
                UpdateFinalgrade();
                backupData.clear();
                JOptionPane.showMessageDialog(frame, "Table Restored Successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Nothing to undo.");
            }
        });
        btnFeedback.addActionListener(e -> {
            String feedback = JOptionPane.showInputDialog(frame, "Please leave your feedback below...");
            if (feedback != null && !feedback.trim().isEmpty()) {
                try {
                    File feedbackFile = new File("UserFeedback.txt");
                    PrintWriter writer = new PrintWriter(new java.io.FileWriter(feedbackFile, true)); // <-- fixed here
                    writer.println("Feedback: " + feedback);
                    writer.println("Submitted at: " + new java.util.Date());
                    writer.println("-----------------------------");
                    writer.close();
        
                    JOptionPane.showMessageDialog(frame, "Thank you! Your feedback has been saved", "Feedback Received", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving feedback: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
                }
            } else {
                JOptionPane.showMessageDialog(frame, "No Feedback entered...", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

       
        btnUndoDelete.addActionListener(e -> {
            if (!undoDeleteStack.isEmpty()) {
                Vector<Object> restoredRow = undoDeleteStack.pop();
                model.addRow(restoredRow);
                updateGraph();
                UpdateFinalgrade();
                JOptionPane.showMessageDialog(frame, "Last deleted module restored.");
            } else {
                JOptionPane.showMessageDialog(frame, "No deleted modules to undo.");
            }
        });
        
    
        ExitItem.addActionListener(e -> {
            if (isDataModified) {
                int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "You have unsaved changes. Would you like to save before exiting?",
                    "Save Before Exit",
                    JOptionPane.YES_NO_CANCEL_OPTION
                );
        
                if (confirm == JOptionPane.CANCEL_OPTION) return;
                if (confirm == JOptionPane.YES_OPTION) saveChanges();
            }
        
            System.exit(0);
        });
        
        frame.setVisible(true);
        setupIdleLogout();

        saveChangesButton.addActionListener(e -> {
            try {
                saveChanges();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error saving changes: " + ex.getMessage());
            }
        });

        btnOpenGraph.addActionListener(e -> showFullgraph());
        exportItem.addActionListener(e -> exportModulesToCSV());

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String moduleName = model.getValueAt(selectedRow, 0).toString();
                try {
                    
                    Vector<Object> deletedRow = new Vector<>();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        deletedRow.add(model.getValueAt(selectedRow, i));
                    }
                    undoDeleteStack.push(deletedRow);  
        
                    ModuleDAO dao = new ModuleDAO();
                    model.removeRow(selectedRow);
                    updateGraph();
                    UpdateFinalgrade();
                    isDataModified = true;
                    JOptionPane.showMessageDialog(frame, "Module Deleted Successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error deleting module: " + ex.getMessage());
                }
            }
        });
        
        // Functions For Buttons and Features //
    }
    private void filterTable() {
    String keyword = searchField.getText().toLowerCase();
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);
    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
}

    private void updateGraph() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int i = 0; i < model.getRowCount(); i++) {
            String module = model.getValueAt(i, 0).toString();
            double grade = Double.parseDouble(model.getValueAt (i, 2).toString());
            dataset.addValue(grade, "Grades", module);
        }

        JFreeChart barChart = ChartFactory.createBarChart("Module Grades", "Module", "Grade", dataset);
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Added Color to bars dependent on grades achieved //
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                double grade = dataset.getValue(row, column).doubleValue();
                if (grade >= 70) {
                    return new Color(0, 128, 0); // Green
                } else if (grade >= 60) {
                    return new Color(0, 0, 255); // Blue
                } else if (grade >= 50) {
                    return new Color(255, 165, 0); // Orange
                } else {
                    return Color.RED; // Red
                }
            }
        };
    
        renderer.setBarPainter(new StandardBarPainter());
        plot.setRenderer(renderer);
    
        graphPanel.removeAll();
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(300, graphPanel.getHeight()));
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();

        graphPanel.revalidate();
        graphPanel.repaint();
    }
    private void loadSampleModules() {
        if (model.getRowCount() > 0) {
            return; // Prevent duplicate loading
        }
    
        Object[][] sampleModules = {
            {"Programming Fundamentals", 15, 75, 1, 90, 150, 1},
            {"Database Systems", 15, 65, 1, 85, 140, 2},
            {"Computer Networks", 15, 55, 2, 80, 120, 1},
            {"Software Engineering", 30, 72, 2, 88, 200, 2},
            {"Web Development", 15, 48, 3, 75, 110, 1}
        };
    
        for (Object[] row : sampleModules) {
            Vector<Object> addedRow = new Vector<>(Arrays.asList(row));
            undoAddStack.push(addedRow);  // ✅ Now addedRow is declared
            model.addRow(row);
        }
    }
    
        
    private void toggleDarkMode() {
        Color backgroundColor = isDarkMode ? Color.WHITE : new Color(45, 45, 45);
        Color textColor = isDarkMode ? Color.BLACK : Color.WHITE;
    
        // Apply to all main content //
        applyTheme(frame.getContentPane(), backgroundColor, textColor);
    
        // Force JTable header styling //
        JTableHeader header = table.getTableHeader();
        header.setOpaque(true);
        header.setBackground(backgroundColor);
        header.setForeground(textColor);
        header.repaint();
    
        // Update JMenuBar and all its menus/items //
        JMenuBar menuBar = frame.getJMenuBar();
        if (menuBar != null) {
            menuBar.setBackground(backgroundColor);
            menuBar.setForeground(textColor);
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                if (menu != null) {
                    menu.setOpaque(true);
                    menu.setBackground(backgroundColor);
                    menu.setForeground(textColor);
                    for (int j = 0; j < menu.getItemCount(); j++) {
                        JMenuItem item = menu.getItem(j);
                        if (item != null) {
                            item.setOpaque(true);
                            item.setBackground(backgroundColor);
                            item.setForeground(textColor);
                        }
                    }
                }
            }
            menuBar.repaint();
            table.getTableHeader().repaint();
        }
    
        // Force sidebar button styling again //
        for (Component comp : frame.getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                for (Component child : ((JPanel) comp).getComponents()) {
                    if (child instanceof JButton) {
                        child.setBackground(backgroundColor);
                        child.setForeground(textColor);
                        ((JButton) child).setOpaque(true);
                        ((JButton) child).setBorderPainted(false);
                    }
                }
            }
        }
    
        isDarkMode = !isDarkMode;
    }
    
   
    private void showFullgraph() {
        JFrame graphWindow = new JFrame("Full Graph View");
        graphWindow.setSize(900, 600);
        graphWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphWindow.setLayout(new BorderLayout());
    
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < model.getRowCount(); i++) {
            String moduleName = model.getValueAt(i, 0).toString();
            try {
                double grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                dataset.addValue(grade, "Grades", moduleName);
            } catch (Exception e) {
            }
        }
    
        // Create Bar Chart //
        JFreeChart barChart = ChartFactory.createBarChart(
            "Full View: Module Grades", "Module", "Grade", dataset);
        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
    
        // Custom color rendering //
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                double grade = dataset.getValue(row, column).doubleValue();
                if (grade >= 70) return new Color(0, 128, 0);         // Green
                else if (grade >= 60) return new Color(0, 0, 255);    // Blue
                else if (grade >= 50) return new Color(255, 165, 0);  // Orange
                else return Color.RED;
            }
        };
        renderer.setBarPainter(new StandardBarPainter());
        plot.setRenderer(renderer);
    
        ChartPanel chartPanel = new ChartPanel(barChart);
    
        //  Add Legend Panel (Color Index) //
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new GridLayout(4, 1));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Grade Colour Index"));
    
        legendPanel.add(createLegendItem("First (70%+)", new Color(0, 128, 0)));
        legendPanel.add(createLegendItem("2:1 (60-69%)", new Color(0, 0, 255)));
        legendPanel.add(createLegendItem("2:2 (50-59%)", new Color(255, 165, 0)));
        legendPanel.add(createLegendItem("Fail (<50%)", Color.RED));
    
        // Add chart and legend to window //
        graphWindow.add(chartPanel, BorderLayout.CENTER);
        graphWindow.add(legendPanel, BorderLayout.EAST);
    
        graphWindow.setLocationRelativeTo(null);
        graphWindow.setVisible(true);
    }
    
    // Helper to create legend items //
    private JPanel createLegendItem(String label, Color color) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel colorBox = new JLabel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        JLabel textLabel = new JLabel(label);
        itemPanel.add(colorBox);
        itemPanel.add(Box.createHorizontalStrut(5));
        itemPanel.add(textLabel);
        return itemPanel;
    }
    

        private void showGradePieChart() {
            int first = 0, upperSecond = 0, lowerSecond = 0, fail = 0;

            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    double grade = Double.parseDouble(model.getValueAt(i, 2).toString());

                    if (grade >= 70) first++;
                    else if (grade >= 60) upperSecond++;
                    else if (grade >= 50) lowerSecond++;
                    else fail++;

                } catch (Exception e) {

                }
                }
            
            
            
            DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue("First (70%+)", first);
            dataset.setValue("2:1 (60-69%+)", upperSecond);
            dataset.setValue("2:2 (50-59%+)", lowerSecond);
            dataset.setValue("Fail (<50%)", fail);

            JFreeChart pieChart = ChartFactory.createPieChart("Grade Distribution", dataset, true, true, false);

            PiePlot plot = (PiePlot) pieChart.getPlot();
            plot.setSectionPaint("First (70%+)", new Color(0,128, 0));
            plot.setSectionPaint("2:1 (60-69%+)", new Color(0,0, 255)); 
            plot.setSectionPaint("2:2 (50-59%+)", new Color(255,165,0)); // make orange here instead of red
            plot.setSectionPaint("Fail (<50%)", Color.RED);

            plot.setSimpleLabels(true);

            ChartPanel pieChartPanel = new ChartPanel(pieChart);
            JFrame pieFrame = new JFrame("Grade Distribution");
            pieFrame.setSize(600, 400);
            pieFrame.add(pieChartPanel);
            pieFrame.setLocationRelativeTo(frame);
            pieFrame.setVisible(true);
            }

            private void loadModules() throws SQLException {
                ModuleDAO dao = new ModuleDAO();
                java.util.List<Module> modules = dao.getAllModules();
                model.setRowCount(0);
                for (Module m : modules) {
                    model.addRow(new Object[]{
                        m.getName(), 
                        m.getCredits(), 
                        m.getGrade(), 
                        m.getYearUndertakingModule(), 
                        m.getAttendance(),
                        m.getHoursStudied(),
                        m.getSemester()
                        
                    });
                }
                updateGraph();
                UpdateFinalgrade();
                System.out.println("Table now has " + model.getRowCount() + " rows.");
                if (modules.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No Modules Found in the database...", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            
            private void applyTheme(Component component, Color bg, Color fg) {
    component.setBackground(bg);
    component.setForeground(fg);

    if (component instanceof JMenuBar || component instanceof JMenu || component instanceof JMenuItem) {
        component.setBackground(bg);
        component.setForeground(fg);
    }

    if (component instanceof JTable) {
        JTable t = (JTable) component;
        t.setBackground(bg);
        t.setForeground(fg);
        t.setGridColor(Color.LIGHT_GRAY);

        JTableHeader header = t.getTableHeader();
        if (header != null) {
            header.setBackground(bg);
            header.setForeground(fg);
            header.setOpaque(true);
            header.repaint();
        }
    }

    if (component instanceof JButton) {
        JButton btn = (JButton) component;
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
    }

    if (component instanceof JLabel) {
        ((JLabel) component).setForeground(fg);
    }

    if (component instanceof Container) {
        for (Component child : ((Container) component).getComponents()) {
            applyTheme(child, bg, fg);
        }
    }
}

            
            
            
            
        private void loadModulesFromCSV() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select CSV File to Load");

            int userSelection = fileChooser.showOpenDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    File fileToLoad = fileChooser.getSelectedFile();
                    Scanner scanner = new Scanner(fileToLoad);
                    model.setRowCount(0);

                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }

                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] data = line.split(",");

                        if (data.length == model.getColumnCount()) {
                            model.addRow(data);
                        }
                    }

                    scanner.close();
                    JOptionPane.showMessageDialog(frame, "Modules Loaded Successfully from CSV.");
                    updateGraph();
                    UpdateFinalgrade();
                } catch( Exception e) {
                    JOptionPane.showMessageDialog(frame, "Error loading file: " + e.getMessage());
                }
            }
        }
        private void UpdateFinalgrade() {
            double totalpoints = 0;
            double totalCredits = 0;

            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    double grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                    int credits = Integer.parseInt(model.getValueAt(i, 1).toString());
                    totalpoints += grade * credits;
                    totalCredits += credits;
                } catch (Exception ignored) {}
                }

                if (totalCredits == 0) {
                    FinalGradelabel.setText("Predicted Final Grade");
                } else {
                    double predictedGrade = totalpoints / totalCredits;
                    FinalGradelabel.setText(String.format("Predicted Final Grade: %.2f", predictedGrade));

                    if (predictedGrade >= 70) {
                        FinalGradelabel.setForeground(new Color(0,128,0)); //--green--//
                    } else if (predictedGrade >= 60) {
                        FinalGradelabel.setForeground(new Color(0,0,255)); //--Blue--//
                    } else if (predictedGrade >= 50) {
                        FinalGradelabel.setForeground(new Color(255,165,0)); //--orange--//                               
                    } else {
                        FinalGradelabel.setForeground(Color.RED);
                    }
                }
            }
        
            private void setupIdleLogout() {
                idletimer = new Timer(TIMEOUT, e -> {
                    int choice = JOptionPane.showConfirmDialog(frame, 
                        "You haven't made any actions in a while. Do you want to stay logged in?",
                        "Auto Logout Warning", 
                        JOptionPane.YES_NO_OPTION);
            
                    if (choice != JOptionPane.YES_OPTION) {
                        frame.dispose();
                        new LoginWindow();
                    } else {
                        idletimer.restart(); 
                    }
                });
            
                idletimer.setRepeats(false);
                idletimer.start();
            
                Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
                    idletimer.restart(); 
                }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
            }
            private void saveChanges() {
                try {
                    ModuleDAO dao = new ModuleDAO();
                    dao.clearModules();
            
                    for (int i = 0; i < model.getRowCount(); i++) {
                        String moduleName = model.getValueAt(i, 0).toString();
                        int credits = Integer.parseInt(model.getValueAt(i, 1).toString());
                        double grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                        int year = Integer.parseInt(model.getValueAt(i, 3).toString());
                        int attendance = Integer.parseInt(model.getValueAt(i, 4).toString());
                        int hoursStudied = Integer.parseInt(model.getValueAt(i, 5).toString());
                        int semester = Integer.parseInt(model.getValueAt(i, 6).toString());

            
                        Module m = new Module(moduleName, credits, grade, year, semester, attendance, hoursStudied);
                        dao.addModule(m);
                    }
            
                    JOptionPane.showMessageDialog(frame, "Changes Saved Successfully!");
            
                    loadModules(); 
                    
                    updateGraph();
                    UpdateFinalgrade();
                    isDataModified = false;
            
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving changes: " + ex.getMessage());
                }
            }
                
            
                private void exportModulesToCSV() {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save Modules As CSV");
                    int userSelection = fileChooser.showSaveDialog(frame);
                
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        try {
                            File fileToSave = fileChooser.getSelectedFile();
                
                            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
                            }
                
                            PrintWriter writer = new PrintWriter(fileToSave);
        
                            for (int i = 0; i < model.getColumnCount(); i++) {
                                writer.print(model.getColumnName(i));
                                if (i < model.getColumnCount() - 1) writer.print(",");
                            }
                            writer.println();
                
                            for (int row = 0; row < model.getRowCount(); row++) {
                                for (int col = 0; col < model.getColumnCount(); col++) {
                                    writer.print(model.getValueAt(row, col));
                                    if (col < model.getColumnCount() - 1) writer.print(",");
                                }
                                writer.println();

                            }
                
                            writer.close();
                            JOptionPane.showMessageDialog(frame, "Modules Exported Successfully");
                            Desktop.getDesktop().open(fileToSave);
                
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(frame, "Error exporting file: " + e.getMessage());
                        }
                        
                    }
                }
        
                
                
                
                private void addHoverEffect(JButton button, Color normalLight, Color hoverLight, Color normalDark, Color hoverDark) {
                    button.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                            button.setBackground(isDarkMode ? hoverDark : hoverLight);
                        }
                        public void mouseExited(java.awt.event.MouseEvent evt) {
                            button.setBackground(isDarkMode ? normalDark : normalLight);
                        }
                    });
                }
                
        
        private void updatePieGraph() {
            DefaultPieDataset dataset = new DefaultPieDataset();
            int first = 0, upperSecond = 0, lowerSecond = 0, fail = 0;
        
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    double grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                    if (grade >= 70) first++;
                    else if (grade >= 60) upperSecond++;
                    else if (grade >= 50) lowerSecond++;
                    else fail++;
                } catch (Exception e) {
                }
            }
        
            dataset.setValue("First (70%+)", first);
            dataset.setValue("2:1 (60-69%)", upperSecond);
            dataset.setValue("2:2 (50-59%)", lowerSecond);
            dataset.setValue("Fail (<50%)", fail);
        
            JFreeChart pieChart = ChartFactory.createPieChart("Grade Distribution", dataset, true, true, false);
            PiePlot plot = (PiePlot) pieChart.getPlot();
            plot.setSectionPaint("First (70%+)", new Color(0, 128, 0));
            plot.setSectionPaint("2:1 (60-69%)", new Color(0, 0, 255));
            plot.setSectionPaint("2:2 (50-59%)", new Color(255, 165, 0));
            plot.setSectionPaint("Fail (<50%)", Color.RED);
        
            plot.setSimpleLabels(true);
        
        
            graphPanel.removeAll();
            graphPanel.add(new ChartPanel(pieChart));
            graphPanel.revalidate();
            graphPanel.repaint();
        }
        private DefaultCategoryDataset buildCategoryDataset() {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < model.getRowCount(); i++) {
                String module = model.getValueAt(i, 0).toString();
                double grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                dataset.addValue(grade, "Grades", module);
            }
            return dataset;
        }
        
        private DefaultPieDataset buildPieDataset() {
            int first = 0, upperSecond = 0, lowerSecond = 0, fail = 0;
        
            for (int i = 0; i < model.getRowCount(); i++) {
                double grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                if (grade >= 70) first++;
                else if (grade >= 60) upperSecond++;
                else if (grade >= 50) lowerSecond++;
                else fail++;
            }
        
            DefaultPieDataset dataset = new DefaultPieDataset();
            dataset.setValue("First (70%+)", first);
            dataset.setValue("2:1 (60-69%)", upperSecond);
            dataset.setValue("2:2 (50-59%)", lowerSecond);
            dataset.setValue("Fail (<50%)", fail);
            return dataset;
        }
        
        private void saveAllModulesToDatabase() {
            try {
                ModuleDAO dao = new ModuleDAO();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String ModuleName = model.getValueAt(i, 0).toString();
                    int Credits = Integer.parseInt(model.getValueAt(i, 1).toString());
                    double Grade = Double.parseDouble(model.getValueAt(i, 2).toString());
                    int YearUndertakingModule = Integer.parseInt(model.getValueAt(i, 3).toString());
                    int Attendance = Integer.parseInt(model.getValueAt(i, 4).toString());
                    int HoursStudied = Integer.parseInt(model.getValueAt(i, 5).toString());
                    int Semester = Integer.parseInt(model.getValueAt(i, 6).toString());
                    
                    Module m = new Module(ModuleName, Credits, Grade, YearUndertakingModule, Semester, Attendance, HoursStudied);
                    dao.addModule(m);
                }
                JOptionPane.showMessageDialog(frame, "All Modules Saved Successfully...");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame," Error Saving Modules" + ex.getMessage());}
            }

            private ImageIcon resizeIcon(String path, int width, int height) {
                ImageIcon orignalIcon = new ImageIcon(path);
                Image img = orignalIcon.getImage();
                Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(resizedImg);
            }
            private void addModule() {
                JTextField nameField = new JTextField();
                JTextField creditsField = new JTextField();
                JTextField gradeField = new JTextField();
                JTextField yearField = new JTextField();
                JTextField attendanceField = new JTextField();
                JTextField hoursField = new JTextField();
                JTextField semesterField = new JTextField();
            
                JPanel panel = new JPanel(new GridLayout(0, 2));
                panel.add(new JLabel("Module Name:")); panel.add(nameField);
                panel.add(new JLabel("Credits:")); panel.add(creditsField);
                panel.add(new JLabel("Grade:")); panel.add(gradeField);
                panel.add(new JLabel("Year Undertaking:")); panel.add(yearField);
                panel.add(new JLabel("Attendance %:")); panel.add(attendanceField);
                panel.add(new JLabel("Hours Studied:")); panel.add(hoursField);
                panel.add(new JLabel("Semester:")); panel.add(semesterField);
            
                int result = JOptionPane.showConfirmDialog(frame, panel, "Add Module", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String name = nameField.getText().trim();
                        int credits = Integer.parseInt(creditsField.getText().trim());
                        double grade = Double.parseDouble(gradeField.getText().trim());
                        int year = Integer.parseInt(yearField.getText().trim());
                        int attendance = Integer.parseInt(attendanceField.getText().trim());
                        int hours = Integer.parseInt(hoursField.getText().trim());
                        int semester = Integer.parseInt(semesterField.getText().trim());
            
                        Vector<Object> newRow = new Vector<>(Arrays.asList(name, credits, grade, year, attendance, hours, semester));
                        model.addRow(newRow);
                        undoAddStack.push(newRow); 
                        updateGraph();
                        UpdateFinalgrade();
                        isDataModified = true;
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
        }
        