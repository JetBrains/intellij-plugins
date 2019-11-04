/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.migration

import com.intellij.openapi.diagnostic.Logger
import training.learn.lesson.LessonState
import training.learn.lesson.LessonStateBase

object MigrationManager {

  private val LOG = Logger.getInstance(MigrationManager::class.java)

  private val migrateAgents = arrayOf<MigrationAgent>(MigrationAgent074())

  fun processLegacyStates(service: LessonStateBase) {
    migrateAgents.forEach {
      LOG.info("Adding lesson states from previous version (${it.VERSION}) of trainingPlugin / IDE Features Trainer from xml file: ${it.XML_FILE_NAME}")
      service.map.putAll(it.extractLessonStateMap())
    }
  }

  abstract class MigrationAgent {
    abstract val VERSION: String
    abstract val XML_FILE_NAME: String
    abstract fun extractLessonStateMap(): Map<String, LessonState>
  }

}