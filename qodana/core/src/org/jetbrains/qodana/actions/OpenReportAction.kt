// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.actions

import com.intellij.json.JsonFileType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.filetype.SarifFileType
import org.jetbrains.qodana.report.openReportFromFileAndHighlight
import org.jetbrains.qodana.settings.qodanaSettings
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType
import org.jetbrains.qodana.ui.OpenReportDialog

internal class OpenReportAction : DumbAwareAction() {

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    e.presentation.isVisible = true
    e.presentation.isEnabled = project != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getData(CommonDataKeys.PROJECT) ?: return
    if (application.qodanaSettings().showPromo) {
      OpenReportDialog(project) {
        chooseAndOpenReport(project)
      }.show()
    }
    else {
      chooseAndOpenReport(project)
    }
  }

  private fun chooseAndOpenReport(project: Project) {
    val descriptor = object : FileChooserDescriptor(
      FileChooserDescriptorFactory.singleFile()
        .withExtensionFilter(QodanaBundle.message("filetype.sarif.chooser.label"), SarifFileType.defaultExtension, JsonFileType.INSTANCE.defaultExtension)
    ) {
      override fun validateSelectedFiles(files: Array<out VirtualFile>) {
        if (!FileTypeManager.getInstance().isFileOfType(files[0], SarifFileType)) {
          throw Exception(QodanaBundle.message("filetype.sarif.chooser.error"))
        }
      }
    }
    FileChooser.chooseFile(descriptor, project, null) { file ->
      project.qodanaProjectScope.launch(QodanaDispatchers.Ui) {
        if (openReportFromFileAndHighlight(project, file.toNioPath()) == null) return@launch

        logHighlightStats(project)
      }
    }
  }

  private fun logHighlightStats(project: Project) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      true,
      StatsReportType.FILE,
      SourceHighlight.TOOLS_SELECT_SARIF_FILE
    )
  }
}
