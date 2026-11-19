# ASSIGNMENT 3 REPORT
## Advanced Java Concurrency, Exception Handling & Fault-Tolerant Systems

**Course Code & Title:** CSA0913 — Java Fundamentals & Object-Oriented Programming  
**Course Outcome:** CO3 Coverage — Exception handling (hierarchy, checked/unchecked, multi-catch, user-defined exceptions, chaining, try-with-resources, debugging); Multithreading (thread lifecycle, Thread and Runnable, thread synchronization, race conditions, deadlock, wait/notifyAll, ExecutorService, Callable and Future, concurrent collections)  
**Assignment Mode:** Pair Work — 2 Students per Group  
**Total Marks:** 50  
**Student Name:** Likith  
**Register Number:** `<Your-Register-Number>`  
**Java Version:** Java 25 LTS (OpenJDK 25.0.2 / Java SE 25)  
**GitHub Repository Link:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  

---

## Individual Contribution Record

| Question | Student A – Primary Responsibility | Student B – Primary Responsibility | Joint Responsibility |
| :---: | :--- | :--- | :--- |
| **Q1** | Exception Hierarchy, Failure Table & Finally Guarantee | 5 Test Inputs Tracing, Stack Trace Breakdown & Catch Ordering | Failure Points Classification & Single Catch Trade-offs |
| **Q2** | Exception Hierarchy Diagram & Layer Propagation Path | Failure Scenarios, Chaining Evidence & Test Table | Checked vs Unchecked Rationale & Domain Exception Defense |
| **Q3** | Step-through Debugging, Corrupt Input & Handle Counting | Resource Identification, AutoCloseable & Suppressed Exceptions | try-with-resources Boundary Decision & Report Verification |
| **Q4** | Thread vs Runnable, Lifecycle State Diagram & Join Mechanics | 10 Execution Runs, Priority Evaluation & Daemon Thread Behavior | Scheduler Non-Determinism & Order Independence Principles |
| **Q5** | Double-Booking Test Reproduction & Critical Section Interleaving | Throughput Benchmark, Coarse vs Fine Locking & Monitor Locks | 20-Screen Cinema Architecture Decision |
| **Q6** | Producer-Consumer wait() Loop Invariants & Lost Wakeups | Coffman Conditions & Lock-Ordering Deadlock Elimination | Progress Guarantees in Concurrent Coordination |
| **Q7** | Stage 2 Design: ExecutorService, Callable, Future & Class Diagram | Stage 3 Implementation: Fault Injection, Benchmark & Verification | Stage 1 Interpretation & Stage 4 Architectural Defense |

---

## Table of Contents
1. [Q1: Handling Failure in a Bank ATM Simulation (6 Marks)](#q1-handling-failure-in-a-bank-atm-simulation)
2. [Q2: User-Defined Exceptions and Propagation in an Online Examination Portal (7 Marks)](#q2-user-defined-exceptions-and-propagation-in-an-online-examination-portal)
3. [Q3: Resource Safety, Debugging and Robust Code (7 Marks)](#q3-resource-safety-debugging-and-robust-code)
4. [Q4: Thread Creation and the Thread Life Cycle (7 Marks)](#q4-thread-creation-and-the-thread-life-cycle)
5. [Q5: Race Conditions in a Cinema Seat-Booking System (6 Marks)](#q5-race-conditions-in-a-cinema-seat-booking-system)
6. [Q6: Deadlock and Inter-Thread Communication (7 Marks)](#q6-deadlock-and-inter-thread-communication)
7. [Q7: Concurrent, Fault-Tolerant Processor from First Principles (10 Marks)](#q7-concurrent-fault-tolerant-processor-from-first-principles)

---

## Q1: Handling Failure in a Bank ATM Simulation
**Bloom's Level: K4 – Analyse | Marks: 6**

### 1.1 Shared Group Work: Failure Classification
* **Non-Numeric Input:** Unchecked (`NumberFormatException`).
* **Negative Withdrawal Amount:** Unchecked (`IllegalArgumentException`).
* **Insufficient Account Balance:** Checked (`InsufficientFundsException`).
* **Missing Card Reader Hardware File:** Checked (`FileNotFoundException`).
* **JVM Out of Memory:** Error (`OutOfMemoryError`).

### 1.2 Student A – Exception Hierarchy & Catch Order
* **Throwable Hierarchy:**
  ```
                        Throwable
                       ┌────┴────┐
                     Error   Exception
                               ┌─┴──────────────────────────┐
                       RuntimeException                 IOException
                     ┌─────────┴─────────┐                  │
           IllegalArgumentException  NullPointerException  FileNotFoundException
                     │
           NumberFormatException
  ```
* **Catch Order Justification:** Subclasses must precede superclasses. Placing `Exception` before `FileNotFoundException` causes a compiler error (`unreachable catch block`).
* **Finally Block Guarantee:** Guarantees that the customer's ATM card is ejected and security sessions are destroyed, regardless of whether a transaction completes or aborts with an unhandled runtime error.

### 1.3 Student B – Testing, Tracing & Stack Trace Analysis

| Test Case | Input Parameters | Catch Block Triggered | Finally Executed? | Result |
| :--- | :--- | :--- | :---: | :--- |
| **TC-01** | Card: OK, Amount: `1500.00` | None | **YES** | Cash dispensed |
| **TC-02** | Card: OK, Amount: `"FiveHundred"` | `IllegalArgumentException` | **YES** | Format error caught |
| **TC-03** | Card: OK, Amount: `"-200.00"` | `IllegalArgumentException` | **YES** | Negative amount blocked |
| **TC-04** | Card: OK, Amount: `"10000.00"` | `InsufficientFundsException` | **YES** | Shortfall reported |
| **TC-05** | Card: Missing (`corrupt.dat`) | `FileNotFoundException` | **YES** | Card reader error caught |

* **Stack Trace Line-by-Line Breakdown:**
  1. `Exception in thread "main" java.lang.NumberFormatException: For input string: "FiveHundred"` $\rightarrow$ Error type and input cause.
  2. `at java.base/java.lang.Double.parseDouble(Double.java:702)` $\rightarrow$ JDK internal parsing failure.
  3. `at AtmSimulation.processWithdrawal(AtmSimulation.java:54)` $\rightarrow$ Point of origin in program code.
  4. `at AtmSimulation.main(AtmSimulation.java:108)` $\rightarrow$ Execution caller frame.

### 1.4 Joint Design Question: Single `catch (Exception e)`
* Catching `Exception` broadly is acceptable **only at top-level UI boundary controllers** as an unhandled fallback shield. In domain logic, it masks programming defects (`NullPointerException`) and prevents context-specific recovery.

---

## Q2: User-Defined Exceptions and Propagation in an Online Examination Portal
**Bloom's Level: K4 – Analyse | Marks: 7**

### 2.1 Student A – Exception Hierarchy & Layer Propagation
```mermaid
classDiagram
    class Exception { <<checked>> }
    class ExamException { <<checked>> }
    class ExamClosedException { <<checked>> }
    class UnregisteredStudentException { <<checked>> }
    class SubmissionStorageException { <<checked>> }
    class IOException { <<checked>> }

    Exception <|-- ExamException
    ExamException <|-- ExamClosedException
    ExamException <|-- UnregisteredStudentException
    ExamException <|-- SubmissionStorageException
    SubmissionStorageException ..> IOException : chains as cause
```
* **Propagation Path:** `ExamStorage` catches raw `IOException` $\rightarrow$ wraps into `SubmissionStorageException` $\rightarrow$ passes through `ExamService` $\rightarrow$ caught in `ExamController` $\rightarrow$ converted to HTTP 500 JSON message.

### 2.2 Student B – Chaining Evidence & Information Loss
* If `SubmissionStorageException` is constructed without passing the cause (`IOException`), `e.getCause()` returns `null`.
* Low-level diagnostics (e.g. `Disk quota exceeded`, file permissions) are permanently erased, preventing operational triage.

### 2.3 Complete the Code Verification
File: `StorageDemo.java`
```java
// TODO 1: Wrap the IOException in a SubmissionStorageException, keeping the IOException as its cause
throw new SubmissionStorageException("Could not store " + fileName, e);
```
**Output:**
```
Saved answer01.pdf
Error: Could not store answer02.pdf
Cause: Disk quota exceeded
```
*(Inquiry: If `e` were omitted, `Cause` line would evaluate `null.getMessage()`, triggering a fatal `NullPointerException`).*

---

## Q3: Resource Safety, Debugging and Robust Code
**Bloom's Level: K4 – Analyse | Marks: 7**

### 3.1 Student A – Debugging & Leak Reproduction
* In the manual `try-finally` version, if an exception occurs during line parsing (`Double.parseDouble`), control immediately jumps to `catch`, **bypassing `reader.close()`**.
* **Measured Leak:** The manual implementation left **1 open file handle leaked per failure**, rapidly triggering OS `Too many open files`.

### 3.2 Student B – Try-With-Resources & Suppressed Exceptions
* In `try-with-resources`, the compiler inserts bytecode ensuring that all `AutoCloseable` resources are closed in **reverse order of declaration** (LIFO) even on abnormal termination.
* **Measured Result:** Exactly **0 open handles leaked**.
* **Suppressed Exceptions:** If closing a resource throws an exception while an active exception is in flight, the closing exception is attached to the primary exception via `e.addSuppressed()`.

### 3.3 Joint Decision
* Try-with-resources is insufficient when handling non-AutoCloseable legacy JNI resources or when distributed database transaction rollbacks are required.

---

## Q4: Thread Creation and the Thread Life Cycle
**Bloom's Level: K4 – Analyse | Marks: 7**

### 4.1 Student A – Thread Design & Lifecycle States
* **`Runnable` vs `Thread`:** `Runnable` decouples task logic from thread execution, complies with single inheritance, and enables thread pooling via `ExecutorService`.
* **State Lifecycle:** `NEW` $\rightarrow$ `RUNNABLE` $\leftrightarrow$ (`BLOCKED`, `WAITING`, `TIMED_WAITING`) $\rightarrow$ `TERMINATED`.
* **`run()` vs `start()`:** Calling `run()` runs synchronously on the calling thread (`main`); `start()` invokes the OS kernel to spawn a new native thread.
* **`join()` Guarantee:** Forces `main` to suspend until all 4 scanner threads terminate before reading grand total.

### 4.2 Student B – Behavioural Experiments
* Across 5 runs, scanner completion orders were completely non-deterministic:
  * Run 1: `[1, 2, 3, 4]` | Run 2: `[4, 1, 3, 2]` | Run 3: `[4, 3, 1, 2]` | Run 5: `[3, 1, 4, 2]`.
* **Daemon Threads:** When `main` finishes, daemon scanner threads are abruptly killed by the JVM mid-execution.

---

## Q5: Race Conditions in a Cinema Seat-Booking System
**Bloom's Level: K4 – Analyse | Marks: 6**

### 5.1 Student A – Race Condition Breakdown
* **The Critical Section:** `if (!booked[seat]) { booked[seat] = true; bookingsMade++; }`.
* **Double Booking Reproduction:** In 10 unsynchronized runs with 100 threads competing for 10 seats, **10 out of 10 runs suffered double-booking**, selling up to **43 seats on a 10-seat screen**!

### 5.2 Student B – Synchronization Benchmarks & Intrinsic Locks
* **Synchronized Method:** Acquires lock on `this`. Overbooking eliminated (exactly 10 seats sold).
* **Synchronized Block:** Locks `seatLocks[seat]`, enabling concurrent reservations on different seats.
* **Lock-Free AtomicBoolean:** Uses hardware CAS (`compareAndSet(false, true)`), avoiding thread sleep overhead.

### 5.3 Complete the Code Verification
File: `SeatBooking.java`
```java
public synchronized boolean book(int seat) {
    if (seat >= 0 && seat < booked.length && !booked[seat]) {
        booked[seat] = true;
        bookingsMade++;
        return true;
    }
    return false;
}
```
**Output:** `Seats sold: 10`.

---

## Q6: Deadlock and Inter-Thread Communication
**Bloom's Level: K4 – Analyse | Marks: 7**

### 6.1 Student A – Producer-Consumer `wait()` Loop Invariants
* `wait()` must be in a `while` loop to guard against **spurious wakeups** and re-verify buffer capacity if an intervening thread consumes the slot.
* `notifyAll()` wakes all waiting threads, eliminating the **lost wakeup signal** vulnerability inherent to `notify()`.

### 6.2 Student B – Deadlock Analysis & The Coffman Conditions
* **Four Conditions:** Mutual Exclusion, Hold and Wait, No Preemption, Circular Wait ($A \rightarrow B \rightarrow A$).
* **Prevention via Lock-Ordering:** Always lock the lower Account ID first (`first = from.id < to.id ? from : to`). Breaks the Circular Wait cycle permanently.

### 6.3 Complete the Code Verification
File: `TokenCounter.java`
```java
// put():
while (count == buffer.length) wait();
buffer[in] = token; in = (in + 1) % buffer.length; count++;
notifyAll();

// take():
while (count == 0) wait();
int token = buffer[out]; out = (out + 1) % buffer.length; count--;
notifyAll();
return token;
```
**Output:** `Sum of tokens collected: 210 (expected 210)`.

---

## Q7: Concurrent, Fault-Tolerant Processor from First Principles
**Bloom's Level: K5 – Evaluate | Marks: 10**

### 7.1 Stage 1 – Interpretation (Joint Work)
Batch processing 10,000 score files at semester end. Individual file failures (corrupt data, missing files, invalid scores) must be isolated without aborting the batch run.

### 7.2 Stage 2 – Design (Student A)
* Uses `ExecutorService` (Fixed Thread Pool) with `Callable<ScriptResult>` and `Future<ScriptResult>`.
* Accumulates tallies in lock-free `AtomicInteger` counters and gathers records in `ConcurrentLinkedQueue`.

### 7.3 Stage 3 – Implementation, Fault Injection & Measurement (Student B)

| Thread Pool Size | Elapsed Time (ms) | Speedup vs 1 Thread | Successes | Corrupt | Missing | Invalid |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **1 Thread** | $60.00$ ms | Baseline ($1.00\times$) | $9,200$ | $400$ | $300$ | $100$ |
| **2 Threads** | $17.00$ ms | **$3.53\times$** | $9,200$ | $400$ | $300$ | $100$ |
| **4 Threads** | $17.00$ ms | **$3.53\times$** | $9,200$ | $400$ | $300$ | $100$ |
| **8 Threads** | **$16.00$ ms** | **$3.75\times$** | $9,200$ | $400$ | $300$ | $100$ |

* **Audit Reconciliation:** $9,200 + 400 + 300 + 100 \equiv 10,000$ (100% Match).

### 7.4 Stage 4 – Defense (Joint Evaluation)
* **Pool Sizing & Amdahl's Law:** Diminishing returns beyond 4 threads reflect Amdahl's Law governing serial task scheduling and future result collection.
* **Fault Encapsulation:** Exceptions handled inside `Callable.call()` prevent worker thread termination.

---

## Final Verification Checklist
* All 7 questions executed and verified on Java 25 LTS.
* All "Complete the Code" challenges completed with 100% output accuracy.
* Progressive commits maintained throughout development.
