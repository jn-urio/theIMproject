import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.List;

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

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        // When run directly (not from LoginPage), use a default session so the app opens without login
        if (AppSession.getUsername() == null) {
            AppSession.setHRUser(1, "admin", "admin", 1);
        }
        SwingUtilities.invokeLater(page::createAndShowUI);
    }

    public static void createAndShowUI() {
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

        String[] nav = { "Employee", "Deductions", "Compensation" };
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

        // Nav listeners by button index
        buttons[0].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_EMPLOYEE"));
        buttons[1].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_DEDUCTIONS"));
        buttons[2].addActionListener(e -> cardLayout.show(cardPanel, "PAGE_COMP"));
        buttons[3].addActionListener(e -> { AppSession.clear(); System.exit(0); });

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setMaximumSize(new Dimension(200, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setBackground(new Color(45, 52, 54));
        b.setForeground(Color.WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(new Color(63, 72, 75)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(new Color(45, 52, 54)); }
        });
        return b;
    }

    private static JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        border.setTitleColor(DARK_BROWN);
        border.setBorder(BorderFactory.createLineBorder(SAGE, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
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

    private static void refreshTable(DefaultTableModel model, String[] columns, List<?> rows, RowMapper mapper) {
        model.setRowCount(0);
        model.setColumnIdentifiers(columns);
        for (Object row : rows) model.addRow(mapper.toRow(row));
    }

    private interface RowMapper { Object[] toRow(Object o); }

    // ===================== HUB PAGES (requested buttons) =====================
    private static JPanel buildEmployeeHubPage() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel header = new JLabel("Employee", SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(DARK_BROWN);
        top.add(header, BorderLayout.WEST);

        JLabel banner = new JLabel("Logged in as: " + (AppSession.getUsername() != null ? AppSession.getUsername() : "") +
            " | Role: " + (AppSession.getHrRole() != null ? AppSession.getHrRole() : "") +
            " | Employee ID: " + (AppSession.getEmployeeId() != null ? AppSession.getEmployeeId() : "N/A"));
        banner.setForeground(MEDIUM_GREY);
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

        JPanel detail = new JPanel(new BorderLayout());
        detail.setPreferredSize(new Dimension(340, 0));
        detail.setOpaque(false);
        JPanel form = createGroupPanel("Employee Details");
        JTextField fId = new JTextField();
        JTextField fCode = new JTextField();
        JTextField fName = new JTextField();
        JTextField fBasic = new JTextField();
        JTextField fDaily = new JTextField();
        JTextField fHourly = new JTextField();
        JComboBox<String> fDept = new JComboBox<>();
        JComboBox<PositionDao.Position> fPos = new JComboBox<>();
        JTextField fRole = new JTextField();
        fRole.setEditable(false);
        JTextField fAttendanceDate = new JTextField(10);
        fAttendanceDate.setText(java.time.LocalDate.now().toString());
        JComboBox<String> fAttendance = new JComboBox<>(new String[]{"Present", "Late", "Absent"});
        fAttendance.setSelectedItem("Present");
        JCheckBox fActive = new JCheckBox("Active", true);
        addLabeledField(form, "ID:", fId);
        addLabeledField(form, "Code:", fCode);
        addLabeledField(form, "Name:", fName);
        addLabeledField(form, "Role:", fRole);
        addLabeledField(form, "Department:", fDept);
        addLabeledField(form, "Position:", fPos);
        addLabeledField(form, "Attendance date:", fAttendanceDate);
        addLabeledField(form, "Attendance:", fAttendance);
        addLabeledField(form, "Basic salary:", fBasic);
        addLabeledField(form, "Daily rate:", fDaily);
        addLabeledField(form, "Hourly rate:", fHourly);
        form.add(fActive);
        JPanel actBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actBar.setOpaque(false);
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnClear = new JButton("Clear");
        JButton btnRegisterUser = new JButton("Register User");
        JButton btnExportDTR = new JButton("Export DTR to CSV");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(9, 132, 227)); btnUpdate.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(225, 112, 85)); btnClear.setForeground(Color.WHITE);
        btnRegisterUser.setBackground(new Color(99, 110, 114)); btnRegisterUser.setForeground(Color.WHITE);
        btnExportDTR.setBackground(GOLDEN); btnExportDTR.setForeground(DARK_BROWN);
        actBar.add(btnAdd); actBar.add(btnUpdate); actBar.add(btnClear); actBar.add(btnRegisterUser); actBar.add(btnExportDTR);
        detail.add(actBar, BorderLayout.NORTH);
        detail.add(form, BorderLayout.CENTER);

        String[] cols = {"ID", "Code", "Name", "Department", "Basic", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        JScrollPane scroll = new JScrollPane(table);

        Runnable loadDepts = () -> {
            try {
                cmbDept.removeAllItems();
                cmbDept.addItem("All Departments");
                for (DepartmentDao.Department d : DepartmentDao.findAll())
                    cmbDept.addItem(d.departmentName);
                fDept.removeAllItems();
                fDept.addItem("");
                for (DepartmentDao.Department d : DepartmentDao.findAll())
                    fDept.addItem(d.departmentName);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadPositions = () -> {
            try {
                fPos.removeAllItems();
                fPos.addItem(null);
                for (PositionDao.Position p : PositionDao.findAll()) fPos.addItem(p);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadEmployees = () -> {
            try {
                String dept = (String) cmbDept.getSelectedItem();
                String search = txtSearch.getText();
                List<EmployeeDao.EmployeeRow> rows = EmployeeDao.findAllWithDepartment(dept, search);
                refreshTable(tblModel, cols, rows, o -> {
                    EmployeeDao.EmployeeRow r = (EmployeeDao.EmployeeRow) o;
                    return new Object[]{ r.employeeId, r.employeeCode, r.fullName, r.departmentName != null ? r.departmentName : "", r.basicSalary != null ? MONEY.format(r.basicSalary) : "", r.isActive ? "Active" : "Inactive" };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadDepts.run();
        loadPositions.run();
        loadEmployees.run();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            Integer id = (Integer) tblModel.getValueAt(row, 0);
            if (id == null) return;
            try {
                EmployeeDao.Employee emp = EmployeeDao.findById(id);
                if (emp != null) {
                    fId.setText(String.valueOf(emp.employeeId));
                    fCode.setText(emp.employeeCode);
                    fName.setText(emp.fullName);
                    fBasic.setText(emp.basicSalary != null ? emp.basicSalary.toPlainString() : "");
                    fDaily.setText(emp.dailyRate != null ? emp.dailyRate.toPlainString() : "");
                    fHourly.setText(emp.hourlyRate != null ? emp.hourlyRate.toPlainString() : "");
                    fActive.setSelected(emp.isActive);
                }
                EmployeeRoleDao.EmployeeRoleInfo role = EmployeeRoleDao.getActiveRole(id);
                fRole.setText(role != null && role.roleType != null ? role.roleType : "");
                if (role != null && role.departmentName != null)
                    fDept.setSelectedItem(role.departmentName);
                if (role != null) {
                    Integer posId = RegUserDao.getPositionIdForRole(role.employeeRoleId);
                    if (posId != null) {
                        for (int i = 0; i < fPos.getItemCount(); i++) {
                            PositionDao.Position p = fPos.getItemAt(i);
                            if (p != null && p.positionId == posId) { fPos.setSelectedIndex(i); break; }
                        }
                    } else {
                        fPos.setSelectedItem(null);
                    }
                }
                // Load attendance for selected date (default Present)
                try {
                    java.sql.Date attDate = java.sql.Date.valueOf(fAttendanceDate.getText().trim());
                    String status = DTRDao.getStatusForDate(id, attDate);
                    if (status != null && (status.equals("Present") || status.equals("Late") || status.equals("Absent")))
                        fAttendance.setSelectedItem(status);
                    else
                        fAttendance.setSelectedItem("Present");
                } catch (Exception ignored) {
                    fAttendance.setSelectedItem("Present");
                }
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        fAttendance.addActionListener(ev -> {
            if (fId.getText().trim().isEmpty()) return;
            try {
                int empId = Integer.parseInt(fId.getText().trim());
                java.sql.Date attDate = java.sql.Date.valueOf(fAttendanceDate.getText().trim());
                String status = (String) fAttendance.getSelectedItem();
                if (status != null) DTRDao.setAttendanceStatus(empId, attDate, status);
            } catch (Exception ignored) { }
        });

        btnExportDTR.addActionListener(e -> {
            if (fId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(main, "Select an employee first."); return; }
            try {
                int empId = Integer.parseInt(fId.getText().trim());
                ReportExporter.exportDTRToCsv(main, empId, fName.getText().trim(), null);
            } catch (Exception ex) { JOptionPane.showMessageDialog(main, "Select an employee first."); }
        });

        btnRefresh.addActionListener(e -> { loadDepts.run(); loadEmployees.run(); });
        txtSearch.addActionListener(e -> loadEmployees.run());
        btnClear.addActionListener(e -> {
            fId.setText(""); fCode.setText(""); fName.setText(""); fRole.setText("");
            fAttendanceDate.setText(java.time.LocalDate.now().toString());
            fAttendance.setSelectedItem("Present");
            fBasic.setText(""); fDaily.setText(""); fHourly.setText(""); fActive.setSelected(true);
            table.clearSelection();
        });
        btnAdd.addActionListener(e -> {
            try {
                BigDecimal basic = fBasic.getText().trim().isEmpty() ? null : new BigDecimal(fBasic.getText().trim());
                BigDecimal daily = fDaily.getText().trim().isEmpty() ? null : new BigDecimal(fDaily.getText().trim());
                BigDecimal hourly = fHourly.getText().trim().isEmpty() ? null : new BigDecimal(fHourly.getText().trim());
                EmployeeDao.insert(fCode.getText().trim(), fName.getText().trim(), basic, daily, hourly, fActive.isSelected());
                loadEmployees.run();
                btnClear.doClick();
            } catch (Exception ex) { /* database unavailable - show empty data */ }
        });
        btnUpdate.addActionListener(e -> {
            if (fId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(main, "Select an employee first."); return; }
            try {
                BigDecimal basic = fBasic.getText().trim().isEmpty() ? null : new BigDecimal(fBasic.getText().trim());
                BigDecimal daily = fDaily.getText().trim().isEmpty() ? null : new BigDecimal(fDaily.getText().trim());
                BigDecimal hourly = fHourly.getText().trim().isEmpty() ? null : new BigDecimal(fHourly.getText().trim());
                EmployeeDao.update(Integer.parseInt(fId.getText()), fCode.getText().trim(), fName.getText().trim(), basic, daily, hourly, fActive.isSelected());
                EmployeeRoleDao.EmployeeRoleInfo role = EmployeeRoleDao.getActiveRole(Integer.parseInt(fId.getText()));
                if (role != null) {
                    PositionDao.Position pos = (PositionDao.Position) fPos.getSelectedItem();
                    RegUserDao.upsertPositionForRole(role.employeeRoleId, pos != null ? pos.positionId : null);
                }
                loadEmployees.run();
            } catch (Exception ex) { /* database unavailable - show empty data */ }
        });

        btnRegisterUser.addActionListener(e -> {
            if (fId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(main, "Select an employee first."); return; }
            String deptName = (String) fDept.getSelectedItem();
            if (deptName == null || deptName.trim().isEmpty()) { JOptionPane.showMessageDialog(main, "Select a department for the employee role."); return; }
            try {
                // Lookup department id
                int deptId = -1;
                for (DepartmentDao.Department d : DepartmentDao.findAll()) {
                    if (deptName.equals(d.departmentName)) { deptId = d.departmentId; break; }
                }
                if (deptId < 0) { JOptionPane.showMessageDialog(main, "Invalid department."); return; }
                int empId = Integer.parseInt(fId.getText().trim());
                int roleId = EmployeeRoleDao.ensureActiveRole(empId, deptId, "hr");

                JTextField u = new JTextField();
                JPasswordField p = new JPasswordField();
                JComboBox<String> r = new JComboBox<>(new String[]{"user", "admin"});
                int res = JOptionPane.showConfirmDialog(main, new Object[]{
                    "Username:", u,
                    "Password:", p,
                    "Role:", r
                }, "Register User (admin only)", JOptionPane.OK_CANCEL_OPTION);
                if (res != JOptionPane.OK_OPTION) return;
                HRUserDao.createUser(roleId, (String) r.getSelectedItem(), u.getText().trim(), new String(p.getPassword()));
                JOptionPane.showMessageDialog(main, "User registered.");
            } catch (Exception ex) {
                /* database unavailable - show empty data */
            }
        });

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
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
        btnUpdate.setBackground(new Color(9, 132, 227)); btnUpdate.setForeground(Color.WHITE);
        form.add(btnAdd); form.add(btnUpdate); form.add(btnClear);

        String[] cols = {"ID", "Code", "Name"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
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
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("New Period");
        JTextField fName = new JTextField(20);
        JTextField fStart = new JTextField(10);
        JTextField fEnd = new JTextField(10);
        JTextField fPay = new JTextField(10);
        JComboBox<String> fStatus = new JComboBox<>(new String[]{"open", "closed"});
        addLabeledField(form, "Period name:", fName);
        addLabeledField(form, "Start (YYYY-MM-DD):", fStart);
        addLabeledField(form, "End (YYYY-MM-DD):", fEnd);
        addLabeledField(form, "Pay date (YYYY-MM-DD):", fPay);
        addLabeledField(form, "Status:", fStatus);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
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
                PayrollPeriodDao.insert(fName.getText().trim(), Date.valueOf(fStart.getText().trim()), Date.valueOf(fEnd.getText().trim()), Date.valueOf(fPay.getText().trim()), (String) fStatus.getSelectedItem());
                load.run();
                fName.setText(""); fStart.setText(""); fEnd.setText(""); fPay.setText("");
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(form, BorderLayout.WEST);
        main.add(north, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
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
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
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
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
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
        main.setBackground(new Color(230, 245, 255));
        main.add(new JLabel("Contributions (SSS, PhilHealth, PagIBIG)", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export summary to Excel");
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(Color.WHITE);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Period:"));
        top.add(cmbPeriod);
        top.add(btnRefresh);
        top.add(btnExport);

        String[] cols = {"ID", "Employee", "Period", "Type", "Amount", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
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
        btnAdd.setForeground(Color.WHITE);
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

    // --- Deductions (Loan, Cash Advance, Other) ---
    private static JPanel buildDeductionsPage() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(245, 246, 247));
        main.add(new JLabel("Deductions", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); setForeground(DARK_BROWN); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export report to CSV");
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(Color.WHITE);
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

        String[] cols = {"ID", "Employee", "Period", "Type", "Amount", "Status"};
        DefaultTableModel tblModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tblModel);
        JScrollPane scroll = new JScrollPane(table);

        JPanel form = createGroupPanel("Add deduction");
        JTextField fEmpId = new JTextField(6);
        JTextField fAmount = new JTextField(10);
        JTextField fDesc = new JTextField(20);
        JComboBox<String> fType = new JComboBox<>(new String[]{"SSS","PhilHealth","PagIBIG","Loan","Cash Advance","Other","Tax","Insurance","Savings"});
        fType.setEditable(true);
        addLabeledField(form, "Employee ID:", fEmpId);
        addLabeledField(form, "Type:", fType);
        addLabeledField(form, "Amount:", fAmount);
        addLabeledField(form, "Description:", fDesc);
        JButton btnAdd = new JButton("Add");
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
        form.add(btnAdd);

        Runnable loadPeriods = () -> {
            try {
                cmbPeriod.removeAllItems();
                cmbPeriod.addItem(null);
                for (PayrollPeriodDao.PayrollPeriod p : PayrollPeriodDao.findAll()) cmbPeriod.addItem(p);
                if (cmbPeriod.getItemCount() > 1) cmbPeriod.setSelectedIndex(1);
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        Runnable loadDed = () -> {
            try {
                Integer periodId = cmbPeriod.getSelectedItem() instanceof PayrollPeriodDao.PayrollPeriod
                    ? ((PayrollPeriodDao.PayrollPeriod) cmbPeriod.getSelectedItem()).periodId : null;
                Integer empId = txtEmployeeId.getText().trim().isEmpty() ? null : Integer.parseInt(txtEmployeeId.getText().trim());
                if (!AppSession.isAdmin()) empId = AppSession.getEmployeeId();
                List<DeductionDao.DeductionRow> list = DeductionDao.findByPeriodAndEmployee(periodId, empId);
                refreshTable(tblModel, cols, list, o -> {
                    DeductionDao.DeductionRow r = (DeductionDao.DeductionRow) o;
                    return new Object[]{ r.deductionId, r.fullName, r.payrollPeriodId, r.deductionType, r.amount != null ? MONEY.format(r.amount) : "", r.status };
                });
            } catch (SQLException ex) { /* database unavailable - show empty data */ }
        };
        loadPeriods.run();
        loadDed.run();
        btnRefresh.addActionListener(e -> loadDed.run());
        cmbPeriod.addActionListener(e -> loadDed.run());
        txtEmployeeId.addActionListener(e -> loadDed.run());
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
        main.add(new JLabel("Compensation", SwingConstants.LEFT) {{ setFont(new Font("SansSerif", Font.BOLD, 22)); setForeground(DARK_BROWN); }}, BorderLayout.NORTH);

        JComboBox<PayrollPeriodDao.PayrollPeriod> cmbPeriod = new JComboBox<>();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export report to CSV");
        btnExport.setBackground(new Color(0, 102, 204));
        btnExport.setForeground(Color.WHITE);
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
        btnAdd.setBackground(new Color(0, 184, 148)); btnAdd.setForeground(Color.WHITE);
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
