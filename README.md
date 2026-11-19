# Campus Smart-Parking System — Java OOP Assignment 1

**Course:** CSA0913 — Java Fundamentals & Object-Oriented Programming  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Total Marks:** 50  
**Student Name:** Likith  
**GitHub Repository:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  
**Java Version:** Java 25 LTS (OpenJDK 25.0.2 / Java SE 25)

---

## Repository Structure & Task Map

Each task has its own dedicated folder containing the Java source code, test inputs/drivers, execution logs (`output.txt`), and detailed analytical documentation (`README.md`):

```
CSA0913-Java/
├── .gitignore
├── README.md                          <- Master Repository Guide
├── Assignment1_Report.md              <- Comprehensive 50-Mark Assignment Report
├── Task01/                            <- Task 1: Problem Analysis & Object Identification
│   ├── ParallelArraysVsObjectDemo.java
│   ├── README.md
│   └── output.txt
├── Task02/                            <- Task 2: Data Representation & Array Structure
│   ├── DataRepresentationDemo.java
│   ├── README.md
│   └── output.txt
├── Task03/                            <- Task 3: Control Logic, SIMATS Pseudocode & Complete the Code
│   ├── ParkingFacility.java
│   ├── README.md
│   └── output.txt
├── Task04/                            <- Task 4: Classes, Objects, Constructors & Memory Model
│   ├── ConstructorDemo.java
│   ├── README.md
│   └── output.txt
├── Task05/                            <- Task 5: static Members, Lifecycle & Multi-Facility Analysis
│   ├── StaticAndLifecycleDemo.java
│   ├── README.md
│   └── output.txt
├── Task06/                            <- Task 6: Inheritance Hierarchy, Constructor Tracing & UML
│   ├── VehicleHierarchyDemo.java
│   ├── README.md
│   └── output.txt
├── Task07/                            <- Task 7: Method Overriding, Equality Contract & OCP
│   ├── MethodOverridingDemo.java
│   ├── README.md
│   └── output.txt
├── Task08/                            <- Task 8: Polymorphism, Dynamic Dispatch & Complete the Code
│   ├── FeeDemo.java
│   ├── README.md
│   └── output.txt
├── Task09/                            <- Task 9: Abstraction, Abstract Classes & Interfaces
│   ├── AbstractionDemo.java
│   ├── README.md
│   └── output.txt
├── Task10/                            <- Task 10: Packages, Access Modifiers & Encapsulation
│   ├── src/
│   │   └── parking/
│   │       ├── model/                 <- ParkingSlot, Vehicle, Car
│   │       ├── service/               <- ParkingService
│   │       └── app/                   <- EncapsulationDemoApp
│   ├── README.md
│   └── output.txt
├── Task11/                            <- Task 11: Complete System Implementation, Testing & Defence
│   ├── CampusSmartParkingApp.java     <- Full Integration Test Suite + Exit DS Benchmark
│   ├── README.md
│   └── output.txt
└── Task12/                            <- Task 12: Day-4 Design Challenge - Impact, Adaptation & Defence
    ├── Day4SmartParkingApp.java       <- EBike, DeliveryVan, OneDayPass, 5-Floor Jagged Array
    ├── README.md
    └── output.txt
```

---

## How to Compile and Execute Each Task

All programs were compiled and validated on **Java 25 LTS**.

### Task 01: Parallel Arrays vs Object-Oriented Modeling
```bash
cd Task01
javac ParallelArraysVsObjectDemo.java
java ParallelArraysVsObjectDemo
```
*Result:* Contrasts parallel array fragility with cohesive object encapsulation; confirms linear search behavior.

### Task 02: Data Representation & Integer Division Risks
```bash
cd Task02
javac DataRepresentationDemo.java
java DataRepresentationDemo
```
*Result:* Proves how naive integer division `minutes / 60` causes underbilling on 65 mins, and validates the integer ceiling formula `(minutes + 59) / 60`. Demonstrates jagged array floor indexing.

### Task 03: Control Logic & ParkingFacility Implementation
```bash
cd Task03
javac ParkingFacility.java
java ParkingFacility
```
*Result:* Completes the given code snippet; produces the exact required output:
```
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

### Task 04: Constructors, Chaining & Copy Constructors
```bash
cd Task04
javac ConstructorDemo.java
java ConstructorDemo
```
*Result:* Demonstrates parameterized constructors, `this(...)` chaining, copy constructor for re-issuing duplicate tickets, and stack vs heap memory layout.

### Task 05: Static Members & Object Lifecycle
```bash
cd Task05
javac StaticAndLifecycleDemo.java
java StaticAndLifecycleDemo
```
*Result:* Demonstrates class-level static sequence generators, pass-by-value reference semantics, GC reachability, and independent instance counters for multi-gate facilities.

### Task 06: Inheritance Hierarchy & Constructor Execution Order
```bash
cd Task06
javac VehicleHierarchyDemo.java
java VehicleHierarchyDemo
```
*Result:* Validates hierarchical and multilevel inheritance; outputs constructor execution order: `Object` $\rightarrow$ `Vehicle` $\rightarrow$ `Car` $\rightarrow$ `ElectricCar`.

### Task 07: Method Overriding & Equality Contract
```bash
cd Task07
javac MethodOverridingDemo.java
java MethodOverridingDemo
```
*Result:* Validates `@Override`, template pattern with `final`, and overrides `equals()` and `hashCode()` by vehicle registration plate, demonstrating `HashSet` deduplication.

### Task 08: Polymorphism & FeeDemo Implementation
```bash
cd Task08
javac FeeDemo.java
java FeeDemo
```
*Result:* Completes the given code snippet; produces the exact required output:
```
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

### Task 09: Abstraction & Interface Capabilities
```bash
cd Task09
javac AbstractionDemo.java
java AbstractionDemo
```
*Result:* Demonstrates abstract base class `Vehicle` combined with multi-interface implementation (`Chargeable`, `Payable`), interface default methods, and static methods.

### Task 10: Encapsulation with Packages & Defensive Invariants
```bash
cd Task10
javac -d bin src/parking/model/*.java src/parking/service/*.java src/parking/app/*.java
java -cp bin parking.app.EncapsulationDemoApp
```
*Result:* Enforces layered packages (`parking.model`, `parking.service`, `parking.app`); demonstrates defensive rejection of double-occupancy and negative durations; exposes the `Integer == Integer` cache boundary trap.

### Task 11: Full System Implementation & Test Suite
```bash
cd Task11
javac CampusSmartParkingApp.java
java CampusSmartParkingApp
```
*Result:* Executes 7 automated test cases (all PASS); confirms 100% reconciliation match between hand-calculated collection (Rs 250.00) and program output (Rs 250.00); benchmarks exit search proving HashMap is **46x faster** than linear array scan.

### Task 12: Day-4 Design Challenge
```bash
cd Task12
javac Day4SmartParkingApp.java
java Day4SmartParkingApp
```
*Result:* Validates adapted architecture supporting E-Bikes (flat fee), Delivery Vans (double rate 9 AM - 5 PM), Visitor One-Day Passes (Rs 100 flat), and 5 non-uniform floors (170 total slots) with 0 regressions.

---

## Progressive Commit History

In compliance with the assignment instructions requiring progressive commits rather than a single bulk upload, each task was developed, tested, and committed sequentially:

* **Commit 1:** `Task 01: Understand the Problem and Identify the Objects`
* **Commit 2:** `Task 02: Choose the Data Representation and Array Structures`
* **Commit 3:** `Task 03: Design Control Logic, SIMATS Pseudocode, and ParkingFacility Implementation`
* **Commit 4:** `Task 04: Design Classes, Objects, Constructors, and Memory Lifecycle`
* **Commit 5:** `Task 05: Apply static Members, Object Life Cycle, and Multi-Facility Analysis`
* **Commit 6:** `Task 06: Design the Inheritance Hierarchy, Constructor Tracing, and UML Modeling`
* **Commit 7:** `Task 07: Method Overriding, Contract of Equality, and Open-Closed Principle`
* **Commit 8:** `Task 08: Apply Polymorphism, Dynamic Dispatch, and FeeDemo Completion`
* **Commit 9:** `Task 09: Introduce Abstraction, Interfaces, and Architectural Trade-off Analysis`
* **Commit 10:** `Task 10: Enforce Encapsulation through Packages, Access Modifiers, and Defensive Validation`
* **Commit 11:** `Task 11: Full System Implementation, Test Suite Execution, and Exit Data Structure Benchmark`
* **Commit 12:** `Task 12: Day-4 Design Challenge - Impact Analysis, Adaptation, and Defence`
* **Commit 13:** `Documentation: Master README and Comprehensive 50-Mark Assignment Report`
