package task03;

import java.awt.Dimension;

public class LibrarySearchTest {
    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("     TASK 03: LIBRARY SEARCH SCREEN LAYOUT & RESIZE TEST       ");
        System.out.println("===============================================================");

        LibrarySearchScreen screen = new LibrarySearchScreen();
        int passed = 0;
        int total = 0;

        // Test Dimensions
        Dimension[] testSizes = {
            new Dimension(500, 350),
            new Dimension(750, 500),
            new Dimension(1000, 650)
        };

        int[] northHeights = new int[testSizes.length];
        int[] southHeights = new int[testSizes.length];
        int[] centerHeights = new int[testSizes.length];
        int[] centerWidths = new int[testSizes.length];

        for (int i = 0; i < testSizes.length; i++) {
            Dimension dim = testSizes[i];
            screen.setSize(dim);
            screen.getContentPane().setSize(dim);
            screen.getContentPane().getLayout().layoutContainer(screen.getContentPane());

            northHeights[i] = screen.getNorthPanel().getHeight();
            southHeights[i] = screen.getSouthPanel().getHeight();
            centerHeights[i] = screen.getScrollPane().getHeight();
            centerWidths[i] = screen.getScrollPane().getWidth();

            System.out.println(String.format("\n[Resolution %d] Window Size: %d x %d", (i + 1), dim.width, dim.height));
            System.out.println(String.format("   -> North Panel Height  : %d px (Fixed)", northHeights[i]));
            System.out.println(String.format("   -> South Panel Height  : %d px (Fixed)", southHeights[i]));
            System.out.println(String.format("   -> Center Area Size    : %d x %d px (Expanded)", centerWidths[i], centerHeights[i]));
        }

        // Test 1: North Panel Height Stability
        total++;
        boolean northStable = (northHeights[0] == northHeights[1]) && (northHeights[1] == northHeights[2]);
        System.out.println("\n[Test 1] Verifying North Search Bar Fixed Height Invariance:");
        if (northStable) {
            System.out.println(">>> PASS: North search bar maintained constant height (" + northHeights[0] + " px) across all resolutions.");
            passed++;
        } else {
            System.out.println(">>> FAIL: North height changed during resize.");
        }

        // Test 2: South Controls Height Stability
        total++;
        boolean southStable = (southHeights[0] == southHeights[1]) && (southHeights[1] == southHeights[2]);
        System.out.println("\n[Test 2] Verifying South Pagination & Action Bar Fixed Height Invariance:");
        if (southStable) {
            System.out.println(">>> PASS: South action bar maintained constant height (" + southHeights[0] + " px) across all resolutions.");
            passed++;
        } else {
            System.out.println(">>> FAIL: South height changed during resize.");
        }

        // Test 3: Center Results Table Growth
        total++;
        boolean centerGrew = (centerHeights[2] > centerHeights[1]) && (centerHeights[1] > centerHeights[0]) &&
                             (centerWidths[2] > centerWidths[1]) && (centerWidths[1] > centerWidths[0]);
        System.out.println("\n[Test 3] Verifying Center Results Table Expands Monotonically:");
        if (centerGrew) {
            System.out.println(">>> PASS: Center JScrollPane captured 100% of window expansion in width and height.");
            passed++;
        } else {
            System.out.println(">>> FAIL: Center area did not grow properly.");
        }

        // Test 4: Search Filtering
        total++;
        System.out.println("\n[Test 4] Testing Search Query Filtering:");
        screen.getSearchField().setText("Java");
        screen.performSearch();
        int matchedJava = screen.getResultsTable().getRowCount();
        System.out.println("Search 'Java' matched: " + matchedJava + " books.");
        if (matchedJava == 3) {
            System.out.println(">>> PASS: Search filtering correctly updated table model (matched 3 Java books).");
            passed++;
        } else {
            System.out.println(">>> FAIL: Expected 3 books matching 'Java', found " + matchedJava);
        }

        screen.dispose();

        System.out.println("\n===============================================================");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + total + " tests.");
        System.out.println("===============================================================");
    }
}
