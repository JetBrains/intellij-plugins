// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import training.commands.kotlin.TaskContext

class LessonContextImpl(private val executor: LessonExecutor): LessonContext() {
  override fun task(taskContent: TaskContext.() -> Unit) {
    executor.task(taskContent)
  }

  override fun caret(offset: Int) {
    executor.caret(offset)
  }

  override fun caret(line: Int, column: Int) {
    executor.caret(line, column)
  }

  override fun caret(text: String) {
    executor.caret(text)
  }

  override fun caret(position: LessonSamplePosition) {
    executor.caret(position)
  }

  override fun waitBeforeContinue(delayMillis: Int) {
    executor.waitBeforeContinue(delayMillis)
  }

  override fun prepareSample(sample: LessonSample) {
    executor.prepareSample(sample)
  }
}
