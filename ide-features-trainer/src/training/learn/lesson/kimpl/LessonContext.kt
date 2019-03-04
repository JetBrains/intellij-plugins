package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import kotlin.concurrent.thread

class LessonContext(val lesson: KLesson, val editor: Editor, val project: Project) {
  /**
   * Start a new task in a lesson context
   */
  fun task(taskContent: TaskContext.() -> Unit) {
    val taskContext = TaskContext(lesson, editor, project)
    taskContext.apply(taskContent)

    if (TaskContext.inTestMode) {
      thread(name = "TestLearningPlugin") {
        taskContext.testActions.forEach { it.run() }
      }
    }

    val recorder = ActionsRecorder(project, editor.document)
    LessonManager.getInstance(lesson).registerActionsRecorder(recorder)

    val actionId = taskContext.myActionId
    val check = taskContext.myCheck
    if (actionId != null) {
      if (check != null) {
        recorder.futureActionAndCheckAround(actionId, check).get()
      }
      else {
        recorder.futureAction(actionId).get()
      }
    }
    else if (check != null) {
      check.before()
      recorder.futureCheck { check.check() }.get()
    }
    LessonManager.getInstance(lesson).passExercise()
  }

  fun triggerTask(action: String, taskContent: TaskContext.(action: String) -> Unit) {
    task {
      taskContent(action)
      trigger(action)
    }
  }

  fun prepareSample(sample: LessonSample) {
    task {
      setDocumentCode(sample.text)
      caret(sample.getInfo(START_TAG).startOffset)
    }
  }
}
