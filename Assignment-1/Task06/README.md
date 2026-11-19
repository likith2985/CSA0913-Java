# Task 06: Design the Inheritance Hierarchy

## 1. Hierarchy Architecture & Inheritance Types

```
                +-------------------------+
                |    <<abstract class>>   |
                |         Vehicle         |
                +-------------------------+
                | - regNo: String         |
                +-------------------------+
                | + getRegNo(): String    |
                | # ratePerHour(): double |
                | + calculateFee(int): dbl|
                +-------------------------+
                             ^
                             |
            +----------------+----------------+
            | (Hierarchical)                  | (Hierarchical)
+-----------------------+         +-----------------------+
|      TwoWheeler       |         |          Car          |
+-----------------------+         +-----------------------+
| # ratePerHour(): double|        | # ratePerHour(): double|
+-----------------------+         +-----------------------+
                                              ^
                                              | (Multilevel)
                                  +-----------------------+
                                  |      ElectricCar      |
                                  +-----------------------+
                                  | + chargingFee(int):dbl|
                                  +-----------------------+
```

### 1.1 Superclass and Subclasses
* **Superclass:** `Vehicle` (Abstract). Defines core registration state (`regNo`) and common fee template method `calculateFee(int minutes)`.
* **Subclasses:**
  * `TwoWheeler`: Models scooters and motorcycles. Overrides `ratePerHour()` to return Rs 10.0.
  * `Car`: Models conventional motorcars. Overrides `ratePerHour()` to return Rs 30.0.
  * `ElectricCar`: Models electric vehicles with charging capabilities. Inherits from `Car` and implements `Chargeable`.
* **Inheritance Types:**
  * **Hierarchical Inheritance:** Multiple classes (`TwoWheeler`, `Car`) extend the single superclass `Vehicle`.
  * **Multilevel Inheritance:** `ElectricCar` extends `Car`, which extends `Vehicle`.

### 1.2 Role of `super` Keyword
1. **`super(regNo)` in Constructors:** Invokes the direct superclass constructor, ensuring base class invariants are established before subclass logic executes.
2. **`super.method()`:** Allows a subclass to access overridden superclass methods if needed.

---

## 2. Constructor Execution Order Trace

When `new ElectricCar("TN-03-EVCAR-3", 60.0)` is invoked, the JVM executes constructors strictly top-down along the inheritance chain:

1. `java.lang.Object()` $\rightarrow$ Root object initialization.
2. `Vehicle(String regNo)` $\rightarrow$ Initializes base field `this.regNo = "TN-03-EVCAR-3"`.
3. `Car(String regNo)` $\rightarrow$ Executes car-specific initialization.
4. `ElectricCar(String regNo, double battery)` $\rightarrow$ Initializes `batteryCapacityKWh` and completes construction.

### Execution Output Evidence:
```text
Instantiating ElectricCar('TN-03-EVCAR-3'):
  -> [1] Vehicle constructor executing: regNo = TN-03-EVCAR-3
  -> [2] Car constructor executing
  -> [3] ElectricCar constructor executing: battery = 60.0 kWh
```

---

## 3. Alternative Concept and Complexity Analysis

### Inheritance ("is-a") vs Composition ("has-a")

Instead of class inheritance, suppose a single `Vehicle` class contains a reference to a `VehicleType` strategy object:
```java
public class Vehicle {
    private final String regNo;
    private final VehicleType type; // Composition: has-a

    public double calculateFee(int minutes) {
        return ((minutes + 59) / 60) * type.getRatePerHour();
    }
}
```

* **Adding a New Vehicle Type:**
  * *Inheritance:* Requires creating a new subclass (e.g., `class Truck extends Vehicle`) overriding `ratePerHour()`. Existing classes remain completely untouched (Open-Closed Principle).
  * *Composition:* Requires instantiating a new `VehicleType` configuration object or enum instance (e.g., `new VehicleType("Truck", 50.0)`). Adding types can even happen dynamically at runtime from a database without recompiling classes!
* **Time Complexity of Fee Calculation:**
  * *Inheritance:* Direct virtual method call via vtable dispatch $\rightarrow O(1)$ time ($\sim 1$–$2$ CPU cycles).
  * *Composition:* Dereferences the `Vehicle` reference then dereferences `VehicleType` pointer $\rightarrow O(1)$ time, but involves one extra pointer hop (indirection).
* **Space Complexity per Object:**
  * *Inheritance:* 1 object header ($12$–$16$ bytes) $+$ `regNo` reference ($8$ bytes) $\approx 24$–$32$ bytes.
  * *Composition:* `Vehicle` object ($24$ bytes) $+$ additional reference to `VehicleType` ($8$ bytes) $+$ `VehicleType` object ($24$ bytes if instantiated separately) $\approx 48$–$56$ bytes (unless `VehicleType` is a shared singleton/flyweight enum).
* **Conclusion:** For static domain models with specialized behaviors (like EV charging calculations), class inheritance with interfaces (`Chargeable`) is idiomatic and clean; composition is superior when tariff rates must be reconfigured dynamically at runtime.
