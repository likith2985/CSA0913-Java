# Q2 – User-Defined Exceptions and Propagation in an Online Examination Portal

## 1. Problem Context – Online Examination Portal
An academic examination portal enforces strict compliance rules across three decoupled architectural tiers:
* **Presentation / Web Controller Layer (`ExamController`)**
* **Business Service Layer (`ExamService`)**
* **Persistence Storage Layer (`ExamStorage`)**

A submission must be rejected if:
1. The examination submission window has closed.
2. The student is not registered for the course.
3. The answer script file exceeds the quota limit.

---

## 2. Student A – Exception Hierarchy & Propagation Architecture

### 2.1 Exception Class Hierarchy Diagram
```
                     java.lang.Exception (Checked)
                                  │
                          ExamException
                   ┌──────────────┼──────────────┐
                   │              │              │
          ExamClosedException  Unregistered   SubmissionStorage
                                StudentException    Exception
                                                    (Chained: wraps IOException)
```

### 2.2 Rationale: Why These Exceptions are Checked
* All three failure conditions represent **anticipated, recoverable operational contingencies** in a real-world web application.
* Making them **Checked Exceptions** (`extends Exception`) forces the compiler to ensure that the calling layers explicitly anticipate, catch, and handle them (e.g. converting them to structured HTTP 403, 401, or 500 error responses with human-friendly messages).
* Unchecked exceptions (`RuntimeException`) are reserved for programmer logic defects (e.g. `NullPointerException`, array index bugs) that should not be caught.

### 2.3 Exception Propagation Path
```
[Storage Layer: ExamStorage]
   │  Detects (sizeKb > 500) -> Catches low-level raw IOException
   │  Wraps into new SubmissionStorageException(message, ioException)
   ▼
throws SubmissionStorageException
   │
   ▼
[Service Layer: ExamService]
   │  Declares 'throws ExamException' in method signature
   │  Propagates exception upwards without catching (preserves abstraction)
   ▼
throws ExamException
   │
   ▼
[Controller Layer: ExamController]
   │  Catches specific subtype: catch (SubmissionStorageException sse)
   │  Translates to HTTP 500 JSON response for the student UI
   ▼
[Student Web Client Screen]
   "HTTP 500: System storage rejected script. (Cause: Disk quota exceeded)"
```

---

## 3. Student B – Failure Scenarios & Chaining Evidence

### 3.1 Failure Scenarios & Test Matrix

| Test Scenario | Layer of Origin | Exception Thrown | Cause Preserved? | Student Facing Message |
| :--- | :--- | :--- | :---: | :--- |
| **Normal Submission** | None | None | N/A | HTTP 200: Answer script accepted. |
| **Late Submission** | `ExamService` | `ExamClosedException` | None | HTTP 403: Submission window closed. |
| **Unregistered Student**| `ExamService` | `UnregisteredStudentException`| None | HTTP 401: Student not registered. |
| **Storage Overflow** | `ExamStorage` | `SubmissionStorageException` | **YES (`IOException`)**| HTTP 500: Storage issue (Disk quota exceeded). |

### 3.2 What Information Is Lost if Re-thrown Without Cause?
If the exception is constructed without passing the cause:
```java
// ANTI-PATTERN: Cause lost!
throw new SubmissionStorageException("Could not store " + fileName);
```
* **Consequences:**
  1. The underlying root cause (`IOException: Disk quota exceeded`) is completely erased.
  2. The stack trace originating from disk hardware or low-level I/O streams is lost.
  3. When sysadmins or monitoring tools call `e.getCause()`, it returns `null`.
  4. Debugging requires blind guessing because the developer cannot tell whether the failure was due to disk full, file permissions denied, network NFS timeout, or path corruption.

---

## 4. Joint Conclusion: Why Domain Exceptions Beat Raw `IOException`

* **Abstraction Integrity:** The UI controller and student have no business knowing low-level operating system POSIX error codes or file paths.
* **Security Shielding:** Exposing raw `java.io.IOException` leaks internal server topology, mount paths (e.g. `/var/exams`), and database connection strings to potential attackers.
* **Separation of Concerns:** Wrapping into `SubmissionStorageException` allows the application to distinguish user-correctable errors from infrastructure outages.

---

## 5. Complete the Code Verification
File: `StorageDemo.java`

* **TODO 1 Implementation:**
  ```java
  // TODO 1: Wrap the IOException in a SubmissionStorageException, keeping the IOException as its cause
  throw new SubmissionStorageException("Could not store " + fileName, e);
  ```
* **Verified Program Output:**
  ```
  Saved answer01.pdf
  Error: Could not store answer02.pdf
  Cause: Disk quota exceeded
  ```
* **Inquiry Answer:** If `IOException` were **not** passed to the constructor, `e.getCause()` would return `null`. Evaluating `e.getCause().getMessage()` would immediately crash the application with a `NullPointerException`!

---

## 6. Alternative Concept: Checked vs Unchecked Custom Exceptions

| Dimension | Checked (`extends Exception`) | Unchecked (`extends RuntimeException`) |
| :--- | :--- | :--- |
| **Method Signatures** | Mandatory `throws ExamException` across all intermediate methods. | Clean signatures; no `throws` declarations required. |
| **Calling Code** | Compiler enforces `try-catch` blocks at build time. | Calling code can easily forget to handle errors, crashing the thread. |
| **Stack Trace Cost** | Constructing a `Throwable` captures the native execution frame stack (`fillInStackTrace()`), costing $\approx 1$–$3\,\mu\text{s}$ and allocating stack trace element objects. | Identical heap cost because `Throwable` captures stack traces regardless of checked/unchecked status unless overridden. |
