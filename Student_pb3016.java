// Student_pb3016.java: an implementation for Student that uses a combined metric
// to balance desirability (Q_u + synergy) and competitiveness (aptitude + synergy)
// COS 445 SD1

import java.util.Arrays;
import java.util.List;

public class Student_pb3016 implements Student {

  private class School implements Comparable<School> {
    public int index;
    public double metric;

    public School(int i, double m) {
      index = i;
      metric = m;
    }

    // Compare in descending order by metric. In case of a tie, compare by index ascending.
    public int compareTo(School other) {
      int ret = Double.compare(other.metric, this.metric);
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

    // Prepare an array to store each school's index and its computed metric
    School[] preferences = new School[N];

    // Approximate the average competitor's combined aptitude + synergy
    double avgCompetitor = (S / 2.0) + (W / 2.0);

    // Compute the metric for each school and store it
    for (int i = 0; i < N; i++) {
      double quality = schools.get(i);
      double synergy = synergies.get(i);

      // Desirability: (Q_u + synergy)
      double desirability = quality + synergy;

      // Competitiveness: (aptitude + synergy - avgCompetitor)
      double competitiveness = (aptitude + synergy) - avgCompetitor;

      // Final metric = desirability + competitiveness
      double metric = desirability + competitiveness;

      preferences[i] = new School(i, metric);
    }

    // Sort the schools by their metric in descending order
    Arrays.sort(preferences);

    // Select the top 10 schools
    int[] ret = new int[10];
    for (int i = 0; i < 10; i++) {
      ret[i] = preferences[i].index;
    }

    return ret;
  }
}
