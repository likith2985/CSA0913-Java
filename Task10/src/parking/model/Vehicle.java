package parking.model;

import java.util.Objects;

/**
 * Base abstract class demonstrating protected and package-private access modifiers.
 */
public abstract class Vehicle {
    private final String regNo;

    // protected constructor: only accessible to subclasses or classes within parking.model
    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty");
        }
        this.regNo = regNo.trim().toUpperCase();
    }

    public final String getRegNo() { return regNo; }

    // protected abstract method: accessible to subclasses
    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + minutes);
        }
        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle vehicle)) return false;
        return Objects.equals(regNo, vehicle.regNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }
}
