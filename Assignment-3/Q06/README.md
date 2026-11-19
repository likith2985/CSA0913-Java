# Q6 – Deadlock and Inter-Thread Communication

## 1. Problem Context
* **Part A (Hostel Wallet Transfers):** Two concurrent transactions attempt to lock accounts in reverse order (Account 1 $\rightarrow$ 2 vs Account 2 $\rightarrow$ 1), risking deadly embrace deadlock.
* **Part B (Mess Token Counter):** A bounded buffer with capacity of at most 5 tokens coordinated between a producer (Cook) and multiple consumers (Students).

---

## 2. Student A – Producer–Consumer Architecture & `wait()` Mechanics

### 2.1 Why `wait()` Must Be Called in a `while` Loop (Never `if`)
```java
// CORRECT
while (count == buffer.length) {
    wait();
}
```
* **Reason 1: Guard Against Spurious Wakeups:** The POSIX thread specification and JVM allow threads to wake up from `wait()` without any explicit `notify()` or interrupt call.
* **Reason 2: Re-checking Predicates in Multi-Consumer Scenarios:** If Thread A wakes up, but before it can acquire the monitor, Thread B slips in and consumes the available token, the condition `count == 0` is true again. If an `if` block was used, Thread A would wake up and blind-execute `buffer[out]`, causing **Array Index Underflow / Buffer Corruption**!

### 2.2 Why `wait()` Must Be Inside a `synchronized` Block
* `wait()` requires holding the object monitor lock. Calling it without synchronization throws `IllegalMonitorStateException`.
* Inside `synchronized`, calling `wait()` **atomically releases the monitor** and places the thread on the object's wait set, allowing the other thread (e.g. producer) to acquire the lock and call `put()`.

### 2.3 Why `notifyAll()` is Required Over `notify()`
* `notify()` wakes up an arbitrary single thread from the wait set.
* In a system with 1 producer and 2 consumers:
  1. A consumer empties the buffer and calls `notify()`.
  2. Instead of waking the producer, the JVM scheduler wakes the *second consumer*.
  3. The second consumer finds the buffer empty and immediately goes back to `wait()`.
  4. The producer is never signaled and sleeps forever $\rightarrow$ **Permanent System Hang (Lost Wakeup Signal)**.
* `notifyAll()` wakes all waiting threads, guaranteeing the producer is notified when buffer space frees up.

### 2.4 Difference Between `Thread.sleep()` and `Object.wait()`

| Feature | `Thread.sleep(millis)` | `Object.wait()` |
| :--- | :--- | :--- |
| **Lock Handling** | **Holds all locks/monitors**; does NOT release. | **Releases the object monitor lock** while waiting. |
| **Class Origin** | Static method on `java.lang.Thread`. | Instance method on root `java.lang.Object`. |
| **Wakeup Mechanism**| Resumes automatically after time expires or interrupt. | Requires `notify()` / `notifyAll()` from another thread. |
| **Context** | Any method or block. | Strictly inside a `synchronized` context. |

---

## 3. Student B – Deadlock Analysis & The Lock-Ordering Rule

### 3.1 The Four Coffman Conditions for Deadlock Present in Part A
1. **Mutual Exclusion:** Each wallet account lock is unshareable and held exclusively by one thread.
2. **Hold and Wait:** Thread 1 holds Account A while blocking, waiting for Account B.
3. **No Preemption:** An acquired lock cannot be forcibly confiscated from a thread.
4. **Circular Wait:** Thread 1 holds A and waits for B, while Thread 2 holds B and waits for A ($A \rightarrow B \rightarrow A$).

### 3.2 The Lock-Ordering Rule (Breaking Circular Wait)
To mathematically eliminate the Circular Wait condition:
* Always acquire locks according to a **globally consistent total order** (e.g. sorting by immutable `Account.id`):
  ```java
  Account first = from.id < to.id ? from : to;
  Account second = from.id < to.id ? to : from;

  synchronized (first) {
      synchronized (second) {
          // Transfer is 100% deadlock-free!
      }
  }
  ```
* Because all threads acquire lower-numbered account locks before higher-numbered ones, a circular wait graph can never form.

---

## 4. Complete the Code Verification
File: `TokenCounter.java`

* **TODO 1 & 2 Completed:**
  ```java
  // put():
  while (count == buffer.length) wait();
  buffer[in] = token; in = (in + 1) % buffer.length; count++;
  notifyAll();

  // take():
  while (count == 0) wait();
  int token = buffer[out]; out = (out + 1) % buffer.length; count--;
  notifyAll();
  return token;
  ```
* **Output:**
  ```
  Sum of tokens collected: 210 (expected 210)
  ```
* **Changing `while` to `if`:**
  Under high thread contention, changing `while` to `if` introduces race conditions where threads resume without verifying buffer capacity, corrupting `count` outside $[0, 5]$ and throwing index errors.

---

## 5. Alternative Concept: `ArrayBlockingQueue` & `tryLock()`

* **`ArrayBlockingQueue<Integer>(5)`:** High-level concurrency utility encapsulating `ReentrantLock` with `notFull` and `notEmpty` condition variables; eliminates raw wait/notify bugs.
* **`tryLock(timeout)`:** Allows a thread attempting a transfer to give up and release held locks if the second lock cannot be acquired within a timeout, breaking the Hold-and-Wait condition.
