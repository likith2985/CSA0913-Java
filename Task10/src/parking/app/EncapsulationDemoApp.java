package parking.app;

import parking.model.Car;
import parking.model.ParkingSlot;
import parking.model.Vehicle;
import parking.service.ParkingService;

/**
 * Task 10: Enforce Encapsulation through Packages and Access Modifiers
 * Demonstrates:
 * 1. Package segregation (model, service, app)
 * 2. Access control (private state, protected members, public contracts)
 * 3. Validation defenses in setters and constructors
 * 4. Wrapper classes, autoboxing and the Integer == Integer cache trap
 */
public class EncapsulationDemoApp {

    public static void main(String[] args) {
        System.out.println("=== TASK 10: PACKAGES, ACCESS MODIFIERS & ENCAPSULATION ===");

        // 1. Package collaboration
        ParkingService service = new ParkingService(new int[]{2, 2});
        Vehicle car1 = new Car("KA01-1111");

        ParkingSlot allocated = service.parkVehicle(car1);
        System.out.println("Successfully allocated slot: " + allocated);

        // 2. Data Validation Defense
        System.out.println("\n--- Testing Data Validation & Defensive Invariants ---");
        try {
            allocated.occupy("SOME-OTHER-CAR"); // Should fail because slot is already occupied
        } catch (IllegalStateException e) {
            System.out.println("Encapsulation Protected: Caught expected exception -> " + e.getMessage());
        }

        try {
            car1.calculateFee(-45); // Negative duration should be rejected
        } catch (IllegalArgumentException e) {
            System.out.println("Encapsulation Protected: Caught expected exception -> " + e.getMessage());
        }

        // 3. Autoboxing & Wrapper Class Trap Demonstration
        System.out.println("\n--- Autoboxing & Wrapper Class Trap (Integer == Integer) ---");
        Integer cachedA = 100;
        Integer cachedB = 100;
        System.out.printf("Comparing Integer(100) == Integer(100): %s (Cached [-128..127])%n",
                (cachedA == cachedB));

        Integer nonCachedA = 300;
        Integer nonCachedB = 300;
        System.out.printf("Comparing Integer(300) == Integer(300): %s (FAIL: Outside cache range!)%n",
                (nonCachedA == nonCachedB));
        System.out.printf("Comparing with .equals(): %s (ALWAYS CORRECT)%n",
                nonCachedA.equals(nonCachedB));
    }
}
