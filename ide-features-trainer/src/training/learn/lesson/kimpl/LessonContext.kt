// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.CalledInAwt
import training.commands.kotlin.TaskContext

class LessonContext(val lesson: KLesson, val editor: Editor, val project: Project, private val executor: LessonExecutor) {
  /**
   * Start a new task in a lesson context
   */
  @CalledInAwt
  fun task(taskContent: TaskContext.() -> Unit) {
    executor.task(taskContent)
  }

  /** Describe a simple task: just one action required */
  fun actionTask(action: String, getText: TaskContext.(action: String) -> String) {
    task {
      text(getText(action))
      trigger(action)
      test { actions(action) }
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

  fun caret(offset: Int) {
    executor.caret(offset)
  }

  /** NOTE:  [line] and [column] starts from 1 not from zero. So these parameters should be same as in editors. */
  fun caret(line: Int, column: Int) {
    executor.caret(line, column)
  }

  fun caret(text: String) {
    executor.caret(text)
  }

  fun caret(position: LessonSamplePosition) {
    executor.caret(position)
  }

  /**
   * There will not be any freeze in GUI thread.
   * The continue of the script will be scheduled with the [delayMillis]
   */
  fun waitBeforeContinue(delayMillis: Int) {
    executor.waitBeforeContinue(delayMillis)
  }

  fun prepareSample(sample: LessonSample) {
    executor.prepareSample(sample)
  }
}
