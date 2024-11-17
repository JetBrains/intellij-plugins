package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.jetbrains.qodana.ui.problemsView.isLocalRunEnabled
import org.jetbrains.qodana.ui.run.wizard.RunQodanaWizard

class RunQodanaAction : DumbAwareAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    if (!isLocalRunEnabled()) {
      e.presentation.isEnabledAndVisible = false
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    RunQodanaWizard.create(project).show()
  }
}