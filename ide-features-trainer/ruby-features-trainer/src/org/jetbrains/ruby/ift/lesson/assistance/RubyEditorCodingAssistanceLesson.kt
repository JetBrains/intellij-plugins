// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.assistance

import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.impl.jList
import org.jetbrains.plugins.ruby.RBundle
import training.learn.interfaces.Module
import training.learn.lesson.general.assistance.EditorCodingAssistanceLesson
import training.learn.lesson.kimpl.LessonSample

class RubyEditorCodingAssistanceLesson(module: Module, lang: String, sample: LessonSample) :
  EditorCodingAssistanceLesson(module, lang, sample) {

  override fun IdeFrameFixture.simulateErrorFixing() {
    jList(intentionDisplayName).item(intentionDisplayName).doubleClick()
  }

  override val fixedText: String = "cat.say_meow()"

  override val intentionDisplayName: String
    get() = RBundle.message("inspection.argcount.extra.argument.fix")

  override val variableNameToHighlight: String = "happiness"
}