package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.google.gson.JsonSyntaxException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaseline
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.sarif.OriginalUriBaseId
import org.jetbrains.qodana.staticAnalysis.sarif.createSarifReport
import org.jetbrains.qodana.staticAnalysis.sarif.getOrAssignProperties
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.script.TEAMCITY_CHANGES_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.deleteIfExists
import kotlin.io.path.notExists

private const val BASELINE_SOURCE_PROPERTY = "baselineSource"

private suspend fun readBaselineReport(
  baseline: Path,
  projectPath: Path,
  readResults: Boolean = true,
): SarifReport? {
  val baselineFile = baseline.takeIf(Path::isAbsolute) ?: projectPath.resolve(baseline)

  if (baselineFile.notExists()) {
    return null
  }
  if (!readResults) BaselineHeaderCache.get(baselineFile)?.let { return it }
  try {
    return runInterruptible(StaticAnalysisDispatchers.IO) {
      SarifUtil.readReport(baselineFile, readResults)
    }.also { BaselineHeaderCache.put(baselineFile, it) }
  }
  catch (e: JsonSyntaxException) {
    if (e.cause is IOException) {
      // These simple cases do not need the stack trace of the cause.
      throw JsonSyntaxException("Cannot read baseline report from '${baselineFile}': ${e.cause}")
    }
    throw JsonSyntaxException("Cannot read baseline report from '${baselineFile}': $e", e)
  }
}

/**
 * Keeps the runs of the baseline without their results.
 *
 * The analysis reads the whole baseline to compare, and then reads it again without results for the rule names
 * of the summary; the second read still parses the whole file, half a gigabyte for large projects.
 * An entry is valid while the size and modification time of the file stay the same.
 */
private object BaselineHeaderCache {
  private class Entry(val path: Path, val size: Long, val modified: FileTime, val header: SarifReport)

  @Volatile
  private var entry: Entry? = null

  fun get(path: Path): SarifReport? {
    val cached = entry ?: return null
    return cached.header.takeIf { cached.path == path && cached.size == path.fileSize() && cached.modified == path.getLastModifiedTime() }
  }

  fun put(path: Path, report: SarifReport) {
    val runs = report.runs ?: return
    // Only the tool and the properties of a run are read without results; share them instead of copying.
    val header = SarifReport().withRuns(runs.map { Run().withTool(it.tool).withProperties(it.properties) })
    entry = Entry(path, path.fileSize(), path.getLastModifiedTime(), header)
  }
}

/**
 * Reads the baseline file of the run. It never downloads the cloud baseline.
 * The file of a cloud baseline holds only results: it has no rules and no properties of a run.
 */
internal suspend fun getBaselineReport(config: QodanaConfig, includeResults: Boolean = true): SarifReport? =
  (config.baseline as? QodanaBaseline.Local)?.let { readBaselineReport(it.path, config.projectPath, includeResults) }

internal suspend fun applyBaselineCalculation(
  report: SarifReport,
  config: QodanaConfig,
  scope: QodanaAnalysisScope,
  reporter: QodanaMessageReporter
) {
  val baseline = config.baseline ?: return setBaselineSource(report, "NONE")
  // The cloud baseline downloads here, after the analysis.
  val baselinePath = when (baseline) {
    is QodanaBaseline.Local -> baseline.path
    is QodanaBaseline.Cloud -> baseline.download() ?: return setBaselineSource(report, "NONE")
  }
  try {
    var source = if (baseline is QodanaBaseline.Cloud) "CLOUD" else "LOCAL"
    val baselineReport = readBaselineReport(baselinePath, config.projectPath) ?: run {
      reporter.reportError("Can't find baseline report file: $baselinePath. Baseline will be calculated against empty report.")
      source = "NONE"
      createSarifReport(emptyList())
    }
    val options = getOptions(scope, config)

    // This mutates the `report` parameter!
    BaselineCalculation.compare(report, baselineReport, options)
    setBaselineSource(report, source)
  }
  finally {
    // Only this comparison reads the cloud file.
    if (baseline is QodanaBaseline.Cloud) withContext(NonCancellable + StaticAnalysisDispatchers.IO) { baselinePath.deleteIfExists() }
  }
}

/** Tells which baseline the analysis applied to [report], `CLOUD`, `LOCAL` or `NONE`. */
internal fun appliedBaselineSource(report: SarifReport): Any? = report.runs?.firstOrNull()?.let(::appliedBaselineSource)

/** Tells which baseline the analysis applied to [run], `CLOUD`, `LOCAL` or `NONE`. */
internal fun appliedBaselineSource(run: Run): Any? = run.properties?.get(BASELINE_SOURCE_PROPERTY)

/** Tells whether the analysis compared [run] with a baseline file or with a cloud baseline. */
internal fun Run.hasAppliedBaseline(): Boolean = appliedBaselineSource(this).let { it == "LOCAL" || it == "CLOUD" }

/** Writes to each run of [report] which baseline the analysis applied. */
private fun setBaselineSource(report: SarifReport, source: String) {
  for (run in report.runs.orEmpty()) {
    run.getOrAssignProperties()[BASELINE_SOURCE_PROPERTY] = source
  }
}

private fun getOptions(scope: QodanaAnalysisScope, config: QodanaConfig): BaselineCalculation.Options {
  val limitedScope = config.script.name == TEAMCITY_CHANGES_SCRIPT_NAME || config.script.name == SCOPED_SCRIPT_NAME

  if (!limitedScope) return BaselineCalculation.Options(config.includeAbsent, false)
  val check: (Result) -> Boolean = { r ->
    r.locations.any {
      val virtualFile = it.physicalLocation?.artifactLocation?.toVirtualFile(config.projectPath)
      if (virtualFile == null) false else scope.contains(virtualFile)
    }
  }

  return BaselineCalculation.Options(config.includeAbsent, true, true, check)
}

private fun ArtifactLocation.toVirtualFile(projectPath: Path): VirtualFile? {
  if (uriBaseId == OriginalUriBaseId.SRCROOT.uriBaseId) {
    val path = projectPath.resolve(uri)

    return VirtualFileManager.getInstance().findFileByNioPath(path)
  }

  return VirtualFileManager.getInstance().findFileByNioPath(Paths.get(uri))
}
