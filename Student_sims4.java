import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A simulation-based strategy where each competitor is a "holist" student.
 * We generate random aptitudes and synergy for each competitor, let them
 * pick their top 10 schools by (Q_u + synergy), and then check whether
 * our student is top among all who applied to each school.
 *
 * We then compute an estimated admission probability for each school,
 * multiply by desirability (Q_u + S_{s,u}), and pick the 10 with the highest
 * expected utility.
 */
public class Student_sims4 implements Student {

    // Number of mini-lotteries per school
    private static final int M = 100;
    // Number of competitors in each mini-lottery
    private static final int POOL_SIZE = 30;
    private static final Random rand = new Random();

    private class SchoolScore implements Comparable<SchoolScore> {
        int index;
        double expUtility;

        SchoolScore(int i, double e) {
            index = i;
            expUtility = e;
        }

        @Override
        public int compareTo(SchoolScore other) {
            // Sort descending by expUtility; tie-break by index ascending
            int ret = Double.compare(other.expUtility, this.expUtility);
            return (ret == 0) ? Integer.compare(this.index, other.index) : ret;
        }
    }

    /**
     * Represents one "holist" competitor: random aptitude, random synergy array,
     * plus a method to get which schools they apply to (top 10 by Q + synergy).
     */
    private static class HolistCompetitor {
        double aptitude;
        double[] synergy;
        int[] apps; // the top-10 schools they apply to

        HolistCompetitor(double apt, double[] syn, List<Double> schools) {
            aptitude = apt;
            synergy = syn;

            // We'll compute (Q_u + synergy[u]) for each school,
            // then pick the top 10 indices
            int N = schools.size();
            SchoolChoice[] choices = new SchoolChoice[N];
            for (int i = 0; i < N; i++) {
                double value = schools.get(i) + synergy[i];
                choices[i] = new SchoolChoice(i, value);
            }
            Arrays.sort(choices); // sorts descending by value

            // Store the top 10
            apps = new int[10];
            for (int i = 0; i < 10; i++) {
                apps[i] = choices[i].index;
            }
        }

        /**
         * Returns true if this competitor applies to school 'u'.
         */
        boolean appliesTo(int u) {
            for (int x : apps) {
                if (x == u) return true;
            }
            return false;
        }

        /**
         * The school's perspective rank for this competitor is (aptitude + synergy[u]).
         */
        double competitivenessAt(int u) {
            return aptitude + synergy[u];
        }
    }

    /**
     * Helper for sorting the holist competitor's (Q_u + synergy).
     */
    private static class SchoolChoice implements Comparable<SchoolChoice> {
        int index;
        double value;

        SchoolChoice(int i, double v) {
            index = i;
            value = v;
        }

        @Override
        public int compareTo(SchoolChoice other) {
            // Sort descending by value; tie-break by index ascending
            int ret = Double.compare(other.value, this.value);
            return (ret == 0) ? Integer.compare(this.index, other.index) : ret;
        }
    }

    @Override
    public int[] getApplications(
        int N,
        double S,
        double T,
        double W,
        double aptitude,
        List<Double> schools,
        List<Double> synergies
    ) {

        // We'll store the final "expected utility" for each school
        SchoolScore[] results = new SchoolScore[N];

        for (int u = 0; u < N; u++) {
            // My competitiveness for school u
            double myCompet = aptitude + synergies.get(u);

            // Count how many times we are top among a random set of holist competitors
            int wins = 0;

            for (int trial = 0; trial < M; trial++) {
                // Generate POOL_SIZE holist competitors
                List<HolistCompetitor> competitorPool = new ArrayList<>(POOL_SIZE);
                for (int c = 0; c < POOL_SIZE; c++) {
                    double cApt = rand.nextDouble() * S;
                    double[] cSyn = new double[N];
                    for (int k = 0; k < N; k++) {
                        cSyn[k] = rand.nextDouble() * W;
                    }
                    competitorPool.add(new HolistCompetitor(cApt, cSyn, schools));
                }

                // Check if I'm top among all competitors who apply to school u
                // For each competitor that applies to school u, compare
                // (competitorApt + competitorSyn[u]) vs myCompet
                boolean iAmTop = true;
                for (HolistCompetitor hc : competitorPool) {
                    if (hc.appliesTo(u)) {
                        double competitorRank = hc.competitivenessAt(u);
                        if (competitorRank >= myCompet) {
                            iAmTop = false;
                            break;
                        }
                    }
                }

                if (iAmTop) {
                    wins++;
                }
            }

            // Probability of admission for school u
            double pAdmission = (double) wins / (double) M;

            // Desirability = Q_u + synergy[u]
            double desirability = schools.get(u) + synergies.get(u);

            // Expected utility
            double expUtility = desirability * pAdmission;

            results[u] = new SchoolScore(u, expUtility);
        }

        // Sort by descending expected utility
        Arrays.sort(results);

        // Pick the top 10
        int[] ret = new int[10];
        for (int i = 0; i < 10; i++) {
            ret[i] = results[i].index;
        }
        return ret;
    }
}
