// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.assistance

import org.jetbrains.plugins.ruby.RBundle
import training.commands.kotlin.TaskRuntimeContext
import training.learn.interfaces.Module
import training.learn.lesson.general.assistance.EditorCodingAssistanceLesson
import training.learn.lesson.kimpl.LessonSample

class RubyEditorCodingAssistanceLesson(module: Module, lang: String, sample: LessonSample) :
  EditorCodingAssistanceLesson(module, lang, sample) {

  override fun TaskRuntimeContext.checkErrorFixed(): Boolean {
    return editor.document.charsSequence.contains("cat.say_meow()")
  }

  override val intentionDisplayName: String
    get() = RBundle.message("inspection.argcount.extra.argument.fix")

  override val variableNameToHighlight: String = "@happiness"
}