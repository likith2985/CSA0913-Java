# Task 02: Choose the Data Representation

## 1. Data Type Selection Rationale

| Field | Chosen Java Data Type | Rationale |
| :--- | :--- | :--- |
| **Slot Number** | `int` | Slots are small, non-negative ordinal values ($0$ to several hundreds). `int` provides direct array index compatibility with zero memory waste. |
| **Vehicle Number** | `String` | Vehicle registration numbers contain letters, digits, and hyphens (e.g., `"TN01AB1111"`). Requires immutability, `equals()`, and string formatting. |
| **Entry Time** | `java.time.LocalDateTime` | Standard modern Java Time API representing date and time without timezone skew. Also converts smoothly to epoch seconds/minutes. |
| **Duration** | `int` or `long` (minutes) | Durations stored as integer minutes prevent floating-point cumulative rounding errors during fee calculation. |
| **Parking Fee** | `double` | Currency values require decimal fractional precision (e.g., 30.50). Calculations follow financial rounding rules. |

---

## 2. Array Structure Representation

### Jagged 2D Array vs Rectangular 2D Array vs 1D Array
* **Chosen Structure:** **Jagged 2D Array** (`String[][] slots = new String[floors][]`).
* **Justification:** Real-world parking facilities rarely have identical numbers of slots per floor. For example:
  * Floor 0 (Ground) might have 20 slots + driveway.
  * Floor 1 might have 50 standard slots.
  * Floor 2 might have 15 EV charging bays.
  * A rigid rectangular array (`String[F][S]`) wastes significant heap memory by over-allocating unused slot references on smaller floors.
  * A jagged array allows each floor row to be dynamically sized to its exact capacity:
    ```java
    slots = new String[slotsPerFloor.length][];
    for (int f = 0; f < slotsPerFloor.length; f++) {
        slots[f] = new String[slotsPerFloor[f]];
    }
    ```

---

## 3. Type Conversion, Casting Risks & Integer Division

### Risk Analysis
The business rule states: *"Every started hour is charged in full."*
* If parking duration is 65 minutes:
  * **Naive Integer Division:** `int hours = minutes / 60;` $\rightarrow 65 / 60 = 1$ (truncated integer division).
    * Result: Customer is billed for only 1 hour instead of 2 hours, causing serious revenue underbilling!
  * **Incorrect Cast:** `int hours = (int) (minutes / 60);` performs integer division before casting, leaving the bug intact.
* **Robust Solutions:**
  1. **Pure Integer Arithmetic (Ceiling Formula):**
     $$\text{hours} = \frac{\text{minutes} + 59}{60}$$
     For 65 minutes: $(65 + 59) / 60 = 124 / 60 = 2$ hours.
     For 60 minutes: $(60 + 59) / 60 = 119 / 60 = 1$ hour.
     For 1 minute: $(1 + 59) / 60 = 60 / 60 = 1$ hour.
  2. **Floating-point Ceiling:**
     $$\text{hours} = (\text{int}) \lceil \text{minutes} / 60.0 \rceil$$

---

## 4. Alternative Concept and Complexity Analysis

### Single 1D Array vs ArrayList<String[]> vs Jagged Array
Suppose slots are mapped to a 1D array using $index = floor \times S + slot$:

1. **When floors have equal slots ($S$):**
   * Locating slot $(f, s)$ takes $O(1)$ arithmetic: $index = f \times S + s$.
   * Memory is a single contiguous block of references.
2. **When floors have different numbers of slots ($S_0 \neq S_1 \dots$):**
   * The direct formula $f \times S + s$ breaks down.
   * A lookup table (Prefix Sum array) of floor offsets is required:
     $$index = \text{prefixSum}[f] + s$$
   * Locating slot remains $O(1)$ with 1 additional array access.
   * However, resizing or restructuring a floor requires re-allocating and copying the entire 1D array ($O(\sum S_i)$).
3. **With `ArrayList<String[]>`:**
   * Locating slot $(f, s)$: `list.get(f)[s]` is $O(1)$ time.
   * Space overhead: ArrayList object header ($16$ bytes) + capacity buffer array ($24 + C \times 8$ bytes) + $F$ inner array headers.
4. **Summary Comparison:**

| Structure | Slot Lookup Time | Space Overhead | Flexibility for Uneven Floors |
| :--- | :--- | :--- | :--- |
| **Jagged Array `String[][]`** | $O(1)$ | $1$ outer header $+ F$ inner headers | High (native Java jagged arrays) |
| **Flattened 1D `String[]`** | $O(1)$ | $1$ array header + prefix array | Rigid (requires offset math) |
| **`ArrayList<String[]>`** | $O(1)$ | Highest (object wrappers) | High |

---

## 5. Input Ingestion Strategy
* **Interactive Mode:** Uses `java.util.Scanner` bound to standard input (`System.in`) for live terminal prompts.
* **Batch / Automated Mode:** Supports command-line arguments (`String[] args`) or file redirection for testing pipelines.
