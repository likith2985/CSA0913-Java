# Assignment 4: Event-Driven GUI Design, AWT/Swing, Layout Managers, and GUI-JDBC Integration

This directory contains the complete source code, automated test suites, execution logs, and detailed task reports for **Assignment 4 (Course Outcome CO4)** of the **CSA0913 - Java Programming** course.

---

## Directory Structure
```text
Assignment-4/
├── Assignment4_Report.md         # Master 50-mark formal lab report covering Tasks 1 to 6
├── README.md                      # This quickstart guide
├── Task01/                        # GUI Design Review and Architectural Repair (FeeWindow)
│   ├── FeeService.java            # Parameterized business & data service layer
│   ├── FeeWindowRepaired.java     # EDT-responsive Swing GUI with SwingWorker
│   ├── FeeWindowTest.java         # Test suite (6 tests)
│   ├── output.txt                 # Test execution logs
│   └── README.md                  # Detailed task analysis & 100-word defence
├── Task02/                        # Outpatient Appointment Desk (Event Delegation)
│   ├── HospitalAppointmentDesk.java  # Dynamic doctor selection & booking GUI
│   ├── HospitalAppointmentTest.java  # Test suite (5 tests)
│   ├── output.txt                 # Test execution logs
│   └── README.md                  # Event model, component tree & applet comparison
├── Task03/                        # Campus Library Search Screen (Layout Resizing)
│   ├── LibrarySearchScreen.java   # BorderLayout + FlowLayout + JTable
│   ├── LibrarySearchTest.java     # Multi-resolution resize tests (4 tests)
│   ├── output.txt                 # Test execution logs
│   └── README.md                  # Layout analysis & 3-resolution verification table
├── Task04/                        # Event-Handling Forensics (DrawPad)
│   ├── DrawPad.java               # Repaired code (mouseClicked + retained paint)
│   ├── DrawPadTest.java           # Test suite (4 tests)
│   ├── output.txt                 # Test execution logs
│   └── README.md                  # Defect diagnosis, @Override & vector vs raster
├── Task05/                        # Four Event-Handling Styles (Currency Converter)
│   ├── Converter.java             # Style (d) Lambda implementation
│   ├── ConverterStylesDemo.java   # Comparison demo of Styles A, B, C, D (5 tests)
│   ├── output.txt                 # Test execution logs
│   └── README.md                  # Change impact analysis & comparison matrix
└── Task06/                        # Campus Event Registration Desktop App (Swing-JDBC)
    ├── schema.sql                 # SQL schema with unique constraint
    ├── Registration.java          # Domain entity
    ├── RegistrationDAO.java       # Parameterized JDBC DAO with in-memory fallback
    ├── EventRegistrationApp.java  # MenuBar, form, filterable JTable, SwingWorker, CSV
    ├── EventRegistrationTest.java # Comprehensive test suite (7 tests)
    ├── output.txt                 # Test execution logs
    └── README.md                  # Stages 1 to 5, architecture & pagination analysis
```

---

## Quickstart: How to Compile and Run

All tasks run with zero external library dependencies on standard JDK (Java 11+ / Java 17+ / Java 21+ / Java 25).

### Task 01: Fee Payment Review and Repair
```bash
cd Assignment-4/Task01
javac -d . FeeService.java FeeWindowRepaired.java FeeWindowTest.java
java -cp . task01.FeeWindowTest
```

### Task 02: Hospital Outpatient Appointment Desk
```bash
cd Assignment-4/Task02
javac -d . HospitalAppointmentDesk.java HospitalAppointmentTest.java
java -cp . task02.HospitalAppointmentTest
```

### Task 03: Campus Library Search Screen (Layout Dynamics)
```bash
cd Assignment-4/Task03
javac -d . LibrarySearchScreen.java LibrarySearchTest.java
java -cp . task03.LibrarySearchTest
```

### Task 04: Event-Handling Forensics (DrawPad)
```bash
cd Assignment-4/Task04
javac -d . DrawPad.java DrawPadTest.java
java -cp . task04.DrawPadTest
```

### Task 05: Four Event-Handling Styles (Converter)
```bash
cd Assignment-4/Task05
javac -d . Converter.java ConverterStylesDemo.java
java -cp . task05.ConverterStylesDemo
```

### Task 06: Campus Event Registration Desktop App (Swing + JDBC)
```bash
cd Assignment-4/Task06
javac -d . Registration.java RegistrationDAO.java EventRegistrationApp.java EventRegistrationTest.java
java -cp . task06.EventRegistrationTest
```

---

## Test Results Summary
**Total Test Cases Executed Across Assignment 4: 31 / 31 Passed (100% Success)**  
Refer to [Assignment4_Report.md](Assignment4_Report.md) for the complete 50-mark formal report.
