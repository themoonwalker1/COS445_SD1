
import java.util.Arrays;
import java.util.List;

public class Student_angy implements Student {

  private class School implements Comparable<School> {
    public int index;
    public double score;

    public School(int i, double s) {
      index = i;
      score = s;
    }

    // Sort in descending order of score. If tie, compare by index ascending.
    public int compareTo(School other) {
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
      List<Double> synergies) {

    School[] preferences = new School[N];

    // Estimate the average competitor's combined aptitude + synergy
    double avgCompetitor = (S / 2.0) + (W / 2.0);

    // Tune this parameter to be more or less aggressive
    double aggressiveness = 0.5;

    for (int i = 0; i < N; i++) {
      double quality = schools.get(i);
      double synergy = synergies.get(i);

      // Desirability: (Q_u + synergy)
      double desirability = quality + synergy;

      // Compute how far above/below the average competitor we are
      double delta = (aptitude + synergy) - avgCompetitor;

      /*
       * Instead of adding delta directly, we convert it into a [0, 1] scale
       * via a logistic function. This means even if we're a bit below average,
       * we don't necessarily drop the school altogether, and if we're well above average,
       * we still avoid overweighting that advantage too much.
       */
      double competitivenessFactor = 1.0 / (1.0 + Math.exp(-aggressiveness * delta));

      /*
       * Combine desirability with the logistic competitiveness factor.
       * This approach often does better than a purely linear combination,
       * because it smoothly balances "high preference" vs "realistic chance."
       */
      double score = desirability * competitivenessFactor;

      preferences[i] = new School(i, score);
    }

    // Sort schools by this "score" in descending order
    Arrays.sort(preferences);

    // Select the top 10
    int[] ret = new int[10];
    for (int i = 0; i < 10; i++) {
      ret[i] = preferences[i].index;
    }

    return ret;
  }
}
