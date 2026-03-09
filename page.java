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
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(btnInfo);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnMove);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnDeduct);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnComp);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnServCharge);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(btnAllow);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(btnExit);
        leftPanel.add(Box.createVerticalStrut(10));

        // --- CARD PANEL SETUP (The "Pages") ---
        // To add a NEW PAGE: 1. Create a method for it. 2. Add it here with a name.
        cardPanel.add(ProfilePage(), "PAGE_INFO");
        cardPanel.add(DeductionPage(), "PAGE_DEDUCT");
        cardPanel.add(CompPage(), "PAGE_COMP");
        cardPanel.add(createGenericPage("Allowances & Perks", new Color(235, 245, 255)), "PAGE_ALLOW");
        cardPanel.add(createGenericPage("Employee Movements / Transfers", Color.WHITE), "PAGE_MOVE");
        cardPanel.add(createGenericPage("Service Charge", new Color(255, 235, 235)), "PAGE_SERVECHARGE");

        // --- NAVIGATION LISTENERS ---
        // This logic connects the button click to the card swap
        btnInfo.addActionListener(e -> cardLayout.show(cardPanel, "PAGE_INFO"));
        btnServCharge.addActionListener(e -> cardLayout.show(cardPanel, "PAGE_SERVECHARGE"));
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

    // MODIFICATION TIP: Copy this method and rename it to create a specific page (e.g., DeductionPage)
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

        // --- 1. SETTING THE PADDING (The Border) ---
        // The first 10, 10 are Top/Bottom padding. The 20, 20 are Left/Right padding.
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // --- 2. BASIC STYLING ---
        b.setMaximumSize(new Dimension(200, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Define our colors
        Color idleColor = new Color(45, 52, 54);    // Original Dark
        Color hoverColor = new Color(63, 72, 75);   // Slightly Lighter for Hover

        b.setBackground(idleColor);
        b.setForeground(Color.BLACK);

        // --- 3. THE HOVER EFFECT (MouseListener) ---
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(hoverColor); // Change to lighter color when mouse is over
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(idleColor);  // Change back to original when mouse leaves
            }
        });

        return b;
    }

    // --- UI HELPERS ---

    /**
     * Creates a JPanel with a titled border and a GridBagLayout.
     * This is used to group related input fields together (e.g., "Personal Details").
     */
    private static JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Keeps the background color of the parent page

        // Create a professional titled border
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        border.setTitleColor(new Color(100, 100, 100));

        // Compound border adds some "breathing room" (10px padding) inside the group
        panel.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        return panel;
    }

    // Overloaded version to support JComboBox and other JComponents
    private static void addLabeledField(JPanel panel, String labelText, JComponent component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(component, gbc);
        gbc.gridy++;
    }

    private static JPanel ProfilePage() {
        JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainContainer.setBackground(Color.WHITE);

        // Title Section
        JLabel header = new JLabel("Employee Profile Management");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(new Color(45, 52, 54));
        mainContainer.add(header, BorderLayout.NORTH);

        // Form Section (Two Columns)
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        formPanel.setOpaque(false);

        // Left Column: Personal Data
        JPanel leftCol = createGroupPanel("Personal Details");
        addLabeledField(leftCol, "First Name:", new JTextField());
        addLabeledField(leftCol, "Last Name:", new JTextField());
        addLabeledField(leftCol, "Birth Date:", new JTextField("YYYY-MM-DD"));
        addLabeledField(leftCol, "Contact Number:", new JTextField());
        addLabeledField(leftCol, "Email Address:", new JTextField());

        // Right Column: Employment Data
        JPanel rightCol = createGroupPanel("Employment Details");
        addLabeledField(rightCol, "Employee ID:", new JTextField());
        addLabeledField(rightCol, "Department:", new JComboBox<>(new String[]{"Admin", "IT", "HR", "Sales", "Operations"}));
        addLabeledField(rightCol, "Job Title:", new JTextField());
        addLabeledField(rightCol, "Date Hired:", new JTextField("2026-03-10"));
        addLabeledField(rightCol, "Status:", new JComboBox<>(new String[]{"Regular", "Probationary", "Contractual"}));

        formPanel.add(leftCol);
        formPanel.add(rightCol);

        // Footer: Save Button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton btnSave = new JButton("Save Profile");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(0, 184, 148)); // Professional Green
        btnSave.setForeground(Color.BLACK);
        btnSave.setFocusPainted(false);

        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(btnSave, "Employee Profile Saved Successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        footer.add(btnSave);

        mainContainer.add(formPanel, BorderLayout.CENTER);
        mainContainer.add(footer, BorderLayout.SOUTH);

        return mainContainer;
    }

    private static JPanel DeductionPage() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        main.setBackground(new Color(250, 251, 252)); // Slightly off-white

        JLabel header = new JLabel("Deductions Management");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        main.add(header, BorderLayout.NORTH);

        // Form for adding a new deduction
        JPanel inputPanel = createGroupPanel("Fixed Monthly Deductions");
        addLabeledField(inputPanel, "Government Tax (%):", new JTextField());
        addLabeledField(inputPanel, "Health Insurance:", new JTextField());
        addLabeledField(inputPanel, "Social Security:", new JTextField());
        addLabeledField(inputPanel, "Other Loan Deductions:", new JTextField());

        JButton btnUpdate = new JButton("Update Deduction Rates");
        btnUpdate.setBackground(new Color(45, 52, 54));
        btnUpdate.setForeground(Color.WHITE);

        // Using a wrapper to keep the button from stretching
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnUpdate);
        inputPanel.add(btnWrapper); // GridBagLayout will handle this via the helper

        // Bottom Section: A Table to show history or current settings
        String[] cols = {"Type", "Amount/Rate", "Effective Date", "Status"};
        Object[][] data = {
                {"Income Tax", "12%", "2026-01-01", "Active"},
                {"Health Fund", "500.00", "2026-01-01", "Active"}
        };
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(data, cols));

        main.add(inputPanel, BorderLayout.CENTER);
        main.add(new JScrollPane(table), BorderLayout.SOUTH);

        return main;
    }
    private static JPanel CompPage() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        main.setBackground(Color.WHITE);

        JLabel header = new JLabel("Compensations & Bonuses");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        main.add(header, BorderLayout.NORTH);

        JPanel form = createGroupPanel("Salary Structure");
        addLabeledField(form, "Monthly Base Pay:", new JTextField());
        addLabeledField(form, "De Minimis Benefits:", new JTextField());
        addLabeledField(form, "Incentive Bonus:", new JTextField());
        addLabeledField(form, "13th Month Accrual:", new JTextField());

        // A nice big "Total" display
        JLabel lblTotal = new JLabel("Total Monthly Package: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotal.setForeground(new Color(0, 102, 204));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.CENTER);
        centerPanel.add(lblTotal, BorderLayout.SOUTH);

        main.add(centerPanel, BorderLayout.CENTER);
        return main;
    }

}