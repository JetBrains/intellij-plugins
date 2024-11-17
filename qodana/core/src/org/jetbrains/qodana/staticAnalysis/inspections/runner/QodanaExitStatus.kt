package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.diagnostic.logger
import com.jetbrains.qodana.sarif.model.Notification
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.coverageStats
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.staticAnalysis.sarif.ResultSummaryContributor
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.ToolErrorInspectListener
import org.jetbrains.qodana.staticAnalysis.sarif.qodanaKind
import org.jetbrains.qodana.staticAnalysis.sarif.resultSummary

private val logger by lazy { logger<ExitStatus>() }

private const val FAIL_THRESHOLD_EXIT_CODE = 255

/**
 * Arbitrary, but based on https://man.freebsd.org/cgi/man.cgi?query=sysexits
 * EX_SOFTWARE (70)  An	internal software error has been detected.
 */
private const val RUNTIME_ERRORS_EXIT_CODE = 70
private const val BULLET_POINT = "\n- "

internal val Run.firstExitStatus: ExitStatus
  get() {
    val invocation = invocations?.firstOrNull()
    return ExitStatus(invocation?.exitCode ?: 0, invocation?.exitCodeDescription, invocation?.executionSuccessful != false)
  }

internal data class ExitStatus(val code: Int, val description: String?, val success: Boolean)

internal fun setInvocationExitStatus(run: Run, config: QodanaConfig) {
  val summary = run.resultSummary ?: run {
    logger.warn("Cannot update exit code because the result summary has not been generated. See ${ResultSummaryContributor::class.qualifiedName}")
    return
  }
  val invocation = run.invocations?.singleOrNull() ?: run {
    logger.warn("Cannot update exit code because there are ${run.invocations?.count()} invocations in this run")
    return
  }

  if (invocation.exitCode != null) {
    logger.info("Invocation already has an exit code, not updating")
    return
  }

  val isToolError: (Notification) -> Boolean = {
    it.qodanaKind == ToolErrorInspectListener.TOOL_ERROR_NOTIFICATION && it.level == Notification.Level.ERROR
  }
  if (config.failOnErrorNotification && invocation.toolExecutionNotifications.orEmpty().any(isToolError)) {
    invocation.exitCode = RUNTIME_ERRORS_EXIT_CODE
    invocation.exitCodeDescription = QodanaBundle.message("exit.runtime.error.notifications")
    invocation.executionSuccessful = false
    return
  }

  invocation.executionSuccessful = true
  var failedConditionsCount = 0
  val failedConditions = checkSeverityThresholds(summary, config)
    .plus(checkCoverageThresholds(run, config))
    .onEach { failedConditionsCount++ }
    .joinToString(prefix = BULLET_POINT, separator = BULLET_POINT)

  if (failedConditionsCount == 0) {
    invocation.exitCode = 0
  }
  else {
    invocation.exitCode = FAIL_THRESHOLD_EXIT_CODE
    invocation.exitCodeDescription = QodanaBundle.message("exit.failure.summary", failedConditionsCount, failedConditions)
  }
}


private fun checkSeverityThresholds(summary: Map<String, Int>, config: QodanaConfig): Sequence<String> {
  val thresholds = config.failureConditions

  val severitiesByName = QodanaSeverity.entries.associateBy { it.name.lowercase() }
  val failedSeverities = summary.asSequence()
    .mapNotNull { (sevName, count) ->
      val severity = severitiesByName[sevName]
      val threshold = severity?.let(config.failureConditions::bySeverity)
      if (threshold != null && count > threshold) {
        Triple(severity, count, threshold)
      }
      else {
        null
      }
    }
    .sortedByDescending { (severity) -> severity.weight }
    .map { (severity, count, threshold) ->
      QodanaBundle.message("exit.threshold.severity.exceeded", count, severity.name, threshold)
    }

  val totalResults = summary[ResultSummaryContributor.TOTAL_KEY] ?: 0

  val totalThreshold = thresholds.severityThresholds.any
  return if (totalThreshold != null && totalResults > totalThreshold) {
    failedSeverities + QodanaBundle.message("exit.threshold.count.exceeded", totalResults, totalThreshold)
  }
  else {
    failedSeverities
  }
}

private fun checkCoverageThresholds(run: Run, config: QodanaConfig): Sequence<String> =
  sequenceOf(CoverageData.TOTAL_COV, CoverageData.FRESH_COV)
    .mapNotNull { coverage ->
      val threshold = config.failureConditions.byCoverage(coverage)
      val stat = run.coverageStats[coverage.prop]
      if (stat != null && threshold != null && stat < threshold) {
        Triple(coverage, stat, threshold)
      }
      else {
        null
      }
    }
    .map { (coverage, level, threshold) ->
      QodanaBundle.message("exit.threshold.coverage.not.met", coverage.title, level, threshold)
    }

