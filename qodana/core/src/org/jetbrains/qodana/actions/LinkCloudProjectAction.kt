package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceLinkState
import org.jetbrains.qodana.ui.link.LinkCloudProjectDialog

internal class LinkCloudProjectAction : DumbAwareAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val authorized = QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized
    val linkState = e.project?.service<QodanaCloudProjectLinkService>()?.linkState?.value

    val isVisible = (authorized != null && linkState != null && QodanaRegistry.isQodanaCloudIntegrationEnabled)
    e.presentation.isVisible = isVisible
    if (!isVisible) return

    e.presentation.text = getActionText(linkState!!)
  }

  @NlsActions.ActionText
  private fun getActionText(linkState: LinkState): String = when(linkState) {
    is LinkState.Linked -> QodanaBundle.message("qodana.unlink.project.action")
    is LinkState.NotLinked -> QodanaBundle.message("qodana.link.project.action")
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    when(val linkState = QodanaCloudProjectLinkService.getInstance(project).linkState.value) {
      is LinkState.Linked -> {
        linkState.unlink()
        logUnlinkStats(project)
      }
      is LinkState.NotLinked -> {
        LinkCloudProjectDialog(project).show()
      }
    }
  }

  private fun logUnlinkStats(project: Project) {
    QodanaPluginStatsCounterCollector.UPDATE_CLOUD_LINK.log(
      project,
      false,
      SourceLinkState.TOOLS_LIST
    )
  }
}