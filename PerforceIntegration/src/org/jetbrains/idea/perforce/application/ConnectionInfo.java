package org.jetbrains.idea.perforce.application;

import com.google.common.base.MoreObjects;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceAuthenticationException;
import org.jetbrains.idea.perforce.perforce.PerforceServerUnavailable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConnectionInfo {
  private final Map<String, List<String>> myInfo;
  private final ClientData myClient;
  private final VcsException myException;

  ConnectionInfo(@NotNull Map<String, List<String>> info, @NotNull ClientData client) {
    myInfo = info;
    myClient = client;
    myException = null;
  }

  ConnectionInfo(VcsException exception) {
    myInfo = Collections.emptyMap();
    myClient = new ClientData(Collections.emptyMap());
    this.myException = exception;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConnectionInfo)) return false;

    ConnectionInfo info1 = (ConnectionInfo)o;
    return myClient.equals(info1.myClient) && myInfo.equals(info1.myInfo) && Comparing.equal(myException, info1.myException);
  }

  @Override
  public int hashCode() {
    int result = myInfo.hashCode();
    result = 31 * result + myClient.hashCode();
    if (myException != null) {
      result = 31 * result + myException.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    if (myException != null) {
      return myException.getMessage();
    }

    return MoreObjects.toStringHelper(this)
      .add("info", myInfo)
      .add("client", myClient)
      .toString();
  }

  private static void wrapAndThrow(VcsException vcsException) throws VcsException {
    if (vcsException instanceof PerforceAuthenticationException) {
      throw new PerforceAuthenticationException((PerforceAuthenticationException)vcsException);
    }
    if (vcsException instanceof PerforceServerUnavailable) {
      throw new PerforceServerUnavailable((PerforceServerUnavailable)vcsException);
    }
    throw new VcsException(vcsException);
  }

  Map<String, List<String>> getInfo() throws VcsException {
    if (myException != null) wrapAndThrow(myException);
    return Collections.unmodifiableMap(myInfo);
  }

  ClientData getClient() throws VcsException {
    if (myException != null) wrapAndThrow(myException);
    return myClient;
  }

  boolean hasErrorsBesidesAuthentication() {
    return myException != null && !(myException instanceof PerforceAuthenticationException);
  }
}