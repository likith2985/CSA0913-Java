# Task 04: Design Classes, Objects and Constructors

## 1. Class Design and Constructor Justifications

### 1.1 Constructor Types & Roles
* **Default Constructor:**
  * Has no parameters. In domain entities like `ParkingTicket` or `Vehicle`, a no-argument constructor is intentionally **omitted** or marked private because an uninitialized parking ticket without a ticket ID, vehicle registration, and slot coordinates is an invalid domain state.
* **Parameterized Constructor:**
  * Initializes mandatory immutable fields (`ticketId`, `regNo`, `floor`, `slot`, `entryTime`, `isPassHolder`). Guarantees that every instantiated ticket is fully valid upon creation.
* **Overloaded Constructor & Constructor Chaining (`this(...)`):**
  * Allows convenient creation of tickets with common defaults (e.g., standard visitor entering at the current system time `LocalDateTime.now()` without a pass).
  * Chaining with `this(...)` eliminates code duplication by routing all initializations through the primary canonical constructor.
* **Copy Constructor:**
  * Accepts an existing object of the same class (`public ParkingTicket(ParkingTicket original)`).
  * **Use Case:** Re-issuing lost or damaged tickets. It creates a distinct new ticket instance cloned from the active session with an appended identifier (e.g., `"TKT-1001-DUP"`) while preserving slot coordinates and original entry timestamp.

---

## 2. Memory Organization: Stack vs Heap

```
Stack Memory                                    Heap Memory
+-----------------------+                       +---------------------------------------+
| Reference t1          | --------------------> | ParkingTicket Object                  |
| (Address: 0x7A01)     |                       | - ticketId = "TKT-1001"               |
+-----------------------+                       | - regNo = "TN-01-AA-1111"             |
| Reference t1Duplicate | --------------------> | - floor = 0, slot = 1                 |
| (Address: 0x8B04)     |                       | - entryTime = 2026-11-19T13:41:53     |
+-----------------------+                       +---------------------------------------+
                                                | ParkingTicket Object (Duplicate)      |
                                                | - ticketId = "TKT-1001-DUP"           |
                                                | - regNo = "TN-01-AA-1111"             |
                                                | - floor = 0, slot = 1                 |
                                                +---------------------------------------+
```
* **Stack Memory:**
  * Holds method frames, primitive local variables, and reference variables (pointers like `t1`, `t2`). Allocation and deallocation follow strict LIFO order as stack frames enter and exit.
* **Heap Memory:**
  * Stores all Java class instances (objects) and arrays dynamically allocated via `new`. Managed by the JVM Garbage Collector (GC).
  * `t1 == t1Duplicate` returns `false` because they occupy distinct memory addresses on the heap, even though their domain fields match.

---

## 3. Alternative Concept and Complexity Analysis

### Comparison: Overloaded Constructors vs Setters vs Static Factory Methods

| Design Pattern | Guarantee of Valid Object State | Mutability / Thread Safety | Creation Overhead (Time / Space for $n$ objects) |
| :--- | :--- | :--- | :--- |
| **Parameterized / Chained Constructors** | **Guaranteed**: All mandatory fields must be passed; fields can be `final`. | **Immutable & Thread-safe** | $O(n)$ allocations, minimal memory overhead ($1$ constructor call per object). |
| **Default Constructor + Setters (JavaBeans)** | **Poor**: Object exists in a partially initialized, inconsistent state between setter calls. | **Mutable & Prone to race conditions** | $O(n)$ allocations $+ 6n$ method invocations. Risk of forgetting a critical field like `regNo`. |
| **Static Factory Methods** | **Guaranteed**: High readability (`issueVisitorTicket()`, `issuePassHolderTicket()`). | **Immutable & Encapsulated** | $O(n)$ allocations, clear semantic intent with zero memory penalty. |

* **Analysis for $n$ `ParkingTicket` instances:**
  * All three approaches allocate $n$ heap objects $\rightarrow$ Space Complexity is $O(n)$.
  * The JavaBeans setter pattern incurs significant method call overhead ($O(k \cdot n)$ where $k$ is number of fields) and exposes mutable state.
  * Therefore, **overloaded chained constructors combined with static factory methods** provide the optimal trade-off of immutability, validation safety, and performance.
