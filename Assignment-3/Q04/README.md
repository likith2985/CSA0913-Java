# Q4 – Thread Creation and the Thread Life Cycle

## 1. Problem Context – Parallel Answer-Script Scanning
An examination cell processes exam scripts across 4 parallel scanner stations. Each station runs concurrently as an independent thread to maximize scanner throughput.

---

## 2. Student A – Thread Design & Lifecycle Architecture

### 2.1 Extending `Thread` vs Implementing `Runnable`

| Dimension | `extends Thread` | `implements Runnable` |
| :--- | :--- | :--- |
| **Inheritance Flexibility** | Locks class inheritance (Java single inheritance forbids extending another class). | High flexibility (class can extend domain models and implement other interfaces). |
| **Separation of Concerns** | Tightly couples task execution logic with thread management. | Decouples the work (`Runnable` task) from the execution vehicle (`Thread` or `ExecutorService`). |
| **Resource Reusability** | Cannot reuse the Thread instance for multiple tasks. | Same Runnable task can be submitted to ThreadPools repeatedly. |
| **Verdict** | Used only for specialized low-level thread customizations. | **Industry standard; strongly preferred in modern production code**. |

### 2.2 Thread Life-Cycle State Diagram
```
         [ NEW ]
            │  .start()
            ▼
      [ RUNNABLE ] <════════════════════════════════════════════════════╗
      (Ready/Running)                                                   ║
       │            │                              │                    ║
       │            │ .sleep(t)                    │ .wait()            ║
       │            │                              │                    ║
       ▼            ▼                              ▼                    ║
  [ BLOCKED ]   [ TIMED_WAITING ]             [ WAITING ]               ║
  (Waiting for  (Sleeping / timed join)       (Waiting on .join()/lock) ║
   monitor lock)    │                              │                    ║
       │            │ Timer expires                │ .notify() /        ║
       └────────────┴──────────────────────────────┴────────────────────╝
                          │
                          │ Thread finishes run() body
                          ▼
                    [ TERMINATED ]
```

### 2.3 Proof: Why Calling `run()` Does NOT Create a New Thread
* `start()` executes native OS system calls (`JVM_StartThread`), allocating a dedicated call stack frame and asking the OS kernel to schedule a new execution thread.
* Calling `run()` directly merely invokes an ordinary Java instance method synchronously on the **current calling thread** (`main`).
* **Experimental Evidence:**
  ```text
  Invoking directRunThread.run() on calling thread:
    [Thread] Station 99 finished scanning 5 scripts on [main]
  ```
  The thread name printed was `[main]`, proving zero thread concurrency was achieved.

### 2.4 How `join()` Guarantees Accurate Totals
* `stationThread.join()` causes the calling thread (`main`) to transition into `WAITING` state until `stationThread` completes execution and reaches `TERMINATED`.
* This guarantees that when `main` proceeds to calculate the grand total of scanned scripts, all 4 stations have completely finished writing their output counters, eliminating race conditions.

---

## 3. Student B – Behavioural Experiments & OS Scheduling

### 3.1 Non-Deterministic Completion Order Evidence
Across 5 consecutive runs with identical code:
* **Run 1:** `[Station_1, Station_2, Station_3, Station_4]`
* **Run 2:** `[Station_4, Station_1, Station_3, Station_2]`
* **Run 3:** `[Station_4, Station_3, Station_1, Station_2]`
* **Run 4:** `[Station_2, Station_1, Station_3, Station_4]`
* **Run 5:** `[Station_3, Station_1, Station_4, Station_2]`

### 3.2 Thread Priorities (`setPriority()`)
* Thread priority values ($1$ to $10$) serve merely as **hints** to the underlying operating system thread scheduler.
* Modern preemptive operating systems (Windows, Linux CFS) employ sophisticated multi-level feedback queues, thread aging, and dynamic priority boosting. A priority-10 thread is not guaranteed to finish before a priority-1 thread; relying on thread priorities for program correctness is a critical design flaw.

### 3.3 Daemon Thread Mechanics
* User threads prevent the JVM from shutting down; daemon threads do not.
* When the last user thread (`main`) terminates, the JVM abruptly kills all running daemon threads without executing remaining code or `finally` blocks.
* Daemon threads are suited only for background housekeeping (e.g., GC helpers, heartbeat timers), never for reliable transactional work like saving scanned exam scripts.

---

## 4. Joint Conclusion: Why Multithreaded Output Order Varies

* **Preemptive Time-Slicing:** The OS scheduler distributes CPU cores across thousands of system threads using fractional quantum time slices. Variations in cache misses, hardware interrupts, and context switches cause threads to finish in non-deterministic orders.
* **Core Rule of Concurrent Programming:** A correct concurrent program **must never depend on the execution order or interleaving of threads**. Correctness must be enforced through synchronization primitives, latches (`join()`, `CountDownLatch`), or concurrent queues.

---

## 5. Alternative Concept and Complexity Analysis

| Architecture | Thread Overhead ($n$ scripts, $p$ threads) | Memory Footprint | Context Switching Cost |
| :--- | :--- | :--- | :--- |
| **Manual `Thread` Creation (1 per task)** | Allocates $n$ OS threads ($O(n)$) | Huge: $\approx 1$ MB stack per thread ($10,000 \times 1\text{ MB} \approx 10\text{ GB}$ JVM crash) | Heavy OS context-switching thrashing |
| **`ExecutorService` (Fixed Pool of $p$ threads)**| Reuses $p$ worker threads ($O(p)$) | **Low & Bounded:** Only $p \times 1$ MB stack memory | **Minimal:** Worker threads stay warm, pulling tasks from an internal queue |
