package training.learn.lesson.kimpl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.Alarm
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager

class LessonExecutor(val lesson: KLesson, val editor: Editor, val project: Project) {
  private var isUnderTaskProcessing = false
  private val taskActions: MutableList<() -> Unit> = ArrayList()

  private var currentRecorder: ActionsRecorder? = null

  fun addSimpleTaskAction(taskAction: () -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (!isUnderTaskProcessing) {
      taskActions.add {
        taskAction()
        processNextTask()
      }
    }
    else {
      // allow some simple tasks like caret move and so on...
      taskAction()
    }
  }

  fun waitBeforeContinue(delayMillis: Int) {
    if(isUnderTaskProcessing) {
      throw IllegalStateException("Delay should be specified between tasks!")
    }

    taskActions.add {
      Alarm().addRequest({ processNextTask() }, delayMillis)
    }
  }

  fun task(taskContent: TaskContext.() -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if(isUnderTaskProcessing) {
      throw IllegalStateException("Nested tasks are not permitted!")
    }

    taskActions.add { processTask(taskContent) }
  }

  fun stopLesson() {
    assert(ApplicationManager.getApplication().isDispatchThread)
    currentRecorder?.let { Disposer.dispose(it) }
  }

  fun processNextTask() {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (taskActions.size == 0) {
      lesson.pass()
      LessonManager.instance.passLesson(project, lesson)
      return
    }
    val content = taskActions[0]
    taskActions.removeAt(0)
    content()
  }

  private fun processTask(taskContent: TaskContext.() -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    val recorder = ActionsRecorder(project, editor.document)
    currentRecorder = recorder
    val taskContext = TaskContext(lesson, editor, project, recorder)
    isUnderTaskProcessing = true
    taskContext.apply(taskContent)
    isUnderTaskProcessing = false

    if (TaskContext.inTestMode) {
      LessonManager.instance.testActionsExecutor.execute {
        taskContext.testActions.forEach { it.run() }
      }
    }

    taskContext.steps.forEach { step ->
      step.thenAccept {
        assert(ApplicationManager.getApplication().isDispatchThread)
        val taskHasBeenDone = taskContext.steps.all { it.isDone }
        if (taskHasBeenDone) {
          // Now we are inside some listener registered by recorder
          ApplicationManager.getApplication().invokeLater {
            // So better to exit from all callbacks and then clear all related data
            Disposer.dispose(recorder)
            currentRecorder = null
            LessonManager.instance.passExercise()

            processNextTask()
          }
        }
      }
    }
  }
}