# Assignment 5 - Part A: Persistence Design & Algorithmic Pseudocode
**Course Code:** CSA0913 - Java Programming  
**Course Outcome:** CO5 - Data Persistence Engineering (Files and Databases)  
**Total Marks for Part A:** 25 Marks  
**Assessment Standard:** SIMATS Engineering - Standard for Algorithmic Pseudocode

---

## Task A1 – Design a File Format (7 Marks - K4)

### A1.1 – Record Representation (2 Marks)
The university transport office issues ~8,000 bus passes per year. Each pass record contains:
- `pass_id` (Alphanumeric identifier, e.g., "P-10482")
- `reg_no` (Student register number, e.g., "192311042")
- `name` (Student full name, e.g., "R. Kumar, Jr.")
- `route_no` (Integer route number, e.g., 14)
- `boarding_stop` (Bus stop name, e.g., "Tambaram Sanatorium")
- `validity_date` (ISO-8601 Date string, e.g., "2026-12-31")
- `amount_paid` (Double precision numeric fee, e.g., 4500.00)

#### (a) Delimited Text File Layout (RFC 4180 CSV Compliant)
- **Field Order:** `pass_id,reg_no,name,route_no,boarding_stop,validity_date,amount_paid\n`
- **Field Separator:** Comma (`,`)
- **Record Terminator:** Newline (`\n` or `\r\n`)
- **Delimiter Collision Handling:** If any field (such as `name` or `boarding_stop`) contains the delimiter (`,`) or newline, the entire field is enclosed within double quotes (`"`). Any double quote inside the field is escaped as double-double quotes (`""`).  
  *Example Record:*  
  `P-10482,192311042,"Kumar, R. Jr.",14,"Tambaram, Gate 2",2026-12-31,4500.00`

#### (b) Binary File Layout (`DataOutputStream`)
Binary representation uses strictly ordered, typed primitive streams:

| Field | Java Type | Binary Encoding Method | Size on Disk (Bytes) |
|---|---|---|---|
| `pass_id` | `String` | `writeUTF(passId)` | 2 bytes (length) + UTF-8 payload ($\approx 7–10$ bytes) |
| `reg_no` | `String` | `writeUTF(regNo)` | 2 bytes (length) + UTF-8 payload ($\approx 9–11$ bytes) |
| `name` | `String` | `writeUTF(name)` | 2 bytes (length) + UTF-8 payload ($\approx 15–30$ bytes) |
| `route_no` | `int` | `writeInt(routeNo)` | 4 bytes (Big-Endian integer) |
| `boarding_stop` | `String` | `writeUTF(boardingStop)` | 2 bytes (length) + UTF-8 payload ($\approx 15–30$ bytes) |
| `validity_date`| `long` | `writeLong(epochMillis)` | 8 bytes (Epoch timestamp) |
| `amount_paid` | `double` | `writeDouble(amountPaid)`| 8 bytes (IEEE 754 64-bit float) |

*Total Average Binary Record Size:* $\approx 65$ bytes per pass. For 8,000 passes, total binary file size $\approx 520$ KB.

---

### A1.2 – Design the Operations (3 Marks)
For the delimited text file store, the three operations are implemented using **`BufferedReader`** and **`BufferedWriter`**.
- **Stream Justification:** Raw `FileReader`/`FileWriter` make direct kernel system calls for individual characters, inducing massive CPU context-switch overhead. `BufferedReader`/`BufferedWriter` utilize an internal 8,192-byte buffer in memory, reading and writing large disk blocks in single I/O calls.

#### 1. Add a New Pass
```text
ALGORITHM AddPass(filePath, passRecord):
    INPUT: filePath (Path), passRecord (PassObject)
    OUTPUT: Boolean success status

    TRY
        OPEN writer AS BufferedWriter(FileWriter(filePath, APPEND=TRUE))
        csvLine <- FORMAT_CSV(passRecord.passId, passRecord.regNo, 
                              ESCAPE_CSV(passRecord.name), 
                              passRecord.routeNo, 
                              ESCAPE_CSV(passRecord.boardingStop), 
                              passRecord.validityDate, 
                              passRecord.amountPaid)
        writer.WRITE(csvLine)
        writer.NEW_LINE()
        writer.FLUSH()
        RETURN TRUE
    CATCH IOException ex
        LOG_ERROR("Failed to append pass: " + ex.getMessage())
        RETURN FALSE
    FINALLY
        CLOSE writer
    END TRY
END ALGORITHM
```

#### 2. Find Pass by Pass ID
```text
ALGORITHM FindPassById(filePath, targetPassId):
    INPUT: filePath (Path), targetPassId (String)
    OUTPUT: PassRecord or NULL if not found

    IF NOT FILE_EXISTS(filePath) THEN
        RETURN NULL
    END IF

    TRY
        OPEN reader AS BufferedReader(FileReader(filePath))
        WHILE (line <- reader.READ_LINE()) IS NOT NULL DO
            fields <- PARSE_CSV_ROW(line)
            IF fields.LENGTH = 7 AND fields[0] EQUALS targetPassId THEN
                RETURN CONVERT_TO_RECORD(fields)
            END IF
        END WHILE
        RETURN NULL
    CATCH IOException ex
        LOG_ERROR("Read error during search: " + ex.getMessage())
        RETURN NULL
    FINALLY
        CLOSE reader
    END TRY
END ALGORITHM
```

#### 3. List All Passes on a Given Route
```text
ALGORITHM ListPassesOnRoute(filePath, targetRouteNo):
    INPUT: filePath (Path), targetRouteNo (Integer)
    OUTPUT: List of PassRecords matching targetRouteNo

    matchingList <- EMPTY_LIST()
    IF NOT FILE_EXISTS(filePath) THEN
        RETURN matchingList
    END IF

    TRY
        OPEN reader AS BufferedReader(FileReader(filePath))
        WHILE (line <- reader.READ_LINE()) IS NOT NULL DO
            fields <- PARSE_CSV_ROW(line)
            IF fields.LENGTH = 7 THEN
                TRY
                    route <- PARSE_INTEGER(fields[3])
                    IF route EQUALS targetRouteNo THEN
                        matchingList.APPEND(CONVERT_TO_RECORD(fields))
                    END IF
                CATCH NumberFormatException nfe
                    CONTINUE // Skip corrupted route number
                END TRY
            END IF
        END WHILE
    CATCH IOException ex
        LOG_ERROR("Failed to list route passes: " + ex.getMessage())
    FINALLY
        CLOSE reader
    END TRY

    RETURN matchingList
END ALGORITHM
```

---

### A1.3 – Design for Damaged Data (2 Marks)
1. **Damaged / Truncated Line Handling:**
   - When reading, each line is tokenized. If `fields.length != 7`, or if numeric fields (`route_no`, `amount_paid`) fail parsing, the reader catches the specific record exception, logs a warning with line number to an audit log (`corrupt_records.log`), and safely continues parsing subsequent records without crashing.
2. **Missing File Handling:**
   - Checks `Files.exists(path)`. If absent, file creation is initialized atomically with standard CSV headers; searches return empty collections or `null` without throwing unhandled `FileNotFoundException`.
3. **Power Failure / Incomplete Write Prevention (Write-Ahead / Atomic Swap):**
   - For batch modifications or updates: writes are directed to a temporary file (`passes.tmp`). Once all records are flushed and synced via `FileDescriptor.sync()`, an atomic filesystem rename (`Files.move(..., StandardCopyOption.ATOMIC_MOVE)`) replaces the target file. If power cuts mid-write, the original database file remains completely uncorrupted.

---

### Alternative Concept: `RandomAccessFile` with Fixed-Length Records
- **Implementation Mechanism:** Every record is assigned a strictly fixed width (e.g., exactly 128 bytes). To read record $i$, the file pointer seeks directly to byte position $i \times 128$ via `raf.seek(i * 128)`.
- **Complexity Comparison:**
  - *Lookup by Record Index:* $O(1)$ constant time seek versus $O(n)$ linear scan in delimited text file.
  - *Lookup by ID (Unindexed):* Still requires scanning keys $O(n)$ unless a secondary in-memory index or sorted binary search is maintained ($O(\log n)$).
  - *Space Wasted by Padding:* Since names and bus stops vary in length, fixed slots (e.g. 40 bytes for name) waste an average of 22 bytes per record. For 8,000 passes, wasted padding $\approx 8,000 \times 40\text{ bytes} \approx 320\text{ KB}$ ($\approx 35\%$ storage waste).

---

## Task A2 – Don't Read What You Don't Need (5 Marks - K4)

### A2.1 – Cost Prediction for Single-Byte `FileInputStream.read()` (2 Marks)
- **Total Read Invocations:** In a 50 MB file, exactly $50 \times 1024 \times 1024 = 52,428,800$ individual calls to `.read()` are executed.
- **Why It Is Catastrophically Slow:**
  - Each unbuffered `.read()` call executes a native JNI transition and an Operating System system call (`sys_read`).
  - Every syscall forces a user-space to kernel-space context switch, saving CPU registers, flushing processor instruction pipelines, and checking file locks.
  - Making 52 million system calls takes between **45 to 180 seconds** depending on OS overhead, despite the disk hardware being capable of reading 50 MB in a fraction of a second.

### A2.2 – Redesign Using Buffering (2 Marks)
```text
ALGORITHM CountLinesBuffered(filePath):
    INPUT: filePath (Path)
    OUTPUT: Integer lineCount

    buffer <- ALLOCATE_BYTE_ARRAY(8192) // 8 KB standard buffer
    lineCount <- 0
    bytesRead <- 0

    TRY
        OPEN stream AS FileInputStream(filePath)
        WHILE (bytesRead <- stream.READ(buffer)) != -1 DO
            FOR i FROM 0 TO bytesRead - 1 DO
                IF buffer[i] == '\n' THEN
                    lineCount <- lineCount + 1
                END IF
            END FOR
        END WHILE
        RETURN lineCount
    FINALLY
        CLOSE stream
    END TRY
END ALGORITHM
```
- **Change in OS Calls:** With an 8 KB ($8,192$ byte) buffer, the number of OS system calls drops from $52,428,800$ to:
  $$\frac{52,428,800}{8,192} \approx 6,400 \text{ system calls}$$
  This is a **$99.988\%$ reduction in kernel context switches**, reducing elapsed time from minutes to milliseconds.

### A2.3 – When Buffering Does Not Help (1 Mark)
Buffering provides minimal or negative benefit in:
- **Random-Access Seeking:** Applications reading arbitrary single bytes scattered non-sequentially across a multi-gigabyte file (e.g. B-Tree disk nodes). Pre-buffering 8 KB sequentially is wasted work if subsequent reads immediately seek elsewhere.
- **Memory-Mapped I/O (`FileChannel.map`):** Direct memory mapping offloads page fault management directly to OS Virtual Memory Manager.

### Alternative Concept: `Files.readAllLines()` vs Buffered Stream
- **Time Complexity:** Both approaches require $O(\text{file size})$ disk read time.
- **Memory Footprint ($O(N)$ vs $O(1)$ Heap):**
  - `Files.readAllLines()` loads every line into distinct Java `String` objects inside an `ArrayList`. A 50 MB file inflates on the heap into **180–300 MB** due to Java object headers, string deduplication lack, and pointer arrays. For large files (e.g., 2 GB), it triggers `OutOfMemoryError: Java heap space`.
  - A buffered stream uses a fixed **$O(\text{buffer size}) \approx 8$ KB** heap footprint regardless of whether the file is 50 MB, 50 GB, or 5 TB.
- **Guideline:** Use `Files.readAllLines()` only for small config files (< 5 MB); use buffered streams for large logs and data pipelines.

---

## Task A3 – Discover the Data-Access Design Pattern (5 Marks - K4)

### A3.1 – Common Pattern Identification (2 Marks)
In student database implementations (`StudentDB`, `CourseDB`, `MarksDB`):
- **Identical Repeated Steps:**
  1. Driver loading and database URL connection acquisition (`DriverManager.getConnection()`).
  2. SQL statement preparation (`Connection.prepareStatement()`).
  3. Transaction boundary management (`setAutoCommit`, `commit`, `rollback`).
  4. Exception handling and logging (`catch (SQLException e)`).
  5. Guaranteed resource closing in `try-with-resources` (`ResultSet`, `PreparedStatement`, `Connection`).
- **Differing Components:**
  1. SQL string definitions (table name, column names, WHERE clauses).
  2. Parameter binding logic (mapping domain entity properties to `?` indices).
  3. Result mapping logic (extracting columns from `ResultSet` to instantiate concrete domain objects).

### A3.2 – Generic Data-Access Interface Design (2 Marks)
```java
public interface Dao<T, K> {
    void insert(T entity) throws SQLException;
    T findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    void update(T entity) throws SQLException;
    void delete(K id) throws SQLException;
}
```

```text
ALGORITHM StudentDao.findById(studentId):
    INPUT: studentId (String)
    OUTPUT: Student object or NULL

    sql <- "SELECT reg_no, name, email, department FROM students WHERE reg_no = ?"
    TRY (conn <- DBConnection.GET(), pstmt <- conn.PREPARE(sql))
        pstmt.SET_STRING(1, studentId)
        TRY (rs <- pstmt.EXECUTE_QUERY())
            IF rs.NEXT() THEN
                RETURN NEW Student(rs.GET_STRING("reg_no"), 
                                   rs.GET_STRING("name"), 
                                   rs.GET_STRING("email"), 
                                   rs.GET_STRING("department"))
            END IF
        END TRY
    END TRY
    RETURN NULL
END ALGORITHM
```

### A3.3 – Object-to-Table Mapping (1 Mark)
| `Student` Class Field | Database Column | SQL Data Type | Key Constraint |
|---|---|---|---|
| `regNo` (`String`) | `reg_no` | `VARCHAR(20)` | **PRIMARY KEY** |
| `name` (`String`) | `name` | `VARCHAR(100)`| `NOT NULL` |
| `email` (`String`) | `email` | `VARCHAR(120)`| `UNIQUE NOT NULL` |
| `department` (`String`) | `department` | `VARCHAR(50)` | `NOT NULL` |

### Alternative Concept: Inline SQL in GUI/Service vs Generic DAO
If SQL is embedded directly into UI event handlers or service classes:
- Renaming a single database column requires hunting and modifying **dozens of disparate UI source files**, creating extreme regression risk.
- With the DAO pattern, renaming a column requires updating **exactly one mapping method** in `StudentDao`.
- Database query execution time ($O(1)$ indexed query) remains identical, but software maintenance cost drops by over $90\%$.

---

## Task A4 – What Should Be Serialized? (4 Marks - K4)

### A4.1 – Transient Fields & Object Graph Traversal (2 Marks)
In the `Hostel` $\rightarrow$ `Room` $\rightarrow$ `Resident` object graph:
1. **Fields to Mark `transient`:**
   - `Connection dbConnection`: **MUST be marked `transient`**. A database connection represents an active TCP socket and native C driver handle allocated by the OS. It cannot be serialized to disk. Attempting to serialize an un-marked connection throws `NotSerializableException`.
   - `String password`: **SHOULD be marked `transient`**. Serializing sensitive credentials in plaintext into binary stream dumps exposes critical security risks.
2. **Object Graph Traversal:**
   - When `ObjectOutputStream.writeObject(hostel)` is invoked, Java initiates a **recursive depth-first traversal** of all reachable object references.
   - It serializes the `Hostel` object header, visits the `List<Room>`, traverses each `Room`, and serializes every `Resident`. Cyclic references are detected and resolved via internal object handle tables.

### A4.2 – Planning for Change & `serialVersionUID` (2 Marks)
- **Adding `phoneNumber`:** If a new field `private String phoneNumber` is added to `Resident`:
  - **Without `serialVersionUID`:** Java runtime automatically calculates a 64-bit cryptographic SHA-1 hash based on class structure, method signatures, and fields. Adding `phoneNumber` alters this hash. When deserializing an old file, the runtime compares the stored hash with the new class hash, detects a mismatch, and throws:
    `java.io.InvalidClassException: Resident; local class incompatible: stream classdesc serialVersionUID = X, local class serialVersionUID = Y`
  - **With Explicit `serialVersionUID`:**  
    `private static final long serialVersionUID = 1L;`  
    The JVM recognizes the class version as compatible. The new `phoneNumber` field is initialized with its default value (`null`), allowing seamless backward compatibility!

### Alternative Concept: CSV / JSON vs Java Serialization
- **Java Serialization:** Fast for JVM-to-JVM, but opaque, strictly coupled to Java runtimes, fragile across class refactorings, and vulnerable to deserialization security exploits (RCE gadget chains).
- **JSON / Text Format:** Highly human-readable, cross-language interoperable, easy to debug, and resilient when fields are added or removed.

---

## Task A5 – Design for Correctness and Safety (4 Marks - K4)

### A5.1 – Transaction Design (Wallet Transfer) (2 Marks)
```text
ALGORITHM TransferWalletFunds(conn, studentId, amount):
    INPUT: conn (Connection), studentId (String), amount (500.0)
    OUTPUT: Boolean success

    initialAutoCommit <- conn.GET_AUTO_COMMIT()
    TRY
        conn.SET_AUTO_COMMIT(FALSE) // Begin Transaction

        // Step 1: Check balance & deduct from Hostel Wallet
        deductSql <- "UPDATE hostel_wallet SET balance = balance - ? WHERE student_id = ? AND balance >= ?"
        pstmt1 <- conn.PREPARE(deductSql)
        pstmt1.SET_DOUBLE(1, amount)
        pstmt1.SET_STRING(2, studentId)
        pstmt1.SET_DOUBLE(3, amount)
        rowsDeducted <- pstmt1.EXECUTE_UPDATE()

        IF rowsDeducted == 0 THEN
            conn.ROLLBACK() // Insufficient balance or invalid ID
            RETURN FALSE
        END IF

        // Step 2: Credit to Canteen Wallet
        creditSql <- "UPDATE canteen_wallet SET balance = balance + ? WHERE student_id = ?"
        pstmt2 <- conn.PREPARE(creditSql)
        pstmt2.SET_DOUBLE(1, amount)
        pstmt2.SET_STRING(2, studentId)
        pstmt2.EXECUTE_UPDATE()

        conn.COMMIT() // Commit both atomically
        RETURN TRUE
    CATCH SQLException ex
        conn.ROLLBACK() // Failure midway: undo all changes
        LOG_ERROR("Transfer failed, transaction rolled back: " + ex.getMessage())
        RETURN FALSE
    FINALLY
        conn.SET_AUTO_COMMIT(initialAutoCommit) // Restore state
    END TRY
END ALGORITHM
```

### A5.2 – Injection Analysis & Safe Redesign (2 Marks)
- **Vulnerable Code:**
  ```java
  String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
  ```
- **Exploitation:**
  If an adversary inputs:
  - `id`: `admin' --`
  - `pwd`: `arbitrary_password`
  The SQL query interpolates into:
  ```sql
  SELECT * FROM users WHERE id='admin' --' AND pwd='arbitrary_password'
  ```
  The `--` sequence comments out the entire remainder of the SQL clause, bypassing password verification and authenticating the attacker as `admin`.
- **PreparedStatement Redesign:**
  ```java
  String safeSql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
  try (PreparedStatement ps = conn.prepareStatement(safeSql)) {
      ps.setString(1, id);
      ps.setString(2, pwd);
      try (ResultSet rs = ps.executeQuery()) {
          return rs.next(); // True only if credentials match
      }
  }
  ```
  The SQL engine compiles the query parse tree *before* variable evaluation. Input parameters are treated strictly as literal data strings, rendering injection syntactically impossible.
