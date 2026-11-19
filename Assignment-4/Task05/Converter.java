package task05;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Currency Converter - Completed Code (Style d - Lambda Expression).
 * Converts Indian Rupee (INR) to USD or EUR.
 * Supports both button click and Enter key trigger.
 */
public class Converter extends JFrame {
    private JTextField amount;
    private JComboBox<String> target;
    private JLabel result;
    private JButton convertButton;
    private boolean showDialog = true;

    public Converter() {
        this(true);
    }

    public Converter(boolean showDialog) {
        super("Currency Converter (INR to USD/EUR)");
        this.showDialog = showDialog;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Amount in INR:"));
        amount = new JTextField();
        add(amount);

        add(new JLabel("Target Currency:"));
        target = new JComboBox<>(new String[]{"USD", "EUR"});
        add(target);

        convertButton = new JButton("Convert");
        add(convertButton);

        result = new JLabel("Result: -");
        result.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(result);

        // TODO 1: Attach event handler using Style (d) Lambda expression
        // to both convertButton and amount (Enter key)
        ActionListener convertAction = e -> convert();
        convertButton.addActionListener(convertAction);
        amount.addActionListener(convertAction);

        // Window closing adapter
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    // TODO 2: Convert amount from INR to target currency and update result
    public void convert() {
        String input = amount.getText() != null ? amount.getText().trim() : "";
        if (input.isEmpty()) {
            result.setText("Error: Enter amount");
            if (showDialog && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount in INR.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        try {
            double inr = Double.parseDouble(input);
            if (inr < 0) {
                result.setText("Error: Negative amount");
                if (showDialog && !GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(this, "Amount cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                }
                return;
            }

            String curr = (String) target.getSelectedItem();
            double converted = inr * rate(curr);
            String formatted = String.format("%.2f %s", converted, curr);
            result.setText("Result: " + formatted);

            if (showDialog && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this,
                    "Converted Amount:\n" + String.format("%.2f INR = %.2f %s", inr, converted, curr),
                    "Conversion Result",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            result.setText("Error: Invalid number");
            if (showDialog && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric value.", "Format Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public double rate(String curr) {
        return "USD".equals(curr) ? 0.012 : 0.011;
    }

    // Accessors for testing
    public JTextField getAmountField() { return amount; }
    public JComboBox<String> getTargetCombo() { return target; }
    public JLabel getResultLabel() { return result; }
    public JButton getConvertButton() { return convertButton; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Converter().setVisible(true);
        });
    }
}
