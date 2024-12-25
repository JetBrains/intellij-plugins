/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4Revision;

import java.util.Objects;

public class PerforceVcsRevisionNumber implements VcsRevisionNumber {
  private final long myRevisionNumber;
  private final long myChangeNumber;
  private final boolean myBranched;
  private final String myDepotPath;

  public PerforceVcsRevisionNumber(final long revisionNumber, final long changeNumber, final boolean branched, @NotNull String depotPath) {
    myRevisionNumber = revisionNumber;
    myChangeNumber = changeNumber;
    myBranched = branched;
    myDepotPath = depotPath;
  }

  public PerforceVcsRevisionNumber(final P4Revision p4Revision) {
    this(p4Revision.getRevisionNumber(), p4Revision.getChangeNumber(), p4Revision.isBranched(), p4Revision.getDepotPath());
  }

  public static @Nullable PerforceVcsRevisionNumber createFromFStat(FStat fstat) {
    try {
      return new PerforceVcsRevisionNumber(java.lang.Long.parseLong(fstat.haveRev) , java.lang.Long.parseLong(fstat.headChange), false, fstat.depotFile);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public @NotNull String asString() {
    return String.valueOf(myChangeNumber);
  }

  @Override
  public int compareTo(final VcsRevisionNumber o) {
    if(o instanceof PerforceVcsRevisionNumber) {
      return java.lang.Long.signum(myChangeNumber - ((PerforceVcsRevisionNumber)o).myChangeNumber);
    }
    if (o instanceof PerforceOnlyRevisionNumber) {
      return java.lang.Long.signum(myRevisionNumber - ((PerforceOnlyRevisionNumber) o).getNumber());
    }
    return 0;
  }

  public @NotNull String getDepotPath() {
    return myDepotPath;
  }

  public long getRevisionNumber() {
    return myRevisionNumber;
  }

  public long getChangeNumber() {
    return myChangeNumber;
  }

  public boolean isBranched() {
    return myBranched;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PerforceVcsRevisionNumber that = (PerforceVcsRevisionNumber)o;

    if (myChangeNumber != that.myChangeNumber) return false;
    return myRevisionNumber == that.myRevisionNumber && Objects.equals(myDepotPath, that.myDepotPath);

  }

  @Override
  public int hashCode() {
    int result;
    result = java.lang.Long.hashCode(myRevisionNumber);
    result = 29 * result + java.lang.Long.hashCode(myChangeNumber);
    if (myDepotPath != null) {
      result = 29 * result + myDepotPath.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return java.lang.Long.toString(myChangeNumber);
  }
}
