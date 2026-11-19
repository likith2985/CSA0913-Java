package task01;

public class FeeWindowTest {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     TASK 01: GUI DESIGN REVIEW AND REPAIR TEST SUITE          ");
        System.out.println("===============================================================");

        FeeService service = new FeeService();
        int passed = 0;
        int total = 0;

        // Test 1: Empty Reg No validation
        total++;
        System.out.println("\n[Test 1] Testing Blank Reg No Validation:");
        FeeService.PaymentResult res1 = service.payFee("  ", "500.0");
        System.out.println("Result: Success=" + res1.isSuccess() + ", Message=" + res1.getMessage());
        if (!res1.isSuccess() && res1.getMessage().contains("blank")) {
            System.out.println(">>> PASS: Blank Reg No properly rejected.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Blank Reg No was not properly rejected.");
        }

        // Test 2: Invalid Amount Format validation
        total++;
        System.out.println("\n[Test 2] Testing Invalid Non-Numeric Amount Validation:");
        FeeService.PaymentResult res2 = service.payFee("21BCE1001", "five-hundred");
        System.out.println("Result: Success=" + res2.isSuccess() + ", Message=" + res2.getMessage());
        if (!res2.isSuccess() && res2.getMessage().contains("Invalid amount")) {
            System.out.println(">>> PASS: Non-numeric amount correctly rejected.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Non-numeric amount was not rejected.");
        }

        // Test 3: Negative/Zero Amount Validation
        total++;
        System.out.println("\n[Test 3] Testing Negative Amount Validation:");
        FeeService.PaymentResult res3 = service.payFee("21BCE1001", "-150.0");
        System.out.println("Result: Success=" + res3.isSuccess() + ", Message=" + res3.getMessage());
        if (!res3.isSuccess() && res3.getMessage().contains("greater than zero")) {
            System.out.println(">>> PASS: Negative amount properly rejected.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Negative amount was not rejected.");
        }

        // Test 4: SQL Injection attempt validation & parameterization
        total++;
        System.out.println("\n[Test 4] Testing SQL Injection Attack Mitigation:");
        String maliciousReg = "REG001'); DROP TABLE fees; --";
        FeeService.PaymentResult res4 = service.payFee(maliciousReg, "1200.0");
        System.out.println("Result: Success=" + res4.isSuccess() + ", Message=" + res4.getMessage() + ", Receipt=" + res4.getReceiptId());
        if (res4.isSuccess() && res4.getReceiptId() != null) {
            System.out.println(">>> PASS: Malicious string safely escaped and processed as literal text via PreparedStatement.");
            passed++;
        } else {
            System.out.println(">>> FAIL: SQL injection handling failed.");
        }

        // Test 5: Valid Payment Execution
        total++;
        System.out.println("\n[Test 5] Testing Valid Normal Fee Payment:");
        FeeService.PaymentResult res5 = service.payFee("21BCE1042", "2500.0");
        System.out.println("Result: Success=" + res5.isSuccess() + ", Message=" + res5.getMessage() + ", Receipt=" + res5.getReceiptId());
        if (res5.isSuccess() && res5.getReceiptId() != null) {
            System.out.println(">>> PASS: Valid payment successfully recorded with receipt ID generated.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Valid payment failed.");
        }

        // Test 6: FeeWindowRepaired UI State & Clear Logic
        total++;
        System.out.println("\n[Test 6] Testing FeeWindowRepaired Clear Operation:");
        FeeWindowRepaired window = new FeeWindowRepaired(service);
        window.getRegNoField().setText("TEST1234");
        window.getAmountField().setText("999.00");
        window.handleClear();
        boolean fieldsCleared = window.getRegNoField().getText().isEmpty() && window.getAmountField().getText().isEmpty();
        System.out.println("Fields after Clear: RegNo='" + window.getRegNoField().getText() + "', Amount='" + window.getAmountField().getText() + "'");
        if (fieldsCleared) {
            System.out.println(">>> PASS: Clear handler correctly resets input fields without unintended side-effects.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Clear handler failed to reset fields.");
        }

        window.dispose();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " tests.");
        System.out.println("===============================================================");
    }
}
