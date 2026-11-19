package taskb2;

import java.util.HashMap;
import java.util.Map;

/**
 * Task B2: Adversarial Security Demonstration.
 * Compares an unsafe string-concatenated SQL login check against a secure PreparedStatement model.
 */
public class LoginSecurityDemo {
    private final Map<String, String> userTable = new HashMap<>();

    public LoginSecurityDemo() {
        // Seed database
        userTable.put("admin", "SuperSecretPass#2026");
        userTable.put("student01", "Password@123");
    }

    /**
     * VULNERABLE IMPLEMENTATION (from Task A5.2)
     * Directly concatenates user input strings:
     * String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
     * 
     * Simulates SQL parser behavior:
     * If input contains SQL comment marker "--" or injection payload "' OR '1'='1",
     * it bypasses the password condition.
     */
    public boolean unsafeLogin(String id, String pwd) {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("   [Unsafe Engine] Constructed SQL: " + sql);

        // SQL Parser simulation of vulnerability:
        // Case 1: Injection comments out password check (e.g. "admin' --")
        if (id.contains("' --") || id.contains("'--") || id.contains("' /*")) {
            String targetUser = id.split("'")[0].trim();
            if (userTable.containsKey(targetUser)) {
                System.out.println("   [VULNERABILITY EXPLOITED] Comment marker intercepted! Password check bypassed for: " + targetUser);
                return true;
            }
        }

        // Case 2: Tautology injection (e.g. "' OR '1'='1")
        if (id.contains("' OR '1'='1") || id.contains("' OR 1=1") ||
            pwd.contains("' OR '1'='1") || pwd.contains("' OR 1=1")) {
            System.out.println("   [VULNERABILITY EXPLOITED] Tautology payload evaluated to TRUE! Arbitrary login granted.");
            return true;
        }

        // Normal check
        return userTable.containsKey(id) && userTable.get(id).equals(pwd);
    }

    /**
     * SECURE REPAIRED IMPLEMENTATION (PreparedStatement)
     * Query structure: SELECT * FROM users WHERE id = ? AND pwd = ?
     * 
     * In PreparedStatement, the query parse tree is fixed at compile-time.
     * All inputs are treated strictly as literal data strings.
     */
    public boolean safeLogin(String id, String pwd) {
        System.out.println("   [Safe PreparedStatement] Query: SELECT * FROM users WHERE id = ? AND pwd = ?");
        System.out.println("   [Safe PreparedStatement] Bound Param 1 (id): [\"" + id + "\"]");
        System.out.println("   [Safe PreparedStatement] Bound Param 2 (pwd): [\"" + pwd + "\"]");

        // Strict literal equality matching - SQL injection characters are treated as literal text
        if (userTable.containsKey(id)) {
            return userTable.get(id).equals(pwd);
        }
        return false;
    }
}
