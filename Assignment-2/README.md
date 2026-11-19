# Java Collections & Strings Framework — Assignment 2

**Course:** CSA0913 — Java Fundamentals & Object-Oriented Programming  
**Course Outcome:** CO2 Coverage — Strings and StringBuilder, regular expressions, the Collection Framework (List, Queue, Deque, PriorityQueue, Set, Map), the hashCode–equals contract, Generics, Comparable and Comparator, and Collections utility class  
**Total Marks:** 50  
**Student Name:** Likith  
**GitHub Repository:** [https://github.com/likith2985/CSA0913-Java](https://github.com/likith2985/CSA0913-Java)  
**Java Version:** Java 25 LTS (OpenJDK 25.0.2 / Java SE 25)

---

## Directory Structure

```
Assignment-2/
├── README.md                          <- Assignment 2 Master Guide & Run Instructions
├── Assignment2_Report.md              <- Comprehensive 50-Mark Formal Report
├── Q01/                               <- Q1: String, StringBuilder & StringBuffer Under Load
│   ├── TextConcatenationBenchmark.java
│   ├── README.md
│   └── output.txt
├── Q02/                               <- Q2: ArrayList, LinkedList & ArrayDeque (Playlist Editor)
│   ├── PlaylistBenchmark.java
│   ├── README.md
│   └── output.txt
├── Q03/                               <- Q3: HashSet & the hashCode-equals Contract
│   ├── Attendance.java                <- Complete the Code
│   ├── HashCodeContractInvestigation.java
│   ├── README.md
│   └── output.txt
├── Q04/                               <- Q4: Map Selection for Pharmacy Inventory
│   ├── PharmacyInventoryBenchmark.java
│   ├── README.md
│   └── output.txt
├── Q05/                               <- Q5: Generic Ranking Component & Transitivity Principle
│   ├── LeaderboardDemo.java           <- Complete the Code
│   ├── GenericRankingInvestigation.java
│   ├── README.md
│   └── output.txt
├── Q06/                               <- Q6: PriorityQueue Hospital Triage & Fairness Aging
│   ├── Triage.java                    <- Complete the Code
│   ├── HospitalTriageInvestigation.java
│   ├── README.md
│   └── output.txt
└── Q07/                               <- Q7: Day-4 Design Challenge - Triage Redesign & Validation
    ├── TriageRedesignBenchmark.java
    ├── README.md
    └── output.txt
```

---

## How to Compile and Execute Each Task

All programs were compiled and validated on **Java 25 LTS**.

### Q1: Building Text Under Load: String, StringBuilder or StringBuffer?
```bash
cd Q01
javac TextConcatenationBenchmark.java
java TextConcatenationBenchmark
```
*Result:* Proves $O(n^2)$ vs $O(n)$ character copying; StringBuilder is **$441\times$ faster** on 3,000 fields and creates only 1 intermediate object vs 3,000.

### Q2: When Is ArrayList an Appropriate Engineering Choice?
```bash
cd Q02
javac PlaylistBenchmark.java
java PlaylistBenchmark
```
*Result:* Demonstrates ArrayList random access $O(1)$ is **$>400\times$ faster** than LinkedList; evaluates ArrayDeque as the optimal $O(1)$ ring buffer for the "Up Next" queue.

### Q3: Engineer the hashCode–equals Contract
```bash
cd Q03
javac Attendance.java
java Attendance
```
*Result:* Solves the Complete the Code challenge; outputs `Unique students: 2`. Run `HashCodeContractInvestigation` to observe the broken duplicate bug and the mutable ghost key trap.

### Q4: Choosing a Map for a Pharmacy Inventory
```bash
cd Q04
javac PharmacyInventoryBenchmark.java
java PharmacyInventoryBenchmark
```
*Result:* Benchmarks 100,000 lookups (HashMap/LinkedHashMap: $24$–$79$ ns vs TreeMap: $927$ ns). Proves TreeMap naturally generates code-order report while LinkedHashMap naturally generates insertion-order report.

### Q5: Derive a Generic Ranking Component from a Principle
```bash
cd Q05
javac LeaderboardDemo.java
java LeaderboardDemo
```
*Result:* Solves the Complete the Code challenge; outputs `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`. Validates comparator transitivity and exposes the subtraction overflow bug.

### Q6: Priority, Order and Fairness in a Queue
```bash
cd Q06
javac Triage.java
java Triage
```
*Result:* Solves the Complete the Code challenge; outputs `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`. Demonstrates arrival counter FIFO stability and dynamic aging.

### Q7: Day-4 Design Challenge — Diagnose, Redesign and Validate
```bash
cd Q07
javac TriageRedesignBenchmark.java
java TriageRedesignBenchmark
```
*Result:* Redesigns the triage queue into a Multi-Level Bucket Queue (`ArrayDeque[5]`); achieves **$15.03\times$ faster dispatch**, strict $O(1)$ admission, and strict $O(1)$ starvation aging.
