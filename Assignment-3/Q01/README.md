# Q1 – Handling Failure in a Bank ATM Simulation

## 1. Problem Context – ATM Withdrawal Failure Handling
An ATM reads user account information and withdrawal amount, checks account balance against requested funds, verifies hardware connectivity to the card reader, and dispenses cash.

---

## 2. Shared Group Work: Failure Classification

| Failure Point | Trigger Condition | Classification | Specific Java Class | Handled By |
| :--- | :--- | :--- | :--- | :--- |
| **Card Reader Failure** | Hardware data file missing / unreadable | **Checked Exception** | `FileNotFoundException` / `IOException` | Enclosing service layer |
| **Non-Numeric Input** | User enters letters (e.g., `"FiveHundred"`) | **Unchecked Exception** | `NumberFormatException` | Input validation layer |
| **Negative Amount** | User enters $\le 0$ amount (e.g., `"-200.00"`) | **Unchecked Exception** | `IllegalArgumentException` | Parameter verification |
| **Insufficient Balance**| Withdrawal exceeds current balance | **Checked Exception** | `InsufficientFundsException` (Custom) | Domain business logic |
| **JVM Memory Depletion**| ATM kiosk runs out of native memory | **Error** | `OutOfMemoryError` | Unrecoverable; crash kiosk |

---

## 3. Student A – Exception Hierarchy & Design

### 3.1 Throwable Hierarchy Relevant to ATM
```
                      Throwable
                     ┌────┴────┐
                     │         │
                   Error   Exception
                             ┌─┴──────────────────────────┐
                             │                            │
                     RuntimeException                 IOException
                   ┌─────────┴─────────┐                  │
         IllegalArgumentException  NullPointerException  FileNotFoundException
                   │
         NumberFormatException
```

### 3.2 Catch Block Order Justification
* Catch blocks **must be ordered from most specific subclass to most general superclass**.
* If a broader catch block (e.g. `catch (Exception e)`) is placed before a narrower subtype (e.g. `catch (FileNotFoundException e)`), the Java compiler emits a compile-time error:
  `"error: exception FileNotFoundException has already been caught"`.
* The JVM matches the first assignable catch block in textual top-down order.

### 3.3 What the `finally` Block Guarantees
* The `finally` block executes unconditionally:
  1. If `try` completes successfully.
  2. If an exception is caught and handled.
  3. Even if an unhandled exception propagates up the stack.
* In the ATM, the `finally` block guarantees that the user's physical ATM card is ejected, the user session token is invalidated, and the transaction audit log is flushed, preventing card theft or session hijacking.

---

## 4. Student B – Testing, Tracing & Stack Trace Analysis

### 4.1 Test Scenarios & Trace Matrix

| Test ID | Input Parameters | Catch Block Triggered | Finally Block Executed? | Business Outcome |
| :--- | :--- | :--- | :---: | :--- |
| **TC-01** | Card: Valid, Amount: `1500.00` | None (Success) | **YES** | Cash dispensed; balance updated. |
| **TC-02** | Card: Valid, Amount: `"FiveHundred"` | `IllegalArgumentException` | **YES** | Transaction aborted; prompt re-entry. |
| **TC-03** | Card: Valid, Amount: `"-200.00"` | `IllegalArgumentException` | **YES** | Negative amount rejected. |
| **TC-04** | Card: Valid, Amount: `"10000.00"` | `InsufficientFundsException` | **YES** | Overdraft blocked; shortfall reported. |
| **TC-05** | Card: Missing (`corrupted_slot.dat`) | `FileNotFoundException` | **YES** | Card reader error flagged. |

### 4.2 Stack Trace of an Uncaught Exception Explained Line by Line
```text
Exception in thread "main" java.lang.NumberFormatException: For input string: "FiveHundred"
    at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
    at java.base/java.lang.Double.parseDouble(Double.java:702)
    at AtmSimulation.processWithdrawal(AtmSimulation.java:54)
    at AtmSimulation.main(AtmSimulation.java:108)
```
* **Line 1:** Declares the thread (`main`), the exception class (`NumberFormatException`), and the error description message (`For input string: "FiveHundred"`).
* **Line 2:** The private factory method in `NumberFormatException` that constructed the exception object at line 67.
* **Line 3:** The JDK core library method `Double.parseDouble()` at line 702 that detected non-numeric characters.
* **Line 4:** The point of failure in our program code: `AtmSimulation.processWithdrawal()` at source line 54.
* **Line 5:** The caller frame: `AtmSimulation.main()` at line 108 that triggered the operation.

---

## 5. Joint Design Question: Single `catch (Exception e)` Trade-offs

* **Should the ATM catch every exception with a single `catch (Exception e)`?**
  * **No**, in general application logic. Catching `Exception` blindly masks programming bugs (such as `NullPointerException` or `IndexOutOfBoundsException`), treating coding defects as ordinary business validation failures. It also eliminates the ability to recover specifically (e.g. giving the user another chance to re-type a number vs calling maintenance for a missing hardware file).
* **When is it acceptable?**
  * Acceptable **only at the outermost boundary layer (e.g., the root main loop or web controller gateway)** as a fallback safety net to prevent the ATM process from crashing entirely, ensuring an error screen is displayed and a telemetry alert is dispatched to tech support.

---

## 6. Alternative Concept: Pre-checking vs Exception Handling

### Defensive Checking (`Scanner.hasNextDouble()` + Error Codes) vs Exceptions
* **Defensive Checking:**
  ```java
  if (!isValidNumber(input)) return -1;
  if (amount <= 0) return -2;
  if (amount > balance) return -3;
  ```
  * *Advantages:* Zero object allocation overhead; slightly faster CPU execution ($\approx 10$–$50$ ns).
  * *Disadvantages:* Causes "Error Code Pollution" where return values are conflated with data; callers can accidentally ignore return codes; does not support rich context (e.g. shortfall amounts, file paths).
* **Exception Handling:**
  * Separates normal business flow from error handling paths (Clean Code).
  * Forces callers to handle checked exceptions at compile time.
  * Incurred cost: Capturing the stack trace (`fillInStackTrace()`) costs $\approx 1$–$5\,\mu\text{s}$ and allocates an object, but in user-interactive ATM workflows (operating on human millisecond time scales), this overhead is completely negligible.
