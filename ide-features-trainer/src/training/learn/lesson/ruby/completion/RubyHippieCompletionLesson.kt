// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.completion

import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RubyHippieCompletionLesson(module: Module) : KLesson("Hippie Completion", module, "ruby") {
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
        text("Sometimes you need to complete a word by textual similarity. Press ${action(it)} to invoke hippie completion.")
        trigger(it) { editor.document.text == step1 }
        test { actions(it) }
      }
      task("HippieCompletion") {
        text("You can repeat ${action(it)} until the desired word is found. Try it once more now.")
        trigger(it) { editor.document.text == step2 }
        test { actions(it) }
      }
      task("HippieBackwardCompletion") {
        text("You can return to the previous variant with ${action(it)}. Use it now.")
        trigger(it) { editor.document.text == step1 }
        test { actions(it) }
      }
    }

  private fun calculateResult(insert: String) =
    StringBuffer(sample.text).insert(sample.startOffset, insert).toString()
}