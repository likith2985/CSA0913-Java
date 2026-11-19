package taskb2;

import taskb1.PassStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class AdversarialFileTest {
    public static void main(String[] args) throws IOException {
        System.out.println("===============================================================");
        System.out.println("     TASK B2: ADVERSARIAL INPUTS & EXPLOITATION DEFENCE        ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        // -------------------------------------------------------------
        // Part 1: Adversarial SQL Injection Testing
        // -------------------------------------------------------------
        LoginSecurityDemo loginDemo = new LoginSecurityDemo();

        // Adversarial Attack 1: Comment injection to bypass password
        total++;
        System.out.println("\n[Adversarial Attack 1] SQL Comment Injection (id: \"admin' --\", pwd: \"fake_pwd\"):");
        String attackerId = "admin' --";
        String fakePwd = "invalid_attacker_password";

        boolean unsafeResult1 = loginDemo.unsafeLogin(attackerId, fakePwd);
        boolean safeResult1 = loginDemo.safeLogin(attackerId, fakePwd);

        System.out.println("-> Unsafe Login Outcome : " + (unsafeResult1 ? "LOGIN ACCEPTED (EXPLOITED)" : "REJECTED"));
        System.out.println("-> Safe PreparedStatement: " + (safeResult1 ? "LOGIN ACCEPTED" : "LOGIN REJECTED (SAFE)"));

        if (unsafeResult1 && !safeResult1) {
            System.out.println(">>> PASS: Unsafe code fell victim to injection; PreparedStatement neutralised the attack.");
            passed++;
        } else {
            System.out.println(">>> FAIL: SQL injection behaviour did not match expected threat model.");
        }

        // Adversarial Attack 2: Tautology injection
        total++;
        System.out.println("\n[Adversarial Attack 2] Tautology Injection (id: \"student01\", pwd: \"' OR '1'='1\"):");
        String tautologyPwd = "' OR '1'='1";
        boolean unsafeResult2 = loginDemo.unsafeLogin("student01", tautologyPwd);
        boolean safeResult2 = loginDemo.safeLogin("student01", tautologyPwd);

        System.out.println("-> Unsafe Login Outcome : " + (unsafeResult2 ? "LOGIN ACCEPTED (EXPLOITED)" : "REJECTED"));
        System.out.println("-> Safe PreparedStatement: " + (safeResult2 ? "LOGIN ACCEPTED" : "LOGIN REJECTED (SAFE)"));

        if (unsafeResult2 && !safeResult2) {
            System.out.println(">>> PASS: Tautology bypass succeeded on unsafe query and was neutralized by PreparedStatement.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Tautology test failed.");
        }

        // -------------------------------------------------------------
        // Part 2: Adversarial Damaged File Store Testing
        // -------------------------------------------------------------
        System.out.println("\n---------------------------------------------------------------");
        System.out.println("Part 2: Adversarial Corrupted File Store Testing");
        System.out.println("---------------------------------------------------------------");

        Path damagedFile = Files.createTempFile("adversarial_passes_", ".csv");
        PassStore store = new PassStore(damagedFile);

        // Injecting diverse corruptions:
        StringBuilder adversarialPayload = new StringBuilder();
        adversarialPayload.append("P001,192311001,Valid First Record,10,3500.0\n"); // Valid line 1
        adversarialPayload.append("P002,WRONG_FIELD_COUNT_CORRUPT\n");              // Malformed: only 2 fields
        adversarialPayload.append("\n");                                             // Malformed: empty line
        adversarialPayload.append("   \n");                                          // Malformed: whitespace only
        adversarialPayload.append("P003,192311003,Bad Amount,14,NaN_AMOUNT_CORRUPT\n");// Malformed: non-numeric amount
        adversarialPayload.append("P004,192311004,Punctuation,In,Name,22,5000.0\n");// Malformed: unquoted commas in name (6 fields)
        adversarialPayload.append("P005,192311005,Valid Last Record,10,4200.0\n");  // Valid line 2

        Files.writeString(damagedFile, adversarialPayload.toString(), StandardOpenOption.CREATE);

        // Test 3: Reading Valid Record Ahead of Corruptions
        total++;
        System.out.println("\n[Test 3] Finding Valid Record Before Injected Corruptions (P001):");
        String[] r1 = store.find("P001");
        System.out.println("Result: " + Arrays.toString(r1));
        if (r1 != null && "P001".equals(r1[0])) {
            System.out.println(">>> PASS: Valid initial record successfully parsed.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Initial record read failed.");
        }

        // Test 4: Querying Truncated Record (P002)
        total++;
        System.out.println("\n[Test 4] Querying Corrupt Truncated Line (P002):");
        String[] r2 = store.find("P002");
        System.out.println("Result: " + Arrays.toString(r2));
        if (r2 == null) {
            System.out.println(">>> PASS: Truncated line rejected safely without index out of bounds exception.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Corrupt line was erroneously returned.");
        }

        // Test 5: Querying Valid Record Trailing Multiple Corruptions (P005)
        total++;
        System.out.println("\n[Test 5] Finding Valid Record After Multiple Corruptions (P005):");
        String[] r5 = store.find("P005");
        System.out.println("Result: " + Arrays.toString(r5));
        if (r5 != null && "P005".equals(r5[0])) {
            System.out.println(">>> PASS: Stream parser traversed all empty, truncated, and malformed lines without termination.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Search halted prematurely.");
        }

        Files.deleteIfExists(damagedFile);

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " adversarial tests.");
        System.out.println("===============================================================");
    }
}
