package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

/**
 * @author peter
 */
public interface LoginSupport {
  boolean silentLogin(final P4Connection connection) throws VcsException;
  void notLogged(final P4Connection connection);

}
