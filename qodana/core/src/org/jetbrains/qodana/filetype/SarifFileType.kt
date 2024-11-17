package org.jetbrains.qodana.filetype

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileTypes.INativeFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icons.QodanaIcons
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.report.openReportFromFileAndHighlight
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType
import javax.swing.Icon

object SarifFileType : JsonFileType(), INativeFileType {
  private const val DEFAULT_EXTENSION = "sarif"

  override fun getName(): String = "SARIF"

  override fun getDescription(): String = QodanaBundle.message("filetype.sarif.description")

  override fun getDefaultExtension(): String = DEFAULT_EXTENSION

  override fun getIcon(): Icon = QodanaIcons.Icons.Sarif

  override fun openFileInAssociatedApplication(project: Project?, file: VirtualFile): Boolean  {
    if (project == null) return false
    if (QodanaRegistry.openSarifInEditor) {
      return OpenFileDescriptor(project, file).navigateInEditor(project, true)
    }

    project.qodanaProjectScope.launch(QodanaDispatchers.Ui) {
      if (openReportFromFileAndHighlight(project, file.toNioPath()) == null) return@launch

      logHighlightStats(project)
    }
    return true
  }

  private fun logHighlightStats(project: Project) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      true,
      StatsReportType.FILE,
      SourceHighlight.SARIF_FILE
    )
  }

  override fun useNativeIcon(): Boolean = false
}