package task02;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Hospital Outpatient Appointment Desk GUI.
 * Demonstrates:
 * 1. Dynamic doctor list population via ItemListener on Department choice.
 * 2. Form validation and booking confirmation via ActionListener.
 * 3. Graceful exit confirmation via WindowAdapter and WindowEvent.
 */
public class HospitalAppointmentDesk extends JFrame {
    private final JTextField patientNameField;
    private final JComboBox<String> departmentCombo;
    private final JComboBox<String> doctorCombo;
    private final JComboBox<String> slotCombo;
    private final JButton bookButton;
    private final JButton resetButton;
    private final JLabel statusLabel;
    private final boolean showDialogs;

    // In-memory catalog mapping department to doctors
    private static final Map<String, List<String>> DEPT_DOCTORS = new LinkedHashMap<>();

    static {
        DEPT_DOCTORS.put("Cardiology", Arrays.asList("Dr. Sarah Jenkins (Senior Cardiologist)", "Dr. Arvind Rao (Interventional Specialist)"));
        DEPT_DOCTORS.put("Orthopedics", Arrays.asList("Dr. Michael Chang (Joint Replacement)", "Dr. Priya Nair (Trauma Surgeon)"));
        DEPT_DOCTORS.put("Pediatrics", Arrays.asList("Dr. Emily Stone (Neonatologist)", "Dr. Rajesh Kumar (Pediatric Care)"));
        DEPT_DOCTORS.put("Neurology", Arrays.asList("Dr. Alan Turing (Neurosurgeon)", "Dr. Sunita Sharma (Cognitive Neurologist)"));
        DEPT_DOCTORS.put("General Medicine", Arrays.asList("Dr. David Wilson (Internal Medicine)", "Dr. Fatima Zahra (Primary Care)"));
    }

    public HospitalAppointmentDesk() {
        this(true);
    }

    public HospitalAppointmentDesk(boolean showDialogs) {
        super("Hospital Outpatient Appointment Desk - SIMATS Health");
        this.showDialogs = showDialogs;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(540, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Window closing confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                promptExitConfirmation();
            }
        });

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 88, 148));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel titleLabel = new JLabel("SIMATS Outpatient Appointment Desk");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);
        JLabel subtitleLabel = new JLabel("Fast-Track Specialist Booking System");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(220, 235, 252));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        // Row 1: Patient Name
        formPanel.add(new JLabel("Patient Full Name:"));
        patientNameField = new JTextField();
        formPanel.add(patientNameField);

        // Row 2: Department Selection
        formPanel.add(new JLabel("Specialist Department:"));
        departmentCombo = new JComboBox<>(DEPT_DOCTORS.keySet().toArray(new String[0]));
        formPanel.add(departmentCombo);

        // Row 3: Doctor Selection (dynamically populated)
        formPanel.add(new JLabel("Consulting Doctor:"));
        doctorCombo = new JComboBox<>();
        formPanel.add(doctorCombo);

        // Row 4: Time Slot Selection
        formPanel.add(new JLabel("Preferred Time Slot:"));
        slotCombo = new JComboBox<>(new String[]{
            "09:00 AM - 09:30 AM",
            "10:30 AM - 11:00 AM",
            "02:00 PM - 02:30 PM",
            "04:00 PM - 04:30 PM"
        });
        formPanel.add(slotCombo);

        // Row 5: Blank spacer & Status
        formPanel.add(new JLabel("Registration Status:"));
        statusLabel = new JLabel("Ready. Please complete the form.");
        statusLabel.setForeground(new Color(30, 30, 30));
        formPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        resetButton = new JButton("Reset Form");
        bookButton = new JButton("Confirm & Book Appointment");
        bookButton.setBackground(new Color(24, 128, 56));
        bookButton.setForeground(Color.WHITE);

        buttonPanel.add(resetButton);
        buttonPanel.add(bookButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Populate initial doctors for default department
        updateDoctorList((String) departmentCombo.getSelectedItem());

        // Dynamic Department -> Doctor update listener
        departmentCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedDept = (String) e.getItem();
                updateDoctorList(selectedDept);
            }
        });

        // Booking action listener
        bookButton.addActionListener(e -> handleBooking());

        // Reset action listener
        resetButton.addActionListener(e -> handleReset());
    }

    public void updateDoctorList(String department) {
        doctorCombo.removeAllItems();
        List<String> doctors = DEPT_DOCTORS.get(department);
        if (doctors != null) {
            for (String doc : doctors) {
                doctorCombo.addItem(doc);
            }
        }
        statusLabel.setText("Department updated to " + department + " (" + (doctors != null ? doctors.size() : 0) + " doctors available).");
    }

    public boolean handleBooking() {
        String patient = patientNameField.getText() != null ? patientNameField.getText().trim() : "";
        String dept = (String) departmentCombo.getSelectedItem();
        String doctor = (String) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();

        if (patient.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: Patient name is required.");
            if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Please enter patient name before booking.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        if (doctor == null || doctor.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: No doctor selected.");
            if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Please select an available doctor.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        String apptId = "APT-" + System.currentTimeMillis() % 100000;
        statusLabel.setForeground(new Color(0, 128, 0));
        statusLabel.setText("Booked! ID: " + apptId + " for " + patient);

        String summary = "========================================\n" +
                         "       APPOINTMENT CONFIRMATION RECEIPT  \n" +
                         "========================================\n" +
                         " Appointment ID : " + apptId + "\n" +
                         " Patient Name   : " + patient + "\n" +
                         " Department     : " + dept + "\n" +
                         " Doctor         : " + doctor + "\n" +
                         " Scheduled Slot : " + slot + "\n" +
                         " Status         : CONFIRMED\n" +
                         "========================================";

        if (showDialogs && !GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, summary, "Appointment Confirmed", JOptionPane.INFORMATION_MESSAGE);
        }
        return true;
    }

    public void handleReset() {
        patientNameField.setText("");
        departmentCombo.setSelectedIndex(0);
        slotCombo.setSelectedIndex(0);
        statusLabel.setForeground(new Color(30, 30, 30));
        statusLabel.setText("Form reset successfully.");
    }

    public void promptExitConfirmation() {
        if (GraphicsEnvironment.isHeadless()) {
            dispose();
            return;
        }
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to close the Outpatient Appointment Desk?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (option == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    // Accessors for testing
    public JTextField getPatientNameField() { return patientNameField; }
    public JComboBox<String> getDepartmentCombo() { return departmentCombo; }
    public JComboBox<String> getDoctorCombo() { return doctorCombo; }
    public JComboBox<String> getSlotCombo() { return slotCombo; }
    public JLabel getStatusLabel() { return statusLabel; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HospitalAppointmentDesk desk = new HospitalAppointmentDesk();
            desk.setVisible(true);
        });
    }
}
