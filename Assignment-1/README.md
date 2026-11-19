# Campus Smart-Parking System — Assignment 1

**Course:** CSA0913 — Java Fundamentals & Object-Oriented Programming  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Total Marks:** 50  
**Student Name:** Likith  
**GitHub Repository:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  
**Java Version:** Java 25 LTS (OpenJDK 25.0.2 / Java SE 25)

---

## Directory Structure

Each task has its own dedicated folder containing the Java source code, test inputs/drivers, execution logs (`output.txt`), and detailed analytical documentation (`README.md`):

```
Assignment-1/
├── README.md                          <- Assignment 1 Guide
├── Assignment1_Report.md              <- Comprehensive 50-Mark Formal Report
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

### Task 02: Data Representation & Integer Division Risks
```bash
cd Task02
javac DataRepresentationDemo.java
java DataRepresentationDemo
```

### Task 03: Control Logic & ParkingFacility Implementation
```bash
cd Task03
javac ParkingFacility.java
java ParkingFacility
```

### Task 04: Constructors, Chaining & Copy Constructors
```bash
cd Task04
javac ConstructorDemo.java
java ConstructorDemo
```

### Task 05: Static Members & Object Lifecycle
```bash
cd Task05
javac StaticAndLifecycleDemo.java
java StaticAndLifecycleDemo
```

### Task 06: Inheritance Hierarchy & Constructor Execution Order
```bash
cd Task06
javac VehicleHierarchyDemo.java
java VehicleHierarchyDemo
```

### Task 07: Method Overriding & Equality Contract
```bash
cd Task07
javac MethodOverridingDemo.java
java MethodOverridingDemo
```

### Task 08: Polymorphism & FeeDemo Implementation
```bash
cd Task08
javac FeeDemo.java
java FeeDemo
```

### Task 09: Abstraction & Interface Capabilities
```bash
cd Task09
javac AbstractionDemo.java
java AbstractionDemo
```

### Task 10: Encapsulation with Packages & Defensive Invariants
```bash
cd Task10
javac -d bin src/parking/model/*.java src/parking/service/*.java src/parking/app/*.java
java -cp bin parking.app.EncapsulationDemoApp
```

### Task 11: Full System Implementation & Test Suite
```bash
cd Task11
javac CampusSmartParkingApp.java
java CampusSmartParkingApp
```

### Task 12: Day-4 Design Challenge
```bash
cd Task12
javac Day4SmartParkingApp.java
java Day4SmartParkingApp
```
