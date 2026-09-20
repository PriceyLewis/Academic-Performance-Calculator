import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

public class LoginWindow {
    private static final String DEMO_USERNAME = "student";
    private static final String DEMO_PASSWORD = "password123";

    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private int loginAttempts = 0;

    public LoginWindow() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        frame = new JFrame("Academic Performance Calculator Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(460, 340);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.setIconImage(loadWindowIcon());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Academic Performance Calculator", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Portfolio demo for academic performance forecasting", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField(DEMO_USERNAME);
        passwordField = new JPasswordField(DEMO_PASSWORD);
        passwordField.setEchoChar('*');

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 0, 12, 0));
        form.add(new JLabel("Username"));
        form.add(usernameField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        JCheckBox showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(e -> passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '*'));

        JLabel hint = new JLabel("Demo credentials: student / password123");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginButton = new JButton("Login");
        JButton helpButton = new JButton("Demo Notes");
        actions.add(loginButton);
        actions.add(helpButton);

        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(subtitle);
        content.add(form);
        content.add(showPasswordCheckbox);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(hint);
        content.add(Box.createRigidArea(new Dimension(0, 14)));
        content.add(actions);

        frame.getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> authenticate());
        helpButton.addActionListener(e -> JOptionPane.showMessageDialog(
            frame,
            "Use the demo credentials shown on screen.\nIf the database is empty, the dashboard opens with sample records so the features can be demonstrated immediately.",
            "Demo Notes",
            JOptionPane.INFORMATION_MESSAGE
        ));

        frame.add(content, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void authenticate() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog loadingDialog = new JDialog(frame, "Signing In", true);
        loadingDialog.setSize(360, 180);
        loadingDialog.setResizable(false);
        loadingDialog.setLocationRelativeTo(frame);
        loadingDialog.setLayout(new BorderLayout());

        JPanel loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel loadingText = new JLabel("Loading dashboard...", SwingConstants.CENTER);
        loadingText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        loadingPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        loadingPanel.add(loadingText);
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        loadingPanel.add(progressBar);
        loadingDialog.add(loadingPanel, BorderLayout.CENTER);

        Timer timer = new Timer(35, null);
        timer.addActionListener(e -> {
            int next = progressBar.getValue() + 5;
            progressBar.setValue(next);
            if (next >= 100) {
                timer.stop();
                loadingDialog.dispose();
                finishAuthentication(username, password);
            }
        });

        timer.start();
        loadingDialog.setVisible(true);
    }

    private void finishAuthentication(String username, String password) {
        if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password)) {
            JOptionPane.showMessageDialog(frame, "Login successful. Opening the dashboard.", "Access Granted", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            SwingUtilities.invokeLater(MainWindow::new);
            return;
        }

        loginAttempts++;
        if (loginAttempts >= 3) {
            JOptionPane.showMessageDialog(frame, "Too many failed attempts. The application will close.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }

        JOptionPane.showMessageDialog(
            frame,
            "Incorrect username or password.\nAttempts remaining: " + (3 - loginAttempts),
            "Login Failed",
            JOptionPane.WARNING_MESSAGE
        );
    }

    private Image loadWindowIcon() {
        URL resource = LoginWindow.class.getResource("/Icons/App Icon.png");
        if (resource != null) {
            return new ImageIcon(resource).getImage();
        }

        Path iconPath = DBConnector.resolveProjectPath("src", "Icons", "App Icon.png");
        if (!Files.exists(iconPath)) {
            return null;
        }
        return new ImageIcon(iconPath.toString()).getImage();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}
