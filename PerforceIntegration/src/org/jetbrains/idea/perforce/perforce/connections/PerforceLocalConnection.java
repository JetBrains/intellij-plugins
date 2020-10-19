package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;

import java.io.File;

/**
 * @author irengrig
 */
public class PerforceLocalConnection extends AbstractP4Connection {
  // also singleton but not equal to those SingletonConnection
  private final ConnectionId myConnectionId;
  private final File myCwd;

  public PerforceLocalConnection(@NotNull final String workingDir) {
    myCwd = new File(workingDir);
    myConnectionId = new ConnectionId(P4ConfigHelper.getP4ConfigFileName(), workingDir);
  }

  @Override
  public void runP4Command(PerforcePhysicalConnectionParametersI parameters,
                           String[] p4args,
                           ExecResult retVal,
                           @Nullable StringBuffer inputStream) {
    runP4CommandImpl(parameters, ArrayUtilRt.EMPTY_STRING_ARRAY, p4args, retVal, inputStream);
  }

  @NotNull
  @Override
  public File getWorkingDirectory() {
    return myCwd;
  }

  @NotNull
  @Override
  public ConnectionKey getConnectionKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ConnectionId getId() {
    return myConnectionId;
  }

  @Override
  public boolean handlesFile(File file) {
    return true;
  }
}
