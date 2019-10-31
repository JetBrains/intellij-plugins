/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import training.learn.interfaces.Lesson
import training.migration.MigrationManager
import training.util.trainerPluginConfigName
import java.util.*

@State(name = "LessonStateBase", storages = arrayOf(Storage(value = trainerPluginConfigName)))
class LessonStateBase : PersistentStateComponent<LessonStateBase> {

  override fun getState(): LessonStateBase = this

  override fun loadState(persistedState: LessonStateBase) {
    this.myMap.putAll(persistedState.myMap)
  }

  var myMap: MutableMap<String, LessonState> = HashMap<String, LessonState>()

  companion object {
    var cachedService: LessonStateBase? = null

    val instance: LessonStateBase
      get() {
        if (cachedService == null) {
          cachedService = ServiceManager.getService(LessonStateBase::class.java)
          if (cachedService != null && !cachedService!!.myMap.values.any { it == LessonState.PASSED }) {
            MigrationManager.processLegacyStates(cachedService!!)
          }
        }
        return cachedService!!
      }
  }
}

object LessonStateManager {

  fun setPassed(lesson: Lesson) {
    LessonStateBase.instance.myMap.put(lesson.id, LessonState.PASSED)
  }

  fun resetPassedStatus() {
    for (lesson in LessonStateBase.instance.myMap) {
      lesson.setValue(LessonState.NOT_PASSED)
    }
  }

  fun getStateFromBase(id: String): LessonState {
    return if (LessonStateBase.instance.myMap.containsKey(id)) {
      LessonStateBase.instance.myMap[id]!!
    } else {
      LessonStateBase.instance.myMap.put(id, LessonState.NOT_PASSED)
      LessonState.NOT_PASSED
    }
  }

}


