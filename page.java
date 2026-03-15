import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class page {
    private static CardLayout cardLayout = new CardLayout();
    private static JPanel cardPanel = new JPanel(cardLayout);
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    // Match LoginPage palette
    private static final Color SAGE = new Color(0xA4, 0xBB, 0x8E);
    private static final Color DARK_OLIVE = new Color(0x5E, 0x71, 0x4B);
    private static final Color GOLDEN = new Color(0xE8, 0xAB, 0x2F);
    private static final Color DARK_BROWN = new Color(0x58, 0x3C, 0x2A);
    private static final Color LIGHT_GREY = new Color(0xF5, 0xF5, 0xF5);
    private static final Color MEDIUM_GREY = new Color(0x88, 0x88, 0x88);
    /** Soft background for content area so panels stand out. */
    private static final Color CONTENT_BG = new Color(0xF2, 0xF3, 0xF5);
    /** Table header background for clear column labels. */
    private static final Color TABLE_HEADER_BG = new Color(0x5E, 0x71, 0x4B);
    /** All UI text: black or this dark grey for consistency. */
    private static final Color TEXT_DARK = new Color(0x2D, 0x2D, 0x2D);

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        // When run directly (not from LoginPage), use a default session so the app opens without login
        if (AppSession.getUsername() == null) {
            AppSession.setHRUser(1, "admin", "admin", 1);
        }
        SwingUtilities.invokeLater(page::createAndShowUI);
    }

    public static void createAndShowUI() {
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("TextField.foreground", TEXT_DARK);
        UIManager.put("ComboBox.foreground", TEXT_DARK);
        UIManager.put("Button.foreground", TEXT_DARK);
        UIManager.put("Table.foreground", Color.BLACK);
        UIManager.put("Table.selectionForeground", Color.BLACK);
        JFrame frame = new JFrame("Philippine Payroll – " + (AppSession.getUsername() != null ? AppSession.getUsername() : "HR"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1200, 800));

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBackground(DARK_OLIVE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, DARK_BROWN));

        // Logo at top left (same as LoginPage)
        JLabel logoLabel = new JLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ImageIcon logoIcon = loadLogo("elements/RanchoLogo.jpg", 180);
        if (logoIcon != null) logoLabel.setIcon(logoIcon);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalStrut(12));

        String[] nav = AppSession.isAdmin()
            ? new String[]{ "Attendance", "Deductions", "Compensation", "Reports", "Payroll Periods", "Offset" }
            : new String[]{ "Attendance", "Deductions", "Compensation", "Reports", "Offset" };
        JButton[] buttons = new JButton[nav.length + 1];
        for (int i = 0; i < nav.length; i++) {
            buttons[i] = createNavButton(nav[i]);
            leftPanel.add(Box.createVerticalStrut(i == 0 ? 15 : 8));
            leftPanel.add(buttons[i]);
        }
        leftPanel.add(Box.createVerticalGlue());
        buttons[nav.length] = createNavButton("Logout");
        leftPanel.add(buttons[nav.length]);
        leftPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(buildEmployeeHubPage(), "PAGE_EMPLOYEE");
        cardPanel.add(buildDeductionsHubPage(), "PAGE_DEDUCTIONS");
        cardPanel.add(buildCompensationHubPage(), "PAGE_COMP");
        cardPanel.add(buildReportsPage(), "PAGE_REPORTS");
        cardPanel.add(buildPayrollPeriodsPage(), "PAGE_PAYROLL_PERIODS");
        cardPanel.add(buildOffsetPage(), "PAGE_OFFSET");

        buttons[0].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_EMPLOYEE"));
        buttons[1].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_DEDUCTIONS"));
        buttons[2].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_COMP"));
        buttons[3].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_REPORTS"));
        if (AppSession.isAdmin()) {
            buttons[4].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_PAYROLL_PERIODS"));
            buttons[5].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_OFFSET"));
            buttons[6].addActionListener(e -> { AppSession.clear(); System.exit(0); });
        } else {
            buttons[4].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_OFFSET"));
            buttons[5].addActionListener(e -> { AppSession.clear(); System.exit(0); });
        }

        cardPanel.setBackground(CONTENT_BG);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    /** Loads and scales the sidebar logo (elements/RanchoLogo.jpg). Returns null if file missing or unreadable. */
    private static ImageIcon loadLogo(String path, int maxWidth) {
        File file = new File(path);
        if (!file.exists()) return null;
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage();
        if (img.getWidth(null) <= 0) return null;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int newH = (maxWidth * h) / w;
        Image scaled = img.getScaledInstance(maxWidth, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setMaximumSize(new Dimension(200, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        boolean isLogout = "Logout".equals(text);
        if (isLogout) {
            b.setBackground(new Color(0x6B, 0x5B, 0x50));
            b.setForeground(Color.WHITE);
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(new Color(0x7A, 0x68, 0x5C)); }
                public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(new Color(0x6B, 0x5B, 0x50)); }
            });
        } else {
            b.setBackground(SAGE);
            b.setForeground(TEXT_DARK);
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(SAGE.brighter()); }
                public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(SAGE); }
            });
        }
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Pure black for table cell text so it always appears black. */
    private static final Color CELL_BLACK = new Color(0, 0, 0);

    /** Makes tables clearer: header styling, row height, light grid, and cell text that appears black. */
    private static void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setPreferredSize(new Dimension(0, 32));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        table.setForeground(CELL_BLACK);
        table.setSelectionBackground(new Color(0xE8, 0xED, 0xE0));
        table.setSelectionForeground(CELL_BLACK);
        DefaultTableCellRenderer blackRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focused, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, selected, focused, row, column);
                l.setForeground(CELL_BLACK);
                l.setOpaque(true);
                if (selected) l.setBackground(new Color(0xE8, 0xED, 0xE0));
                else l.setBackground((row % 2 == 0) ? Color.WHITE : new Color(0xF5, 0xF5, 0xF5));
                return l;
            }
        };
        table.setDefaultRenderer(Object.class, blackRenderer);
        table.setDefaultRenderer(String.class, blackRenderer);
        table.setDefaultRenderer(Number.class, blackRenderer);
    }

    private static JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 13));
        border.setTitleColor(TEXT_DARK);
        border.setBorder(BorderFactory.createLineBorder(SAGE, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return panel;
    }

    private static void addLabeledField(JPanel panel, String labelText, JComponent comp) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(comp, gbc);
        gbc.gridy++;
    }

    /** Like addLabeledField but reuses gbc so rows stack (gridy advances). */
    private static void addLabeledField(JPanel panel, String labelText, JComponent comp, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(comp, gbc);
        gbc.gridy++;
    }

    private static void refreshTable(DefaultTableModel model, String[] columns, List<?> rows, RowMapper mapper) {
        model.setRowCount(0);
        model.setColumnIdentifiers(columns);
        for (Object row : rows) model.addRow(mapper.toRow(row));
    }

    private interface RowMapper { Object[] toRow(Object o); }

    /**
     * Opens the Full Employee Information dialog for the given employee.
     * Shows read-only fields (ID, code, name, legal/SSS) and editable fields (dept, position, pay, active).
     * Save button updates only the editable fields. Add/Register new employee is a placeholder for the next developer.
     *
     * @param parent       parent frame for the dialog
     * @param employeeId   selected employee ID (from table)
     * @param onSaved      optional callback to refresh the employee list after save (can be null)
     */
    /** Add new employee with role: form then EmployeeDao.insert + EmployeeRoleDao.insert. onAdded called after success to refresh list. */
    private static void showAddNewEmployeePlaceholder(Component parent, Runnable onAdded) {
        JDialog dlg = new JDialog(SwingUtilities.windowForComponent(parent), "Add New Employee", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(Color.WHITE);
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 8, 4, 8);
        g.gridy = 0;
        g.weightx = 0;

        JTextField fCode = new JTextField(20);
        JTextField fName = new JTextField(24);
        JTextField fBasic = new JTextField(12);
        JTextField fDaily = new JTextField(12);
        JTextField fHourly = new JTextField(12);
        JTextField fBank = new JTextField(20);
        JComboBox<String> fDept = new JComboBox<>();
        JTextField fRoleType = new JTextField(20);
        fRoleType.setText("Staff");
        JCheckBox fActive = new JCheckBox("Active", true);

        addLabeledField(center, "Employee code:", fCode, g);
        addLabeledField(center, "Full name:", fName, g);
        addLabeledField(center, "Basic salary:", fBasic, g);
        addLabeledField(center, "Daily rate:", fDaily, g);
        addLabeledField(center, "Hourly rate:", fHourly, g);
        addLabeledField(center, "Bank account:", fBank, g);
        addLabeledField(center, "Department:", fDept, g);
        addLabeledField(center, "Role type:", fRoleType, g);
        g.gridx = 0; g.gridwidth = 2; center.add(fActive, g);

        try {
            fDept.addItem("");
            for (DepartmentDao.Department d : DepartmentDao.findAll()) fDept.addItem(d.departmentName);
        } catch (SQLException ex) { /* ignore */ }

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(SAGE);
        btnAdd.setForeground(TEXT_DARK);
        JButton btnCancel = new JButton("Cancel");
        btnAdd.addActionListener(e -> {
            String code = fCode.getText().trim();
            String name = fName.getText().trim();
            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Employee code and full name are required.");
                return;
            }
            String deptName = (String) fDept.getSelectedItem();
            if (deptName == null || deptName.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please select a department.");
                return;
            }
            try {
                BigDecimal basic = fBasic.getText().trim().isEmpty() ? null : new BigDecimal(fBasic.getText().trim());
                BigDecimal daily = fDaily.getText().trim().isEmpty() ? null : new BigDecimal(fDaily.getText().trim());
                BigDecimal hourly = fHourly.getText().trim().isEmpty() ? null : new BigDecimal(fHourly.getText().trim());
                int newId = EmployeeDao.insert(code, name, basic, daily, hourly, fBank.getText().trim(), fActive.isSelected());
                int deptId = -1;
                for (DepartmentDao.Department d : DepartmentDao.findAll()) {
                    if (deptName.equals(d.departmentName)) { deptId = d.departmentId; break; }
                }
                if (deptId >= 0) EmployeeRoleDao.insert(newId, deptId, fRoleType.getText().trim());
                JOptionPane.showMessageDialog(dlg, "Employee added (ID " + newId + ").");
                dlg.dispose();
                if (onAdded != null) onAdded.run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, "Save failed: " + ex.getMessage());
            }
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        south.add(btnAdd);
        south.add(btnCancel);
        dlg.add(new JScrollPane(center), BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    /** Configure government/statutory deduction rates (SSS, PhilHealth, Pag-IBIG) and apply Pag-IBIG for period. */
    private static void showStatutoryRatesDialog(Window parent) {
        if (parent == null) return;
        JDialog dlg = new JDialog(parent, "Configure Statutory Rates", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(15, 15));
        dlg.getContentPane().setBackground(Color.WHITE);
        dlg.setSize(480, 420);

        JPanel form = createGroupPanel("Rates (SSS, PhilHealth, Pag-IBIG)");
        JTextField fPagibigEmp = new JTextField(10);
        JTextField fPagibigEr = new JTextField(10);
        JTextField fPhilHealth = new JTextField(10);
        JTextField fSssNote = new JTextField(30);
        addLabeledField(form, "Pag-IBIG employee share (fixed, e.g. 200):", fPagibigEmp);
        addLabeledField(form, "Pag-IBIG employer share (fixed, e.g. 200):", fPagibigEr);
        addLabeledField(form, "PhilHealth % of basic (e.g. 5):", fPhilHealth);
        addLabeledField(form, "SSS note (bracket table / manual):", fSssNote);
        JButton btnSave = new JButton("Save rates");
        btnSave.setBackground(SAGE);
        btnSave.setForeground(TEXT_DARK);
        form.add(btnSave);

        Runnable loadRates = () -> {
            try {
                java.math.BigDecimal pe = StatutoryDao.getDecimal("pagibig_employee_share");
                java.math.BigDecimal pr = StatutoryDao.getDecimal("pagibig_employer_share");
                java.math.BigDecimal ph = StatutoryDao.getDecimal("philhealth_rate_pct");
                String ss = StatutoryDao.getText("sss_note");
                fPagibigEmp.setText(pe != null ? pe.toPlainString() : "200");
                fPagibigEr.setText(pr != null ? pr.toPlainString() : "200");
                fPhilHealth.setText(ph != null ? ph.toPlainString() : "5");
                fSssNote.setText(ss != null ? ss : "");
            } catch (SQLException ex) { /* ignore */ }
        };
        loadRates.run();
        btnSave.addActionListener(e -> {
            try {
                StatutoryDao.setDecimal("pagibig_employee_share", new BigDecimal(fPagibigEmp.getText().trim()));
                StatutoryDao.setDecimal("pagibig_employer_share", new BigDecimal(fPagibigEr.getText().trim()));
                StatutoryDao.setDecimal("philhealth_rate_pct", new BigDecimal(fPhilHealth.getText().trim()));
                StatutoryDao.setText("sss_note", fSssNote.getText().trim());
                JOptionPane.showMessageDialog(dlg, "Rates saved.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Save failed."); }
        });

        JPanel applyPanel = createGroupPanel("Apply for period");
        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnApplyPagibig = new JButton("Add Pag-IBIG deduction for all employees (period)");
        btnApplyPagibig.setBackground(GOLDEN);
        btnApplyPagibig.setForeground(TEXT_DARK);
        try {
            cmbPeriod.addItem(null);
            for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
        } catch (SQLException ex) { /* ignore */ }
        addLabeledField(applyPanel, "Payroll period:", cmbPeriod);
        applyPanel.add(btnApplyPagibig);
        btnApplyPagibig.addActionListener(e -> {
            if (!(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                JOptionPane.showMessageDialog(dlg, "Select a payroll period."); return;
            }
            int periodId = ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId;
            try {
                java.math.BigDecimal amt = StatutoryDao.getDecimal("pagibig_employee_share");
                if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Set Pag-IBIG employee share first."); return;
                }
                int count = 0;
                for (EmployeeDao.EmployeeRow emp : EmployeeDao.findAll()) {
                    if (!emp.isActive) continue;
                    DeductionDao.insert(emp.employeeId, periodId, "Pag-IBIG", amt, "Statutory (fixed)", "active", AppSession.getHrUserId());
                    count++;
                }
                JOptionPane.showMessageDialog(dlg, "Added Pag-IBIG deduction for " + count + " employees.");
            } catch (SQLException ex) { JOptionPane.showMessageDialog(dlg, "Failed: " + ex.getMessage()); }
        });

        JPanel north = new JPanel(new BorderLayout(10, 10));
        north.setOpaque(false);
        north.add(form, BorderLayout.NORTH);
        north.add(applyPanel, BorderLayout.CENTER);
        dlg.add(north, BorderLayout.CENTER);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private static void showFullEmployeeInfoDialog(Window parent, int employeeId, Runnable onSaved) {
        JDialog dlg = new JDialog(parent, "Full Employee Information", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(Color.WHITE);
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 8, 4, 8);
        g.gridy = 0;
        g.weightx = 0;

        // ----- Read-only: do not allow editing name, code, or legal identifiers (e.g. SSS). -----
        JTextField fId = new JTextField(14);
        fId.setEditable(false);
        JTextField fCode = new JTextField(14);
        fCode.setEditable(false);
        JTextField fName = new JTextField(14);
        fName.setEditable(false);
        // TODO (next developer): If SSS number or other legal IDs are added to schema, show here as read-only.
        JTextField fSss = new JTextField(14);
        fSss.setEditable(false);
        fSss.setText("—"); // Placeholder until SSS/legal ID is in DB.

        addLabeledField(center, "ID:", fId, g);
        addLabeledField(center, "Code:", fCode, g);
        addLabeledField(center, "Name:", fName, g);
        addLabeledField(center, "SSS / Legal ID:", fSss, g);

        // ----- Editable: only these fields should be updated by Save. -----
        JComboBox<String> fDept = new JComboBox<>();
        JComboBox<PositionDao.Position> fPos = new JComboBox<>();
        JTextField fBasic = new JTextField(14);
        JTextField fDaily = new JTextField(14);
        JTextField fHourly = new JTextField(14);
        JTextField fBank = new JTextField(20);
        fBank.setToolTipText("Bank account for payroll funding / direct deposit");
        JCheckBox fActive = new JCheckBox("Active", true);
        addLabeledField(center, "Department:", fDept, g);
        addLabeledField(center, "Position:", fPos, g);
        addLabeledField(center, "Basic salary:", fBasic, g);
        addLabeledField(center, "Daily rate:", fDaily, g);
        addLabeledField(center, "Hourly rate:", fHourly, g);
        addLabeledField(center, "Bank account:", fBank, g);
        g.gridx = 0;
        g.gridwidth = 2;
        g.weightx = 0;
        center.add(fActive, g);

        // Load depts and positions for combos
        try {
            fDept.addItem("");
            for (DepartmentDao.Department d : DepartmentDao.findAll()) fDept.addItem(d.departmentName);
            fPos.addItem(null);
            for (PositionDao.Position p : PositionDao.findAll()) fPos.addItem(p);
        } catch (SQLException ex) { /* DB unavailable */ }

        // Load current employee data
        try {
            EmployeeDao.Employee emp = EmployeeDao.findById(employeeId);
            if (emp != null) {
                fId.setText(String.valueOf(emp.employeeId));
                fCode.setText(emp.employeeCode);
                fName.setText(emp.fullName);
                fBasic.setText(emp.basicSalary != null ? emp.basicSalary.toPlainString() : "");
                fDaily.setText(emp.dailyRate != null ? emp.dailyRate.toPlainString() : "");
                fHourly.setText(emp.hourlyRate != null ? emp.hourlyRate.toPlainString() : "");
                fBank.setText(emp.bankAccount != null ? emp.bankAccount : "");
                fActive.setSelected(emp.isActive);
            }
            EmployeeRoleDao.EmployeeRoleInfo role = EmployeeRoleDao.getActiveRole(employeeId);
            if (role != null && role.departmentName != null) fDept.setSelectedItem(role.departmentName);
            if (role != null) {
                Integer posId = RegUserDao.getPositionIdForRole(role.employeeRoleId);
                if (posId != null) {
                    for (int i = 0; i < fPos.getItemCount(); i++) {
                        PositionDao.Position p = fPos.getItemAt(i);
                        if (p != null && p.positionId == posId) { fPos.setSelectedIndex(i); break; }
                    }
                }
            }
        } catch (SQLException ex) { /* DB unavailable */ }

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        south.setBackground(Color.WHITE);
        JButton btnSave = new JButton("Save");
        btnSave.setBackground(new Color(9, 132, 227));
        btnSave.setForeground(TEXT_DARK);
        JButton btnClose = new JButton("Close");
        // Add/Register new employee: button only; implementation left for next developer.
        JButton btnAddNew = new JButton("Add / Register new employee");
        btnAddNew.setBackground(GOLDEN);
        btnAddNew.setForeground(TEXT_DARK);
        btnAddNew.setToolTipText("Not yet implemented. Add logic to insert new employee and optionally register as user.");

        btnSave.addActionListener(e -> {
            try {
                BigDecimal basic = fBasic.getText().trim().isEmpty() ? null : new BigDecimal(fBasic.getText().trim());
                BigDecimal daily = fDaily.getText().trim().isEmpty() ? null : new BigDecimal(fDaily.getText().trim());
                BigDecimal hourly = fHourly.getText().trim().isEmpty() ? null : new BigDecimal(fHourly.getText().trim());
                EmployeeDao.update(employeeId, fCode.getText(), fName.getText(), basic, daily, hourly, fBank.getText().trim(), fActive.isSelected());
                EmployeeRoleDao.EmployeeRoleInfo role = EmployeeRoleDao.getActiveRole(employeeId);
                if (role != null) {
                    String deptName = (String) fDept.getSelectedItem();
                    if (deptName != null && !deptName.isEmpty()) {
                        int deptId = -1;
                        for (DepartmentDao.Department d : DepartmentDao.findAll()) {
                            if (deptName.equals(d.departmentName)) { deptId = d.departmentId; break; }
                        }
                        if (deptId >= 0) EmployeeRoleDao.ensureActiveRole(employeeId, deptId, role.roleType != null ? role.roleType : "hr");
                    }
                    PositionDao.Position pos = (PositionDao.Position) fPos.getSelectedItem();
                    RegUserDao.upsertPositionForRole(role.employeeRoleId, pos != null ? pos.positionId : null);
                }
                if (onSaved != null) onSaved.run();
                JOptionPane.showMessageDialog(dlg, "Saved.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Save failed: " + ex.getMessage());
            }
        });
        btnClose.addActionListener(e -> dlg.dispose());
        // TODO (next developer): Implement Add/Register new employee — insert into Employee (and optionally HRUser).
        btnAddNew.addActionListener(e -> JOptionPane.showMessageDialog(dlg, "Add/Register new employee is not yet implemented. Please add logic to insert a new employee and optionally register as system user."));

        south.add(btnAddNew);
        south.add(btnSave);
        south.add(btnClose);
        dlg.add(new JScrollPane(center), BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    // ===================== HUB PAGES (requested buttons) =====================
    private static JPanel buildEmployeeHubPage() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel header = new JLabel("Attendance", SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(TEXT_DARK);
        top.add(header, BorderLayout.WEST);

        JLabel banner = new JLabel("Logged in as: " + (AppSession.getUsername() != null ? AppSession.getUsername() : "") +
            " | Role: " + (AppSession.getHrRole() != null ? AppSession.getHrRole() : "") +
            " | Employee ID: " + (AppSession.getEmployeeId() != null ? AppSession.getEmployeeId() : "N/A"));
        banner.setForeground(TEXT_DARK);
        top.add(banner, BorderLayout.EAST);
        main.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.add(AppSession.isAdmin() ? buildEmployeesPage() : buildMyEmployeePage(), BorderLayout.CENTER);
        center.add(buildEmployeeDTRPanel(), BorderLayout.SOUTH);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    private static JPanel buildMyEmployeePage() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setOpaque(false);
        JPanel form = createGroupPanel("My Information");
        JTextField fId = new JTextField();
        JTextField fCode = new JTextField();
        JTextField fName = new JTextField();
        JTextField fBasic = new JTextField();
        JTextField fStatus = new JTextField();
        fId.setEditable(false); fCode.setEditable(false); fName.setEditable(false); fBasic.setEditable(false); fStatus.setEditable(false);
        addLabeledField(form, "Employee ID:", fId);
        addLabeledField(form, "Employee code:", fCode);
        addLabeledField(form, "Full name:", fName);
        addLabeledField(form, "Basic salary:", fBasic);
        addLabeledField(form, "Status:", fStatus);
        try {
            if (AppSession.getEmployeeId() != null) {
                EmployeeDao.Employee e = EmployeeDao.findById(AppSession.getEmployeeId());
                if (e != null) {
                    fId.setText(String.valueOf(e.employeeId));
                    fCode.setText(e.employeeCode);
                    fName.setText(e.fullName);
                    fBasic.setText(e.basicSalary != null ? MONEY.format(e.basicSalary) : "");
                    fStatus.setText(e.isActive ? "Active" : "Inactive");
                }
            }
        } catch (SQLException ex) {
            /* database unavailable */
        }
        p.add(form, BorderLayout.NORTH);
        return p;
    }

    private static JPanel buildEmployeeDTRPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("DTR (Daily Time Record)"));

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JTextField txtEmployeeId = new JTextField(8);
        JButton btnLoad = new JButton("Load DTR");

        Integer sessionEmpId = AppSession.getEmployeeId();
        txtEmployeeId.setText(sessionEmpId != null ? String.valueOf(sessionEmpId) : "");
        txtEmployeeId.setEditable(AppSession.isAdmin());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(new JLabel("Employee ID:"));
        top.add(txtEmployeeId);
        top.add(btnLoad);

        String[] cols = {"DTR ID", "Date", "Time In", "Time Out", "Regular", "OT", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 220));

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable */ }
        };
        Runnable loadDtr = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                    ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                Integer empId = txtEmployeeId.getText().trim().isEmpty() ? null : Integer.parseInt(txtEmployeeId.getText().trim());
                if (!AppSession.isAdmin()) empId = AppSession.getEmployeeId();
                if (empId == null) { JOptionPane.showMessageDialog(panel, "No employee selected."); return; }
                List<DTRDao.DTRRow> rows = DTRDao.findByPeriodAndEmployee(periodId, empId);
                model.setRowCount(0);
                for (DTRDao.DTRRow r : rows) {
                    model.addRow(new Object[]{ r.dtrId, r.dateVal, r.timeIn, r.timeOut, r.regularHours, r.overtimeHours, r.status });
                }
            } catch (Exception ex) { /* database unavailable */ }
        };

        loadPeriods.run();
        loadDtr.run();
        btnLoad.addActionListener(e -> loadDtr.run());
        cmbPeriod.addActionListener(e -> loadDtr.run());

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel buildDeductionsHubPage() { return buildDeductionsPage(); }
    private static JPanel buildCompensationHubPage() { return buildCompensationPage(); }

    // --- Employees (Core: Employee + Department from EmployeeRole) ---
    // Employee button: opens a menu with (1) Full employee information, (2) Add new employee, (3) Export DTR. See action bar and showAddNewEmployeePlaceholder / showFullEmployeeInfoDialog for extension points.
    private static JPanel buildEmployeesPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);

        JTextField txtSearch = new JTextField(25);
        JComboBox<String> cmbDept = new JComboBox<>(new String[]{"All Departments"});
        JButton btnRefresh = new JButton("Refresh");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Department:"));
        top.add(cmbDept);
        top.add(new JLabel("Search:"));
        top.add(txtSearch);
        top.add(btnRefresh);
        main.add(new JLabel("Employees", SwingConstants.LEFT) {{
            setFont(new Font("SansSerif", Font.BOLD, 22));
        }}, BorderLayout.NORTH);
        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.setOpaque(false);
        topWrap.add(top, BorderLayout.EAST);
        main.add(topWrap, BorderLayout.NORTH);

        // ----- Employee attendance panel: format matches DTR (Date YYYY-MM-DD, Status). Confirm button saves the change. -----
        JPanel detail = new JPanel(new BorderLayout());
        detail.setPreferredSize(new Dimension(300, 0));
        detail.setOpaque(false);
        JPanel attPanel = createGroupPanel("Employee");
        JLabel lblSelected = new JLabel("Select an employee from the table.");
        lblSelected.setForeground(TEXT_DARK);
        JTextField fAttendanceDate = new JTextField(10);
        fAttendanceDate.setText(java.time.LocalDate.now().toString());
        JComboBox<String> fAttendance = new JComboBox<>(new String[]{"Active", "Inactive", "Late"});
        fAttendance.setSelectedItem("Active");
        GridBagConstraints gbcAtt = new GridBagConstraints();
        gbcAtt.fill = GridBagConstraints.HORIZONTAL;
        gbcAtt.insets = new Insets(6, 5, 6, 5);
        gbcAtt.gridy = 0;
        gbcAtt.weightx = 0;
        gbcAtt.gridwidth = 2;
        gbcAtt.gridx = 0;
        attPanel.add(lblSelected, gbcAtt);
        gbcAtt.gridy++;
        gbcAtt.gridwidth = 1;
        addLabeledField(attPanel, "Date (YYYY-MM-DD):", fAttendanceDate, gbcAtt);
        addLabeledField(attPanel, "Status:", fAttendance, gbcAtt);
        JButton btnConfirmAttendance = new JButton("Confirm");
        btnConfirmAttendance.setToolTipText("Confirm attendance status for the selected employee and date.");
        btnConfirmAttendance.setBackground(GOLDEN);
        btnConfirmAttendance.setForeground(TEXT_DARK);
        gbcAtt.gridy++;
        gbcAtt.gridwidth = 2;
        attPanel.add(btnConfirmAttendance, gbcAtt);

        // Hold selected employee id/name for attendance and export (updated when table selection changes).
        final int[] selectedEmployeeId = { -1 };
        final String[] selectedEmployeeName = { "" };

        // ----- Employee action bar: these buttons expose the main employee functions for the next developer. -----
        // • Full employee information: view/edit selected employee (department, position, salary, active). Opens dialog with Save and Add/Register.
        // • Add new employee: placeholder for implementing employee registration/insert (see handler below).
        // • Export DTR: export selected employee's DTR to CSV.
        JPanel actBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        actBar.setOpaque(false);
        JButton btnEmployee = new JButton("Attendance");
        btnEmployee.setBackground(DARK_OLIVE);
        btnEmployee.setForeground(TEXT_DARK);
        JButton btnFullInfo = new JButton("Full employee information");
        JButton btnAddNewEmployee = new JButton("Add new employee");
        JButton btnExportDTR = new JButton("Export DTR");
        btnFullInfo.setBackground(new Color(9, 132, 227));
        btnFullInfo.setForeground(TEXT_DARK);
        btnAddNewEmployee.setBackground(SAGE);
        btnAddNewEmployee.setForeground(TEXT_DARK);
        btnExportDTR.setBackground(GOLDEN);
        btnExportDTR.setForeground(TEXT_DARK);
        actBar.add(btnEmployee);
        actBar.add(btnFullInfo);
        actBar.add(btnAddNewEmployee);
        actBar.add(btnExportDTR);
        detail.add(actBar, BorderLayout.NORTH);
        detail.add(attPanel, BorderLayout.CENTER);

        String[] cols = {"Code", "Name", "Department", "Basic", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        final AtomicReference<List<EmployeeDao.EmployeeRow>> currentEmployeeRows = new AtomicReference<>();

        Runnable loadDepts = () -> {
            try {
                cmbDept.removeAllItems();
                cmbDept.addItem("All Departments");
                for (DepartmentDao.Department d : DepartmentDao.findAll())
                    cmbDept.addItem(d.departmentName);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadEmployees = () -> {
            try {
                String dept = (String) cmbDept.getSelectedItem();
                String search = txtSearch.getText();
                List<EmployeeDao.EmployeeRow> rows = EmployeeDao.findAllWithDepartment(dept, search);
                currentEmployeeRows.set(rows);
                refreshTable(tblModel, cols, rows, o -> {
                    EmployeeDao.EmployeeRow r = (EmployeeDao.EmployeeRow) o;
                    return new Object[]{ r.employeeCode, r.fullName, r.departmentName != null ? r.departmentName : "", r.basicSalary != null ? MONEY.format(r.basicSalary) : "", r.isActive ? "Active" : "Inactive" };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadDepts.run();
        loadEmployees.run();

        // When user selects a row: update selected employee id/name, label, and attendance for that date.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            Integer id = null;
            String name = "";
            List<EmployeeDao.EmployeeRow> list = currentEmployeeRows.get();
            if (list != null && row >= 0 && row < list.size()) {
                EmployeeDao.EmployeeRow r = list.get(row);
                id = r.employeeId;
                name = r.fullName != null ? r.fullName : "";
            }
            if (id == null) return;
            selectedEmployeeId[0] = id;
            selectedEmployeeName[0] = name;
            lblSelected.setText("Selected: " + (name.isEmpty() ? "ID " + id : name));
            try {
                java.sql.Date attDate = java.sql.Date.valueOf(fAttendanceDate.getText().trim());
                String status = DTRDao.getStatusForDate(id, attDate);
                if (status != null && (status.equals("Active") || status.equals("Inactive") || status.equals("Late")))
                    fAttendance.setSelectedItem(status);
                else
                    fAttendance.setSelectedItem("Active");
            } catch (Exception ignored) {
                fAttendance.setSelectedItem("Active");
            }
        });

        // Confirm button and dropdown change: save attendance status for selected employee and date.
        Runnable saveAttendanceStatus = () -> {
            if (selectedEmployeeId[0] < 0) {
                JOptionPane.showMessageDialog(main, "Select an employee from the table first.");
                return;
            }
            try {
                java.sql.Date attDate = java.sql.Date.valueOf(fAttendanceDate.getText().trim());
                String status = (String) fAttendance.getSelectedItem();
                if (status == null) status = "Active";
                if (!status.equals("Active") && !status.equals("Inactive") && !status.equals("Late")) status = "Active";
                DTRDao.setAttendanceStatus(selectedEmployeeId[0], attDate, status);
                JOptionPane.showMessageDialog(main, "Attendance status confirmed.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(main, "Save failed. Check date format (YYYY-MM-DD).");
            }
        };
        btnConfirmAttendance.addActionListener(e -> saveAttendanceStatus.run());
        fAttendance.addActionListener(ev -> saveAttendanceStatus.run());

        // "Employee" button: shows a menu of the main employee functions (for discoverability).
        JPopupMenu employeeMenu = new JPopupMenu();
        JMenuItem miFullInfo = new JMenuItem("Full employee information");
        JMenuItem miAddNew = new JMenuItem("Add new employee");
        JMenuItem miExportDTR = new JMenuItem("Export DTR");
        employeeMenu.add(miFullInfo);
        employeeMenu.add(miAddNew);
        employeeMenu.add(miExportDTR);
        btnEmployee.addActionListener(e -> employeeMenu.show(btnEmployee, 0, btnEmployee.getHeight()));

        // Full employee information: opens dialog with read-only (name, code, legal ID) and editable (dept, position, pay). Save + Add/Register button.
        Runnable openFullInfo = () -> {
            if (selectedEmployeeId[0] < 0) {
                JOptionPane.showMessageDialog(main, "Select an employee from the table first.");
                return;
            }
            Window win = SwingUtilities.windowForComponent(main);
            showFullEmployeeInfoDialog(win != null ? win : new JFrame(), selectedEmployeeId[0], loadEmployees);
        };
        btnFullInfo.addActionListener(e -> openFullInfo.run());
        miFullInfo.addActionListener(e -> openFullInfo.run());

        // Add new employee: placeholder for next developer. Implement employee insert/registration (e.g. open a form and call EmployeeDao.insert).
        btnAddNewEmployee.addActionListener(e -> showAddNewEmployeePlaceholder(main, loadEmployees));
        miAddNew.addActionListener(e -> showAddNewEmployeePlaceholder(main, loadEmployees));

        btnExportDTR.addActionListener(e -> {
            if (selectedEmployeeId[0] < 0) { JOptionPane.showMessageDialog(main, "Select an employee first."); return; }
            ReportExporter.exportDTRToCsv(main, selectedEmployeeId[0], selectedEmployeeName[0], null);
        });
        miExportDTR.addActionListener(e -> {
            if (selectedEmployeeId[0] < 0) { JOptionPane.showMessageDialog(main, "Select an employee first."); return; }
            ReportExporter.exportDTRToCsv(main, selectedEmployeeId[0], selectedEmployeeName[0], null);
        });

        btnRefresh.addActionListener(e -> { loadDepts.run(); loadEmployees.run(); });
        txtSearch.addActionListener(e -> loadEmployees.run());

        main.add(detail, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Departments ---
    private static JPanel buildDepartmentsPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Departments", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JPanel form = createGroupPanel("Add / Edit Department");
        JTextField fId = new JTextField();
        JTextField fCode = new JTextField();
        JTextField fName = new JTextField();
        addLabeledField(form, "ID:", fId);
        addLabeledField(form, "Code:", fCode);
        addLabeledField(form, "Name:", fName);
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnClear = new JButton("Clear");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(TEXT_DARK);
        btnUpdate.setBackground(new Color(9, 132, 227)); btnUpdate.setForeground(TEXT_DARK);
        form.add(btnAdd); form.add(btnUpdate); form.add(btnClear);

        String[] cols = {"ID", "Code", "Name"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        Runnable load = () -> {
            try {
                List<DepartmentDao.Department> list = DepartmentDao.findAll();
                refreshTable(tblModel, cols, list, o -> {
                    DepartmentDao.Department d = (DepartmentDao.Department) o;
                    return new Object[]{ d.departmentId, d.departmentCode, d.departmentName };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        load.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            fId.setText(String.valueOf(tblModel.getValueAt(row, 0)));
            fCode.setText(String.valueOf(tblModel.getValueAt(row, 1)));
            fName.setText(String.valueOf(tblModel.getValueAt(row, 2)));
        });
        btnClear.addActionListener(e -> { fId.setText(""); fCode.setText(""); fName.setText(""); table.clearSelection(); });
        btnAdd.addActionListener(e -> {
            try {
                DepartmentDao.insert(fCode.getText().trim(), fName.getText().trim());
                load.run(); btnClear.doClick();
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });
        btnUpdate.addActionListener(e -> {
            if (fId.getText().isEmpty()) { JOptionPane.showMessageDialog(main, "Select a row."); return; }
            try {
                DepartmentDao.update(Integer.parseInt(fId.getText()), fCode.getText().trim(), fName.getText().trim());
                load.run();
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(320, 0));
        left.setOpaque(false);
        left.add(form, BorderLayout.CENTER);
        main.add(left, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Payroll Periods ---
    private static JPanel buildPayrollPeriodsPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Payroll Periods", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        String[] cols = {"ID", "Period", "Start", "End", "Pay date", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("New Period");
        JTextField fName = new JTextField(20);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        JSpinner fStart = new JSpinner(new javax.swing.SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner fEnd = new JSpinner(new javax.swing.SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner fPay = new JSpinner(new javax.swing.SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH));
        fStart.setEditor(new JSpinner.DateEditor(fStart, "yyyy-MM-dd"));
        fEnd.setEditor(new JSpinner.DateEditor(fEnd, "yyyy-MM-dd"));
        fPay.setEditor(new JSpinner.DateEditor(fPay, "yyyy-MM-dd"));
        JComboBox<String> fStatus = new JComboBox<>(new String[]{"open", "closed"});
        addLabeledField(form, "Period name:", fName);
        addLabeledField(form, "Start:", fStart);
        addLabeledField(form, "End:", fEnd);
        addLabeledField(form, "Pay date:", fPay);
        addLabeledField(form, "Status:", fStatus);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(TEXT_DARK);
        form.add(btnAdd);

        Runnable load = () -> {
            try {
                List<PayrollPeriodDao.PayrollPeriod> list = PayrollPeriodDao.findAll();
                refreshTable(tblModel, cols, list, o -> {
                    PayrollPeriodDao.PayrollPeriod p = (PayrollPeriodDao.PayrollPeriod) o;
                    return new Object[]{ p.periodId, p.periodName, p.startDate, p.endDate, p.payDate, p.status };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        load.run();
        btnAdd.addActionListener(e -> {
            try {
                java.util.Date startUtil = (java.util.Date) fStart.getValue();
                java.util.Date endUtil = (java.util.Date) fEnd.getValue();
                java.util.Date payUtil = (java.util.Date) fPay.getValue();
                Date startSql = new Date(startUtil.getTime());
                Date endSql = new Date(endUtil.getTime());
                Date paySql = new Date(payUtil.getTime());
                PayrollPeriodDao.insert(fName.getText().trim(), startSql, endSql, paySql, (String) fStatus.getSelectedItem());
                load.run();
                fName.setText("");
                fStart.setValue(new java.util.Date());
                fEnd.setValue(new java.util.Date());
                fPay.setValue(new java.util.Date());
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(form, BorderLayout.WEST);
        main.add(north, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Offset ---
    private static JPanel buildOffsetPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Offset (Time-in-lieu)", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        boolean admin = AppSession.isAdmin();
        Integer sessionEmpId = AppSession.getEmployeeId();

        JComboBox<EmployeeDao.EmployeeRow> cmbEmployee = new JComboBox<>();
        cmbEmployee.addItem(new EmployeeDao.EmployeeRow(0, "", "", null, false, ""));
        try {
            for (EmployeeDao.EmployeeRow e : EmployeeDao.findAll()) { if (e.isActive) cmbEmployee.addItem(e); }
        } catch (SQLException ignored) {}

        JLabel lblBalance = new JLabel("Balance: --");
        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 14));

        Runnable refreshBalance = () -> {
            try {
                int empId = admin ? (cmbEmployee.getSelectedItem() instanceof EmployeeDao.EmployeeRow ? ((EmployeeDao.EmployeeRow) cmbEmployee.getSelectedItem()).employeeId : 0) : (sessionEmpId != null ? sessionEmpId : 0);
                if (empId <= 0) { lblBalance.setText("Balance: --"); return; }
                BigDecimal bal = OffsetDao.getBalance(empId);
                lblBalance.setText("Balance: " + MONEY.format(bal) + " hrs");
            } catch (SQLException ex) { lblBalance.setText("Balance: error"); }
        };

        if (admin) cmbEmployee.addActionListener(e -> refreshBalance.run());
        refreshBalance.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        if (admin) { top.add(new JLabel("Employee:")); top.add(cmbEmployee); }
        top.add(lblBalance);
        main.add(top, BorderLayout.NORTH);

        String[] reqCols = {"ID", "Employee", "Hours", "Date to apply", "Status", "Requested", "Notes"};
        DefaultTableModel reqModel = new DefaultTableModel(reqCols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable reqTable = new JTable(reqModel);
        reqTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(reqTable);
        JScrollPane reqScroll = new JScrollPane(reqTable);
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{ "All", "pending", "approved", "rejected" });
        Runnable refreshRequestsTable = () -> {
            reqModel.setRowCount(0);
            try {
                Integer filterEmp = admin ? null : sessionEmpId;
                List<OffsetDao.OffsetRequestRow> list = OffsetDao.findRequests((String) cmbStatus.getSelectedItem(), filterEmp);
                for (OffsetDao.OffsetRequestRow r : list)
                    reqModel.addRow(new Object[]{ r.requestId, r.fullName, r.hoursToUse, r.dateToApply, r.status, r.requestedAt != null ? r.requestedAt.toString() : "", r.notes != null ? r.notes : "" });
            } catch (SQLException ex) { /* ignore */ }
        };

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        if (admin) {
            JPanel addForm = new JPanel(new GridLayout(0, 1, 5, 5));
            addForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(MEDIUM_GREY), "Add offset earned (e.g. OT converted)", TitledBorder.LEFT, TitledBorder.TOP));
            addForm.setOpaque(false);
            JTextField fAddHours = new JTextField(8);
            JTextField fAddNotes = new JTextField(20);
            JButton btnAdd = new JButton("Add to balance");
            addForm.add(new JLabel("Hours:"));
            addForm.add(fAddHours);
            addForm.add(new JLabel("Reason/notes:"));
            addForm.add(fAddNotes);
            addForm.add(btnAdd);
            btnAdd.addActionListener(e -> {
                if (!(cmbEmployee.getSelectedItem() instanceof EmployeeDao.EmployeeRow)) { JOptionPane.showMessageDialog(main, "Select an employee."); return; }
                int empId = ((EmployeeDao.EmployeeRow) cmbEmployee.getSelectedItem()).employeeId;
                if (empId <= 0) { JOptionPane.showMessageDialog(main, "Select an employee."); return; }
                try {
                    BigDecimal hrs = new BigDecimal(fAddHours.getText().trim());
                    if (hrs.compareTo(BigDecimal.ZERO) <= 0) { JOptionPane.showMessageDialog(main, "Hours must be positive."); return; }
                    OffsetDao.addToBalance(empId, hrs, fAddNotes.getText().trim());
                    JOptionPane.showMessageDialog(main, "Added " + hrs + " hrs to balance.");
                    fAddHours.setText(""); fAddNotes.setText("");
                    refreshBalance.run();
                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(main, "Enter a valid number for hours."); }
                catch (SQLException ex) { JOptionPane.showMessageDialog(main, "Failed: " + ex.getMessage()); }
            });
            left.add(addForm);
            left.add(Box.createVerticalStrut(15));
        }

        JPanel reqForm = new JPanel(new GridLayout(0, 1, 5, 5));
        reqForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(MEDIUM_GREY), "Request offset (use balance on a date)", TitledBorder.LEFT, TitledBorder.TOP));
        reqForm.setOpaque(false);
        JComboBox<EmployeeDao.EmployeeRow> cmbReqEmployee = new JComboBox<>();
        try {
            if (admin) { cmbReqEmployee.addItem(new EmployeeDao.EmployeeRow(0, "", "", null, false, "")); for (EmployeeDao.EmployeeRow e : EmployeeDao.findAll()) { if (e.isActive) cmbReqEmployee.addItem(e); } }
            else if (sessionEmpId != null) { EmployeeDao.Employee e = EmployeeDao.findById(sessionEmpId); if (e != null) cmbReqEmployee.addItem(new EmployeeDao.EmployeeRow(e.employeeId, e.employeeCode, e.fullName, e.basicSalary, e.isActive, "")); }
        } catch (SQLException ignored) {}
        JTextField fReqHours = new JTextField(8);
        JTextField fReqDate = new JTextField(12);
        fReqDate.setText(java.time.LocalDate.now().toString());
        JButton btnRequest = new JButton("Submit request");
        reqForm.add(new JLabel("Employee:"));
        reqForm.add(cmbReqEmployee);
        reqForm.add(new JLabel("Hours to use:"));
        reqForm.add(fReqHours);
        reqForm.add(new JLabel("Date to apply (YYYY-MM-DD):"));
        reqForm.add(fReqDate);
        reqForm.add(btnRequest);
        btnRequest.addActionListener(e -> {
            if (cmbReqEmployee.getItemCount() == 0 || !(cmbReqEmployee.getSelectedItem() instanceof EmployeeDao.EmployeeRow)) { JOptionPane.showMessageDialog(main, "Select an employee."); return; }
            int empId = ((EmployeeDao.EmployeeRow) cmbReqEmployee.getSelectedItem()).employeeId;
            if (empId <= 0) { JOptionPane.showMessageDialog(main, "Select an employee."); return; }
            try {
                BigDecimal hrs = new BigDecimal(fReqHours.getText().trim());
                if (hrs.compareTo(BigDecimal.ZERO) <= 0) { JOptionPane.showMessageDialog(main, "Hours must be positive."); return; }
                Date d = Date.valueOf(fReqDate.getText().trim());
                OffsetDao.insertRequest(empId, hrs, d, "Requested from UI");
                JOptionPane.showMessageDialog(main, "Request submitted.");
                fReqHours.setText(""); refreshRequestsTable.run();
                if (!admin) refreshBalance.run();
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(main, "Enter a valid number for hours."); }
            catch (SQLException ex) { JOptionPane.showMessageDialog(main, "Invalid date or failed: " + ex.getMessage()); }
        });
        left.add(reqForm);

        main.add(left, BorderLayout.WEST);

        JButton btnRefreshReq = new JButton("Refresh");
        btnRefreshReq.addActionListener(e -> refreshRequestsTable.run());
        refreshRequestsTable.run();

        JPanel reqTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reqTop.setOpaque(false);
        reqTop.add(new JLabel("Status:"));
        reqTop.add(cmbStatus);
        reqTop.add(btnRefreshReq);
        cmbStatus.addActionListener(e -> refreshRequestsTable.run());

        JPanel reqCenter = new JPanel(new BorderLayout(5, 5));
        reqCenter.setOpaque(false);
        reqCenter.add(reqTop, BorderLayout.NORTH);
        reqCenter.add(reqScroll, BorderLayout.CENTER);

        if (admin) {
            JButton btnApprove = new JButton("Approve");
            JButton btnReject = new JButton("Reject");
            reqTop.add(btnApprove);
            reqTop.add(btnReject);
            btnApprove.addActionListener(e -> {
                int row = reqTable.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(main, "Select a request."); return; }
                Object idObj = reqTable.getValueAt(row, 0);
                if (!(idObj instanceof Number)) return;
                int reqId = ((Number) idObj).intValue();
                try {
                    OffsetDao.approveRequest(reqId, AppSession.getHrUserId());
                    JOptionPane.showMessageDialog(main, "Approved.");
                    refreshRequestsTable.run();
                    refreshBalance.run();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(main, "Failed: " + ex.getMessage()); }
            });
            btnReject.addActionListener(e -> {
                int row = reqTable.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(main, "Select a request."); return; }
                Object idObj = reqTable.getValueAt(row, 0);
                if (!(idObj instanceof Number)) return;
                try {
                    OffsetDao.rejectRequest(((Number) idObj).intValue());
                    JOptionPane.showMessageDialog(main, "Rejected.");
                    refreshRequestsTable.run();
                } catch (SQLException ex) { JOptionPane.showMessageDialog(main, "Failed: " + ex.getMessage()); }
            });
        }

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.add(reqCenter, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    // --- DTR ---
    private static JPanel buildDTRPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Time & Attendance (DTR)", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JComboBox<EmployeeDao.EmployeeRow> cmbEmployee = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(new JLabel("Employee:"));
        top.add(cmbEmployee);
        top.add(btnRefresh);

        String[] cols = {"DTR ID", "Employee", "Date", "Time In", "Time Out", "Regular", "OT", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("Add DTR entry");
        JTextField fEmpId = new JTextField(6);
        JTextField fDate = new JTextField(10);
        JTextField fTimeIn = new JTextField(8);
        JTextField fTimeOut = new JTextField(8);
        JTextField fReg = new JTextField(6);
        JTextField fOT = new JTextField(6);
        addLabeledField(form, "Employee ID:", fEmpId);
        addLabeledField(form, "Date (YYYY-MM-DD):", fDate);
        addLabeledField(form, "Time In (HH:mm):", fTimeIn);
        addLabeledField(form, "Time Out (HH:mm):", fTimeOut);
        addLabeledField(form, "Regular hours:", fReg);
        addLabeledField(form, "OT hours:", fOT);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(TEXT_DARK);
        form.add(btnAdd);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll())
                    cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadEmployees = () -> {
            try {
                cmbEmployee.removeAllItems();
                cmbEmployee.addItem(null);
                for (EmployeeDao.EmployeeRow r : EmployeeDao.findAll())
                    cmbEmployee.addItem(r);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadDTR = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                    ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                Integer empId = cmbEmployee.getSelectedItem() instanceof EmployeeDao.EmployeeRow
                    ? ((EmployeeDao.EmployeeRow) cmbEmployee.getSelectedItem()).employeeId : null;
                List<DTRDao.DTRRow> list = DTRDao.findByPeriodAndEmployee(periodId, empId);
                refreshTable(tblModel, cols, list, o -> {
                    DTRDao.DTRRow r = (DTRDao.DTRRow) o;
                    return new Object[]{ r.dtrId, r.fullName, r.dateVal, r.timeIn, r.timeOut, r.regularHours, r.overtimeHours, r.status };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        loadEmployees.run();
        loadDTR.run();
        btnRefresh.addActionListener(e -> loadDTR.run());
        cmbPeriod.addActionListener(e -> loadDTR.run());
        cmbEmployee.addActionListener(e -> loadDTR.run());

        btnAdd.addActionListener(e -> {
            try {
                int empId = Integer.parseInt(fEmpId.getText().trim());
                Date d = Date.valueOf(fDate.getText().trim());
                Time tin = Time.valueOf(fTimeIn.getText().trim() + ":00");
                Time tout = Time.valueOf(fTimeOut.getText().trim() + ":00");
                BigDecimal reg = fReg.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(fReg.getText().trim());
                BigDecimal ot = fOT.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(fOT.getText().trim());
                DTRDao.insert(empId, d, tin, tout, reg, ot, "pending");
                loadDTR.run();
                fEmpId.setText(""); fDate.setText(""); fTimeIn.setText(""); fTimeOut.setText(""); fReg.setText(""); fOT.setText("");
            } catch (Exception ex) { /* database unavailable - show empty data */ }
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(280, 0));
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        main.add(north, BorderLayout.NORTH);
        main.add(left, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Leave ---
    private static JPanel buildLeavePage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Leave", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"All", "pending", "approved", "rejected"});
        JButton btnRefresh = new JButton("Refresh");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Status:"));
        top.add(cmbStatus);
        top.add(btnRefresh);

        String[] cols = {"ID", "Employee", "Type", "Start", "End", "Days", "With pay", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("New leave");
        JTextField fEmpId = new JTextField(6);
        JTextField fType = new JTextField(20);
        JTextField fStart = new JTextField(10);
        JTextField fEnd = new JTextField(10);
        JTextField fDays = new JTextField(5);
        JCheckBox fWithPay = new JCheckBox("With pay", true);
        JTextField fRemarks = new JTextField(20);
        addLabeledField(form, "Employee ID:", fEmpId);
        addLabeledField(form, "Leave type:", fType);
        addLabeledField(form, "Start (YYYY-MM-DD):", fStart);
        addLabeledField(form, "End (YYYY-MM-DD):", fEnd);
        addLabeledField(form, "Total days:", fDays);
        form.add(fWithPay);
        addLabeledField(form, "Remarks:", fRemarks);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(TEXT_DARK);
        form.add(btnAdd);

        Runnable load = () -> {
            try {
                String st = (String) cmbStatus.getSelectedItem();
                List<LeaveDao.LeaveRow> list = LeaveDao.findAll("All".equals(st) ? null : st);
                refreshTable(tblModel, cols, list, o -> {
                    LeaveDao.LeaveRow r = (LeaveDao.LeaveRow) o;
                    return new Object[]{ r.leaveId, r.fullName, r.leaveType, r.startDate, r.endDate, r.totalDays, r.withPay ? "Yes" : "No", r.status };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        cmbStatus.addActionListener(e -> load.run());
        btnAdd.addActionListener(e -> {
            try {
                LeaveDao.insert(Integer.parseInt(fEmpId.getText()), fType.getText().trim(), Date.valueOf(fStart.getText().trim()), Date.valueOf(fEnd.getText().trim()),
                    fDays.getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(fDays.getText().trim()), fWithPay.isSelected(), "pending", fRemarks.getText());
                load.run();
                fEmpId.setText(""); fType.setText(""); fStart.setText(""); fEnd.setText(""); fDays.setText(""); fRemarks.setText("");
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(300, 0));
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);
        main.add(north, BorderLayout.NORTH);
        main.add(left, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Contributions (SSS, PhilHealth, PagIBIG) ---
    private static JPanel buildContributionsPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Contributions (SSS, PhilHealth, PagIBIG)", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export summary to Excel");
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(TEXT_DARK);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(btnRefresh);
        top.add(btnExport);

        String[] cols = {"ID", "Employee", "Period", "Type", "Amount", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("Add contribution");
        JTextField fEmpId = new JTextField(6);
        JTextField fAmount = new JTextField(10);
        JTextField fDesc = new JTextField(20);
        JComboBox<String> fType = new JComboBox<>(DeductionDao.CONTRIBUTION_TYPES);
        addLabeledField(form, "Employee ID:", fEmpId);
        addLabeledField(form, "Type:", fType);
        addLabeledField(form, "Amount:", fAmount);
        addLabeledField(form, "Description:", fDesc);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148));
        btnAdd.setForeground(TEXT_DARK);
        form.add(btnAdd);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadList = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                    ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                List<DeductionDao.DeductionRow> list = DeductionDao.findContributionsByPeriod(periodId);
                refreshTable(tblModel, cols, list, o -> {
                    DeductionDao.DeductionRow r = (DeductionDao.DeductionRow) o;
                    return new Object[]{ r.deductionId, r.fullName, r.payrollPeriodId, r.deductionType, r.amount != null ? MONEY.format(r.amount) : "", r.status };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        loadList.run();
        btnRefresh.addActionListener(e -> loadList.run());
        cmbPeriod.addActionListener(e -> loadList.run());
        btnExport.addActionListener(e -> {
            Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
            ReportExporter.exportContributionsToExcel(main, periodId);
        });

        btnAdd.addActionListener(e -> {
            try {
                if (cmbPeriod.getSelectedItem() == null || !(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                    JOptionPane.showMessageDialog(main, "Select a period first."); return;
                }
                int periodId = ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId;
                DeductionDao.insert(Integer.parseInt(fEmpId.getText()), periodId, (String) fType.getSelectedItem(),
                    new BigDecimal(fAmount.getText().trim()), fDesc.getText(), "active", AppSession.getHrUserId());
                loadList.run();
                fEmpId.setText(""); fAmount.setText(""); fDesc.setText("");
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(280, 0));
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);
        main.add(north, BorderLayout.NORTH);
        main.add(left, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Deductions: table shows statutory (SSS, PhilHealth, PagIBIG) and other deductions as columns; Add form for one-off entries. ---
    // Next developer: statutory/government rates can be edited via "Configure statutory rates" (placeholder dialog to implement).
    private static JPanel buildDeductionsPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Deductions", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); setForeground(TEXT_DARK); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export report to CSV");
        JButton btnStatutoryRates = new JButton("Configure statutory rates");
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(TEXT_DARK);
        btnStatutoryRates.setBackground(GOLDEN);
        btnStatutoryRates.setForeground(TEXT_DARK);
        JTextField txtEmployeeId = new JTextField(8);
        Integer sessionEmpId = AppSession.getEmployeeId();
        txtEmployeeId.setText(sessionEmpId != null ? String.valueOf(sessionEmpId) : "");
        txtEmployeeId.setEditable(AppSession.isAdmin());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(new JLabel("Employee ID:"));
        top.add(txtEmployeeId);
        top.add(btnRefresh);
        top.add(btnExport);
        top.add(btnStatutoryRates);

        // Table columns: statutory and government deductions (SSS, PhilHealth, PagIBIG) plus Loan, Cash Advance, Other — no Type dropdown in table.
        String[] cols = {"Employee", "Period", "SSS", "PhilHealth", "PagIBIG", "Loan", "Cash Advance", "Other", "Total"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("Add deduction");
        JTextField fEmpId = new JTextField(6);
        JTextField fAmount = new JTextField(10);
        JTextField fDesc = new JTextField(20);
        // Type dropdown for one-off entries; statutory amounts also appear in the table when present in DB.
        JComboBox<String> fType = new JComboBox<>(new String[]{"SSS","PhilHealth","PagIBIG","Loan","Cash Advance","Other","Tax","Insurance","Savings"});
        fType.setEditable(true);
        addLabeledField(form, "Employee ID:", fEmpId);
        addLabeledField(form, "Type:", fType);
        addLabeledField(form, "Amount:", fAmount);
        addLabeledField(form, "Description:", fDesc);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(TEXT_DARK);
        form.add(btnAdd);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        // Load deductions and group by employee+period; show one row per employee with statutory (SSS, PhilHealth, PagIBIG) and other columns.
        Runnable loadDed = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                    ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                Integer empId = null;
                if (!txtEmployeeId.getText().trim().isEmpty()) {
                    try { empId = Integer.parseInt(txtEmployeeId.getText().trim()); } catch (NumberFormatException ignored) { }
                }
                if (!AppSession.isAdmin()) empId = AppSession.getEmployeeId();
                List<DeductionDao.DeductionRow> list = DeductionDao.findByPeriodAndEmployee(periodId, empId);
                Map<String, Map<String, BigDecimal>> byKey = new LinkedHashMap<>();
                for (DeductionDao.DeductionRow r : list) {
                    String key = r.employeeId + "\t" + r.fullName + "\t" + r.payrollPeriodId;
                    byKey.computeIfAbsent(key, k -> new LinkedHashMap<>());
                    String type = r.deductionType != null ? r.deductionType.trim() : "Other";
                    BigDecimal val = r.amount != null ? r.amount : BigDecimal.ZERO;
                    Map<String, BigDecimal> rowAmt = byKey.get(key);
                    rowAmt.put(type, rowAmt.getOrDefault(type, BigDecimal.ZERO).add(val));
                }
                tblModel.setRowCount(0);
                for (Map.Entry<String, Map<String, BigDecimal>> e : byKey.entrySet()) {
                    String[] parts = e.getKey().split("\t", 3);
                    String name = parts.length >= 2 ? parts[1] : "";
                    String periodStr = parts.length >= 3 ? parts[2] : (periodId != null ? String.valueOf(periodId) : "");
                    Map<String, BigDecimal> amt = e.getValue();
                    BigDecimal sss = amt.getOrDefault("SSS", BigDecimal.ZERO);
                    BigDecimal ph = amt.getOrDefault("PhilHealth", BigDecimal.ZERO);
                    BigDecimal pag = amt.getOrDefault("PagIBIG", BigDecimal.ZERO);
                    BigDecimal loan = amt.getOrDefault("Loan", BigDecimal.ZERO);
                    BigDecimal ca = amt.getOrDefault("Cash Advance", BigDecimal.ZERO);
                    BigDecimal other = BigDecimal.ZERO;
                    for (Map.Entry<String, BigDecimal> te : amt.entrySet()) {
                        String t = te.getKey();
                        if (!t.equals("SSS") && !t.equals("PhilHealth") && !t.equals("PagIBIG") && !t.equals("Loan") && !t.equals("Cash Advance"))
                            other = other.add(te.getValue());
                    }
                    BigDecimal total = sss.add(ph).add(pag).add(loan).add(ca).add(other);
                    tblModel.addRow(new Object[]{ name, periodStr, MONEY.format(sss), MONEY.format(ph), MONEY.format(pag), MONEY.format(loan), MONEY.format(ca), MONEY.format(other), MONEY.format(total) });
                }
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        loadDed.run();
        btnRefresh.addActionListener(e -> loadDed.run());
        cmbPeriod.addActionListener(e -> loadDed.run());
        txtEmployeeId.addActionListener(e -> loadDed.run());
        // Configure statutory rates: for when government/statutory deduction rates change. Next developer: implement rate storage (e.g. table or config) and apply when computing SSS, PhilHealth, PagIBIG.
        btnStatutoryRates.addActionListener(ev -> showStatutoryRatesDialog(SwingUtilities.getWindowAncestor(main)));
        btnExport.addActionListener(e -> {
            Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
            Integer empId = txtEmployeeId.getText().trim().isEmpty() ? null : Integer.parseInt(txtEmployeeId.getText().trim());
            if (!AppSession.isAdmin()) empId = AppSession.getEmployeeId();
            ReportExporter.exportEmployeeDeductionsToExcel(main, periodId, empId, empId != null ? ("EMP" + empId) : "ALL");
        });

        JLabel lblTotal = new JLabel("Total: 0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAdd.addActionListener(e -> {
            try {
                if (cmbPeriod.getSelectedItem() == null || !(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                    JOptionPane.showMessageDialog(main, "Select a period first."); return;
                }
                int periodId = ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId;
                int empId = Integer.parseInt(fEmpId.getText().trim());
                if (!AppSession.isAdmin() && AppSession.getEmployeeId() != null) empId = AppSession.getEmployeeId();
                DeductionDao.insert(empId, periodId, String.valueOf(fType.getSelectedItem()).trim(),
                    new BigDecimal(fAmount.getText().trim()), fDesc.getText(), "active", AppSession.getHrUserId());
                loadDed.run();
                try {
                    lblTotal.setText("Total: " + MONEY.format(DeductionDao.totalByPeriod(periodId)));
                } catch (SQLException ignored) {}
                fEmpId.setText(""); fAmount.setText(""); fDesc.setText("");
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setBackground(new Color(45, 52, 54));
        south.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        south.add(lblTotal); lblTotal.setForeground(Color.WHITE);
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(280, 0));
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);
        main.add(north, BorderLayout.NORTH);
        main.add(left, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        return main;
    }

    // --- Compensation ---
    private static JPanel buildCompensationPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Compensation", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); setForeground(TEXT_DARK); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export report to CSV");
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(TEXT_DARK);
        JTextField txtEmployeeId = new JTextField(8);
        Integer sessionEmpId = AppSession.getEmployeeId();
        txtEmployeeId.setText(sessionEmpId != null ? String.valueOf(sessionEmpId) : "");
        txtEmployeeId.setEditable(AppSession.isAdmin());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(new JLabel("Employee ID:"));
        top.add(txtEmployeeId);
        top.add(btnRefresh);
        top.add(btnExport);

        String[] cols = {"ID", "Employee", "Period", "Basic", "OT", "Total", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable load = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                    ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                Integer empId = txtEmployeeId.getText().trim().isEmpty() ? null : Integer.parseInt(txtEmployeeId.getText().trim());
                if (!AppSession.isAdmin()) empId = AppSession.getEmployeeId();
                List<CompensationDao.CompensationRow> list = CompensationDao.findByPeriodAndEmployee(periodId, empId);
                refreshTable(tblModel, cols, list, o -> {
                    CompensationDao.CompensationRow r = (CompensationDao.CompensationRow) o;
                    return new Object[]{ r.compensationId, r.fullName, r.payrollPeriodId, r.basicAmount != null ? MONEY.format(r.basicAmount) : "", r.overtimeAmount != null ? MONEY.format(r.overtimeAmount) : "", r.totalCompensation != null ? MONEY.format(r.totalCompensation) : "", r.hrStatus };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        cmbPeriod.addActionListener(e -> load.run());
        txtEmployeeId.addActionListener(e -> load.run());
        btnExport.addActionListener(e -> {
            Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
            Integer empId = txtEmployeeId.getText().trim().isEmpty() ? null : Integer.parseInt(txtEmployeeId.getText().trim());
            if (!AppSession.isAdmin()) empId = AppSession.getEmployeeId();
            ReportExporter.exportEmployeeCompensationToExcel(main, periodId, empId, empId != null ? ("EMP" + empId) : "ALL");
        });

        JPanel form = createGroupPanel("Add compensation");
        JTextField fEmpId = new JTextField(6);
        JTextField fBasicH = new JTextField(6);
        JTextField fBasicA = new JTextField(10);
        JTextField fOTH = new JTextField(6);
        JTextField fOTA = new JTextField(10);
        JTextField fTotal = new JTextField(10);
        addLabeledField(form, "Employee ID:", fEmpId);
        addLabeledField(form, "Basic hours:", fBasicH);
        addLabeledField(form, "Basic amount:", fBasicA);
        addLabeledField(form, "OT hours:", fOTH);
        addLabeledField(form, "OT amount:", fOTA);
        addLabeledField(form, "Total:", fTotal);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(TEXT_DARK);
        form.add(btnAdd);
        btnAdd.addActionListener(e -> {
            try {
                if (cmbPeriod.getSelectedItem() == null || !(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                    JOptionPane.showMessageDialog(main, "Select a period."); return;
                }
                int periodId = ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId;
                BigDecimal basicH = fBasicH.getText().trim().isEmpty() ? null : new BigDecimal(fBasicH.getText());
                BigDecimal basicA = fBasicA.getText().trim().isEmpty() ? null : new BigDecimal(fBasicA.getText());
                BigDecimal otH = fOTH.getText().trim().isEmpty() ? null : new BigDecimal(fOTH.getText());
                BigDecimal otA = fOTA.getText().trim().isEmpty() ? null : new BigDecimal(fOTA.getText());
                BigDecimal total = fTotal.getText().trim().isEmpty() ? null : new BigDecimal(fTotal.getText());
                CompensationDao.insert(Integer.parseInt(fEmpId.getText()), periodId, null, AppSession.getHrUserId(), basicH, basicA, otH, otA, total, "draft");
                load.run();
                fEmpId.setText(""); fBasicH.setText(""); fBasicA.setText(""); fOTH.setText(""); fOTA.setText(""); fTotal.setText("");
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(280, 0));
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);
        main.add(north, BorderLayout.NORTH);
        main.add(left, BorderLayout.WEST);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Reports (company-style exports) ---
    private static JPanel buildReportsPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Reports", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriodEnd = new JComboBox<>();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Payroll period:"));
        top.add(cmbPeriod);
        top.add(new JLabel("  (for 13th month: end period)"));
        top.add(cmbPeriodEnd);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriodEnd.removeAllItems();
                cmbPeriod.addItem(null);
                cmbPeriodEnd.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) {
                    cmbPeriod.addItem(p);
                    cmbPeriodEnd.addItem(p);
                }
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
                if (cmbPeriodEnd.getItemCount() > 1) cmbPeriodEnd.setSelectedIndex(1);
            } catch (SQLException ex) { JOptionPane.showMessageDialog(main, ex.getMessage()); }
        };
        loadPeriods.run();

        JPanel center = new JPanel(new GridLayout(0, 1, 10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createTitledBorder("Export company-style reports (CSV)"));

        JButton btnDeduction = new JButton("Export Deduction Summary (company format)");
        btnDeduction.setBackground(new Color(0, 102, 204));
        btnDeduction.setForeground(Color.WHITE);
        btnDeduction.addActionListener(e -> {
            if (!(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                JOptionPane.showMessageDialog(main, "Select a payroll period first."); return;
            }
            PayrollPeriodDao.PayrollPeriod p = (PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem();
            ReportExporter.exportDeductionSummaryCompanyFormat(main, p.periodId, p.periodName);
        });

        JButton btnCompensation = new JButton("Export Compensation Summary (company format)");
        btnCompensation.setBackground(new Color(0, 102, 204));
        btnCompensation.setForeground(Color.WHITE);
        btnCompensation.addActionListener(e -> {
            if (!(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                JOptionPane.showMessageDialog(main, "Select a payroll period first."); return;
            }
            PayrollPeriodDao.PayrollPeriod p = (PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem();
            ReportExporter.exportCompensationSummaryCompanyFormat(main, p.periodId, p.periodName);
        });

        JButton btnLedger = new JButton("Export Signature Ledger");
        btnLedger.setBackground(new Color(0, 102, 204));
        btnLedger.setForeground(Color.WHITE);
        btnLedger.addActionListener(e -> {
            if (!(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                JOptionPane.showMessageDialog(main, "Select a payroll period first."); return;
            }
            PayrollPeriodDao.PayrollPeriod p = (PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem();
            ReportExporter.exportSignatureLedger(main, p.periodId, p.periodName);
        });

        JButton btnFunding = new JButton("Export Payroll Funding (bank list)");
        btnFunding.setBackground(new Color(0, 102, 204));
        btnFunding.setForeground(Color.WHITE);
        btnFunding.addActionListener(e -> {
            if (!(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                JOptionPane.showMessageDialog(main, "Select a payroll period first."); return;
            }
            PayrollPeriodDao.PayrollPeriod p = (PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem();
            java.util.Date payDate = p.payDate != null ? new java.util.Date(p.payDate.getTime()) : null;
            ReportExporter.exportPayrollFunding(main, p.periodId, p.periodName, payDate);
        });

        JButton btn13th = new JButton("Export 13th Month (quarter)");
        btn13th.setBackground(new Color(0, 102, 204));
        btn13th.setForeground(Color.WHITE);
        btn13th.addActionListener(e -> {
            if (!(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod) || !(cmbPeriodEnd.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) {
                JOptionPane.showMessageDialog(main, "Select start and end payroll periods for the quarter."); return;
            }
            PayrollPeriodDao.PayrollPeriod startP = (PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem();
            PayrollPeriodDao.PayrollPeriod endP = (PayrollPeriodDao.PayrollPeriod) cmbPeriodEnd.getSelectedItem();
            if (startP.startDate == null || endP.startDate == null) {
                JOptionPane.showMessageDialog(main, "Periods must have start dates."); return;
            }
            java.sql.Date qStart = startP.startDate.before(endP.startDate) ? startP.startDate : endP.startDate;
            java.sql.Date qEnd = endP.startDate.after(startP.startDate) ? endP.startDate : startP.startDate;
            try {
                ReportExporter.export13thMonth(main, qStart, qEnd);
            } catch (Exception ex) { JOptionPane.showMessageDialog(main, ex.getMessage()); }
        });

        center.add(btnDeduction);
        center.add(btnCompensation);
        center.add(btnLedger);
        center.add(btnFunding);
        center.add(btn13th);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        return main;
    }

    // --- Payroll ---
    private static JPanel buildPayrollPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Payroll", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(btnRefresh);

        String[] cols = {"ID", "Employee", "Gross", "Deductions", "Net", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable load = () -> {
            try {
                if (cmbPeriod.getSelectedItem() == null || !(cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod)) return;
                int periodId = ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId;
                List<PayrollDao.PayrollRow> list = PayrollDao.findByPeriod(periodId);
                refreshTable(tblModel, cols, list, o -> {
                    PayrollDao.PayrollRow r = (PayrollDao.PayrollRow) o;
                    return new Object[]{ r.payrollId, r.fullName, r.grossPay != null ? MONEY.format(r.grossPay) : "", r.totalDeductions != null ? MONEY.format(r.totalDeductions) : "", r.netPay != null ? MONEY.format(r.netPay) : "", r.status };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        cmbPeriod.addActionListener(e -> load.run());

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        main.add(north, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // --- Ledger ---
    private static JPanel buildLedgerPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);
        main.add(new JLabel("Ledger", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JComboBox<DepartmentDao.Department> cmbDept = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(new JLabel("Department:"));
        top.add(cmbDept);
        top.add(btnRefresh);

        String[] cols = {"Ledger ID", "Department", "Period", "Gross", "Deductions", "Net", "Generated"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadDepts = () -> {
            try {
                cmbDept.removeAllItems();
                cmbDept.addItem(null);
                for (DepartmentDao.Department d : DepartmentDao.findAll()) cmbDept.addItem(d);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable load = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                Integer deptId = cmbDept.getSelectedItem() instanceof DepartmentDao.Department ? ((DepartmentDao.Department) cmbDept.getSelectedItem()).departmentId : null;
                List<LedgerDao.LedgerRow> list = LedgerDao.findAll(periodId, deptId);
                refreshTable(tblModel, cols, list, o -> {
                    LedgerDao.LedgerRow r = (LedgerDao.LedgerRow) o;
                    return new Object[]{ r.ledgerId, r.departmentName, r.payrollPeriodId, r.totalGross != null ? MONEY.format(r.totalGross) : "", r.totalDeductions != null ? MONEY.format(r.totalDeductions) : "", r.totalNet != null ? MONEY.format(r.totalNet) : "", r.generationDate };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        loadDepts.run();
        load.run();
        btnRefresh.addActionListener(e -> load.run());
        cmbPeriod.addActionListener(e -> load.run());
        cmbDept.addActionListener(e -> load.run());

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(top, BorderLayout.WEST);
        main.add(north, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        return main;
    }
}