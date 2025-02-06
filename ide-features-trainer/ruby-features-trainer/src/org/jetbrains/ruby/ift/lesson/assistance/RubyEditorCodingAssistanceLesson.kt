// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.assistance

import com.intellij.codeInsight.daemon.impl.runActionCustomShortcutSet
import com.intellij.openapi.actionSystem.KeyboardShortcut
import org.jetbrains.plugins.ruby.RBundle
import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.dsl.LessonSample
import training.dsl.LessonUtil
import training.learn.lesson.general.assistance.EditorCodingAssistanceLesson

internal class RubyEditorCodingAssistanceLesson(sample: LessonSample) :
  EditorCodingAssistanceLesson(sample) {
  override val errorIntentionText: String
    get() = RBundle.message("ruby.inspection.incorrect.call.argument.count.excess.arguments.quickFix.name", 1)
  override val warningIntentionText: String
    get() = RBundle.message("inspection.parentheses.around.conditional.message")

  override val errorFixedText: String = "cat.say_meow()"
  override val warningFixedText: String = "cat.say_meow\n"

  override val variableNameToHighlight: String = "happiness"

  override fun getFixWarningText(): String {
    val shortcut = runActionCustomShortcutSet.shortcuts.first() as KeyboardShortcut
    return RubyLessonsBundle.message("ruby.editor.coding.assistance.press.to.fix", LessonUtil.rawKeyStroke(shortcut.firstKeyStroke))
  }
}