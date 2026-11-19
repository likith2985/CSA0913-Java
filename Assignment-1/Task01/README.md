# Task 01: Understand the Problem and Identify the Objects

## 1. Problem Analysis & Identification of Objects

### 1.1 Inputs and Outputs
* **System Inputs:**
  * Vehicle Registration Number (e.g., `"TN-01-AB-1234"`).
  * Vehicle Type (Two-Wheeler, Car, Electric Vehicle / EV, etc.).
  * User Category (Student, Staff with Monthly Pass, Visitor).
  * Timestamps (Entry Time, Exit Time).
  * Facility Configuration (Number of floors, number of slots per floor).
  * EV charging parameters (kWh consumed).
* **System Outputs:**
  * Parking Ticket (Unique Ticket Number, Allocated Floor, Slot ID, Entry Time, Vehicle Details).
  * Slot Allocation Status (Allocated coordinates `[floor, slot]` or `null` if facility is full).
  * Slot Deallocation / Release confirmation.
  * Fee Breakdown & Final Receipt (Parking duration, Hourly rate, EV charging surcharge, Pass discounts, Total payable fee).

---

### 1.2 Candidate Classes (Nouns) and Methods (Verbs)

| Candidate Class (Noun) | Real-world Concept | Core Methods (Verbs) |
| :--- | :--- | :--- |
| `ParkingFacility` | Entire multi-level parking complex | `allocate(vehicle)`, `release(floor, slot)`, `getAvailableSlotsCount()` |
| `ParkingFloor` | Individual floor level | `findFreeSlot()`, `getOccupancyRate()` |
| `ParkingSlot` | Specific numbered parking stall | `assignVehicle(vehicle)`, `vacate()`, `isAvailable()` |
| `Vehicle` (Abstract) | Base vehicle entity entering campus | `getRegNo()`, `calculateFee(duration)` |
| `Car`, `TwoWheeler`, `ElectricCar` | Subclasses with specific fee rates | `ratePerHour()`, `chargingFee(kWh)` |
| `ParkingTicket` | Issued receipt representing active session | `issueTicket()`, `closeTicket(exitTime)` |
| `User` / `Pass` | Parking permit / subscription | `isValidPass()`, `applyDiscount()` |

---

### 1.3 Responsibilities of Candidate Classes
* **`ParkingFacility`**: Manages the grid of floors and slots; maintains global occupancy; acts as the primary orchestrator for entry allocation and exit release.
* **`ParkingSlot`**: Encapsulates slot identity (floor, slot number), type compatibility (compact, standard, EV charging), and state (`isOccupied`, reference to parked vehicle).
* **`Vehicle`**: Encapsulates vehicle registration number, category, and standard fee computation strategy.
* **`ParkingTicket`**: Encapsulates session temporal state (entry timestamp, exit timestamp, slot reference, ticket ID), preventing ticket forgery or reuse.

---

### 1.4 Business Rules
1. **Single Occupancy**: A slot cannot hold more than one vehicle at any time.
2. **Ticket Uniqueness**: Ticket numbers are strictly unique and monotonically incremented; a ticket ID is never reused.
3. **Vehicle Eligibility**: Two-wheelers cannot occupy EV charging bays or oversize slots.
4. **Pass Privileges**: Students and staff holding valid monthly passes park free of charge ($0.00 fee); visitors pay standard vehicle slab fees.
5. **Billing Resolution**: Parking durations are charged on a full started-hour ceiling basis (e.g., 61 minutes $\rightarrow$ 2 full hours).

---

### 1.5 Stated Assumptions
* Slot allocations follow first-available traversal (Floor 0 upwards, Slot 0 upwards).
* Vehicles exit through automated or staffed gates where the physical ticket is scanned and marked paid.
* EV charging fees are flat additions per kWh consumed, independent of parking duration.

---

## 2. Alternative Concept and Complexity Analysis

### Comparison: Parallel Arrays vs Object-Oriented Modeling
Suppose vehicles are stored in parallel arrays: `String[] regNo`, `String[] type`, `long[] entryTime`.

* **Code Structure Comparison:**
  * In parallel arrays, a single logical record is dispersed across multiple separate arrays. If an element at index $i$ is removed or swapped in `regNo`, every corresponding array (`type`, `entryTime`) must also be updated simultaneously, creating high coupling and vulnerability to index desynchronization.
  * In Object-Oriented modeling, `Vehicle` / `VehicleRecord` wraps all attributes in a cohesive memory unit.
* **Adding a New Vehicle Attribute (e.g., `boolean isEv`):**
  * *Parallel Arrays:* Requires declaring `boolean[] isEv`, modifying constructor/initialization methods, updating every method signature that passes vehicle records, and ensuring index consistency across $N$ arrays.
  * *Object-Oriented Design:* Open-Closed principle; simply add a private field `isEv` to `Vehicle` with getters. Existing callers pass the same `Vehicle` reference without signature explosion.
* **Time Complexity Analysis (Search by Registration Number):**
  * *Both:* Scan $N$ entries $\rightarrow$ Time Complexity is $O(N)$ for linear search.
  * *CPU Cache Locality:* Parallel arrays have good spatial locality for scanning a single field (`regNo`), but bad locality when accessing all attributes of the found vehicle. OO array of references incurs pointer dereferencing to heap objects.
* **Space Complexity Analysis (Per Vehicle):**
  * *Parallel Arrays:* Storing $N$ items across $K$ arrays requires $K$ array headers ($\sim 16$ bytes each) $+ N \times 8$ bytes (object references/primitives). Space per item is $\approx 8K$ bytes.
  * *Object-Oriented Design:* Each vehicle object has an object header ($12$–$16$ bytes on 64-bit JVM with compressed oops) $+$ field references $+$ 8-byte pointer in the master array. Space per item is $\approx 32$–$48$ bytes.
  * *Verdict:* While parallel arrays use slightly less heap space, they lack encapsulation, type safety, polymorphism, and maintainability. OO design is vastly superior for extensible campus enterprise systems.

---

## 3. Compilation and Execution
```bash
javac ParallelArraysVsObjectDemo.java
java ParallelArraysVsObjectDemo
```
*Output captured in `output.txt`.*
