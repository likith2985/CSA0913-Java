import java.util.*;

/**
 * Assignment 2 - Q5: Derive a Generic Ranking Component from a Principle
 * Demonstrates:
 * 1. Reusable generic Leaderboard<T> tested on Teams, Players, and QuizTeams
 * 2. Multi-level tie breaking (points -> goalDiff -> name)
 * 3. Proving mathematical transitivity and consistency of ranking
 * 4. Exposing the classic Subtraction-based Comparator Overflow Bug
 */
public class GenericRankingInvestigation {

    // Domain Entity 1: QuizTeam
    public static class QuizTeam {
        final String college;
        final int score;
        final int timeTakenSecs;

        public QuizTeam(String college, int score, int timeTakenSecs) {
            this.college = college;
            this.score = score;
            this.timeTakenSecs = timeTakenSecs;
        }

        @Override
        public String toString() {
            return String.format("%s(Score:%d, Time:%ds)", college, score, timeTakenSecs);
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("  Q5: GENERIC RANKING COMPONENT & COMPARISON TRANSITIVITY PRINCIPLE");
        System.out.println("==========================================================================\n");

        // 1. Reusability: Leaderboard for Quiz Teams
        // Ranking criteria: Score (high first) -> Time Taken (low first) -> College Name (A-Z)
        Comparator<QuizTeam> quizOrder = Comparator.comparingInt((QuizTeam q) -> q.score).reversed()
                .thenComparingInt(q -> q.timeTakenSecs)
                .thenComparing(q -> q.college);

        Leaderboard<QuizTeam> quizBoard = new Leaderboard<>(quizOrder);
        quizBoard.add(new QuizTeam("MIT", 95, 120));
        quizBoard.add(new QuizTeam("Stanford", 95, 90));  // Higher rank due to faster time
        quizBoard.add(new QuizTeam("Harvard", 95, 90));   // Tied with Stanford, but 'H' < 'S'
        quizBoard.add(new QuizTeam("Oxford", 88, 70));

        System.out.println("1. Generic Leaderboard Reused for Quiz Teams:");
        System.out.println("   " + quizBoard.ranking());

        // 2. Demonstration of the Subtraction Bug (Violating the Principle)
        System.out.println("\n2. Faulty Comparator Analysis: The Integer Subtraction Overflow Bug");
        // Naive comparator: (a, b) -> (b.points - a.points)
        int scoreA = Integer.MAX_VALUE - 10;
        int scoreB = -50; // Negative points/penalty

        // If subtracting: scoreB - scoreA
        int naiveDiff = scoreB - scoreA; // OVERFLOW! Wraps around to positive number
        System.out.printf("   Score A: %,d | Score B: %d%n", scoreA, scoreB);
        System.out.printf("   Naive (scoreB - scoreA) evaluates to: %,d (POSITIVE due to 32-bit overflow!)%n", naiveDiff);
        System.out.println("   Result: Naive subtraction falsely declares Score B ranked HIGHER than Score A!");
        System.out.println("   Correct Fix: Integer.compare(scoreB, scoreA) or Comparator.comparingInt()");

        // 3. Mathematical Transitivity Verification
        System.out.println("\n3. Principle Consistency Verification:");
        System.out.println("   Principle: If A ranks above B, and B ranks above C, then A MUST rank above C.");
        Team a = new Team("Eagles", 9, 6);
        Team b = new Team("Bisons", 9, 4);
        Team c = new Team("Cobras", 6, 1);

        Comparator<Team> comp = Comparator.comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                .thenComparing(t -> t.name);

        boolean aAboveB = comp.compare(a, b) < 0;
        boolean bAboveC = comp.compare(b, c) < 0;
        boolean aAboveC = comp.compare(a, c) < 0;

        System.out.printf("   comp(Eagles, Bisons) < 0: %s%n", aAboveB);
        System.out.printf("   comp(Bisons, Cobras) < 0: %s%n", bAboveC);
        System.out.printf("   comp(Eagles, Cobras) < 0: %s [Transitivity Upheld: %s]%n",
                aAboveC, (aAboveB && bAboveC && aAboveC));
    }
}
