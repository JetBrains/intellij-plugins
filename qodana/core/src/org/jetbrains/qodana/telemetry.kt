package org.jetbrains.qodana

import com.intellij.platform.diagnostic.telemetry.IJTracer
import com.intellij.platform.diagnostic.telemetry.Scope
import com.intellij.platform.diagnostic.telemetry.TelemetryManager
import com.intellij.platform.diagnostic.telemetry.helpers.useWithScope
import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.staticAnalysis.inspections.runner.runTaskAndLogTime
import org.jetbrains.qodana.staticAnalysis.stat.InspectionEventsCollector
import org.jetbrains.qodana.staticAnalysis.stat.InspectionEventsCollector.QodanaActivityKind

@JvmField
val QodanaScope = Scope("qodana")

fun qodanaTracer(): IJTracer = TelemetryManager.getTracer(QodanaScope)

private typealias SpanTag = String
private typealias HumanReadable = String

private val activityTexts: Map<QodanaActivityKind, Pair<HumanReadable?, SpanTag>> = mapOf(
  QodanaActivityKind.LINTER_EXECUTION to (null to "qodana run"),
  QodanaActivityKind.PROJECT_OPENING to ("Project opening" to "qodanaProjectOpening"),
  QodanaActivityKind.PROJECT_CONFIGURATION to ("Project configuration" to "qodanaProjectConfiguration"),
  QodanaActivityKind.PROJECT_ANALYSIS to ("Project analysis" to "qodanaProjectAnalysis"),
)

internal suspend fun <T> runActivityWithTiming(activityKind: QodanaActivityKind, activity: suspend CoroutineScope.(Span) -> T): T {
  val (humanReadable, spanTag) = activityTexts.getValue(activityKind)
  return if (humanReadable != null) {
    runTaskAndLogTime(humanReadable) {
      InspectionEventsCollector.logQodanaActivityDuration(activityKind) {
        qodanaTracer().spanBuilder(spanTag).useWithScope(operation = activity)
      }
    }
  }
  else {
    InspectionEventsCollector.logQodanaActivityDuration(activityKind) {
      qodanaTracer().spanBuilder(spanTag).useWithScope(operation = activity)
    }
  }
}
