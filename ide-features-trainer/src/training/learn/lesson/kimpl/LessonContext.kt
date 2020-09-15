// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.util.concurrency.annotations.RequiresEdt
import org.jetbrains.annotations.Nls
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext

abstract class LessonContext {
  /**
   * Start a new task in a lesson context
   */
  @RequiresEdt
  open fun task(taskContent: TaskContext.() -> Unit) = Unit

  /** Describe a simple task: just one action required */
  fun actionTask(action: String, @Nls getText: TaskContext.(action: String) -> String) {
    task {
      text(getText(action))
      trigger(action)
      test { actions(action) }
    }
  }

  fun prepareRuntimeTask(modalityState: ModalityState? = ModalityState.any(), preparation: TaskRuntimeContext.() -> Unit) {
    task {
      addFutureStep {
        invokeLater(modalityState) {
          preparation()
          completeStep()
        }
      }
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

  open fun caret(offset: Int) = Unit

  /** NOTE:  [line] and [column] starts from 1 not from zero. So these parameters should be same as in editors. */
  open fun caret(line: Int, column: Int) = Unit

  open fun caret(text: String, select: Boolean = false) = Unit

  open fun caret(position: LessonSamplePosition) = Unit

  /**
   * There will not be any freeze in GUI thread.
   * The continue of the script will be scheduled with the [delayMillis]
   */
  open fun waitBeforeContinue(delayMillis: Int) = Unit

  open fun prepareSample(sample: LessonSample) = Unit
}
