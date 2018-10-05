package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.util.Pair;

import java.util.List;

public class OutlineStepSubstitution {
  private final String substitution;
  private final List<? extends Pair<Integer, Integer>> offsets;

  public OutlineStepSubstitution(String substitution) {
    this(substitution, null);
  }

  public OutlineStepSubstitution(String substitution,
                                 List<? extends Pair<Integer, Integer>> offsets) {
    this.substitution = substitution;
    this.offsets = offsets;
  }

  public int getOffsetInOutlineStep(int offsetInSubstitutedStep) {
    if (offsets == null) {
      return offsetInSubstitutedStep;
    }
    int i = 0;
    int shift = 0;
    while (i < offsets.size() && offsets.get(i).first < offsetInSubstitutedStep) {
      shift += offsets.get(i).second;
      i++;
    }
    return offsetInSubstitutedStep + shift;
  }

  public String getSubstitution() {
    return substitution;
  }
}
