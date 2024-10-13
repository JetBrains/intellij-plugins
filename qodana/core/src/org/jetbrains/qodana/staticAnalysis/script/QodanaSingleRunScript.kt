package org.jetbrains.qodana.staticAnalysis.script

import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.sarif.SarifReportContributor
import org.jetbrains.qodana.staticAnalysis.sarif.fillComponents
import org.jetbrains.qodana.staticAnalysis.sarif.maybeApplyFixes
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector

abstract class QodanaSingleRunScript(
  @VisibleForTesting val runContextFactory: QodanaRunContextFactory,
  private val analysisKind: AnalysisKind,
) : QodanaScript {

  abstract suspend fun execute(
    report: SarifReport,
    run: Run,
    runContext: QodanaRunContext,
    inspectionContext: QodanaGlobalInspectionContext
  )

  protected open suspend fun createGlobalInspectionContext(runContext: QodanaRunContext) = runContext.createGlobalInspectionContext()

  final override suspend fun execute(report: SarifReport, run: Run): QodanaScriptResult {
    val runContext = runContextFactory.openRunContext()

    fillComponents(run.tool, runContext.qodanaProfile)
    runContext.appendRunDetails(run, analysisKind)
    runContext.writeProfiles(runContext.qodanaProfile)
    runContext.writeProjectDescriptionBeforeWork()

    val inspectionContext = createGlobalInspectionContext(runContext)
    try {
      execute(report, run, runContext, inspectionContext)
    }
    finally {
      withContext(NonCancellable) {
        runContext.writeProjectDescriptionAfterWork()
        inspectionContext.closeQodanaContext()
      }
    }

    maybeApplyFixes(run, runContext)
    SarifReportContributor.runContributors(run, runContext.project, runContext.config)
    val scriptResult = QodanaScriptResult.create(inspectionContext)
    CoverageFeatureEventsCollector.logCoverageStatistics(runContext, scriptResult.coverageStats)
    return scriptResult
  }
}
