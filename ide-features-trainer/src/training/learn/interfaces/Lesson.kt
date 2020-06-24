// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.interfaces

import training.commands.kotlin.TaskTestContext
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonState
import training.learn.lesson.LessonStateManager
import training.util.findLanguageByID

interface Lesson {

  val name: String

  val id: String

  /** This name will be used for generated file with lesson sample */
  val fileName: String
    get() = module.sanitizedName + "." + findLanguageByID(lang)!!.associatedFileType!!.defaultExtension

  var module: Module

  val classLoader: ClassLoader
    get() = module.classLoader

  val passed: Boolean
    get() = LessonStateManager.getStateFromBase(id) == LessonState.PASSED

  var isOpen: Boolean

  val lang: String

  val lessonListeners: MutableList<LessonListener>

  /** Relative path to existed file in the learning project */
  val existedFile: String?
    get() = null

  fun addLessonListener(lessonListener: LessonListener) {
    lessonListeners.add(lessonListener)
  }

  fun onStart() {
    lessonListeners.forEach { it.lessonStarted(this) }
  }

  fun onPass() {
    lessonListeners.forEach { it.lessonPassed(this) }
  }

  fun onStop() {
    lessonListeners.forEach { it.lessonStopped(this) }
  }

  fun pass() {
    LessonStateManager.setPassed(this)
    onPass()
  }

  val testScriptProperties : TaskTestContext.TestScriptProperties
    get() = TaskTestContext.TestScriptProperties()
}
