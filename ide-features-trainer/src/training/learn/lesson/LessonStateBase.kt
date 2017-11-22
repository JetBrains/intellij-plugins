/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package training.learn.lesson

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import training.migration.MigrationManager
import java.util.*

@State(name = "LessonStateBase", storages = arrayOf(Storage(value = "ide-features-trainer.xml")))
class LessonStateBase : PersistentStateComponent<LessonStateBase> {

  override fun getState(): LessonStateBase = this

  override fun loadState(persistedState: LessonStateBase?) {
    if (persistedState == null) return
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

  fun isPassed(lesson: Lesson): Boolean {
    return LessonStateBase.instance.myMap[lesson.id]?.equals(LessonState.PASSED) ?: false
  }

  fun setPassed(lesson: Lesson) {
    LessonStateBase.instance.myMap.put(lesson.id, LessonState.PASSED)
  }

  fun addLesson(lesson: Lesson) {
    LessonStateBase.instance.myMap.put(lesson.id, if (lesson.passed) LessonState.PASSED else LessonState.NOT_PASSED)
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


