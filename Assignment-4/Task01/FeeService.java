package task01;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service layer decoupling business logic, validation, and database operations
 * from the GUI layer.
 */
public class FeeService {
    private final String dbUrl;
    private boolean useJdbc = true;
    private final List<String> memoryRecords = Collections.synchronizedList(new ArrayList<>());

    public FeeService() {
        this("jdbc:sqlite::memory:");
    }

    public FeeService(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS fees (" +
                             "reg_no TEXT NOT NULL, " +
                             "amount REAL NOT NULL, " +
                             "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            }
        } catch (ClassNotFoundException | SQLException e) {
            // Fallback to internal storage if SQLite JDBC driver is not on classpath
            useJdbc = false;
        }
    }

    /**
     * Validates inputs and records a fee payment.
     * Uses PreparedStatement to prevent SQL injection when JDBC is active.
     */
    public PaymentResult payFee(String regNo, String amountStr) {
        if (regNo == null || regNo.trim().isEmpty()) {
            return new PaymentResult(false, "Registration number cannot be blank.", null);
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) {
                return new PaymentResult(false, "Payment amount must be greater than zero.", null);
            }
        } catch (NumberFormatException | NullPointerException e) {
            return new PaymentResult(false, "Invalid amount format. Please enter a valid positive number.", null);
        }

        // Simulate network / bank gateway processing delay off-EDT
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new PaymentResult(false, "Payment processing was interrupted.", null);
        }

        if (useJdbc) {
            String sql = "INSERT INTO fees (reg_no, amount) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, regNo.trim());
                pstmt.setDouble(2, amount);
                pstmt.executeUpdate();
                String receiptId = "RCP-" + System.currentTimeMillis() % 1000000;
                return new PaymentResult(true, "Payment of $" + String.format("%.2f", amount) + " processed successfully via Secure JDBC.", receiptId);
            } catch (SQLException e) {
                return new PaymentResult(false, "Database error: " + e.getMessage(), null);
            }
        } else {
            // Memory storage fallback
            memoryRecords.add(regNo.trim() + ":" + amount);
            String receiptId = "RCP-" + System.currentTimeMillis() % 1000000;
            return new PaymentResult(true, "Payment of $" + String.format("%.2f", amount) + " processed successfully (In-Memory Data Store).", receiptId);
        }
    }

    public List<String> getRecords() {
        return new ArrayList<>(memoryRecords);
    }

    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final String receiptId;

        public PaymentResult(boolean success, String message, String receiptId) {
            this.success = success;
            this.message = message;
            this.receiptId = receiptId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getReceiptId() { return receiptId; }
    }
}
