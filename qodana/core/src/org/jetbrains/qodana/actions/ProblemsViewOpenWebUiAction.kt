package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.report.BrowserViewProvider

private class ProblemsViewOpenWebUiAction : DumbAwareAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.browserViewProvider != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val browserViewProvider = e.browserViewProvider ?: return

    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      browserViewProvider.openBrowserView()
    }
  }

  private val AnActionEvent.browserViewProvider: BrowserViewProvider?
    get() {
      return this.qodanaProblemsViewModel?.browserViewProviderStateFlow?.value
    }
}