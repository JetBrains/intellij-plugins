package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

public class LoginState {
  public static final LoginState SUCCESS = new LoginState(true, -1, null);
  private final boolean mySuccess;
  private final long myTimeLeft;
  private final @NlsContexts.DialogMessage String myError;

  public LoginState(boolean success, long timeLeft, @Nullable @NlsContexts.DialogMessage String error) {
    mySuccess = success;
    myTimeLeft = timeLeft;
    myError = error;
  }

  public boolean isSuccess() {
    return mySuccess;
  }

  public long getTimeLeft() {
    return myTimeLeft;
  }

  public @NlsContexts.DialogMessage String getError() {
    return myError;
  }

  public boolean isPasswordInvalid() {
    return myError != null && myError.contains(PerforceRunner.PASSWORD_INVALID_MESSAGE2);
  }

  @Override
  public String toString() {
    return "LoginState{" +
           "mySuccess=" + mySuccess +
           ", myTimeLeft=" + myTimeLeft +
           ", myError='" + myError + '\'' +
           '}';
  }
}
