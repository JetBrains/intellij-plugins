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

@State(name = "LessonStateBase", storages = [Storage(value = trainerPluginConfigName)])
class LessonStateBase : PersistentStateComponent<LessonStateBase> {

  override fun getState(): LessonStateBase = this

  override fun loadState(persistedState: LessonStateBase) {
    this.map.putAll(persistedState.map)
  }

  val map: MutableMap<String, LessonState> = mutableMapOf()

  companion object {
    internal val instance: LessonStateBase by lazy {
      val service = ServiceManager.getService(LessonStateBase::class.java)
      if (service.map.values.none { it == LessonState.PASSED }) {
        MigrationManager.processLegacyStates(service)
      }
      service
    }
  }
}

object LessonStateManager {

  fun setPassed(lesson: Lesson) {
    LessonStateBase.instance.map[lesson.id] = LessonState.PASSED
  }

  fun resetPassedStatus() {
    for (lesson in LessonStateBase.instance.map) {
      lesson.setValue(LessonState.NOT_PASSED)
    }
  }

  fun getStateFromBase(id: String): LessonState = LessonStateBase.instance.map.getOrPut(id, { LessonState.NOT_PASSED })

}


