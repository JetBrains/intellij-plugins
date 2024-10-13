package org.jetbrains.qodana.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.NlsContexts
import icons.QodanaIcons

sealed class QodanaNotifications private constructor(val groupId: String) {
  data object General : QodanaNotifications("Qodana")

  data object Tips : QodanaNotifications("Qodana Tips")


  data object ProblemsTab : QodanaNotifications("Qodana Problems Tab")

  fun notification(
    @NlsContexts.NotificationTitle title: String?,
    @NlsContexts.NotificationContent content: String,
    notificationType: NotificationType,
    withQodanaIcon: Boolean = false,
  ): Notification {
    val notification = if (title != null) {
      Notification(groupId, title, content, notificationType)
    } else {
      Notification(groupId, content, notificationType)
    }
    if (withQodanaIcon) {
      notification.setIcon(QodanaIcons.Icons.Qodana)
    }
    return notification
  }
}