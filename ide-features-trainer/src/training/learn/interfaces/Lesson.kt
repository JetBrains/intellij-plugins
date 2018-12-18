package training.learn.interfaces

import training.learn.lesson.LessonListener
import training.learn.lesson.LessonStateManager

interface Lesson {

  val name: String

  val id: String

  var module: Module

  var passed: Boolean

  var isOpen: Boolean

  val lang: String

  val lessonListeners: MutableList<LessonListener>

  val existedFile : String?
    get() = null

  fun addLessonListener(lessonListener: LessonListener) {
    lessonListeners.add(lessonListener)
  }

  fun onStart() {
    lessonListeners.forEach { it.lessonStarted(this) }
  }

  fun onClose() {
    lessonListeners.clear()
  }

  fun onPass() {
    lessonListeners.forEach { it.lessonPassed(this) }
  }

  fun pass() {
    passed = true
    LessonStateManager.setPassed(this)
    onPass()
  }

}
