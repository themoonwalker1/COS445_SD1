// Student_hybrid.java: an adaptive two-phase strategy for N>20, 
// direct approach for N<=20

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Student_ASU implements Student {

    /*
     * For "large N" (N>20), we filter down to K schools after a quick desirability check,
     * then perform a more expensive computation on those K. 
     * If N<=20, we can just do the expensive approach on every school, no filtering needed.
     */
    private static final int K = 20;   

    /*
     * Mini-lottery parameters:
     *   M is how many times we run each mini-lottery,
     *   POOL_SIZE is how many competitors per mini-lottery.
     * Increase them for more accuracy, or decrease if running too slowly.
     */
    private static final int M = 120;
    private static final int POOL_SIZE = 35;

    /*
     * Logistic transform parameter to estimate probability:
     *   AGGRESSIVENESS controls how steep the logistic function is.
     * Values in ~[3,10] are typical.
     */
    private static final double AGGRESSIVENESS = 3.0;

    private static final Random rand = new Random();

    /**
     * Simple container for sorting. We'll sort in descending order of 'score'.
     */
    private static class SchoolData implements Comparable<SchoolData> {
        int index;
        double score; // Generic "score" or "expected utility"

        SchoolData(int i, double s) {
            index = i;
            score = s;
        }

        @Override
        public int compareTo(SchoolData other) {
            // Descending order by score; tie-break by index ascending
            int ret = Double.compare(other.score, this.score);
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

        // If N <= 20, just do one-phase: compute expected utility for all schools directly.
        if (N <= 20) {
            return doSinglePhase(N, S, W, aptitude, schools, synergies);
        }

        // Otherwise, do the two-phase approach.

        // ----- PHASE 1: Quick Filter -----
        // We'll keep the top K schools by Q_u + S_{s,u} (a quick desirability measure).
        SchoolData[] initial = new SchoolData[N];
        for (int i = 0; i < N; i++) {
            double desirability = schools.get(i) + synergies.get(i);
            initial[i] = new SchoolData(i, desirability);
        }
        // Sort by descending desirability
        Arrays.sort(initial);

        int Kactual = Math.min(K, N);
        SchoolData[] candidates = Arrays.copyOf(initial, Kactual);

        // ----- PHASE 2: More Detailed Probability + Expected Utility -----
        List<SchoolData> finalList = new ArrayList<>(Kactual);

        // We'll use a combination of logistic approach + mini-lottery simulation
        double avgCompetitor = (S / 2.0) + (W / 2.0);

        for (SchoolData cd : candidates) {
            int idx = cd.index;
            double quality = schools.get(idx);
            double synergy = synergies.get(idx);

            double desirability = quality + synergy;

            // Competitiveness advantage
            double compAdv = (aptitude + synergy) - avgCompetitor;

            // Probability estimate A: logistic
            double pLogistic = 1.0 / (1.0 + Math.exp(-AGGRESSIVENESS * compAdv));

            // Probability estimate B: mini-lottery sim
            double pSim = estimateProbabilitySimulation(aptitude, synergy, S, W, M, POOL_SIZE);

            // Weighted combination: 60% sim, 40% logistic
            double pAdmission = 0.6 * pSim + 0.4 * pLogistic;

            // expected utility = desirability * probability
            double expUtility = desirability * pAdmission;
            finalList.add(new SchoolData(idx, expUtility));
        }

        finalList.sort(null); // descending by expUtility
        int[] ret = new int[10];
        for (int i = 0; i < 10; i++) {
            ret[i] = finalList.get(i).index;
        }
        return ret;
    }

    /**
     * One-phase approach for small N (N<=20): we can afford to do 
     * a detailed simulation/logistic for every school directly.
     */
    private int[] doSinglePhase(
        int N,
        double S,
        double W,
        double aptitude,
        List<Double> schools,
        List<Double> synergies
    ) {
        double avgCompetitor = (S / 2.0) + (W / 2.0);
        SchoolData[] data = new SchoolData[N];
        for (int i = 0; i < N; i++) {
            double desirability = schools.get(i) + synergies.get(i);
            double compAdv = (aptitude + synergies.get(i)) - avgCompetitor;

            double pLogistic = 1.0 / (1.0 + Math.exp(-AGGRESSIVENESS * compAdv));
            double pSim = estimateProbabilitySimulation(aptitude, synergies.get(i), S, W, M, POOL_SIZE);

            // 50% sim, 50% logistic for small N
            double pAdmission = 0.5 * pLogistic + 0.5 * pSim;
            double expUtility = desirability * pAdmission;
            data[i] = new SchoolData(i, expUtility);
        }

        // Sort descending by expected utility
        Arrays.sort(data);

        // Return top 10 (or fewer if N<10, but the problem states N>=10)
        int[] ret = new int[10];
        for (int i = 0; i < 10; i++) {
            ret[i] = data[i].index;
        }
        return ret;
    }

    /**
     * estimateProbabilitySimulation: run M mini-lotteries, each with POOL_SIZE
     * random competitors in [0,S] x [0,W], and see how often we are top.
     */
    private double estimateProbabilitySimulation(
        double myAptitude,
        double mySynergy,
        double S,
        double W,
        int M,
        int poolSize
    ) {
        int wins = 0;
        double myScore = myAptitude + mySynergy;
        for (int trial = 0; trial < M; trial++) {
            boolean iAmTop = true;
            for (int c = 0; c < poolSize; c++) {
                double compApt = rand.nextDouble() * S;
                double compSyn = rand.nextDouble() * W;
                double compScore = compApt + compSyn;
                if (compScore >= myScore) {
                    iAmTop = false;
                    break;
                }
            }
            if (iAmTop) {
                wins++;
            }
        }
        return (double) wins / (double) M;
    }
}
