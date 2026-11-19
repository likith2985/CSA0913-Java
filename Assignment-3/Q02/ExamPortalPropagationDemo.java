import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Assignment 3 - Q2: User-Defined Exceptions and Multi-Layer Propagation
 * Architecture:
 *   Controller (ExamController)
 *      └── Service (ExamService)
 *            └── Storage (ExamStorage)
 */
public class ExamPortalPropagationDemo {

    // Base Domain Exception
    public static class ExamException extends Exception {
        public ExamException(String message) { super(message); }
        public ExamException(String message, Throwable cause) { super(message, cause); }
    }

    // Subclass 1: Exam Closed
    public static class ExamClosedException extends ExamException {
        public ExamClosedException(String message) { super(message); }
    }

    // Subclass 2: Unregistered Student
    public static class UnregisteredStudentException extends ExamException {
        public UnregisteredStudentException(String message) { super(message); }
    }

    // Subclass 3: Storage failure wrapping lower-level I/O issues (Chained Exception)
    public static class SubmissionStorageException extends ExamException {
        public SubmissionStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // --- Layer 1: Storage Layer ---
    public static class ExamStorage {
        public void persistAnswerSheet(String studentId, String fileName, long sizeKb)
                throws SubmissionStorageException {
            try {
                if (sizeKb > 500) {
                    throw new IOException("Storage volume full: Disk quota exceeded for block /var/exams");
                }
                System.out.printf("    [Storage Layer] Persisted %s (%d KB) to disk.%n", fileName, sizeKb);
            } catch (IOException ioEx) {
                // EXCEPTION CHAINING: Wrap low-level I/O into domain exception
                throw new SubmissionStorageException(
                        "File store rejected script for student " + studentId, ioEx);
            }
        }
    }

    // --- Layer 2: Business Service Layer ---
    public static class ExamService {
        private final ExamStorage storage = new ExamStorage();
        private final Set<String> registeredStudents = new HashSet<>();
        private final LocalDateTime examDeadline = LocalDateTime.of(2026, 11, 20, 12, 0);

        public ExamService() {
            registeredStudents.add("STU-1001");
            registeredStudents.add("STU-1002");
        }

        public void submitAnswerScript(String studentId, String fileName, long sizeKb, LocalDateTime submitTime)
                throws ExamException {

            // Rule 1: Check deadline
            if (submitTime.isAfter(examDeadline)) {
                throw new ExamClosedException("Submission window closed at " + examDeadline + ". Attempted at: " + submitTime);
            }

            // Rule 2: Check course registration
            if (!registeredStudents.contains(studentId)) {
                throw new UnregisteredStudentException("Student " + studentId + " is not registered for this course examination!");
            }

            // Delegate to storage layer
            storage.persistAnswerSheet(studentId, fileName, sizeKb);
        }
    }

    // --- Layer 3: Presentation Controller Layer ---
    public static class ExamController {
        private final ExamService service = new ExamService();

        public String handleStudentSubmission(String studentId, String fileName, long sizeKb, LocalDateTime submitTime) {
            try {
                System.out.printf("Incoming submission from %s (file: %s, size: %d KB)...%n", studentId, fileName, sizeKb);
                service.submitAnswerScript(studentId, fileName, sizeKb, submitTime);
                return "HTTP 200 OK: Your answer script was accepted and safely recorded.";
            } catch (ExamClosedException ece) {
                return "HTTP 403 FORBIDDEN: [Exam Closed] " + ece.getMessage();
            } catch (UnregisteredStudentException use) {
                return "HTTP 401 UNAUTHORIZED: [Not Registered] " + use.getMessage();
            } catch (SubmissionStorageException sse) {
                // High-level message shown to student while logging low-level cause internally
                return String.format("HTTP 500 SERVER ERROR: [Storage Issue] %s (Underlying Cause: %s)",
                        sse.getMessage(), sse.getCause().getMessage());
            } catch (ExamException ee) {
                return "HTTP 400 BAD REQUEST: [Portal Error] " + ee.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  Q2: ONLINE EXAM PORTAL - USER EXCEPTIONS & MULTI-LAYER PROPAGATION");
        System.out.println("==========================================================================\n");

        ExamController controller = new ExamController();
        LocalDateTime onTime = LocalDateTime.of(2026, 11, 20, 11, 30);
        LocalDateTime lateTime = LocalDateTime.of(2026, 11, 20, 12, 15);

        // Scenario 1: Successful Submission
        System.out.println("--- Scenario 1: Valid Normal Submission ---");
        String res1 = controller.handleStudentSubmission("STU-1001", "ans_1001.pdf", 320, onTime);
        System.out.println("Response to Student: " + res1 + "\n");

        // Scenario 2: Submission After Deadline (ExamClosedException)
        System.out.println("--- Scenario 2: Late Submission ---");
        String res2 = controller.handleStudentSubmission("STU-1001", "ans_late.pdf", 250, lateTime);
        System.out.println("Response to Student: " + res2 + "\n");

        // Scenario 3: Unregistered Student (UnregisteredStudentException)
        System.out.println("--- Scenario 3: Unregistered Student ---");
        String res3 = controller.handleStudentSubmission("STU-9999", "ans_9999.pdf", 180, onTime);
        System.out.println("Response to Student: " + res3 + "\n");

        // Scenario 4: Storage Exceeded (SubmissionStorageException with Chained IOException)
        System.out.println("--- Scenario 4: Storage Quota Exceeded (Chained Exception) ---");
        String res4 = controller.handleStudentSubmission("STU-1002", "huge_ans.pdf", 950, onTime);
        System.out.println("Response to Student: " + res4);
    }
}
