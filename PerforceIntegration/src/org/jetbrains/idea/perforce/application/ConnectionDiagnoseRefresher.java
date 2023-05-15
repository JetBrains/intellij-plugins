package org.jetbrains.idea.perforce.application;

import org.jetbrains.idea.perforce.perforce.connections.PerforceMultipleConnections;

public interface ConnectionDiagnoseRefresher {
  void refresh();
  PerforceMultipleConnections getMultipleConnections();
  P4RootsInformation getP4RootsInformation();
}
