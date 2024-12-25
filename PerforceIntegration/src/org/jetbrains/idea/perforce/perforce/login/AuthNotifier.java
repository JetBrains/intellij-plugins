package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.impl.GenericNotifierImpl;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionProblemsNotifier;

public class AuthNotifier {
  public static final @NonNls String INSPECT = "inspect";
  public static final @NonNls String FIX = "fix";
  public static final @NonNls String RETRY = "retry";
  public static final @NonNls String OFFLINE = "offline";

  private final Project myProject;
  private final PerforceSettings mySettings;

  private final MyLoginNotifier myLoginNotifier;
  private final MyPasswordNotifier myPasswordNotifier;

  public AuthNotifier(final Project project, final PerforceLoginManager loginManager, final PerforceSettings perforceSettings) {
    myProject = project;
    mySettings = perforceSettings;
    myLoginNotifier = new MyLoginNotifier(myProject, loginManager);
    myPasswordNotifier = new MyPasswordNotifier(myProject, loginManager);
  }

  private static final class MyPasswordNotifier extends GenericNotifierImpl<P4Connection, ConnectionId> {
    private final PerforceLoginManager myLoginManager;

    public MyPasswordNotifier(final @NotNull Project project, final @NotNull PerforceLoginManager loginManager) {
      super(project, PerforceVcs.NAME, PerforceBundle.message("login.password.expired.title"), NotificationType.ERROR);
      myLoginManager = loginManager;
    }

    @Override
    protected boolean ask(P4Connection obj, @Nullable String description) {
      if (FIX.equals(description)) {
        return myLoginManager.checkPasswordExpirationAndRepair(obj);
      }
      if (INSPECT.equals(description)) {
        PerforceConnectionProblemsNotifier.showConnectionState(myProject, true);
        return false;
      }
      return false;
    }

    @Override
    protected ConnectionId getKey(P4Connection obj) {
      return null;
    }

    @Override
    protected @NotNull @NlsContexts.NotificationContent String getNotificationContent(P4Connection connection) {
      HtmlBuilder builder = new HtmlBuilder();
      builder.append(
        HtmlChunk.raw(PerforceBundle.message("login.password.expired", connection.getWorkingDirectory()))
      );
      builder.append(HtmlChunk.br());
      builder.append(HtmlChunk.link(FIX, PerforceBundle.message("login.fix")));
      builder.append(HtmlChunk.br());
      builder.append(HtmlChunk.link(INSPECT, PerforceBundle.message("login.inspect")));
      return builder.toString();
    }
  }

  private static final class MyLoginNotifier extends GenericNotifierImpl<P4Connection, ConnectionId> {
    private final PerforceLoginManager myLoginManager;

    private MyLoginNotifier(final Project project, final PerforceLoginManager loginManager) {
      super(project, PerforceVcs.NAME, PerforceBundle.message("login.not.logged.in.title"), NotificationType.ERROR);
      myLoginManager = loginManager;
    }

    @Override
    protected @NotNull @NlsContexts.NotificationContent String getNotificationContent(@NotNull P4Connection connection) {
      HtmlBuilder builder = new HtmlBuilder();
      builder.append(
        HtmlChunk.raw(PerforceBundle.message("login.error.notification", connection.getWorkingDirectory()))
      );
      return builder.toString();
    }

    @Override
    protected void configureNotification(@NotNull Notification notification, @NotNull P4Connection connection) {

      NotificationAction fixAction = NotificationAction.createSimple(PerforceBundle.message("login.fix"), FIX,
                                                               () -> {
                                                                 if (myLoginManager.checkAndRepair(connection)) notification.expire();
                                                               });
      notification.addAction(fixAction);
      NotificationAction inspectAction =
        NotificationAction.createSimple(PerforceBundle.message("login.inspect"), INSPECT,
                                        () -> PerforceConnectionProblemsNotifier.showConnectionState(myProject, true));
      notification.addAction(inspectAction);
    }

    @Override
    protected boolean ask(final P4Connection obj, String description) {
      return false;
    }

    @Override
    protected @NotNull ConnectionId getKey(final P4Connection obj) {
      return obj.getId();
    }

  }

  public void ensureNotify(final P4Connection connection, final VcsConnectionProblem exception) {
    if (! (mySettings.ENABLED && mySettings.USE_LOGIN)) return;

    if (exception.getMessage().contains(PerforceRunner.PASSWORD_EXPIRED)) {
      myPasswordNotifier.ensureNotify(connection);
    }
    else {
      myLoginNotifier.ensureNotify(connection);
    }
  }

  public void removeLazyNotification(final P4Connection connection) {
    myLoginNotifier.removeLazyNotification(connection);
  }

  public void showPasswordWasOk(final boolean value) {
    if (value) {
      new VcsBalloonProblemNotifier(myProject, PerforceBundle.message("login.successful"), MessageType.INFO).run();
    } else {
      new VcsBalloonProblemNotifier(myProject, PerforceBundle.message("login.not.logged.into.p4"), MessageType.ERROR).run();
    }
  }

  public boolean isEmpty() {
    return myLoginNotifier.isEmpty();
  }

  public void clear() {
    myLoginNotifier.clear();
    myPasswordNotifier.clear();
  }
}
