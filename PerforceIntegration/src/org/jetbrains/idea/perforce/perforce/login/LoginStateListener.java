package org.jetbrains.idea.perforce.perforce.login;

import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LoginStateListener {
  private boolean myIsInBatch;
  private final Set<P4Connection> myConnections = com.intellij.util.containers.ContainerUtil.newConcurrentSet();

  public void reconnected(P4Connection connection) {
    if (myIsInBatch) {
      myConnections.add(connection);
    } else {
      notifyListeners(Collections.singleton(connection));
    }
  }

  public void startBatch() {
    myIsInBatch = true;
  }

  public void fireBatchFinished() {
    myIsInBatch = false;
    notifyListeners(myConnections);
    myConnections.clear();
  }

  protected abstract void notifyListeners(Set<P4Connection> connections);
}
