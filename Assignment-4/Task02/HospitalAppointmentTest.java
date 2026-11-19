package task02;

public class HospitalAppointmentTest {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   TASK 02: HOSPITAL APPOINTMENT DESK EVENT-DRIVEN TEST SUITE  ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        HospitalAppointmentDesk desk = new HospitalAppointmentDesk(false);

        // Test 1: Initial Department and Doctor list
        total++;
        System.out.println("\n[Test 1] Testing Initial Department & Dynamic Doctor Population:");
        String initialDept = (String) desk.getDepartmentCombo().getSelectedItem();
        int initialDocCount = desk.getDoctorCombo().getItemCount();
        System.out.println("Initial Selected Department: " + initialDept);
        System.out.println("Doctors loaded: " + initialDocCount);
        if ("Cardiology".equals(initialDept) && initialDocCount == 2) {
            System.out.println(">>> PASS: Initial department loaded correct doctor catalog.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Initial doctor count incorrect.");
        }

        // Test 2: Dynamic Doctor List Update on Department Change
        total++;
        System.out.println("\n[Test 2] Testing Dynamic Doctor Refresh on Department Switch:");
        desk.getDepartmentCombo().setSelectedItem("Neurology");
        int neuroDocCount = desk.getDoctorCombo().getItemCount();
        String firstNeuroDoc = desk.getDoctorCombo().getItemAt(0);
        System.out.println("Switched to: Neurology");
        System.out.println("Doctors loaded: " + neuroDocCount + ", First: " + firstNeuroDoc);
        if (neuroDocCount == 2 && firstNeuroDoc.contains("Alan Turing")) {
            System.out.println(">>> PASS: Dynamic ItemListener updated doctor list accordingly.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Dynamic update did not reflect Neurology doctors.");
        }

        // Test 3: Validation on Blank Patient Name
        total++;
        System.out.println("\n[Test 3] Testing Blank Patient Name Validation on Booking:");
        desk.getPatientNameField().setText("   ");
        boolean bookResBlank = desk.handleBooking();
        System.out.println("Booking result with blank name: " + bookResBlank + " | Status: " + desk.getStatusLabel().getText());
        if (!bookResBlank && desk.getStatusLabel().getText().contains("Patient name is required")) {
            System.out.println(">>> PASS: Booking properly rejected when patient name is blank.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Blank patient name was not intercepted.");
        }

        // Test 4: Successful Appointment Booking
        total++;
        System.out.println("\n[Test 4] Testing Valid Appointment Booking Flow:");
        desk.getPatientNameField().setText("Jane Doe");
        desk.getSlotCombo().setSelectedItem("10:30 AM - 11:00 AM");
        boolean bookResValid = desk.handleBooking();
        System.out.println("Booking result: " + bookResValid + " | Status: " + desk.getStatusLabel().getText());
        if (bookResValid && desk.getStatusLabel().getText().contains("Booked! ID: APT-")) {
            System.out.println(">>> PASS: Appointment successfully scheduled and confirmed.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Valid booking failed.");
        }

        // Test 5: Form Reset
        total++;
        System.out.println("\n[Test 5] Testing Form Reset Handler:");
        desk.handleReset();
        boolean resetNameEmpty = desk.getPatientNameField().getText().isEmpty();
        System.out.println("Reset Name Field: '" + desk.getPatientNameField().getText() + "' | Status: " + desk.getStatusLabel().getText());
        if (resetNameEmpty && desk.getStatusLabel().getText().contains("Form reset successfully")) {
            System.out.println(">>> PASS: Form reset restored default state.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Form reset failed.");
        }

        desk.dispose();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " tests.");
        System.out.println("===============================================================");
    }
}
