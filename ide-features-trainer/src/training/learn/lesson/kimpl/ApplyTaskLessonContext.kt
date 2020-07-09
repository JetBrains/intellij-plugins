// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import training.commands.kotlin.TaskContext

internal class ApplyTaskLessonContext(private val taskContext: TaskContext) : LessonContext() {
  override fun task(taskContent: TaskContext.() -> Unit) {
    taskContent(taskContext)
  }

  override fun caret(offset: Int) = Unit // do nothing

  override fun caret(line: Int, column: Int) = Unit // do nothing

  override fun caret(text: String) = Unit // do nothing

  override fun caret(position: LessonSamplePosition) = Unit // do nothing

  override fun waitBeforeContinue(delayMillis: Int) = Unit // do nothing

  override fun prepareSample(sample: LessonSample) = Unit // do nothing
}
