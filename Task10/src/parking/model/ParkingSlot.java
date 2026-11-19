package parking.model;

/**
 * Encapsulated domain model representing a parking slot.
 */
public class ParkingSlot {
    private final int floor;
    private final int slotNumber;
    private boolean occupied;
    private String vehicleRegNo;

    public ParkingSlot(int floor, int slotNumber) {
        if (floor < 0) {
            throw new IllegalArgumentException("Floor index cannot be negative: " + floor);
        }
        if (slotNumber < 0) {
            throw new IllegalArgumentException("Slot number cannot be negative: " + slotNumber);
        }
        this.floor = floor;
        this.slotNumber = slotNumber;
        this.occupied = false;
        this.vehicleRegNo = null;
    }

    public synchronized void occupy(String vehicleRegNo) {
        if (this.occupied) {
            throw new IllegalStateException("Slot [" + floor + "," + slotNumber + "] is already occupied!");
        }
        if (vehicleRegNo == null || vehicleRegNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle registration number cannot be blank");
        }
        this.vehicleRegNo = vehicleRegNo.trim();
        this.occupied = true;
    }

    public synchronized void vacate() {
        if (!this.occupied) {
            throw new IllegalStateException("Slot [" + floor + "," + slotNumber + "] is already vacant!");
        }
        this.occupied = false;
        this.vehicleRegNo = null;
    }

    // Accessors
    public int getFloor() { return floor; }
    public int getSlotNumber() { return slotNumber; }
    public boolean isOccupied() { return occupied; }
    public String getVehicleRegNo() { return vehicleRegNo; }

    @Override
    public String toString() {
        return String.format("Slot[F%d-S%d, occupied=%s, vehicle=%s]",
                floor, slotNumber, occupied, vehicleRegNo);
    }
}
