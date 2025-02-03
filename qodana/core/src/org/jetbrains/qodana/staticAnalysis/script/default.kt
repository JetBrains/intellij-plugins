package org.jetbrains.qodana.staticAnalysis.script

import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.applyBaselineCalculation
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory

const val DEFAULT_SCRIPT_NAME = "default"

class DefaultScriptFactory : QodanaScriptFactory {
  override val scriptName: String get() = DEFAULT_SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> {
    if (parameters != "") throw QodanaException("The 'default' script does not take parameters")
    return emptyMap()
  }

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters
  ): QodanaScript = DefaultScript(contextFactory, AnalysisKind.REGULAR)
}

internal open class DefaultScript(
  runContextFactory: QodanaRunContextFactory,
  analysisKind: AnalysisKind,
) : QodanaSingleRunScript(runContextFactory, analysisKind) {

  override suspend fun execute(
    report: SarifReport,
    run: Run,
    runContext: QodanaRunContext,
    inspectionContext: QodanaGlobalInspectionContext
  ) {
    runContext.runAnalysis(context = inspectionContext)
    run.results = runContext.getResultsForInspectionGroup(inspectionContext)
    applyBaselineCalculation(report, runContext.config, runContext.scope, runContext.messageReporter)
  }
}
