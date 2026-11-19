import java.time.LocalDateTime;

/**
 * Task 1: Problem Analysis and Object Identification
 * Demonstrating the trade-offs between Parallel Arrays and Object-Oriented Modeling.
 */
public class ParallelArraysVsObjectDemo {

    // --- Object-Oriented Design ---
    public static class VehicleRecord {
        private final String regNo;
        private final String vehicleType;
        private final LocalDateTime entryTime;
        private final boolean isEv; // Easily added attribute

        public VehicleRecord(String regNo, String vehicleType, LocalDateTime entryTime, boolean isEv) {
            this.regNo = regNo;
            this.vehicleType = vehicleType;
            this.entryTime = entryTime;
            this.isEv = isEv;
        }

        public String getRegNo() { return regNo; }
        public String getVehicleType() { return vehicleType; }
        public LocalDateTime getEntryTime() { return entryTime; }
        public boolean isEv() { return isEv; }

        @Override
        public String toString() {
            return String.format("VehicleRecord[RegNo=%s, Type=%s, EntryTime=%s, EV=%s]",
                    regNo, vehicleType, entryTime, isEv);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 1: OBJECT-ORIENTED MODELING VS PARALLEL ARRAYS ===");

        int capacity = 3;

        // 1. Parallel Arrays Approach
        System.out.println("\n--- Approach 1: Parallel Arrays ---");
        String[] regNos = new String[capacity];
        String[] types = new String[capacity];
        String[] entryTimes = new String[capacity];

        // Populate Parallel Arrays
        regNos[0] = "KA-01-AB-1234"; types[0] = "Car"; entryTimes[0] = "2026-11-19T09:00:00";
        regNos[1] = "TN-02-CD-5678"; types[1] = "TwoWheeler"; entryTimes[1] = "2026-11-19T09:15:00";
        regNos[2] = "DL-03-EF-9012"; types[2] = "ElectricCar"; entryTimes[2] = "2026-11-19T09:30:00";

        // Search by Registration Number in Parallel Arrays
        String targetReg = "TN-02-CD-5678";
        int foundIndex = -1;
        for (int i = 0; i < capacity; i++) {
            if (regNos[i] != null && regNos[i].equals(targetReg)) {
                foundIndex = i;
                break;
            }
        }
        if (foundIndex != -1) {
            System.out.printf("Found in parallel arrays at index %d: RegNo=%s, Type=%s, Time=%s%n",
                    foundIndex, regNos[foundIndex], types[foundIndex], entryTimes[foundIndex]);
        }

        // 2. Object-Oriented Approach
        System.out.println("\n--- Approach 2: Object-Oriented Design ---");
        VehicleRecord[] vehicles = new VehicleRecord[capacity];
        vehicles[0] = new VehicleRecord("KA-01-AB-1234", "Car", LocalDateTime.parse("2026-11-19T09:00:00"), false);
        vehicles[1] = new VehicleRecord("TN-02-CD-5678", "TwoWheeler", LocalDateTime.parse("2026-11-19T09:15:00"), false);
        vehicles[2] = new VehicleRecord("DL-03-EF-9012", "ElectricCar", LocalDateTime.parse("2026-11-19T09:30:00"), true);

        // Search by Registration Number in Object Array
        VehicleRecord foundVehicle = null;
        for (VehicleRecord v : vehicles) {
            if (v != null && v.getRegNo().equals(targetReg)) {
                foundVehicle = v;
                break;
            }
        }
        if (foundVehicle != null) {
            System.out.println("Found in OO model: " + foundVehicle);
        }

        System.out.println("\nComparison Summary:");
        System.out.println("1. Cohesion: OO encapsulates all attributes in one cohesive entity, preventing index misalignment.");
        System.out.println("2. Extensibility: Adding attribute 'isEv' only updates VehicleRecord class, whereas Parallel Arrays requires introducing a whole new array bool[] isEv across all methods.");
        System.out.println("3. Type-safety: Parallel arrays rely on loose string parsing; OO enforces strong typing (LocalDateTime, boolean).");
    }
}
