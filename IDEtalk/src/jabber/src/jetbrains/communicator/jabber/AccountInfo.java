// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public class AccountInfo {
  private static final int DEFAULT_PORT = 5222;

  private String myUsername;
  private int myPort;
  private String myServer;
  private String myPassword;
  private boolean myForceSSL;
  private boolean myRememberPassword;
  private boolean myShouldLogin = true;

  public AccountInfo() {
    this(CommunicatorStrings.getMyUsername(), null, "jabber.org", DEFAULT_PORT);
  }

  public AccountInfo(String user, String password, String server, int port) {
    myUsername = user;
    myServer = server;
    myPort = port;
    setPassword(password);
  }

  public String getServer() {
    return myServer;
  }

  public String getPassword() {
    return decode(myPassword);
  }

  public int getPort() {
    return myPort;
  }

  public String getUsername() {
    return myUsername;
  }

  public void setUsername(String username) {
    myUsername = username;
  }

  public void setPort(int port) {
    myPort = port;
  }

  public void setServer(String server) {
    myServer = server;
  }

  public final void setPassword(String password) {
    myPassword = CommunicatorStrings.toXMLSafeString(encode(password));
  }

  public boolean isForceSSL() {
    return myForceSSL;
  }

  public void setForceSSL(boolean forceSSL) {
    myForceSSL = forceSSL;
  }

  public boolean shouldRememberPassword() {
    return myRememberPassword;
  }

  public void setRememberPassword(boolean rememberPassword) {
    myRememberPassword = rememberPassword;
  }

  public void setLoginAllowed(final boolean shouldLogin) {
    myShouldLogin = shouldLogin;
  }

  public boolean isLoginAllowed() {
    return myShouldLogin;
  }

  public String toString() {
    return getUsername() + ' ' + myPassword + ' ' + getServer() + ' ' + getPort();
  }

  private String encode(String password) {
    if (password == null) return "";
    String kit = getKit();
    StringBuilder result = new StringBuilder(password);
    for (int i = 0; i < password.length(); i ++) {
      result.setCharAt(i, (char) ((((int) result.charAt(i)) ^ ((int) kit.charAt(i % kit.length())))));
    }
    return result.toString();
  }

  private String decode(String password) {
    return encode(CommunicatorStrings.fromXMLSafeString(password));
  }

  private String getKit() {
    return getUsername() + getUsername();
  }

  public String getJabberId() {
    if (getUsername().indexOf('@') < 0) {
      return getUsername() + "@" + getServer();
    }
    return getUsername();
  }
}
