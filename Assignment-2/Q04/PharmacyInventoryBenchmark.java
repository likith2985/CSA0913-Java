import java.util.*;

/**
 * Assignment 2 - Q4: Choosing a Map for a Pharmacy Inventory
 * Compares HashMap, TreeMap, and LinkedHashMap across:
 * 1. 100,000 lookups
 * 2. Time to generate Code-Order Report (natural sort)
 * 3. Time to generate Insertion-Order Report (chronological)
 * Across three dataset sizes: 500, 5,000, 50,000 medicines.
 */
public class PharmacyInventoryBenchmark {

    public static class Medicine implements Comparable<Medicine> {
        private final String code;
        private final String name;
        private int stock;

        public Medicine(String code, String name, int stock) {
            this.code = code;
            this.name = name;
            this.stock = stock;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public int getStock() { return stock; }

        @Override
        public int compareTo(Medicine o) {
            return this.code.compareTo(o.code);
        }

        @Override
        public String toString() {
            return code + ":" + name + "(" + stock + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  Q4 BENCHMARK: MAP SELECTION FOR HOSPITAL PHARMACY INVENTORY");
        System.out.println("==========================================================================\n");

        int[] datasetSizes = {500, 5000, 50000};
        int lookupTrials = 100000;

        for (int size : datasetSizes) {
            System.out.println("==========================================================================");
            System.out.printf("  DATASET SIZE: %,d Medicines | Lookups: %,d%n", size, lookupTrials);
            System.out.println("==========================================================================");

            // Generate deterministic medicine data in arbitrary order
            List<Medicine> inputData = generateMedicines(size);

            // 1. Populate Maps
            Map<String, Medicine> hashMap = new HashMap<>();
            Map<String, Medicine> treeMap = new TreeMap<>();
            Map<String, Medicine> linkedHashMap = new LinkedHashMap<>();

            for (Medicine m : inputData) {
                hashMap.put(m.getCode(), m);
                treeMap.put(m.getCode(), m);
                linkedHashMap.put(m.getCode(), m);
            }

            // Generate lookup keys
            Random rand = new Random(1337);
            String[] queryKeys = new String[lookupTrials];
            for (int i = 0; i < lookupTrials; i++) {
                queryKeys[i] = inputData.get(rand.nextInt(size)).getCode();
            }

            // 2. Measure 100,000 Lookups
            long tGetHash = benchmarkLookups(hashMap, queryKeys);
            long tGetTree = benchmarkLookups(treeMap, queryKeys);
            long tGetLinked = benchmarkLookups(linkedHashMap, queryKeys);

            System.out.println("1. Time for 100,000 Lookups:");
            System.out.printf("   HashMap       : %8.2f ms (Avg: %6.2f ns/op) [O(1)]%n",
                    tGetHash / 1e6, (double) tGetHash / lookupTrials);
            System.out.printf("   LinkedHashMap : %8.2f ms (Avg: %6.2f ns/op) [O(1)]%n",
                    tGetLinked / 1e6, (double) tGetLinked / lookupTrials);
            System.out.printf("   TreeMap       : %8.2f ms (Avg: %6.2f ns/op) [O(log N)]%n",
                    tGetTree / 1e6, (double) tGetTree / lookupTrials);

            // 3. Report A: All medicines in Code Order (Alphabetical / Natural)
            System.out.println("\n2. Report 1 Generation: Code Order (Alphabetical):");
            // TreeMap: Native iterator
            long start = System.nanoTime();
            List<Medicine> codeReportTree = new ArrayList<>(treeMap.values());
            long tReportTree = System.nanoTime() - start;

            // HashMap: Requires copying and explicit sorting!
            start = System.nanoTime();
            List<Medicine> codeReportHash = new ArrayList<>(hashMap.values());
            Collections.sort(codeReportHash);
            long tReportHash = System.nanoTime() - start;

            // LinkedHashMap: Requires copying and explicit sorting!
            start = System.nanoTime();
            List<Medicine> codeReportLinked = new ArrayList<>(linkedHashMap.values());
            Collections.sort(codeReportLinked);
            long tReportLinked = System.nanoTime() - start;

            System.out.printf("   TreeMap       : %8.3f ms [NATIVELY SORTED - 0 extra sorting]%n", tReportTree / 1e6);
            System.out.printf("   HashMap       : %8.3f ms [Requires Collections.sort() O(N log N)]%n", tReportHash / 1e6);
            System.out.printf("   LinkedHashMap : %8.3f ms [Requires Collections.sort() O(N log N)]%n", tReportLinked / 1e6);

            // 4. Report B: All medicines in Insertion Order (Chronological)
            System.out.println("\n3. Report 2 Generation: Insertion Order (Chronological):");
            // LinkedHashMap: Native iterator
            start = System.nanoTime();
            List<Medicine> insertReportLinked = new ArrayList<>(linkedHashMap.values());
            long tInsertLinked = System.nanoTime() - start;

            // HashMap / TreeMap: CANNOT produce insertion order without external history list!
            System.out.printf("   LinkedHashMap : %8.3f ms [NATIVELY PRESERVES INSERTION ORDER]%n", tInsertLinked / 1e6);
            System.out.println("   HashMap       : CANNOT produce insertion order (Bucket order is arbitrary)");
            System.out.println("   TreeMap       : CANNOT produce insertion order (Keys always sorted by code)");

            // Order correctness check
            boolean treeMatchesCodeOrder = isSorted(codeReportTree);
            boolean linkedMatchesInsertOrder = matchesInputOrder(insertReportLinked, inputData);
            System.out.printf("\nOrder Validation:%n");
            System.out.println("   TreeMap matches Code Order: " + treeMatchesCodeOrder);
            System.out.println("   LinkedHashMap matches Insertion Order: " + linkedMatchesInsertOrder);
            System.out.println();
        }
    }

    private static List<Medicine> generateMedicines(int count) {
        List<Medicine> list = new ArrayList<>(count);
        // Ensure unique codes in pseudo-random order
        List<Integer> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) ids.add(i + 1);
        Collections.shuffle(ids, new Random(42));

        for (int i = 0; i < count; i++) {
            String code = String.format("MED-%06d", ids.get(i));
            list.add(new Medicine(code, "Drug_" + i, 50 + (i % 500)));
        }
        return list;
    }

    private static long benchmarkLookups(Map<String, Medicine> map, String[] keys) {
        long start = System.nanoTime();
        long dummy = 0;
        for (String k : keys) {
            Medicine m = map.get(k);
            if (m != null) dummy += m.getStock();
        }
        return System.nanoTime() - start;
    }

    private static boolean isSorted(List<Medicine> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).getCode().compareTo(list.get(i + 1).getCode()) > 0) return false;
        }
        return true;
    }

    private static boolean matchesInputOrder(List<Medicine> actual, List<Medicine> expected) {
        if (actual.size() != expected.size()) return false;
        for (int i = 0; i < actual.size(); i++) {
            if (!actual.get(i).getCode().equals(expected.get(i).getCode())) return false;
        }
        return true;
    }
}
