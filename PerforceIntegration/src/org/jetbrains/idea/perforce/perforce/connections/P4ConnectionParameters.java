package org.jetbrains.idea.perforce.perforce.connections;

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class P4ConnectionParameters {
  // server + port
  @Nullable
  private String myServer;
  @Nullable
  private String myUser;
  @Nullable
  private String myClient;
  @Nullable
  private String myPassword;
  @Nullable
  private String myCharset;
  @Nullable  
  private String myConfigFileName = P4ConfigHelper.getP4ConfigFileNameFromEnv();
  @Nullable 
  private String myIgnoreFileName = P4ConfigHelper.getP4IgnoreFileName();

  private Throwable myException;
  private final List<String> myWarnings;
  private boolean myNoConfigFound;

  public P4ConnectionParameters() {
    myWarnings = new ArrayList<>();
  }

  private P4ConnectionParameters(@Nullable String server, @Nullable String user, @Nullable String client, @Nullable String password,
                                 @Nullable final String charset) {
    myServer = server;
    myUser = user;
    myClient = client;
    myPassword = password;
    myWarnings = new ArrayList<>();
    myCharset = charset;
  }

  public P4ConnectionParameters(final P4ConnectionParameters parameters) {
    this(parameters.getServer(), parameters.getUser(), parameters.getClient(), parameters.getPassword(), parameters.getCharset());
  }

  public boolean allFieldsDefined() {
    return myUser != null && myServer != null && myClient != null && myPassword != null;
  }

  @Nullable
  public String getServer() {
    return myServer;
  }

  public void setServer(@Nullable String server) {
    myServer = server;
  }

  @Nullable
  public String getUser() {
    return myUser;
  }

  public void setUser(@Nullable String user) {
    myUser = user;
  }

  @Nullable
  public String getClient() {
    return myClient;
  }

  public void setClient(@Nullable String client) {
    myClient = client;
  }

  @Nullable
  public String getPassword() {
    return myPassword;
  }

  public void setPassword(@Nullable String password) {
    myPassword = password;
  }

  public void trySetParams(P4ConnectionParameters parameters) {
    if (myServer == null)
      setServer(parameters.myServer);
    if (myUser == null)
      setUser(parameters.myUser);
    if (myClient == null)
      setClient(parameters.myClient);
    if (myPassword == null)
      setPassword(parameters.myPassword);
  }

  public Throwable getException() {
    return myException;
  }

  public void setException(Throwable exception) {
    myException = exception;
  }

  public boolean hasProblems() {
    return myException != null || (! myWarnings.isEmpty());
  }

  public List<String> getWarnings() {
    return myWarnings;
  }

  public void addWarning(String warning) {
    myWarnings.add(warning);
  }

  @Nullable
  public String getCharset() {
    return myCharset;
  }

  public void setCharset(@Nullable String charset) {
    myCharset = charset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    P4ConnectionParameters that = (P4ConnectionParameters)o;

    if (myClient != null ? !myClient.equals(that.myClient) : that.myClient != null) return false;
    if (myPassword != null ? !myPassword.equals(that.myPassword) : that.myPassword != null) return false;
    if (myServer != null ? !myServer.equals(that.myServer) : that.myServer != null) return false;
    if (myUser != null ? !myUser.equals(that.myUser) : that.myUser != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myServer != null ? myServer.hashCode() : 0;
    result = 31 * result + (myUser != null ? myUser.hashCode() : 0);
    result = 31 * result + (myClient != null ? myClient.hashCode() : 0);
    result = 31 * result + (myPassword != null ? myPassword.hashCode() : 0);
    return result;
  }

  public boolean isNoConfigFound() {
    return myNoConfigFound;
  }

  public void setNoConfigFound(boolean noConfigFound) {
    myNoConfigFound = noConfigFound;
  }

  @Nullable 
  public String getConfigFileName() {
    return myConfigFileName;
  }

  public void setConfigFileName(@Nullable String configFileName) {
    myConfigFileName = configFileName;
  }

  @Nullable public String getIgnoreFileName() {
    return myIgnoreFileName;
  }

  public void setIgnoreFileName(@NotNull String ignoreFileName) {
    myIgnoreFileName = ignoreFileName;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("").omitNullValues()
      .add("myConfigFileName", myConfigFileName)
      .add("myServer", myServer)
      .add("myUser", myUser)
      .add("myClient", myClient)
      .add("myIgnoreFileName", myIgnoreFileName)
      .add("myException", myException)
      .add("myWarnings", myWarnings)
      .add("myNoConfigFound", myNoConfigFound)
      .toString();
  }
}