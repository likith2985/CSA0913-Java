# Q5 – Derive a Generic Ranking Component from a Principle

## 1. The Core Ranking Principle
> *"A ranking is correct only if the comparison used is consistent: if A ranks above B and B ranks above C, then A must rank above C, and equal items must compare as equal."*

Mathematically, this demands that the comparison relation forms a **strict weak ordering** satisfying:
1. **Irreflexivity:** $A$ cannot rank above $A$ (`compare(a, a) == 0`).
2. **Antisymmetry:** If $A$ ranks above $B$, then $B$ cannot rank above $A$.
3. **Transitivity:** If $A \succ B$ and $B \succ C$, then $A \succ C$.
4. **Equivalence Transitivity:** If $A \sim B$ and $B \sim C$, then $A \sim C$.

---

## 2. Design Rationale: `Comparable<Team>` vs Separate `Comparator`

### Why Separate `Comparator` Objects are Better:
1. **Single Responsibility & Open-Closed Principle:** A domain entity like `Team` or `Player` represents data state, not sorting policy. Different tournaments or contexts rank teams differently (e.g. by fair-play cards, head-to-head records, or goal ratio). Implementing `Comparable` locks `Team` into one rigid natural ordering.
2. **Reusability across Unrelated Types:** By designing `Leaderboard<T>` to accept an injected `Comparator<? super T>`, the identical class can rank `Team`, `Player`, `QuizTeam`, and `College` without forcing them to share an inheritance hierarchy.
3. **Use of Generics and Wildcards:**
   * `Comparator<? super T>` follows the **PECS rule (Producer Extends, Consumer Super)**.
   * `Leaderboard<T>` *consumes* items of type `T` to compare them. Declaring `? super T` allows passing a comparator defined for a base type (e.g., `Comparator<Participant>` can rank a `Leaderboard<Player>`).
   * `items.sort(order)` sorts a cloned copy (`new ArrayList<>(items)`), preserving the immutability of the internal collection.

---

## 3. Multi-Field Tie-Breaking Expression
```java
Comparator<Team> byRank = Comparator.comparingInt((Team t) -> t.points).reversed()
        .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
        .thenComparing(t -> t.name);
```

### Why Bisons is Ranked Above Panthers:
* **Points:** Both Bisons (9) and Panthers (9) are tied on points.
* **Goal Difference:** Both Bisons (+4) and Panthers (+4) are tied on goal difference.
* **Tertiary Tie-Breaker (Name A–Z):** `"Bisons".compareTo("Panthers")` returns a negative integer because `'B' < 'P'`.
* Therefore, the comparator strictly orders `Bisons` above `Panthers`.

---

## 4. Faulty Comparator Analysis: The Subtraction Overflow Trap

A common naive implementation subtracts integers:
```java
// BUGGY: (a, b) -> (b.points - a.points)
```
* If Score $A = 2,147,483,637$ and Score $B = -50$ (negative penalty):
  $$\text{scoreB} - \text{scoreA} = -50 - 2,147,483,637 = -2,147,483,687 \longrightarrow \mathbf{+2,147,483,609} \text{ (32-bit integer overflow)}$$
* The positive result causes the JVM to rank $-50$ **above** $+2.14\times 10^9$, completely destroying mathematical transitivity and causing sorting algorithms (like TimSort) to throw `IllegalArgumentException: Comparison method violates its general contract!`.
* **Resolution:** Always use `Integer.compare(b, a)` or `Comparator.comparingInt()`.

---

## 5. Complete the Code Verification Output
Executing `LeaderboardDemo.java`:
```
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
```
*(100% matches assignment specification).*
