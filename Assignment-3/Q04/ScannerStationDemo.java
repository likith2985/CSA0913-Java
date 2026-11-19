import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Assignment 3 - Q4: Thread Creation and the Thread Life Cycle
 * Context: Parallel Answer-Script Scanning across 4 Scanner Stations.
 */
public class ScannerStationDemo {

    // Approach 1: Extending java.lang.Thread
    public static class ScannerStationThread extends Thread {
        private final int stationId;
        private final int scriptBatchSize;
        private int scannedCount = 0;

        public ScannerStationThread(int stationId, int scriptBatchSize) {
            super("ScannerStation-Thread-" + stationId);
            this.stationId = stationId;
            this.scriptBatchSize = scriptBatchSize;
        }

        @Override
        public void run() {
            try {
                // Simulate physical scanning with sleep (TIMED_WAITING state)
                for (int i = 0; i < scriptBatchSize; i++) {
                    Thread.sleep(2); // 2ms per script
                    scannedCount++;
                }
                System.out.printf("  [Thread] Station %d finished scanning %d scripts on [%s]%n",
                        stationId, scannedCount, Thread.currentThread().getName());
            } catch (InterruptedException e) {
                System.out.printf("  [Thread] Station %d interrupted!%n", stationId);
                Thread.currentThread().interrupt();
            }
        }

        public int getScannedCount() { return scannedCount; }
    }

    // Approach 2: Implementing java.lang.Runnable (Separation of Task from Thread)
    public static class ScannerStationTask implements Runnable {
        private final int stationId;
        private final int scriptBatchSize;
        private final AtomicInteger globalCounter;

        public ScannerStationTask(int stationId, int scriptBatchSize, AtomicInteger globalCounter) {
            this.stationId = stationId;
            this.scriptBatchSize = scriptBatchSize;
            this.globalCounter = globalCounter;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < scriptBatchSize; i++) {
                    Thread.sleep(2);
                    globalCounter.incrementAndGet();
                }
                System.out.printf("  [Runnable] Station %d completed batch on [%s]%n",
                        stationId, Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println("  Q4: PARALLEL ANSWER-SCRIPT SCANNING - THREAD LIFE CYCLE & EXPERIMENTS");
        System.out.println("==========================================================================\n");

        // 1. Thread Creation & Lifecycle Demonstration
        System.out.println("--- 1. Extending Thread vs Implementing Runnable ---");
        ScannerStationThread s1 = new ScannerStationThread(1, 10);
        ScannerStationThread s2 = new ScannerStationThread(2, 10);

        AtomicInteger sharedTotal = new AtomicInteger(0);
        Thread s3 = new Thread(new ScannerStationTask(3, 10, sharedTotal), "ScannerStation-Runnable-3");
        Thread s4 = new Thread(new ScannerStationTask(4, 10, sharedTotal), "ScannerStation-Runnable-4");

        System.out.println("State before start(): s1 state = " + s1.getState() + " | isAlive = " + s1.isAlive());

        s1.start();
        s2.start();
        s3.start();
        s4.start();

        System.out.println("State immediately after start(): s1 state = " + s1.getState() + " | isAlive = " + s1.isAlive());

        // Wait for all 4 stations to finish using join()
        s1.join();
        s2.join();
        s3.join();
        s4.join();

        System.out.println("State after join(): s1 state = " + s1.getState() + " | isAlive = " + s1.isAlive());
        int grandTotal = s1.getScannedCount() + s2.getScannedCount() + sharedTotal.get();
        System.out.println("Grand Total Scripts Scanned across all stations: " + grandTotal + " (Expected: 40)\n");

        // 2. Calling run() vs start() Proof
        System.out.println("--- 2. Direct run() Call vs start() Proof ---");
        ScannerStationThread directRunThread = new ScannerStationThread(99, 5);
        System.out.println("Invoking directRunThread.run() on calling thread:");
        directRunThread.run(); // SYNCHRONOUS CALL! Does NOT spawn OS thread!
        System.out.println("Notice: Thread name printed was [" + Thread.currentThread().getName() + "], NOT a new worker thread!\n");

        // 3. Multi-Run Non-Deterministic Execution Order (10 runs)
        System.out.println("--- 3. Recording Output Finishing Orders over 5 Concurrent Runs ---");
        for (int run = 1; run <= 5; run++) {
            List<String> finishOrder = new ArrayList<>();
            List<Thread> workers = new ArrayList<>();

            for (int st = 1; st <= 4; st++) {
                final int id = st;
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep((long) (Math.random() * 10));
                        synchronized (finishOrder) {
                            finishOrder.add("Station_" + id);
                        }
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
                workers.add(t);
                t.start();
            }

            for (Thread t : workers) t.join();
            System.out.printf("  Run #%d Completion Order: %s%n", run, finishOrder);
        }

        // 4. Daemon Thread Demonstration
        System.out.println("\n--- 4. Daemon Thread Behavior Demonstration ---");
        Thread daemonScanner = new Thread(() -> {
            try {
                System.out.println("  [Daemon Station] Background scanner started...");
                Thread.sleep(5000); // Long task
                System.out.println("  [Daemon Station] THIS LINE WILL NEVER PRINT IF MAIN EXITS!");
            } catch (InterruptedException e) {
                System.out.println("  [Daemon Station] Interrupted.");
            }
        });
        daemonScanner.setDaemon(true); // Marked as DAEMON
        daemonScanner.start();
        System.out.println("  Main thread reached end of method. Daemon thread will be terminated automatically by JVM.");
    }
}
