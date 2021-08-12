package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.IOException;

class PerforceContext {
  private static final int ourVeryLongServerTimeout = 1200 * 1000;
  @NotNull public final P4Connection connection;
  public final boolean longTimeout;
  public final boolean justLogged;

  PerforceContext(@NotNull P4Connection connection) {
    this(connection, false, false);
  }

  PerforceContext(@NotNull P4Connection connection, boolean longTimeout, boolean justLogged) {
    this.connection = connection;
    this.longTimeout = longTimeout;
    this.justLogged = justLogged;
  }

  void runP4Command(PerforceSettings settings, String[] p4args, ExecResult retVal, @Nullable final StringBuffer inputStream)
    throws VcsException, PerforceTimeoutException, IOException, InterruptedException {
    connection.runP4Command(longTimeout ? new LongTimeoutProxy(settings) : settings, p4args, retVal, inputStream);
  }

  private static class LongTimeoutProxy implements PerforcePhysicalConnectionParametersI {
    private final PerforcePhysicalConnectionParametersI myDelegate;

    LongTimeoutProxy(PerforcePhysicalConnectionParametersI delegate) {
      myDelegate = delegate;
    }

    @Override
    public int getServerTimeout() {
      return myDelegate.getServerTimeout() <= 0 ? ourVeryLongServerTimeout : Math.max(ourVeryLongServerTimeout, myDelegate.getServerTimeout());
    }

    @NotNull
    @Override
    public String getCharsetName() {
      return myDelegate.getCharsetName();
    }

    @Override
    public String getPathToExec() {
      return myDelegate.getPathToExec();
    }

    @Override
    public String getPathToIgnore() {
      return myDelegate.getPathToIgnore();
    }

    @Override
    public Project getProject() {
      return myDelegate.getProject();
    }

    @Override
    public void disable() {
      myDelegate.disable();
    }
  }
}
