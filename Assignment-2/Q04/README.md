# Q4 – Choosing a Map for a Pharmacy Inventory

## 1. Problem Context – Hospital Pharmacy Stock Lookup
A hospital pharmacy manages tens of thousands of medicines identified by alphanumeric codes.
* **Operational Workload:** Hundreds to thousands of high-frequency stock lookups daily.
* **Evening Reporting Requirements:**
  1. **Report 1:** All medicines in **Code Order** (natural / alphabetical sorting).
  2. **Report 2:** All medicines in **Insertion Order** (the chronological order in which they were added during the day).

---

## 2. Pre-Implementation Theoretical Analysis

| Map Implementation | Expected `get(k)` Cost | Expected `put(k, v)` Cost | Iteration Cost | Natural Report Produced | Extra Work if Wrong Map Chosen |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`HashMap`** | **$O(1)$** average | **$O(1)$** average | $O(\text{Capacity} + N)$ | Neither (Arbitrary bucket order) | **Code Order:** Requires copying all values to a `List` and running `Collections.sort()` ($O(N \log N)$ time, $O(N)$ extra memory).<br>**Insertion Order:** Impossible without an external tracking list. |
| **`TreeMap`** | **$O(\log N)$** | **$O(\log N)$** | $O(N)$ | **Report 1: Code Order** (Natural key sorting via Red-Black Tree) | **Insertion Order:** Impossible without attaching external monotonic timestamps/counters. |
| **`LinkedHashMap`** | **$O(1)$** average | **$O(1)$** average | **$O(N)$** | **Report 2: Insertion Order** (Maintains doubly-linked list through entries) | **Code Order:** Requires copying and running `Collections.sort()` ($O(N \log N)$). |

---

## 3. Empirical Experimental Evidence

### 3.1 100,000 Lookups Benchmark Across Three Sizes

| Dataset Size ($N$) | `HashMap` Time (ms) | `LinkedHashMap` Time (ms) | `TreeMap` Time (ms) | Latency per Lookup (HashMap vs TreeMap) |
| :--- | :--- | :--- | :--- | :--- |
| **$N = 500$** | $11.76$ ms ($117$ ns/op) | $9.09$ ms ($90$ ns/op) | $21.10$ ms ($211$ ns/op) | HashMap is **$1.8\times$ faster** |
| **$N = 5,000$** | $2.46$ ms ($24$ ns/op) | $2.23$ ms ($22$ ns/op) | $26.03$ ms ($260$ ns/op) | HashMap is **$10.6\times$ faster** |
| **$N = 50,000$** | **$5.25$ ms** ($52$ ns/op) | **$7.98$ ms** ($79$ ns/op) | **$92.77$ ms** ($927$ ns/op) | HashMap is **$17.7\times$ faster** |

### 3.2 Evening Report Generation Benchmark ($N = 50,000$)
* **Report 1 (Code Order / Alphabetical):**
  * `TreeMap`: **$16.274$ ms** (Natively sorted via in-order tree traversal; 0 extra sorting overhead).
  * `HashMap`: **$33.983$ ms** (Demands dumping entries to list and running $O(N \log N)$ `Collections.sort`).
  * `LinkedHashMap`: **$34.895$ ms** (Requires explicit $O(N \log N)$ sorting).
* **Report 2 (Insertion Order / Chronological):**
  * `LinkedHashMap`: **$15.860$ ms** (Natively ordered via doubly-linked pointers; 100% match).
  * `HashMap` & `TreeMap`: **CANNOT produce insertion order** without auxiliary data structures.

---

## 4. Scalability Inquiry & Architectural Deployment Decision

### 4.1 Acceptable Response Time Threshold
* In high-throughput hospital dispensing kiosks, point-of-sale lookup latency must remain **$< 1\,\mu\text{s}$ ($1000$ ns)** to prevent UI freezing.
* `HashMap` and `LinkedHashMap` achieved **$24$–$79$ ns/lookup**, easily exceeding this bar.
* In contrast, `TreeMap` scaled to **$927$ ns/lookup** at $50,000$ items, near the acceptable threshold. The cost in `TreeMap` stems from:
  1. Pointer chasing across tree nodes (`Node.left`, `Node.right`, `Node.parent`), incurring frequent CPU L1/L2 cache misses.
  2. String `compareTo()` string character comparisons at each tree level ($\approx \log_2(50000) \approx 16$ node comparisons per lookup).

### 4.2 Production Deployment Architecture
* **Selected Structure:** **`LinkedHashMap<String, Medicine>`**
  * Delivers blazing **$O(1)$ lookup performance** during the daytime rush ($79$ ns/op).
  * Natively produces **Report 2 (Insertion Order)** with zero extra CPU overhead.
  * For the single daily evening **Report 1 (Code Order)**, copying and sorting once takes only **$34$ ms**, an imperceptible cost on a once-a-day batch job.

### 4.3 Why `Hashtable` Is Deprecated / Not Preferred in Modern Code
1. **Thread Contention:** Every method in `Hashtable` (`get`, `put`, `containsKey`) is synchronized at the whole-table monitor level. Concurrent reads block each other. Modern multithreaded systems use `ConcurrentHashMap` with striped lock-free CAS buckets.
2. **Null Rejection:** `Hashtable` throws `NullPointerException` on `null` keys or values; `HashMap` supports `null` key and values cleanly.
3. **Legacy API:** Employs obsolete `Enumeration` rather than modern standard fail-fast `Iterator` / Stream interfaces.

---

## 5. Alternative Concept and Complexity Analysis

### Sorted Array with `Arrays.binarySearch()` vs Hash/Tree Maps

Suppose inventory is stored in a sorted array `Medicine[]`:
* **Lookup Time:** **$O(\log N)$** via binary search. Contiguous array elements offer superior CPU cache locality compared to `TreeMap` node pointers.
* **Insertion Time:** **$O(N)$**. Adding a new medicine requires finding insertion index ($O(\log N)$) and shifting subsequent array elements right via `System.arraycopy` ($O(N)$). Highly inefficient for active stock updates.
* **Report 1 (Code Order):** **$O(N)$** direct scan (already sorted).
* **Report 2 (Insertion Order):** Impossible without an additional array tracking ingestion sequence.
* **Space Overhead:** Smallest memory footprint (only 8 bytes per array slot, no node wrapper objects).
