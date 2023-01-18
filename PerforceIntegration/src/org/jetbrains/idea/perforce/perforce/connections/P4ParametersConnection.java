package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;

import java.io.File;

public class P4ParametersConnection extends AbstractP4Connection {
  private final P4ConnectionParameters myParameters;
  private final ConnectionId myConnectionId;
  private final File myCwd;

  public P4ParametersConnection(final P4ConnectionParameters parameters, @NotNull final ConnectionId connectionId) {
    myParameters = parameters;
    myConnectionId = connectionId;
    myCwd = myConnectionId.myWorkingDir == null ? new File(".") : new File(myConnectionId.myWorkingDir);
  }

  @Override
  public void runP4Command(PerforcePhysicalConnectionParametersI parameters, String[] p4args, ExecResult retVal, @Nullable StringBuffer inputStream) {
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
    //if parameters are empty, this means that default values from env are used, and all connections effectively have the same server/client/port
    return new ConnectionKey(StringUtil.notNullize(myParameters.getServer()), StringUtil.notNullize(myParameters.getClient()), StringUtil.notNullize(myParameters.getUser()));
  }

  @Override
  public ConnectionId getId() {
    return myConnectionId;
  }

  @Override
  public boolean handlesFile(File file) {
    return !myConnectionId.myUseP4Config || FileUtil.isAncestor(myCwd, file, false);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    P4ParametersConnection that = (P4ParametersConnection)o;

    if (!myConnectionId.equals(that.myConnectionId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myConnectionId.hashCode();
  }

  public P4ConnectionParameters getParameters() {
    return myParameters;
  }

  @Override
  public String toString() {
    return "P4ParametersConnection{" + myParameters + '}' + System.identityHashCode(this);
  }
}
