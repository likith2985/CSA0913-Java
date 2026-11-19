package parking.service;

import parking.model.ParkingSlot;
import parking.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Service orchestrating parking operations across floors.
 */
public class ParkingService {
    private final ParkingSlot[][] layout;

    public ParkingService(int[] slotsPerFloor) {
        if (slotsPerFloor == null || slotsPerFloor.length == 0) {
            throw new IllegalArgumentException("Facility must have at least one floor");
        }
        layout = new ParkingSlot[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            layout[f] = new ParkingSlot[slotsPerFloor[f]];
            for (int s = 0; s < slotsPerFloor[f]; s++) {
                layout[f][s] = new ParkingSlot(f, s);
            }
        }
    }

    public synchronized ParkingSlot parkVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        for (ParkingSlot[] floor : layout) {
            for (ParkingSlot slot : floor) {
                if (!slot.isOccupied()) {
                    slot.occupy(vehicle.getRegNo());
                    return slot;
                }
            }
        }
        return null; // Facility full
    }

    public synchronized boolean vacateSlot(int floor, int slot) {
        if (floor < 0 || floor >= layout.length || slot < 0 || slot >= layout[floor].length) {
            return false;
        }
        ParkingSlot target = layout[floor][slot];
        if (!target.isOccupied()) {
            return false;
        }
        target.vacate();
        return true;
    }

    public ParkingSlot getSlot(int floor, int slot) {
        if (floor >= 0 && floor < layout.length && slot >= 0 && slot < layout[floor].length) {
            return layout[floor][slot];
        }
        return null;
    }
}
