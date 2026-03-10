import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
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
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);

        // --- 1. TOP SECTION (Search/Filter) ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        JTextField txtSearch = new JTextField("Search employees...");
        JComboBox<String> cmbDept = new JComboBox<>(new String[]{"All Departments", "Admin", "IT", "HR", "Sales"});

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchControls.setOpaque(false);
        searchControls.add(new JLabel("Department:"));
        searchControls.add(cmbDept);
        searchControls.add(Box.createHorizontalStrut(20));
        searchControls.add(new JLabel("Search:"));
        txtSearch.setPreferredSize(new Dimension(250, 30));
        searchControls.add(txtSearch);
        topPanel.add(new JLabel("Employee Database Management"), BorderLayout.WEST);
        topPanel.add(searchControls, BorderLayout.EAST);

        // --- 2. LEFT SECTION: Individual Employee View + ACTIONS ---
        JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
        detailPanel.setPreferredSize(new Dimension(350, 0));
        detailPanel.setOpaque(false);

        // Toolbar
        JPanel actionToolbar = new JPanel(new GridLayout(1, 3, 5, 0));
        actionToolbar.setOpaque(false);
        JButton btnAdd = new JButton("Add New");
        JButton btnEdit = new JButton("Update");
        JButton btnClear = new JButton("Clear");

        // Styling
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
        btnEdit.setBackground(new Color(9, 132, 227)); btnEdit.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(225, 112, 85)); btnClear.setForeground(Color.WHITE);

        actionToolbar.add(btnAdd);
        actionToolbar.add(btnEdit);
        actionToolbar.add(btnClear);

        // Form Fields (Only declare this ONCE)
        JPanel formFields = createGroupPanel("Employee Details");
        JTextField fId = new JTextField();
        JTextField fName = new JTextField();
        JTextField fDept = new JTextField();
        JTextField fPos = new JTextField();
        JTextField fRole = new JTextField();

        addLabeledField(formFields, "ID:", fId);
        addLabeledField(formFields, "Name:", fName);
        addLabeledField(formFields, "Department:", fDept);
        addLabeledField(formFields, "Position:", fPos);
        addLabeledField(formFields, "Role:", fRole);

        detailPanel.add(actionToolbar, BorderLayout.NORTH);
        detailPanel.add(formFields, BorderLayout.CENTER);

        // --- 3. RIGHT SECTION: The Employee List ---
        String[] columns = {"ID", "Name", "Department", "Position", "Status"};
        Object[][] data = {
                {"EMP-001", "Jesse Ulbenario", "IT", "Lead", "Active"},
                {"EMP-002", "Jane Smith", "HR", "Manager", "Active"},
                {"EMP-003", "John Doe", "Sales", "Associate", "On Leave"}
        };
        DefaultTableModel tableModel = new DefaultTableModel(data, columns);
        JTable employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        // --- 4. THE MAGIC: LINKING TABLE TO FORM ---
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && employeeTable.getSelectedRow() != -1) {
                int row = employeeTable.getSelectedRow();
                // Convert index in case the table is sorted
                int modelRow = employeeTable.convertRowIndexToModel(row);

                fId.setText(tableModel.getValueAt(modelRow, 0).toString());
                fName.setText(tableModel.getValueAt(modelRow, 1).toString());
                fDept.setText(tableModel.getValueAt(modelRow, 2).toString());
                fPos.setText(tableModel.getValueAt(modelRow, 3).toString());
            }
        });

        // Clear Logic
        btnClear.addActionListener(e -> {
            fId.setText(""); fName.setText(""); fDept.setText(""); fPos.setText(""); fRole.setText("");
            employeeTable.clearSelection();
        });

        // Final Assembly
        main.add(topPanel, BorderLayout.NORTH);
        main.add(detailPanel, BorderLayout.WEST);
        main.add(scrollPane, BorderLayout.CENTER);

        return main;
    }

    private static JPanel DeductionPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        main.setBackground(new Color(245, 246, 247));

        // --- 1. NORTH: HEADER & CASH ADVANCE TOOLBAR ---
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);

        JLabel header = new JLabel("Deductions Management");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));

        // The "Add Advance" Button
        JButton btnAdvance = new JButton("+ Add Cash Advance");
        btnAdvance.setBackground(new Color(0, 184, 148)); // Green to signify adding funds/action
        btnAdvance.setForeground(Color.WHITE);
        btnAdvance.setFocusPainted(false);

        northPanel.add(header, BorderLayout.WEST);
        northPanel.add(btnAdvance, BorderLayout.EAST);

        // --- 2. CENTER: FIXED DEDUCTION FORM ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        JPanel inputPanel = createGroupPanel("Fixed Monthly Deductions");
        addLabeledField(inputPanel, "Government Tax (%):", new JTextField());
        addLabeledField(inputPanel, "Health Insurance:", new JTextField());
        addLabeledField(inputPanel, "Social Security:", new JTextField());
        addLabeledField(inputPanel, "Mortgage/Housing Loan:", new JTextField());
        addLabeledField(inputPanel, "Other Loan Deductions:", new JTextField());

        JButton btnUpdate = new JButton("Update Deduction Rates");
        btnUpdate.setBackground(new Color(4, 7, 14));
        btnUpdate.setForeground(Color.WHITE);

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnUpdate);
        inputPanel.add(btnWrapper);

        // History Table
        String[] cols = {"Type", "Amount/Rate", "Effective Date", "Status"};
        Object[][] data = {
                {"Income Tax", "12%", "2026-01-01", "Active"},
                {"Health Fund", "500.00", "2026-01-01", "Active"},
                {"Housing Loan", "2,500.00", "2026-02-15", "Active"}
        };
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(data, cols));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(0, 200));

        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        // --- 3. SOUTH: THE "EXCEL" STATUS BAR (TOTALS) ---
        JPanel summaryBar = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryBar.setBackground(new Color(45, 52, 54)); // Dark background like a status bar
        summaryBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Summary Labels
        summaryBar.add(createSummaryLabel("Gov't Total: $450.00"));
        summaryBar.add(createSummaryLabel("Loans/Mortgage: $2,500.00"));
        summaryBar.add(createSummaryLabel("Advances: $1,200.00"));
        summaryBar.add(createSummaryLabel("TOTAL DEDUCTIONS: $4,150.00", Color.YELLOW));

        // Assemble
        main.add(northPanel, BorderLayout.NORTH);
        main.add(centerPanel, BorderLayout.CENTER);
        main.add(summaryBar, BorderLayout.SOUTH);

        return main;
    }

    // Helper for the Status Bar Labels
    private static JLabel createSummaryLabel(String text) {
        return createSummaryLabel(text, Color.WHITE);
    }

    private static JLabel createSummaryLabel(String text, Color textColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(textColor);
        return label;
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