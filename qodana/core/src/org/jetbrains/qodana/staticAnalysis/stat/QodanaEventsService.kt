package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import org.jetbrains.qodana.staticAnalysis.script.AnalysisKind

internal const val ANALYSIS_KIND_EVENT_NAME = "analysisKind"

@Service(Service.Level.APP)
class QodanaEventsService {
  companion object {
    @JvmStatic
    fun getInstance(): QodanaEventsService = service()
    internal val analysisKindField = EventFields.Enum<AnalysisKind>(ANALYSIS_KIND_EVENT_NAME)
  }

  internal var analysisKindPair: EventPair<*> = analysisKindField.with(AnalysisKind.OTHER)

  internal fun initAnalysisKind(analysisKind: AnalysisKind) {
    synchronized(this) {
      analysisKindPair = analysisKindField.with(analysisKind)
    }
  }
}