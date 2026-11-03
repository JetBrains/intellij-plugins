package org.jetbrains.qodana.dart

import com.intellij.ide.analysisignore.AnalysisIgnoreDefaultsSettings
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.SystemProperties
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ConsoleLog
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import kotlin.io.path.exists

private val LOG = logger<QodanaIncludeDartBinDirectory>()
private const val AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY = "qodana.dart.autoreconfigure.analysisignore"

internal class QodanaIncludeDartBinDirectory : QodanaWorkflowExtension {

  // reconfigures AnalysisIgnore settings if repo is detected as a dart repo
  // allows the qodana-dart linter to report issues from bin directory under root
  // setting is then stored in AnalysisIgnoreDefaultsSettings state for future runs
  // no cleanup on purpose, so future runs don't have to reconfigure
  override suspend fun beforeProjectOpened(config: QodanaConfig) {
    if (!SystemProperties.getBooleanProperty(AUTO_RECONFIGURE_ANALYSISIGNORE_PROPERTY, true)) return

    val settings = AnalysisIgnoreDefaultsSettings.getInstance()
    if (!config.projectPath.resolve("pubspec.yaml").exists()) return

    val lines = settings.lines
    if ("/bin/" !in lines) return

    if (settings.isCustomized) {
      ConsoleLog.warn(QodanaDartBundle.message("dart.analyze.warning.bin.excluded", config.projectPath))
      return
    }

    settings.setLines(lines - "/bin/")
    LOG.info("Including the root bin directory in analysis for Dart package at ${config.projectPath}")
  }
}