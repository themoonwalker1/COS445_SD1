import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A thorough simulation-based strategy that, for each school, runs multiple
 * mini-lotteries against a random pool of competitors. We then pick the top 10
 * schools by estimated expected utility.
 */
public class Student_simulation implements Student {

    // Number of mini-lotteries per school
    private static final int M = 300;
    // Number of random "competitors" in each mini-lottery
    private static final int COMP_POOL = 30;
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
            // Sort descending by expUtility; tiebreak by index ascending
            int ret = Double.compare(other.expUtility, this.expUtility);
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
        // We'll store each school's expected utility in this array
        SchoolScore[] scores = new SchoolScore[N];

        for (int u = 0; u < N; u++) {
            // My competitiveness at school u
            double myCompet = aptitude + synergies.get(u);

            // Run M mini-lotteries for this school
            int numWins = 0;
            for (int trial = 0; trial < M; trial++) {
                boolean iAmTop = true;
                // Generate COMP_POOL random competitors
                for (int c = 0; c < COMP_POOL; c++) {
                    double compApt = rand.nextDouble() * S;
                    double compSyn = rand.nextDouble() * W;
                    double compCompet = compApt + compSyn;
                    if (compCompet >= myCompet) {
                        // If any competitor >= me, I'm not top in this lottery
                        iAmTop = false;
                        break;
                    }
                }
                if (iAmTop) {
                    numWins++;
                }
            }

            // Probability of being the top applicant at this school
            double pAdmission = (double) numWins / (double) M;

            // My preference for the school
            double desirability = schools.get(u) + synergies.get(u);

            // Expected utility
            double expUtility = desirability * pAdmission;

            scores[u] = new SchoolScore(u, expUtility);
        }

        // Sort by descending expected utility
        Arrays.sort(scores);

        // Pick top 10
        int[] ret = new int[10];
        for (int i = 0; i < 10; i++) {
            ret[i] = scores[i].index;
        }
        return ret;
    }
}
