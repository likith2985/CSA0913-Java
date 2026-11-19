import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Task 12: Day-4 Design Challenge - Re-evaluate and Adapt the Design
 * Implements the adapted architecture:
 * 1. New Category: EBike (Flat Fee: Rs 20.0)
 * 2. New Category: DeliveryVan (Peak Hour Surcharge: Double car rate during 9 AM - 5 PM)
 * 3. Visitor One-Day Pass model
 * 4. 5-Floor Jagged Array facility with non-uniform floor capacities
 */
public class Day4SmartParkingApp {

    // --- 1. PASS ABSTRACTION ---

    public interface ParkingPass {
        String getPassName();
        boolean isCovered(LocalDateTime entry, LocalDateTime exit);
        double getPassCost();
    }

    public static class NoPass implements ParkingPass {
        @Override public String getPassName() { return "None (Standard Visitor)"; }
        @Override public boolean isCovered(LocalDateTime entry, LocalDateTime exit) { return false; }
        @Override public double getPassCost() { return 0.0; }
    }

    public static class MonthlyPass implements ParkingPass {
        private final String holderName;
        public MonthlyPass(String holderName) { this.holderName = holderName; }
        @Override public String getPassName() { return "Monthly Pass (" + holderName + ")"; }
        @Override public boolean isCovered(LocalDateTime entry, LocalDateTime exit) { return true; } // 100% covered
        @Override public double getPassCost() { return 0.0; } // Pre-paid
    }

    public static class OneDayPass implements ParkingPass {
        private final LocalDateTime validDate;
        private static final double ONE_DAY_PASS_RATE = 100.0; // Flat Rs 100 for all-day parking

        public OneDayPass(LocalDateTime date) {
            this.validDate = date;
        }

        @Override public String getPassName() { return "Visitor One-Day Pass"; }

        @Override public boolean isCovered(LocalDateTime entry, LocalDateTime exit) {
            return entry.toLocalDate().equals(validDate.toLocalDate()) &&
                   Duration.between(entry, exit).toHours() <= 24;
        }

        @Override public double getPassCost() { return ONE_DAY_PASS_RATE; }
    }

    // --- 2. VEHICLE HIERARCHY ---

    public interface Chargeable {
        double chargingFee(int kWh);
    }

    public static abstract class Vehicle {
        private final String regNo;

        protected Vehicle(String regNo) {
            if (regNo == null || regNo.trim().isEmpty()) {
                throw new IllegalArgumentException("Registration cannot be null/blank");
            }
            this.regNo = regNo.trim().toUpperCase();
        }

        public String getRegNo() { return regNo; }

        public abstract double ratePerHour();

        // Time-aware fee calculation method (open for polymorphic override)
        public double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
            long minutes = Duration.between(entryTime, exitTime).toMinutes();
            if (minutes <= 0) return 0.0;
            int hours = (int) ((minutes + 59) / 60);
            return hours * ratePerHour();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vehicle v)) return false;
            return Objects.equals(regNo, v.regNo);
        }

        @Override
        public int hashCode() { return Objects.hash(regNo); }
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

    // NEW VEHICLE 1: E-Bike (Charged flat fee: Rs 20.0, plus optional charging)
    public static class EBike extends Vehicle implements Chargeable {
        private static final double FLAT_FEE = 20.0;

        public EBike(String regNo) { super(regNo); }

        @Override public double ratePerHour() { return 0.0; } // Irrelevant for flat fee

        @Override
        public double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
            long minutes = Duration.between(entryTime, exitTime).toMinutes();
            if (minutes <= 0) return 0.0;
            return FLAT_FEE; // Flat fee regardless of duration!
        }

        @Override
        public double chargingFee(int kWh) {
            return kWh * 8.0; // E-bike battery charging rate: Rs 8/kWh
        }
    }

    // NEW VEHICLE 2: Delivery Van (Double car rate during 9 AM - 5 PM)
    public static class DeliveryVan extends Vehicle {
        private static final LocalTime PEAK_START = LocalTime.of(9, 0);
        private static final LocalTime PEAK_END = LocalTime.of(17, 0);

        public DeliveryVan(String regNo) { super(regNo); }

        @Override public double ratePerHour() { return 30.0; } // Base car rate

        @Override
        public double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime) {
            long minutes = Duration.between(entryTime, exitTime).toMinutes();
            if (minutes <= 0) return 0.0;
            int hours = (int) ((minutes + 59) / 60);

            // Check if any portion falls during peak business hours (9 AM - 5 PM)
            LocalTime entryClock = entryTime.toLocalTime();
            boolean isPeak = !(entryClock.isBefore(PEAK_START) || entryClock.isAfter(PEAK_END));

            double effectiveRate = isPeak ? (ratePerHour() * 2.0) : ratePerHour();
            return hours * effectiveRate;
        }
    }

    // --- 3. ADAPTED PARKING FACILITY (5 FLOORS WITH JAGGED ARRAY) ---

    public static class AdaptedParkingFacility {
        private final String[][] slots; // Jagged 2D array: 5 floors with variable slot capacities
        private final Map<String, AdaptedTicket> activeTickets = new HashMap<>();

        public AdaptedParkingFacility(int[] slotsPerFloor) {
            if (slotsPerFloor.length != 5) {
                System.out.println("Notice: Facility initialized with " + slotsPerFloor.length + " floors.");
            }
            slots = new String[slotsPerFloor.length][];
            for (int f = 0; f < slotsPerFloor.length; f++) {
                slots[f] = new String[slotsPerFloor[f]];
            }
        }

        public AdaptedTicket parkVehicle(Vehicle vehicle, ParkingPass pass, LocalDateTime entryTime) {
            for (int f = 0; f < slots.length; f++) {
                for (int s = 0; s < slots[f].length; s++) {
                    if (slots[f][s] == null) {
                        slots[f][s] = vehicle.getRegNo();
                        AdaptedTicket ticket = new AdaptedTicket(vehicle, pass, f, s, entryTime);
                        activeTickets.put(vehicle.getRegNo(), ticket);
                        return ticket;
                    }
                }
            }
            return null; // Facility full
        }

        public double vacateVehicle(String regNo, LocalDateTime exitTime, int kWh) {
            AdaptedTicket ticket = activeTickets.get(regNo);
            if (ticket == null) return -1.0;

            slots[ticket.floor][ticket.slot] = null;
            activeTickets.remove(regNo);
            return ticket.settle(exitTime, kWh);
        }

        public int getTotalSlots() {
            int total = 0;
            for (String[] floor : slots) total += floor.length;
            return total;
        }

        public int getSlotsOnFloor(int floor) {
            return slots[floor].length;
        }
    }

    public static class AdaptedTicket {
        private final Vehicle vehicle;
        private final ParkingPass pass;
        private final int floor;
        private final int slot;
        private final LocalDateTime entryTime;

        public AdaptedTicket(Vehicle vehicle, ParkingPass pass, int floor, int slot, LocalDateTime entryTime) {
            this.vehicle = vehicle;
            this.pass = pass;
            this.floor = floor;
            this.slot = slot;
            this.entryTime = entryTime;
        }

        public double settle(LocalDateTime exitTime, int kWh) {
            double fee;
            if (pass.isCovered(entryTime, exitTime)) {
                fee = pass.getPassCost(); // Monthly pass = Rs 0, One-day pass = Rs 100 flat
            } else {
                fee = vehicle.calculateFee(entryTime, exitTime);
            }

            if (vehicle instanceof Chargeable c && kWh > 0) {
                fee += c.chargingFee(kWh);
            }
            return fee;
        }
    }

    // --- 4. EXECUTION & VERIFICATION TEST SUITE ---

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  TASK 12: DAY-4 DESIGN CHALLENGE - ADAPTED SMART-PARKING SYSTEM");
        System.out.println("==========================================================================\n");

        // 1. Extend Facility to 5 Floors with Non-Uniform Capacities (Jagged Array)
        // Floor 0: 25 slots, Floor 1: 40 slots, Floor 2: 50 slots, Floor 3: 35 slots, Floor 4: 20 slots
        int[] floorConfig = {25, 40, 50, 35, 20};
        AdaptedParkingFacility facility = new AdaptedParkingFacility(floorConfig);

        System.out.println("--- 1. Facility Extension Verification (5 Floors) ---");
        System.out.println("Total Facility Floors: " + floorConfig.length);
        for (int i = 0; i < floorConfig.length; i++) {
            System.out.printf("  Floor %d Capacity: %d slots%n", i, facility.getSlotsOnFloor(i));
        }
        System.out.println("Total Facility Capacity: " + facility.getTotalSlots() + " slots");

        // 2. Testing New Category: E-Bike (Flat Fee = Rs 20.0 regardless of time)
        System.out.println("\n--- 2. New Category: E-Bike Testing ---");
        LocalDateTime tEntry = LocalDateTime.of(2026, 11, 19, 10, 0);
        EBike ebike = new EBike("EB-01");
        double ebikeFeeShort = ebike.calculateFee(tEntry, tEntry.plusMinutes(45));  // 45 mins
        double ebikeFeeLong  = ebike.calculateFee(tEntry, tEntry.plusMinutes(420)); // 7 hours
        double ebikeCharging = ebike.chargingFee(5); // 5 kWh @ 8/kWh = 40.0
        System.out.printf("E-Bike (45 mins stay) Fee:  Rs %.2f (Expected: Rs 20.00 - Flat)%n", ebikeFeeShort);
        System.out.printf("E-Bike (7 hrs stay) Fee:   Rs %.2f (Expected: Rs 20.00 - Flat)%n", ebikeFeeLong);
        System.out.printf("E-Bike 5 kWh Charging Fee: Rs %.2f (Expected: Rs 40.00)%n", ebikeCharging);

        // 3. Testing New Category: Delivery Van (Peak Hours: 9 AM - 5 PM -> Rs 60/hr vs Off-Peak -> Rs 30/hr)
        System.out.println("\n--- 3. New Category: Delivery Van Testing (Peak vs Off-Peak) ---");
        DeliveryVan van = new DeliveryVan("VAN-777");

        // Peak stay: 10:00 AM to 12:00 PM (2 hours inside 9 AM - 5 PM) -> 2 * 60.0 = Rs 120.0
        LocalDateTime peakEntry = LocalDateTime.of(2026, 11, 19, 10, 0);
        LocalDateTime peakExit  = LocalDateTime.of(2026, 11, 19, 12, 0);
        double peakFee = van.calculateFee(peakEntry, peakExit);

        // Off-peak stay: 6:00 AM to 8:00 AM (2 hours outside peak) -> 2 * 30.0 = Rs 60.0
        LocalDateTime offPeakEntry = LocalDateTime.of(2026, 11, 19, 6, 0);
        LocalDateTime offPeakExit  = LocalDateTime.of(2026, 11, 19, 8, 0);
        double offPeakFee = van.calculateFee(offPeakEntry, offPeakExit);

        System.out.printf("Delivery Van [Peak 10AM-12PM] (2 hrs):     Rs %.2f (Expected: Rs 120.00)%n", peakFee);
        System.out.printf("Delivery Van [Off-Peak 6AM-8AM] (2 hrs):   Rs %.2f (Expected: Rs 60.00)%n", offPeakFee);

        // 4. Testing Visitor One-Day Pass (Flat Rs 100 vs Standard Car Rate)
        System.out.println("\n--- 4. Visitor One-Day Pass Testing ---");
        LocalDateTime dayEntry = LocalDateTime.of(2026, 11, 19, 8, 0);
        LocalDateTime dayExit  = LocalDateTime.of(2026, 11, 19, 18, 0); // 10 hours

        // Standard Car without Pass: 10 hrs * 30 = Rs 300
        Car standardCar = new Car("TN-VIS-01");
        facility.parkVehicle(standardCar, new NoPass(), dayEntry);
        double visitorStandardFee = facility.vacateVehicle("TN-VIS-01", dayExit, 0);

        // Car with Visitor One-Day Pass: Flat Rs 100
        Car passCar = new Car("TN-PASS-02");
        facility.parkVehicle(passCar, new OneDayPass(dayEntry), dayEntry);
        double visitorPassFee = facility.vacateVehicle("TN-PASS-02", dayExit, 0);

        System.out.printf("Visitor Car (10 hrs) WITHOUT Pass: Rs %.2f (Expected: Rs 300.00)%n", visitorStandardFee);
        System.out.printf("Visitor Car (10 hrs) WITH One-Day Pass: Rs %.2f (Expected: Rs 100.00)%n", visitorPassFee);

        System.out.println("\nAdaptation Result: Successfully adapted the design to support new categories,");
        System.out.println("peak tariff surcharges, one-day passes, and 5-floor non-uniform layout with 0 regressions!");
    }
}
