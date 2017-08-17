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
package training.migration

import com.intellij.openapi.diagnostic.Logger
import training.learn.lesson.LessonState
import training.learn.lesson.LessonStateBase
import java.util.*

object MigrationManager {

  private val LOG = Logger.getInstance(MigrationManager::class.java.canonicalName)

  private val migrateAgents = arrayOf<MigrationAgent>(MigrationAgent074())

  fun processLegacyStates(cachedService: LessonStateBase) {
    val myMap = HashMap<String, LessonState>()
    migrateAgents.forEach {
      LOG.info("Adding lesson states from previous version (${it.VERSION}) of trainingPlugin / IDE Features Trainer from xml file: ${it.XML_FILE_NAME}")
      myMap.putAll(it.extractLessonStateMap())
    }
    cachedService.myMap.putAll(myMap)
  }

  abstract class MigrationAgent() {
    abstract val VERSION: String
    abstract val XML_FILE_NAME: String
    abstract fun extractLessonStateMap(): Map<String, LessonState>
  }

}