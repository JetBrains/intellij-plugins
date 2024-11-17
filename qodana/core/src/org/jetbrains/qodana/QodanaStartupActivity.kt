package org.jetbrains.qodana

import com.intellij.ide.util.propComponentProperty
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.EditorBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.stats.SourceLinkState

internal class QodanaStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    val notification = getApplyDefaultCloudProjectNotification(project) ?: return
    val job = project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
        QodanaCloudProjectLinkService.getInstance(project).linkState
          .filterIsInstance<LinkState.Linked>()
          .onEach { notification.expire() }
          .first()
    }
    notification.whenExpired {
      job.cancel()
    }
    notification.notify(project)
  }

  private fun getApplyDefaultCloudProjectNotification(project: Project): Notification? {
    val settingsService = QodanaIntelliJYamlService.getInstance(project)
    if (settingsService.disableApplyDefaultCloudProjectNotification) {
      return null
    }

    var apply: Boolean by propComponentProperty(project, "qodana.show.apply.default.cloud.project.notification", defaultValue = true)
    if (!apply) return null

    val defaultLinkPrimaryData = settingsService.cloudProjectPrimaryData ?: return null
    if (QodanaCloudProjectLinkService.getInstance(project).linkState.value !is LinkState.NotLinked) return null

    @Suppress("DialogTitleCapitalization") val notification = QodanaNotifications.General.notification(
      QodanaBundle.message("notification.title.qodana.cloud"),
      QodanaBundle.message("notification.login.default.cloud.project"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    )

    when(val userState = QodanaCloudStateService.getInstance().userState.value) {
      is UserState.NotAuthorized -> {
        notification.addAction(NotificationAction.createSimpleExpiring(QodanaBundle.message("notification.action.login.default.cloud.project")) {
          userState.authorize(userState.selfHostedFrontendUrl)
        })
      }
      is UserState.Authorized -> {
        notification.setContent(QodanaBundle.message("notification.link.default.cloud.project"))
        notification.addAction(NotificationAction.createSimpleExpiring(QodanaBundle.message("notification.action.link.default.cloud.project")) {
          project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
            linkWithCloudProjectAndApply(
              project,
              CloudProjectData(defaultLinkPrimaryData, CloudProjectProperties(null)),
              SourceLinkState.AUTO_LINK
            )
          }
        })
      }
      is UserState.Authorizing -> return null
    }
    notification.addAction(NotificationAction.createSimpleExpiring(EditorBundle.message("notification.dont.show.again.message")) {
      apply = false
    })
    return notification
  }
}