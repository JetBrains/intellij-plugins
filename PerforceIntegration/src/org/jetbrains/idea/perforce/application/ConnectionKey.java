package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;

/**
 * There are two main kinds of p4 connections: 
 *   1. singleton, where p4 settings are given in IDEA settings
 *   2. parameterized, where P4CONFIG environment is used, or a special file on disk with server/client/user parameters in it
 *   
 * In the second case, different VCS roots have different {@link org.jetbrains.idea.perforce.perforce.connections.P4ParametersConnection}s.
 * But quite often those connections still have the same server, client and user.
 * So it makes sense to treat those connections as one when querying the server, to execute just one p4 command instead of as many as there are VCS roots
 * Hence {@link org.jetbrains.idea.perforce.perforce.connections.P4Connection#getConnectionKey()} which returns an instance of ConnectionKey.
 * These instances are equal for connections where it's safe to replace many p4 commands with one.
 */
public class ConnectionKey {
  private final String server;
  private final String client;
  private final String user;

  public ConnectionKey(final @NotNull String server, final @NotNull String client, final @NotNull String user) {
    this.server = server;
    this.client = client;
    this.user = user;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ConnectionKey that = (ConnectionKey)o;

    if (!client.equals(that.client)) return false;
    if (!server.equals(that.server)) return false;
    if (!user.equals(that.user)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    result = server.hashCode();
    result = 31 * result + client.hashCode();
    result = 31 * result + user.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return server + ", " + user + "@" + client;
  }

  public @NlsSafe String getServer() {
    return server;
  }

  public String getClient() {
    return client;
  }

  public String getUser() {
    return user;
  }
}
