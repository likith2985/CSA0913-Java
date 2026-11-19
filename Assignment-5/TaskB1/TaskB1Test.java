package taskb1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class TaskB1Test {
    public static void main(String[] args) throws IOException {
        System.out.println("===============================================================");
        System.out.println("      TASK B1: IMPLEMENT AND INSTRUMENT THE FILE STORE         ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        Path tempFile = Files.createTempFile("test_bus_passes_", ".csv");
        PassStore store = new PassStore(tempFile);

        // -------------------------------------------------------------
        // Test 1: Normal Add and Find
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 1] Testing Normal Pass Record Insertion & Retrieval:");
        store.add("P1001", "192311001", "Arun Kumar", 12, 4500.0);
        String[] found1 = store.find("P1001");
        System.out.println("Result: " + Arrays.toString(found1));
        if (found1 != null && "P1001".equals(found1[0]) && "Arun Kumar".equals(found1[2])) {
            System.out.println(">>> PASS: Normal pass successfully written and retrieved.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Normal pass retrieval failed.");
        }

        // -------------------------------------------------------------
        // Test 2: Pass ID that Does Not Exist
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 2] Testing Non-Existent Pass ID Lookup:");
        String[] foundMissing = store.find("P9999_NON_EXISTENT");
        System.out.println("Result for non-existent ID: " + Arrays.toString(foundMissing));
        if (foundMissing == null) {
            System.out.println(">>> PASS: Non-existent ID correctly returned null.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Expected null for missing ID.");
        }

        // -------------------------------------------------------------
        // Test 3: Handling Damaged Line Without Stopping Search
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 3] Testing Search Resilience with Corrupted Line Injected:");
        Files.writeString(tempFile, "P1002,DAMAGED_RECORD_WITH_FEW_FIELDS\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya Sharma", 12, 3800.0);

        String[] foundDamaged = store.find("P1002");
        String[] foundSubsequent = store.find("P1003");

        System.out.println("Damaged line query (P1002): " + Arrays.toString(foundDamaged));
        System.out.println("Subsequent valid line query (P1003): " + Arrays.toString(foundSubsequent));

        if (foundDamaged == null && foundSubsequent != null && "P1003".equals(foundSubsequent[0])) {
            System.out.println(">>> PASS: Damaged line safely skipped; search progressed to find subsequent valid records.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Damaged line either stopped the search or was accepted as valid.");
        }

        // -------------------------------------------------------------
        // Test 4: List Passes by Route
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 4] Testing Route Filtering (Route 12):");
        List<String[]> route12Passes = store.listByRoute(12);
        System.out.println("Route 12 Matches Count: " + route12Passes.size());
        for (String[] p : route12Passes) {
            System.out.println("   -> Pass " + p[0] + " (" + p[2] + ", Reg: " + p[1] + ")");
        }
        if (route12Passes.size() == 2) {
            System.out.println(">>> PASS: Route filter successfully extracted exactly matching passes.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Route filter count mismatch.");
        }

        Files.deleteIfExists(tempFile);

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " unit tests.");
        System.out.println("===============================================================");

        // Run Benchmarks
        FileBenchmark.runBufferingBenchmark();
        FileBenchmark.runSearchGrowthBenchmark();
    }
}
