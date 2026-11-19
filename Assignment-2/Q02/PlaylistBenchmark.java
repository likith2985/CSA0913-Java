import java.util.*;

/**
 * Assignment 2 - Q2: When Is ArrayList an Appropriate Engineering Choice?
 * Experiments comparing ArrayList, LinkedList, and ArrayDeque for a Music Playlist Editor.
 */
public class PlaylistBenchmark {

    public static class Song {
        private final String title;
        private final String artist;
        public Song(String title, String artist) {
            this.title = title;
            this.artist = artist;
        }
        public String getTitle() { return title; }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  Q2 BENCHMARK: ARRAYLIST vs LINKEDLIST vs ARRAYDEQUE (PLAYLIST EDITOR)");
        System.out.println("==========================================================================\n");

        int[] testSizes = {1000, 10000, 50000};

        for (int size : testSizes) {
            System.out.println("==========================================================================");
            System.out.printf("  DATASET SIZE: %,d Songs%n", size);
            System.out.println("==========================================================================");

            // 1. Add at End
            long tAddEndAL = benchmarkAddEnd(new ArrayList<>(), size);
            long tAddEndLL = benchmarkAddEnd(new LinkedList<>(), size);
            long tAddEndAD = benchmarkAddEnd(new ArrayDeque<>(), size);
            System.out.printf("1. Add at End (%d items):%n", size);
            System.out.printf("   ArrayList : %8.3f ms%n", tAddEndAL / 1e6);
            System.out.printf("   LinkedList: %8.3f ms%n", tAddEndLL / 1e6);
            System.out.printf("   ArrayDeque: %8.3f ms%n", tAddEndAD / 1e6);

            // Pre-populate lists for subsequent tests
            List<Song> arrayList = new ArrayList<>(size);
            List<Song> linkedList = new LinkedList<>();
            ArrayDeque<Song> arrayDeque = new ArrayDeque<>(size);
            for (int i = 0; i < size; i++) {
                Song s = new Song("Title_" + i, "Artist_" + i);
                arrayList.add(s);
                linkedList.add(s);
                arrayDeque.add(s);
            }

            // 2. Random Access: get(k) - 1,000 lookups
            int lookupCount = Math.min(size, 2000);
            long tGetAL = benchmarkGetK(arrayList, lookupCount, size);
            long tGetLL = benchmarkGetK(linkedList, lookupCount, size);
            System.out.printf("\n2. Access k-th element (%d random get(k) calls):%n", lookupCount);
            System.out.printf("   ArrayList : %8.3f ms [O(1) direct indexing]%n", tGetAL / 1e6);
            System.out.printf("   LinkedList: %8.3f ms [O(N) pointer traversal]%n", tGetLL / 1e6);

            // 3. Iterating through whole list
            long tIterAL = benchmarkIterate(arrayList);
            long tIterLL = benchmarkIterate(linkedList);
            long tIterAD = benchmarkIterateQueue(arrayDeque);
            System.out.printf("\n3. Iterate whole list (%d elements):%n", size);
            System.out.printf("   ArrayList : %8.3f ms%n", tIterAL / 1e6);
            System.out.printf("   LinkedList: %8.3f ms%n", tIterLL / 1e6);
            System.out.printf("   ArrayDeque: %8.3f ms%n", tIterAD / 1e6);

            // 4. Insert at Front: add(0, song) - 500 operations
            int insertCount = 500;
            long tInsFrontAL = benchmarkInsertFront(new ArrayList<>(arrayList), insertCount);
            long tInsFrontLL = benchmarkInsertFront(new LinkedList<>(linkedList), insertCount);
            long tInsFrontAD = benchmarkInsertFrontDeque(new ArrayDeque<>(arrayDeque), insertCount);
            System.out.printf("\n4. Insert at Front (%d operations):%n", insertCount);
            System.out.printf("   ArrayList : %8.3f ms [System.arraycopy shifting]%n", tInsFrontAL / 1e6);
            System.out.printf("   LinkedList: %8.3f ms [O(1) pointer rewire]%n", tInsFrontLL / 1e6);
            System.out.printf("   ArrayDeque: %8.3f ms [O(1) head pointer move]%n", tInsFrontAD / 1e6);

            // 5. Remove from Front: remove(0) - 500 operations
            long tRemFrontAL = benchmarkRemoveFront(new ArrayList<>(arrayList), insertCount);
            long tRemFrontLL = benchmarkRemoveFront(new LinkedList<>(linkedList), insertCount);
            long tRemFrontAD = benchmarkRemoveFrontDeque(new ArrayDeque<>(arrayDeque), insertCount);
            System.out.printf("\n5. Remove from Front (%d operations):%n", insertCount);
            System.out.printf("   ArrayList : %8.3f ms [System.arraycopy shifting]%n", tRemFrontAL / 1e6);
            System.out.printf("   LinkedList: %8.3f ms [O(1) pointer rewire]%n", tRemFrontLL / 1e6);
            System.out.printf("   ArrayDeque: %8.3f ms [O(1) head pointer move]%n", tRemFrontAD / 1e6);

            System.out.println();
        }
    }

    private static long benchmarkAddEnd(Collection<Song> coll, int size) {
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            coll.add(new Song("S" + i, "A" + i));
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkGetK(List<Song> list, int lookups, int size) {
        Random rand = new Random(42);
        long start = System.nanoTime();
        long dummy = 0;
        for (int i = 0; i < lookups; i++) {
            dummy += list.get(rand.nextInt(size)).hashCode();
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkIterate(List<Song> list) {
        long start = System.nanoTime();
        long count = 0;
        for (Song s : list) {
            count += s.getTitle().length();
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkIterateQueue(ArrayDeque<Song> deque) {
        long start = System.nanoTime();
        long count = 0;
        for (Song s : deque) {
            count += s.getTitle().length();
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkInsertFront(List<Song> list, int ops) {
        Song testSong = new Song("NewSong", "NewArtist");
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            list.add(0, testSong);
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkInsertFrontDeque(ArrayDeque<Song> deque, int ops) {
        Song testSong = new Song("NewSong", "NewArtist");
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            deque.addFirst(testSong);
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkRemoveFront(List<Song> list, int ops) {
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (!list.isEmpty()) list.remove(0);
        }
        return System.nanoTime() - start;
    }

    private static long benchmarkRemoveFrontDeque(ArrayDeque<Song> deque, int ops) {
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            if (!deque.isEmpty()) deque.pollFirst();
        }
        return System.nanoTime() - start;
    }
}
