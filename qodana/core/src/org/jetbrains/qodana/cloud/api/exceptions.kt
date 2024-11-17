package org.jetbrains.qodana.cloud.api

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.NlsContexts
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.notifications.QodanaNotifications

fun QDCloudResponse.Error.getErrorNotification(@NlsContexts.NotificationTitle title: String): Notification {
  return QodanaNotifications.General.notification(
    title,
    message,
    NotificationType.ERROR
  )
}

val QDCloudResponse.Error.message: String
  get() {
    return when(this) {
      is QDCloudResponse.Error.ResponseFailure -> {
        if (responseCode != null) QodanaBundle.message("qdcloud.server.error", responseCode, errorMessage) else errorMessage
      }
      is QDCloudResponse.Error.Offline -> {
        QodanaBundle.message("qodana.cloud.offline")
      }
    }
  }