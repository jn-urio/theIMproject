import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DeductionsPage {

    public static void open() {
        SwingUtilities.invokeLater(DeductionsPage::createAndShowUI);
    }

    private static void createAndShowUI() {
        JFrame frame = new JFrame("Deductions & Contributions");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Deductions & Contributions Setup", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Left buttons
        JPanel leftButtons = new JPanel();
        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.Y_AXIS));
        leftButtons.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton removeBtn = new JButton("Remove");
        JButton closeBtn = new JButton("Close");

        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(addBtn);
        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(updateBtn);
        leftButtons.add(Box.createVerticalStrut(5));
        leftButtons.add(removeBtn);
        leftButtons.add(Box.createVerticalGlue());
        leftButtons.add(closeBtn);

        mainPanel.add(leftButtons, BorderLayout.WEST);

        // Center: form + table
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        JLabel codeLabel = new JLabel("Code:");
        JTextField codeField = new JTextField(10);

        JLabel nameLabel = new JLabel("Description:");
        JTextField nameField = new JTextField(20);

        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Deduction", "Contribution"});

        JLabel rateLabel = new JLabel("Rate (%):");
        JTextField rateField = new JTextField("0.00", 8);

        JCheckBox activeCheck = new JCheckBox("Active", true);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(codeLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(codeField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(typeLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(typeCombo, gbc);

        row++;
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(rateLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        formPanel.add(rateField, gbc);

        row++;
        gbc.gridx = 1; gbc.gridy = row;
        formPanel.add(activeCheck, gbc);

        String[] columnNames = {"Code", "Description", "Type", "Rate (%)", "Active"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(table);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, tableScroll);
        splitPane.setResizeWeight(0.35);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Button actions
        addBtn.addActionListener((ActionEvent e) -> {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            String rateText = rateField.getText().trim();
            boolean active = activeCheck.isSelected();

            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Code and Description are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double rate = Double.parseDouble(rateText);
                model.addRow(new Object[]{
                        code,
                        name,
                        type,
                        String.format("%.2f", rate),
                        active ? "Yes" : "No"
                });
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Rate must be a valid number.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        updateBtn.addActionListener((ActionEvent e) -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(frame,
                        "Select a row to update.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            String rateText = rateField.getText().trim();
            boolean active = activeCheck.isSelected();

            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Code and Description are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double rate = Double.parseDouble(rateText);
                model.setValueAt(code, selected, 0);
                model.setValueAt(name, selected, 1);
                model.setValueAt(type, selected, 2);
                model.setValueAt(String.format("%.2f", rate), selected, 3);
                model.setValueAt(active ? "Yes" : "No", selected, 4);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Rate must be a valid number.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        removeBtn.addActionListener((ActionEvent e) -> {
            int selected = table.getSelectedRow();
            if (selected >= 0) {
                model.removeRow(selected);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Select a row to remove.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selected = table.getSelectedRow();
                if (selected >= 0) {
                    codeField.setText((String) model.getValueAt(selected, 0));
                    nameField.setText((String) model.getValueAt(selected, 1));
                    typeCombo.setSelectedItem(model.getValueAt(selected, 2));
                    rateField.setText((String) model.getValueAt(selected, 3));
                    activeCheck.setSelected("Yes".equals(model.getValueAt(selected, 4)));
                }
            }
        });

        closeBtn.addActionListener((ActionEvent e) -> frame.dispose());

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}

