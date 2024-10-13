package org.jetbrains.qodana.run

import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.progress.util.ProgressIndicatorWithDelayedPresentation
import com.intellij.platform.util.progress.RawProgressReporter
import com.intellij.platform.util.progress.reportRawProgress
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.UiAnyModality
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaCancellationException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.applyBaselineCalculation
import org.jetbrains.qodana.staticAnalysis.sarif.SarifReportContributor
import org.jetbrains.qodana.staticAnalysis.sarif.fillComponents
import org.jetbrains.qodana.staticAnalysis.script.*
import org.jetbrains.qodana.staticAnalysis.stat.CoverageFeatureEventsCollector

const val RUN_TIMESTAMP = "runTimestamp"

class QodanaInIdeScript(private val runContext: QodanaRunContext) : QodanaScript {

  override suspend fun execute(report: SarifReport, run: Run): QodanaScriptResult {
    val inspectionContext = runContext.createGlobalInspectionContext()
    fillComponents(run.tool, runContext.qodanaProfile)

    try {
      runContext.appendRunDetails(run, AnalysisKind.IDE)
      runContext.writeProjectDescriptionBeforeWork()
      runContext.writeProfiles(runContext.qodanaProfile)
      reportRawProgress { reporter ->
        runContext.runAnalysis(runContext.scope, inspectionContext, InspectionProgressIndicator(reporter), isOffline = false)
      }
      runContext.writeProjectDescriptionAfterWork()
      val results = runContext.getResultsForInspectionGroup(inspectionContext)
      run.results = results
      applyBaselineCalculation(report, runContext.config, runContext.scope, runContext.messageReporter)

      SarifReportContributor.runContributors(run, runContext.project, runContext.config)
      val scriptResult = QodanaScriptResult.create(inspectionContext)
      CoverageFeatureEventsCollector.logCoverageStatistics(runContext, scriptResult.coverageStats)
      return scriptResult
    }
    catch (e: QodanaCancellationException) {
      QodanaNotifications.Tips.notification(
        QodanaBundle.message("notification.run.in.ide.stopped"),
        e.message ?: "",
        NotificationType.WARNING
      ).notify(runContext.project)
      run.results = emptyList()
      throw e
    }
    finally {
      report.withProperties(PropertyBag().also {
        it[RUN_TIMESTAMP] = System.currentTimeMillis().toString()
      })
      withContext(NonCancellable) {
        inspectionContext.closeQodanaContext()
        withContext(QodanaDispatchers.UiAnyModality) {
          inspectionContext.close(true)
        }
      }
    }
  }

  private class InspectionProgressIndicator(
    private val rawProgressReporter: RawProgressReporter
  ) : ProgressIndicatorBase(), ProgressIndicatorWithDelayedPresentation {
    override fun setDelayInMillis(delayInMillis: Int) {
    }

    override fun setText(text: String?) {
      rawProgressReporter.text(text)
      super.setText(text)
    }

    override fun setText2(text: String?) {
      rawProgressReporter.details(text)
      super.setText2(text)
    }

    override fun setFraction(fraction: Double) {
      rawProgressReporter.fraction(fraction)
      super.setFraction(fraction)
    }

    override fun setIndeterminate(indeterminate: Boolean) {
      if (indeterminate) {
        rawProgressReporter.fraction(null)
      }
      else {
        rawProgressReporter.fraction(0.0)
      }
      super.setIndeterminate(indeterminate)
    }
  }
}
