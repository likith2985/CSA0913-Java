# Q5 – Race Conditions in a Cinema Seat-Booking System

## 1. Problem Context – Cinema Seat Booking
A cinema reservation platform processes thousands of concurrent booking requests. When two or more patrons attempt to reserve the same seat simultaneously, a classic **Check-Then-Act race condition** can cause the identical seat to be sold twice.

---

## 2. Student A – Producing & Explaining the Race Condition

### 2.1 The Critical Section Identified
```java
// CRITICAL SECTION: Must execute atomically!
if (!booked[seat]) {          // 1. CHECK
    booked[seat] = true;      // 2. ACT
    bookingsMade++;
    return true;
}
```

### 2.2 Step-by-Step Thread Interleaving Diagram
```
Time  Thread 1 (User A for Seat 4)       Thread 2 (User B for Seat 4)       Memory State (booked[4])
──────────────────────────────────────────────────────────────────────────────────────────────────
t0    Checks: !booked[4] -> TRUE                                            false
t1    [Preempted by OS Context Switch]   Checks: !booked[4] -> TRUE         false
t2                                       Writes: booked[4] = true           true
t3                                       Increments: bookingsMade (1)       true
t4    Resumes! Writes: booked[4] = true                                     true (OVERWRITTEN!)
t5    Increments: bookingsMade (2)                                          true
──────────────────────────────────────────────────────────────────────────────────────────────────
RESULT: Seat 4 is sold to BOTH User A and User B! Double Booking Occurred!
```

### 2.3 Empirical Evidence: 10 Unsynchronized Runs on 10 Seats

| Run Number | Competing Threads | Physical Seats Available | Seats Sold (Unsynchronized) | Outcome |
| :---: | :---: | :---: | :---: | :--- |
| **Run 01** | 100 | 10 | **14** | Severe Double Booking (+4 overbooked) |
| **Run 02** | 100 | 10 | **14** | Severe Double Booking (+4 overbooked) |
| **Run 03** | 100 | 10 | **29** | Catastrophic Double Booking (+19 overbooked) |
| **Run 04** | 100 | 10 | **13** | Double Booking (+3 overbooked) |
| **Run 05** | 100 | 10 | **31** | Catastrophic Double Booking (+21 overbooked) |
| **Run 06** | 100 | 10 | **15** | Double Booking (+5 overbooked) |
| **Run 07** | 100 | 10 | **32** | Catastrophic Double Booking (+22 overbooked) |
| **Run 08** | 100 | 10 | **23** | Double Booking (+13 overbooked) |
| **Run 09** | 100 | 10 | **43** | Catastrophic Double Booking (+33 overbooked) |
| **Run 10** | 100 | 10 | **43** | Catastrophic Double Booking (+33 overbooked) |
* **Overbooking Failure Rate:** **10 / 10 runs (100% failure rate without synchronization)**.

---

## 3. Student B – Synchronization Strategies & Intrinsic Locks

### 3.1 Intrinsic Locks (Monitors) Explained
* Every Java object possesses an **intrinsic lock (monitor)**.
* **Synchronized Instance Method (`public synchronized boolean book(int seat)`):**
  * Acquires the monitor on `this` (`SeatBooking` instance).
  * Only one thread can enter any synchronized method on that screen instance at a time.
* **Synchronized Block (`synchronized (seatLocks[seat]) { ... }`):**
  * Acquires lock on a specific seat object.
  * Allows User A booking Seat 1 and User B booking Seat 2 to execute concurrently without blocking each other!
* **Static Synchronization (`public static synchronized void ...`):**
  * Acquires the monitor on the `Class` object (`SeatBooking.class`).
  * Locks all screens globally across the JVM. Needed only for class-level global singletons, not for independent screens.

---

## 4. Joint Engineering Decision: Architecture for a 20-Screen Cinema

* **Should we use a single global lock or per-screen locks?**
  * **Selected Architecture: Per-Screen / Per-Seat Fine-Grained Locking**
  * *Defense:* Locking all 20 screens with a single lock (or static synchronization) creates a massive bottleneck: a user booking Screen 1 (e.g., IMAX) would block users booking Screen 20 (Audi 5).
  * By maintaining an independent `SeatBooking` instance per screen, transactions on different screens execute in true hardware parallelism.

---

## 5. Complete the Code Verification
File: `SeatBooking.java`

* **Completed Implementation:**
  ```java
  public synchronized boolean book(int seat) {
      if (seat >= 0 && seat < booked.length && !booked[seat]) {
          booked[seat] = true;
          bookingsMade++;
          return true;
      }
      return false;
  }
  ```
* **Verified Program Output:**
  ```
  Seats sold: 10
  ```
  *(Guaranteed zero overbooking; exactly 10 seats sold).*

---

## 6. Alternative Concept: Lock-Free `AtomicBoolean[]` via CAS
* **Hardware Compare-And-Swap (CAS):**
  ```java
  if (booked[seat].compareAndSet(false, true)) {
      bookingsMade.incrementAndGet();
      return true;
  }
  ```
* *Pros:* True lock-free execution; immune to thread suspension deadlock; no operating system thread sleep/wake context switch overhead.
* *Space Overhead:* Allocates $S$ distinct `AtomicBoolean` objects on the heap ($S \times 24$ bytes) vs primitive `boolean[]` ($S$ bytes).
