package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.components.serviceAsync
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.Options
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.sarif.createInvocation
import org.jetbrains.qodana.staticAnalysis.sarif.createSarifReport
import org.jetbrains.qodana.staticAnalysis.sarif.getOrCreateRun
import org.jetbrains.qodana.staticAnalysis.script.*
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.notExists

internal const val STAGE_ARG = "stage"
internal const val RESULT_PRINTING_SKIPPED = "qodana.result.skipped"
const val REVERSE_SCOPED_SCRIPT_NAME = "reverse-scoped"
internal const val DUMP_REDUCED_SCOPE_ARG = "qodana.reduced.scope.path"

internal enum class Stage {
  NEW,
  OLD,
  FIXES;
}

internal class ReverseScopedScriptFactory : QodanaScriptFactory {
  override val scriptName get() = REVERSE_SCOPED_SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> =
    if (parameters.isBlank()) {
      throw QodanaException("Cannot start $scriptName script without scope file and stage")
    }
    else {
      val split = parameters.split(",", limit = 2)
      if (Stage.entries.none { it.name == split[0] }) {
        throw QodanaException("Cannot start $scriptName script: Unknown stage ${split[0]}, expected one of ${Stage.entries.joinToString { it.name }}")
      }
      if (split.size != 2) {
        throw QodanaException("Cannot start $scriptName script: Stage $STAGE_ARG should be followed by scope file path")
      }
      mapOf(STAGE_ARG to split[0], SCOPE_ARG to split[1])
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
    val stage = Stage.valueOf(parameters.require<String>(STAGE_ARG))

    val runContextFactory = ReverseScopedRunContextFactory(contextFactory, path, config)
    return when (stage) {
      Stage.NEW -> ReverseScopedScriptNew(runContextFactory)
      Stage.OLD -> ReverseScopedScriptOld(runContextFactory)
      Stage.FIXES -> ReverseScopedScriptFixes(runContextFactory)
    }
  }
}

internal class ReverseScopedScriptNew(runContextFactory: ReverseScopedRunContextFactory) :
  ReverseScopedScript(false, runContextFactory) {
  override suspend fun execute(report: SarifReport, runContext: QodanaRunContext, inspectionContext: QodanaGlobalInspectionContext) {
    val run = report.getOrCreateRun()
    runContext.runAnalysis(context = inspectionContext)
    run.results = runContext.getResultsForInspectionGroup(inspectionContext)

    applyBaselineCalculation(report, runContext.config, runContext.scope, runContext.messageReporter)

    val state = runContext.config.skipResultStrategy.shouldSkip(run)
    preserveShouldSkipState(run, state)
    val dumpReducedScope = System.getProperty(DUMP_REDUCED_SCOPE_ARG)
    if (state && dumpReducedScope != null) {
      dumpScopeToPath(dumpReducedScope, runContext, report)
    }
  }

  private suspend fun dumpScopeToPath(
    dumpReducedScope: String,
    runContext: QodanaRunContext,
    report: SarifReport,
  ) {
    val path = run {
      val p = Path(dumpReducedScope)
      if (p.isAbsolute) p else runContext.config.projectPath.resolve(p)
    }
    val nextScope = computeNextScope(runContext.project, report)
    writeNextScope(path, nextScope)
  }
}

internal class ReverseScopedScriptOld(runContextFactory: ReverseScopedRunContextFactory) :
  ReverseScopedScript(true, runContextFactory) {
  override suspend fun execute(report: SarifReport, runContext: QodanaRunContext, inspectionContext: QodanaGlobalInspectionContext) {
    val baselineReport = getScopedBaselineReport(runContext)
    if (baselineReport == null) {
      throw QodanaException("Cannot find scoped-baseline report. It is required on the 'old' stage of the reverse-scoped script.")
    }

    val runOld = report.getOrCreateRun() // we retrieve original run created by QodanaRunner
    val runNew = updateReportWithScopedRun(baselineReport, report)

    val reportOld = createSarifReport(listOf(runOld)) // we create "fake" report containing current (old) run for baseline purposes
    runContext.runAnalysis(context = inspectionContext)
    runOld.results = runContext.getResultsForInspectionGroup(inspectionContext)
    BaselineCalculation.compare(report, reportOld, Options(false, false, false))

    applyBaselineCalculation(report, runContext.config, runContext.scope, runContext.messageReporter)

    preserveShouldSkipState(runNew, runContext.config.skipResultStrategy.shouldSkip(runNew))
  }
}


internal class ReverseScopedScriptFixes(runContextFactory: ReverseScopedRunContextFactory) :
  ReverseScopedScript(true, runContextFactory) {
  override suspend fun execute(report: SarifReport, runContext: QodanaRunContext, inspectionContext: QodanaGlobalInspectionContext) {
    val baselineReport = getScopedBaselineReport(runContext)
    if (baselineReport == null) {
      throw QodanaException("Cannot find scoped-baseline report. It is required on the 'old' stage of the reverse-scoped script.")
    }

    val runNew = updateReportWithScopedRun(baselineReport, report)
    // no need to apply baseline, was applied before

    preserveShouldSkipState(runNew, runContext.config.skipResultStrategy.shouldSkip(runNew))
  }
}

internal abstract class ReverseScopedScript(val skipCoverageComputation: Boolean, runContextFactory: ReverseScopedRunContextFactory) :
  DefaultScript(runContextFactory, AnalysisKind.INCREMENTAL) {

  override suspend fun createGlobalInspectionContext(runContext: QodanaRunContext) : QodanaGlobalInspectionContext {
    val computationState = if (skipCoverageComputation) {
      QodanaCoverageComputationState.SKIP_COMPUTE
    } else {
      QodanaCoverageComputationState.SKIP_REPORT
    }
    return runContext.createGlobalInspectionContext(coverageComputationState = computationState)
  }

  protected fun getScopedBaselineReport(runContext: QodanaRunContext): SarifReport? {
    val scopedBaselinePath = System.getProperty(SCOPED_BASELINE_PROPERTY)?.let { Path.of(it) } ?: return null
    val absPath = if (scopedBaselinePath.isAbsolute) scopedBaselinePath else runContext.config.projectPath.resolve(scopedBaselinePath)
    return SarifUtil.readReport(absPath)
  }

  protected fun preserveShouldSkipState(run: Run, state: Boolean) {
    val invocation = run.invocations.first()
    invocation.properties = invocation.properties ?: PropertyBag()
    invocation.properties?.put(RESULT_PRINTING_SKIPPED, state)
  }

  protected fun updateReportWithScopedRun(
    baselineReport: SarifReport,
    report: SarifReport,
  ): Run {
    val runNew = baselineReport.runs.singleOrNull() ?: throw QodanaException("Expected a single run in the scoped-baseline report.")
    val invocationNew = runNew.invocations?.firstOrNull()
    report.withRuns(listOf(runNew)) // we replace this run in the report with matching "new" stage run
    // we preserve execution notifications happened in previous run
    runNew.withInvocations(listOf(createInvocation()
                                    .withToolExecutionNotifications(invocationNew?.toolExecutionNotifications)))
    return runNew
  }
}

// to be later populated with scope extender logic
internal class ReverseScopedRunContextFactory(
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
