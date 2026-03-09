import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class page {
    // 1. GLOBAL COMPONENTS: These allow all buttons to "talk" to the same deck of pages
    private static CardLayout cardLayout = new CardLayout();
    private static JPanel cardPanel = new JPanel(cardLayout);

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(page::createAndShowUI);
    }

    public static void createAndShowUI() {
        JFrame frame = new JFrame("Integrated Payroll & HR System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1200, 800));

        JPanel mainPanel = new JPanel(new BorderLayout());

        // --- SIDE NAVIGATION PANEL ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBackground(new Color(45, 52, 54)); // Dark professional theme
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));

        // --- BUTTON DEFINITIONS ---
        // To add a NEW BUTTON: 1. Declare it here. 2. Add it to leftPanel. 3. Add listener at bottom.
        JButton btnDeduct    = createNavButton("Deductions");
        JButton btnComp      = createNavButton("Compensations");
        JButton btnAllow     = createNavButton("Allowances");
        JButton btnMove      = createNavButton("Movements");
        JButton btnServCharge      = createNavButton("Service Charge");
        JButton btnInfo      = createNavButton("Information");
        JButton btnExit      = createNavButton("Logout");

        // Styling the Headers
        leftPanel.add(btnInfo);
        leftPanel.add(btnMove);
        leftPanel.add(btnDeduct);
        leftPanel.add(btnComp);
        leftPanel.add(btnServCharge);
        leftPanel.add(btnAllow);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(btnExit);
        leftPanel.add(Box.createVerticalStrut(10));

        // --- CARD PANEL SETUP (The "Pages") ---
        // To add a NEW PAGE: 1. Create a method for it. 2. Add it here with a name.
        cardPanel.add(createGenericPage("Profile Information", new Color(255, 235, 235)), "PAGE_INFO");
        cardPanel.add(createGenericPage("Deductions Management", new Color(255, 235, 235)), "PAGE_DEDUCT");
        cardPanel.add(createGenericPage("Compensations & Bonuses", new Color(235, 255, 235)), "PAGE_COMP");
        cardPanel.add(createGenericPage("Allowances & Perks", new Color(235, 245, 255)), "PAGE_ALLOW");
        cardPanel.add(createGenericPage("Employee Movements / Transfers", Color.WHITE), "PAGE_MOVE");
        cardPanel.add(createGenericPage("Service Charge", new Color(255, 235, 235)), "PAGE_SERVECHARGE");

        // --- NAVIGATION LISTENERS ---
        // This logic connects the button click to the card swap
        btnDeduct.addActionListener(e -> cardLayout.show(cardPanel, "PAGE_DEDUCT"));
        btnComp.addActionListener(e -> cardLayout.show(cardPanel, "PAGE_COMP"));
        btnAllow.addActionListener(e -> cardLayout.show(cardPanel, "PAGE_ALLOW"));
        btnMove.addActionListener(e -> cardLayout.show(cardPanel, "PAGE_MOVE"));
        btnExit.addActionListener(e -> System.exit(0));

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // --- PAGE CREATION METHODS ---

    // Your main payroll form
    private static JPanel createCalculationPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Payroll Calculation Form Goes Here", SwingConstants.CENTER));
        // You would paste your previous form code here
        return p;
    }

    // MODIFICATION TIP: Copy this method and rename it to create a specific page (e.g., createDeductionPage)
    // Then you can add text fields and tables specific to that category.
    private static JPanel createGenericPage(String title, Color bgColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bgColor);
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    // --- UI HELPERS ---

    private static void addNavHeader(JPanel panel, String text) {
        panel.add(Box.createVerticalStrut(20));
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(150, 150, 150));
        panel.add(l);
        panel.add(Box.createVerticalStrut(5));
    }

    private static JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(200, 40));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setBackground(new Color(45, 52, 54));
        b.setForeground(Color.WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Simple hover effect can be added here with a MouseListener if desired
        return b;
    }
}