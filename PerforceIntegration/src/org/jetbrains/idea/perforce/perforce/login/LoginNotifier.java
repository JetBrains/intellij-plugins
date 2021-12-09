package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.impl.GenericNotifierImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionProblemsNotifier;

/**
 * @author peter
 */
class LoginNotifier extends GenericNotifierImpl<P4Connection, ConnectionId> {
  private static final String INSPECT = Notifier.INSPECT;
  private static final String FIX = Notifier.FIX;
  private final PerforceLoginManager myLoginManager;

  LoginNotifier(final Project project, final PerforceLoginManager loginManager) {
    super(project, PerforceVcs.NAME, PerforceBundle.message("connection.problems"), NotificationType.ERROR);
    myLoginManager = loginManager;
  }

  @NotNull
  @Override
  @NlsContexts.NotificationContent
  protected String getNotificationContent(final P4Connection connection) {
    return PerforceBundle.message("connection.error.notification", connection.getWorkingDirectory(), FIX, INSPECT);
  }

  @Override
  protected boolean ask(final P4Connection obj, String description) {
    if (FIX.equals(description)) {
      PerforceSettings.getSettings(myProject).USE_LOGIN = true;
      PerforceConnectionManager.getInstance(myProject).updateConnections();
      return myLoginManager.checkAndRepair(obj);
    }
    if (INSPECT.equals(description)) {
      PerforceConnectionProblemsNotifier.showConnectionState(myProject, true);
      return false;
    }
    return false;
  }

  @NotNull
  @Override
  protected ConnectionId getKey(final P4Connection obj) {
    return obj.getId();
  }

}
