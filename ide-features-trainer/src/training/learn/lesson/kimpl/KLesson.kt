package training.learn.lesson.kimpl

import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.lesson.LessonListener

abstract class KLesson(final override val name  : String,
                             override var module: Module,
                             override val lang  : String) : Lesson {

  abstract val lessonContent: LessonContext.() -> Unit
  override val id: String = name
  override var passed = false
  override var isOpen = false
  override val lessonListeners: MutableList<LessonListener> = mutableListOf()
}
