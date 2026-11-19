# Task 09: Introduce Abstraction

## 1. Abstract Classes vs Interfaces Design Rationale

### 1.1 Why `Vehicle` is an Abstract Class
* **State Ownership:** Every vehicle possesses an immutable physical state (`private final String regNo`) and constructor initialization logic that must execute when any subtype is instantiated.
* **Interfaces cannot hold non-static instance state** or constructors in Java.
* **Template Method Pattern:** The master fee computation rule:
  $$\text{hours} = (\text{minutes} + 59) / 60$$
  is identical for all vehicle types and can be implemented directly as a concrete template method in the abstract class, preventing subclasses from altering the duration-to-hours billing algorithm.

### 1.2 Why `Chargeable` is an Interface
* **Cross-Cutting / Orthogonal Capability:** Charging is not a universal vehicle trait; only certain vehicles have batteries (e.g. Electric Cars, E-Bikes, Hybrid Delivery Vans).
* In Java, a class can inherit from only one superclass (Single Class Inheritance). If `Chargeable` were an abstract class, an `ElectricCar` could not extend `Car` (which extends `Vehicle`) while simultaneously inheriting from `Chargeable`!
* By defining `Chargeable` as an interface, any vehicle in the hierarchy can adopt the charging contract without disturbing the primary inheritance hierarchy.

### 1.3 Default and Static Methods in Interfaces
* **Default Methods (`default double estimateChargingMinutes(...)`):**
  * Introduced in Java 8 to allow evolving interfaces with new capabilities without breaking backward compatibility for existing implementing classes.
* **Static Methods (`static double getGreenTariffDiscount()`):**
  * Provide cohesive utility operations directly linked to the domain interface namespace without needing separate utility classes.

---

## 2. Alternative Concept and Complexity Analysis

### Inverting the Design: Vehicle as Interface & Chargeable as Abstract Class

Suppose `Vehicle` were declared as an `interface` with default methods, and `Chargeable` were an `abstract class`:

```java
// Alternative Inverted Design
public interface Vehicle {
    String getRegNo(); // Cannot have private final String regNo field!
    default double calculateFee(int minutes) { ... }
}

public abstract class ChargeableVehicle {
    private double batteryCapacity;
    public abstract double chargingFee(int kWh);
}
```

* **What Can No Longer Be Expressed?**
  1. **Encapsulated Instance State & Constructors:** The `Vehicle` interface cannot hold the `regNo` instance variable or have a constructor. Every single implementing class (`Car`, `TwoWheeler`, `ElectricCar`) would be forced to duplicate the `private String regNo` declaration, constructor, and getter, completely destroying DRY (Don't Repeat Yourself) principles.
  2. **Multiple Inheritance Limitation:** Since Java prohibits multiple class inheritance, a class extending `ChargeableVehicle` cannot extend `Car` or `Motorcycle`. A common vehicle base cannot be shared.
* **Time Complexity per Method Call:**
  * In the standard design (`abstract class Vehicle`), method calls use `invokevirtual` bytecode instruction, which looks up the vtable with a fixed constant offset ($O(1)$ constant time, 1–2 CPU cycles).
  * In interface calls (`invokeinterface`), the JVM must search the interface table (itable) or rely on monomorphic inline caching. While modern JIT compilers optimize this well, worst-case megamorphic interface dispatch carries slightly higher overhead than direct vtable dispatch.
* **Space Complexity per Object:**
  * Abstract class with fields stores fields directly in the object layout contiguous with the object header.
  * Inverted design causes code duplication without space savings, and forces multiple levels of wrapping or interface tables.
* **Verdict:** The chosen architecture—**Abstract Class for core identity & state (`Vehicle`) + Interfaces for optional capabilities (`Chargeable`, `Payable`)**—is the textbook optimal object-oriented architecture in Java.
