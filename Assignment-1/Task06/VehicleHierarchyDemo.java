/**
 * Task 6: Design the Inheritance Hierarchy
 * Demonstrates:
 * 1. Superclass and Subclass relationships (Hierarchical and Multilevel inheritance)
 * 2. Usage of 'super' for constructor chaining and method invocation
 * 3. Tracing constructor execution order during object instantiation
 */
public class VehicleHierarchyDemo {

    // Base Superclass
    public static abstract class Vehicle {
        private final String regNo;

        public Vehicle(String regNo) {
            System.out.println("  -> [1] Vehicle constructor executing: regNo = " + regNo);
            this.regNo = regNo;
        }

        public String getRegNo() { return regNo; }
        public abstract double ratePerHour();

        public double calculateFee(int minutes) {
            int hours = (minutes + 59) / 60;
            return hours * ratePerHour();
        }
    }

    // Subclass 1: TwoWheeler (Hierarchical branch)
    public static class TwoWheeler extends Vehicle {
        public TwoWheeler(String regNo) {
            super(regNo);
            System.out.println("  -> [2] TwoWheeler constructor executing");
        }

        @Override
        public double ratePerHour() { return 10.0; }
    }

    // Subclass 2: Car (Hierarchical branch)
    public static class Car extends Vehicle {
        public Car(String regNo) {
            super(regNo);
            System.out.println("  -> [2] Car constructor executing");
        }

        @Override
        public double ratePerHour() { return 30.0; }
    }

    // Subclass 3: ElectricCar (Multilevel inheritance: Vehicle -> Car -> ElectricCar)
    public static class ElectricCar extends Car {
        private final double batteryCapacityKWh;

        public ElectricCar(String regNo, double batteryCapacityKWh) {
            super(regNo);
            System.out.println("  -> [3] ElectricCar constructor executing: battery = " + batteryCapacityKWh + " kWh");
            this.batteryCapacityKWh = batteryCapacityKWh;
        }

        public double chargingFee(int kWh) {
            return kWh * 12.0;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 6: INHERITANCE HIERARCHY & CONSTRUCTOR TRACE ===");

        System.out.println("\nInstantiating TwoWheeler('TN-01-BIKE-1'):");
        TwoWheeler bike = new TwoWheeler("TN-01-BIKE-1");

        System.out.println("\nInstantiating Car('TN-02-CAR-2'):");
        Car car = new Car("TN-02-CAR-2");

        System.out.println("\nInstantiating ElectricCar('TN-03-EVCAR-3'):");
        ElectricCar ev = new ElectricCar("TN-03-EVCAR-3", 60.0);

        System.out.println("\nVerifying Inheritance & Polymorphic Rates:");
        System.out.println("Bike (" + bike.getRegNo() + ") Rate: Rs " + bike.ratePerHour() + "/hr");
        System.out.println("Car (" + car.getRegNo() + ") Rate: Rs " + car.ratePerHour() + "/hr");
        System.out.println("EV Car (" + ev.getRegNo() + ") Rate: Rs " + ev.ratePerHour() + "/hr, Charging 10 kWh: Rs " + ev.chargingFee(10));
    }
}
