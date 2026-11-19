import java.time.LocalDateTime;
import java.util.*;

/**
 * Task 11: Implement, Test and Defend the Design
 * Complete Campus Smart-Parking System Implementation with Test Table Verification
 * and Exit-Processing Data Structure Benchmarking.
 */
public class CampusSmartParkingApp {

    // --- 1. DOMAIN MODEL ---

    public enum UserType {
        STUDENT_PASS,
        STAFF_PASS,
        VISITOR
    }

    public interface Chargeable {
        double chargingFee(int kWh);
    }

    public static abstract class Vehicle {
        private final String regNo;

        protected Vehicle(String regNo) {
            if (regNo == null || regNo.trim().isEmpty()) {
                throw new IllegalArgumentException("Registration number cannot be null or empty");
            }
            this.regNo = regNo.trim().toUpperCase();
        }

        public String getRegNo() { return regNo; }

        public abstract double ratePerHour();

        public double calculateFee(int minutes) {
            if (minutes < 0) throw new IllegalArgumentException("Minutes cannot be negative");
            if (minutes == 0) return 0.0;
            int hours = (minutes + 59) / 60;
            return hours * ratePerHour();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vehicle v)) return false;
            return Objects.equals(regNo, v.regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }
    }

    public static class TwoWheeler extends Vehicle {
        public TwoWheeler(String regNo) { super(regNo); }
        @Override public double ratePerHour() { return 10.0; }
    }

    public static class Car extends Vehicle {
        public Car(String regNo) { super(regNo); }
        @Override public double ratePerHour() { return 30.0; }
    }

    public static class ElectricCar extends Car implements Chargeable {
        public ElectricCar(String regNo) { super(regNo); }
        @Override public double chargingFee(int kWh) { return kWh * 12.0; }
    }

    // --- 2. TICKET MODEL ---

    public static class ParkingTicket {
        private static int counter = 1000;

        private final String ticketId;
        private final Vehicle vehicle;
        private final UserType userType;
        private final int floor;
        private final int slot;
        private final LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private int durationMinutes;
        private int kWhConsumed;
        private double totalFee;
        private boolean isSettled;

        public ParkingTicket(Vehicle vehicle, UserType userType, int floor, int slot, LocalDateTime entryTime) {
            this.ticketId = "TKT-" + (++counter);
            this.vehicle = vehicle;
            this.userType = userType;
            this.floor = floor;
            this.slot = slot;
            this.entryTime = entryTime;
            this.isSettled = false;
        }

        public void settle(LocalDateTime exitTime, int kWh) {
            this.exitTime = exitTime;
            this.durationMinutes = (int) java.time.Duration.between(entryTime, exitTime).toMinutes();
            this.kWhConsumed = kWh;

            // Business rule: Staff and Students with monthly passes park free of parking duration charge
            double baseParkingFee = 0.0;
            if (userType == UserType.VISITOR) {
                baseParkingFee = vehicle.calculateFee(durationMinutes);
            }

            double chargingCost = 0.0;
            if (vehicle instanceof Chargeable c && kWh > 0) {
                chargingCost = c.chargingFee(kWh);
            }

            this.totalFee = baseParkingFee + chargingCost;
            this.isSettled = true;
        }

        // Getters
        public String getTicketId() { return ticketId; }
        public Vehicle getVehicle() { return vehicle; }
        public UserType getUserType() { return userType; }
        public int getFloor() { return floor; }
        public int getSlot() { return slot; }
        public LocalDateTime getEntryTime() { return entryTime; }
        public LocalDateTime getExitTime() { return exitTime; }
        public int getDurationMinutes() { return durationMinutes; }
        public double getTotalFee() { return totalFee; }
        public boolean isSettled() { return isSettled; }
    }

    // --- 3. FACILITY ENGINE ---

    public static class SmartParkingFacility {
        private final String[][] slots; // slots[floor][slot] = vehicleRegNo or null
        private final Map<String, ParkingTicket> activeTicketsMap; // RegNo -> Ticket
        private final List<ParkingTicket> activeTicketsList;
        private final ParkingTicket[] activeTicketsArray; // Fixed array for benchmarking
        private int arrayCount = 0;
        private double dailyCollection = 0.0;

        public SmartParkingFacility(int[] slotsPerFloor) {
            int total = 0;
            slots = new String[slotsPerFloor.length][];
            for (int f = 0; f < slotsPerFloor.length; f++) {
                slots[f] = new String[slotsPerFloor[f]];
                total += slotsPerFloor[f];
            }
            activeTicketsMap = new HashMap<>();
            activeTicketsList = new ArrayList<>();
            activeTicketsArray = new ParkingTicket[total + 1000];
        }

        public synchronized ParkingTicket parkVehicle(Vehicle vehicle, UserType userType, LocalDateTime entryTime) {
            if (vehicle == null) throw new IllegalArgumentException("Vehicle cannot be null");
            if (activeTicketsMap.containsKey(vehicle.getRegNo())) {
                throw new IllegalStateException("Vehicle " + vehicle.getRegNo() + " is already parked in facility!");
            }

            // Find first free slot
            for (int f = 0; f < slots.length; f++) {
                for (int s = 0; s < slots[f].length; s++) {
                    if (slots[f][s] == null) {
                        slots[f][s] = vehicle.getRegNo();
                        ParkingTicket ticket = new ParkingTicket(vehicle, userType, f, s, entryTime);
                        activeTicketsMap.put(vehicle.getRegNo(), ticket);
                        activeTicketsList.add(ticket);
                        activeTicketsArray[arrayCount++] = ticket;
                        return ticket;
                    }
                }
            }
            return null; // Facility full
        }

        public synchronized ParkingTicket vacateVehicle(String regNo, LocalDateTime exitTime, int kWh) {
            ParkingTicket ticket = activeTicketsMap.get(regNo);
            if (ticket == null) return null;

            // Release slot
            slots[ticket.getFloor()][ticket.getSlot()] = null;

            // Settle ticket
            ticket.settle(exitTime, kWh);
            dailyCollection += ticket.getTotalFee();

            // Remove from active structures
            activeTicketsMap.remove(regNo);
            activeTicketsList.remove(ticket);

            return ticket;
        }

        public double getDailyCollection() { return dailyCollection; }

        public int getFreeSlotCount() {
            int free = 0;
            for (String[] floor : slots) {
                for (String s : floor) {
                    if (s == null) free++;
                }
            }
            return free;
        }

        // Benchmark lookup methods
        public ParkingTicket findTicketByArray(String regNo) {
            for (int i = 0; i < arrayCount; i++) {
                if (activeTicketsArray[i] != null && activeTicketsArray[i].getVehicle().getRegNo().equals(regNo)) {
                    return activeTicketsArray[i];
                }
            }
            return null;
        }

        public ParkingTicket findTicketByList(String regNo) {
            for (ParkingTicket t : activeTicketsList) {
                if (t.getVehicle().getRegNo().equals(regNo)) return t;
            }
            return null;
        }

        public ParkingTicket findTicketByMap(String regNo) {
            return activeTicketsMap.get(regNo);
        }
    }

    // --- 4. VERIFICATION TEST SUITE ---

    public static void main(String[] args) {
        System.out.println("===================================================================");
        System.out.println("   TASK 11: FULL CAMPUS SMART-PARKING SYSTEM TEST & DEFENCE");
        System.out.println("===================================================================\n");

        SmartParkingFacility facility = new SmartParkingFacility(new int[]{2, 2}); // 4 total slots
        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 19, 8, 0);

        System.out.printf("%-6s | %-32s | %-16s | %-16s | %s%n",
                "TC_ID", "Test Scenario Description", "Expected Result", "Actual Result", "Status");
        System.out.println("-".repeat(88));

        // Test 1: Normal entry & slot allocation
        ParkingTicket t1 = facility.parkVehicle(new Car("TN01-1111"), UserType.VISITOR, baseTime);
        boolean pass1 = (t1 != null && t1.getFloor() == 0 && t1.getSlot() == 0);
        printRow("TC-01", "Normal Entry: Car in [0,0]", "Slot [0,0]",
                t1 != null ? String.format("Slot [%d,%d]", t1.getFloor(), t1.getSlot()) : "null", pass1);

        // Test 2: Normal entry of EV and Two-Wheeler
        ParkingTicket t2 = facility.parkVehicle(new TwoWheeler("TN02-2222"), UserType.STUDENT_PASS, baseTime);
        ParkingTicket t3 = facility.parkVehicle(new ElectricCar("TN03-3333"), UserType.VISITOR, baseTime);
        boolean pass2 = (t2 != null && t3 != null);
        printRow("TC-02", "Multi-type Entry: Bike & EV", "Allocated", "Allocated", pass2);

        // Test 3: Last free slot boundary
        ParkingTicket t4 = facility.parkVehicle(new Car("TN04-4444"), UserType.VISITOR, baseTime);
        boolean pass3 = (t4 != null && facility.getFreeSlotCount() == 0);
        printRow("TC-03", "Boundary: Last Free Slot", "Slot Allocated (0 free)",
                String.format("Slot [%d,%d] (%d free)", t4.getFloor(), t4.getSlot(), facility.getFreeSlotCount()), pass3);

        // Test 4: Facility full rejection
        ParkingTicket tFull = facility.parkVehicle(new Car("TN05-5555"), UserType.VISITOR, baseTime);
        boolean pass4 = (tFull == null);
        printRow("TC-04", "Boundary: Facility Full Rejection", "null (Rejected)",
                String.valueOf(tFull), pass4);

        // Test 5: Invalid case: Duplicate registration entry
        boolean duplicateCaught = false;
        try {
            facility.parkVehicle(new Car("TN01-1111"), UserType.VISITOR, baseTime);
        } catch (IllegalStateException ex) {
            duplicateCaught = true;
        }
        printRow("TC-05", "Invalid Case: Duplicate RegNo", "Exception Thrown",
                duplicateCaught ? "IllegalStateException" : "Allowed", duplicateCaught);

        // Test 6: Boundary case: Zero-minute parking
        // Settle t1 immediately with 0 duration
        ParkingTicket settledT1 = facility.vacateVehicle("TN01-1111", baseTime, 0);
        boolean pass6 = (settledT1 != null && settledT1.getTotalFee() == 0.0);
        printRow("TC-06", "Boundary: Zero-minute Stay", "Fee = Rs 0.0",
                "Fee = Rs " + (settledT1 != null ? settledT1.getTotalFee() : "err"), pass6);

        // Test 7: Fee-Slab boundary: exactly 60 minutes vs 61 minutes
        Vehicle testCarA = new Car("KA01-60M");
        Vehicle testCarB = new Car("KA02-61M");
        double fee60 = testCarA.calculateFee(60);  // 1 hr -> Rs 30.0
        double fee61 = testCarB.calculateFee(61);  // 2 hrs -> Rs 60.0
        boolean pass7 = (fee60 == 30.0 && fee61 == 60.0);
        printRow("TC-07", "Boundary: 60 min vs 61 min", "Rs 30.0 / Rs 60.0",
                String.format("Rs %.1f / Rs %.1f", fee60, fee61), pass7);

        // Test 8: Small Reconciliation Scenario
        System.out.println("\n-------------------------------------------------------------------");
        System.out.println("   HAND-CALCULATED RECONCILIATION SCENARIO");
        System.out.println("-------------------------------------------------------------------");
        SmartParkingFacility reconFacility = new SmartParkingFacility(new int[]{5});
        LocalDateTime t0 = LocalDateTime.of(2026, 11, 19, 9, 0);

        // V1: Visitor Car, 120 mins (2 hrs @ 30) = Rs 60.0
        reconFacility.parkVehicle(new Car("RECON-CAR"), UserType.VISITOR, t0);
        // V2: Student TwoWheeler with Pass, 180 mins = Rs 0.0
        reconFacility.parkVehicle(new TwoWheeler("RECON-BIKE"), UserType.STUDENT_PASS, t0);
        // V3: Visitor ElectricCar, 90 mins (2 hrs @ 30 = 60) + 10 kWh @ 12 = 120 -> Rs 180.0
        reconFacility.parkVehicle(new ElectricCar("RECON-EV"), UserType.VISITOR, t0);
        // V4: Visitor TwoWheeler, 45 mins (1 hr @ 10) = Rs 10.0
        reconFacility.parkVehicle(new TwoWheeler("RECON-TW-VIS"), UserType.VISITOR, t0);

        // Exit operations
        reconFacility.vacateVehicle("RECON-CAR", t0.plusMinutes(120), 0);
        reconFacility.vacateVehicle("RECON-BIKE", t0.plusMinutes(180), 0);
        reconFacility.vacateVehicle("RECON-EV", t0.plusMinutes(90), 10);
        reconFacility.vacateVehicle("RECON-TW-VIS", t0.plusMinutes(45), 0);

        double expectedHandCollection = 60.0 + 0.0 + 180.0 + 10.0; // 250.0
        double actualCollection = reconFacility.getDailyCollection();
        boolean reconMatch = Math.abs(expectedHandCollection - actualCollection) < 0.001;

        System.out.printf("Hand-Calculated Revenue: Rs %.2f%n", expectedHandCollection);
        System.out.printf("Actual Program Revenue:  Rs %.2f%n", actualCollection);
        System.out.printf("Reconciliation Status:   %s%n", reconMatch ? "PASSED (100%% MATCH)" : "FAILED");

        // 5. Data Structure Exit-Processing Benchmark
        System.out.println("\n-------------------------------------------------------------------");
        System.out.println("   EXIT SEARCH COMPLEXITY BENCHMARK (Array vs List vs HashMap)");
        System.out.println("-------------------------------------------------------------------");
        int benchmarkN = 5000;
        int[] largeFloors = new int[50];
        Arrays.fill(largeFloors, 150); // 7500 slots
        SmartParkingFacility benchFacility = new SmartParkingFacility(largeFloors);

        for (int i = 0; i < benchmarkN; i++) {
            benchFacility.parkVehicle(new Car("BENCH-" + i), UserType.VISITOR, baseTime);
        }

        String searchTarget = "BENCH-" + (benchmarkN - 1); // Worst-case last element

        // Benchmark Array Linear Search
        long start = System.nanoTime();
        ParkingTicket fromArray = benchFacility.findTicketByArray(searchTarget);
        long timeArray = System.nanoTime() - start;

        // Benchmark ArrayList Search
        start = System.nanoTime();
        ParkingTicket fromList = benchFacility.findTicketByList(searchTarget);
        long timeList = System.nanoTime() - start;

        // Benchmark HashMap Search
        start = System.nanoTime();
        ParkingTicket fromMap = benchFacility.findTicketByMap(searchTarget);
        long timeMap = System.nanoTime() - start;

        System.out.printf("Dataset: %d active parked vehicles%n", benchmarkN);
        System.out.printf("1. Array Linear Scan [O(N)]    : %8d ns (Found: %s)%n", timeArray, fromArray != null);
        System.out.printf("2. ArrayList Scan    [O(N)]    : %8d ns (Found: %s)%n", timeList, fromList != null);
        System.out.printf("3. HashMap Hash Lookup [O(1)]  : %8d ns (Found: %s)%n", timeMap, fromMap != null);
        System.out.printf("Speedup (HashMap vs Array Scan): %.2fx faster%n", (double) timeArray / Math.max(1, timeMap));
    }

    private static void printRow(String id, String desc, String expected, String actual, boolean pass) {
        System.out.printf("%-6s | %-32s | %-16s | %-16s | %s%n",
                id, desc, expected, actual, pass ? "PASS" : "FAIL");
    }
}
