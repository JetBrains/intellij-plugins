package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.jetbrains.qodana.stats.SetupCiDialogSource
import org.jetbrains.qodana.ui.ci.showSetupCIDialogOrWizardWithYaml
import org.jetbrains.qodana.ui.problemsView.isSetupCiEnabled

internal class AddQodanaToCiAction : DumbAwareAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    if (!isSetupCiEnabled()) {
      e.presentation.isEnabledAndVisible = false
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    showSetupCIDialogOrWizardWithYaml(project, SetupCiDialogSource.TOOLS_LIST)
  }
}