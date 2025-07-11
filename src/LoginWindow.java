import javax.swing.*;
import java.awt.*;

// LoginWindow class creates and manages the login GUI for the application //
public class LoginWindow {
    private JFrame frame;                       
    private JTextField usernameField;           
    private JPasswordField passwordField;       
    private int loginAttempts = 0;              
    private JPanel panel;                       

    // Constructor sets up the login GUI //
    public LoginWindow() {
        frame = new JFrame("Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        passwordField.setEchoChar('*'); // Hides entered password characters //

        // Adds username and password fields to the panel //
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
 
        // Checkbox to toggle password visibility //
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0); // Password visible //
            } else {
                passwordField.setEchoChar('*');      // Password hidden //
            }
        });
        panel.add(showPasswordCheckBox);

        // Buttons panel for login and forgot password functionality //
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton forgotButton = new JButton("Forgot Password?");

        frame.getRootPane().setDefaultButton(loginButton); // Allows pressing Enter to trigger login //

        buttonPanel.add(loginButton);
        buttonPanel.add(forgotButton);
        panel.add(buttonPanel);

        // Login button action listener to authenticate user//
        loginButton.addActionListener(e -> authenticate());

        // Forgot password button action listener //
        forgotButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Please contact AcademicPerformanceCalculator@outlook.com to reset your password.");
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    // Authenticate method validates user credentials //
    public void authenticate() {
        // Loading dialog setup during authentication process //
        JDialog loadingDialog = new JDialog(frame, "Logging in...", true);
        loadingDialog.setSize(500,300);
        loadingDialog.setLocationRelativeTo(frame);

        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel loadingText = new JLabel("Logging in...");
        loadingText.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon spinnerIcon = new ImageIcon("src/Icons/Spinner.gif");
        JLabel spinnerLabel = new JLabel(spinnerIcon);
        spinnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Adds components to loading panel //
        loadingPanel.add(spinnerLabel);
        loadingPanel.add(Box.createRigidArea(new Dimension(0,10)));
        loadingPanel.add(loadingText);
        loadingPanel.add(Box.createRigidArea(new Dimension(0,10)));
        loadingPanel.add(progressBar);

        loadingDialog.getContentPane().add(loadingPanel);

        // Thread to simulate loading progress bar //
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(150); // Simulates loading time //
                    final int progress = i;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                }

                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose();

                    // Retrieves entered credentials //
                    String username = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword());  

                    // Validates credentials (basic check for demonstration) //
                    if (username.equals("student") && password.equals("password123")) {
                        JOptionPane.showMessageDialog(frame, "Welcome, " + username + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                        // Asks user if they want to load sample data //
                        int response = JOptionPane.showConfirmDialog(
                            frame,
                            "Would you like to load sample data into your module list?",
                            "Load Sample Data",
                            JOptionPane.YES_NO_OPTION
                        );

                        frame.dispose();

                        boolean loadSample = (response == JOptionPane.YES_OPTION);
                        new MainWindow(loadSample); // Opens main window with or without sample data //

                    } else {
                        // Handles failed login attempts //
                        loginAttempts++;
                        if (loginAttempts >= 3) {
                            JOptionPane.showMessageDialog(frame, "Too many failed attempts. Exiting.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Incorrect Username or Password.\nAttempts left: " + (3 - loginAttempts), "Login Failed", JOptionPane.WARNING_MESSAGE);
                            usernameField.setText("");
                            passwordField.setText("");
                            usernameField.requestFocus();
                        }
                    }                        
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        loadingDialog.setVisible(true);
    }

    // Main method to run the login window when the program starts //
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWindow());
    }
}
