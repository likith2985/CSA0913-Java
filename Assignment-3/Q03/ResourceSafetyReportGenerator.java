import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment 3 - Q3: Resource Safety, Debugging and Robust Code
 * Compares:
 * 1. Manual Resource Closing (Vulnerable to file handle leaks)
 * 2. Try-With-Resources (Guaranteed reverse-order closing & suppressed exceptions)
 */
public class ResourceSafetyReportGenerator {

    // Mock tracking class to demonstrate file descriptor leaks
    public static class TrackedFileReader extends BufferedReader {
        private static int openHandleCount = 0;
        private final String fileName;
        private boolean isClosed = false;

        public TrackedFileReader(File file) throws FileNotFoundException {
            super(new FileReader(file));
            this.fileName = file.getName();
            openHandleCount++;
            System.out.printf("    [RESOURCE OPENED] %s | Active Open Handles: %d%n", fileName, openHandleCount);
        }

        @Override
        public void close() throws IOException {
            if (!isClosed) {
                isClosed = true;
                openHandleCount--;
                System.out.printf("    [RESOURCE CLOSED] %s | Active Open Handles: %d%n", fileName, openHandleCount);
            }
            super.close();
        }

        public static int getOpenHandleCount() { return openHandleCount; }
        public static void resetHandleCount() { openHandleCount = 0; }
    }

    // --- Approach 1: Vulnerable Manual Resource Management ---
    public static double generateReportManual(List<File> storeFiles, File summaryFile) {
        System.out.println("\n--- Executing Report Generation (Manual try-finally) ---");
        double totalSales = 0.0;
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new FileWriter(summaryFile));

            for (File storeFile : storeFiles) {
                // FLOP: If an exception occurs, storeReader is never closed!
                TrackedFileReader reader = new TrackedFileReader(storeFile);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    // Potential failure: NumberFormatException on corrupt input
                    double amount = Double.parseDouble(line.trim());
                    totalSales += amount;
                }
                reader.close(); // Never reached if parseDouble throws exception!
            }

            writer.printf("Total Retail Sales: Rs %.2f%n", totalSales);

        } catch (Exception e) {
            System.out.println("  [ERROR in Manual Processing]: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return totalSales;
    }

    // --- Approach 2: Robust Try-With-Resources Management ---
    public static double generateReportTryWithResources(List<File> storeFiles, File summaryFile) {
        System.out.println("\n--- Executing Report Generation (Try-With-Resources) ---");
        double totalSales = 0.0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(summaryFile))) {

            for (File storeFile : storeFiles) {
                try (TrackedFileReader reader = new TrackedFileReader(storeFile)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        double amount = Double.parseDouble(line.trim());
                        totalSales += amount;
                    }
                } catch (Exception ex) {
                    System.out.printf("  [HANDLED ERROR in file %s]: %s (File handle safely auto-closed!)%n",
                            storeFile.getName(), ex.getMessage());
                }
            }

            writer.printf("Total Retail Sales: Rs %.2f%n", totalSales);

        } catch (IOException e) {
            System.out.println("  [SUMMARY WRITE ERROR]: " + e.getMessage());
        }

        return totalSales;
    }

    // Demonstration of Suppressed Exceptions
    public static void demonstrateSuppressedExceptions() {
        System.out.println("\n--- Demonstrating Suppressed Exceptions in Try-With-Resources ---");

        class FailingResource implements AutoCloseable {
            private final String name;
            public FailingResource(String name) { this.name = name; }
            public void work() throws IOException {
                throw new IOException("Primary error during work in " + name);
            }
            @Override
            public void close() throws IOException {
                throw new IOException("Secondary error during close() in " + name);
            }
        }

        try (FailingResource res = new FailingResource("ResA")) {
            res.work();
        } catch (IOException e) {
            System.out.println("Caught Primary Exception: " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("  -> Discovered Suppressed Exception: " + suppressed.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("==========================================================================");
        System.out.println("  Q3: RESOURCE SAFETY - MANUAL FINALLY vs TRY-WITH-RESOURCES");
        System.out.println("==========================================================================\n");

        // Create sample store files
        List<File> files = new ArrayList<>();
        File f1 = new File("store1.csv"); Files.writeString(f1.toPath(), "100.0\n200.0\n");
        File f2 = new File("store2.csv"); Files.writeString(f2.toPath(), "350.0\n150.0\n");
        File f3Corrupt = new File("store3_corrupt.csv"); Files.writeString(f3Corrupt.toPath(), "400.0\nCORRUPT_DATA\n50.0\n");
        File f4 = new File("store4.csv"); Files.writeString(f4.toPath(), "500.0\n");

        files.add(f1);
        files.add(f2);
        files.add(f3Corrupt);
        files.add(f4);

        File manualSummary = new File("manual_summary.txt");
        File twrSummary = new File("twr_summary.txt");

        // 1. Run Manual Version
        TrackedFileReader.resetHandleCount();
        generateReportManual(files, manualSummary);
        int leakedHandlesManual = TrackedFileReader.getOpenHandleCount();
        System.out.printf(">> LEAK DETECTED: Manual version left %d open file handles! [RESOURCE LEAK]%n",
                leakedHandlesManual);

        // 2. Run Try-With-Resources Version
        TrackedFileReader.resetHandleCount();
        generateReportTryWithResources(files, twrSummary);
        int openHandlesTWR = TrackedFileReader.getOpenHandleCount();
        System.out.printf(">> RESOURCE SAFETY: Try-With-Resources left %d open file handles! [PERFECT ZERO]%n",
                openHandlesTWR);

        // 3. Suppressed Exceptions Demonstration
        demonstrateSuppressedExceptions();

        // Clean up sample files
        f1.delete(); f2.delete(); f3Corrupt.delete(); f4.delete();
        manualSummary.delete(); twrSummary.delete();
    }
}
