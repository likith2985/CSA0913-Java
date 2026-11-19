# Task 10: Enforce Encapsulation through Packages and Access Modifiers

## 1. Package Architecture and Modular Structure

The codebase is organized into cohesive, layered packages following industry architectural standards:
* **`parking.model`**: Contains core domain entities (`Vehicle`, `Car`, `TwoWheeler`, `ParkingSlot`, `ParkingTicket`). Models encapsulate state and domain validation logic.
* **`parking.service`**: Contains business logic and orchestrators (`ParkingService`). Manages slot allocation, deallocation, and multi-floor facility state.
* **`parking.app`**: Application entry point and runner (`EncapsulationDemoApp`). Interacts with the domain via public service contracts.

---

## 2. Access Modifier Strategy

| Modifier | Target Member | Justification |
| :--- | :--- | :--- |
| **`private`** | Fields (`occupied`, `vehicleRegNo`, `floor`, `slotNumber`, `regNo`) | State is strictly hidden from outside classes to prevent unmediated corruption. |
| **`protected`** | `Vehicle(String regNo)`, `ratePerHour()` | Accessible only to subclasses and classes within the same package, enabling inheritance while hiding internals from outer packages (`parking.app`). |
| **`default` (package-private)** | Internal package helpers | Scopes helper routines strictly to package collaborators without polluting the public API. |
| **`public`** | Service methods (`parkVehicle()`, `vacateSlot()`), public getters | Defines the explicit contractual API exposed to clients. |

---

## 3. Data Validation & Defensive Invariants
* **Slot State Invariants:**
  * `occupy()` throws `IllegalStateException` if the slot is already occupied, preventing double-parking.
  * Rejects null or blank registration numbers (`IllegalArgumentException`).
  * `vacate()` throws `IllegalStateException` if invoked on an already vacant slot.
* **Temporal & Tariff Validation:**
  * `calculateFee()` rejects negative durations ($< 0$), throwing `IllegalArgumentException`.

---

## 4. Wrapper Classes, Autoboxing & The Identity Trap

### 4.1 Autoboxing and Unboxing
* Converting primitives (`int`) to wrapper objects (`Integer`) is autoboxing; extracting primitives is unboxing.
* **Risks:**
  * **NullPointerException (NPE):** Unboxing an uninitialized wrapper object (`Integer duration = null; int d = duration;`) causes an immediate runtime NPE.
  * **The Integer Identity Trap (`==` vs `.equals()`):**
    * Java caches `Integer` objects in the range $[-128, 127]$.
    * `Integer(100) == Integer(100)` evaluates to `true` due to the flyweight JVM cache.
    * `Integer(300) == Integer(300)` evaluates to `false` because distinct objects are allocated on the heap!
    * **Resolution:** Always use `Objects.equals(a, b)` or `a.equals(b)` when comparing object wrappers.

---

## 5. Alternative Concept and Complexity Analysis

### Encapsulation with Validating Setters vs Public Fields with Caller Validation

Suppose all fields were made `public` (e.g. `public boolean occupied; public String vehicleRegNo;`), with validation left to calling code:

* **What Could Go Wrong?**
  1. **Broken Invariants:** Any developer, script, or concurrent thread can write `slot.occupied = true;` without setting `vehicleRegNo`, leaving the system in a corrupted zombie state.
  2. **Audit Bypass:** Direct field manipulation circumvents logging, security checks, and concurrency locks (`synchronized`).
* **Maintenance & Blast Radius:**
  * *Caller Validation:* If 15 different components (entry kiosk, mobile app, attendant terminal, batch loader, web dashboard) interact with `ParkingSlot`, the validation logic (`if (floor < 0 || reg == null)...`) must be copy-pasted across all 15 sites!
  * If a validation rule changes (e.g. allowing foreign registration plates), all 15 sites must be tracked down and rewritten. A single missed check introduces subtle production bugs.
  * *Encapsulated Design:* The rule is validated in exactly **1 place** inside `ParkingSlot`.
* **Performance / Time Cost:**
  * Calling a validated setter incurs only an infinitesimal standard method call overhead ($\sim 1$ ns, which JIT aggressively inlines).
  * The time cost of debugging corrupted state from public fields far exceeds any negligible nanosecond difference.
