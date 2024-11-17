package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.components.serviceAsync
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.Options
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.*
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.notExists

private const val SCOPE_ARG = "scope-file"
const val SCOPED_SCRIPT_NAME = "scoped"
internal const val SCOPED_BASELINE_PROPERTY = "qodana.scoped.baseline.path"
internal const val COVERAGE_SKIP_REPORTING_PROPERTY = "qodana.skip.coverage.issues.reporting"
internal const val COVERAGE_SKIP_COMPUTATION_PROPERTY = "qodana.skip.coverage.computation"

internal class ScopedScriptFactory : QodanaScriptFactory {
  override val scriptName get() = SCOPED_SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> =
    if (parameters.isBlank()) {
      throw QodanaException("Cannot start $scriptName script without scope file")
    }
    else {
      mapOf(SCOPE_ARG to parameters)
    }

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters,
  ): QodanaScript {
    val path = run {
      val p = Path(parameters.require<String>(SCOPE_ARG))
      if (p.isAbsolute) p else config.projectPath.resolve(p)
    }
    if (path.notExists()) throw QodanaException("Scope file $path does not exist")

    val runContextFactory = ScopedRunContextFactory(contextFactory, path, config)
    return ScopedScript(runContextFactory)
  }
}

internal class ScopedScript(runContextFactory: ScopedRunContextFactory) :
  DefaultScript(runContextFactory, AnalysisKind.INCREMENTAL) {
  override suspend fun execute(report: SarifReport, run: Run, runContext: QodanaRunContext, inspectionContext: QodanaGlobalInspectionContext) {
    runContext.runAnalysis(context = inspectionContext)
    run.results = runContext.getResultsForInspectionGroup(inspectionContext)

    // compare before and current, keeping only 'NEW' issues in current
    val baselineReport = getScopedBaselineReport(runContext)
    if (baselineReport != null) {
      BaselineCalculation.compare(report, baselineReport, Options(false, false, false))
    }

    applyBaselineCalculation(report, runContext.config, runContext.scope, runContext.messageReporter)
  }

  override suspend fun createGlobalInspectionContext(runContext: QodanaRunContext) : QodanaGlobalInspectionContext {
    val skipCoverageComputation = java.lang.Boolean.getBoolean(COVERAGE_SKIP_COMPUTATION_PROPERTY)
    val skipCoverageReporting = java.lang.Boolean.getBoolean(COVERAGE_SKIP_REPORTING_PROPERTY)
    val computationState = if (skipCoverageComputation) {
      QodanaCoverageComputationState.SKIP_COMPUTE
    } else if (skipCoverageReporting) {
      QodanaCoverageComputationState.SKIP_REPORT
    } else {
      throw QodanaException("Coverage computation mode is not set to 'skip computation' or 'skip reporting'")
    }
    return runContext.createGlobalInspectionContext(coverageComputationState = computationState)
  }

  private fun getScopedBaselineReport(runContext: QodanaRunContext): SarifReport? {
    val scopedBaselinePath = System.getProperty(SCOPED_BASELINE_PROPERTY)?.let { Path.of(it) } ?: return null
    val absPath = if (scopedBaselinePath.isAbsolute) scopedBaselinePath else runContext.config.projectPath.resolve(scopedBaselinePath)
    return SarifUtil.readReport(absPath)
  }
}


internal class ScopedRunContextFactory(
  private val delegate: QodanaRunContextFactory,
  @VisibleForTesting val scopeFile: Path,
  val config: QodanaConfig,
) : QodanaRunContextFactory {

  override suspend fun openRunContext(): QodanaRunContext {
    var sourceContext = delegate.openRunContext()
    val changedFiles = parseChangedFiles(scopeFile)
    val paths = changedFiles.files.map { Path.of(it.path) }
    val addedLines = collectAddedLines(changedFiles, config)

    sourceContext = sourceContext.copy(changes = addedLines)

    sourceContext.project.serviceAsync<LocalChangesService>()
      .isIncrementalAnalysis
      .set(true)

    return sourceContext.applyExternalFileScope(paths)
  }
}
