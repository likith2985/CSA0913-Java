/**
 * Task 5: Apply static Members and the Object Life Cycle
 * Demonstrates:
 * 1. Static vs Instance variables (Shared ticket sequence & global occupancy)
 * 2. Static initializer blocks
 * 3. Pass-by-value semantics for object references in Java
 * 4. Object lifecycle and Garbage Collection eligibility
 * 5. Multi-facility architecture (North vs South Gate ticket counters)
 */
public class StaticAndLifecycleDemo {

    // Ticket class with class-level static sequence counter
    public static class Ticket {
        private static int sequenceCounter = 1000; // Static variable: shared across all instances
        private static final String FACILITY_CODE;

        static {
            // Static initialization block
            FACILITY_CODE = "CAMPUS-MAIN";
            System.out.println("[Static Init] Class loaded. Facility Code set to: " + FACILITY_CODE);
        }

        private final String ticketId;
        private final String regNo;
        private boolean isSettled;

        public Ticket(String regNo) {
            this.ticketId = FACILITY_CODE + "-" + (++sequenceCounter);
            this.regNo = regNo;
            this.isSettled = false;
        }

        public static int getNextSequence() {
            return sequenceCounter + 1;
        }

        public void settle() { this.isSettled = true; }
        public String getTicketId() { return ticketId; }
        public String getRegNo() { return regNo; }
        public boolean isSettled() { return isSettled; }

        @Override
        public String toString() {
            return "Ticket{" + "id='" + ticketId + '\'' + ", reg='" + regNo + '\'' + ", settled=" + isSettled + '}';
        }
    }

    // Facility with its own instance-level counter (for North & South gates)
    public static class MultiGateFacility {
        private final String gateName;
        private int localCounter; // Instance variable: unique per gate facility

        public MultiGateFacility(String gateName) {
            this.gateName = gateName;
            this.localCounter = 0;
        }

        public String issueTicket(String regNo) {
            localCounter++;
            return String.format("%s-TKT-%04d (Vehicle: %s)", gateName, localCounter, regNo);
        }

        public int getLocalCounter() { return localCounter; }
        public String getGateName() { return gateName; }
    }

    // Method demonstrating Pass-by-Value of object references
    public static void processTicket(Ticket t) {
        System.out.println("Processing ticket: " + t.getTicketId());
        t.settle(); // Mutates the object referenced on the heap
        t = null;   // Reassigns local copy of reference; does NOT affect caller's reference
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 5: STATIC MEMBERS AND OBJECT LIFE CYCLE ===");

        // 1. Static sequence across multiple Ticket creations
        System.out.println("\n--- 1. Shared Static Sequence Counter ---");
        Ticket t1 = new Ticket("KA01AA1111");
        Ticket t2 = new Ticket("TN02BB2222");
        System.out.println("Ticket 1: " + t1);
        System.out.println("Ticket 2: " + t2);
        System.out.println("Next global sequence will be: " + Ticket.getNextSequence());

        // 2. Pass-by-value demonstration
        System.out.println("\n--- 2. Object Reference Passing Semantics ---");
        System.out.println("Before processTicket: " + t1);
        processTicket(t1);
        System.out.println("After processTicket: " + t1);
        System.out.println("t1 is still reachable and not null in caller scope!");

        // 3. Object Lifecycle and Garbage Collection Eligibility
        System.out.println("\n--- 3. Garbage Collection Eligibility ---");
        System.out.println("Setting t2 = null...");
        t2 = null; // Object previously pointed to by t2 has zero incoming live GC roots
        System.out.println("The heap object formerly referenced by t2 is now eligible for Garbage Collection.");

        // 4. Multi-facility Analysis: North Gate vs South Gate
        System.out.println("\n--- 4. Multi-Gate Facility Instance Counters ---");
        MultiGateFacility northGate = new MultiGateFacility("NORTH-GATE");
        MultiGateFacility southGate = new MultiGateFacility("SOUTH-GATE");

        System.out.println(northGate.issueTicket("TN01-CAR-1"));
        System.out.println(northGate.issueTicket("TN01-CAR-2"));
        System.out.println(southGate.issueTicket("KA02-BIKE-1"));
        System.out.println(northGate.issueTicket("TN01-CAR-3"));

        System.out.printf("North Gate Total Issues: %d%n", northGate.getLocalCounter());
        System.out.printf("South Gate Total Issues: %d%n", southGate.getLocalCounter());
    }
}
