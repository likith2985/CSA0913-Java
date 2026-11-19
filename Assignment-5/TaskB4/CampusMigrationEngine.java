package taskb4;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Task B4.1: Production Migration Engine.
 * Reads existing pass files, validates records, streams valid records into
 * database batches within a transaction, and collects an audit report of failed records.
 */
public class CampusMigrationEngine {

    public static class MigrationReport {
        private int totalRecordsRead = 0;
        private int totalRecordsMigrated = 0;
        private final List<String> failedRecords = new ArrayList<>();

        public void recordRead() { totalRecordsRead++; }
        public void recordMigrated() { totalRecordsMigrated++; }
        public void recordFailure(int lineNo, String rawLine, String reason) {
            failedRecords.add(String.format("Line %d: [%s] -> Reason: %s", lineNo, rawLine, reason));
        }

        public int getTotalRecordsRead() { return totalRecordsRead; }
        public int getTotalRecordsMigrated() { return totalRecordsMigrated; }
        public List<String> getFailedRecords() { return failedRecords; }
    }

    public MigrationReport migrate(Path csvFile, Connection con) throws IOException, SQLException {
        MigrationReport report = new MigrationReport();
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";

        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try (BufferedReader reader = Files.newBufferedReader(csvFile);
             PreparedStatement ps = con.prepareStatement(sql)) {

            String line;
            int lineNo = 0;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue; // Skip blank lines
                }
                report.recordRead();

                String[] fields = line.split(",");
                if (fields.length != 5) {
                    report.recordFailure(lineNo, line, "Invalid field count (expected 5, got " + fields.length + ")");
                    continue;
                }

                int route;
                double amount;
                try {
                    route = Integer.parseInt(fields[3].trim());
                    if (route <= 0) {
                        report.recordFailure(lineNo, line, "Route number must be positive");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    report.recordFailure(lineNo, line, "Non-numeric route number");
                    continue;
                }

                try {
                    amount = Double.parseDouble(fields[4].trim());
                    if (amount < 0) {
                        report.recordFailure(lineNo, line, "Fee amount cannot be negative");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    report.recordFailure(lineNo, line, "Non-numeric fee amount");
                    continue;
                }

                // Add to JDBC Batch
                ps.setString(1, fields[0].trim());
                ps.setString(2, fields[1].trim());
                ps.setString(3, fields[2].trim());
                ps.setInt(4, route);
                ps.setDouble(5, amount);
                ps.addBatch();
                report.recordMigrated();
                batchCount++;

                if (batchCount % 1000 == 0) {
                    ps.executeBatch();
                }
            }

            if (batchCount % 1000 != 0) {
                ps.executeBatch();
            }

            con.commit();
        } catch (SQLException | IOException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }

        return report;
    }
}
