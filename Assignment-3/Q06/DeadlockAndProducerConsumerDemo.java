import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Assignment 3 - Q6: Deadlock and Inter-Thread Communication
 * Demonstrates:
 * Part A: Wallet Transfer Deadlock & Coffman Conditions Resolution via Lock-Ordering
 * Part B: Mess Token Counter (Producer-Consumer) with wait(), notifyAll(), and Bounds Check
 */
public class DeadlockAndProducerConsumerDemo {

    // =========================================================================
    // PART A: WALLET TRANSFERS (DEADLOCK & PREVENTION)
    // =========================================================================

    public static class Account {
        final int id;
        double balance;
        final ReentrantLock lock = new ReentrantLock();

        public Account(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    // Buggy transfer that deadlocks if thread 1 does A->B while thread 2 does B->A
    public static void buggyTransfer(Account from, Account to, double amount) {
        synchronized (from) {
            System.out.printf("  [%s] Acquired lock on Account %d; trying to acquire Account %d...%n",
                    Thread.currentThread().getName(), from.id, to.id);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}

            synchronized (to) {
                from.balance -= amount;
                to.balance += amount;
                System.out.printf("  [%s] Transfer completed from %d to %d%n",
                        Thread.currentThread().getName(), from.id, to.id);
            }
        }
    }

    // Deadlock-free transfer using global lock-ordering rule
    public static void safeTransfer(Account from, Account to, double amount) {
        // Enforce consistent acquisition order by Account ID!
        Account firstLock = from.id < to.id ? from : to;
        Account secondLock = from.id < to.id ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                from.balance -= amount;
                to.balance += amount;
                System.out.printf("  [%s] SAFE Transfer completed: %d -> %d (Rs %.2f)%n",
                        Thread.currentThread().getName(), from.id, to.id, amount);
            }
        }
    }

    // =========================================================================
    // PART B: MESS TOKEN COUNTER (PRODUCER-CONSUMER)
    // =========================================================================

    public static class BoundedTokenCounter {
        private final int[] buffer = new int[5];
        private int count = 0, in = 0, out = 0;
        private int maxRecorded = 0;
        private int minRecorded = 0;

        public synchronized void put(int token) throws InterruptedException {
            // MUST BE WHILE LOOP: Guard against spurious wakeups and multiple producers
            while (count == buffer.length) {
                wait();
            }
            buffer[in] = token;
            in = (in + 1) % buffer.length;
            count++;
            if (count > maxRecorded) maxRecorded = count;
            notifyAll(); // Wake up consumers
        }

        public synchronized int take() throws InterruptedException {
            while (count == 0) {
                wait();
            }
            int token = buffer[out];
            out = (out + 1) % buffer.length;
            count--;
            if (count < minRecorded) minRecorded = count;
            notifyAll(); // Wake up producer
            return token;
        }

        public int getMaxRecorded() { return maxRecorded; }
        public int getMinRecorded() { return minRecorded; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println("  Q6: DEADLOCK PREVENTION & PRODUCER-CONSUMER INTER-THREAD COMMUNICATION");
        System.out.println("==========================================================================\n");

        // 1. Part A: Safe Lock-Ordering Demonstration
        System.out.println("--- 1. Part A: Deadlock-Free Wallet Transfer via Lock Ordering ---");
        Account accA = new Account(101, 1000.0);
        Account accB = new Account(102, 1000.0);

        Thread t1 = new Thread(() -> safeTransfer(accA, accB, 200.0), "TransferThread-1");
        Thread t2 = new Thread(() -> safeTransfer(accB, accA, 150.0), "TransferThread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("Safe transfer finished successfully without circular wait deadlock!\n");

        // 2. Part B: Producer-Consumer Bounds & Inter-Thread Coordination
        System.out.println("--- 2. Part B: Mess Token Counter Coordination Verification ---");
        BoundedTokenCounter counter = new BoundedTokenCounter();
        AtomicInteger totalTokensReceived = new AtomicInteger(0);

        // Producer: Cook produces 30 tokens
        Thread cook = new Thread(() -> {
            try {
                for (int i = 1; i <= 30; i++) {
                    counter.put(i);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "CookThread");

        // Consumers: 3 Students take 10 tokens each
        Thread[] students = new Thread[3];
        for (int i = 0; i < 3; i++) {
            students[i] = new Thread(() -> {
                try {
                    for (int k = 0; k < 10; k++) {
                        int token = counter.take();
                        totalTokensReceived.addAndGet(token);
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "StudentThread-" + i);
        }

        cook.start();
        for (Thread s : students) s.start();

        cook.join();
        for (Thread s : students) s.join();

        int expectedSum = (30 * 31) / 2; // 465
        System.out.println("Total tokens consumed sum: " + totalTokensReceived.get() + " (Expected: " + expectedSum + ")");
        System.out.printf("Counter Bounds Audit: Min Observed = %d | Max Observed = %d (Capacity = 5)%n",
                counter.getMinRecorded(), counter.getMaxRecorded());
        System.out.println("Bounds Invariant (0 <= count <= 5) Upheld: " +
                (counter.getMinRecorded() >= 0 && counter.getMaxRecorded() <= 5));
    }
}
