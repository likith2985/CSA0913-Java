package task06;

import java.io.File;
import java.util.List;

public class EventRegistrationTest {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   TASK 06: CAMPUS EVENT REGISTRATION APP TEST SUITE           ");
        System.out.println("===============================================================");

        RegistrationDAO dao = new RegistrationDAO();
        EventRegistrationApp app = new EventRegistrationApp(dao, false);
        int passed = 0;
        int total = 0;

        // -------------------------------------------------------------
        // Test 1: Validation of Missing Input Fields
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 1] Testing Missing Input Form Validation:");
        app.getStudentIdField().setText("");
        app.getNameField().setText("John Doe");
        app.getEmailField().setText("john@example.com");
        boolean v1 = app.validateInputs();
        System.out.println("Validation result with blank Student ID: " + v1 + " | Status: " + app.getStatusLabel().getText());
        if (!v1 && app.getStatusLabel().getText().contains("mandatory")) {
            System.out.println(">>> PASS: Empty student ID correctly flagged as invalid.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Blank input validation failed.");
        }

        // -------------------------------------------------------------
        // Test 2: Validation of Malformed Email Address
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 2] Testing Invalid Email Format Validation:");
        app.getStudentIdField().setText("21BCE001");
        app.getNameField().setText("John Doe");
        app.getEmailField().setText("invalid-email-string");
        boolean v2 = app.validateInputs();
        System.out.println("Validation result with invalid email: " + v2 + " | Status: " + app.getStatusLabel().getText());
        if (!v2 && app.getStatusLabel().getText().contains("Invalid email")) {
            System.out.println(">>> PASS: Malformed email correctly rejected.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Email validation failed.");
        }

        // -------------------------------------------------------------
        // Test 3: Valid Registration Execution
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 3] Testing Valid Registration Submission:");
        try {
            Registration r1 = new Registration("REG-101", "21BCE1001", "Alice Smith", "alice@university.edu", "Annual Hackathon 2026");
            boolean reg1 = dao.registerStudent(r1);
            System.out.println("Registration 1 inserted: " + reg1);
            if (reg1 && dao.isAlreadyRegistered("21BCE1001", "Annual Hackathon 2026")) {
                System.out.println(">>> PASS: Valid registration successfully persisted.");
                passed++;
            } else {
                System.out.println(">>> FAIL: Registration not persisted.");
            }
        } catch (Exception e) {
            System.out.println(">>> FAIL: Exception during valid registration: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // Test 4: Duplicate Registration Prevention (Same Student, Same Event)
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 4] Testing Duplicate Registration Interception:");
        try {
            Registration duplicate = new Registration("REG-102", "21BCE1001", "Alice Smith", "alice@university.edu", "Annual Hackathon 2026");
            dao.registerStudent(duplicate);
            System.out.println(">>> FAIL: Duplicate registration was erroneously permitted.");
        } catch (RegistrationDAO.DuplicateRegistrationException e) {
            System.out.println("Caught Expected Duplicate Exception: " + e.getMessage());
            System.out.println(">>> PASS: Duplicate registration intercepted and rejected.");
            passed++;
        } catch (Exception e) {
            System.out.println(">>> FAIL: Unexpected exception type: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // Test 5: Same Student Registering for Different Event (Allowed)
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 5] Testing Multi-Event Registration (Same Student, Different Event):");
        try {
            Registration r2 = new Registration("REG-103", "21BCE1001", "Alice Smith", "alice@university.edu", "Robotics Innovation Expo");
            boolean reg2 = dao.registerStudent(r2);
            System.out.println("Registration for different event: " + reg2);
            if (reg2) {
                System.out.println(">>> PASS: Same student successfully registered for a separate distinct event.");
                passed++;
            } else {
                System.out.println(">>> FAIL: Multi-event registration failed.");
            }
        } catch (Exception e) {
            System.out.println(">>> FAIL: Unexpected exception: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // Test 6: Event Filtering Query
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 6] Testing Event Catalog Filtering Logic:");
        try {
            Registration r3 = new Registration("REG-104", "21BCE2002", "Bob Johnson", "bob@university.edu", "Annual Hackathon 2026");
            dao.registerStudent(r3);

            List<Registration> hackathonList = dao.getRegistrations("Annual Hackathon 2026");
            List<Registration> roboticsList = dao.getRegistrations("Robotics Innovation Expo");
            List<Registration> allList = dao.getRegistrations("All Events");

            System.out.println("Hackathon Attendees: " + hackathonList.size());
            System.out.println("Robotics Attendees: " + roboticsList.size());
            System.out.println("Total Attendees: " + allList.size());

            if (hackathonList.size() == 2 && roboticsList.size() == 1 && allList.size() == 3) {
                System.out.println(">>> PASS: Filtering precisely segments attendees by event category.");
                passed++;
            } else {
                System.out.println(">>> FAIL: Filtering counts mismatch.");
            }
        } catch (Exception e) {
            System.out.println(">>> FAIL: Filtering exception: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // Test 7: CSV Export Functionality
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 7] Testing Data Export to CSV:");
        File csvFile = new File("test_export.csv");
        boolean exportSuccess = app.exportToCsv(csvFile);
        System.out.println("Export result: " + exportSuccess + " | File exists: " + csvFile.exists() + " | Length: " + csvFile.length() + " bytes");
        if (exportSuccess && csvFile.exists() && csvFile.length() > 50) {
            System.out.println(">>> PASS: Registrations successfully exported to formatted CSV file.");
            passed++;
            csvFile.delete(); // cleanup
        } else {
            System.out.println(">>> FAIL: CSV export failed.");
        }

        app.dispose();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " tests.");
        System.out.println("===============================================================");
    }
}
