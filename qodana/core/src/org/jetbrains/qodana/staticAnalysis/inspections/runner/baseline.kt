package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.google.gson.JsonSyntaxException
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.sarif.createSarifReport
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.TEAMCITY_CHANGES_SCRIPT_NAME
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.notExists

private suspend fun readBaselineReport(
  baseline: String,
  projectPath: Path,
  readResults: Boolean = true,
): SarifReport? {
  val baselineFile = runInterruptible(StaticAnalysisDispatchers.IO) {
    Path(baseline).takeIf(Path::isAbsolute) ?: projectPath.resolve(baseline)
  }

  if (baselineFile.notExists()) {
    return null
  }
  try {
    return runInterruptible(StaticAnalysisDispatchers.IO) {
      SarifUtil.readReport(baselineFile, readResults)
    }
  }
  catch (e: JsonSyntaxException) {
    if (e.cause is IOException) {
      // These simple cases do not need the stack trace of the cause.
      throw JsonSyntaxException("Cannot read baseline report from '${baselineFile}': ${e.cause}")
    }
    throw JsonSyntaxException("Cannot read baseline report from '${baselineFile}': $e", e)
  }
}

internal suspend fun getBaselineReport(config: QodanaConfig, includeResults: Boolean = true): SarifReport? =
  config.baseline?.let { readBaselineReport(config.baseline, config.projectPath, includeResults) }

internal suspend fun applyBaselineCalculation(
  report: SarifReport,
  config: QodanaConfig,
  scope: QodanaAnalysisScope,
  reporter: QodanaMessageReporter
) {
  if (config.baseline == null) return
  val baseline = readBaselineReport(config.baseline, config.projectPath) ?: run {
    reporter.reportError("Can't find baseline report file: ${config.baseline}. Baseline will be calculated against empty report.")
    createSarifReport(emptyList())
  }
  val options = getOptions(scope, config)

  // This mutates the `report` parameter!
  BaselineCalculation.compare(report, baseline, options)
}

private fun getOptions(scope: QodanaAnalysisScope, config: QodanaConfig): BaselineCalculation.Options {
  val limitedScope = config.script.name == TEAMCITY_CHANGES_SCRIPT_NAME || config.script.name == SCOPED_SCRIPT_NAME

  if (!limitedScope) return BaselineCalculation.Options(config.includeAbsent)
  val check: (Result) -> Boolean = { r ->
    r.locations.any {
      val virtualFile = it.physicalLocation?.artifactLocation?.toVirtualFile(config.projectPath)
      if (virtualFile == null) false else scope.contains(virtualFile)
    }
  }

  return BaselineCalculation.Options(config.includeAbsent, true, true, check)
}

private fun ArtifactLocation.toVirtualFile(projectPath: Path): VirtualFile? {
  if (uriBaseId == "SRCROOT") {
    val path = projectPath.resolve(uri)

    return LocalFileSystem.getInstance().findFileByNioFile(path)
  }

  return LocalFileSystem.getInstance().findFileByNioFile(Paths.get(uri))
}
