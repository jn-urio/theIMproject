import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class LoginPage {

    // In-memory users for this run only
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        // Dummy default account
        USERS.put("admin", "password123");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::createAndShowLogin);
    }

    private static void createAndShowLogin() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Payroll System Login", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener((ActionEvent e) -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (USERS.containsKey(username) && USERS.get(username).equals(password)) {
                JOptionPane.showMessageDialog(frame,
                        "Login successful. Welcome, " + username + "!",
                        "Login",
                        JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                // Open the payroll window
                page.createAndShowUI();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Invalid username or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                passField.setText("");
            }
        });

        registerButton.addActionListener((ActionEvent e) -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Username and password cannot be empty.",
                        "Register Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            USERS.put(username, password);
            JOptionPane.showMessageDialog(frame,
                    "User \"" + username + "\" registered.\nYou can now log in with this account.",
                    "Registered",
                    JOptionPane.INFORMATION_MESSAGE);
            passField.setText("");
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(passField, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.add(registerButton);
        buttonsPanel.add(loginButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonsPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

