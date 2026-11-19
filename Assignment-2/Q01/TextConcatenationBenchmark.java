import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Assignment 2 - Q1: Building Text Under Load: String, StringBuilder or StringBuffer?
 * Investigates text concatenation performance across three contrasting workloads
 * for Railway e-Ticket generation.
 */
public class TextConcatenationBenchmark {

    // Helper to generate sample fields for a ticket
    private static List<String> generateTicketFields(int fieldCount) {
        List<String> fields = new ArrayList<>(fieldCount);
        fields.add("PNR: 842-9918231");
        fields.add("TRAIN: 12626/Kerala Express");
        fields.add("FROM: NDLS (New Delhi)");
        fields.add("TO: TVC (Thiruvananthapuram)");
        fields.add("CLASS: 3A (Third AC)");
        fields.add("DATE: 2026-11-20");
        fields.add("QUOTA: General");
        fields.add("TOTAL_FARE: Rs 2150.00");
        for (int i = 8; i < fieldCount; i++) {
            fields.add(String.format("PASSENGER_%d: Name%d | Coach:B%d | Berth:%d | Status:CNF",
                    i - 7, i - 7, (i % 6) + 1, (i % 64) + 1));
        }
        return fields;
    }

    // Approach 1: String concatenation with '+' operator
    public static String buildWithStringConcat(List<String> fields) {
        String ticket = "";
        for (String field : fields) {
            ticket = ticket + field + "\n";
        }
        return ticket;
    }

    // Approach 2: StringBuilder (unsynchronized, mutable buffer)
    public static String buildWithStringBuilder(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(field).append("\n");
        }
        return sb.toString();
    }

    // Approach 3: StringBuffer (synchronized, thread-safe mutable buffer)
    public static String buildWithStringbuffer(List<String> fields) {
        StringBuffer sb = new StringBuffer();
        for (String field : fields) {
            sb.append(field).append("\n");
        }
        return sb.toString();
    }

    // Alternative: StringJoiner
    public static String buildWithStringJoiner(List<String> fields) {
        StringJoiner sj = new StringJoiner("\n", "", "\n");
        for (String field : fields) {
            sj.add(field);
        }
        return sj.toString();
    }

    // Alternative: String.join()
    public static String buildWithStringJoin(List<String> fields) {
        return String.join("\n", fields) + "\n";
    }

    // Benchmark runner for multiple iterations
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  Q1 BENCHMARK: STRING vs STRINGBUILDER vs STRINGBUFFER UNDER LOAD");
        System.out.println("==========================================================================\n");

        // Warm up the JVM
        List<String> warmupFields = generateTicketFields(20);
        for (int i = 0; i < 1000; i++) {
            buildWithStringConcat(warmupFields);
            buildWithStringBuilder(warmupFields);
            buildWithStringbuffer(warmupFields);
        }

        // --- WORKLOAD 1: Single Short Ticket (10 fields) ---
        System.out.println("--- Workload 1: Single Short Ticket (10 fields) ---");
        List<String> shortFields = generateTicketFields(10);
        runComparison("Workload 1", shortFields, 1, 10000);

        // --- WORKLOAD 2: Batch Generation of 10,000 Tickets with 50 fields each ---
        System.out.println("\n--- Workload 2: Batch Generation of 10,000 Tickets (50 fields each) ---");
        List<String> batchFields = generateTicketFields(50);
        runComparison("Workload 2", batchFields, 10000, 1);

        // --- WORKLOAD 3: Mega Reservation Chart Ticket (3,000 passenger segments in one ticket) ---
        System.out.println("\n--- Workload 3: Single Mega Train Chart (3,000 passenger fields in 1 ticket) ---");
        List<String> megaFields = generateTicketFields(3000);
        runComparison("Workload 3", megaFields, 1, 5);

        // --- Alternative Concepts Benchmark on 50-field ticket ---
        System.out.println("\n--- Alternative Concepts Comparison (50 fields, 10,000 tickets) ---");
        long start, duration;

        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) buildWithStringJoiner(batchFields);
        duration = System.nanoTime() - start;
        System.out.printf("StringJoiner    : %8.2f ms%n", duration / 1_000_000.0);

        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) buildWithStringJoin(batchFields);
        duration = System.nanoTime() - start;
        System.out.printf("String.join()   : %8.2f ms%n", duration / 1_000_000.0);
    }

    private static void runComparison(String label, List<String> fields, int ticketCount, int repeatCount) {
        long timeString = 0, timeBuilder = 0, timeBuffer = 0;

        // Measure String '+'
        for (int r = 0; r < repeatCount; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                buildWithStringConcat(fields);
            }
            timeString += (System.nanoTime() - start);
        }
        timeString /= repeatCount;

        // Measure StringBuilder
        for (int r = 0; r < repeatCount; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                buildWithStringBuilder(fields);
            }
            timeBuilder += (System.nanoTime() - start);
        }
        timeBuilder /= repeatCount;

        // Measure StringBuffer
        for (int r = 0; r < repeatCount; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                buildWithStringbuffer(fields);
            }
            timeBuffer += (System.nanoTime() - start);
        }
        timeBuffer /= repeatCount;

        int fieldsPerTicket = fields.size();
        long totalConcatSteps = (long) ticketCount * fieldsPerTicket;
        long intermediateObjectsString = totalConcatSteps; // Each '+' step creates a new intermediate String
        long intermediateObjectsBuilder = (long) ticketCount; // Only 1 final String per ticket

        System.out.printf("Configuration: %d ticket(s) x %d fields = %d total field operations%n",
                ticketCount, fieldsPerTicket, totalConcatSteps);
        System.out.printf("  1. String (+ loop)   : %10.2f ms | Est. Intermediate Strings: ~%,d%n",
                timeString / 1_000_000.0, intermediateObjectsString);
        System.out.printf("  2. StringBuilder     : %10.2f ms | Est. Intermediate Strings: ~%,d%n",
                timeBuilder / 1_000_000.0, intermediateObjectsBuilder);
        System.out.printf("  3. StringBuffer      : %10.2f ms | Est. Intermediate Strings: ~%,d%n",
                timeBuffer / 1_000_000.0, intermediateObjectsBuilder);

        double speedup = (double) timeString / Math.max(1, timeBuilder);
        System.out.printf("  Speedup (StringBuilder vs String +): %.2fx faster%n", speedup);
    }
}
