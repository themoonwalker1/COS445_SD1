import java.util.Arrays;
import java.util.List;

public class Student_angyman implements Student {

  private class School implements Comparable<School> {
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
    
    // First, compute delta for each school
    for (int i = 0; i < N; i++) {
      double synergy = synergies.get(i);
      // delta = (aptitude + synergy) - ((S + W)/2)
      double delta = (aptitude + synergy) - ((S + W) / 2.0);
      deltas[i] = delta;
    }

    // Compute the minimum and maximum delta from the finite set.
    double dmin = Double.POSITIVE_INFINITY;
    double dmax = Double.NEGATIVE_INFINITY;
    for (double delta : deltas) {
      if (delta < dmin) dmin = delta;
      if (delta > dmax) dmax = delta;
    }

    // Compute the midpoint and logistic slope k.
    double midpoint = (dmin + dmax) / 2.0;
    // Use a fallback in case all delta values are identical.
    double k = (dmax - dmin != 0) ? Math.log(99) / (dmax - dmin) : 1.0;

    // Now compute the overall score for each school.
    for (int i = 0; i < N; i++) {
      double quality = qualities.get(i);    // Q_u for school i
      double synergy = synergies.get(i);      // Sₛ,ᵤ for school i

      // True desirability V(u) = quality + synergy.
      double desirability = quality + synergy;

      // Retrieve the computed delta.
      double delta = deltas[i];

      // Compute acceptance probability using the logistic function:
      // f = 1 / (1 + exp(-k*(delta - midpoint)))
      double acceptanceProb = 1.0 / (1.0 + Math.exp(-k * (delta - midpoint)));

      // Final score: expected payoff = V(u) * acceptance probability.
      double score = desirability * acceptanceProb;

      preferences[i] = new School(i, score, delta);
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
