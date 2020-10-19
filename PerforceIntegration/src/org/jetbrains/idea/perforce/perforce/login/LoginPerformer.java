package org.jetbrains.idea.perforce.perforce.login;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public interface LoginPerformer {
  LoginState getLoginState();

  boolean isCredentialsChanged();
  LoginState login(@NotNull String password);
  LoginState loginWithStoredPassword();
  P4Connection getMyConnection();
}
