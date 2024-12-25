package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.PerforceAuthenticationException;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;

import java.util.*;

@Service(Service.Level.PROJECT)
public final class PerforceLoginManager implements LoginSupport {
  private static final Logger LOG = Logger.getInstance(PerforceLoginManager.class);
  private final AuthNotifier myAuthNotifier;
  private final DisabledLoginNotifier myDisabledLoginNotifier;

  private final Object myMapLock = new Object();
  private final Map<P4Connection, AttemptsStateMachine> myState = new HashMap<>();
  private final PerforceSettings mySettings;
  private final Project myProject;
  private final PerforceConnectionManagerI myConnectionManager;
  private final LoginStateListener myLoginStateListener;
  private final List<Runnable> mySuccessfulLoginListeners = ContainerUtil.createLockFreeCopyOnWriteList();

  public PerforceLoginManager(Project project) {
    myProject = project;
    myConnectionManager = project.getService(PerforceConnectionManagerI.class);

    mySettings = PerforceSettings.getSettings(project);
    myAuthNotifier = new AuthNotifier(myProject, this, mySettings);
    myDisabledLoginNotifier = new DisabledLoginNotifier(project, this);
    myLoginStateListener = new LoginStateListener() {
      @Override
      protected void notifyListeners(Set<P4Connection> connections) {
        for (P4Connection connection : connections) {
          myAuthNotifier.removeLazyNotification(connection);
          myDisabledLoginNotifier.removeLazyNotification(connection);
        }
        VcsDirtyScopeManager.getInstance(project).markEverythingDirty();
        for (Runnable listener : mySuccessfulLoginListeners) {
          listener.run();
        }
      }
    };
  }

  public static PerforceLoginManager getInstance(final Project project) {
    return project.getService(PerforceLoginManager.class);
  }

  public void addSuccessfulLoginListener(final Runnable runnable) {
    mySuccessfulLoginListeners.add(runnable);
  }

  public void clearAll() {
    synchronized (myMapLock) {
      myAuthNotifier.clear();
      myState.clear();
    }
  }

  public void refreshLoginState() {
    if (!loginPingAllowed()) {
      return;
    }

    myLoginStateListener.startBatch();
    for (P4Connection connection : mySettings.getAllConnections()) {
      getOrCreate(connection).ensure(true);
    }
    myLoginStateListener.fireBatchFinished();
  }

  private boolean loginPingAllowed() {
    return mySettings.ENABLED && mySettings.USE_LOGIN;
  }

  @Override
  public boolean silentLogin(P4Connection connection) throws VcsException {
    LOG.debug("silent login called");
    if (!loginPingAllowed()) {
      LOG.debug("ping is NOT allowed");
      return false;
    }
    final AttemptsStateMachine machine = getOrCreate(connection);
    final LoginState state = machine.ensure(true);
    if (state.isSuccess()) {
      myAuthNotifier.removeLazyNotification(connection);
      return true;
    }

    // don't show login error notification when network is down - not a problem of login
    if (state.getError() != null) {
      throw new VcsException(state.getError());
    }
    return false;
  }

  public boolean check(final P4Connection connection, boolean forceCheck) throws VcsConnectionProblem {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      ApplicationManager.getApplication().assertIsNonDispatchThread();
    }

    if (!loginPingAllowed()) return false;

    final AttemptsStateMachine machine = getOrCreate(connection);
    final LoginState state = machine.ensure(forceCheck);
    if (state.isSuccess()) {
      myAuthNotifier.removeLazyNotification(connection);
      return true;
    }
    // don't show login error notification when network is down - not a problem of login
    if (state.getError() != null) {
      throw new VcsConnectionProblem(state.getError());
    }

    throw new PerforceAuthenticationException(PerforceBundle.message("perforce.authentication.problem"), connection, myProject);
  }

  public boolean checkAndRepair(final P4Connection connection) {
    ThreadingAssertions.assertEventDispatchThread();

    if (!loginPingAllowed()) {
      return true;
    }

    final AttemptsStateMachine machine = getOrCreate(connection);

    // check if already logged in
    {
      final LoginState state = runUnderProgress(PerforceBundle.message("login.checking.auth.state"), () -> machine.ensure(true));
      if (state == null) {
        return false;
      }

      if (state.isSuccess()) {
        myAuthNotifier.removeLazyNotification(connection);
        myConnectionManager.updateConnections();
        return true;
      }
      if (reportConnectionError(state)) {
        return false;
      }
    }

    // try to log in with the saved password
    return runUnderProgress(PerforceBundle.message("login.getting.credentials"), () -> {
      String password = mySettings.getPasswd();
      if (StringUtil.isNotEmpty(password)) {
        final LoginState newLoginState = loginUnderProgress(machine, password);
        if (newLoginState == null) {
          return false;
        }

        if (checkLoginState(newLoginState, connection)) {
          return true;
        }
      }

      return askUserForPassword(connection, machine);
    });
  }

  public boolean checkPasswordExpirationAndRepair(final P4Connection connection) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    if (!loginPingAllowed()) {
      return true;
    }

    final AttemptsStateMachine machine = getOrCreate(connection);
    final LoginState state = runUnderProgress(PerforceBundle.message("login.checking.auth.state"), () -> machine.ensure(true));
    if (state.getError() != null && state.getError().contains(PerforceRunner.PASSWORD_EXPIRED)) {
      if (!mySettings.requestForPasswordUpdate(machine)) {
        return false;
      }

      final LoginState newLoginState = runUnderProgress(PerforceBundle.message("login.checking.auth.state"), () -> machine.ensure(true));
      if (newLoginState == null) {
        return false;
      }

      if (checkLoginState(newLoginState, connection)) {
        return true;
      }
      if (reportConnectionError(newLoginState)) {
        myAuthNotifier.showPasswordWasOk(false);
        return false;
      }
    }

    return checkAndRepair(connection);
  }

  private boolean askUserForPassword(P4Connection connection, final AttemptsStateMachine machine) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return false;
    }

    while (true) {
      final String newPassword = mySettings.requestForPassword(myConnectionManager.isSingletonConnectionUsed() ? null : connection);
      if (newPassword == null) {
        return false;
      }
      if (StringUtil.isEmpty(newPassword)) {
        Messages.showInfoMessage(myProject, PerforceBundle.message("login.empty.password.restricted"), PerforceBundle.message("login.auth.problem"));
        continue;
      }
      final LoginState newLoginState = loginUnderProgress(machine, newPassword);
      if (newLoginState == null) {
        return false;
      }
      if (checkLoginState(newLoginState, connection)) {
        return true;
      }
      if (reportConnectionError(newLoginState)) {
        myAuthNotifier.showPasswordWasOk(false);
        return false;
      }
    }
  }

  private boolean checkLoginState(LoginState newLoginState, P4Connection connection) {
    if (newLoginState.isSuccess()) {
      myAuthNotifier.showPasswordWasOk(true);
      myAuthNotifier.removeLazyNotification(connection);
      myConnectionManager.updateConnections();
      return true;
    }

    return false;
  }

  private @Nullable LoginState loginUnderProgress(final AttemptsStateMachine machine, final String newPassword) {
    return runUnderProgress(PerforceBundle.message("login"), () -> machine.login(newPassword));
  }

  private boolean reportConnectionError(LoginState state) {
    if (state.isPasswordInvalid()) {
      return false;
    }

    // don't show login error notification when network is down - not a problem of login
    String error = state.getError();
    if (error != null) {
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        Messages.showWarningDialog(myProject, error, PerforceBundle.message("connection.cannot.connect"));
      }

      return true;
    }
    return false;
  }

  public void ensureNotifyAboutDisabledLogin(P4Connection connection) {
    myDisabledLoginNotifier.ensureNotify(connection);
  }

  private <T> T runUnderProgress(@NlsContexts.ProgressTitle String progressTitle, final Computable<T> computable) {
    return ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
      ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
      try {
        return computable.compute();
      }
      catch (ProcessCanceledException e) {
        return null;
      }
    }, progressTitle, true, myProject);
  }

  public boolean checkAndRepairAll() {
    Collection<P4Connection> connections = runUnderProgress(PerforceBundle.message("login.determining.credentials"),
                                                            () -> mySettings.getConnectionsByKeys().values());
    if (connections == null) {
      return false;
    }

    for (P4Connection connection : connections) {
      if (!checkAndRepair(connection)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void notLogged(final P4Connection connection) {
    final AttemptsStateMachine machine = getOrCreate(connection);
    machine.failed(false, null);
  }

  private AttemptsStateMachine getOrCreate(final P4Connection connection) {
    synchronized (myMapLock) {
      final AttemptsStateMachine machine = myState.get(connection);
      if (machine != null) return machine;

      final AttemptsStateMachine newMachine = new AttemptsStateMachine(new LoginPerformerImpl(myProject, connection, myConnectionManager),
                                                                       myLoginStateListener);
      myState.put(connection, newMachine);
      return newMachine;
    }
  }

  public void startListening(@NotNull Disposable parentDisposable) {
    if (PerforceLoginTicketsListener.shouldRegister()) {
      PerforceLoginTicketsListener myPerforceLoginTicketsListener = new PerforceLoginTicketsListener(myProject, this, parentDisposable);
      myPerforceLoginTicketsListener.pingListening();
      VirtualFileManager.getInstance().addVirtualFileListener(myPerforceLoginTicketsListener, parentDisposable);
    }
  }

  public AuthNotifier getNotifier() {
    return myAuthNotifier;
  }
}
