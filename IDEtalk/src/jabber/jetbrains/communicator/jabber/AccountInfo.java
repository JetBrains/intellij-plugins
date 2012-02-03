/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.jabber;

import jetbrains.communicator.util.StringUtil;

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
    this(StringUtil.getMyUsername(), null, "intellijoin.org", DEFAULT_PORT);
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
    myPassword = StringUtil.toXMLSafeString(encode(password));
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
    StringBuffer result = new StringBuffer(password);
    for (int i = 0; i < password.length(); i ++) {
      result.setCharAt(i, (char) ((((int) result.charAt(i)) ^ ((int) kit.charAt(i % kit.length())))));
    }
    return result.toString();
  }

  private String decode(String password) {
    return encode(StringUtil.fromXMLSafeString(password));
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
