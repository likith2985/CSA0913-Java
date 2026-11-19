# Advanced Java Concurrency & Exception Engineering — Assignment 3

**Course:** CSA0913 — Java Fundamentals & Object-Oriented Programming  
**Course Outcome:** CO3 Coverage — Exception handling (hierarchy, checked/unchecked, multi-catch, try-with-resources, chaining); Multithreading (thread lifecycle, Runnable, synchronization, race conditions, deadlock, wait/notifyAll, ExecutorService, Callable, Future)  
**Total Marks:** 50  
**Mode:** Pair Work (Student A & Student B)  
**Student Name:** Likith  
**GitHub Repository:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  
**Java Version:** Java 25 LTS (OpenJDK 25.0.2 / Java SE 25)

---

## Directory Structure

```
Assignment-3/
├── README.md                          <- Assignment 3 Master Guide & Run Instructions
├── Assignment3_Report.md              <- Comprehensive 50-Mark Formal Report
├── Q01/                               <- Q1: Handling Failure in a Bank ATM Simulation
│   ├── AtmSimulation.java
│   ├── README.md
│   └── output.txt
├── Q02/                               <- Q2: User-Defined Exceptions & Multi-Layer Propagation
│   ├── StorageDemo.java               <- Complete the Code
│   ├── ExamPortalPropagationDemo.java
│   ├── README.md
│   └── output.txt
├── Q03/                               <- Q3: Resource Safety, Debugging & Robust Code
│   ├── ResourceSafetyReportGenerator.java
│   ├── README.md
│   └── output.txt
├── Q04/                               <- Q4: Thread Creation and the Thread Life Cycle
│   ├── ScannerStationDemo.java
│   ├── README.md
│   └── output.txt
├── Q05/                               <- Q5: Race Conditions in Cinema Seat-Booking
│   ├── SeatBooking.java               <- Complete the Code
│   ├── CinemaRaceConditionInvestigation.java
│   ├── README.md
│   └── output.txt
├── Q06/                               <- Q6: Deadlock & Inter-Thread Communication
│   ├── TokenCounter.java              <- Complete the Code
│   ├── DeadlockAndProducerConsumerDemo.java
│   ├── README.md
│   └── output.txt
└── Q07/                               <- Q7: Concurrent Fault-Tolerant Processor (Batch)
    ├── SemesterResultBatchProcessor.java
    ├── README.md
    └── output.txt
```

---

## How to Compile and Execute Each Task

All programs were compiled and validated on **Java 25 LTS**.

### Q1: Bank ATM Simulation (Exception Design & Tracing)
```bash
cd Q01
javac AtmSimulation.java
java AtmSimulation
```
*Result:* Validates checked vs unchecked exceptions, catch block ordering, and unconditional `finally` execution (card ejection).

### Q2: Online Exam Portal (User-Defined Exceptions & Propagation)
```bash
cd Q02
javac StorageDemo.java
java StorageDemo
```
*Result:* Completes the given code snippet; outputs `Error: Could not store answer02.pdf` with `Cause: Disk quota exceeded`. Run `ExamPortalPropagationDemo` to verify the 3-layer architecture.

### Q3: Daily Sales Report (Resource Safety & Try-With-Resources)
```bash
cd Q03
javac ResourceSafetyReportGenerator.java
java ResourceSafetyReportGenerator
```
*Result:* Identifies file descriptor leaks in manual finally blocks (1 leaked handle on error) vs guaranteed 0 leaked handles in `try-with-resources`; demonstrates suppressed exceptions.

### Q4: Parallel Answer-Script Scanning (Thread Life Cycle)
```bash
cd Q04
javac ScannerStationDemo.java
java ScannerStationDemo
```
*Result:* Compares `extends Thread` vs `implements Runnable`; demonstrates thread lifecycle states, `join()`, and non-deterministic scheduling orders across 5 runs.

### Q5: Cinema Seat-Booking System (Race Conditions & Synchronization)
```bash
cd Q05
javac SeatBooking.java
java SeatBooking
```
*Result:* Completes the given code snippet; outputs `Seats sold: 10`. Run `CinemaRaceConditionInvestigation` to observe 100% double-booking reproduction without synchronization.

### Q6: Hostel Wallet & Mess Token Counter (Deadlock & Inter-Thread Coordination)
```bash
cd Q06
javac TokenCounter.java
java TokenCounter
```
*Result:* Completes the given code snippet; outputs `Sum of tokens collected: 210 (expected 210)`. Run `DeadlockAndProducerConsumerDemo` to verify lock-ordering deadlock prevention and bounds checking.

### Q7: Semester Result Processor (Concurrent, Fault-Tolerant Design)
```bash
cd Q07
javac SemesterResultBatchProcessor.java
java SemesterResultBatchProcessor
```
*Result:* Scales across 1, 2, 4, 8 threads; processes 10,000 files in **16.0 ms** ($3.75\times$ speedup) with 100% audit reconciliation (9,200 valid, 400 corrupt, 300 missing, 100 invalid).
