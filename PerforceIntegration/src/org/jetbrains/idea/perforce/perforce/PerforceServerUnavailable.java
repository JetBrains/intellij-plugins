package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.vcs.VcsConnectionProblem;

/**
* @author peter
*/
public class PerforceServerUnavailable extends VcsConnectionProblem {
  public PerforceServerUnavailable(String stderr) {
    super(stderr);
  }

  public PerforceServerUnavailable(PerforceServerUnavailable cause) {
    super(cause);
  }
}
