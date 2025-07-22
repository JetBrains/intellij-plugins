package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventField
import com.intellij.internal.statistic.eventLog.events.EventIdName
import org.jetbrains.annotations.NonNls

class QodanaEventLogGroup(id: String, version: Int) {
  val eventLogGroup: EventLogGroup = EventLogGroup(id, version)

  fun registerVarargEvent(@NonNls @EventIdName eventId: String, vararg fields: EventField<*>): QodanaVarargEventId {
    return QodanaVarargEventId(eventLogGroup.registerVarargEvent(
      eventId, *fields, QodanaEventsService.analysisKindField))
  }
}