package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.application.PathManager
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.Options
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.*
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector
import java.nio.file.Paths

/**
 * Runs the inspections two times on the project, "before" and "after".
 * Reports only the problems that differ between the two runs.
 */
abstract class ComparingScript(
  private val config: QodanaConfig,
  private val messageReporter: QodanaMessageReporter,
  private val contextFactory: QodanaRunContextFactory,
  private val analysisKind: AnalysisKind,
) : QodanaScript {

  protected val progressIndicator = QodanaProgressIndicator(messageReporter)

  override suspend fun execute(report: SarifReport, run: Run): QodanaScriptResult {
    val scriptResult: QodanaScriptResult
    val runContext = contextFactory.openRunContext()
    fillComponents(run.tool, runContext.qodanaProfile)
    runContext.appendRunDetails(run, analysisKind)

    setUpAll(runContext)

    val beforeRun: Run = createRun()
    runContext.appendRunDetails(beforeRun, analysisKind)
    val beforeReport = createSarifReport(mutableListOf(beforeRun))

    try {
      setUpBefore(runContext)
      try {
        runBefore(beforeReport, beforeRun, runContext)
      }
      finally {
        tearDownBefore(runContext)
      }

      setUpAfter(runContext)
      try {
        scriptResult = runAfter(report, run, runContext)
      }
      finally {
        tearDownAfter(runContext)
      }
    }
    finally {
      tearDownAll(runContext)
      withContext(NonCancellable) {
        runContext.writeProjectDescriptionAfterWork()
      }
    }

    writeReport(Paths.get(PathManager.getLogPath(), "before.qodana.sarif.json"), SarifReport().withRuns(listOf(beforeRun)))
    writeReport(Paths.get(PathManager.getLogPath(), "after.qodana.sarif.json"), SarifReport().withRuns(listOf(run)))
    // compare before and current, keeping only 'NEW' issues in current
    BaselineCalculation.compare(report, beforeReport, Options(false, false, false))
    // compare current and baseline to generate the final report
    applyBaselineCalculation(report, config, runContext.scope, messageReporter)

    maybeApplyFixes(run, runContext)

    SarifReportContributor.runContributors(run, runContext.project, runContext.config)
    CoverageFeatureEventsCollector.logCoverageStatistics(runContext, scriptResult.coverageStats)
    return scriptResult
  }

  /**
   * Called at the very beginning, before the "before" run.
   * Can be used for saving the state before modifying it for the two runs.
   * Corresponds to [tearDownAll].
   */
  protected open suspend fun setUpAll(runContext: QodanaRunContext) {}

  protected open suspend fun setUpBefore(runContext: QodanaRunContext) {}

  protected open suspend fun runBefore(report: SarifReport, run: Run, runContext: QodanaRunContext) {
    val outPathBefore = config.resultsStorage.resolve("before")
    outPathBefore.toFile().mkdir()

    val beforeQodanaProfile = QodanaProfile.create(runContext.project, runContext.baseProfile,
                                                   QodanaInspectionProfileLoader(runContext.project),
                                                   config, sanity = false, promo = false)
    val inspectionContext = runContext.createGlobalInspectionContext(outPathBefore, beforeQodanaProfile, QodanaCoverageComputationState.SKIP_COMPUTE)
    try {
      runInspections(runContext.scope, run, inspectionContext, runContext)
    } finally {
      withContext(NonCancellable) {
        inspectionContext.closeQodanaContext()
      }
    }
  }

  protected open suspend fun tearDownBefore(runContext: QodanaRunContext) {}

  protected open suspend fun setUpAfter(runContext: QodanaRunContext) {}

  protected open suspend fun runAfter(report: SarifReport, run: Run, runContext: QodanaRunContext): QodanaScriptResult {
    val inspectionContext = runContext.createGlobalInspectionContext(config.resultsStorage, runContext.qodanaProfile, QodanaCoverageComputationState.SKIP_REPORT)
    val scriptResult = try {
      runContext.writeProfiles(inspectionContext.profile)
      runInspections(runContext.scope, run, inspectionContext, runContext)
      runContext.writeProjectDescriptionBeforeWork(config.outPath)
      QodanaScriptResult.create(inspectionContext)
    } finally {
      withContext(NonCancellable) {
        inspectionContext.closeQodanaContext()
      }
    }
    return scriptResult
  }

  protected open suspend fun tearDownAfter(runContext: QodanaRunContext) {}

  /**
   * Called at the very end, after the "after" run.
   * Can be used for restoring the previous state, as saved by [setUpAll].
   */
  protected open suspend fun tearDownAll(runContext: QodanaRunContext) {}

  protected suspend fun runInspections(
    scope: QodanaAnalysisScope,
    run: Run,
    context: QodanaGlobalInspectionContext,
    runContext: QodanaRunContext) {
    runContext.runAnalysis(scope, context, progressIndicator)
    run.results = runContext.getResultsForInspectionGroup(context)
    val mainProfileState = context.profileState.mainState
    if (runContext.config.isAboveStopThreshold(mainProfileState.getCount())) {
      runContext.messageReporter.reportMessage(
        1,
        "Inspection run was stopped cause it's reached threshold: ${runContext.config.stopThreshold}. " +
        "Problems count: ${mainProfileState.getCount()}")
    }
  }
}