import java.util.Arrays;
import java.util.List;

public class Student_softlocked implements Student {

  private class School implements Comparable<School> {
    public int index;
    public double score;
    public double delta; // Store delta for debugging

    public School(int index, double score, double delta) {
      this.index = index;
      this.score = score;
      this.delta = delta;
    }

    // Sort in descending order by score. If tie, use ascending index.
    @Override
    public int compareTo(School other) {
      int cmp = Double.compare(other.score, this.score);
      return (cmp == 0) ? Integer.compare(this.index, other.index) : cmp;
    }
  }

  @Override
  public int[] getApplications(
      int N,
      double S,
      double T,
      double W,
      double aptitude,
      List<Double> qualities,   // Q_u for each university u
      List<Double> synergies) { // Sₛ,ᵤ for each university u

    School[] preferences = new School[N];
    double[] deltas = new double[N];
    double[] expDeltas = new double[N];

    // Compute delta for each school
    for (int i = 0; i < N; i++) {
      double synergy = synergies.get(i);
      deltas[i] = (aptitude + synergy) - ((S + W) / 2.0);
    }

    // **Softmax Temperature τ**
    double tau = 5.0; // Higher value makes selection more aggressive

    // Compute exponentials for softmax
    double expSum = 0.0;
    for (int i = 0; i < N; i++) {
      expDeltas[i] = Math.exp(tau * deltas[i]);
      expSum += expDeltas[i];
    }

    // Compute softmax probabilities for each school
    for (int i = 0; i < N; i++) {
      expDeltas[i] /= expSum; // Normalize
    }

    // Compute final scores
    for (int i = 0; i < N; i++) {
      double quality = qualities.get(i);
      double synergy = synergies.get(i);

      // True desirability V(u) = quality + synergy
      double desirability = quality + synergy;

      // Use softmax-based probability as acceptance probability
      double acceptanceProb = expDeltas[i];

      // Compute expected payoff: V(u) * softmax(Δ)
      double score = desirability * acceptanceProb;

      preferences[i] = new School(i, score, deltas[i]);
    }

    // Sort schools in descending order by score.
    Arrays.sort(preferences);

    // Select the top 10 schools.
    int[] applications = new int[10];
    for (int i = 0; i < 10; i++) {
      applications[i] = preferences[i].index;
    }

    return applications;
  }
}
