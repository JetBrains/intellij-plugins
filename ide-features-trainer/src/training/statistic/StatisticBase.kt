/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.statistic

import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import training.learn.interfaces.Lesson
import training.statistic.FeatureUsageStatisticConsts.DURATION
import training.statistic.FeatureUsageStatisticConsts.LANGUAGE
import training.statistic.FeatureUsageStatisticConsts.LESSON_ID
import training.statistic.FeatureUsageStatisticConsts.PASSED
import training.statistic.FeatureUsageStatisticConsts.START
import java.util.concurrent.ConcurrentHashMap

@Suppress("PropertyName")
class StatisticBase {

  private val sessionLessonTimestamp: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
  private val LOG = Logger.getInstance(StatisticBase::class.java)
  //should be the same as res/META-INF/plugin.xml <statistics.counterUsagesCollector groupId="ideFeaturesTrainer" .../>
  private val GROUP_ID = "ideFeaturesTrainer"

  companion object {
    val instance: StatisticBase by lazy { ServiceManager.getService(StatisticBase::class.java) }
  }

  fun onStartLesson(lesson: Lesson) {
    sessionLessonTimestamp[lesson.id] = System.currentTimeMillis()
    logEvent(START, FeatureUsageData()
      .addData(LESSON_ID, lesson.id)
      .addData(LANGUAGE, lesson.lang.toLowerCase()))
  }

  fun onPassLesson(lesson: Lesson) {
    val timestamp = sessionLessonTimestamp[lesson.id]
    if (timestamp == null) {
      LOG.warn("Unable to find timestamp for a lesson: ${lesson.name}")
      return
    }
    val delta = System.currentTimeMillis() - timestamp
    logEvent(PASSED, FeatureUsageData()
      .addData(LESSON_ID, lesson.id)
      .addData(LANGUAGE, lesson.lang.toLowerCase())
      .addData(DURATION, delta))
  }

  private fun logEvent(event: String, featureUsageData: FeatureUsageData) {
    FUCounterUsageLogger.getInstance().logEvent(GROUP_ID, event, featureUsageData)
  }

}