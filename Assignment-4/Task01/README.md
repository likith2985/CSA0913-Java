# Task 01: GUI Design Review and Architectural Repair

## 1. Defect Diagnosis (Task 1.1)
The original `FeeWindow` implementation exhibited serious anti-patterns and architectural defects:
1. **Violation of Separation of Concerns (Coupling):** UI presentation, raw JDBC database access, and payment business logic were conflated inside a single GUI class (`FeeWindow`).
2. **String Identity vs Equality Bug:** The check `e.getActionCommand() == "Pay"` used reference identity (`==`) instead of value equality (`.equals("Pay")`), which can fail depending on intern pool conditions.
3. **Blocking Event Dispatch Thread (EDT):** Network calls (`DriverManager.getConnection`), SQL execution, and `Thread.sleep(3000)` executed directly on the EDT. This causes the entire UI window to freeze and trigger OS "Not Responding" warnings.
4. **Unhandled Exceptions & Blank Input:** `Integer.parseInt(amount.getText())` throws unhandled `NumberFormatException` when passed empty, decimal, or invalid strings.
5. **SQL Injection Vulnerability:** String concatenation `"INSERT INTO fees VALUES('" + regNo.getText() + "'," + a + ")"` allows malicious SQL injection payloads.
6. **Resource Leaks:** Neither the `Connection` nor the `Statement` was closed inside `try-with-resources` or `finally` blocks, exhausting database connection pools.
7. **Flawed Button Handling:** The `if ("Pay") ... else` structure meant clicking any button other than "Pay" (such as "Receipt") blindly executed the clearing branch.
8. **No Visual Feedback:** Feedback was printed only to console (`System.out.println`), giving zero feedback to the end user in the GUI.
9. **Missing Window Closing Handler:** The `Frame` lacked a `WindowListener` or `WindowAdapter`, meaning clicking the OS close button (X) failed to close the window or terminate the JVM.

---

## 2. Decoupled Architecture & Repair (Task 1.2)
The repaired system separates responsibilities into two distinct layers:
- **`FeeService.java` (Business & Data Layer):**
  - Validates registration format and non-negative numeric fee amounts.
  - Employs `PreparedStatement` to prevent SQL injection.
  - Manages database lifecycles with `try-with-resources`.
  - Simulates payment gateway processing off the EDT.
- **`FeeWindowRepaired.java` (Presentation Layer):**
  - Built on Swing `JFrame` with responsive layout and proper `EXIT_ON_CLOSE` semantics.
  - Uses `SwingWorker` for asynchronous background payment processing, keeping the EDT completely responsive.
  - Displays interactive visual feedback (status labels, progress bar, error/success dialogs).
  - Employs dedicated button listeners to avoid faulty shared `if-else` branching.

---

## 3. Architecture Defence (Task 1.3)
> **Defence (98 words):**  
> Decoupling `FeeService` from `FeeWindowRepaired` isolates business logic and database persistence from GUI rendering. This allows comprehensive unit testing of payment validation, SQL safety, and receipt generation via automated headless test suites without initializing heavyweight GUI components or window servers. Maintenance is significantly improved: database connection strings, validation rules, or transaction schemas can be altered without risking UI layout regressions. Security is fortified by centralizing input sanitation and parameterized `PreparedStatement` handling within the service layer, preventing SQL injection and credential leakage while guaranteeing that long-running operations never freeze the Event Dispatch Thread.

---

## 4. Alternative Concept Exploration: Event Dispatch Trade-Offs
- **Single Listener with `if-else` Branching:**
  - *Time Complexity:* $O(k)$ linear branching where $k$ is the number of buttons/action commands.
  - *Memory:* $O(1)$ allocation (only 1 listener object instantiated for all controls).
  - *Drawbacks:* Brittle string matching, tightly coupled code, error-prone when buttons are added or renamed.
- **Dedicated Listeners / Swing Actions:**
  - *Time Complexity:* $O(1)$ direct dispatch per control.
  - *Memory:* $O(k)$ listener objects instantiated.
  - *Benefits:* Clean encapsulation, modular reuse, isolated error boundaries, and zero risk of command collision.
