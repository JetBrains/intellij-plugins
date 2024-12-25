package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;
import org.jetbrains.idea.perforce.perforce.PerforceTimeoutException;

import java.io.File;
import java.io.IOException;

public interface P4Connection {

  void runP4Command(PerforcePhysicalConnectionParametersI parameters, String[] p4args, ExecResult retVal, final @Nullable StringBuffer inputStream)
    throws VcsException, PerforceTimeoutException, IOException, InterruptedException;

  ExecResult runP4CommandLine(@NotNull PerforcePhysicalConnectionParametersI settings, final @NonNls String[] strings, final @Nullable StringBuffer stringBuffer)
    throws VcsException;

  ExecResult runP4CommandLine(@NotNull PerforcePhysicalConnectionParametersI settings, @NonNls @NotNull String[] conArgs, @NonNls @NotNull String[] p4args, final @Nullable StringBuffer stringBuffer)
          throws VcsException;

  @NotNull
  ConnectionKey getConnectionKey();

  ConnectionId getId();
  
  default @NotNull String getWorkingDir() {
    return getWorkingDirectory().getPath();
  }

  @NotNull
  File getWorkingDirectory();

  boolean handlesFile(File file);

  boolean isConnected();
}
