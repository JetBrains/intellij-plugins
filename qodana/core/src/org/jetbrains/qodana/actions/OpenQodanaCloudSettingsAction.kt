package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.ui.settings.QodanaCloudSettingsPanel

class OpenQodanaCloudSettingsAction : DumbAwareAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val isVisible = QodanaRegistry.isQodanaCloudIntegrationEnabled
    e.presentation.isVisible = isVisible
    if (!isVisible) return

    e.presentation.text = getActionText()
  }

  private fun getActionText() = when(QodanaCloudStateService.getInstance().userState.value) {
    is UserState.NotAuthorized -> QodanaBundle.message("qodana.open.settings.log.in")
    else -> QodanaBundle.message("qodana.open.settings.other")
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    QodanaCloudSettingsPanel.openSettings(project)
  }
}
