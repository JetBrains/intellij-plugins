package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.impl.GenericNotifierImpl;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionProblemsNotifier;

public class Notifier {
  @NonNls public static final String INSPECT = "inspect";
  @NonNls public static final String FIX = "fix";
  @NonNls public static final String RETRY = "retry";
  @NonNls public static final String OFFLINE = "offline";

  private final Project myProject;
  private final PerforceSettings mySettings;

  private final MyInnerNotifier myInnerNotifier;

  public Notifier(final Project project, final PerforceLoginManager loginManager, final PerforceSettings perforceSettings) {
    myProject = project;
    mySettings = perforceSettings;
    myInnerNotifier = new MyInnerNotifier(myProject, loginManager);
  }

  private static final class MyInnerNotifier extends GenericNotifierImpl<P4Connection, ConnectionId> {
    private final PerforceLoginManager myLoginManager;

    private MyInnerNotifier(final Project project, final PerforceLoginManager loginManager) {
      super(project, PerforceVcs.NAME, PerforceBundle.message("login.not.logged.in.title"), NotificationType.ERROR);
      myLoginManager = loginManager;
    }

    @NotNull
    @Override
    @NlsContexts.NotificationContent
    protected String getNotificationContent(final P4Connection connection) {
      return PerforceBundle.message("login.error.notification", connection.getWorkingDirectory(), FIX, INSPECT);
    }

    @Override
    protected boolean ask(final P4Connection obj, String description) {
      if (FIX.equals(description)) {
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

  public void ensureNotify(final P4Connection connection) {
    if (! (mySettings.ENABLED && mySettings.USE_LOGIN)) return;

    myInnerNotifier.ensureNotify(connection);
  }

  public void removeLazyNotification(final P4Connection connection) {
    myInnerNotifier.removeLazyNotification(connection);
  }

  public void showPasswordWasOk(final boolean value) {
    if (value) {
      new VcsBalloonProblemNotifier(myProject, PerforceBundle.message("login.successful"), MessageType.INFO).run();
    } else {
      new VcsBalloonProblemNotifier(myProject, PerforceBundle.message("login.not.logged.into.p4"), MessageType.ERROR).run();
    }
  }

  public boolean isEmpty() {
    return myInnerNotifier.isEmpty();
  }

  public void clear() {
    myInnerNotifier.clear();
  }
}
