# Task 12: Day-4 Design Challenge — Re-evaluate and Adapt the Design

## 1. Section A: Impact Analysis (2 Marks)

### 1.1 Changed Requirements Summary
1. **New Vehicle Category 1 — E-Bikes:** Charged a **flat fee** (Rs 20.0) regardless of duration, plus optional EV battery charging.
2. **New Vehicle Category 2 — Delivery Vans:** Charged at **double the car rate** (Rs 60.0/hr vs normal Rs 30.0/hr) during peak commercial hours (**9:00 AM – 5:00 PM**).
3. **Visitor One-Day Pass:** Visitors can now purchase a flat 24-hour pass (Rs 100.0) waiving duration-based billing.
4. **Facility Extension:** Extended from 3 floors to **5 floors**, with a **different number of slots on each floor** (e.g. 25, 40, 50, 35, 20 slots).

### 1.2 Impact on Existing Classes, Methods, and Data Structures
* **`Vehicle.calculateFee()`**:
  * Originally accepted only `int minutes` and computed `hours * ratePerHour()`.
  * *Impact:* A flat fee (E-Bike) cannot be expressed purely via `hours * ratePerHour()`, and peak-hour rate doubling (Delivery Van) requires knowledge of the actual time of day (`LocalDateTime entry, exit`), not just total elapsed duration.
* **Array Design (`String[][] slots`)**:
  * Because our Task 2 design selected a **Jagged Array** (`slots = new String[slotsPerFloor.length][]`), extending from 3 to 5 floors with uneven slot capacities required **ZERO architectural changes**. The array initialization simply takes `int[]{25, 40, 50, 35, 20}`.
* **User / Pass Representation**:
  * The simple `UserType` enum was insufficient to model dynamic date-bound passes (`OneDayPass`). An extensible `ParkingPass` strategy interface was introduced.

---

## 2. Section B: Evaluation of the Original Design (2 Marks)

| Evaluation Question | Assessment | Detailed Technical Rationale |
| :--- | :--- | :--- |
| **Can new vehicle types be added only by writing new subclasses?** | **Partially** | Adding standard subclasses with constant hourly rates was trivial. However, adding vehicles with non-hourly pricing (flat fee) or time-of-day surcharge required evolving the fee calculation method signature in `Vehicle` to receive timestamps. |
| **Are pricing rules spread across many classes or kept in one place?** | **Well-Modularized** | Hourly rates were encapsulated inside respective vehicle subclasses, while pass exemptions lived in `ParkingTicket`. With the introduction of One-Day Passes, decoupling pass rules into a dedicated `ParkingPass` hierarchy cleanly localizes discount logic. |
| **Can existing tested classes remain unchanged?** | **Yes** | Existing classes (`Car`, `TwoWheeler`, `ElectricCar`) remained fully operational with zero regression. |

---

## 3. Section C: Adaptation or Redesign Decision (2 Marks)

### Design Decision: **MODIFIED ADAPTATION**
A complete rewrite/redesign is unnecessary and wasteful because the core abstractions (Jagged Array layout, `Vehicle` abstract base, and `Chargeable` interface) proved exceptionally robust. We adopted a targeted **Modification & Extension**:

### 3.1 Revised Class Structure
1. **`ParkingPass` Interface:**
   * Implementations: `NoPass`, `MonthlyPass`, and `OneDayPass` (encapsulating date validity and pass tariff).
2. **Time-Aware `calculateFee()` Polymorphism in `Vehicle`:**
   * Added `calculateFee(LocalDateTime entry, LocalDateTime exit)` open to specialization.
3. **`EBike` Subclass:**
   * Extends `Vehicle`, implements `Chargeable`. Overrides `calculateFee(...)` to return flat Rs 20.0.
4. **`DeliveryVan` Subclass:**
   * Extends `Vehicle`. Overrides `calculateFee(...)` to dynamically inspect whether the stay falls inside 9:00 AM – 5:00 PM and doubles the car rate accordingly.
5. **5-Floor Jagged Array in `AdaptedParkingFacility`:**
   * Configured with `{25, 40, 50, 35, 20}` without structural changes.

---

## 4. Section D: Evidence-Based Defence (2 Marks)

### 4.1 Quantitative Impact Metrics
* **Number of Existing Vehicle Subclasses Modified:** **0** (`Car`, `TwoWheeler`, `ElectricCar` were untouched).
* **Number of New Classes Added:** **5** (`EBike`, `DeliveryVan`, `ParkingPass`, `MonthlyPass`, `OneDayPass`).
* **Ease of Adding Future Vehicle Types:** Extremely high (e.g. adding `HeavyTruck` or `AutonomousShuttle` only requires extending `Vehicle`).

### 4.2 Test Verification Results

| Vehicle / Feature | Test Duration / Condition | Expected Fee | Actual Program Fee | Status |
| :--- | :--- | :--- | :--- | :--- |
| **E-Bike (Short Stay)** | 45 minutes | Rs 20.00 (Flat) | **Rs 20.00** | **PASS** |
| **E-Bike (Long Stay)** | 7 hours (420 mins) | Rs 20.00 (Flat) | **Rs 20.00** | **PASS** |
| **E-Bike (Battery Charge)**| 5 kWh @ Rs 8/kWh | Rs 40.00 | **Rs 40.00** | **PASS** |
| **Delivery Van (Peak)** | 10:00 AM – 12:00 PM (2 hrs) | $2 \times \text{Rs } 60 = \text{Rs } 120.00$ | **Rs 120.00** | **PASS** |
| **Delivery Van (Off-Peak)**| 6:00 AM – 8:00 AM (2 hrs) | $2 \times \text{Rs } 30 = \text{Rs } 60.00$ | **Rs 60.00** | **PASS** |
| **Visitor with One-Day Pass**| 10 hours stay (Standard = Rs 300) | Rs 100.00 (Pass Cap) | **Rs 100.00** | **PASS** |
| **5-Floor Jagged Array** | Capacity $\{25, 40, 50, 35, 20\}$ | 170 slots across 5 floors | **170 slots** | **PASS** |

### 4.3 Architectural Trade-Offs
* **Trade-off:** Adding time-of-day awareness (`LocalDateTime`) slightly increases method parameter footprint compared to raw `int minutes`, but provides the essential flexibility needed for modern peak-load surge pricing without breaking existing contracts.
