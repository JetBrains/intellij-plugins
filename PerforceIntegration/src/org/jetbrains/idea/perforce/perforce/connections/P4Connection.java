package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.*;

import java.io.File;
import java.io.IOException;

public interface P4Connection {

  void runP4Command(PerforcePhysicalConnectionParametersI parameters, String[] p4args, ExecResult retVal, @Nullable final StringBuffer inputStream)
    throws VcsException, PerforceTimeoutException, IOException, InterruptedException;

  ExecResult runP4CommandLine(final PerforceSettings settings, @NonNls final String[] strings, @Nullable final StringBuffer stringBuffer)
    throws VcsException;

  ExecResult runP4CommandLine(final PerforceSettings settings, @NonNls @NotNull String[] conArgs, @NonNls @NotNull String[] p4args, @Nullable final StringBuffer stringBuffer)
          throws VcsException;

  @NotNull
  ConnectionKey getConnectionKey();

  ConnectionId getId();
  
  @NotNull
  default String getWorkingDir() {
    return getWorkingDirectory().getPath();
  }

  @NotNull
  File getWorkingDirectory();

  boolean handlesFile(File file);

  boolean isConnected();
}
