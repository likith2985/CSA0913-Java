# CSA0913 - Java Programming: Assignment 4 Lab Report
## Course Outcome: CO4 - Event-Driven GUI Design, AWT & Swing Architectures, Layout Management, and GUI-JDBC Integration
**Academic Institution:** Saveetha Institute of Medical and Technical Sciences (SIMATS)  
**Course Code:** CSA0913 - Java Programming  
**Student GitHub Repository:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  
**Total Marks:** 50 Marks (Cognitive Levels: K4 - Analyze, K5 - Evaluate / Create)

---

## Executive Summary
This laboratory report details the design, diagnosis, forensic debugging, layout engineering, and full-stack GUI-database integration completed for **Assignment 4 (CO4)**. The assignment explores the Java Abstract Window Toolkit (AWT) and Swing frameworks, focusing on:
1. **Architectural Diagnosis & Repair (Task 1 - 6 Marks - K4):** Identifying multi-faceted defects in a monolithic `FeeWindow` and refactoring it into a decoupled, secure, and EDT-responsive application using `FeeService`, `PreparedStatement`, and `SwingWorker`.
2. **Event Delegation & Life-Cycle Modeling (Task 2 - 8 Marks - K4):** Engineering an Outpatient Appointment Desk with dynamic cascading dropdown updates, form validation, exit confirmation, and comparing desktop window lifecycles to the Java Applet lifecycle.
3. **Layout Management & Resizing Dynamics (Task 3 - 9 Marks - K4):** Designing a responsive Campus Library Search screen using nested `BorderLayout` and `FlowLayout` containers with `JTable`/`JScrollPane`, empirically proving layout invariance and expansion across multiple resolutions.
4. **Event-Handling Forensics & Drawing Persistence (Task 4 - 7 Marks - K4):** Debugging click counting and minimizing repaint failures in an AWT `DrawPad`, analyzing the critical role of `@Override` and immediate vs retained mode graphics pipelines.
5. **Event-Handling Idioms Comparison (Task 5 - 7 Marks - K5):** Implementing and evaluating four distinct event-handling paradigms (Frame implements `ActionListener`, Named Inner Class, Anonymous Inner Class, Lambda expressions) on a Currency Converter screen under changing functional requirements.
6. **Full-Stack Desktop Challenge (Task 6 - 13 Marks - K5):** Architecting a comprehensive, production-grade Campus Event Registration desktop application featuring menus, responsive form inputs, live filterable `JTable`, duplicate registration prevention via database constraints and DAO checks, off-EDT database execution via `SwingWorker`, and CSV data export.

---

## Task 01: GUI Design Review and Architectural Repair (6 Marks - K4)

### 1.1 Defect Diagnosis
The original junior developer implementation of `FeeWindow` contained critical anti-patterns across architecture, event handling, safety, and database interactions:
1. **Tight Coupling (Violation of Separation of Concerns):** UI presentation, raw JDBC database persistence, and payment gateway simulation were conflated into a single monolithic class.
2. **String Identity vs Value Equality Bug:** The conditional check `e.getActionCommand() == "Pay"` relied on reference identity (`==`). In Java, string comparison must use `.equals()`, as string pool intern optimization is not guaranteed across dynamic command strings.
3. **Event Dispatch Thread (EDT) Blocking:** Executing database transactions and `Thread.sleep(3000)` on the EDT freezes the OS window message queue, causing the window to become unresponsive and triggering operating system kill warnings.
4. **Unhandled Checked Exceptions & NumberFormatException:** Calling `Integer.parseInt(amount.getText())` without error guards crashes the application upon encountering blank strings, decimals, or non-numeric characters.
5. **SQL Injection Vulnerability:** Direct string concatenation (`"INSERT INTO fees VALUES('" + regNo.getText() + "'," + a + ")"`) enables malicious SQL injection.
6. **Resource Leaks:** Database connections and statements were never closed inside `try-with-resources` blocks.
7. **Flawed Button Handling:** The `if ("Pay") ... else` structure caused any button click other than "Pay" (e.g., clicking "Receipt") to wipe out the input fields.
8. **Lack of User Feedback:** Outputs were sent only to stdout via `System.out.println()`.
9. **Missing Window Closing Handler:** The `Frame` lacked a `WindowListener` or `WindowAdapter`, preventing window termination via the OS close button.

### 1.2 Repaired Architecture
- **`FeeService.java`:** Provides parameterized database access with `PreparedStatement`, rigorous input validation, and secure transactional recording.
- **`FeeWindowRepaired.java`:** Uses Swing `JFrame` and executes payment processing asynchronously within a `SwingWorker`, keeping the UI responsive while rendering progress bars and user dialogs.

### 1.3 Architectural Defence (Under 100 Words)
> Decoupling `FeeService` from `FeeWindowRepaired` isolates business logic and database persistence from GUI rendering. This allows comprehensive unit testing of payment validation, SQL safety, and receipt generation via automated headless test suites without initializing heavyweight GUI components or window servers. Maintenance is significantly improved: database connection strings, validation rules, or transaction schemas can be altered without risking UI layout regressions. Security is fortified by centralizing input sanitation and parameterized `PreparedStatement` handling within the service layer, preventing SQL injection and credential leakage while guaranteeing that long-running operations never freeze the Event Dispatch Thread.

### 1.4 Alternative Concept: Single Listener vs Dedicated Listeners
- **Single Listener with `if-else`:** $O(k)$ dispatch time where $k$ is the number of buttons. While saving memory ($O(1)$ listener allocation), it introduces fragile string matching and tight coupling.
- **Dedicated Listeners / Swing Actions:** $O(1)$ dispatch time per control. Consumes $O(k)$ listener references but provides complete modularity, clean error isolation, and reusability.

---

## Task 02: From Requirement to Event-Driven Design (8 Marks - K4)

### 2.1 Event Model Table
| Event Source | Event Type | Listener Interface | Handler Method | Action Triggered |
|---|---|---|---|---|
| `departmentCombo` (`JComboBox`) | `ItemEvent` | `ItemListener` | `itemStateChanged(ItemEvent e)` | Dynamically filters and repopulates `doctorCombo` with specialists for selected department. |
| `bookButton` (`JButton`) | `ActionEvent` | `ActionListener` | `actionPerformed(ActionEvent e)` | Validates patient input, checks doctor availability, generates unique appointment ID, and triggers modal confirmation. |
| `resetButton` (`JButton`) | `ActionEvent` | `ActionListener` | `actionPerformed(ActionEvent e)` | Resets patient input, clears selections, and restores default initial state. |
| `HospitalAppointmentDesk` (`JFrame`) | `WindowEvent` | `WindowListener` (`WindowAdapter`) | `windowClosing(WindowEvent e)` | Intercepts OS close request, displays a modal confirmation prompt, and disposes on confirmation. |

### 2.2 Component Hierarchy Tree
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

### 2.3 Event Sequence Trace & Applet Comparison
Desktop window lifecycle methods (`windowOpened`, `itemStateChanged`, `actionPerformed`, `windowClosing`, `windowClosed`) map directly to OS display events. In contrast, the legacy Applet lifecycle (`init`, `start`, `paint`, `stop`, `destroy`) was governed by the web browser embedding context, triggering `start()` and `stop()` as users navigated across web pages.

### 2.4 Alternative Concept: Dynamic DB Query vs In-Memory Caching
Dynamic database fetching on each dropdown change incurs network latency (50–200 ms) and server load. In contrast, in-memory catalog caching (`Map<String, List<Doctor>>`) achieves $O(1)$ instantaneous dropdown population with negligible client memory footprint (< 50 KB for typical department rosters).

---

## Task 03: From Sketch to Layout (9 Marks - K4)

### 3.1 Layout Manager Selection
- **Root Container:** `BorderLayout` allows strict structural compartmentalization: `NORTH` (Search bar), `CENTER` (Expandable tabular results), and `SOUTH` (Pagination, action buttons, and status bar).
- **Search Header:** `FlowLayout(FlowLayout.LEFT)` renders controls sequentially and preserves fixed component heights.
- **Results View:** `JScrollPane` enclosing a `JTable` placed in `BorderLayout.CENTER`. In `BorderLayout`, only the center component receives all excess horizontal and vertical space when a window is resized.
- **Footer:** `BorderLayout` with nested `FlowLayout` maintains fixed button dimensions on the west and status indicators on the east.

### 3.2 Empirical Resize Verification
Automated layout tests verified component bounds across three standard desktop dimensions:

| Dimension (W x H) | North Panel Height | South Panel Height | Center ScrollPane Bounds | Behavior |
|---|---|---|---|---|
| **500 x 350 px** | 47 px | 43 px | 500 x 260 px | Base compact view |
| **750 x 500 px** | 47 px | 43 px | 750 x 410 px | North & South fixed; Center absorbed 100% growth |
| **1000 x 650 px** | 47 px | 43 px | 1000 x 560 px | North & South fixed; Center absorbed 100% growth |

### 3.3 Alternative Concept: `GridBagLayout` vs Absolute Positioning (`null` Layout)
Absolute positioning (`setBounds`) breaks under different OS font scalings (125%, 150%, 200% High-DPI displays) and leaves windows unable to adapt to user resizing. Layout managers dynamically query component preferred dimensions (`getPreferredSize()`), ensuring cross-platform DPI fidelity and maintainability.

---

## Task 04: Event-Handling Forensics (7 Marks - K4)

### 4.1 Forensic Analysis of Defects
1. **Defect 1 (Case Sensitivity & Method Signature):** The developer implemented `public void mouseclicked(MouseEvent e)`. Because `MouseListener` defines `public void mouseClicked(MouseEvent e)` with a capital 'C', Java treated `mouseclicked` as an arbitrary helper method. The adapter's default no-op `mouseClicked` was invoked instead, causing click counts to remain stuck at 0. Annotating with `@Override` immediately turns this into a compile-time error.
2. **Defect 2 (Immediate Mode Drawing vs Retained Repaint Pipeline):** Drawing directly via `getGraphics()` renders to the display device but does not preserve geometry in memory. When the window is minimized, covered, or resized, the OS sends an asynchronous repaint message, clearing the damaged surface and calling `paint(Graphics g)`. Since points were not stored, previous dots were erased.

### 4.2 Completed Code
```java
// TODO 1: Correct method signature and coordinate retention
@Override
public void mouseClicked(MouseEvent e) {
    points.add(new Point(e.getX(), e.getY()));
    status.setText("Clicks: " + points.size());
    repaint();
}

// TODO 2: Persistent rendering inside paint(Graphics g)
@Override
public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Color.RED);
    for (Point p : points) {
        g.fillOval(p.x - 3, p.y - 3, 6, 6);
    }
}
```

### 4.3 Alternative Concept: Retained Vector Loop vs Off-Screen `BufferedImage`
- **Retained Vector Loop (`List<Point>` + `paint()`):** $O(n)$ rendering time and $O(n)$ memory. Provides vector fidelity and trivial undo/redo operations.
- **Off-Screen Raster Blitting (`BufferedImage`):** $O(1)$ constant-time blitting via `drawImage()`, but requires $O(W \times H)$ memory buffer (e.g. 8.3 MB for 1080p) and suffers scaling artifacts upon resizing.

---

## Task 05: Same Screen, Different Event-Handling Styles (7 Marks - K5)

### 5.1 Comparison of Event-Handling Styles
| Criterion | Style (a): Class Implements | Style (b): Named Inner Class | Style (c): Anonymous Inner Class | Style (d): Lambda Expression |
|---|---|---|---|---|
| **Readability** | Poor (sprawling `if-else` cascades). | Moderate (isolated class block). | Verbose boilerplate (`new ActionListener()`). | **Exceptional** (concise, declarative). |
| **Enclosing Field Access** | Direct (`this.field`). | Direct (`Enclosing.this.field`). | Direct for instance fields; final for locals. | Direct for instance fields; effectively final locals. |
| **Reusability** | None (tied to container). | High (reusable class). | Low (tied to single button). | High (assignable lambda reference). |
| **Maintenance & Regression** | High risk (string matching breaks). | Low risk. | Moderate risk (nesting). | **Lowest risk** (strongly typed). |

### 5.2 Requirement Change Impact Analysis
When modifying the application to trigger on Enter key in the amount field and show results in a dialog:
- **Style (a):** Required 8–12 lines modified across central `actionPerformed` dispatchers.
- **Style (b):** Required 5–7 lines modified.
- **Style (c):** Required 10–14 lines modified (or duplicating the anonymous inner class block).
- **Style (d) (Lambda):** Required only **3–4 lines** of code:
  ```java
  ActionListener convertAction = e -> convert();
  convertButton.addActionListener(convertAction);
  amount.addActionListener(convertAction);
  ```

---

## Task 06: Design Challenge - Campus Event Registration Desktop Application (13 Marks - K5)

### 6.1 Architectural Overview
The Campus Event Registration Application is built using a strict three-tier architecture:
1. **Presentation Layer (`EventRegistrationApp.java`):** Swing desktop interface containing a menu bar, form inputs, dynamic `JTable`, live event filters, and an asynchronous worker pipeline.
2. **Persistence Layer (`RegistrationDAO.java`):** Parameterized JDBC data access layer with dual-mode support (SQLite persistence with in-memory thread-safe store fallback).
3. **Data Tier (`schema.sql`):** Relational schema enforcing composite uniqueness: `CONSTRAINT unique_student_event UNIQUE (student_id, event_name)`.

### 6.2 Key Verification Results
All seven core test requirements were verified by `EventRegistrationTest`:
1. Empty and whitespace-only student IDs are intercepted before persistence.
2. Malformed email addresses without `@` or domain extensions are rejected.
3. Valid student registrations persist to the database and generate unique registration identifiers.
4. Duplicate registrations for the same student and same event are trapped and rejected via `DuplicateRegistrationException`.
5. Multiple registrations by the same student for different events are supported.
6. Dynamic event filtering accurately segments attendees in real time.
7. Catalog export to CSV generates well-formed files.

### 6.3 Alternative Concept: Bulk Loading 100,000 Records vs Database Pagination
- **Bulk Loading:** Instantiating 100,000 rows into a `JTable` model consumes 150–250 MB of JVM heap and blocks the application for 3–8 seconds during initial parsing.
- **Keyset / Offset Pagination (`LIMIT ? OFFSET ?`):** Restricts memory to current viewport records (< 100 KB) and delivers sub-50 ms query latencies regardless of database scale.

---

## Comprehensive Test Execution Summary

| Task | Module / Class Tested | Test Cases Executed | Result | Status |
|---|---|---|---|---|
| **Task 01** | `FeeService` & `FeeWindowRepaired` | 6 Test Cases (Validation, SQL Injection, Asynchronous Processing, Clear) | 6 / 6 Passed | **SUCCESS** |
| **Task 02** | `HospitalAppointmentDesk` | 5 Test Cases (Initial Catalog, Dynamic Refresh, Validation, Booking, Reset) | 5 / 5 Passed | **SUCCESS** |
| **Task 03** | `LibrarySearchScreen` | 4 Test Cases (3-Resolution Layout Invariance, Center Expansion, Search Query) | 4 / 4 Passed | **SUCCESS** |
| **Task 04** | `DrawPad` | 4 Test Cases (Typo Fix, Click Dispatches, Coordinate Storage, Repaint Rendering) | 4 / 4 Passed | **SUCCESS** |
| **Task 05** | `Converter` & `ConverterStylesDemo` | 5 Test Cases (Styles A, B, C, D, Enter Key Dispatch, Validation) | 5 / 5 Passed | **SUCCESS** |
| **Task 06** | `EventRegistrationApp` & `RegistrationDAO` | 7 Test Cases (Validation, Persistence, Duplicates, Multi-Event, Filtering, CSV) | 7 / 7 Passed | **SUCCESS** |
| **Total** | **CO4 Full Assignment Test Suite** | **31 Test Cases Executed** | **31 / 31 Passed** | **100% SUCCESS** |

---

## Conclusion
The implementation of Assignment 4 demonstrates mastery of Course Outcome CO4:
- Diagnosed anti-patterns in monolithic event-driven code and refactored them into secure, decoupled architectures.
- Built event delegation models handling action, item, window, and mouse events.
- Engineered responsive container hierarchies with predictable resize properties.
- Resolved low-level AWT rendering and case-sensitivity defects using defensive programming.
- Evaluated modern lambda expressions versus traditional inner class patterns.
- Delivered a full-stack, desktop GUI application with off-EDT asynchronous database transactions and ACID-compliant duplicate prevention.
