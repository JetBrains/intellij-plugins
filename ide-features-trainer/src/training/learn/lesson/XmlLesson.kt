/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson

import com.intellij.openapi.project.Project
import training.learn.CourseManager
import training.learn.exceptons.BadLessonException
import training.learn.exceptons.BadModuleException
import training.learn.exceptons.LessonIsOpenedException
import training.learn.exceptons.NoProjectException
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.log.LessonLog
import java.awt.FontFormatException
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

data class XmlLesson(val scenario: Scenario, override val lang: String, override var module: Module): Lesson {

  override var lessonListeners: MutableList<LessonListener> = ArrayList()
    private set
  override var passed: Boolean = false
  override var isOpen: Boolean = false
  override val name: String = scenario.name
  override val id: String = scenario.id

  /*Log lesson metrics*/
  private val lessonLog: LessonLog = LessonLog(this)

  init {
    passed = LessonStateManager.getStateFromBase(id) == LessonState.PASSED
  }

  @Throws(NoProjectException::class, BadLessonException::class, ExecutionException::class, LessonIsOpenedException::class, IOException::class, FontFormatException::class, InterruptedException::class, BadModuleException::class)
  fun open(projectWhereToOpenLesson: Project) {
    CourseManager.instance.openLesson(projectWhereToOpenLesson, this)
  }

  override fun onStart() {
    super.onStart()
    lessonLog.log("XmlLesson started")
    lessonLog.resetCounter()
  }

  //call onPass handlers in lessonListeners
  override fun onPass() {
    super.onPass()
    lessonLog.log("XmlLesson passed")
  }

  override fun equals(other: Any?): Boolean {
    return other != null && other is XmlLesson && other.name == this.name
  }

  override fun hashCode(): Int {
    var result = scenario.hashCode()
    result = 31 * result + id.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }

}
