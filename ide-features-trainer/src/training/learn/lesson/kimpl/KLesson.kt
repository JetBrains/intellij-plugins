package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonManager

typealias LessonContext = Triple<KLesson, Editor, Project>

class KLesson(override val name: String, override var module: Module, override val lang: String, val lessonContent: Triple<KLesson, Editor, Project>.() -> Unit) : Lesson {

  override val id: String = name
  override var passed = false
  override var isOpen = false
  override val lessonListeners: MutableList<LessonListener> = mutableListOf()
}

fun KModule.lesson(lessonName: String, lang: String, lessonContent: LessonContext.() -> Unit): KLesson {
  return KLesson(lessonName, this, lang, lessonContent)
}

fun LessonContext.complete(text: String? = null) {
  val (lesson, editor, project) = this
  lesson.pass()
  LessonManager.getInstance(lesson).passLesson(project, editor)
}