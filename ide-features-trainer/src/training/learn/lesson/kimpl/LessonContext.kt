package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.commands.kotlin.TaskContext
import training.learn.lesson.LessonManager

class LessonContext(val lesson: KLesson, val editor: Editor, val project: Project) {
  /**
   * Start a new task in a lesson context
   */
  fun task(taskContent: TaskContext.() -> Unit) {
    val taskContext = TaskContext(lesson, editor, project)
    taskContext.apply(taskContent)
    taskContext.checkFutures.all { it.get() }
    taskContext.triggerFutures.forEach { it.get() }
    LessonManager.getInstance(lesson).passExercise()
  }

  fun complete() {
    lesson.pass()
    LessonManager.getInstance(lesson).passLesson(project, editor)
  }
}
