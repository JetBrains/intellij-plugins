package training.statistic

import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.eventLog.FeatureUsageGroup
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.Transient
import training.learn.interfaces.Lesson
import training.util.trainerPluginConfigName

@State(name = "StatisticBase", storages = arrayOf(Storage(value = trainerPluginConfigName)))
class StatisticBase : PersistentStateComponent<StatisticBase> {

  override fun getState(): StatisticBase = this

  override fun loadState(persistedState: StatisticBase) {
    this.persistedLessonId2State.putAll(persistedState.persistedLessonId2State)
  }

  var persistedLessonId2State: LinkedHashMap<String, StatisticData> = LinkedHashMap()
  //non persisted history of lessons states
  @Transient
  val sessionLessonId2State: MutableList<Pair<String, StatisticData>> = ArrayList()

  init {
    FUCounterUsageLogger.getInstance().register(FeatureUsageGroup(GROUP_ID, VERSION))
  }

  companion object {
    private var cachedService: StatisticBase? = null

    val instance: StatisticBase
      get() {
        if (StatisticBase.cachedService == null) {
          StatisticBase.cachedService = ServiceManager.getService(StatisticBase::class.java)
        }
        return StatisticBase.cachedService!!
      }
  }

  enum class StatisticState { STARTED, PASSED }

  data class StatisticData(var state: StatisticState? = null, var timestamp: Long? = null)

  fun onStartLesson(lesson: Lesson) {
    //if persisted base has already state for this lesson do not add
    if (persistedLessonId2State.contains(lesson.id)) return // because it contains already info about this lesson. And al least this lesson has been started.
    val statisticData = StatisticData(StatisticState.STARTED, System.currentTimeMillis())
    persistedLessonId2State[lesson.id] = statisticData
    sessionLessonId2State.add(Pair(lesson.id, statisticData))
    logEvent("start.${lesson.id.sanitizeId()}",
        FeatureUsageData().addData("id", lesson.id.sanitizeId()).addData("lang", lesson.lang))
  }

  fun onPassLesson(lesson: Lesson) {
    //if lesson already has a passed state in a persisted map than skip this step
    if (persistedLessonId2State.containsKey(lesson.id) && persistedLessonId2State[lesson.id]?.state == StatisticState.PASSED) return
    synchronized(persistedLessonId2State) {
      persistedLessonId2State[lesson.id] ?: return //TODO: add log here
      val timestamp = persistedLessonId2State[lesson.id]!!.timestamp ?: return
      val delta = System.currentTimeMillis() - timestamp
      val statisticData = StatisticData(StatisticState.PASSED, delta)
      persistedLessonId2State[lesson.id] = statisticData
      sessionLessonId2State.add(Pair(lesson.id, statisticData))
      logEvent("passed.${lesson.id.sanitizeId()}", FeatureUsageData().addData("duration", delta).addData("id", lesson.id.sanitizeId()).addData("lang", lesson.lang))
    }
  }
}

private fun logEvent(event: String, featureUsageData: FeatureUsageData) {
  FUCounterUsageLogger.getInstance().logEvent(GROUP_ID, event, featureUsageData)
}

private const val GROUP_ID = "ideFeaturesTrainer"
private const val VERSION = 1

private fun String.sanitizeId(): String {
  return this.toLowerCase()
      .replace(" ", "")
      .replace("_", "")
      .replace("/", "")
      .replace("-", "")
}

