// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.basic

import training.dsl.LessonSample
import training.dsl.parseLessonSample
import training.learn.LessonsBundle
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

  override val helpLinks: Map<String, String> = mapOf(
    Pair(LessonsBundle.message("surround.and.unwrap.help.surround.code.fragments"), "https://www.jetbrains.com/help/ruby/generating-code.html#surround_code_with_language_constructs"),
    Pair(LessonsBundle.message("surround.and.unwrap.help.unwrapping.and.removing.statements"), "https://www.jetbrains.com/help/ruby/generating-code.html#unwrap_statement"),
  )
}
