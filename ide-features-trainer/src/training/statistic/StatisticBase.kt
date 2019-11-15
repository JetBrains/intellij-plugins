/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.statistic

import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.annotations.Transient
import training.learn.interfaces.Lesson
import training.statistic.FeatureUsageStatisticConsts.DURATION
import training.statistic.FeatureUsageStatisticConsts.LANGUAGE
import training.statistic.FeatureUsageStatisticConsts.LESSON_ID
import training.statistic.FeatureUsageStatisticConsts.PASSED
import training.statistic.FeatureUsageStatisticConsts.START
import training.util.trainerPluginConfigName

@Suppress("PropertyName")
@State(name = "StatisticBase", storages = [Storage(value = trainerPluginConfigName)])
class StatisticBase : PersistentStateComponent<StatisticBase> {

  override fun getState(): StatisticBase = this

  override fun loadState(persistedState: StatisticBase) {
    this.persistedLessonId2State.putAll(persistedState.persistedLessonId2State)
  }

  private val persistedLessonId2State: LinkedHashMap<String, StatisticData> = linkedMapOf()
  //non persisted history of lesson starting timestamp
  @Transient
  val sessionLessonTimestamp: MutableMap<String, Long> = mutableMapOf()

  companion object {
    val instance: StatisticBase by lazy { ServiceManager.getService(StatisticBase::class.java) }
  }

  val LOG = Logger.getInstance(StatisticBase::class.java)

  enum class StatisticState { STARTED, PASSED }

  data class StatisticData(var state: StatisticState? = null, var timestamp: Long? = null)

  fun onStartLesson(lesson: Lesson) {
    val statisticData = StatisticData(StatisticState.STARTED, System.currentTimeMillis())
    persistedLessonId2State[lesson.id] = statisticData
    sessionLessonTimestamp[lesson.id] = System.currentTimeMillis()
    logEvent(START, FeatureUsageData()
        .addData(LESSON_ID, lesson.id)
        .addData(LANGUAGE, lesson.lang.toLowerCase()))
  }

  fun onPassLesson(lesson: Lesson) {
    synchronized(persistedLessonId2State) {
      if (persistedLessonId2State[lesson.id] == null) {
        LOG.warn("Unable to find ${StatisticState.STARTED} state for a lesson: ${lesson.name}")
        return
      }
      if (sessionLessonTimestamp[lesson.id] == null) {
        LOG.warn("Unable to find timestamp for a lesson: ${lesson.name}")
        return
      }
      val timestamp = sessionLessonTimestamp[lesson.id] ?: return
      val delta = System.currentTimeMillis() - timestamp
      val statisticData = StatisticData(StatisticState.PASSED, delta)
      persistedLessonId2State[lesson.id] = statisticData
      logEvent(PASSED, FeatureUsageData()
          .addData(LESSON_ID, lesson.id)
          .addData(LANGUAGE, lesson.lang.toLowerCase())
          .addData(DURATION, delta))
    }
  }
}

private fun logEvent(event: String, featureUsageData: FeatureUsageData) {
  FUCounterUsageLogger.getInstance().logEvent(GROUP_ID, event, featureUsageData)
}

//should be the same as res/META-INF/plugin.xml <statistics.counterUsagesCollector groupId="ideFeaturesTrainer" .../>
private const val GROUP_ID = "ideFeaturesTrainer"

