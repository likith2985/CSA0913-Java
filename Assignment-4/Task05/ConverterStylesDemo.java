package task05;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Demonstrates and tests all 4 Event Handling Styles:
 * Style A: Frame / Enclosing class implements ActionListener
 * Style B: Named Inner Class implements ActionListener
 * Style C: Anonymous Inner Class
 * Style D: Modern Lambda Expression
 */
public class ConverterStylesDemo {

    // Style A Helper: Outer Listener
    static class StyleAConverter implements ActionListener {
        private final JTextField amount;
        private final JComboBox<String> target;
        private final JLabel result;

        public StyleAConverter(JTextField amount, JComboBox<String> target, JLabel result) {
            this.amount = amount;
            this.target = target;
            this.result = result;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            executeConversion(amount, target, result);
        }
    }

    // Style B Helper: Named Inner Class
    static class NamedConvertHandler implements ActionListener {
        private final JTextField amount;
        private final JComboBox<String> target;
        private final JLabel result;

        public NamedConvertHandler(JTextField amount, JComboBox<String> target, JLabel result) {
            this.amount = amount;
            this.target = target;
            this.result = result;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            executeConversion(amount, target, result);
        }
    }

    public static void executeConversion(JTextField amount, JComboBox<String> target, JLabel result) {
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            String curr = (String) target.getSelectedItem();
            double rate = "USD".equals(curr) ? 0.012 : 0.011;
            result.setText(String.format("%.2f %s", inr * rate, curr));
        } catch (Exception ex) {
            result.setText("Error");
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     TASK 05: FOUR EVENT-HANDLING STYLES TEST SUITE            ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        JTextField amtField = new JTextField();
        JComboBox<String> targetCombo = new JComboBox<>(new String[]{"USD", "EUR"});
        JLabel resLabel = new JLabel();

        // -------------------------------------------------------------
        // Test 1: Style A (Implements ActionListener)
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 1] Testing Style A (Class Implements ActionListener):");
        StyleAConverter styleA = new StyleAConverter(amtField, targetCombo, resLabel);
        amtField.setText("10000");
        targetCombo.setSelectedItem("USD");
        styleA.actionPerformed(new ActionEvent(amtField, ActionEvent.ACTION_PERFORMED, "convert"));
        System.out.println("Style A (10000 INR to USD) -> Result: " + resLabel.getText());
        if ("120.00 USD".equals(resLabel.getText())) {
            System.out.println(">>> PASS: Style A successfully converted currency.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Style A failed.");
        }

        // -------------------------------------------------------------
        // Test 2: Style B (Named Inner Class)
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 2] Testing Style B (Named Inner Class):");
        NamedConvertHandler styleB = new NamedConvertHandler(amtField, targetCombo, resLabel);
        amtField.setText("5000");
        targetCombo.setSelectedItem("EUR");
        styleB.actionPerformed(new ActionEvent(amtField, ActionEvent.ACTION_PERFORMED, "convert"));
        System.out.println("Style B (5000 INR to EUR) -> Result: " + resLabel.getText());
        if ("55.00 EUR".equals(resLabel.getText())) {
            System.out.println(">>> PASS: Style B successfully converted currency.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Style B failed.");
        }

        // -------------------------------------------------------------
        // Test 3: Style C (Anonymous Inner Class)
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 3] Testing Style C (Anonymous Inner Class):");
        ActionListener styleC = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeConversion(amtField, targetCombo, resLabel);
            }
        };
        amtField.setText("25000");
        targetCombo.setSelectedItem("USD");
        styleC.actionPerformed(new ActionEvent(amtField, ActionEvent.ACTION_PERFORMED, "convert"));
        System.out.println("Style C (25000 INR to USD) -> Result: " + resLabel.getText());
        if ("300.00 USD".equals(resLabel.getText())) {
            System.out.println(">>> PASS: Style C successfully converted currency.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Style C failed.");
        }

        // -------------------------------------------------------------
        // Test 4: Style D (Lambda Expression in Converter.java)
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 4] Testing Style D (Modern Lambda in Converter.java):");
        Converter converter = new Converter(false);
        converter.getAmountField().setText("50000");
        converter.getTargetCombo().setSelectedItem("EUR");

        // Simulate Button Click
        converter.getConvertButton().getActionListeners()[0].actionPerformed(
            new ActionEvent(converter.getConvertButton(), ActionEvent.ACTION_PERFORMED, "")
        );
        System.out.println("Style D Button Click (50000 INR to EUR) -> " + converter.getResultLabel().getText());
        boolean buttonPass = converter.getResultLabel().getText().contains("550.00 EUR");

        // Simulate Enter Key in Amount Field
        converter.getAmountField().setText("1000");
        converter.getTargetCombo().setSelectedItem("USD");
        converter.getAmountField().getActionListeners()[0].actionPerformed(
            new ActionEvent(converter.getAmountField(), ActionEvent.ACTION_PERFORMED, "")
        );
        System.out.println("Style D Enter Key (1000 INR to USD) -> " + converter.getResultLabel().getText());
        boolean enterPass = converter.getResultLabel().getText().contains("12.00 USD");

        if (buttonPass && enterPass) {
            System.out.println(">>> PASS: Style D Lambda successfully handled both Button click and Enter key events.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Style D failed.");
        }

        // Test 5: Validation Handling in Converter
        total++;
        System.out.println("\n[Test 5] Testing Non-numeric Validation in Converter:");
        converter.getAmountField().setText("invalid-amt");
        converter.convert();
        System.out.println("Invalid Input -> Result: " + converter.getResultLabel().getText());
        if (converter.getResultLabel().getText().contains("Error: Invalid number")) {
            System.out.println(">>> PASS: Invalid input caught and handled gracefully.");
            passed++;
        } else {
            System.out.println(">>> FAIL: NumberFormatException not caught.");
        }

        converter.dispose();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " tests.");
        System.out.println("===============================================================");
    }
}
