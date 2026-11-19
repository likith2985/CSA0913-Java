# Task 02: From Requirement to Event-Driven Design

## 1. Event Model (Task 2.1)
The event delegation architecture for the Hospital Outpatient Appointment Desk is summarized below:

| UI Component (Event Source) | Event Type Generated | Listener Interface | Handler Method | Action Triggered |
|---|---|---|---|---|
| `departmentCombo` (`JComboBox`) | `ItemEvent` | `ItemListener` | `itemStateChanged(ItemEvent e)` | Filters and repopulates `doctorCombo` with specialists for selected department. |
| `bookButton` (`JButton`) | `ActionEvent` | `ActionListener` | `actionPerformed(ActionEvent e)` | Validates patient input, checks doctor availability, generates appointment ID, and triggers modal confirmation. |
| `resetButton` (`JButton`) | `ActionEvent` | `ActionListener` | `actionPerformed(ActionEvent e)` | Resets patient input, clears selections, and restores default initial state. |
| `HospitalAppointmentDesk` (`JFrame`) | `WindowEvent` | `WindowListener` (`WindowAdapter`) | `windowClosing(WindowEvent e)` | Intercepts OS close request, displays a modal confirmation prompt, and disposes on confirmation. |

---

## 2. Component Hierarchy (Task 2.2)
```text
HospitalAppointmentDesk (JFrame - BorderLayout)
├── [North] headerPanel (JPanel - BorderLayout)
│   ├── [North] titleLabel (JLabel - "SIMATS Outpatient Appointment Desk")
│   └── [South] subtitleLabel (JLabel - "Fast-Track Specialist Booking System")
├── [Center] formPanel (JPanel - GridLayout 5x2)
│   ├── JLabel ("Patient Full Name:")
│   ├── patientNameField (JTextField)
│   ├── JLabel ("Specialist Department:")
│   ├── departmentCombo (JComboBox<String>)
│   ├── JLabel ("Consulting Doctor:")
│   ├── doctorCombo (JComboBox<String>)
│   ├── JLabel ("Preferred Time Slot:")
│   ├── slotCombo (JComboBox<String>)
│   ├── JLabel ("Registration Status:")
│   └── statusLabel (JLabel)
└── [South] buttonPanel (JPanel - FlowLayout Right)
    ├── resetButton (JButton - "Reset Form")
    └── bookButton (JButton - "Confirm & Book Appointment")
```

---

## 3. Handler Logic (Task 2.3 - SIMATS Pseudocode)

### A. Dynamic Doctor List Refresh on Department Selection
```text
ALGORITHM UpdateDoctorList(selectedDept):
    INPUT: selectedDept (String)
    OUTPUT: doctorCombo updated with relevant medical specialists

    IF doctorCombo IS NOT NULL THEN
        doctorCombo.removeAllItems()
    END IF

    doctorsList <- DEPT_DOCTORS.get(selectedDept)
    IF doctorsList IS NOT NULL THEN
        FOR EACH doctor IN doctorsList DO
            doctorCombo.addItem(doctor)
        END FOR
    END IF

    statusLabel.setText("Department updated to " + selectedDept)
END ALGORITHM
```

### B. Validation and Booking Logic
```text
ALGORITHM HandleBooking():
    INPUT: patientName, selectedDept, selectedDoctor, selectedSlot
    OUTPUT: Boolean success, Confirmation receipt or error alert

    patientName <- TRIM(patientNameField.getText())
    IF patientName IS EMPTY THEN
        statusLabel.setText("Validation Error: Patient name is required.")
        SHOW_MESSAGE_DIALOG("Please enter patient name before booking.")
        RETURN FALSE
    END IF

    IF selectedDoctor IS NULL OR selectedDoctor IS EMPTY THEN
        statusLabel.setText("Validation Error: No doctor selected.")
        SHOW_MESSAGE_DIALOG("Please select an available doctor.")
        RETURN FALSE
    END IF

    apptId <- "APT-" + GENERATE_UNIQUE_TIMESTAMP_TOKEN()
    statusLabel.setText("Booked! ID: " + apptId + " for " + patientName)
    
    receiptText <- FORMAT_RECEIPT(apptId, patientName, selectedDept, selectedDoctor, selectedSlot)
    SHOW_MESSAGE_DIALOG(receiptText)
    RETURN TRUE
END ALGORITHM
```

---

## 4. Event Sequence Trace & Applet Comparison (Task 2.4)

### Lifecycle Sequence: Application Open to Close
1. **Instantiation & Assembly:** `main()` invokes `SwingUtilities.invokeLater()` $\rightarrow$ constructor builds component tree.
2. **Window Opened:** `windowOpened(WindowEvent e)` fires when the frame is first made visible on screen.
3. **User Changes Department:** User clicks department dropdown $\rightarrow$ `ItemEvent.DESELECTED` (previous) followed by `ItemEvent.SELECTED` (new) $\rightarrow$ `itemStateChanged()` refreshes `doctorCombo`.
4. **User Submits Form:** User clicks `bookButton` $\rightarrow$ `ActionEvent` dispatched on EDT $\rightarrow$ `actionPerformed()` executes validation and appointment generation.
5. **Window Close Attempt:** User clicks window 'X' $\rightarrow$ `windowClosing(WindowEvent e)` intercepts termination $\rightarrow$ modal confirmation dialog displayed.
6. **Window Disposal:** On 'Yes', `frame.dispose()` is called $\rightarrow$ `windowClosed(WindowEvent e)` fires $\rightarrow$ native OS peers released.

### Comparison: Desktop Window Lifecycle vs Java Applet Lifecycle
| Phase / State | Desktop Event-Driven Window (`JFrame` / `Frame`) | Legacy Java Applet (`Applet` / `JApplet`) |
|---|---|---|
| **Initialization** | Constructor builds containment hierarchy; peer created on `pack()` / `setVisible(true)`. | `init()` called once by browser/viewer to allocate memory and load parameters. |
| **Start / Display** | `windowOpened()`, `setVisible(true)` maps window into OS display server. | `start()` invoked each time browser visits/re-visits the hosting HTML page. |
| **Rendering** | `paint(Graphics g)` / `paintComponent(Graphics g)` called asynchronously by EDT. | `paint(Graphics g)` called on display surface. |
| **Pause / Inactive** | `windowIconified()`, `windowDeactivated()` triggered when minimized or unfocused. | `stop()` invoked automatically when user navigates away from the web page. |
| **Destruction** | `windowClosing()` $\rightarrow$ `dispose()` releases native peers; `System.exit()` halts JVM. | `destroy()` called when browser completely unloads the applet from memory cache. |

---

## 5. Alternative Concept: Dynamic DB Query vs In-Memory Caching
- **Dynamic Database Fetch on Each Dropdown Change:**
  - *Time Complexity:* $O(1)$ query execution + $O(T_{\text{network}})$ latency (often 50–200 ms).
  - *Space Complexity:* $O(1)$ client memory; database holds all state.
  - *Trade-Off:* Ensures real-time doctor availability (e.g. shifts, leaves), but risks UI stutter if network lags, and puts unnecessary read load on the database.
- **In-Memory Caching (`Map<String, List<Doctor>>`):**
  - *Time Complexity:* $O(1)$ instant hash lookup; instantaneous UI response (< 1 ms).
  - *Space Complexity:* $O(d \cdot m)$ client heap memory, where $d$ is departments and $m$ is doctors per department.
  - *Trade-Off:* For outpatient departments ($d \approx 20, m \approx 10$, total ~200 items), memory footprint is trivial (< 50 KB). A hybrid design caches the static catalog and queries the DB asynchronously only for live slot availability.
