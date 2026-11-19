import java.time.LocalDateTime;

/**
 * Task 4: Design Classes, Objects and Constructors
 * Demonstrates:
 * 1. Default, Parameterized, and Overloaded constructors
 * 2. Constructor chaining via this(...)
 * 3. Copy constructor for re-issuing duplicate tickets
 * 4. Comparison with JavaBeans (Getters/Setters) and Static Factory methods
 */
public class ConstructorDemo {

    // Domain Model: ParkingTicket
    public static class ParkingTicket {
        private final String ticketId;
        private final String regNo;
        private final int floor;
        private final int slot;
        private final LocalDateTime entryTime;
        private final boolean isPassHolder;

        // 1. Primary Parameterized Constructor
        public ParkingTicket(String ticketId, String regNo, int floor, int slot,
                             LocalDateTime entryTime, boolean isPassHolder) {
            if (ticketId == null || regNo == null) {
                throw new IllegalArgumentException("Ticket ID and Registration Number cannot be null");
            }
            this.ticketId = ticketId;
            this.regNo = regNo;
            this.floor = floor;
            this.slot = slot;
            this.entryTime = (entryTime != null) ? entryTime : LocalDateTime.now();
            this.isPassHolder = isPassHolder;
        }

        // 2. Overloaded Constructor (Constructor chaining using this(...))
        // Defaults isPassHolder to false and entryTime to now
        public ParkingTicket(String ticketId, String regNo, int floor, int slot) {
            this(ticketId, regNo, floor, slot, LocalDateTime.now(), false);
        }

        // 3. Copy Constructor (Useful for re-issuing a duplicate lost/damaged ticket)
        public ParkingTicket(ParkingTicket original) {
            this(original.ticketId + "-DUP", original.regNo, original.floor, original.slot,
                 original.entryTime, original.isPassHolder);
        }

        // 4. Static Factory Method Pattern
        public static ParkingTicket issueVisitorTicket(String ticketId, String regNo, int floor, int slot) {
            return new ParkingTicket(ticketId, regNo, floor, slot, LocalDateTime.now(), false);
        }

        public static ParkingTicket issuePassHolderTicket(String ticketId, String regNo, int floor, int slot) {
            return new ParkingTicket(ticketId, regNo, floor, slot, LocalDateTime.now(), true);
        }

        // Getters
        public String getTicketId() { return ticketId; }
        public String getRegNo() { return regNo; }
        public int getFloor() { return floor; }
        public int getSlot() { return slot; }
        public LocalDateTime getEntryTime() { return entryTime; }
        public boolean isPassHolder() { return isPassHolder; }

        @Override
        public String toString() {
            return String.format("Ticket[ID=%s, RegNo=%s, Slot=[%d,%d], Entry=%s, Pass=%s]",
                    ticketId, regNo, floor, slot, entryTime, isPassHolder);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 4: CLASSES, OBJECTS AND CONSTRUCTORS ===");

        // Using Chained Overloaded Constructor
        ParkingTicket t1 = new ParkingTicket("TKT-1001", "TN-01-AA-1111", 0, 1);
        System.out.println("1. Ticket created via Overloaded Chained Constructor:");
        System.out.println("   " + t1);

        // Using Full Parameterized Constructor
        ParkingTicket t2 = new ParkingTicket("TKT-1002", "TN-02-BB-2222", 1, 0,
                LocalDateTime.of(2026, 11, 19, 10, 0), true);
        System.out.println("\n2. Ticket created via Full Parameterized Constructor:");
        System.out.println("   " + t2);

        // Using Copy Constructor (Re-issuing duplicate ticket)
        ParkingTicket t1Duplicate = new ParkingTicket(t1);
        System.out.println("\n3. Duplicate Ticket re-issued via Copy Constructor:");
        System.out.println("   " + t1Duplicate);

        // Using Static Factory Method
        ParkingTicket t3 = ParkingTicket.issuePassHolderTicket("TKT-1003", "KA-05-EV-3333", 2, 5);
        System.out.println("\n4. Ticket created via Static Factory Method:");
        System.out.println("   " + t3);

        System.out.println("\nMemory Model Demonstration:");
        System.out.println("Stack: Stores reference variables (t1, t2, t1Duplicate, t3) holding memory addresses.");
        System.out.println("Heap:  Stores the actual ParkingTicket objects and their fields.");
        System.out.println("t1 == t1Duplicate: " + (t1 == t1Duplicate) + " (Distinct Heap Objects)");
    }
}
