# Q3 – Engineer the hashCode–equals Contract

## 1. Problem Context – Event Attendance De-duplication
A college fest records attendance across multiple gates into a `HashSet<Student>`. A junior developer implemented `equals()` based on `regNo` but **failed to override `hashCode()`**.

---

## 2. Experimental Investigation of the Contract

### 2.1 Test 1: Where the Broken Class Deceptively Appears to Work
* **Setup:** Scanning the exact same Java object reference `s1` twice.
* **Prediction:** Size = 1.
* **Observed Size:** 1.
* **Explanation:** In `HashSet.add(e)` (delegating to `HashMap.putVal`):
  ```java
  if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
  ```
  Because `(k = p.key) == key` checks reference identity first, adding the exact same instance evaluates to `true` on the identity check without requiring matching hash codes across instances.

### 2.2 Test 2: Deliberately Exposing Duplicate Failure
* **Setup:** Scanning two distinct heap instances representing the same student:
  `new Student("192311001", "Arun")` twice.
* **Prediction:** Size = 3 (Duplicates retained).
* **Observed Size:** **3** (`[192311002:Divya, 192311001:Arun, 192311001:Arun]`).
* **Explanation:** Because `hashCode()` was not overridden, each `new Student` inherited `Object.hashCode()`, which defaults to JVM internal memory addresses (`System.identityHashCode`). The two identical students generated different hash codes and landed in different buckets. `HashSet` only invokes `equals()` when two elements hash to the **exact same bucket**. Thus, `equals()` was never even executed!

### 2.3 Test 3: Mutating the Key After Insertion (The Ghost Element Trap)
* **Setup:** A student with mutable `regNo` is added to the set with `"192311001"`. The field is subsequently modified to `"192311999"`.
* **Observed Behavior:**
  * `set.contains(student)` returns `false`.
  * `set.remove(student)` returns `false`.
  * `set.size()` remains 1!
* **Explanation:** The object remains trapped in the bucket calculated from its original hash code. When queried with the mutated state, `HashSet` computes the new bucket index, finds it empty or non-matching, and fails to find or remove the element. The element becomes an un-removable "ghost object", causing a memory leak.
* **Rule:** Keys stored in hash-based collections **MUST BE IMMUTABLE**.

---

## 3. How `HashSet` Uses `hashCode()` and `equals()`

```
Insert Element E
       │
       ▼
Compute hash = E.hashCode() ^ (hash >>> 16)
       │
       ▼
Bucket Index = (capacity - 1) & hash
       │
       ▼
Is Bucket Empty? ──── Yes ───► Store E in new node (Size++)
       │
       No (Collision)
       │
       ▼
Traverse linked list / Red-Black Tree in Bucket:
For each node N:
   Does (N.hash == E.hash) AND (N.key == E OR E.equals(N.key))?
       ├── Yes ──► Duplicate found! Do not insert; return false.
       └── No  ──► Check next node.
       │
Reached end of bucket? ──► Append E to bucket (Size++)
```

---

## 4. Complete the Code Verification
File: `Attendance.java`

* **Output BEFORE completing TODO 1:**
  ```
  Unique students: 3
  ```
* **Output AFTER completing TODO 1:**
  ```
  Unique students: 2
  ```
* **Explanation:** Overriding `hashCode()` ensures that whenever two `Student` objects have equal `regNo`, they produce the exact same integer hash code, routing them to the same bucket where `equals()` detects the collision and discards the duplicate.

---

## 5. Alternative Concept and Complexity Analysis

| Structure | Deduplication Mechanism | Scan / Insert Time | Lookup Time | Space for $n$ Unique Students |
| :--- | :--- | :--- | :--- | :--- |
| **`HashSet<Student>`** | Relies on `hashCode()` and `equals()` | **$O(1)$** average | **$O(1)$** average | $1$ `Node` per student ($\approx 32$ bytes) |
| **`TreeSet<Student>`** | Relies on `Comparator<Student>` or `Comparable` | **$O(\log n)$** | **$O(\log n)$** | $1$ `Entry` per student ($\approx 40$ bytes, left/right/parent/color pointers) |
| **`HashMap<String, Student>`** | Keyed by `String regNo` | **$O(1)$** average | **$O(1)$** average | $1$ `Node` holding key + value references |

* **Analysis:** `TreeSet` eliminates dependency on `hashCode()` and automatically maintains sorted order by registration number, but costs $O(\log n)$ per scan. `HashSet<Student>` with a correctly overridden immutable `hashCode()` provides optimal $O(1)$ constant-time throughput.
