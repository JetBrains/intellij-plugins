package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAware

class ProblemsViewOtherActionsGroup : ActionGroup(), DumbAware {
  init {
    templatePresentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true)
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val project = e?.project ?: return emptyArray()

    val actions = mutableListOf(
      ActionManager.getInstance().getAction("Qodana.RunQodanaAction"),
      ActionManager.getInstance().getAction("Qodana.AddQodanaToCiAction"),

      Separator.getInstance(),

      OpenQodanaCloudOpenInIdeReportAction(),
      ActionManager.getInstance().getAction("Qodana.OpenReportAction")
    ).apply {
      addAll(OpenLocalReportAction.getLocalReportsActions(project))
      add(ActionManager.getInstance().getAction("Qodana.ClearQodanaCacheAction"))
    }.filterNotNull()

    return actions.toTypedArray()
  }
}