package training.commands.kotlin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.learn.lesson.LessonManager
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import java.util.concurrent.CompletableFuture
import kotlin.test.assertTrue

class TaskContext(val taskName: String?, val lesson: KLesson, val editor: Editor, val project: Project) {
  val checkFutures: MutableList<CompletableFuture<Boolean>> = mutableListOf()
  val triggerFutures: MutableList<CompletableFuture<Boolean>> = mutableListOf()
}

/**
 * Start a new task in a lesson context. Task may have an optional name.
 */
fun LessonContext.task(taskName: String? = null, taskContent: TaskContext.() -> Unit) {
  val taskContext = TaskContext(taskName, this.first, this.second, this.third)
  taskContext.apply(taskContent)
  taskContext.checkFutures.forEach { assertTrue(it.get()) }
  taskContext.triggerFutures.forEach { assertTrue(it.get()) }
  LessonManager.getInstance(this.first).passExercise()
}