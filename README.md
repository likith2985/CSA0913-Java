# CSA0913 — Java Fundamentals & Object-Oriented Programming

**Student Name:** Likith  
**GitHub Repository:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  
**Java Version:** Java 25 LTS (OpenJDK 25.0.2 / Java SE 25)

---

## Assignments Index

### 📁 [Assignment-1/](Assignment-1/)
**Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**CO Coverage:** CO1 — Java fundamentals and object-oriented programming (classes, inheritance, polymorphism, abstraction, packages, encapsulation)  
**Total Marks:** 50  
**Status:** Completed & Validated (Tasks 1–12)  
* 📄 **[Assignment1_Report.md](Assignment-1/Assignment1_Report.md)**: Formal 50-mark submission report with UML diagrams, SIMATS pseudocode, and test tables.
* 📁 **[Task01–Task12](Assignment-1/)**: Full source code, test drivers, and verified execution logs.

---

### 📁 [Assignment-2/](Assignment-2/)
**Title:** Java Collections Framework, Strings, Generics and Algorithm Engineering  
**CO Coverage:** CO2 — Strings and StringBuilder, regular expressions, Collection Framework (List, Queue, Deque, PriorityQueue, Set, Map), hashCode–equals contract, Generics, Comparable and Comparator, Collections utility class  
**Total Marks:** 50  
**Status:** Completed & Validated (Questions Q1–Q7)  
* 📄 **[Assignment2_Report.md](Assignment-2/Assignment2_Report.md)**: Formal 50-mark submission report with Big-O derivations, empirical evidence, and Day-4 design challenge.
* 📁 **[Q01–Q07](Assignment-2/)**: Full source code, benchmarks, Complete the Code solutions, and verified execution logs.

---

### 📁 [Assignment-3/](Assignment-3/)
**Title:** Advanced Java Concurrency, Exception Handling & Fault-Tolerant Systems  
**CO Coverage:** CO3 — Exception handling (hierarchy, checked/unchecked, multi-catch, user-defined exceptions, chaining, try-with-resources); Multithreading (thread lifecycle, synchronization, race conditions, deadlock, wait/notifyAll, ExecutorService, Callable/Future)  
**Total Marks:** 50  
**Mode:** Pair Work (Student A & Student B)  
**Status:** Completed & Validated (Questions Q1–Q7)  
* 📄 **[Assignment3_Report.md](Assignment-3/Assignment3_Report.md)**: Formal 50-mark submission report with Individual Contribution Record, stack traces, Coffman deadlock proofs, and batch scaling benchmarks.
* 📁 **[Q01](Assignment-3/Q01/)**: Bank ATM Failure Handling & Multi-Catch Exception Tracing.
* 📁 **[Q02](Assignment-3/Q02/)**: User-Defined Exceptions & Multi-Layer Propagation (`StorageDemo.java` Complete the Code).
* 📁 **[Q03](Assignment-3/Q03/)**: Resource Safety & Debugging (File descriptor leak analysis; Try-With-Resources).
* 📁 **[Q04](Assignment-3/Q04/)**: Thread Creation & Life Cycle (`ScannerStationDemo.java`, scheduler non-determinism).
* 📁 **[Q05](Assignment-3/Q05/)**: Race Conditions in Cinema Seat-Booking (`SeatBooking.java` Complete the Code; 100% double-booking reproduction).
* 📁 **[Q06](Assignment-3/Q06/)**: Deadlock Lock-Ordering & Mess Token Counter (`TokenCounter.java` Complete the Code; bounds validation).
* 📁 **[Q07](Assignment-3/Q07/)**: Concurrent, Fault-Tolerant Result Batch Processor (10,000 files in **16 ms** with 100% audit match).


---

### 📁 [Assignment-4/](Assignment-4/)
**Title:** Event-Driven GUI Design, AWT & Swing Architectures, Layout Management, and GUI-JDBC Integration  
**CO Coverage:** CO4 — Event-Driven Programming, Java AWT and Swing frameworks, Event Delegation Model, Component & Container hierarchies, Layout Managers (BorderLayout, FlowLayout, GridLayout), Graphics rendering pipeline, and Desktop GUI to JDBC Database integration.  
**Total Marks:** 50  
**Status:** Completed & Validated (Tasks 1–6)  
* 📄 **[Assignment4_Report.md](Assignment-4/Assignment4_Report.md)**: Formal 50-mark submission report covering architectural defect reviews, event lifecycle traces vs applet lifecycle, layout resizing invariance across 3 resolutions, graphics repaint forensics, comparative evaluation of 4 event handling styles, and the Stage 1–5 Campus Event Registration Challenge.
* 📁 **[Task01](Assignment-4/Task01/)**: GUI Design Review and Architectural Repair (`FeeWindowRepaired.java`, `FeeService.java`, SwingWorker off-EDT processing, SQL injection mitigation).
* 📁 **[Task02](Assignment-4/Task02/)**: Outpatient Appointment Desk Event-Driven Design (`HospitalAppointmentDesk.java`, dynamic doctor catalog dropdowns, form validation, exit confirmation).
* 📁 **[Task03](Assignment-4/Task03/)**: Campus Library Search Screen Nested Layouts and Resizing (`LibrarySearchScreen.java`, empirical validation at 500x350, 750x500, 1000x650 px).
* 📁 **[Task04](Assignment-4/Task04/)**: Event-Handling Forensics & Drawing Persistence (`DrawPad.java` Complete the Code, typo fix, retained mode rendering inside `paint()`).
* 📁 **[Task05](Assignment-4/Task05/)**: Four Event-Handling Styles & Converter Completion (`Converter.java` Complete the Code with Style (d) Lambda, Enter-key triggering, `JOptionPane` dialog).
* 📁 **[Task06](Assignment-4/Task06/)**: Full-Stack Campus Event Registration Desktop Application (`EventRegistrationApp.java`, `RegistrationDAO.java`, `schema.sql`, menu bar, filterable `JTable`, async persistence, CSV export).


---

### 📁 [Assignment-5/](Assignment-5/)
**Title:** Data Persistence Engineering – Files and Databases  
**CO Coverage:** CO5 — Java I/O (byte, character, and buffered streams, the File class, serialization); JDBC (architecture and drivers, Statement, PreparedStatement, CallableStatement, transactions, batch processing, and metadata).  
**Total Marks:** 50  
**Status:** Completed & Validated (Part A: A1–A5, Part B: B1–B4)  
* 📄 **[Assignment5_Report.md](Assignment-5/Assignment5_Report.md)**: Formal 50-mark submission report covering record formats, SIMATS algorithmic pseudocode, buffering speedups, transient object graph serialization, ACID wallet transactions, and multi-campus scaling.
* 📁 **[PartA/](Assignment-5/PartA/)**: Persistence Design & Pseudocode (`PartA_Design.md` covering Tasks A1 to A5).
* 📁 **[TaskB1](Assignment-5/TaskB1/)**: Instrumented File Store (`PassStore.java` Complete the Code, empirical buffering benchmarks measuring 1,269x speedup, search growth measurements).
* 📁 **[TaskB2](Assignment-5/TaskB2/)**: Adversarial Inputs & Exploitation Defence (`LoginSecurityDemo.java`, SQL comment/tautology injection neutralization, damaged CSV fault-tolerance).
* 📁 **[TaskB3](Assignment-5/TaskB3/)**: Statement vs PreparedStatement vs Batch (`PassLoader.java` Complete the Code, scaling across 1K, 10K, and 100K records, auto-commit vs transaction rollback).
* 📁 **[TaskB4](Assignment-5/TaskB4/)**: 12-Campus Migration Strategy & Production Decision (`CampusMigrationEngine.java` batch migration with error audit, `DatabaseInspector.java` `DatabaseMetaData` inspection, multi-campus architectural evaluation).
