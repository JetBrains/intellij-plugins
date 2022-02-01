// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.basic

import org.jetbrains.plugins.ruby.RBundle
import training.dsl.LessonSample
import training.dsl.parseLessonSample
import training.learn.lesson.general.SurroundAndUnwrapLesson

class RubySurroundAndUnwrapLesson : SurroundAndUnwrapLesson() {
  override val sample: LessonSample = parseLessonSample("""
    def surround_and_unwrap_demo
      <select>print 'Surround and Unwrap me!'</select>
    end
  """.trimIndent())

  override val surroundItems = arrayOf("if")

  override val lineShiftBeforeUnwrap = 1

  override val surroundItemName: String
    get() = "if...end"

  override val unwrapTryText: String = RBundle.message("unwrap.if")
}
