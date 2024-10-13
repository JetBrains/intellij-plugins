package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import kotlinx.coroutines.launch
import org.jetbrains.qodana.cloud.project.QodanaReportDownloader
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.report.QodanaLocalReportsService

class ClearQodanaCacheAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      QodanaReportDownloader.getInstance(project).clearAllReports()
      QodanaLocalReportsService.getInstance(project).clear()
    }
  }
}