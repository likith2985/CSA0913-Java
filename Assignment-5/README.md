# Assignment 5: Data Persistence Engineering – Files and Databases

This directory contains the complete source code, automated test suites, execution logs, and detailed task reports for **Assignment 5 (Course Outcome CO5)** of the **CSA0913 - Java Programming** course.

---

## Directory Structure
```text
Assignment-5/
├── Assignment5_Report.md      # Master 50-mark formal lab report covering Part A & Part B
├── README.md                  # This quickstart guide
├── PartA/                     # Persistence Design & Algorithmic Pseudocode (25 Marks)
│   └── PartA_Design.md        # Comprehensive layouts, pseudocode, and security analyses (A1–A5)
├── TaskB1/                    # Instrumented File Store & Buffering Benchmarks (7 Marks)
│   ├── PassStore.java         # Bus-pass file store (Complete the Code TODO 1)
│   ├── FileBenchmark.java     # Buffering benchmarks (1MB, 10MB, 50MB) and search growth
│   ├── TaskB1Test.java        # Automated test harness
│   ├── output.txt             # Verified test execution logs
│   └── README.md              # Detailed task analysis & empirical speedup table
├── TaskB2/                    # Adversarial Inputs & Exploitation Defence (5 Marks)
│   ├── LoginSecurityDemo.java # Vulnerable SQL concatenation vs PreparedStatement
│   ├── AdversarialFileTest.java # SQL injection attacks & corrupted file parsing
│   ├── output.txt             # Verified test execution logs
│   └── README.md              # Threat analysis & regex/quote-escaping trade-offs
├── TaskB3/                    # Statement vs PreparedStatement vs Batch (8 Marks)
│   ├── PassLoader.java        # Batch loader with transaction rollback (TODO 1–3)
│   ├── DatabaseBenchmark.java # Scaling benchmarks & auto-commit vs rollback demo
│   ├── output.txt             # Verified test execution logs
│   └── README.md              # Metadata audit, scaling data & nightly vs counter trade-off
└── TaskB4/                    # 12-Campus Migration Strategy & Production Decision (5 Marks)
    ├── CampusMigrationEngine.java # Batch migration engine with error reporting
    ├── DatabaseInspector.java # DatabaseMetaData audit tool
    ├── TaskB4Test.java        # Automated production test suite
    ├── output.txt             # Verified test execution logs
    └── README.md              # Multi-campus architecture decision & decentralization analysis
```

---

## Quickstart: How to Compile and Run

All tasks run with zero external library dependencies on standard JDK (Java 11+ / Java 17+ / Java 21+ / Java 25).

### Task B1: Instrumented File Store & Buffering Benchmarks
```bash
cd Assignment-5/TaskB1
javac -d . PassStore.java FileBenchmark.java TaskB1Test.java
java -cp . taskb1.TaskB1Test
```

### Task B2: Adversarial SQL Injection & Corrupted File Defence
```bash
cd Assignment-5/TaskB2
javac -d . ../TaskB1/PassStore.java LoginSecurityDemo.java AdversarialFileTest.java
java -cp . taskb2.AdversarialFileTest
```

### Task B3: Statement vs PreparedStatement vs Batch Processing
```bash
cd Assignment-5/TaskB3
javac -d . PassLoader.java DatabaseBenchmark.java
java -cp . taskb3.DatabaseBenchmark
```

### Task B4: 12-Campus Migration Engine & Metadata Inspection
```bash
cd Assignment-5/TaskB4
javac -d . CampusMigrationEngine.java DatabaseInspector.java TaskB4Test.java
java -cp . taskb4.TaskB4Test
```

---

## Test Results Summary
**Total Test Cases Executed Across Assignment 5: 17 / 17 Passed (100% Success)**  
Refer to [Assignment5_Report.md](Assignment5_Report.md) for the complete 50-mark formal report.
