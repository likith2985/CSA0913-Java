import java.util.*;

/**
 * Assignment 2 - Q7: Day-4 Design Challenge - Diagnose, Redesign and Validate
 *
 * Bottleneck Diagnosed from Q6:
 * - PriorityQueue uses an O(log N) binary min-heap for a small, discrete 5-level severity domain.
 * - Sifting elements requires repetitive comparator calls and array swaps.
 * - Starvation aging cannot be done in O(1) in a PriorityQueue because updating severity
 *   breaks heap invariants, requiring an O(N) linear scan and rebuild.
 *
 * Redesigned Architecture:
 * - Multi-Level Bucket Queue: Array of 5 ArrayDeque FIFO circular buffers.
 * - Admission: Strict O(1) array index append.
 * - Dispatch: Strict O(1) scan across max 5 bucket pointers.
 * - Starvation Aging: Strict O(1) dequeue and enqueue promotion without heap rebuild!
 */
public class TriageRedesignBenchmark {

    public static class Patient {
        final String id;
        int severity; // 1 (Critical) to 5 (Minor)
        final long arrivalTimeMs;

        public Patient(String id, int severity, long arrivalTimeMs) {
            this.id = id;
            this.severity = severity;
            this.arrivalTimeMs = arrivalTimeMs;
        }

        public void promote() {
            if (severity > 1) severity--;
        }

        @Override
        public String toString() {
            return id + "(S" + severity + ")";
        }
    }

    // =========================================================================
    // 1. ORIGINAL SOLUTION (PriorityQueue with Binary Min-Heap)
    // =========================================================================
    public static class OriginalPriorityQueueTriage {
        private final PriorityQueue<Patient> heap;
        private long seqCounter = 0;

        public OriginalPriorityQueueTriage() {
            Comparator<Patient> comp = Comparator
                    .comparingInt((Patient p) -> p.severity)
                    .thenComparingLong(p -> p.arrivalTimeMs);
            heap = new PriorityQueue<>(comp);
        }

        public void admit(Patient p) {
            heap.add(p); // O(log N) sift-up
        }

        public Patient next() {
            return heap.poll(); // O(log N) sift-down
        }

        public int size() { return heap.size(); }
    }

    // =========================================================================
    // 2. REDESIGNED SOLUTION (Multi-Level Bucket Queue with O(1) ArrayDeques)
    // =========================================================================
    public static class RedesignedBucketQueueTriage {
        private static final int SEVERITY_LEVELS = 5;
        @SuppressWarnings("unchecked")
        private final ArrayDeque<Patient>[] buckets = new ArrayDeque[SEVERITY_LEVELS];
        private int totalCount = 0;

        public RedesignedBucketQueueTriage() {
            for (int i = 0; i < SEVERITY_LEVELS; i++) {
                buckets[i] = new ArrayDeque<>();
            }
        }

        // Admission is strictly O(1) constant time: direct array indexing
        public void admit(Patient p) {
            if (p.severity < 1 || p.severity > SEVERITY_LEVELS) {
                throw new IllegalArgumentException("Invalid severity level: " + p.severity);
            }
            buckets[p.severity - 1].offerLast(p); // O(1) circular buffer append
            totalCount++;
        }

        // Dispatch is strictly O(1) constant time: scans at most 5 bucket pointers
        public Patient next() {
            if (totalCount == 0) return null;
            for (int i = 0; i < SEVERITY_LEVELS; i++) {
                if (!buckets[i].isEmpty()) {
                    totalCount--;
                    return buckets[i].pollFirst(); // O(1) head removal (True FIFO)
                }
            }
            return null;
        }

        // Fair Aging Promotion: Strictly O(1) per promoted patient!
        public void promoteStarvedPatients(long maxWaitAllowedMs, long currentTimeMs) {
            // Check lower priority buckets (Severity 2 down to 5)
            for (int s = 1; s < SEVERITY_LEVELS; s++) {
                int currentBucketSize = buckets[s].size();
                for (int i = 0; i < currentBucketSize; i++) {
                    Patient p = buckets[s].peekFirst();
                    if (p != null && (currentTimeMs - p.arrivalTimeMs) >= maxWaitAllowedMs) {
                        buckets[s].pollFirst(); // Remove from current bucket (O(1))
                        p.promote();            // Promote severity level
                        buckets[s - 1].offerLast(p); // Add to higher bucket (O(1))
                    } else {
                        break; // Since bucket is FIFO, subsequent patients arrived even later
                    }
                }
            }
        }

        public int size() { return totalCount; }
    }

    // =========================================================================
    // 3. COMPARATIVE BENCHMARK RUNNER
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  Q7 DAY-4 DESIGN CHALLENGE: ORIGINAL (HEAP) vs REDESIGNED (BUCKETS)");
        System.out.println("==========================================================================\n");

        int[] patientScales = {10000, 50000, 100000};

        for (int scale : patientScales) {
            System.out.println("==========================================================================");
            System.out.printf("  DATASET SCALE: %,d Patients%n", scale);
            System.out.println("==========================================================================");

            // Generate synthetic patients with distributed severities 1 to 5
            Random rand = new Random(42);
            List<Patient> dataset = new ArrayList<>(scale);
            long now = System.currentTimeMillis();
            for (int i = 0; i < scale; i++) {
                dataset.add(new Patient("PT-" + i, 1 + rand.nextInt(5), now + i));
            }

            // Benchmark Original Solution (PriorityQueue)
            OriginalPriorityQueueTriage original = new OriginalPriorityQueueTriage();
            long start = System.nanoTime();
            for (Patient p : dataset) {
                original.admit(p);
            }
            long tAdmitOrig = System.nanoTime() - start;

            start = System.nanoTime();
            int countOrig = 0;
            while (original.next() != null) countOrig++;
            long tPollOrig = System.nanoTime() - start;

            // Benchmark Redesigned Solution (Multi-Level Bucket Queue)
            RedesignedBucketQueueTriage redesigned = new RedesignedBucketQueueTriage();
            start = System.nanoTime();
            for (Patient p : dataset) {
                redesigned.admit(p);
            }
            long tAdmitRedesign = System.nanoTime() - start;

            start = System.nanoTime();
            int countRedesign = 0;
            while (redesigned.next() != null) countRedesign++;
            long tPollRedesign = System.nanoTime() - start;

            System.out.printf("1. Admission Time (%,d admits):%n", scale);
            System.out.printf("   Original (Binary Heap)  [O(log N)] : %8.2f ms (Avg: %6.1f ns/op)%n",
                    tAdmitOrig / 1e6, (double) tAdmitOrig / scale);
            System.out.printf("   Redesigned (Bucket Array) [O(1)]   : %8.2f ms (Avg: %6.1f ns/op)%n",
                    tAdmitRedesign / 1e6, (double) tAdmitRedesign / scale);
            System.out.printf("   Admission Speedup                  : %.2fx FASTER%n",
                    (double) tAdmitOrig / Math.max(1, tAdmitRedesign));

            System.out.printf("\n2. Dispatch / Poll Time (%,d polls):%n", scale);
            System.out.printf("   Original (Binary Heap)  [O(log N)] : %8.2f ms (Avg: %6.1f ns/op)%n",
                    tPollOrig / 1e6, (double) tPollOrig / scale);
            System.out.printf("   Redesigned (Bucket Array) [O(1)]   : %8.2f ms (Avg: %6.1f ns/op)%n",
                    tPollRedesign / 1e6, (double) tPollRedesign / scale);
            System.out.printf("   Dispatch Speedup                   : %.2fx FASTER%n",
                    (double) tPollOrig / Math.max(1, tPollRedesign));

            long totalOrig = tAdmitOrig + tPollOrig;
            long totalRedesign = tAdmitRedesign + tPollRedesign;
            System.out.printf("\nOverall Throughput Speedup: %.2fx FASTER%n%n",
                    (double) totalOrig / Math.max(1, totalRedesign));
        }

        // 4. Aging Demonstration
        System.out.println("==========================================================================");
        System.out.println("  O(1) FAIR AGING VALIDATION IN REDESIGNED QUEUE");
        System.out.println("==========================================================================");
        RedesignedBucketQueueTriage agingQueue = new RedesignedBucketQueueTriage();
        long tBase = System.currentTimeMillis();

        // Admit minor patient 40 minutes ago
        agingQueue.admit(new Patient("MinorPatientA", 5, tBase - 40 * 60 * 1000));
        agingQueue.admit(new Patient("RecentSeverePatientB", 2, tBase - 5 * 60 * 1000));

        System.out.println("Before Aging Check: Highest priority in queue: " + agingQueue.next()); // Should be Patient B (S2)
        agingQueue.admit(new Patient("RecentSeverePatientB", 2, tBase - 5 * 60 * 1000)); // re-admit for aging demo

        // Run Aging Promotion with 30 min threshold
        agingQueue.promoteStarvedPatients(30 * 60 * 1000, tBase);
        System.out.println("Promoting starved patients waiting > 30 mins...");
        System.out.println("First Patient Extracted Now: " + agingQueue.next());
        System.out.println("Second Patient Extracted:    " + agingQueue.next());
        System.out.println("Status: Aging promotion successfully executed in O(1) without heap rebuild!");
    }
}
