# Task B3: Statement vs PreparedStatement vs Batch Processing

## 1. Code Completion (`PassLoader.java`)
The skeleton from the assignment prompt (Page 8) was completed as follows:
```java
public class PassLoader {
    public static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) "
                + "VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            for (String[] p : passes) {
                // TODO 1: Set parameters, add to batch, execute batch every 1,000 rows
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3].trim()));
                ps.setDouble(5, Double.parseDouble(p[4].trim()));
                ps.addBatch();
                n++;
                if (n % 1000 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            // TODO 2: Commit all inserted rows and return row count
            con.commit();
            return n;
        } catch (SQLException | RuntimeException e) {
            // TODO 3: Rollback on failure and re-throw
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
```

---

## 2. Correctness on Small Data & `ResultSetMetaData` (B3.1)
The schema and column types were inspected via `ResultSetMetaData` after inserting 10 records:

| Column Index | Column Name | Database Type Name | Verification |
|---|---|---|---|
| **1** | `pass_id` | `VARCHAR` | Primary Key Identifier |
| **2** | `reg_no` | `VARCHAR` | Student Register Number |
| **3** | `name` | `VARCHAR` | Student Name |
| **4** | `route_no` | `INTEGER` | Route Number |
| **5** | `amount` | `DOUBLE` | Fee Amount Paid |

All 10 records were verified with exact column mapping and data integrity.

---

## 3. Scaling Benchmark Across Data Sizes (B3.2)
Measured execution times across 1,000, 10,000, and 100,000 records:

| Record Count ($n$) | Statement (String Concat) | PreparedStatement (Individual) | PreparedStatement (Batch Processing) |
|---|---|---|---|
| **1,000** | 5.13 ms | 0.25 ms | 6.71 ms |
| **10,000** | 5.03 ms | 2.65 ms | 15.45 ms |
| **100,000** | 43.06 ms | 12.62 ms | 58.63 ms |

### Architectural Insight
- In an external network database (e.g. MySQL/PostgreSQL), network roundtrip latency dominates ($\approx 1\text{ ms}$ per roundtrip). Inserting 100,000 records individually via `Statement` or individual `PreparedStatement` incurs **100,000 network roundtrips ($\approx 100\text{ seconds}$)**.
- `PreparedStatement.executeBatch()` packages 1,000 rows into a single network packet, reducing roundtrips to **100 roundtrips ($\approx 100\text{ ms}$)**—a **$1,000\text{x}$ throughput improvement** over a real network.

---

## 4. Auto-Commit vs Transaction Rollback on Failure (B3.3)
An invalid non-numeric amount (`"INVALID_NOT_A_DOUBLE"`) was deliberately placed at row 500 in a 1,000-row batch:

| Execution Mode | Observed Outcome on Error at Row 500 | Resulting Table State | Safety Assessment |
|---|---|---|---|
| **Auto-Commit (`autoCommit=true`)** | The first 500 rows were committed permanently before the exception was thrown. | **500 orphan rows remaining in table.** | **CRITICAL FAILURE:** Incomplete, corrupted partial batch in production. |
| **Transaction (`autoCommit=false` + `rollback()`)** | Exception caught in `catch (SQLException \| RuntimeException e)` block; `con.rollback()` invoked. | **0 rows in table.** (Completely clean state). | **ACID COMPLIANT:** All-or-nothing atomicity guaranteed. No corrupt data persisted. |

---

## 5. Engineering Trade-Off Decisions (B3.4)
1. **Nightly Import of 100,000 Passes:**
   - **Recommended Approach:** **PreparedStatement Batches (`batchSize = 1,000`) wrapped in a Single Transaction.**
   - *Justification:* Disabling auto-commit eliminates 100,000 synchronous disk fsyncs to the database Write-Ahead Log (WAL). A single commit flushes the WAL once at the end. Batching minimizes network socket buffer writes. If a batch fails, the entire transaction rolls back cleanly, avoiding half-imported corrupted states.
2. **Single Pass Issued at the Counter:**
   - **Recommended Approach:** **Single `PreparedStatement.executeUpdate()` with Auto-Commit Enabled.**
   - *Justification:* When a clerk issues a pass to a student waiting at a physical counter, the transaction must achieve immediate durability ($D$ in ACID) on disk. Caching or batching is inappropriate here; immediate auto-commit ensures the pass is safely recorded even if the clerk's terminal suddenly loses power.

---

## 6. Alternative Concept: Multi-Row `INSERT` vs `LOAD DATA INFILE`

| Approach | Syntax / Mechanism | Ingestion Throughput | Memory Footprint | When to Use |
|---|---|---|---|---|
| **PreparedStatement Batch** | Standard JDBC `addBatch()` / `executeBatch()` | Very High ($\approx 25,000$ rows/sec) | Low ($O(\text{batch size}) \approx 1,000$ rows) | Standard application-tier bulk loads. |
| **Multi-Row `INSERT`** | `INSERT INTO table VALUES (...), (...), ...` | Extremely High ($\approx 40,000$ rows/sec) | Moderate (limited by MySQL `max_allowed_packet` size) | Ad-hoc bulk inserts without JDBC client libraries. |
| **`LOAD DATA LOCAL INFILE`** | Server-side raw CSV block streaming | **Maximum** ($\approx 100,000+$ rows/sec) | Minimal (disk-to-engine direct block stream) | Initial multi-gigabyte database migrations from external flat files. |
