package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.NlsActions
import com.intellij.ui.AnimatedIcon
import icons.QodanaIcons
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.ui.settings.QodanaCloudSettingsPanel
import javax.swing.Icon

private class ProblemsViewCloudLoginAction : DumbAwareAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val userState = QodanaCloudStateService.getInstance().userState.value
    e.presentation.text = getActionName(userState)
    e.presentation.icon = getActionIcon(userState)
  }

  @NlsActions.ActionText
  private fun getActionName(userState: UserState): String {
    return when(userState) {
      is UserState.Authorized -> QodanaBundle.message("problems.toolwindow.login.action.authorized")
      is UserState.Authorizing -> QodanaBundle.message("problems.toolwindow.login.action.authorizing")
      is UserState.NotAuthorized -> QodanaBundle.message("qodana.panel.action.log.in")
    }
  }

  private fun getActionIcon(userState: UserState): Icon {
    return when(userState) {
      is UserState.Authorized -> QodanaIcons.Icons.LoggedUser
      is UserState.Authorizing -> AnimatedIcon.Default.INSTANCE
      is UserState.NotAuthorized -> QodanaIcons.Icons.NotLoggedUser
    }
  }

  override fun actionPerformed(e: AnActionEvent) = QodanaCloudSettingsPanel.openSettings(e.project)
}