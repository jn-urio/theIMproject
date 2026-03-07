import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class page {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(page::createAndShowUI);
    }

    // Made public so other classes (like LoginPage) can open the payroll window
    public static void createAndShowUI() {
        JFrame frame = new JFrame("Payroll System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // fill screen
        frame.setMinimumSize(new Dimension(1024, 600));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top area: title + search panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JLabel titleLabel = new JLabel("Payroll System", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLabel = new JLabel("Search (ID or Name):");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(searchPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- Left button section (navigation / actions) ---
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.Y_AXIS));
        leftButtons.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton newEmployeeBtn = new JButton("New Employee");
        JButton clearFormBtn = new JButton("Clear Form");
        JButton removeRowBtn = new JButton("Remove Selected");
        JButton deductionsBtn = new JButton("Deductions && Contributions");
        JButton exitBtn = new JButton("Exit");

        // --- Top form with payroll details ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        JLabel idLabel = new JLabel("Employee ID:");
        JTextField idField = new JTextField(15);

        JLabel nameLabel = new JLabel("Employee Name:");
        JTextField nameField = new JTextField(20);

        JLabel periodLabel = new JLabel("Pay Period:");
        JTextField periodField = new JTextField("2026-03", 10);

        JLabel hoursLabel = new JLabel("Hours Worked:");
        JTextField hoursField = new JTextField(10);

        JLabel overtimeLabel = new JLabel("Overtime Hours:");
        JTextField overtimeField = new JTextField("0", 10);

        JLabel rateLabel = new JLabel("Hourly Rate:");
        JTextField rateField = new JTextField(10);

        JLabel deductionsLabel = new JLabel("Deductions:");
        JTextField deductionsField = new JTextField("0.00", 10);

        JLabel grossLabel = new JLabel("Gross Pay:");
        JTextField grossField = new JTextField(10);
        grossField.setEditable(false);

        JLabel netLabel = new JLabel("Net Pay:");
        JTextField netField = new JTextField(10);
        netField.setEditable(false);

        JButton calcButton = new JButton("Calculate & Add to History");

        // Layout the form in two columns
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(idLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(idField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(periodLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(periodField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(hoursLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(hoursField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(overtimeLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(overtimeField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(rateLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(rateField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(deductionsLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(deductionsField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(grossLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(grossField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(netLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(netField, gbc);

        row++;
        gbc.gridx = 1; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        formPanel.add(calcButton, gbc);

        // --- Payroll history table filling remaining space ---
        String[] columnNames = { "Employee ID", "Name", "Period", "Hours", "Overtime", "Rate", "Gross", "Deductions", "Net" };
        Object[][] data = {};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable historyTable = new JTable(model);
        historyTable.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(historyTable);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, tableScroll);
        centerSplit.setResizeWeight(0.35); // more space to table

        mainPanel.add(centerSplit, BorderLayout.CENTER);

        calcButton.addActionListener((ActionEvent e) -> {
            try {
                double hours = Double.parseDouble(hoursField.getText());
                double overtime = Double.parseDouble(overtimeField.getText());
                double rate = Double.parseDouble(rateField.getText());
                double deductions = Double.parseDouble(deductionsField.getText());

                double gross = hours * rate + overtime * rate * 1.5;
                double net = gross - deductions;

                grossField.setText(String.format("%.2f", gross));
                netField.setText(String.format("%.2f", net));

                model.addRow(new Object[]{
                        idField.getText(),
                        nameField.getText(),
                        periodField.getText(),
                        hours,
                        overtime,
                        rate,
                        String.format("%.2f", gross),
                        String.format("%.2f", deductions),
                        String.format("%.2f", net)
                });
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter valid numbers for hours, overtime, rate and deductions.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Wire up left buttons now that model and fields exist
        newEmployeeBtn.addActionListener((ActionEvent e) -> {
            idField.setText("");
            nameField.setText("");
            hoursField.setText("");
            overtimeField.setText("0");
            rateField.setText("");
            deductionsField.setText("0.00");
            grossField.setText("");
            netField.setText("");
        });

        clearFormBtn.addActionListener(newEmployeeBtn.getActionListeners()[0]);

        removeRowBtn.addActionListener((ActionEvent e) -> {
            int selected = historyTable.getSelectedRow();
            if (selected >= 0) {
                model.removeRow(selected);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Please select a row in the history table to remove.",
                        "No Row Selected",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        exitBtn.addActionListener((ActionEvent e) -> frame.dispose());

        deductionsBtn.addActionListener((ActionEvent e) -> DeductionsPage.open());

        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(newEmployeeBtn);
        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(clearFormBtn);
        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(removeRowBtn);
        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(deductionsBtn);
        leftButtons.add(Box.createVerticalGlue());
        leftButtons.add(exitBtn);

        mainPanel.add(leftButtons, BorderLayout.WEST);

        // Search logic: select first matching row by ID or Name
        searchButton.addActionListener((ActionEvent e) -> {
            String query = searchField.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                historyTable.clearSelection();
                return;
            }

            int idCol = 0;
            int nameCol = 1;
            for (int i = 0; i < model.getRowCount(); i++) {
                Object idVal = model.getValueAt(i, idCol);
                Object nameVal = model.getValueAt(i, nameCol);
                String idText = idVal == null ? "" : idVal.toString().toLowerCase();
                String nameText = nameVal == null ? "" : nameVal.toString().toLowerCase();

                if (idText.contains(query) || nameText.contains(query)) {
                    historyTable.setRowSelectionInterval(i, i);
                    historyTable.scrollRectToVisible(historyTable.getCellRect(i, 0, true));
                    return;
                }
            }

            JOptionPane.showMessageDialog(frame,
                    "No matching records found.",
                    "Search",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

