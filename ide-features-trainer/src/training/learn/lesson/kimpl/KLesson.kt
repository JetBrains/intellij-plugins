package training.learn.lesson.kimpl

import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonState
import training.learn.lesson.LessonStateManager

abstract class KLesson(final override val name  : String,
                             override var module: Module,
                             override val lang  : String) : Lesson {

  abstract val lessonContent: LessonContext.() -> Unit
  final override val id: String = name
  @Volatile override var passed = LessonStateManager.getStateFromBase(id) == LessonState.PASSED
  @Volatile override var isOpen = false
  override val lessonListeners: MutableList<LessonListener> = mutableListOf()
}
