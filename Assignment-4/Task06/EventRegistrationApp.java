package task06;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Campus Event Registration Desktop Application.
 * Integrates Swing Event Delegation, Responsive Layouts, and Off-EDT JDBC persistence.
 */
public class EventRegistrationApp extends JFrame {
    private final RegistrationDAO dao;
    private final boolean showDialogs;

    // Form inputs
    private final JTextField studentIdField;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JComboBox<String> eventCombo;
    private final JButton registerButton;
    private final JButton clearButton;

    // Filter and Table
    private final JComboBox<String> filterCombo;
    private final JButton refreshButton;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    private static final String[] EVENT_OPTIONS = {
        "Annual Hackathon 2026",
        "Robotics Innovation Expo",
        "National AI Symposium",
        "Speed Code Relay",
        "CyberSecurity Capture The Flag"
    };

    public EventRegistrationApp() {
        this(new RegistrationDAO(), true);
    }

    public EventRegistrationApp(RegistrationDAO dao, boolean showDialogs) {
        super("Campus Event Registration Management System");
        this.dao = dao;
        this.showDialogs = showDialogs;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 560);
        setMinimumSize(new Dimension(680, 440));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // -------------------------------------------------------------
        // Menu Bar Setup
        // -------------------------------------------------------------
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export to CSV...");
        JMenuItem exitItem = new JMenuItem("Exit");

        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        exportItem.addActionListener(e -> exportToCsv(null));
        exitItem.addActionListener(e -> dispose());
        aboutItem.addActionListener(e -> {
            if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this,
                    "Campus Event Registration Desktop App\nVersion 1.0\nBuilt with Java Swing & Secure JDBC\nCourse: CSA0913",
                    "About Application", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // -------------------------------------------------------------
        // Split Layout: Left Form Panel | Right Table & Filter Panel
        // -------------------------------------------------------------
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);

        // --- Left Registration Form ---
        JPanel formContainer = new JPanel(new BorderLayout(5, 5));
        formContainer.setBorder(BorderFactory.createTitledBorder("Student Registration Form"));

        JPanel inputFields = new JPanel(new GridLayout(4, 2, 8, 12));
        inputFields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputFields.add(new JLabel("Student ID:"));
        studentIdField = new JTextField();
        inputFields.add(studentIdField);

        inputFields.add(new JLabel("Full Name:"));
        nameField = new JTextField();
        inputFields.add(nameField);

        inputFields.add(new JLabel("Email Address:"));
        emailField = new JTextField();
        inputFields.add(emailField);

        inputFields.add(new JLabel("Select Event:"));
        eventCombo = new JComboBox<>(EVENT_OPTIONS);
        inputFields.add(eventCombo);

        formContainer.add(inputFields, BorderLayout.NORTH);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        registerButton = new JButton("Register Student");
        registerButton.setBackground(new Color(24, 120, 60));
        registerButton.setForeground(Color.WHITE);
        clearButton = new JButton("Clear Form");

        formButtons.add(registerButton);
        formButtons.add(clearButton);
        formContainer.add(formButtons, BorderLayout.CENTER);

        splitPane.setLeftComponent(formContainer);

        // --- Right Table & Filter View ---
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Active Registrations Catalog"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterPanel.add(new JLabel("Filter by Event:"));
        String[] filterChoices = new String[EVENT_OPTIONS.length + 1];
        filterChoices[0] = "All Events";
        System.arraycopy(EVENT_OPTIONS, 0, filterChoices, 1, EVENT_OPTIONS.length);
        filterCombo = new JComboBox<>(filterChoices);
        filterPanel.add(filterCombo);

        refreshButton = new JButton("Refresh");
        filterPanel.add(refreshButton);
        tableContainer.add(filterPanel, BorderLayout.NORTH);

        String[] headers = {"Reg ID", "Student ID", "Student Name", "Email", "Event Name", "Registration Date"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(table);
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        splitPane.setRightComponent(tableContainer);
        add(splitPane, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // Bottom Status Bar
        // -------------------------------------------------------------
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        statusLabel = new JLabel("System Ready.");
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(140, 16));
        progressBar.setVisible(false);

        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        // -------------------------------------------------------------
        // Action Handlers
        // -------------------------------------------------------------
        registerButton.addActionListener(e -> handleRegistration());
        clearButton.addActionListener(e -> clearInputs());
        refreshButton.addActionListener(e -> loadRegistrationsAsync());
        filterCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadRegistrationsAsync();
            }
        });

        // Initial Data Load
        loadRegistrationsAsync();
    }

    public boolean validateInputs() {
        String sid = studentIdField.getText() != null ? studentIdField.getText().trim() : "";
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";

        if (sid.isEmpty() || name.isEmpty() || email.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: All form fields are mandatory.");
            if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Please fill in all registration fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: Invalid email address format.");
            if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Please provide a valid email format (e.g., student@university.edu).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        return true;
    }

    public void handleRegistration() {
        if (!validateInputs()) {
            return;
        }

        String sid = studentIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String event = (String) eventCombo.getSelectedItem();
        String regId = "REG-" + (System.currentTimeMillis() % 100000);

        Registration newReg = new Registration(regId, sid, name, email, event);

        // Disable button & show spinner during async persistence
        setInputsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setForeground(new Color(0, 102, 204));
        statusLabel.setText("Saving registration to database...");

        // Asynchronous SwingWorker to keep EDT fluid
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    // Small delay to simulate DB network handshake
                    Thread.sleep(200);
                    return dao.registerStudent(newReg);
                } catch (RegistrationDAO.DuplicateRegistrationException e) {
                    errorMessage = e.getMessage();
                    return false;
                } catch (Exception e) {
                    errorMessage = "Database Error: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                setInputsEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);

                try {
                    boolean success = get();
                    if (success) {
                        statusLabel.setForeground(new Color(0, 128, 0));
                        statusLabel.setText("Success: Registered " + name + " for " + event + " (ID: " + regId + ")");
                        clearInputs();
                        loadRegistrationsAsync();
                        if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                            JOptionPane.showMessageDialog(EventRegistrationApp.this,
                                "Registration Confirmed!\nRegistration ID: " + regId + "\nStudent: " + name + "\nEvent: " + event,
                                "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Error: " + errorMessage);
                        if (showDialogs && !GraphicsEnvironment.isHeadless()) {
                            JOptionPane.showMessageDialog(EventRegistrationApp.this, errorMessage, "Registration Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Unexpected error: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    public void loadRegistrationsAsync() {
        String filter = (String) filterCombo.getSelectedItem();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<List<Registration>, Void> loader = new SwingWorker<>() {
            @Override
            protected List<Registration> doInBackground() {
                return dao.getRegistrations(filter);
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                try {
                    List<Registration> list = get();
                    tableModel.setRowCount(0);
                    for (Registration r : list) {
                        tableModel.addRow(r.toTableRow());
                    }
                    statusLabel.setForeground(new Color(30, 30, 30));
                    statusLabel.setText("Loaded " + list.size() + " records for [" + filter + "].");
                } catch (Exception e) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Failed to load records: " + e.getMessage());
                }
            }
        };

        loader.execute();
    }

    public boolean exportToCsv(File targetFile) {
        if (targetFile == null) {
            targetFile = new File("event_registrations_export.csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("\"Registration ID\",\"Student ID\",\"Student Name\",\"Email\",\"Event Name\",\"Registration Date\"");
            List<Registration> records = dao.getRegistrations("All Events");
            for (Registration r : records) {
                writer.println(r.toCsvRow());
            }
            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setText("Successfully exported " + records.size() + " records to " + targetFile.getName());
            return true;
        } catch (Exception e) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("CSV Export Error: " + e.getMessage());
            return false;
        }
    }

    public void clearInputs() {
        studentIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        eventCombo.setSelectedIndex(0);
    }

    private void setInputsEnabled(boolean enabled) {
        studentIdField.setEnabled(enabled);
        nameField.setEnabled(enabled);
        emailField.setEnabled(enabled);
        eventCombo.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }

    // Accessors for automated testing
    public JTextField getStudentIdField() { return studentIdField; }
    public JTextField getNameField() { return nameField; }
    public JTextField getEmailField() { return emailField; }
    public JComboBox<String> getEventCombo() { return eventCombo; }
    public JComboBox<String> getFilterCombo() { return filterCombo; }
    public JButton getRegisterButton() { return registerButton; }
    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getStatusLabel() { return statusLabel; }
    public RegistrationDAO getDao() { return dao; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EventRegistrationApp().setVisible(true);
        });
    }
}
