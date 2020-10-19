package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

/**
 * @author peter
 */
public class PerforcePasswordNotAllowedException extends VcsConnectionProblem {
  private final Project myProject;
  private final P4Connection myConnection;

  public PerforcePasswordNotAllowedException(@Nls String message, Project project, P4Connection connection) {
    super(message);
    myProject = project;
    myConnection = connection;
  }

  @Override
  public boolean attemptQuickFix(boolean mayDisplayDialogs) {
    PerforceSettings mySettings = PerforceSettings.getSettings(myProject);

    if (!mySettings.USE_LOGIN && mySettings.ENABLED) {
      if (mayDisplayDialogs) {
        return PerforceAuthenticationException
          .askEnableLogin(PerforceBundle.message("confirmation.text.password.not.allowed.enable.login"), myProject);
      }
      PerforceLoginManager.getInstance(myProject).ensureNotifyAboutDisabledLogin(myConnection);
    }
    else if (mySettings.USE_LOGIN && mySettings.useP4CONFIG) {
      VcsBalloonProblemNotifier.showOverChangesView(myProject, getMessage(), MessageType.ERROR);
    }

    return false;
  }
}
