import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Task 7: Override and Specialise Behaviour
 * Demonstrates:
 * 1. Method overriding with @Override
 * 2. Proper implementation of equals(), hashCode(), and toString()
 * 3. Justified use of the 'final' keyword (fields, template method, classes)
 * 4. Comparison between Polymorphic Overriding vs Monolithic switch(type)
 */
public class MethodOverridingDemo {

    public static abstract class Vehicle {
        // final field: immutable registration number
        private final String regNo;

        public Vehicle(String regNo) {
            this.regNo = Objects.requireNonNull(regNo, "Registration number cannot be null");
        }

        public final String getRegNo() { return regNo; }

        // Abstract method to be specialized by subclasses
        protected abstract double ratePerHour();

        // final template method: enforces ceiling billing policy uniformly; subclasses cannot break this rule!
        public final double calculateFee(int minutes) {
            int hours = (minutes + 59) / 60;
            return hours * ratePerHour();
        }

        // equals() based strictly on vehicle registration identity
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vehicle vehicle)) return false;
            return Objects.equals(regNo, vehicle.regNo);
        }

        // hashCode() compliant with equals contract
        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }

        @Override
        public String toString() {
            return String.format("%s[regNo=%s, rate=Rs %.2f/hr]", getClass().getSimpleName(), regNo, ratePerHour());
        }
    }

    public static class TwoWheeler extends Vehicle {
        public TwoWheeler(String regNo) { super(regNo); }

        @Override
        protected double ratePerHour() {
            return 10.0;
        }
    }

    public static class Car extends Vehicle {
        public Car(String regNo) { super(regNo); }

        @Override
        protected double ratePerHour() {
            return 30.0;
        }
    }

    // Monolithic switch-based alternative for comparison
    public static class SwitchBasedFeeCalculator {
        public static double calculateFee(String type, int minutes) {
            int hours = (minutes + 59) / 60;
            return switch (type) {
                case "TwoWheeler" -> hours * 10.0;
                case "Car" -> hours * 30.0;
                case "ElectricCar" -> hours * 30.0;
                default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
            };
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 7: METHOD OVERRIDING & EQUALITY CONTRACT ===");

        Vehicle v1 = new Car("KA01-1234");
        Vehicle v2 = new Car("KA01-1234"); // Duplicate registration instance
        Vehicle v3 = new TwoWheeler("TN02-5678");

        System.out.println("\n--- 1. toString() Specialization ---");
        System.out.println("v1: " + v1);
        System.out.println("v2: " + v2);
        System.out.println("v3: " + v3);

        System.out.println("\n--- 2. equals() and hashCode() Contract ---");
        System.out.println("v1.equals(v2): " + v1.equals(v2) + " (Expected: true)");
        System.out.println("v1.hashCode() == v2.hashCode(): " + (v1.hashCode() == v2.hashCode()) + " (Expected: true)");

        Set<Vehicle> vehicleSet = new HashSet<>();
        vehicleSet.add(v1);
        vehicleSet.add(v2); // Should not add duplicate
        vehicleSet.add(v3);
        System.out.println("HashSet size after adding duplicate regNo: " + vehicleSet.size() + " (Expected: 2)");

        System.out.println("\n--- 3. Fee Calculation Comparison ---");
        int testMinutes = 125; // 3 billable hours
        System.out.println("Polymorphic Car Fee (125 mins): Rs " + v1.calculateFee(testMinutes));
        System.out.println("Switch-based Car Fee (125 mins): Rs " + SwitchBasedFeeCalculator.calculateFee("Car", testMinutes));
    }
}
