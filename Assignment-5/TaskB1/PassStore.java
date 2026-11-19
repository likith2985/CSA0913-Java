package taskb1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bus-Pass File Store Implementation.
 * Completed from the prompt skeleton (Page 6).
 */
public class PassStore {
    private final Path file;

    public PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name,
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    /** Returns the five fields of the pass with the given ID, or null if not found. */
    public String[] find(String passId) throws IOException {
        // TODO 1: Read the file line by line with a BufferedReader inside try-with-
        // resources; skip damaged lines (not exactly 5 fields); return the fields of
        // the matching pass
        if (!Files.exists(file)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                String[] fields = line.split(",");
                if (fields.length == 5 && fields[0].equals(passId)) {
                    return fields;
                }
            }
        }
        return null;
    }

    /** Lists all passes registered on a given route */
    public List<String[]> listByRoute(int targetRoute) throws IOException {
        List<String[]> list = new ArrayList<>();
        if (!Files.exists(file)) {
            return list;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length == 5) {
                    try {
                        int route = Integer.parseInt(fields[3].trim());
                        if (route == targetRoute) {
                            list.add(fields);
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip corrupted route fields
                    }
                }
            }
        }
        return list;
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);
        System.out.println(Arrays.toString(store.find("P1003")));
        System.out.println(Arrays.toString(store.find("P1002")));
        Files.delete(f);
    }
}
