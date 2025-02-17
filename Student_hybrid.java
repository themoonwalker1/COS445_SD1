import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Student_hybrid implements Student {

  // Helper class to hold information about each school candidate.
  private class SchoolCandidate {
    int index;
    double quality;    // schools.get(index)
    double synergy;    // synergies.get(index)
    double overall;    // quality + synergy

    SchoolCandidate(int index, double quality, double synergy) {
      this.index = index;
      this.quality = quality;
      this.synergy = synergy;
      this.overall = quality + synergy;
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

    // Build the list of candidates.
    List<SchoolCandidate> candidates = new ArrayList<>();
    for (int i = 0; i < schools.size(); i++) {
      candidates.add(new SchoolCandidate(i, schools.get(i), synergies.get(i)));
    }

    // Optionally, one could compute a hybrid weight if desired.
    // double alpha = T / (T + W);  // weight on quality vs. synergy.

    // --- Safety Picks: Identify schools with the highest synergy scores.
    // Create a copy of candidates sorted by synergy (descending).
    List<SchoolCandidate> bySynergy = new ArrayList<>(candidates);
    Collections.sort(bySynergy, new Comparator<SchoolCandidate>() {
      @Override
      public int compare(SchoolCandidate a, SchoolCandidate b) {
        return Double.compare(b.synergy, a.synergy);
      }
    });
    // Select the top 2 as safety picks.
    List<SchoolCandidate> safeties = new ArrayList<>();
    int safetyCount = Math.min(2, bySynergy.size());
    for (int i = 0; i < safetyCount; i++) {
      safeties.add(bySynergy.get(i));
    }

    // Remove safety picks from the candidate list.
    List<SchoolCandidate> remaining = new ArrayList<>();
    for (SchoolCandidate cand : candidates) {
      boolean isSafety = false;
      for (SchoolCandidate safety : safeties) {
        if (cand.index == safety.index) {
          isSafety = true;
          break;
        }
      }
      if (!isSafety) {
        remaining.add(cand);
      }
    }

    // --- Reach and Target Picks: Use overall score (quality + synergy).
    Collections.sort(remaining, new Comparator<SchoolCandidate>() {
      @Override
      public int compare(SchoolCandidate a, SchoolCandidate b) {
        return Double.compare(b.overall, a.overall);
      }
    });

    // The top 4 remaining are considered Reach picks.
    List<SchoolCandidate> reaches = new ArrayList<>();
    int reachCount = Math.min(4, remaining.size());
    for (int i = 0; i < reachCount; i++) {
      reaches.add(remaining.get(i));
    }

    // The next 4 are Target picks.
    List<SchoolCandidate> targets = new ArrayList<>();
    int targetCount = Math.min(4, remaining.size() - reachCount);
    for (int i = reachCount; i < reachCount + targetCount; i++) {
      targets.add(remaining.get(i));
    }

    // --- Final List: Order as Reach picks first, then Targets, then Safeties.
    List<SchoolCandidate> finalList = new ArrayList<>();

    // Sort reaches by overall descending.
    Collections.sort(reaches, new Comparator<SchoolCandidate>() {
      @Override
      public int compare(SchoolCandidate a, SchoolCandidate b) {
        return Double.compare(b.overall, a.overall);
      }
    });
    finalList.addAll(reaches);

    // Sort targets by overall descending.
    Collections.sort(targets, new Comparator<SchoolCandidate>() {
      @Override
      public int compare(SchoolCandidate a, SchoolCandidate b) {
        return Double.compare(b.overall, a.overall);
      }
    });
    finalList.addAll(targets);

    // Sort safeties by synergy descending.
    Collections.sort(safeties, new Comparator<SchoolCandidate>() {
      @Override
      public int compare(SchoolCandidate a, SchoolCandidate b) {
        return Double.compare(b.synergy, a.synergy);
      }
    });
    finalList.addAll(safeties);

    // Ensure the final list has exactly 10 schools.
    // If finalList.size() < 10, fill in with the remaining candidates.
    if (finalList.size() < 10) {
      boolean[] used = new boolean[schools.size()];
      for (SchoolCandidate cand : finalList) {
        used[cand.index] = true;
      }
      for (SchoolCandidate cand : candidates) {
        if (!used[cand.index]) {
          finalList.add(cand);
          if (finalList.size() == 10) {
            break;
          }
        }
      }
    }

    // Build the result array with the indices of the 10 chosen schools.
    int[] result = new int[10];
    for (int i = 0; i < 10; i++) {
      result[i] = finalList.get(i).index;
    }
    return result;
  }
}
