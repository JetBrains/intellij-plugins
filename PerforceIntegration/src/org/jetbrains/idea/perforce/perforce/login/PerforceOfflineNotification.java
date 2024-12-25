package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.impl.GenericNotifierImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

public class PerforceOfflineNotification extends GenericNotifierImpl<Object, Object> {

  public PerforceOfflineNotification(final Project project) {
    super(project, PerforceVcs.NAME, PerforceBundle.message("connection.offline"), NotificationType.WARNING);
  }

  @Override
  protected boolean ask(Object obj, String description) {
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    if (settings.ENABLED) return true;
    settings.enable();
    return settings.ENABLED;
  }

  @Override
  protected @NotNull Object getKey(Object obj) {
    return obj;
  }

  @Override
  protected @NotNull @NlsContexts.NotificationContent String getNotificationContent(Object obj) {
    return PerforceBundle.message("connection.work.offline");
  }
}
