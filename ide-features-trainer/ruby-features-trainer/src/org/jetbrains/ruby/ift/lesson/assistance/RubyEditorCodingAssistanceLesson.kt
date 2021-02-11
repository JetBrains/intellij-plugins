// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.assistance

import org.jetbrains.plugins.ruby.RBundle
import training.learn.lesson.general.assistance.EditorCodingAssistanceLesson
import training.learn.lesson.kimpl.LessonSample

class RubyEditorCodingAssistanceLesson(lang: String, sample: LessonSample) :
  EditorCodingAssistanceLesson(lang, sample) {
  override val errorIntentionText: String
    get() = RBundle.message("inspection.argcount.extra.argument.fix")
  override val warningIntentionText: String
    get() = RBundle.message("inspection.parentheses.around.conditional.message")

  override val errorFixedText: String = "cat.say_meow()"
  override val warningFixedText: String = "cat.say_meow\n"

  override val variableNameToHighlight: String = "happiness"
}