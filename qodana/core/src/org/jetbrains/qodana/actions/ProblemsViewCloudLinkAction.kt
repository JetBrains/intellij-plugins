package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsActions
import icons.QodanaIcons
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService
import org.jetbrains.qodana.ui.link.LinkCloudProjectDialog
import javax.swing.Icon

private class ProblemsViewCloudLinkAction : DefaultActionGroup(), DumbAware {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project ?: return
    val userState = QodanaCloudStateService.getInstance().userState.value
    val linkState = QodanaCloudProjectLinkService.getInstance(project).linkState.value

    e.presentation.text = getActionName(linkState)
    e.presentation.icon = getActionIcon(linkState)
    e.presentation.isPopupGroup = true
    e.presentation.isPerformGroup = true
    e.presentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true)

    if (userState !is UserState.Authorized) {
      e.presentation.isEnabled = false
      return
    }
    if (linkState is LinkState.Linked) {
      e.presentation.isPerformGroup = false
      e.presentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, false)
    }
    e.presentation.isEnabled = true
  }

  @NlsActions.ActionText
  private fun getActionName(linkState: LinkState): String {
    return when(linkState) {
      is LinkState.Linked -> QodanaBundle.message("problems.toolwindow.link.action.linked")
      is LinkState.NotLinked -> QodanaBundle.message("problems.toolwindow.link.action.not.linked")
    }
  }

  private fun getActionIcon(linkState: LinkState): Icon {
    return when(linkState) {
      is LinkState.Linked -> QodanaIcons.Icons.LinkedProject
      is LinkState.NotLinked -> QodanaIcons.Icons.NotLinkedProject
    }
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    return arrayOf(
      OpenQodanaCloudReportAction(),
      LinkCloudProjectAction()
    )
  }

  override fun actionPerformed(e: AnActionEvent) {
    e.project?.let { LinkCloudProjectDialog(it) }?.show()
  }
}