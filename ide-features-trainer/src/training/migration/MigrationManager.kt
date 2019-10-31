/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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