package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.VcsConnectionProblem;
import org.jetbrains.annotations.Nls;

public class PerforceServerUnavailable extends VcsConnectionProblem {
  public PerforceServerUnavailable(@Nls String stderr) {
    super(stderr);
  }

  public PerforceServerUnavailable(PerforceServerUnavailable cause) {
    super(cause);
  }
}
