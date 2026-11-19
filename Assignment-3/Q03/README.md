# Q3 – Resource Safety, Debugging and Robust Code

## 1. Problem Context – Daily Sales Report Generator
A retail system aggregates daily sales records across hundreds of regional stores. Occasionally, the batch job terminates with `"java.io.IOException: Too many open files"`, leaving reports incomplete and corrupting system file descriptors.

---

## 2. Student A – Debugging and Experimental Leak Analysis

### 2.1 The Point at Which Manual Closing Leaks
In the manual approach:
```java
TrackedFileReader reader = new TrackedFileReader(storeFile);
// Read and parse lines
double amount = Double.parseDouble(line); // EXCEPTION THROWN HERE!
reader.close(); // UNREACHABLE!
```
* **Step-Through Diagnosis:** When `store3_corrupt.csv` encounters invalid non-numeric data (`"CORRUPT_DATA"`), `Double.parseDouble()` throws an unchecked `NumberFormatException`.
* Control immediately jumps out of the `for` loop to the outer catch block.
* The instruction `reader.close()` is **bypassed completely**.
* **Observed Open Handle Count:** The manual version left **1 active open file handle** leaked in memory. Over 1,000 stores, this exhausts the OS file descriptor table limit (POSIX default: 1024 handles).
* Furthermore, processing halted prematurely: `store4.csv` was never processed.

---

## 3. Student B – Design, Correctness & Try-With-Resources Guarantee

### 3.1 How Try-With-Resources Guarantees Closing
* Any class implementing `java.lang.AutoCloseable` can be instantiated inside the try-with-resources header:
  ```java
  try (TrackedFileReader reader = new TrackedFileReader(storeFile)) { ... }
  ```
* **Compiler Guarantee:** The Java compiler translates this into an invisible, bulletproof bytecode structure containing nested `try-finally` blocks.
* Even if an unhandled `RuntimeException` or `Error` occurs inside the body, the runtime invokes `close()` **before** leaving the block.
* **Closing Order:** Multiple resources declared in the header are closed in **reverse order of declaration** (LIFO: Last-In, First-Out), ensuring that dependent wrapping streams are flushed before closing underlying streams.
* **Leak Count:** **0 open handles** (Perfect resource reclamation).

### 3.2 Suppressed Exceptions Mechanics
* **The Problem in Legacy Code:** If an exception occurs in the `try` block, and *another* exception occurs inside the `finally` block during `close()`, the `finally` exception overrides and completely swallows the primary business exception!
* **The Try-With-Resources Solution:**
  * The primary exception thrown inside the `try` block is preserved as the main exception.
  * Any exception thrown by `close()` is appended to the primary exception as a **Suppressed Exception**.
  * Accessible via `e.getSuppressed()` without losing either diagnostic trail.

---

## 4. Joint Engineering Decision

### When is Try-With-Resources NOT Sufficient on Its Own?
1. **Non-AutoCloseable Legacy Handles:** Third-party native C libraries or JNI wrappers that do not implement `AutoCloseable` require explicit lifecycle wrappers.
2. **Transactional State Rollback:** Closing a database connection or message queue channel does not automatically rollback uncommitted dirty transactions; explicit application-level compensation logic is still needed.
3. **Slow Asynchronous Resources:** Try-with-resources closes resources synchronously on thread exit. Long-running asynchronous I/O or background worker channels require dedicated shutdown hooks or pooling managers.

---

## 5. Alternative Concept and Complexity Analysis

| Approach | Max Open File Handles on Failure ($f$ files) | Time Overhead | Resilience to Corrupt Files |
| :--- | :--- | :--- | :--- |
| **No Closing** | $f$ file descriptors leaked until JVM exit | Negligible | Crashes OS process with "Too many open files" |
| **Manual `try-finally`** | Up to $f$ leaked descriptors | High boilerplate | Requires cumbersome nested try-catch blocks in finally |
| **`try-with-resources`**| **Strictly $\le 1$ open handle** | Minimal | **Guaranteed automatic closing; resilient batch completion** |
