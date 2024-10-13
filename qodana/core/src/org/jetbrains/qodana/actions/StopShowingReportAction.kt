package org.jetbrains.qodana.actions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.currentlyHighlightedReportStatsType

internal class StopShowingReportAction : IntentionAction, LowPriorityAction {
  override fun startInWriteAction(): Boolean = false

  override fun getText(): String = QodanaBundle.message("qodana.intentions.stop.showing.report")

  override fun getFamilyName(): String = QodanaBundle.message("qodana.intentions.family.name")

  override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = true

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      logUnhighlightReportStats(project)
      QodanaHighlightedReportService.getInstance(project).highlightReport(null)
    }
  }

  private fun logUnhighlightReportStats(project: Project) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      false,
      currentlyHighlightedReportStatsType(project),
      SourceHighlight.EDITOR_INTENTION
    )
  }
}