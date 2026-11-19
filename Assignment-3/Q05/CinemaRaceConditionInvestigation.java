import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Assignment 3 - Q5: Race Conditions in a Cinema Seat-Booking System
 * Compares:
 * 1. Unsynchronized check-then-act (Reliably reproduces double booking)
 * 2. Synchronized method
 * 3. Synchronized block (Fine-grained per-seat lock)
 * 4. Lock-Free AtomicBoolean[] compareAndSet()
 */
public class CinemaRaceConditionInvestigation {

    // --- 1. Unsynchronized Seat Booking (Buggy Check-Then-Act) ---
    public static class UnsynchronizedBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        public UnsynchronizedBooking(int seats) {
            booked = new boolean[seats];
        }

        public boolean book(int seat) {
            // CRITICAL SECTION (UNPROTECTED)
            if (!booked[seat]) {
                // Simulated context switch delay
                try { Thread.sleep(0, 100); } catch (InterruptedException ignored) {}
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // --- 2. Synchronized Method (Coarse-Grained Monitor on 'this') ---
    public static class SynchronizedMethodBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        public SynchronizedMethodBooking(int seats) {
            booked = new boolean[seats];
        }

        public synchronized boolean book(int seat) {
            if (!booked[seat]) {
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // --- 3. Synchronized Block (Fine-Grained Per-Seat Object Locks) ---
    public static class SynchronizedBlockBooking {
        private final boolean[] booked;
        private final Object[] seatLocks;
        private int bookingsMade = 0;

        public SynchronizedBlockBooking(int seats) {
            booked = new boolean[seats];
            seatLocks = new Object[seats];
            for (int i = 0; i < seats; i++) seatLocks[i] = new Object();
        }

        public boolean book(int seat) {
            // Fine-grained lock: Only locks the specific seat, allowing other seats to be booked concurrently!
            synchronized (seatLocks[seat]) {
                if (!booked[seat]) {
                    booked[seat] = true;
                    synchronized (this) { bookingsMade++; }
                    return true;
                }
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // --- 4. Lock-Free AtomicBoolean[] with compareAndSet() ---
    public static class AtomicLockFreeBooking {
        private final AtomicBoolean[] booked;
        private final AtomicInteger bookingsMade = new AtomicInteger(0);

        public AtomicLockFreeBooking(int seats) {
            booked = new AtomicBoolean[seats];
            for (int i = 0; i < seats; i++) booked[i] = new AtomicBoolean(false);
        }

        public boolean book(int seat) {
            // Atomic CAS: Changes false -> true in a single hardware CPU instruction
            if (booked[seat].compareAndSet(false, true)) {
                bookingsMade.incrementAndGet();
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println("  Q5: CINEMA SEAT BOOKING - RACE CONDITIONS & SYNCHRONIZATION STRATEGIES");
        System.out.println("==========================================================================\n");

        int totalSeats = 10;
        int competingThreads = 100;

        // 1. Reproducing Double Booking over 10 Runs (Unsynchronized)
        System.out.println("--- 1. Unsynchronized Execution (10 Runs) - Tracking Double Bookings ---");
        int overbookedRuns = 0;
        for (int run = 1; run <= 10; run++) {
            UnsynchronizedBooking badScreen = new UnsynchronizedBooking(totalSeats);
            Thread[] users = new Thread[competingThreads];
            for (int i = 0; i < competingThreads; i++) {
                final int s = i % totalSeats;
                users[i] = new Thread(() -> badScreen.book(s));
                users[i].start();
            }
            for (Thread t : users) t.join();
            int sold = badScreen.getBookingsMade();
            if (sold > totalSeats) overbookedRuns++;
            System.out.printf("  Run #%02d: Seats sold: %2d / %d %s%n",
                    run, sold, totalSeats, (sold > totalSeats ? ">> [DOUBLE BOOKING DISASTER!]" : ""));
        }
        System.out.printf("Overbooked Runs: %d / 10%n%n", overbookedRuns);

        // 2. Synchronized Method Verification
        System.out.println("--- 2. Synchronized Method Verification ---");
        SynchronizedMethodBooking syncMethod = new SynchronizedMethodBooking(totalSeats);
        Thread[] usersSync = new Thread[competingThreads];
        for (int i = 0; i < competingThreads; i++) {
            final int s = i % totalSeats;
            usersSync[i] = new Thread(() -> syncMethod.book(s));
            usersSync[i].start();
        }
        for (Thread t : usersSync) t.join();
        System.out.printf("Seats sold: %d / %d (Guaranteed Zero Double-Booking)%n%n",
                syncMethod.getBookingsMade(), totalSeats);

        // 3. Performance Benchmark across 10,000 Bookings
        System.out.println("--- 3. Throughput Benchmark: 10,000 Contended Bookings ---");
        int benchmarkOps = 10000;
        int benchSeats = 100;

        // Synchronized Method
        SynchronizedMethodBooking bmSync = new SynchronizedMethodBooking(benchSeats);
        long start = System.nanoTime();
        Thread[] tSync = new Thread[benchmarkOps];
        for (int i = 0; i < benchmarkOps; i++) {
            final int s = i % benchSeats;
            tSync[i] = new Thread(() -> bmSync.book(s));
            tSync[i].start();
        }
        for (Thread t : tSync) t.join();
        long tSyncDuration = System.nanoTime() - start;

        // Synchronized Block
        SynchronizedBlockBooking bmBlock = new SynchronizedBlockBooking(benchSeats);
        start = System.nanoTime();
        Thread[] tBlock = new Thread[benchmarkOps];
        for (int i = 0; i < benchmarkOps; i++) {
            final int s = i % benchSeats;
            tBlock[i] = new Thread(() -> bmBlock.book(s));
            tBlock[i].start();
        }
        for (Thread t : tBlock) t.join();
        long tBlockDuration = System.nanoTime() - start;

        // AtomicBoolean CAS
        AtomicLockFreeBooking bmAtomic = new AtomicLockFreeBooking(benchSeats);
        start = System.nanoTime();
        Thread[] tAtomic = new Thread[benchmarkOps];
        for (int i = 0; i < benchmarkOps; i++) {
            final int s = i % benchSeats;
            tAtomic[i] = new Thread(() -> bmAtomic.book(s));
            tAtomic[i].start();
        }
        for (Thread t : tAtomic) t.join();
        long tAtomicDuration = System.nanoTime() - start;

        System.out.printf("1. Synchronized Method (Whole Method Lock): %8.2f ms (Seats: %d)%n",
                tSyncDuration / 1e6, bmSync.getBookingsMade());
        System.out.printf("2. Synchronized Block  (Fine-Grained Lock): %8.2f ms (Seats: %d)%n",
                tBlockDuration / 1e6, bmBlock.getBookingsMade());
        System.out.printf("3. Lock-Free AtomicBoolean.compareAndSet(): %8.2f ms (Seats: %d)%n",
                tAtomicDuration / 1e6, bmAtomic.getBookingsMade());
    }
}
