package org.jetbrains.qodana.dart

import com.intellij.ide.analysisignore.AnalysisIgnoreDefaults
import com.intellij.ide.analysisignore.AnalysisIgnoreDefaultsSettings
import com.intellij.ide.analysisignore.AnalysisIgnoreService
import com.intellij.ide.analysisignore.findAnalysisIgnoreExclusions
import com.intellij.ide.analysisignore.findDefaultExclusions
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.SystemProperties
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ConsoleLog
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import kotlin.io.path.exists

private val LOG = logger<QodanaIncludeDartBinDirectory>()
private const val AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY = "qodana.dart.autoreconfigure.analysisignore"

internal class QodanaIncludeDartBinDirectory : QodanaWorkflowExtension {
  private var analysisIgnoreReconfigured = false

  // reconfigures AnalysisIgnore settings if repo is detected as a dart repo
  // allows the qodana-dart linter to report issues from bin directory under root
  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    if (!SystemProperties.getBooleanProperty(AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY, true)) return
    if (!config.projectPath.resolve("pubspec.yaml").exists()) return

    val binDirectory = LocalFileSystem.getInstance().findFileByNioFile(config.projectPath.resolve("bin"))
      ?.takeIf { it.isDirectory }
      ?: return

    val analysisIgnoreService = AnalysisIgnoreService.getInstance(project)
    analysisIgnoreService.processNow()

    if (findAnalysisIgnoreExclusions(project, binDirectory).isNotEmpty()) {
      ConsoleLog.warn(QodanaDartBundle.message("dart.analyze.warning.bin.excluded", config.projectPath))
      return
    }

    if (findDefaultExclusions(project, binDirectory).isEmpty()) return

    val settings = AnalysisIgnoreDefaultsSettings.getInstance()
    if (settings.isCustomized || "/bin/" !in settings.lines) {
      ConsoleLog.warn(QodanaDartBundle.message("dart.analyze.warning.bin.excluded", config.projectPath))
      return
    }

    settings.setLines(settings.lines - "/bin/")
    analysisIgnoreReconfigured = true
    analysisIgnoreService.processNow()
    LOG.info("Including the root bin directory in analysis for Dart package at ${config.projectPath}")
  }

  override suspend fun afterProjectClosed(config: QodanaConfig) {
    if (!analysisIgnoreReconfigured) return

    AnalysisIgnoreDefaultsSettings.getInstance().setLines(AnalysisIgnoreDefaults.BUILT_IN_LINES)
    analysisIgnoreReconfigured = false
  }
}