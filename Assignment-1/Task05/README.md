# Task 05: Apply static Members and the Object Life Cycle

## 1. Static vs Instance Members Rationale

### 1.1 Static Variables
* **`ticketCounter` / `globalOccupancy`:**
  * Must be `static` because ticket sequence generation and facility-wide occupancy are global system concerns, not properties of any individual ticket or vehicle.
  * *Why an instance variable is wrong:* If `ticketCounter` were an instance variable inside `ParkingTicket`, each new `ParkingTicket` instantiation would initialize its own independent counter (e.g. at 0 or 1000), producing duplicate ticket numbers and violating business rule #2 (*"a ticket number is never reused"*).

### 1.2 Static Methods and Static Blocks
* **Static Initialization Block (`static { ... }`):**
  * Runs once when the class is first loaded by the JVM ClassLoader. Used to configure immutable facility codes (`CAMPUS-MAIN`), load tariff configuration, or establish database connections.
* **Static Methods:**
  * Functions like `Ticket.getNextSequence()` or utility converters (`ParkingUtils.minutesToHours(int)`) operate without needing an object instance.

---

## 2. Object Lifecycle and Garbage Collection (GC)

### 2.1 Object Lifecycle Phases
1. **Creation & Allocation:** Object is instantiated on the JVM Heap via `new ParkingTicket(...)`; constructor initializes fields.
2. **Active Use:** Reference is held in the facility's active slot matrix (`slots[floor][slot] = ticket.getRegNo()`) or active ticket map.
3. **Exit & Dereferencing:** Vehicle departs; `release(floor, slot)` nullifies the slot entry (`slots[floor][slot] = null`), and ticket settlement completes.
4. **Eligibility for GC:** When all stack frames finish and references to the ticket object are cleared (`t = null`), the object becomes unreachable from any Garbage Collection Root (GC Root).
5. **Garbage Collection:** The JVM Garbage Collector (e.g., G1 GC, ZGC) reclaims heap memory occupied by the orphaned object during its next cycle.

### 2.2 Object Passing Semantics in Java
* Java is strictly **pass-by-value**.
* When an object reference (e.g., `processTicket(Ticket t)`) is passed into a method, the *value of the pointer/reference* is copied onto the called method's stack frame.
* Mutating fields on the referenced object (`t.settle()`) modifies the actual object on the heap, visible to the caller.
* Reassigning the parameter (`t = null`) only alters the local reference copy; the caller's reference remains completely untouched.

---

## 3. Alternative Concept and Complexity Analysis

### Multi-Facility Architecture: North vs South Gate Ticket Counters

Suppose the university runs two independent facilities: **North Gate** and **South Gate**.

#### Scenario A: Global Static Counter
* **Design:** Single `private static int ticketCounter` across all facilities.
* **Behavior:** North gate issues `TKT-1001`, South gate issues `TKT-1002`, North issues `TKT-1003`.
* **Pros:** Guaranteed global uniqueness without coordination.
* **Cons:** Interleaved numbering makes it difficult to audit individual gate traffic volume directly from ticket IDs.
* **Space:** $O(1)$ space (1 static integer in class metadata).

#### Scenario B: Instance Counter per Facility
* **Design:** Each `ParkingFacility` holds an instance variable `private int localCounter` and a gate prefix (`"NF"` or `"SF"`).
* **Behavior:**
  * North Gate issues: `NF-0001`, `NF-0002`, `NF-0003`...
  * South Gate issues: `SF-0001`, `SF-0002`...
* **Pros:** Complete isolation of facility statistics; clear operational auditing per gate; eliminates contention on a single shared variable in multi-threaded environments.
* **Space for $k$ facilities and $n$ tickets:**
  * Each of the $k$ facilities stores an integer counter $\rightarrow k \times 4$ bytes $\rightarrow O(k)$ space.
  * Tickets store the formatted string reference $\rightarrow O(n)$ space.
  * *Verdict:* The instance-per-facility approach with prefixed ticket IDs provides superior operational modularity while retaining $O(1)$ ticket issuance time and $O(k)$ minimal memory footprint.
