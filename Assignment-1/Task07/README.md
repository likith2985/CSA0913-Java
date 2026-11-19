# Task 07: Override and Specialise Behaviour

## 1. Method Overriding Rules and `@Override` Annotation

### 1.1 Overriding Rules in Java
1. **Method Signature:** The method name, parameter types, and parameter order must be identical to the superclass method.
2. **Return Type:** Must be identical or a covariant subtype of the superclass method's return type.
3. **Access Modifier:** Cannot be more restrictive than the superclass method (e.g., if superclass is `protected`, subclass can be `protected` or `public`, but never `private` or package-private).
4. **Checked Exceptions:** An overriding method cannot declare new or broader checked exceptions than the superclass method.
5. **Role of `@Override`:**
   * Compiler-enforced safety check. If the method signature accidentally differs (e.g. typing `rateperHour()` instead of `ratePerHour()`), the compiler emits an error instead of treating it as an unintentional overload.

### 1.2 Justification for the `final` Keyword
* **`final` Field (`private final String regNo`):**
  * Ensures immutable identity. A vehicle's license plate cannot be altered after creation.
* **`final` Method (`public final double calculateFee(int minutes)`):**
  * Implements the **Template Method Design Pattern**. The overarching billing formula (ceiling arithmetic $\times$ rate) is locked so no malicious or buggy subclass can alter how time is converted to billable hours.
* **`final` Class:**
  * Used when a class is complete and must not be extended (e.g., utility classes or immutable value records).

### 1.3 `equals()`, `hashCode()`, and `toString()` Contract
* Two `Vehicle` instances with the same registration number represent the exact same physical vehicle on campus.
* **`equals()`:** Overridden to compare `regNo` equality:
  ```java
  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Vehicle vehicle)) return false;
      return Objects.equals(regNo, vehicle.regNo);
  }
  ```
* **`hashCode()`:** Overridden using `Objects.hash(regNo)` to ensure that equal objects return identical hash codes, enabling correct deduplication in `HashSet` and `HashMap`.
* **`toString()`:** Overridden to output human-readable diagnostic information.

---

## 2. Alternative Concept and Complexity Analysis

### Dynamic Overriding vs Monolithic `switch(type)`

Suppose fee calculation were handled by a central static `switch(vehicleType)` statement instead of polymorphic method overriding:

```java
public static double calculateFee(String type, int minutes) {
    int hours = (minutes + 59) / 60;
    return switch (type) {
        case "TwoWheeler" -> hours * 10.0;
        case "Car" -> hours * 30.0;
        case "ElectricCar" -> hours * 30.0;
        default -> throw new IllegalArgumentException("Unknown: " + type);
    };
}
```

* **Number of Classes Modified on New Vehicle Type Addition:**
  * *Method Overriding:* **0 existing classes modified**. A new class `Truck extends Vehicle` is written in isolation (遵從 Open-Closed Principle).
  * *Switch-based:* **Requires modifying existing core calculator class**, recompiling, and re-running regression tests across the entire application. Risk of missing cases or breaking existing logic.
* **Execution Time Complexity per Fee Calculation for $k$ Vehicle Types:**
  * *Method Overriding:* $O(1)$ constant time via JVM virtual method table (vtable) dispatch (typically 1–2 CPU clock cycles).
  * *Switch-based:* String switch compiles to a hash-based lookup (`tableswitch` / `lookupswitch`) followed by `equals()` checks: $O(1)$ average, but degraded branch prediction and higher instruction cache misses as $k$ expands.
* **Summary Verdict:** Method overriding achieves true decoupling, zero-risk extensibility, and optimal CPU branch execution.
