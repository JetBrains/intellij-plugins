package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public final class AttemptsStateMachine {
  private static final Logger LOG = Logger.getInstance(AttemptsStateMachine.class);
  private static final long ourTimeToRelogin = 3600000;
  private static final long ourSuccessBlindInterval = 600000;
  private static final long ourCredentialsBlindInterval = 1000;
  private static final long ourNetworkBlindInterval = 600000;

  private final LoginPerformer myPerformer;
  private final LoginStateListener myLoginStateListener;

  private boolean myConnectionProblem;
  private @NlsContexts.DialogMessage String myErrorMessage;
  private boolean mySuccess;
  private long myRecentTime;

  private final Object myLock = new Object();

  public AttemptsStateMachine(LoginPerformer performer, LoginStateListener loginStateListener) {
    myPerformer = performer;
    myLoginStateListener = loginStateListener;
    myRecentTime = -1;
  }

  private void fillTime() {
    myRecentTime = System.currentTimeMillis();
  }

  public LoginState login(final String password) {
    LOG.debug("login called");
    return executeUnderLock(() -> myPerformer.login(password));
  }

  private void registerResult(final LoginState state) {
    if ((myRecentTime > 0) && (mySuccess == state.isSuccess()) && (Objects.equals(myErrorMessage, state.getError()) &&
                                                                   (myConnectionProblem == (state.getError() != null)))) {
      if ((System.currentTimeMillis() - myRecentTime) > ourSuccessBlindInterval) {
        fillTime();
      }
      LOG.debug("register result: login state didn't changed");
      return;
    }
    fillTime();
    mySuccess = state.isSuccess();
    myErrorMessage = state.getError();
    myConnectionProblem = myErrorMessage != null;
    LOG.debug("register result: success = " + mySuccess + ", network = " + myConnectionProblem);
  }

  private LoginState checkState() {
    LOG.debug("try checkState");
    final LoginState state = myPerformer.getLoginState();
    if (state.isSuccess()) {
      LOG.debug("login state success");
      final long timeLeft = state.getTimeLeft();
      if (timeLeft > 0 && ourTimeToRelogin > timeLeft) {
        LOG.debug("doing preventing relogin");
        return myPerformer.loginWithStoredPassword();
      }
    }
    return state;
  }

  private LoginState checkLoggedOrSilent() {
    LOG.debug("try checkLoggedOrSilent");
    final LoginState state = checkState();
    if (state.isSuccess()) {
      LOG.debug("login state success (checkLoggedOrSilent)");
      return state;
    }
    LOG.debug("login state not logged");
    if (state.getError() != null) {
      LOG.debug("error not null -> must be connection problem");
      return state;
    }
    LOG.debug("silent login allowed, logging");
    return myPerformer.loginWithStoredPassword();
  }

  private LoginState silentReconnect() {
    LOG.debug("try silent reconnect");
    return myPerformer.loginWithStoredPassword();
  }

  private LoginState ensureImpl(boolean ignoreDelays) {
    try {
      if (myRecentTime == -1) {
        LOG.debug("init state");
        return checkLoggedOrSilent();
      }
      final long time = System.currentTimeMillis() - myRecentTime;
      LOG.debug("ensure, recent time: " + myRecentTime + ", time: " + time + ", ignoreDelays: " + ignoreDelays);
      final boolean inBlindInterval = (!ignoreDelays) && (time < ourSuccessBlindInterval);
      if (mySuccess) {
        LOG.debug("currently success");
        if (inBlindInterval) {
          LOG.debug("success blind interval");
          return LoginState.SUCCESS;
        }
        return checkLoggedOrSilent();
      }

      final LoginState currentState = checkState();
      if (currentState.isSuccess()) {
        LOG.debug("turned out state is success");
        return LoginState.SUCCESS;
      }
      if (! myConnectionProblem) {
        LOG.debug("currently credentials problem");
        if ((! ignoreDelays) && (! myPerformer.isCredentialsChanged()) && (time < ourCredentialsBlindInterval)) {
          LOG.debug("credentials hasn't changed");
          return LoginState.SUCCESS;
        }
        return silentReconnect();
      }
      LOG.debug("currently connection problem");
      if (time < ourNetworkBlindInterval) {
        LOG.debug("connection blind interval");
        return new LoginState(false, -1, currentState.getError() != null ? currentState.getError() : myErrorMessage);
      }
      return silentReconnect();
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (Throwable e) {
      LOG.info(e);
      throw new RuntimeException(e);
    }
  }

  private LoginState executeUnderLock(final Supplier<LoginState> getter) {
    boolean triggerChangesUpdate;
    final LoginState result;
    synchronized (myLock) {
      boolean wasLogged = mySuccess;
      result = getter.get();
      registerResult(result);
      triggerChangesUpdate = !wasLogged && mySuccess;
    }
    if (triggerChangesUpdate) {
      LOG.debug("reconnected");
      myLoginStateListener.reconnected(myPerformer.getMyConnection());
    }
    return result;
  }

  public LoginState ensure(final boolean ignoreDelays) {
    return executeUnderLock(() -> ensureImpl(ignoreDelays));
  }

  public void failed(final boolean connectionProblem, final @Nullable @NlsContexts.DialogMessage String errorMessage) {
    synchronized (myLock) {
      myConnectionProblem = connectionProblem;
      myErrorMessage = errorMessage;
      mySuccess = false;
      fillTime();
      logSuccessOrFailure(false);
    }
  }

  private void logSuccessOrFailure(final boolean success) {
    LOG.debug("Reported: " + (success ? "logged" : "not logged") + ", time: " + myRecentTime);
  }

  public LoginState changePass(String oldPass, String password) {
    LOG.debug("change pass called");
    return executeUnderLock(() -> myPerformer.changePassword(oldPass, password));
  }
}
