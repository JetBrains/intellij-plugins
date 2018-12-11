package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonManager

typealias LessonContext = Triple<KLesson, Editor, Project>

abstract class KLesson(final override val name  : String,
                             override var module: Module,
                             override val lang  : String) : Lesson {

  abstract val lessonContent: LessonContext.() -> Unit
  override val id: String = name
  override var passed = false
  override var isOpen = false
  override val lessonListeners: MutableList<LessonListener> = mutableListOf()
}

fun KModule.lesson(lessonName: String, lang: String, lessonContent: LessonContext.() -> Unit): KLesson {
  return object : KLesson(lessonName, this, lang) {
    override val lessonContent: LessonContext.() -> Unit = lessonContent
  }
}

fun LessonContext.complete(text: String? = null) {
  val (lesson, editor, project) = this
  lesson.pass()
  LessonManager.getInstance(lesson).passLesson(project, editor)
}