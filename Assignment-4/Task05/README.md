# Task 05: Same Screen, Different Event-Handling Styles

## 1. Architectural Review of the 4 Event-Handling Styles (Task 5.1)

### Style (a): Frame Implements `ActionListener`
- The entire GUI container class implements `ActionListener` and overrides a single monolithic `actionPerformed(ActionEvent e)`.
- Multiple sources are disambiguated using string comparisons (`e.getActionCommand().equals(...)`) or source checks (`e.getSource() == convertButton`).

### Style (b): Named Inner Class
- A dedicated inner class (e.g. `ConvertHandler implements ActionListener`) is declared within the enclosing frame.
- Encapsulates handler state and can be instantiated multiple times if needed.

### Style (c): Anonymous Inner Class
- An inlined, nameless implementation of `ActionListener` defined directly inside the `.addActionListener(new ActionListener() { ... })` call.
- Pre-Java 8 standard idiom for localized event handling.

### Style (d): Lambda Expression
- Modern functional idiom leveraging Java 8+ Single Abstract Method (SAM) interface:
  ```java
  ActionListener convertAction = e -> convert();
  convertButton.addActionListener(convertAction);
  amount.addActionListener(convertAction);
  ```

---

## 2. Requirement Change Impact Analysis (Task 5.2)
**Change Requirement:** *"Also trigger conversion when the user presses Enter inside the `amount` text field, and display the result in a dialog (`JOptionPane.showMessageDialog`)."*

| Event-Handling Style | Modifications Required | Approximate Lines of Code Changed |
|---|---|---|
| **Style (a): Frame Implements** | 1. Add `amount.addActionListener(this);`<br>2. Update `actionPerformed(e)` `if-else` or `switch` tree to check `e.getSource() == amount`.<br>3. Add `JOptionPane` call. | **8–12 lines** (Modifies central event dispatcher). |
| **Style (b): Named Inner Class** | 1. Add `amount.addActionListener(new ConvertHandler());`<br>2. Update `ConvertHandler.actionPerformed` with `JOptionPane`. | **5–7 lines**. |
| **Style (c): Anonymous Inner Class** | 1. If duplicating inline: copy entire 8-line anonymous block to `amount.addActionListener(...)`.<br>2. If refactoring: extract listener reference or method, plus update dialog. | **10–14 lines** (or code duplication). |
| **Style (d): Lambda Expression** | 1. Store lambda in `ActionListener action = e -> convert();`<br>2. Add single line: `amount.addActionListener(action);`<br>3. Update `convert()` with `JOptionPane`. | **3–4 lines** (Lowest impact, zero duplication). |

---

## 3. Comprehensive Style Comparison Matrix (Task 5.3)

| Criterion | Style (a): Class Implements | Style (b): Named Inner Class | Style (c): Anonymous Inner Class | Style (d): Lambda Expression |
|---|---|---|---|---|
| **Readability** | Poor. Business logic is separated from component declarations; clutter from sprawling `if-else` cascades. | Moderate. Handler logic is separated into an inner class, which is clean for complex logic. | Moderate to Verbose. High boilerplate syntax (`new ActionListener() { ... }`). | **Exceptional.** Concise, declarative, and colocated with component definition. |
| **Access to Enclosing Fields** | Direct access to all enclosing instance variables (`this.amount`). | Direct access to outer class members (`EnclosingClass.this.amount`). | Direct access to enclosing instance variables; access to local variables restricted to `final` or effectively final. | Direct access to instance variables; closes over effectively final local variables with minimal syntax. |
| **Handler Reusability** | None. Conflates UI container lifecycle with listener dispatch. | **High.** Can be instantiated across multiple buttons or subcomponents within the container. | Low. Tied strictly to the single attachment site unless assigned to a variable. | **High.** A single lambda reference can be registered to multiple components (button, menu item, text field). |
| **Maintainability & Regression Risk** | High Risk. Modifying or adding one button can accidentally break existing `if-else` string matches. | Low Risk. Isolated class scope; compiler verifies method signatures. | Moderate Risk. Deeply nested indentation can obscure business logic. | **Lowest Risk.** Strongly typed, refactoring-friendly, zero string command matching. |

### Architectural Recommendation for Large Enterprise Applications
For enterprise desktop applications:
- **Small to Medium Action Triggers:** **Style (d) (Lambda / Method References)** is the definitive standard, minimizing boilerplate while preserving type safety.
- **Complex Multi-Control Operations (Menu, Toolbar, Button, Key Accelerator):** Use **Swing `Action` (`AbstractAction`)**. A single `Action` instance encapsulates the command logic, display label, icon, enabled/disabled state, and tooltip across menus, toolbars, and shortcut keys simultaneously.

---

## 4. Alternative Concept Exploration: Method References vs Shared Swing Actions
- **Method Reference (`this::convert`):**
  - Reduces syntactic overhead even further: `convertButton.addActionListener(e -> convert())` becomes `convertButton.addActionListener(e -> convert())` or if signature matches `(ActionEvent) -> void`, `convertButton.addActionListener(this::onConvert)`.
- **Shared Swing `Action` (`AbstractAction`):**
  - Enables centralized state synchronization: calling `action.setEnabled(false)` immediately disables the toolbar icon, menu item, and dialog button in a single atomic call.
