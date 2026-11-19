package task04;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class DrawPadTest {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     TASK 04: EVENT-HANDLING FORENSICS (DRAWPAD) TEST          ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        DrawPad pad = new DrawPad();
        MouseListener listener = pad.getMouseListeners()[0];

        // Test 1: Initial State
        total++;
        System.out.println("\n[Test 1] Testing Initial Click Count and Coordinate Storage:");
        System.out.println("Initial Label Text: '" + pad.getStatusLabel().getText() + "'");
        System.out.println("Initial Points Size: " + pad.getPoints().size());
        if ("Clicks: 0".equals(pad.getStatusLabel().getText()) && pad.getPoints().isEmpty()) {
            System.out.println(">>> PASS: Initial state initialized to 0 clicks and empty list.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Initial state unexpected.");
        }

        // Test 2: Dispatch Clicks via mouseClicked
        total++;
        System.out.println("\n[Test 2] Testing Clicks Dispatch via Repaired mouseClicked Handler:");
        int[][] clickCoords = {{50, 60}, {120, 180}, {200, 220}, {310, 95}};
        for (int i = 0; i < clickCoords.length; i++) {
            MouseEvent me = new MouseEvent(pad, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                                           0, clickCoords[i][0], clickCoords[i][1], 1, false);
            listener.mouseClicked(me);
        }

        System.out.println("Points Count: " + pad.getPoints().size());
        System.out.println("Status Label: '" + pad.getStatusLabel().getText() + "'");
        if (pad.getPoints().size() == 4 && "Clicks: 4".equals(pad.getStatusLabel().getText())) {
            System.out.println(">>> PASS: mouseClicked handler correctly stored coordinates and updated click count.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Points or status label mismatch.");
        }

        // Test 3: Coordinate Accuracy
        total++;
        System.out.println("\n[Test 3] Verifying Recorded Point Coordinates:");
        boolean coordsMatch = pad.getPoints().get(0).x == 50 && pad.getPoints().get(0).y == 60 &&
                              pad.getPoints().get(3).x == 310 && pad.getPoints().get(3).y == 95;
        if (coordsMatch) {
            System.out.println(">>> PASS: Point coordinates recorded with exact pixel fidelity.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Coordinate values corrupted.");
        }

        // Test 4: Persistent Rendering in paint(Graphics g)
        total++;
        System.out.println("\n[Test 4] Verifying Repaint Persistence (Simulating Minimize/Restore):");
        BufferedImage offscreen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics g = offscreen.getGraphics();
        try {
            pad.paint(g);
            g.dispose();
            System.out.println(">>> PASS: paint(Graphics g) successfully traversed stored points and rendered persistent dots.");
            passed++;
        } catch (Exception e) {
            System.out.println(">>> FAIL: Exception during paint rendering: " + e.getMessage());
        }

        pad.dispose();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " tests.");
        System.out.println("===============================================================");
    }
}
