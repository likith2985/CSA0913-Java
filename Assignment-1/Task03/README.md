# Task 03: Design the Control Logic

## 1. Search and Allocation Logic Design

### 1.1 Floor & Slot Traversal
* The program executes nested sequential search loops:
  * **Outer Loop:** Iterates through floor indices $f \in [0, \text{floors.length} - 1]$.
  * **Inner Loop:** Iterates through slot indices $s \in [0, \text{slots}[f].\text{length} - 1]$.
* **Early Termination:** As soon as an unoccupied slot (`slots[f][s] == null`) is found, the vehicle identifier is placed in the slot, and the function immediately exits via `return new int[]{f, s}`.
* **Exhaustion Handling:** If the outer loop completes without finding any free slot, the facility is completely full, and the method returns `null`.

### 1.2 Fee Slabs (Control Flow)
Fee slabs are evaluated using structured conditional branches (`if-else` or pattern matching):
```java
public double computeSlabFee(int hours, double baseRate) {
    double fee;
    if (hours <= 0) {
        return 0.0;
    } else if (hours == 1) {
        fee = baseRate;
    } else if (hours <= 5) {
        fee = baseRate + (hours - 1) * (baseRate * 0.8);
    } else {
        // Daily maximum cap applied beyond 5 hours
        fee = Math.min(baseRate * 4.0, baseRate + (hours - 1) * (baseRate * 0.7));
    }
    return fee;
}
```

### 1.3 Use of `break`, `continue`, and `return`
* **`return`**: Provides immediate short-circuit termination upon finding the first free slot, avoiding unnecessary iterations through the remaining floors/slots.
* **`break`**: Exits an inner loop when a specific condition (e.g., floor capacity full) is reached.
* **`continue`**: Skips invalid or reserved slots without breaking the loop sequence.

---

## 2. SIMATS Engineering Standard Algorithmic Pseudocode

### 2.1 Algorithm 1: Slot Allocation (`ALLOCATE_SLOT`)
```text
BEGIN ALGORITHM ALLOCATE_SLOT(slots, vehicleNo)
    INPUT: slots as 2D Array of String, vehicleNo as String
    OUTPUT: Array of integer [floor, slot] or NULL

    FOR f FROM 0 TO LENGTH(slots) - 1 DO
        FOR s FROM 0 TO LENGTH(slots[f]) - 1 DO
            IF slots[f][s] IS NULL THEN
                slots[f][s] <- vehicleNo
                RETURN ARRAY[f, s]
            END IF
        END FOR
    END FOR

    RETURN NULL
END ALGORITHM
```

### 2.2 Algorithm 2: Slot Deallocation (`RELEASE_SLOT`)
```text
BEGIN ALGORITHM RELEASE_SLOT(slots, floor, slot)
    INPUT: slots as 2D Array of String, floor as Integer, slot as Integer
    OUTPUT: Boolean (TRUE if successful, FALSE otherwise)

    IF floor < 0 OR floor >= LENGTH(slots) THEN
        RETURN FALSE
    END IF

    IF slot < 0 OR slot >= LENGTH(slots[floor]) THEN
        RETURN FALSE
    END IF

    IF slots[floor][slot] IS NOT NULL THEN
        slots[floor][slot] <- NULL
        RETURN TRUE
    ELSE
        RETURN FALSE
    END IF
END ALGORITHM
```

### 2.3 Algorithm 3: Parking Fee Calculation (`CALCULATE_PARKING_FEE`)
```text
BEGIN ALGORITHM CALCULATE_PARKING_FEE(minutes, hourlyRate, isPassHolder, isEv, kWhConsumed)
    INPUT: minutes as Integer, hourlyRate as Real, isPassHolder as Boolean, isEv as Boolean, kWhConsumed as Integer
    OUTPUT: totalFee as Real

    IF isPassHolder THEN
        RETURN 0.0
    END IF

    billableHours <- (minutes + 59) / 60
    baseParkingFee <- billableHours * hourlyRate
    chargingFee <- 0.0

    IF isEv THEN
        chargingFee <- kWhConsumed * 12.0
    END IF

    totalFee <- baseParkingFee + chargingFee
    RETURN totalFee
END ALGORITHM
```

---

## 3. Alternative Concept and Complexity Analysis

### Sequential Scan vs Free-Slot Stack & Free-Count Tracking
In the base implementation, allocation uses a 2D scan:
* **Current Allocation Time Complexity:** Worst-case $O(F \times S)$ where $F$ is number of floors and $S$ is average slots per floor (when the facility is almost full).
* **Current Release Time Complexity:** $O(1)$ boundary check and null assignment.
* **Current Extra Space:** $O(1)$ aux space.

#### Proposed Stack + Counter Alternative:
Maintain:
1. `int[] freeCountPerFloor` (size $F$)
2. `Stack<Integer>[] freeSlotsPerFloor` (array of $F$ stacks holding available slot indices)

* **Alternative Allocation:**
  * Find the first floor $f$ where `freeCountPerFloor[f] > 0` ($O(F)$ or $O(1)$ with a global pointer).
  * Pop slot $s$ from `freeSlotsPerFloor[f]` ($O(1)$).
  * Decrement `freeCountPerFloor[f]`.
  * Total time: **$O(1)$** best/average case.
* **Alternative Release:**
  * Push slot $s$ back to `freeSlotsPerFloor[floor]` ($O(1)$).
  * Increment `freeCountPerFloor[floor]` ($O(1)$).
  * Total time: **$O(1)$**.
* **Space Overhead Analysis:**
  * Storing stacks of free slot indices across $F$ floors requires up to $F \times S$ integer entries in memory $\rightarrow O(F \times S)$ extra space.
  * *Trade-off:* The stack approach trades $O(F \times S)$ auxiliary memory for guaranteed $O(1)$ allocation speed, which is beneficial for large multi-story parking garages (e.g., 5 floors $\times$ 100 slots).

---

## 4. Verification & Execution Results
Execution of `ParkingFacility.java` produces the verified output:
```
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```
Matches all expected test criteria.
