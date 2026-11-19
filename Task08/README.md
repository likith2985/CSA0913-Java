# Task 08: Apply Polymorphism

## 1. Compile-Time vs Runtime Polymorphism

### 1.1 Compile-Time (Static) Polymorphism: Method Overloading
* Occurs when multiple methods in the same class share the same name but possess different parameter lists (different types, counts, or orders).
* **Selection Mechanism:** The Java compiler binds the call at compile time based strictly on the static declared types of the arguments (Static Binding).
* **Example in System:**
  * `calculateFee(int minutes)`: Standard tariff based on duration.
  * `calculateFee(int minutes, double discountPercent)`: Overloaded variant applying student/staff promotional pass discounts.

### 1.2 Runtime (Dynamic) Polymorphism: Method Overriding & Upcasting
* **Upcasting:** Subclass references are assigned to superclass references without explicit casting:
  ```java
  Vehicle[] parked = { new TwoWheeler("TN01A1"), new Car("TN02B2"), new ElectricCar("TN03C3") };
  ```
* **Dynamic Method Dispatch:** When invoking `v.calculateFee(130)`:
  * Even though `v` has declared compile-time type `Vehicle`, the JVM determines the **actual concrete runtime type** on the heap (`TwoWheeler`, `Car`, or `ElectricCar`) and executes that specific class's `ratePerHour()` implementation.
* **Elimination of `if-else` Chains:** In procedural code, computing fees requires error-prone type branching:
  ```java
  // Anti-pattern: Fragile type branching
  if (type.equals("Car")) fee = hours * 30;
  else if (type.equals("TwoWheeler")) fee = hours * 10;
  ```
  Polymorphism delegates the decision to the objects themselves, allowing new vehicle types to be integrated with zero changes to caller code.

---

## 2. Dynamic Method Dispatch Call Trace

| Call Step | Property | Value / Description |
| :--- | :--- | :--- |
| 1 | Reference Expression | `v` (in loop iteration 3) |
| 2 | Declared Compile-Time Type | `Vehicle` |
| 3 | Actual Runtime Heap Type | `ElectricCar` |
| 4 | Method Invoked | `v.calculateFee(130)` (defined in `Vehicle`) |
| 5 | Internal Virtual Dispatch | `this.ratePerHour()` dispatched dynamically to `Car.ratePerHour()` returning `30.0` |
| 6 | Interface Check | `v instanceof Chargeable` evaluates to `true` |
| 7 | Additional Interface Call | `c.chargingFee(10)` invokes `ElectricCar.chargingFee(10)` returning `120.0` |
| 8 | Total Computed Fee | $90.0 + 120.0 = \text{Rs } 210.0$ |

---

## 3. Analysis: Why the Last Line Prints `true`

```java
System.out.println(new Car("TN02B2").equals(new ElectricCar("TN02B2"))); // prints true
```

### 3.1 Technical Explanation
In `Vehicle.java`, `equals()` is written as:
```java
@Override public boolean equals(Object o) {
    return o instanceof Vehicle && regNo.equals(((Vehicle) o).regNo);
}
```
1. `ElectricCar` is a direct subclass of `Car`, which extends `Vehicle`. Therefore, `o instanceof Vehicle` evaluates to `true`.
2. Both instances have identical registration numbers (`"TN02B2"`).
3. The comparison `regNo.equals(...)` evaluates to `true`.

### 3.2 Is this the correct behaviour for our system?
* **Domain Identity Perspective (CORRECT):** In a campus parking facility, a vehicle is physically identified and tracked by its **State Registration Number (License Plate)**. A single physical vehicle cannot simultaneously exist as both a standard Car and an ElectricCar under the same registration number. If two records in the system share the same registration number, they logically identify the same vehicle. Thus, returning `true` correctly models physical reality and prevents duplicate slot issuance or fraudulent re-entry under an altered vehicle category.
* **Class Equivalence Perspective (CAUTION):** If the system uses `equals()` to verify slot compatibility (e.g., whether a vehicle is strictly an EV eligible for an EV charging bay), comparing different classes as equal violates strict class symmetry (`getClass() == o.getClass()`). For tariff and billing, distinguishing them by concrete class or interfaces (`Chargeable`) is necessary.

---

## 4. Alternative Concept and Complexity Analysis

### Dynamic Method Dispatch vs `instanceof` Chains and Downcasting

Suppose polymorphism were eliminated and replaced with explicit `instanceof` type checking:
```java
double fee = 0;
if (v instanceof TwoWheeler tw) {
    fee = tw.calculateFee(minutes);
} else if (v instanceof ElectricCar ec) {
    fee = ec.calculateFee(minutes) + ec.chargingFee(10);
} else if (v instanceof Car c) {
    fee = c.calculateFee(minutes);
}
```

* **Time Complexity per Call ($k$ Vehicle Types):**
  * *Dynamic Method Dispatch:* **$O(1)$ constant time**. The JVM looks up the method offset in the class's Virtual Method Table (vtable) and performs an indirect jump.
  * *`instanceof` Chains:* **$O(k)$ linear time worst case**. The program must sequentially evaluate up to $k$ class hierarchy type checks and branches. Order matters: placing `Car` before `ElectricCar` would silently trigger bugs!
* **Code Size & Maintenance Burden:**
  * Dynamic dispatch keeps code clean and modular.
  * `instanceof` cascades introduce massive code duplication across entry gates, exit booths, report generators, and billing engines, making maintenance brittle whenever a new vehicle category is introduced.
