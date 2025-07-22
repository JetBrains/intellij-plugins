package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.eventLog.events.VarargEventId
import com.intellij.openapi.project.Project

class QodanaVarargEventId (val varargEventId: VarargEventId){
  fun log(vararg pairs: EventPair<*>) {
    log(listOf(*pairs))
  }

  fun log(pairs: List<EventPair<*>>) {
    varargEventId.log(pairs + QodanaEventsService.getInstance().analysisKindPair)
  }

  fun log(project: Project?, vararg pairs: EventPair<*>) {
    log(project, listOf(*pairs))
  }

  fun log(project: Project?, pairs: List<EventPair<*>>) {
    varargEventId.log(project, pairs + QodanaEventsService.getInstance().analysisKindPair)
  }

  fun metric(vararg pairs: EventPair<*>): MetricEvent {
    return metric(listOf(*pairs))
  }

  fun metric(pairs: List<EventPair<*>>): MetricEvent {
    return varargEventId.metric(pairs + QodanaEventsService.getInstance().analysisKindPair)
  }
}