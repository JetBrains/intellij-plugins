// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.completion

import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonUtil.restoreIfModifiedOrMoved
import training.dsl.parseLessonSample
import training.learn.course.KLesson

class RubyHippieCompletionLesson
  : KLesson("Hippie Completion", RubyLessonsBundle.message("ruby.hippie.completion.lesson.name")) {

  private val sample = parseLessonSample("""class SomeExampleClass
  attr_reader :callbacks

  def initialize
    @callbacks = [:before_create, :before_save]
  end

  def be<caret>
end
""".trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      val step1 = calculateResult("fore_save")
      val step2 = calculateResult("fore_create")
      task("HippieCompletion") {
        text(RubyLessonsBundle.message("ruby.hippie.completion.invoke.hippie.completion", action(it)))
        trigger(it) { editor.document.text == step1 }
        restoreIfModifiedOrMoved()
        test { actions(it) }
      }
      task("HippieCompletion") {
        text(RubyLessonsBundle.message("ruby.hippie.completion.repeat.one.time", action(it)))
        trigger(it) { editor.document.text == step2 }
        restoreIfModifiedOrMoved()
        test { actions(it) }
      }
      task("HippieBackwardCompletion") {
        text(RubyLessonsBundle.message("ruby.hippie.completion.return.previous", action(it)))
        trigger(it) { editor.document.text == step1 }
        restoreIfModifiedOrMoved()
        test { actions(it) }
      }
    }

  private fun calculateResult(insert: String) =
    StringBuffer(sample.text).insert(sample.startOffset, insert).toString()

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(RubyLessonsBundle.message("ruby.hippie.completion.help.link"),
         "https://www.jetbrains.com/help/ruby/auto-completing-code.html#hippie_completion"),
  )
}