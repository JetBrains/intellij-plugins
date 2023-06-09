package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.impl.GenericNotifierImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.login.Notifier;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

import java.util.Map;

@Service
public final class PerforceConnectionProblemsNotifier extends GenericNotifierImpl<Object, Object> {
  private static final char NEW_LINE_CHAR = '\n';
  private boolean myConnectionProblems;
  private boolean myParametersProblems;
  private final PerforceSettings mySettings;

  public PerforceConnectionProblemsNotifier(Project project) {
    super(project, PerforceVcs.NAME, PerforceBundle.message("connection.problems"), NotificationType.ERROR);
    myConnectionProblems = myParametersProblems = false;
    mySettings = PerforceSettings.getSettings(myProject);
  }

  @Override
  protected boolean ask(Object obj, String description) {
    if (Notifier.INSPECT.equals(description)) {
      showConnectionState(myProject, false);
      return false;
    }
    else if (Notifier.OFFLINE.equals(description)) {
      mySettings.disable(true);
      return true;
    }
    else if (Notifier.RETRY.equals(description)) {
      return PerforceLoginManager.getInstance(myProject).checkAndRepairAll();
    }
    return false;
  }

  public static void showConnectionState(final Project project, final boolean refreshBefore) {
    final PerforceSettings settings = PerforceSettings.getSettings(project);
    final PerforceBaseInfoWorker perforceBaseInfoWorker = project.getService(PerforceBaseInfoWorker.class);
    final PerforceConnectionManagerI connectionManager = PerforceConnectionManager.getInstance(project);
    if (settings.useP4CONFIG) {
      final ConnectionDiagnoseRefresher refresher = new ConnectionDiagnoseRefresher() {
        @Override
        public void refresh() {
          //settings.enable();  // assume user also meant this
          connectionManager.updateConnections();
          // get to force lazy load, refresh
          final PerforceMultipleConnections object = connectionManager.getMultipleConnectionObject();
          final Map<VirtualFile, P4Connection> allConnections = object.getAllConnections();
          refreshChecker(allConnections, perforceBaseInfoWorker);
        }

        @Override
        public PerforceMultipleConnections getMultipleConnections() {
          return connectionManager.getMultipleConnectionObject();
        }

        @Override
        public P4RootsInformation getP4RootsInformation() {
          return perforceBaseInfoWorker.getCheckerResults();
        }
      };
      final Runnable showDialog = () -> {
        final P4ConfigConnectionDiagnoseDialog dialog =
          new P4ConfigConnectionDiagnoseDialog(project, refresher);
        dialog.show();
      };
      if (refreshBefore) {
        underProgress(project, () -> refresher.refresh(), showDialog);
      } else {
        showDialog.run();
      }
    } else {
      final Runnable showResults = () -> {
        final P4RootsInformation checkerResults = perforceBaseInfoWorker.getCheckerResults();
        showSingleConnectionState(project, checkerResults);
      };
      if (refreshBefore) {
        underProgress(project, () -> refreshChecker(connectionManager.getAllConnections(), perforceBaseInfoWorker), showResults);
      } else {
        showResults.run();
      }
    }
  }

  private static void refreshChecker(final Map<VirtualFile, P4Connection> allConnections, PerforceBaseInfoWorker perforceBaseInfoWorker) {
    perforceBaseInfoWorker.scheduleRefresh();
    if (!allConnections.isEmpty()) {
      final P4Connection connection = allConnections.values().iterator().next();
      try {
        perforceBaseInfoWorker.getInfo(connection);
      }
      catch (VcsException e) {
        //
      }
    }
  }

  private static void underProgress(final Project project, final Runnable runnable, final Runnable onSuccess) {
    ProgressManager.getInstance().run(new Task.Backgroundable(project, PerforceBundle.message("connection.refresh.state"), true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        runnable.run();
      }

      @Override
      public void onSuccess() {
        onSuccess.run();
      }
    });
  }

  public static void showSingleConnectionState(Project project, final P4RootsInformation checkerResults) {
    if (! checkerResults.hasAnyErrors() && ! checkerResults.hasNotAuthorized()) {
      Messages.showMessageDialog(project, PerforceBundle.message("connection.successful"), PerforceBundle.message("connection.state.title"), Messages.getInformationIcon());
    } else {
      final @Nls StringBuilder sb = new StringBuilder(PerforceBundle.message("connection.problems.message")).append(NEW_LINE_CHAR);
      final MultiMap<P4Connection, VcsException> errors = checkerResults.getErrors();
      for (VcsException exception : errors.values()) {
        sb.append(exception.getMessage()).append(NEW_LINE_CHAR);
      }
      if (checkerResults.hasNotAuthorized()) {
        sb.append(NEW_LINE_CHAR).append(PerforceBundle.message("connection.not.logged.in"));
      }
      final Map<P4Connection, PerforceClientRootsChecker.WrongRoots> map = checkerResults.getMap();
      for (PerforceClientRootsChecker.WrongRoots wrongRoots : map.values()) {
        sb.append(NEW_LINE_CHAR)
          .append(PerforceBundle.message("config.wrong.client.spec")).append(NEW_LINE_CHAR)
          .append(PerforceBundle.message("config.client.roots")).append(' ').append(NEW_LINE_CHAR);
        for (String root : wrongRoots.getActualInClientSpec()) {
          sb.append(root).append(NEW_LINE_CHAR);
        }
        sb.append(PerforceBundle.message("config.actual.root")).append(' ').append(NEW_LINE_CHAR);
        for (VirtualFile root : wrongRoots.getWrong()) {
          sb.append(root.getPath()).append(NEW_LINE_CHAR);
        }
      }
      Messages.showMessageDialog(sb.toString(), PerforceBundle.message("connection.problems.title"), Messages.getErrorIcon());
    }
  }

  private void recalculateState() {
    if (mySettings.ENABLED && (myConnectionProblems || myParametersProblems)) {
      if (mySettings.myCanGoOffline && !ApplicationManager.getApplication().isUnitTestMode()) {
        ApplicationManager.getApplication().invokeLater(() -> mySettings.disable(), ModalityState.nonModal());
      }
      ensureNotify(this);
    } else {
      clear();
    }
  }

  public void setProblems(boolean connectionProblems, boolean parametersProblems) {
    myConnectionProblems = connectionProblems;
    myParametersProblems = parametersProblems;
    recalculateState();
  }

  @NotNull
  @Override
  protected Object getKey(Object obj) {
    return obj;
  }

  @NotNull
  @Override
  @NlsContexts.NotificationContent
  protected String getNotificationContent(Object obj) {
    if (myConnectionProblems) {
      return PerforceBundle.message("connection.server.not.available", Notifier.INSPECT, Notifier.RETRY, Notifier.OFFLINE);
    }
    return PerforceBundle.message("connection.server.not.available.inspect", Notifier.INSPECT);
  }
}
