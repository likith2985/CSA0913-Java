import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Assignment 3 - Q1: Handling Failure in a Bank ATM Simulation
 * Demonstrates:
 * 1. Multi-catch and structured exception hierarchy
 * 2. Checked vs Unchecked exceptions
 * 3. Guaranteed execution of finally blocks (Card Ejection & Audit)
 * 4. Test harness executing 5 contrasting failure and success scenarios
 */
public class AtmSimulation {

    // Custom Checked Exception for business logic failure
    public static class InsufficientFundsException extends Exception {
        private final double shortfall;

        public InsufficientFundsException(String message, double shortfall) {
            super(message);
            this.shortfall = shortfall;
        }

        public double getShortfall() { return shortfall; }
    }

    // Account state
    private final String accountNo;
    private double balance;
    private boolean sessionActive;

    public AtmSimulation(String accountNo, double initialBalance) {
        this.accountNo = accountNo;
        this.balance = initialBalance;
        this.sessionActive = false;
    }

    public double getBalance() { return balance; }

    /**
     * Executes ATM withdrawal transaction.
     * Demonstrates multi-catch, checked exception handling, and finally block guarantee.
     */
    public boolean processWithdrawal(String cardFilePath, String amountInput) {
        sessionActive = true;
        System.out.printf("[ATM SESSION START] Account: %s | Current Balance: Rs %.2f%n", accountNo, balance);
        boolean success = false;

        try {
            // 1. Verify Card Reader hardware/file (Checked Exception: FileNotFoundException)
            File cardFile = new File(cardFilePath);
            if (!cardFile.exists()) {
                throw new FileNotFoundException("Card reader data file missing: " + cardFilePath);
            }

            // 2. Parse withdrawal amount (Unchecked Exception: NumberFormatException)
            double amount;
            try {
                amount = Double.parseDouble(amountInput);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid numeric format for withdrawal amount: '" + amountInput + "'", nfe);
            }

            // 3. Validate positive amount (Unchecked Exception: IllegalArgumentException)
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be strictly positive. Entered: " + amount);
            }

            // 4. Validate sufficient funds (Custom Checked Exception: InsufficientFundsException)
            if (amount > balance) {
                double shortfall = amount - balance;
                throw new InsufficientFundsException(
                        String.format("Insufficient balance! Requested: Rs %.2f, Available: Rs %.2f", amount, balance),
                        shortfall);
            }

            // 5. Dispense cash
            balance -= amount;
            System.out.printf("  >> [DISPENSE SUCCESS] Dispensed: Rs %.2f | Remaining Balance: Rs %.2f%n", amount, balance);
            success = true;

        } catch (FileNotFoundException fnfe) {
            System.out.println("  [CATCH 1 - HARDWARE/FILE ERROR]: " + fnfe.getMessage());
        } catch (IllegalArgumentException iae) {
            // Multi-catch concept or unified unchecked input handling
            System.out.println("  [CATCH 2 - INVALID INPUT ERROR]: " + iae.getMessage());
        } catch (InsufficientFundsException ife) {
            System.out.printf("  [CATCH 3 - BUSINESS RULE ERROR]: %s (Shortfall: Rs %.2f)%n",
                    ife.getMessage(), ife.getShortfall());
        } finally {
            // FINALLY block guarantees card ejection and audit logging, even on unhandled exceptions!
            sessionActive = false;
            System.out.println("  [FINALLY GUARANTEE]: Card ejected safely. Session closed for " + accountNo + ".");
            System.out.println("--------------------------------------------------------------------------");
        }

        return success;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("==========================================================================");
        System.out.println("  Q1: BANK ATM FAILURE HANDLING & EXCEPTION TRACING");
        System.out.println("==========================================================================\n");

        // Prepare dummy valid card file for tests
        File validCard = new File("card_chip.dat");
        if (!validCard.exists()) validCard.createNewFile();

        AtmSimulation atm = new AtmSimulation("SB-9876543210", 5000.0);

        // --- TEST 1: Valid Normal Withdrawal ---
        System.out.println("--- Test 1: Valid Normal Withdrawal (Rs 1,500) ---");
        atm.processWithdrawal("card_chip.dat", "1500.00");

        // --- TEST 2: User enters letters instead of numbers (NumberFormatException) ---
        System.out.println("--- Test 2: Non-numeric Input ('FiveHundred') ---");
        atm.processWithdrawal("card_chip.dat", "FiveHundred");

        // --- TEST 3: User enters negative amount (IllegalArgumentException) ---
        System.out.println("--- Test 3: Negative Withdrawal Amount (-200.00) ---");
        atm.processWithdrawal("card_chip.dat", "-200.00");

        // --- TEST 4: Request exceeds balance (InsufficientFundsException) ---
        System.out.println("--- Test 4: Overdraft Attempt (Requested Rs 10,000 with Balance Rs 3,500) ---");
        atm.processWithdrawal("card_chip.dat", "10000.00");

        // --- TEST 5: Card reader file missing (FileNotFoundException) ---
        System.out.println("--- Test 5: Missing Card File ('corrupted_slot.dat') ---");
        atm.processWithdrawal("corrupted_slot.dat", "500.00");

        // Clean up temporary card file
        if (validCard.exists()) validCard.delete();
    }
}
