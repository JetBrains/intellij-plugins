// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.basic

import training.learn.interfaces.Module
import training.learn.lesson.general.SurroundAndUnwrapLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class RubySurroundAndUnwrapLesson(module: Module) : SurroundAndUnwrapLesson(module, "ruby") {
  override val sample: LessonSample = parseLessonSample("""
    def surround_and_unwrap_demo
      <select>print 'Surround and Unwrap me!'</select>
    end
  """.trimIndent())

  override val surroundItems = arrayOf("if")

  override val lineShiftBeforeUnwrap = 1

  override val surroundItemName: String
    get() = "if...end"
}
