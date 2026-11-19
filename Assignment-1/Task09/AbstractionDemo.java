/**
 * Task 9: Introduce Abstraction
 * Demonstrates:
 * 1. Abstract classes for core state and identity (Vehicle)
 * 2. Interfaces for cross-cutting capabilities (Chargeable, Payable)
 * 3. Default and static methods in interfaces
 * 4. Multiple interface implementation
 */
public class AbstractionDemo {

    // Capability 1: Chargeable interface
    public interface Chargeable {
        double CHARGING_RATE_PER_KWH = 12.0; // public static final

        double chargingFee(int kWh);

        // Default method: provides a default implementation without breaking implementing classes
        default double estimateChargingMinutes(int kWh) {
            // Assuming average 7 kW AC charger -> ~8.5 minutes per kWh
            return kWh * 8.5;
        }

        // Static utility method in interface
        static double getGreenTariffDiscount() {
            return 5.0; // 5% green discount
        }
    }

    // Capability 2: Payable interface
    public interface Payable {
        void processPayment(double amount);
    }

    // Abstract Superclass: Holds state (regNo) and invariant template logic
    public static abstract class Vehicle {
        private final String regNo;

        protected Vehicle(String regNo) {
            this.regNo = regNo;
        }

        public final String getRegNo() { return regNo; }

        // Abstract method: must be defined by concrete subclasses
        public abstract double ratePerHour();

        // Concrete template method
        public double calculateFee(int minutes) {
            int hours = (minutes + 59) / 60;
            return hours * ratePerHour();
        }
    }

    // Concrete class with single inheritance and multiple interface capabilities
    public static class ElectricCar extends Vehicle implements Chargeable, Payable {

        public ElectricCar(String regNo) {
            super(regNo);
        }

        @Override
        public double ratePerHour() {
            return 30.0;
        }

        @Override
        public double chargingFee(int kWh) {
            return kWh * CHARGING_RATE_PER_KWH;
        }

        @Override
        public void processPayment(double amount) {
            System.out.printf("  [Payment Gateway] Processing Rs %.2f for EV %s via Campus UPI/Card%n",
                    amount, getRegNo());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 9: ABSTRACTION WITH ABSTRACT CLASSES & INTERFACES ===");

        ElectricCar ev = new ElectricCar("TN-09-EV-9999");
        int parkedMinutes = 90; // 2 hours -> 2 * 30 = 60.0
        int energyKWh = 15;

        double parkingFee = ev.calculateFee(parkedMinutes);
        double chargeFee = ev.chargingFee(energyKWh);
        double totalFee = parkingFee + chargeFee;

        System.out.println("Vehicle Registration: " + ev.getRegNo());
        System.out.println("Hourly Parking Rate: Rs " + ev.ratePerHour());
        System.out.println("Parking Fee (90 mins): Rs " + parkingFee);
        System.out.println("Charging Fee (15 kWh): Rs " + chargeFee);
        System.out.println("Estimated Charging Time (via default method): "
                + ev.estimateChargingMinutes(energyKWh) + " minutes");
        System.out.println("Green Discount Applicable (via static method): "
                + Chargeable.getGreenTariffDiscount() + "%");

        System.out.println("\nInvoking multiple interface capability (Payable):");
        ev.processPayment(totalFee);
    }
}
