package task06;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain entity representing an event registration record.
 */
public class Registration {
    private final String regId;
    private final String studentId;
    private final String studentName;
    private final String email;
    private final String eventName;
    private final String registrationDate;

    public Registration(String regId, String studentId, String studentName, String email, String eventName) {
        this(regId, studentId, studentName, email, eventName,
             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public Registration(String regId, String studentId, String studentName, String email, String eventName, String registrationDate) {
        this.regId = regId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.eventName = eventName;
        this.registrationDate = registrationDate;
    }

    public String getRegId() { return regId; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getEmail() { return email; }
    public String getEventName() { return eventName; }
    public String getRegistrationDate() { return registrationDate; }

    public Object[] toTableRow() {
        return new Object[]{regId, studentId, studentName, email, eventName, registrationDate};
    }

    public String toCsvRow() {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
            regId, studentId, studentName, email, eventName, registrationDate);
    }
}
