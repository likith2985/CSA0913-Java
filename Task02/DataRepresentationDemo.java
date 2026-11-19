/**
 * Task 2: Choose Data Representation
 * Demonstrates:
 * 1. Data type precision risks (Integer division bug vs Correct Ceiling Arithmetic)
 * 2. Array representations: Jagged 2D Array vs Flattened 1D Array vs ArrayList
 */
import java.util.ArrayList;
import java.util.Arrays;

public class DataRepresentationDemo {

    public static void main(String[] args) {
        System.out.println("=== TASK 2: DATA REPRESENTATION & TYPE CONVERSION RISKS ===");

        // 1. Integer Division Bug vs Integer Ceiling
        System.out.println("\n--- 1. Precision & Integer Division Pitfall ---");
        int parkedMinutes = 65; // 1 hour and 5 minutes (rule: every started hour charged in full -> 2 hours)
        double ratePerHour = 30.0;

        // Truncation Bug with standard integer division
        int buggyHours = parkedMinutes / 60; // 65 / 60 = 1 (Truncates fractional part!)
        double buggyFee = buggyHours * ratePerHour; // 1 * 30.0 = Rs 30.0 (LOSS OF REVENUE!)

        // Correct calculation using integer ceiling arithmetic: (minutes + 59) / 60
        int correctHoursIntMath = (parkedMinutes + 59) / 60; // (65 + 59) / 60 = 124 / 60 = 2
        double correctFeeIntMath = correctHoursIntMath * ratePerHour; // 2 * 30.0 = Rs 60.0

        // Correct calculation using Math.ceil
        int correctHoursMathCeil = (int) Math.ceil(parkedMinutes / 60.0);
        double correctFeeMathCeil = correctHoursMathCeil * ratePerHour;

        System.out.printf("Parked Time: %d minutes%n", parkedMinutes);
        System.out.printf("Buggy Integer Division [minutes / 60]: %d hr -> Fee = Rs %.2f [WRONG UNDERBILLING]%n",
                buggyHours, buggyFee);
        System.out.printf("Correct Integer Arithmetic [(minutes + 59) / 60]: %d hr -> Fee = Rs %.2f [CORRECT]%n",
                correctHoursIntMath, correctFeeIntMath);
        System.out.printf("Correct Math.ceil [(int)Math.ceil(minutes / 60.0)]: %d hr -> Fee = Rs %.2f [CORRECT]%n",
                correctHoursMathCeil, correctFeeMathCeil);

        // 2. Jagged Array Representation for Non-uniform Floors
        System.out.println("\n--- 2. Jagged Array vs 1D Flattened Array vs ArrayList ---");
        int[] slotsPerFloor = {2, 4, 3}; // Floor 0 has 2 slots, Floor 1 has 4 slots, Floor 2 has 3 slots

        // Jagged 2D array representation
        String[][] jaggedSlots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            jaggedSlots[f] = new String[slotsPerFloor[f]];
        }
        jaggedSlots[1][2] = "TN01AB1111"; // Occupy floor 1, slot 2
        System.out.println("Jagged Array Slot (Floor 1, Slot 2): " + jaggedSlots[1][2]);

        // Flattened 1D array with prefix sums
        int totalSlots = 0;
        int[] prefixSum = new int[slotsPerFloor.length + 1];
        for (int i = 0; i < slotsPerFloor.length; i++) {
            prefixSum[i] = totalSlots;
            totalSlots += slotsPerFloor[i];
        }
        prefixSum[slotsPerFloor.length] = totalSlots;

        String[] flatSlots = new String[totalSlots];
        // Index mapping for (floor, slot): prefixSum[floor] + slot
        int flatIndex = prefixSum[1] + 2;
        flatSlots[flatIndex] = "TN01AB1111";
        System.out.println("Flattened 1D Array Slot at index " + flatIndex + " (prefixSum[1] + 2): " + flatSlots[flatIndex]);

        // ArrayList representation
        ArrayList<String[]> listSlots = new ArrayList<>();
        for (int slotsCount : slotsPerFloor) {
            listSlots.add(new String[slotsCount]);
        }
        listSlots.get(1)[2] = "TN01AB1111";
        System.out.println("ArrayList Slot (list.get(1)[2]): " + listSlots.get(1)[2]);

        System.out.println("\nRepresentation Summary:");
        System.out.println("- Jagged Array provides natural 2D indexing slots[f][s] while supporting uneven floor capacities.");
        System.out.println("- Flattened 1D requires prefix sum lookup tables when floors vary in slot count.");
        System.out.println("- Integer ceiling formula ((minutes + 59) / 60) avoids floating-point overhead and guarantees exact integer billing.");
    }
}
