import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class page {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(page::createAndShowUI);
    }

    private static void createAndShowUI() {
        JFrame frame = new JFrame("Simple Payroll");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Payroll Calculator", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Employee Name:");
        JTextField nameField = new JTextField(20);

        JLabel hoursLabel = new JLabel("Hours Worked:");
        JTextField hoursField = new JTextField(10);

        JLabel rateLabel = new JLabel("Hourly Rate:");
        JTextField rateField = new JTextField(10);

        JLabel grossLabel = new JLabel("Gross Pay:");
        JTextField grossField = new JTextField(10);
        grossField.setEditable(false);

        JButton calcButton = new JButton("Calculate");
        calcButton.addActionListener((ActionEvent e) -> {
            try {
                double hours = Double.parseDouble(hoursField.getText());
                double rate = Double.parseDouble(rateField.getText());
                double gross = hours * rate;
                grossField.setText(String.format("%.2f", gross));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter valid numbers for hours and rate.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(hoursLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(hoursField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(rateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(rateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(grossLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(grossField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(calcButton, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

