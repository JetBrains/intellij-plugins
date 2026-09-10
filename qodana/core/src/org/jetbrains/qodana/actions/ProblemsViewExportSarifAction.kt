package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VfsUtil.markDirtyAndRefresh
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.jetbrains.qodana.sarif.SarifUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.report.LoadedReport
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

private class ProblemsViewExportSarifAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val uiState = e.qodanaProblemsViewModel?.uiStateFlow?.value
    e.presentation.isEnabled = uiState is QodanaProblemsViewModel.UiState.Loaded
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val reportState = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value
    val selectedState = reportState as? HighlightedReportState.Selected ?: return
    val reportDescriptor = selectedState.highlightedReportData.sourceReportDescriptor

    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      withBackgroundProgress(project, QodanaBundle.message("qodana.export.sarif.progress.title")) {
        val loadedReport = reportDescriptor.loadReport(project)

        val sarifReport = (loadedReport as? LoadedReport.Sarif)?.validatedSarif?.sarif ?: return@withBackgroundProgress

        withContext(QodanaDispatchers.Ui) {
          val descriptor = FileSaverDescriptor(
            QodanaBundle.message("qodana.export.sarif.dialog.title"),
            "",
            "json"
          )
          val saveDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
          val wrapper = saveDialog.save("qodana.sarif.json") ?: return@withContext

          launch(QodanaDispatchers.Default) {
            val file = wrapper.file
            SarifUtil.sortResults(sarifReport)
            SarifUtil.writeReport(file.toPath(), sarifReport)
            markDirtyAndRefresh(true, false, false, file)
          }
        }
      }
    }
  }
}
