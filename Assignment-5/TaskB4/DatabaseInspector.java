package taskb4;

import java.sql.*;

/**
 * Task B4.2: Database Metadata Inspector.
 * Uses DatabaseMetaData to inspect product details, driver version,
 * and existing database schema for portability and server migration verification.
 */
public class DatabaseInspector {

    public static void inspect(Connection con) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();

        System.out.println("---------------------------------------------------------------");
        System.out.println("          DATABASE METADATA & SCHEMA AUDIT REPORT              ");
        System.out.println("---------------------------------------------------------------");

        System.out.println("Database Product Name    : " + meta.getDatabaseProductName());
        System.out.println("Database Product Version : " + meta.getDatabaseProductVersion());
        System.out.println("JDBC Driver Name         : " + meta.getDriverName());
        System.out.println("JDBC Driver Version      : " + meta.getDriverVersion());
        System.out.println("JDBC Specification Major : " + meta.getJDBCMajorVersion());
        System.out.println("Max Connections Supported: " + meta.getMaxConnections());
        System.out.println("Supports Transactions    : " + meta.supportsTransactions());
        System.out.println("Supports Batch Updates   : " + meta.supportsBatchUpdates());

        System.out.println("\nTables Present in Database:");
        try (ResultSet rsTables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rsTables.next()) {
                String tableName = rsTables.getString("TABLE_NAME");
                String tableType = rsTables.getString("TABLE_TYPE");
                System.out.println("   -> Table: [" + tableName + "] (" + tableType + ")");
            }
        }

        System.out.println("\nColumns for Table [bus_pass]:");
        try (ResultSet rsCols = meta.getColumns(null, null, "bus_pass", "%")) {
            System.out.printf("   %-16s | %-12s | %-10s%n", "Column Name", "Data Type", "Nullable");
            System.out.println("   -------------------------------------------------");
            while (rsCols.next()) {
                String colName = rsCols.getString("COLUMN_NAME");
                String typeName = rsCols.getString("TYPE_NAME");
                String isNullable = rsCols.getString("IS_NULLABLE");
                System.out.printf("   %-16s | %-12s | %-10s%n", colName, typeName, isNullable);
            }
        }
        System.out.println("---------------------------------------------------------------");
    }
}
