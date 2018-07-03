package training.statistic

import com.intellij.internal.statistic.UsagesCollector
import com.intellij.internal.statistic.beans.GroupDescriptor
import com.intellij.internal.statistic.beans.UsageDescriptor
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.Transient
import training.learn.lesson.Lesson

@State(name = "StatisticBase", storages = arrayOf(Storage(value = "ide-features-trainer.xml")))
class StatisticBase : PersistentStateComponent<StatisticBase> {

  override fun getState(): StatisticBase = this

  override fun loadState(persistedState: StatisticBase) {
    this.persistedLessonId2State.putAll(persistedState.persistedLessonId2State)
  }

  var persistedLessonId2State: LinkedHashMap<String, StatisticData> = LinkedHashMap()
  //non persisted history of lessons states
  @Transient
  val sessionLessonId2State: ArrayList<Pair<String, StatisticData>> = ArrayList()

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
    persistedLessonId2State.put(lesson.id, statisticData)
    sessionLessonId2State.add(Pair(lesson.id, statisticData))
  }

  fun onPassLesson(lesson: Lesson) {
    //if lesson already has a passed state in a persisted map than skip this step
    if (persistedLessonId2State.containsKey(lesson.id) && persistedLessonId2State[lesson.id]?.state == StatisticState.PASSED) return
    synchronized(persistedLessonId2State) {
      persistedLessonId2State[lesson.id] ?: return //TODO: add log here
      val timestamp = persistedLessonId2State[lesson.id]!!.timestamp ?: return
      val delta = System.currentTimeMillis() - timestamp
      val statisticData = StatisticData(StatisticState.PASSED, delta)
      persistedLessonId2State.put(lesson.id, statisticData)
      sessionLessonId2State.add(Pair(lesson.id, statisticData))
    }
  }
}

class StatisticCollector: UsagesCollector() {

  override fun getGroupId(): GroupDescriptor {
    return GroupDescriptor.create("plugin.IdeFeaturesTrainer")
  }

  /**
   * returns a set of lesson states in a next format:
   *    - for a started lesson: UsageDescriptor(${lesson.id}#STARTED, 1)
   *    - for a passed lesson UsageDescriptor(${lesson.id}#PASSED, delta_in_seconds )
   */
  override fun getUsages(): MutableSet<UsageDescriptor> {
    return StatisticBase.instance.sessionLessonId2State
        .map {
          val (lessonId, statisticData) = it
          val key = "$lessonId#${statisticData.state}"
          val count: Int = if (statisticData.state == StatisticBase.StatisticState.STARTED) 1 else (statisticData.timestamp!! / 1000).toInt()
          UsageDescriptor(key, count)
        }
        .toMutableSet()
  }
}