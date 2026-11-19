package task06;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data Access Object (DAO) for Event Registrations.
 * Uses JDBC with parameterized PreparedStatements.
 * Includes in-memory fallback store to ensure seamless zero-dependency execution.
 */
public class RegistrationDAO {
    private final String dbUrl;
    private boolean useJdbc = true;
    private final List<Registration> memoryStore = new CopyOnWriteArrayList<>();

    public RegistrationDAO() {
        this("jdbc:sqlite::memory:");
    }

    public RegistrationDAO(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS event_registrations (" +
                             "reg_id VARCHAR(32) PRIMARY KEY, " +
                             "student_id VARCHAR(32) NOT NULL, " +
                             "student_name VARCHAR(100) NOT NULL, " +
                             "email VARCHAR(120) NOT NULL, " +
                             "event_name VARCHAR(100) NOT NULL, " +
                             "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                             "CONSTRAINT unique_student_event UNIQUE (student_id, event_name))");
            }
        } catch (ClassNotFoundException | SQLException e) {
            useJdbc = false;
        }
    }

    /**
     * Inserts a new registration. Throws IllegalArgumentException on duplicate registration.
     */
    public synchronized boolean registerStudent(Registration reg) throws SQLException, DuplicateRegistrationException {
        // Enforce duplicate check
        if (isAlreadyRegistered(reg.getStudentId(), reg.getEventName())) {
            throw new DuplicateRegistrationException("Student (" + reg.getStudentId() + ") is already registered for '" + reg.getEventName() + "'.");
        }

        if (useJdbc) {
            String sql = "INSERT INTO event_registrations (reg_id, student_id, student_name, email, event_name, registration_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, reg.getRegId());
                pstmt.setString(2, reg.getStudentId());
                pstmt.setString(3, reg.getStudentName());
                pstmt.setString(4, reg.getEmail());
                pstmt.setString(5, reg.getEventName());
                pstmt.setString(6, reg.getRegistrationDate());
                pstmt.executeUpdate();
                return true;
            }
        } else {
            memoryStore.add(reg);
            return true;
        }
    }

    public synchronized boolean isAlreadyRegistered(String studentId, String eventName) {
        if (useJdbc) {
            String sql = "SELECT 1 FROM event_registrations WHERE student_id = ? AND event_name = ?";
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentId.trim());
                pstmt.setString(2, eventName.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                return false;
            }
        } else {
            return memoryStore.stream().anyMatch(r ->
                r.getStudentId().equalsIgnoreCase(studentId.trim()) &&
                r.getEventName().equalsIgnoreCase(eventName.trim()));
        }
    }

    public synchronized List<Registration> getRegistrations(String filterEvent) {
        List<Registration> list = new ArrayList<>();
        boolean filterAll = filterEvent == null || filterEvent.trim().isEmpty() || "All Events".equalsIgnoreCase(filterEvent);

        if (useJdbc) {
            String sql = filterAll
                ? "SELECT reg_id, student_id, student_name, email, event_name, registration_date FROM event_registrations ORDER BY registration_date DESC"
                : "SELECT reg_id, student_id, student_name, email, event_name, registration_date FROM event_registrations WHERE event_name = ? ORDER BY registration_date DESC";
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (!filterAll) {
                    pstmt.setString(1, filterEvent.trim());
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(new Registration(
                            rs.getString("reg_id"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("email"),
                            rs.getString("event_name"),
                            rs.getString("registration_date")
                        ));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Query error: " + e.getMessage());
            }
        } else {
            for (Registration r : memoryStore) {
                if (filterAll || r.getEventName().equalsIgnoreCase(filterEvent.trim())) {
                    list.add(r);
                }
            }
        }
        return list;
    }

    public static class DuplicateRegistrationException extends Exception {
        public DuplicateRegistrationException(String message) {
            super(message);
        }
    }
}
