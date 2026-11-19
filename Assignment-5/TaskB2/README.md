# Task B2: Engineer an Adversarial Input & Exploitation Defence

## 1. Adversarial SQL Injections & PreparedStatement Defence

### Adversarial Payload 1: SQL Comment Injection
- **Crafted Input:**
  - `id`: `admin' --`
  - `pwd`: `invalid_attacker_password`
- **Unsafe Evaluation:**
  ```sql
  SELECT * FROM users WHERE id='admin' --' AND pwd='invalid_attacker_password'
  ```
  The SQL comment marker `--` terminates lexical parsing of the remainder of the query. The password predicate `AND pwd=...` is ignored by the database optimizer. The query matches the `admin` row directly, granting administrator access without credential verification.
- **PreparedStatement Behaviour:**
  ```java
  SELECT * FROM users WHERE id = ? AND pwd = ?
  ```
  The query plan is precompiled. The string `"admin' --"` is bound as an exact literal value for parameter 1. The database queries for a user whose literal username is `admin' --`, which matches nothing, safely returning `false`.

### Adversarial Payload 2: Boolean Tautology Injection
- **Crafted Input:**
  - `id`: `student01`
  - `pwd`: `' OR '1'='1`
- **Unsafe Evaluation:**
  ```sql
  SELECT * FROM users WHERE id='student01' AND pwd='' OR '1'='1'
  ```
  Operator precedence evaluates `('1'='1')` to `TRUE`, overriding the password validation condition and authenticating the caller.
- **PreparedStatement Behaviour:**
  Parameter 2 is treated as literal text `"' OR '1'='1"`. The database evaluates `pwd = '\' OR \'1\'=\'1'`, which fails cleanly against the real password.

---

## 2. Adversarial File Store Testing
The test suite constructed an adversarial CSV file containing five distinct classes of corruption:
1. **Truncated Line (`P002,WRONG_FIELD_COUNT`):** Only 2 tokens instead of 5.
2. **Blank Lines (`\n\n`):** Consecutive empty lines.
3. **Whitespace Lines (`   \n`):** Spaces only.
4. **Non-Numeric Corruptions (`NaN_AMOUNT_CORRUPT`):** Non-parsable numeric fields.
5. **Unquoted Comma Injections (`P004,Punctuation,In,Name,22,5000.0`):** Name containing delimiters without quotes.

### Observed File Store Behaviour
- The `PassStore.find()` implementation verifies `fields.length == 5` before accessing indices.
- Corrupted lines evaluate to `false` and are skipped without throwing `ArrayIndexOutOfBoundsException`.
- The search successfully traversed past all malformed lines to match valid record `P005` at the end of the file.

---

## 3. What Each Adversarial Input Reveals About Original Design
1. **Conflation of Code and Data:** The original SQL string concatenation treated untrusted data as executable instructions, allowing attackers to manipulate the database execution graph.
2. **Lack of Defensive Field Schemas:** Naive file readers that assume every line has exactly $k$ comma-separated tokens crash immediately with unhandled runtime exceptions upon encountering corrupted records or empty lines.

---

## 4. Alternative Concept Exploration: Quote Escaping / Regex Validation vs `PreparedStatement`

| Criterion | Manual Quote Escaping / Regex Filter | `PreparedStatement` (Parameterized Query) |
|---|---|---|
| **Security Resilience** | **Fragile & Weak.** Can be bypassed by multi-byte UTF-8 encodings (e.g. GBK `0xbf27`), alternate quote marks, Unicode homoglyphs, or second-order injections. | **Mathematically Complete.** Code and data are separated at the database protocol level. Injection is impossible. |
| **Execution Performance** | Slower per query: CPU must evaluate complex regex state machines against every incoming parameter string ($O(m)$ regex scan). | **Faster:** Query parse tree is compiled once and cached in database plan cache. Subsequent executions incur zero re-parsing overhead ($O(1)$). |
| **Code Maintainability** | Proliferates complex regexes across codebases; error-prone whenever new character sets or input formats are permitted. | Standardized JDBC API across all compliant relational database engines (PostgreSQL, MySQL, SQLite, Oracle). |
