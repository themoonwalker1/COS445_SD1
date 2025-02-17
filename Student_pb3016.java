import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;

public class Student_pb3016 implements Student {

    private static final int SIMULATION_COUNT = 100;

    // Returns the CDF value for the sum of two independent Uniform[0, X] random
    // variables.
    private double computeProbability(double x, double A, double B) {
        double lower = Math.min(A, B);
        double upper = Math.max(A, B);
        if (x < lower) {
            return (x * x) / (2 * lower * upper);
        } else if (x < upper) {
            return (2 * x - lower) / (2 * upper);
        } else {
            return 1 - ((lower + upper - x) * (lower + upper - x)) / (2 * lower * upper);
        }
    }

    // Helper: Computes an initial score for a given university index.
    private double computeInitialScore(int uniIndex, int totalUnis, double A, double W,
            double applicantScore, double[] uniQualities,
            double[] applicantSynergy, double Q) {
        double prob = computeProbability(applicantScore + applicantSynergy[uniIndex], A, W);
        double rankFraction = 1 - ((double) uniIndex / (totalUnis - 1));
        double expectedMax = Math.max(0, totalUnis - (1 / rankFraction)) / (double) totalUnis;
        double target;
        if (Q != 0) {
            target = (A * rankFraction + W * expectedMax) / (A + W);
        } else {
            target = (double) totalUnis / (totalUnis - 1);
        }
        double factor = Math.min(1, prob / target);
        double score = Math.sqrt(factor) * (factor * uniQualities[uniIndex] + applicantSynergy[uniIndex]);
        return score;
    }

    // Forms a ranking of universities for an applicant.
    private int[] formBasicChoices(int count, double A, double Q, double W,
            double applicantScore, double[] uniQualities, double[] applicantSynergy) {
        int totalUnis = uniQualities.length;
        // Create a list of university indices.
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < totalUnis; i++) {
            indices.add(i);
        }

        // Sort indices based on a computed score (descending). Ties are broken by
        // comparing indices.
        Collections.sort(indices, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                double score1 = computeInitialScore(i1, totalUnis, A, W, applicantScore, uniQualities, applicantSynergy,
                        Q);
                double score2 = computeInitialScore(i2, totalUnis, A, W, applicantScore, uniQualities, applicantSynergy,
                        Q);
                if (score1 == score2) {
                    return i1.compareTo(i2);
                } else {
                    // For descending order, compare score2 to score1.
                    return Double.compare(score2, score1);
                }
            }
        });

        // Select the top 'count' indices.
        ArrayList<Integer> selected = new ArrayList<Integer>(indices.subList(0, count));

        // Re-rank these selected indices based on the simple sum of quality and synergy
        // (descending).
        Collections.sort(selected, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                double newVal1 = uniQualities[i1] + applicantSynergy[i1];
                double newVal2 = uniQualities[i2] + applicantSynergy[i2];
                if (newVal1 == newVal2) {
                    return i1.compareTo(i2);
                } else {
                    return Double.compare(newVal2, newVal1);
                }
            }
        });

        int[] output = new int[count];
        for (int j = 0; j < count; j++) {
            output[j] = selected.get(j);
        }
        return output;
    }

    private int[] opponentStrategy(int count, double A, double Q, double W,
            double opponentAptitude, double[] uniQualities, double[] opponentSynergy) {
        // Local class to represent a school.
        class School implements Comparable<School> {
            public int index;
            public double score;
            public double delta; // for debugging/verification if needed

            public School(int index, double score, double delta) {
                this.index = index;
                this.score = score;
                this.delta = delta;
            }

            // Sort in descending order of score. If tie, compare by index ascending.
            @Override
            public int compareTo(School other) {
                int cmp = Double.compare(other.score, this.score);
                return (cmp == 0) ? Integer.compare(this.index, other.index) : cmp;
            }
        }

        int N = uniQualities.length;
        School[] preferences = new School[N];
        double[] deltas = new double[N];

        // Compute delta for each school:
        // delta = (opponentAptitude + synergy) - ((A + W) / 2)
        for (int i = 0; i < N; i++) {
            double synergy = opponentSynergy[i];
            double delta = (opponentAptitude + synergy) - ((A + W) / 2.0);
            deltas[i] = delta;
        }

        // Compute the minimum and maximum delta.
        double dmin = Double.POSITIVE_INFINITY;
        double dmax = Double.NEGATIVE_INFINITY;
        for (double delta : deltas) {
            if (delta < dmin)
                dmin = delta;
            if (delta > dmax)
                dmax = delta;
        }

        // Compute the midpoint and logistic slope k.
        double midpoint = (dmin + dmax) / 2.0;
        double k = (dmax - dmin != 0) ? Math.log(99) / (dmax - dmin) : 1.0;

        // Compute the overall score for each school.
        for (int i = 0; i < N; i++) {
            double quality = uniQualities[i]; // intrinsic quality of school i
            double synergy = opponentSynergy[i]; // synergy for school i
            // True desirability V(u) = quality + synergy.
            double desirability = quality + synergy;
            double delta = deltas[i];
            // Compute acceptance probability using a logistic function.
            double acceptanceProb = 1.0 / (1.0 + Math.exp(-k * (delta - midpoint)));
            // Final score: expected payoff = V(u) * acceptance probability.
            double score = desirability * acceptanceProb;
            preferences[i] = new School(i, score, delta);
        }

        // Sort schools in descending order by score.
        Arrays.sort(preferences);

        // Select the top 'count' schools.
        int[] applications = new int[count];
        for (int i = 0; i < count; i++) {
            applications[i] = preferences[i].index;
        }

        return applications;
    }

    // Finds the index of a value in an array; if absent, returns a large number.
    private int findPosition(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    // Runs a simulation of the matching process.
    // Each proposal is stored as a two-element array: [score, applicantId].
    private int simulateMatch(int[] myFullList, double myAptitude, double[] uniQualities,
            double[] mySynergies, double A, double Q, double W) {
        int totalUnis = uniQualities.length;
        int totalStudents = totalUnis; // One applicant per university.
        Random rnd = new Random();

        // Generate opponent data.
        double[] oppAptitudes = new double[totalStudents - 1];
        double[][] oppSynergies = new double[totalStudents - 1][totalUnis];
        for (int i = 0; i < totalStudents - 1; i++) {
            oppAptitudes[i] = rnd.nextDouble() * A;
            for (int j = 0; j < totalUnis; j++) {
                oppSynergies[i][j] = rnd.nextDouble() * W;
            }
        }

        // Determine opponent choices using the opponentStrategy helper.
        int[][] oppChoices = new int[totalStudents - 1][];
        for (int i = 0; i < totalStudents - 1; i++) {
            oppChoices[i] = opponentStrategy(10, A, Q, W, oppAptitudes[i], uniQualities, oppSynergies[i]);
        }

        // Assemble all applicants' choices.
        int[][] allChoices = new int[totalStudents][];
        for (int i = 0; i < totalStudents - 1; i++) {
            allChoices[i] = oppChoices[i];
        }
        allChoices[totalStudents - 1] = myFullList;

        // Build proposals for each university.
        ArrayList<ArrayList<double[]>> proposals = new ArrayList<ArrayList<double[]>>();
        for (int u = 0; u < totalUnis; u++) {
            proposals.add(new ArrayList<double[]>());
        }
        // Opponent students' proposals.
        for (int s = 0; s < totalStudents - 1; s++) {
            for (int uni : allChoices[s]) {
                double val = oppAptitudes[s] + oppSynergies[s][uni];
                proposals.get(uni).add(new double[] { val, s });
            }
        }
        // Our student's proposals.
        for (int uni : myFullList) {
            double val = myAptitude + mySynergies[uni];
            proposals.get(uni).add(new double[] { val, totalStudents - 1 });
        }

        // Sort proposals for each university in ascending order (by score, tie-break by
        // applicant id).
        for (int u = 0; u < totalUnis; u++) {
            Collections.sort(proposals.get(u), new Comparator<double[]>() {
                public int compare(double[] a, double[] b) {
                    if (a[0] == b[0]) {
                        return Double.compare(a[1], b[1]);
                    }
                    return Double.compare(a[0], b[0]);
                }
            });
        }

        // Matching process.
        int[] studentAssignment = new int[totalStudents]; // -1 means no match.
        int[] uniAssignment = new int[totalUnis];
        Arrays.fill(studentAssignment, -1);
        Arrays.fill(uniAssignment, -1);

        int[] proposalPointer = new int[totalUnis];
        for (int u = 0; u < totalUnis; u++) {
            proposalPointer[u] = proposals.get(u).size() - 1;
        }

        boolean updated = true;
        while (updated) {
            updated = false;
            for (int u = 0; u < totalUnis; u++) {
                if (uniAssignment[u] == -1 && proposalPointer[u] >= 0) {
                    double[] candidate = proposals.get(u).get(proposalPointer[u]);
                    proposalPointer[u]--;
                    int stuId = (int) candidate[1];
                    if (studentAssignment[stuId] == -1) {
                        studentAssignment[stuId] = u;
                        uniAssignment[u] = stuId;
                        updated = true;
                    } else {
                        int currentUni = studentAssignment[stuId];
                        int newRank = findPosition(allChoices[stuId], u);
                        int currentRank = findPosition(allChoices[stuId], currentUni);
                        if (newRank < currentRank) {
                            uniAssignment[u] = stuId;
                            studentAssignment[stuId] = u;
                            uniAssignment[currentUni] = -1;
                            updated = true;
                        }
                    }
                }
            }
        }
        return studentAssignment[totalStudents - 1];
    }

    @Override
    public int[] getApplications(int totalUnis, double A, double Q, double W,
            double myAptitude, List<Double> qualityList, List<Double> synergyList) {
        double[] uniQualities = new double[totalUnis];
        double[] mySynergies = new double[totalUnis];
        for (int i = 0; i < totalUnis; i++) {
            uniQualities[i] = qualityList.get(i);
            mySynergies[i] = synergyList.get(i);
        }

        if (A == 0 && W == 0) {
            int[] def = new int[10];
            for (int i = 0; i < 10; i++) {
                def[i] = i;
            }
            return def;
        }

        // Build a full ranking of universities.
        int[] fullRanking = formBasicChoices(totalUnis, A, Q, W, myAptitude, uniQualities, mySynergies);
        int[] matchTally = new int[totalUnis];
        for (int sim = 0; sim < SIMULATION_COUNT; sim++) {
            int match = simulateMatch(fullRanking, myAptitude, uniQualities, mySynergies, A, Q, W);
            if (match != -1) {
                matchTally[match]++;
            }
        }
        int bestUni = 0;
        for (int i = 1; i < totalUnis; i++) {
            if (matchTally[i] > matchTally[bestUni]) {
                bestUni = i;
            }
        }
        int pos = findPosition(fullRanking, bestUni);
        int[] chosen = new int[10];

        // Choose a contiguous window of 10 universities centered (as much as possible)
        // around bestUni.
        int start = Math.max(0, Math.min(pos - 6, totalUnis - 10));
        for (int i = 0; i < 10; i++) {
            chosen[i] = fullRanking[start + i];
        }

        return chosen;
    }
}
