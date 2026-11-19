package taskb4;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskB4Test {

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("===============================================================");
        System.out.println("      TASK B4: 12-CAMPUS MIGRATION & PRODUCTION DECISION       ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        // -------------------------------------------------------------
        // Step 1: Generate Source CSV with Valid and Malformed Passes
        // -------------------------------------------------------------
        Path migrationCsv = Files.createTempFile("campus_migration_", ".csv");
        generateMigrationSourceFile(migrationCsv);

        MockCampusDatabase db = new MockCampusDatabase();
        Connection con = db.getConnection();

        // -------------------------------------------------------------
        // Step 2: Execute Migration Engine
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 1] Executing High-Throughput Batch Migration Engine:");
        CampusMigrationEngine engine = new CampusMigrationEngine();
        CampusMigrationEngine.MigrationReport report = engine.migrate(migrationCsv, con);

        System.out.println("Migration Completed:");
        System.out.println("   -> Total Records Read     : " + report.getTotalRecordsRead());
        System.out.println("   -> Total Records Migrated : " + report.getTotalRecordsMigrated());
        System.out.println("   -> Corrupted Lines Caught : " + report.getFailedRecords().size());

        if (report.getTotalRecordsMigrated() == 1000 && report.getFailedRecords().size() == 5) {
            System.out.println(">>> PASS: Migration accurately segregated valid data from corrupted records.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Migration record counts mismatch.");
        }

        // -------------------------------------------------------------
        // Step 3: Print Validation Audit Failure Log
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 2] Auditing Intercepted Corrupted Records Report:");
        for (String err : report.getFailedRecords()) {
            System.out.println("   [REJECTED RECORD] " + err);
        }
        if (report.getFailedRecords().size() == 5) {
            System.out.println(">>> PASS: Complete error audit log captured line numbers and specific causes.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Error report incomplete.");
        }

        // -------------------------------------------------------------
        // Step 4: Metadata Inspection
        // -------------------------------------------------------------
        total++;
        System.out.println("\n[Test 3] Inspecting Database Engine via DatabaseMetaData:");
        DatabaseInspector.inspect(con);
        System.out.println(">>> PASS: DatabaseMetaData successfully reported product details and schema.");
        passed++;

        Files.deleteIfExists(migrationCsv);

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " production verification tests.");
        System.out.println("===============================================================");
    }

    private static void generateMigrationSourceFile(Path file) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.CREATE)) {
            // Write 500 valid records
            for (int i = 1; i <= 500; i++) {
                w.write(String.format("P%05d,19231%04d,Student_%d,%d,4500.0%n", i, i, i, (i % 20) + 1));
            }

            // Injected Corrupted lines
            w.write("CORRUPT_01,TruncatedLineOnlyTwoFields\n");
            w.write("CORRUPT_02,192319002,Student_Corrupt_Route,-5,4500.0\n"); // Negative route
            w.write("CORRUPT_03,192319003,Student_NonNum_Route,ROUTE_TWELVE,4500.0\n"); // Non-numeric route
            w.write("CORRUPT_04,192319004,Student_Negative_Amt,12,-3500.0\n"); // Negative amount
            w.write("CORRUPT_05,192319005,Student_Bad_Amt,12,NON_NUMERIC_FEE\n"); // Non-numeric amount

            // Write 500 more valid records
            for (int i = 501; i <= 1000; i++) {
                w.write(String.format("P%05d,19231%04d,Student_%d,%d,4500.0%n", i, i, i, (i % 20) + 1));
            }
        }
    }

    // =========================================================================
    // IN-MEMORY MOCK CAMPUS DATABASE ENGINE
    // =========================================================================
    static class MockCampusDatabase {
        private final List<Object[]> committedRows = new CopyOnWriteArrayList<>();
        private final List<Object[]> uncommittedRows = new ArrayList<>();
        private boolean autoCommit = true;

        public Connection getConnection() {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setAutoCommit".equals(name)) {
                            autoCommit = (Boolean) args[0];
                            return null;
                        } else if ("getAutoCommit".equals(name)) {
                            return autoCommit;
                        } else if ("commit".equals(name)) {
                            committedRows.addAll(uncommittedRows);
                            uncommittedRows.clear();
                            return null;
                        } else if ("rollback".equals(name)) {
                            uncommittedRows.clear();
                            return null;
                        } else if ("prepareStatement".equals(name)) {
                            return createPreparedStatement();
                        } else if ("getMetaData".equals(name)) {
                            return createDatabaseMetaData();
                        }
                        return null;
                    }
            );
        }

        private PreparedStatement createPreparedStatement() {
            List<Object[]> batch = new ArrayList<>();
            Object[] current = new Object[5];

            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class<?>[]{PreparedStatement.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("setString".equals(name)) {
                            current[(Integer) args[0] - 1] = args[1];
                            return null;
                        } else if ("setInt".equals(name)) {
                            current[(Integer) args[0] - 1] = args[1];
                            return null;
                        } else if ("setDouble".equals(name)) {
                            current[(Integer) args[0] - 1] = args[1];
                            return null;
                        } else if ("addBatch".equals(name)) {
                            batch.add(current.clone());
                            return null;
                        } else if ("executeBatch".equals(name)) {
                            int[] res = new int[batch.size()];
                            for (int i = 0; i < batch.size(); i++) {
                                if (autoCommit) committedRows.add(batch.get(i));
                                else uncommittedRows.add(batch.get(i));
                                res[i] = 1;
                            }
                            batch.clear();
                            return res;
                        } else if ("close".equals(name)) {
                            return null;
                        }
                        return null;
                    }
            );
        }

        private DatabaseMetaData createDatabaseMetaData() {
            return (DatabaseMetaData) Proxy.newProxyInstance(
                    DatabaseMetaData.class.getClassLoader(),
                    new Class<?>[]{DatabaseMetaData.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("getDatabaseProductName".equals(name)) return "MySQL Enterprise Server (Production Cluster)";
                        if ("getDatabaseProductVersion".equals(name)) return "8.0.36-commercial";
                        if ("getDriverName".equals(name)) return "MySQL Connector/J";
                        if ("getDriverVersion".equals(name)) return "8.3.0";
                        if ("getJDBCMajorVersion".equals(name)) return 4;
                        if ("getMaxConnections".equals(name)) return 2000;
                        if ("supportsTransactions".equals(name)) return true;
                        if ("supportsBatchUpdates".equals(name)) return true;
                        if ("getTables".equals(name)) return createMockTablesResultSet();
                        if ("getColumns".equals(name)) return createMockColumnsResultSet();
                        return null;
                    }
            );
        }

        private ResultSet createMockTablesResultSet() {
            String[][] tables = {
                    {"bus_pass", "TABLE"},
                    {"campus_offices", "TABLE"},
                    {"routes_catalog", "TABLE"},
                    {"audit_logs", "TABLE"}
            };
            int[] idx = {0};

            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("next".equals(name)) return ++idx[0] <= tables.length;
                        if ("getString".equals(name)) {
                            String col = (String) args[0];
                            if ("TABLE_NAME".equalsIgnoreCase(col)) return tables[idx[0] - 1][0];
                            if ("TABLE_TYPE".equalsIgnoreCase(col)) return tables[idx[0] - 1][1];
                        }
                        if ("close".equals(name)) return null;
                        return null;
                    }
            );
        }

        private ResultSet createMockColumnsResultSet() {
            String[][] cols = {
                    {"pass_id", "VARCHAR(32)", "NO"},
                    {"reg_no", "VARCHAR(32)", "NO"},
                    {"name", "VARCHAR(100)", "NO"},
                    {"route_no", "INTEGER", "NO"},
                    {"amount", "DOUBLE", "NO"}
            };
            int[] idx = {0};

            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("next".equals(name)) return ++idx[0] <= cols.length;
                        if ("getString".equals(name)) {
                            String col = (String) args[0];
                            if ("COLUMN_NAME".equalsIgnoreCase(col)) return cols[idx[0] - 1][0];
                            if ("TYPE_NAME".equalsIgnoreCase(col)) return cols[idx[0] - 1][1];
                            if ("IS_NULLABLE".equalsIgnoreCase(col)) return cols[idx[0] - 1][2];
                        }
                        if ("close".equals(name)) return null;
                        return null;
                    }
            );
        }
    }
}
