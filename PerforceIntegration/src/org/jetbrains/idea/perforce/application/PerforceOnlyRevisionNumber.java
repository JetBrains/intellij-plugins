package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;

public class PerforceOnlyRevisionNumber implements VcsRevisionNumber {
  private final long myNumber;

  public PerforceOnlyRevisionNumber(long number) {
    myNumber = number;
  }

  @NotNull
  @Override
  public String asString() {
    return String.valueOf(myNumber);
  }

  @Override
  public int compareTo(VcsRevisionNumber o) {
    if (o instanceof PerforceOnlyRevisionNumber) {
      return new Long(myNumber).compareTo(new Long(((PerforceOnlyRevisionNumber) o).myNumber));
    }
    if (o instanceof PerforceVcsRevisionNumber) {
      return new Long(myNumber).compareTo(new Long(((PerforceVcsRevisionNumber) o).getChangeNumber()));
    }
    return 0;
  }

  public long getNumber() {
    return myNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PerforceOnlyRevisionNumber number = (PerforceOnlyRevisionNumber)o;

    if (myNumber != number.myNumber) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (int)(myNumber ^ (myNumber >>> 32));
  }
}
