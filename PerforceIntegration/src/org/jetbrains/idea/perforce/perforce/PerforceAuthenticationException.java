package org.jetbrains.idea.perforce.perforce;

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.actions.MessageManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

/**
 * @author irengrig
 */
public class PerforceAuthenticationException extends VcsConnectionProblem {
  private final P4Connection myConnection;
  private final Project myProject;

  public PerforceAuthenticationException(@Nls String message, @NotNull P4Connection connection, Project project) {
    super(message);
    myConnection = connection;
    myProject = project;
  }

  public PerforceAuthenticationException(PerforceAuthenticationException cause) {
    super(cause);
    myConnection = cause.myConnection;
    myProject = cause.myProject;
  }

  @Override
  public boolean attemptQuickFix(boolean mayDisplayDialogs) {
    PerforceSettings settings = PerforceSettings.getSettings(myProject);
    if (!settings.USE_LOGIN && getMessage().contains(PerforceRunner.PASSWORD_INVALID_MESSAGE)) {
      if (mayDisplayDialogs &&
          askEnableLogin(PerforceBundle.message("login.password.invalid.or.unset"), myProject) &&
          PerforceLoginManager.getInstance(myProject).checkAndRepair(myConnection)) {
        return true;
      }

      PerforceLoginManager.getInstance(myProject).ensureNotifyAboutDisabledLogin(myConnection);

      return false;
    }

    if (mayDisplayDialogs) {
      return PerforceLoginManager.getInstance(myProject).checkAndRepair(myConnection);
    }

    PerforceVcs.getInstance(myProject).runBackgroundTask(
      PerforceBundle.message("connection.verifying"), PerformInBackgroundOption.ALWAYS_BACKGROUND,
      () -> {
        try {
          PerforceLoginManager.getInstance(myProject).check(myConnection, true);
        }
        catch (VcsConnectionProblem e) {
          PerforceLoginManager.getInstance(myProject).getNotifier().ensureNotify(myConnection);
        }
      });

    return false;
  }

  public static boolean askEnableLogin(@NlsContexts.DialogMessage String msg, Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return false;
    }

    final int dialogResult = MessageManager.showDialog(project, msg, PerforceBundle.message("dialog.title.perforce"),
                                                       new String[]{CommonBundle.getYesButtonText(), CommonBundle.getNoButtonText(),
                                                         PerforceBundle.message("connection.go.offline")}, 0,
                                                       Messages.getQuestionIcon());
    if (dialogResult == 0) {
      PerforceSettings.getSettings(project).USE_LOGIN = true;
      PerforceConnectionManager.getInstance(project).updateConnections();
      return true;
    }
    if (dialogResult == 2) {
      PerforceSettings.getSettings(project).disable(true);
    }
    return false;
  }
}
