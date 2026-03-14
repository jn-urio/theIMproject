import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;

public class LoginPage {

    private static final Color SAGE = new Color(0xA4, 0xBB, 0x8E);
    private static final Color DARK_OLIVE = new Color(0x5E, 0x71, 0x4B);
    private static final Color GOLDEN = new Color(0xE8, 0xAB, 0x2F);
    private static final Color DARK_BROWN = new Color(0x58, 0x3C, 0x2A);
    private static final Color WHITE = Color.WHITE;
    private static final Color LIGHT_GREY = new Color(0xF5, 0xF5, 0xF5);
    private static final Color MEDIUM_GREY = new Color(0x88, 0x88, 0x88);
    private static final Color DARK_GREY = new Color(0x55, 0x55, 0x55);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(LoginPage::createAndShowLogin);
    }

    private static void createAndShowLogin() {
        JFrame frame = new JFrame("Philippine Payroll – Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(DARK_OLIVE);
        frame.setBackground(DARK_OLIVE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DARK_OLIVE);
        root.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Top accent strip
        JPanel topAccent = new JPanel();
        topAccent.setPreferredSize(new Dimension(0, 4));
        topAccent.setBackground(SAGE);
        root.add(topAccent, BorderLayout.NORTH);

        // Center card
        JPanel card = new JPanel(new BorderLayout(0, 24));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(SAGE, 1),
            new EmptyBorder(36, 44, 36, 44)
        ));
        card.setMaximumSize(new Dimension(400, 420));

        // Logo (center top) – replace "Philippine Payroll" text
        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        ImageIcon logoIcon = loadLogo("elements/RanchoLogo.jpg", 220);
        if (logoIcon != null) {
            logoLabel.setIcon(logoIcon);
        } else {
            logoLabel.setText("Philippine Payroll");
            logoLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            logoLabel.setForeground(DARK_BROWN);
        }

        JLabel subtitleLabel = new JLabel("HR Login", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(MEDIUM_GREY);
        subtitleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel northCard = new JPanel(new BorderLayout(0, 4));
        northCard.setOpaque(false);
        northCard.add(logoLabel, BorderLayout.NORTH);
        northCard.add(subtitleLabel, BorderLayout.CENTER);
        northCard.setBorder(new EmptyBorder(0, 0, 24, 0));
        card.add(northCard, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userLabel.setForeground(DARK_GREY);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(userLabel, gbc);

        JTextField userField = new JTextField(14);
        styleTextField(userField);
        gbc.gridy = 1;
        formPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        passLabel.setForeground(DARK_GREY);
        gbc.gridy = 2;
        formPanel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(14);
        styleTextField(passField);
        gbc.gridy = 3;
        formPanel.add(passField, gbc);

        gbc.insets = new Insets(14, 0, 0, 0);
        gbc.gridy = 4;

        JButton loginButton = new JButton("Sign in");
        styleLoginButton(loginButton);
        loginButton.addActionListener((ActionEvent e) -> performLogin(frame, userField, passField));
        formPanel.add(loginButton, gbc);

        card.add(formPanel, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.pack();
        frame.setMinimumSize(new Dimension(420, 520));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Load logo from path (relative to working directory). Scale to maxWidth, keep aspect ratio. */
    private static ImageIcon loadLogo(String path, int maxWidth) {
        File file = new File(path);
        if (!file.exists()) return null;
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage();
        if (img.getWidth(null) <= 0) return null;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= maxWidth) return icon;
        int newH = (int) (h * (double) maxWidth / w);
        Image scaled = img.getScaledInstance(maxWidth, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static void styleTextField(JTextComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(MEDIUM_GREY, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        field.setBackground(LIGHT_GREY);
        field.setCaretColor(DARK_BROWN);
        field.setForeground(DARK_BROWN);
    }

    private static void styleLoginButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(GOLDEN);
        b.setForeground(DARK_BROWN);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0xD9, 0x9A, 0x28));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(GOLDEN);
            }
        });
    }

    private static void performLogin(JFrame frame, JTextField userField, JPasswordField passField) {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            HRUserDao.HRUser user = HRUserDao.findByUsernameAndPassword(username, password);
            if (user != null) {
                Integer empId = EmployeeRoleDao.getEmployeeIdByRoleId(user.employeeRoleId);
                AppSession.setHRUser(user.hrUserId, user.username, user.hrRole, empId);
                JOptionPane.showMessageDialog(frame, "Welcome, " + username + "!", "Login", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                page.createAndShowUI();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                passField.setText("");
            }
        } catch (SQLException ex) {
            // Run without database: open main page with entered username as guest
            AppSession.setHRUser(0, username, "Guest", null);
            frame.dispose();
            page.createAndShowUI();
        }
    }
}
