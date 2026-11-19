import java.util.*;

/**
 * Assignment 2 - Q3: Investigation of the hashCode-equals Contract
 * Demonstrates:
 * 1. Broken implementation (equals without hashCode) allowing duplicates
 * 2. Case where HashSet falsely appears to work (same object reference)
 * 3. Mutation after insertion (the "Ghost Element" trap)
 * 4. Verification of corrected Student class
 * 5. Alternative Concept: TreeSet with Comparator vs HashMap vs HashSet
 */
public class HashCodeContractInvestigation {

    // 1. Broken Student class (equals overridden, hashCode NOT overridden)
    public static class BrokenStudent {
        String regNo;
        String name;

        public BrokenStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BrokenStudent other)) return false;
            return Objects.equals(regNo, other.regNo);
        }

        @Override
        public String toString() { return regNo + ":" + name; }
    }

    // 2. Mutable Student class to demonstrate hash mutation vulnerability
    public static class MutableStudent {
        String regNo;
        String name;

        public MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MutableStudent other)) return false;
            return Objects.equals(regNo, other.regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }

        public void setRegNo(String newRegNo) {
            this.regNo = newRegNo;
        }
    }

    // 3. Fully Corrected Immutable Student class
    public static final class CorrectStudent {
        private final String regNo;
        private final String name;

        public CorrectStudent(String regNo, String name) {
            this.regNo = Objects.requireNonNull(regNo);
            this.name = Objects.requireNonNull(name);
        }

        public String getRegNo() { return regNo; }
        public String getName() { return name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CorrectStudent other)) return false;
            return Objects.equals(regNo, other.regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }

        @Override
        public String toString() { return regNo + " " + name; }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   Q3 INVESTIGATION: THE HASHCODE-EQUALS CONTRACT UNDER SCRUTINY");
        System.out.println("==========================================================================\n");

        // TEST 1: Where Broken HashSet APPEARS to work correctly
        // (Adding the exact same reference twice)
        System.out.println("--- Test 1: Where Broken Student Appears to Work ---");
        Set<BrokenStudent> test1Set = new HashSet<>();
        BrokenStudent s1 = new BrokenStudent("192311001", "Arun");
        test1Set.add(s1);
        test1Set.add(s1); // Same reference added
        System.out.println("Prediction: Size = 1 (Reference equality bypasses bucket check)");
        System.out.println("Observed Set Size: " + test1Set.size());
        System.out.println("Result: Looks deceptively correct because s1 == s1 is true.\n");

        // TEST 2: Deliberately exposing duplicate entries in Broken Student
        // (Two distinct instances with identical regNo)
        System.out.println("--- Test 2: Deliberately Exposing Duplicate Failure ---");
        Set<BrokenStudent> test2Set = new HashSet<>();
        test2Set.add(new BrokenStudent("192311001", "Arun"));
        test2Set.add(new BrokenStudent("192311002", "Divya"));
        test2Set.add(new BrokenStudent("192311001", "Arun")); // Duplicate regNo, different heap object
        System.out.println("Prediction: Size = 3 (Different System.identityHashCode lands in different buckets; equals() never called!)");
        System.out.println("Observed Set Size: " + test2Set.size() + " [DUPLICATES RETAINED - CONTRACT BROKEN!]");
        System.out.println("Set Contents: " + test2Set + "\n");

        // TEST 3: Mutating key after insertion (The "Ghost Element" trap)
        System.out.println("--- Test 3: Mutating regNo After Insertion (Key Corruption) ---");
        Set<MutableStudent> test3Set = new HashSet<>();
        MutableStudent mut = new MutableStudent("192311001", "Arun");
        test3Set.add(mut);
        System.out.println("Inserted student with regNo='192311001'. Set contains: " + test3Set.contains(mut));
        // Now mutate the regNo
        mut.setRegNo("192311999");
        System.out.println("Mutated regNo to '192311999'.");
        System.out.println("test3Set.contains(mut): " + test3Set.contains(mut) + " (FAIL: Cannot locate in new hash bucket!)");
        System.out.println("test3Set.remove(mut): " + test3Set.remove(mut) + " (FAIL: Cannot remove ghost element!)");
        System.out.println("Set size remains: " + test3Set.size() + " (Ghost object permanently leaked in old bucket!)\n");

        // TEST 4: Verification with Corrected Student class
        System.out.println("--- Test 4: Corrected Student Class Verification ---");
        Set<CorrectStudent> test4Set = new HashSet<>();
        test4Set.add(new CorrectStudent("192311001", "Arun"));
        test4Set.add(new CorrectStudent("192311002", "Divya"));
        test4Set.add(new CorrectStudent("192311001", "Arun")); // Duplicate
        System.out.println("Observed Set Size: " + test4Set.size() + " [CORRECT: Exactly 2 unique students]");

        // TEST 5: Alternative Concept Comparison (TreeSet vs HashMap vs HashSet)
        System.out.println("\n--- Alternative Concepts Comparison ---");
        // Alternative A: TreeSet with Comparator on regNo
        Set<CorrectStudent> treeSet = new TreeSet<>(Comparator.comparing(CorrectStudent::getRegNo));
        treeSet.add(new CorrectStudent("192311001", "Arun"));
        treeSet.add(new CorrectStudent("192311002", "Divya"));
        treeSet.add(new CorrectStudent("192311001", "Arun"));
        System.out.println("TreeSet with Comparator size: " + treeSet.size() + " (Orders by regNo, O(log N) lookup)");

        // Alternative B: HashMap<String, CorrectStudent> keyed by regNo
        Map<String, CorrectStudent> studentMap = new HashMap<>();
        studentMap.put("192311001", new CorrectStudent("192311001", "Arun"));
        studentMap.put("192311002", new CorrectStudent("192311002", "Divya"));
        studentMap.put("192311001", new CorrectStudent("192311001", "Arun"));
        System.out.println("HashMap<String, Student> size: " + studentMap.size() + " (Keyed by String, O(1) average)");
    }
}
