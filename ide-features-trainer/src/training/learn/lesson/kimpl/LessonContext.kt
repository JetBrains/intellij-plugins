package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import kotlin.concurrent.thread

class LessonContext(val lesson: KLesson, val editor: Editor, val project: Project) {
  val recorder = ActionsRecorder(project, editor.document)

  init {
    LessonManager.getInstance(lesson).registerActionsRecorder(recorder)
  }

  /**
   * Start a new task in a lesson context
   */
  fun task(taskContent: TaskContext.() -> Unit) {
    val taskContext = TaskContext(lesson, editor, project, recorder)
    taskContext.apply(taskContent)

    if (TaskContext.inTestMode) {
      thread(name = "TestLearningPlugin") {
        taskContext.testActions.forEach { it.run() }
      }
    }

    taskContext.steps.all { it.get() }
    LessonManager.getInstance(lesson).passExercise()
  }

  /** Describe a simple task: just one action required */
  fun actionTask(action: String, getText: TaskContext.(action: String) -> String) {
    task {
      text(getText(action))
      trigger(action)
    }
  }

  /**
   * Just shortcut to write action name once
   * @see task
   */
  fun task(action: String, taskContent: TaskContext.(action: String) -> Unit) {
    task {
      taskContent(action)
    }
  }

  fun prepareSample(sample: LessonSample) {
    task {
      setDocumentCode(sample.text)
      caret(sample.getInfo(START_TAG).startOffset)
    }
  }
}
