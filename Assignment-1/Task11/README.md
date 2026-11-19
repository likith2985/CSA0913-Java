# Task 11: Implement, Test and Defend the Design

## 1. System Test Results Matrix

The comprehensive integration test suite evaluates normal, boundary, and error operations across the system:

| Test ID | Test Scenario Description | Input Data | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Normal Entry: Car in empty lot | `Car("TN01-1111")`, Visitor | Allocated Slot `[0,0]` | Slot `[0,0]` | **PASS** |
| **TC-02** | Multi-type Entry: Bike & EV | `TwoWheeler`, `ElectricCar` | Successfully Allocated | Successfully Allocated | **PASS** |
| **TC-03** | Boundary: Last Free Slot | 4th vehicle in 4-slot lot | Slot Allocated ($0$ free) | Slot `[1,1]` ($0$ free) | **PASS** |
| **TC-04** | Boundary: Facility Full Rejection | 5th vehicle in 4-slot lot | Return `null` (Rejected) | `null` | **PASS** |
| **TC-05** | Invalid Case: Duplicate RegNo | Re-park `"TN01-1111"` | `IllegalStateException` | `IllegalStateException` | **PASS** |
| **TC-06** | Boundary: Zero-minute stay | Exit at entry timestamp | Fee = Rs 0.0 | Fee = Rs 0.0 | **PASS** |
| **TC-07** | Boundary: 60 min vs 61 min | 60 min stay vs 61 min stay | Rs 30.0 / Rs 60.0 | Rs 30.0 / Rs 60.0 | **PASS** |

---

## 2. Hand-Calculated Reconciliation Scenario

A controlled test scenario with 4 diverse vehicle sessions was hand-calculated to audit financial integrity:

### 2.1 Hand Calculation Breakdown
1. **Vehicle 1 (Visitor Car - `"RECON-CAR"`):**
   * Parked Duration: 120 minutes $\rightarrow 2$ billable hours.
   * Hourly Rate: Rs 30.0 / hr.
   * Hand Calculation: $2 \times 30.0 = \text{Rs } 60.00$.
2. **Vehicle 2 (Student TwoWheeler with Pass - `"RECON-BIKE"`):**
   * Parked Duration: 180 minutes.
   * Pass Holder Privilege: Monthly pass covers parking duration $\rightarrow \text{Rs } 0.00$.
3. **Vehicle 3 (Visitor ElectricCar - `"RECON-EV"`):**
   * Parked Duration: 90 minutes $\rightarrow (90 + 59)/60 = 2$ billable hours.
   * Parking Fee: $2 \times 30.0 = \text{Rs } 60.00$.
   * EV Charging: 10 kWh consumed $\times$ Rs 12.00 / kWh = Rs 120.00.
   * Hand Calculation: $60.00 + 120.00 = \text{Rs } 180.00$.
4. **Vehicle 4 (Visitor TwoWheeler - `"RECON-TW-VIS"`):**
   * Parked Duration: 45 minutes $\rightarrow 1$ billable hour.
   * Hourly Rate: Rs 10.0 / hr.
   * Hand Calculation: $1 \times 10.0 = \text{Rs } 10.00$.

$$\text{Total Expected Daily Collection} = 60.00 + 0.00 + 180.00 + 10.00 = \mathbf{\text{Rs } 250.00}$$

### 2.2 Program Output Comparison
* Hand-Calculated Revenue: **Rs 250.00**
* Actual Program Revenue:  **Rs 250.00**
* Variance: **Rs 0.00 (100% Reconciliation Match)**

---

## 3. Defense of Object-Oriented Principles

| Principle | Implementation in Design | Evaluation & Defense |
| :--- | :--- | :--- |
| **Encapsulation** | `ParkingSlot`, `ParkingTicket`, `Vehicle` fields are strictly `private`. Mutators enforce domain invariants (`occupy()`, `settle()`). | Prevents illegal state transitions and protects billing integrity. |
| **Inheritance** | Hierarchical (`Vehicle` $\rightarrow$ `Car`, `TwoWheeler`) and Multilevel (`Car` $\rightarrow$ `ElectricCar`). | Enables high code reuse (`regNo`, template fee calculation logic). |
| **Polymorphism** | Compile-time overloading and runtime dynamic method dispatch via abstract `ratePerHour()`. | Eliminates brittle procedural conditional cascades (`if-else` / `switch`). |
| **Abstraction** | Abstract class `Vehicle` and capability interface `Chargeable`. | Separates contract from execution, allowing decoupled extension. |

### Identified Limitation of the Original Design
* In the original design, slot allocation uses a flat sequential search through `slots[f][s]`. While optimal for small facilities ($1$ floor $\times 20$ slots), its worst-case allocation time degrades to $O(F \times S)$ as facilities grow. Furthermore, exit processing using a linear array search is $O(N)$, which becomes a bottleneck under high traffic volume.

---

## 4. Alternative Concept and Complexity Analysis

### Exit-Processing Data Structures: Array vs ArrayList vs HashMap

When a vehicle arrives at the exit booth, its active `ParkingTicket` must be retrieved by registration number:

| Data Structure | Lookup Time Complexity | Space Complexity ($n$ active vehicles) | Measured Lookup Latency ($N=5000$) | Operational Trade-off |
| :--- | :--- | :--- | :--- | :--- |
| **Array `ParkingTicket[]`** | $O(N)$ worst case (linear scan) | $O(N)$ contiguous pointers | **$695,900$ ns** | Simple, but slow search and costly compaction on removal. |
| **`ArrayList<ParkingTicket>`**| $O(N)$ linear search | $O(N)$ dynamic array backing | **$1,017,200$ ns** | Resizing overhead; $O(N)$ shift penalty on `remove(ticket)`. |
| **`HashMap<String, Ticket>`** | **$O(1)$ constant time** | $O(N)$ hash bucket nodes | **$15,100$ ns** | **$46\times$ faster!** Instant retrieval regardless of facility size. |

**Conclusion:** Migrating active ticket lookups to a `HashMap<String, ParkingTicket>` keyed by vehicle registration number yields a massive **46x latency reduction** on exit processing, eliminating exit gate queues.
