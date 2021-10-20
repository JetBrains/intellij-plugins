// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.completion

import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonUtil
import training.dsl.LessonUtil.checkExpectedStateOfEditor
import training.dsl.defaultRestoreDelay
import training.dsl.parseLessonSample
import training.learn.LessonsBundle
import training.learn.course.KLesson

class RubyPostfixCompletionLesson
  : KLesson("Postfix completion", LessonsBundle.message("postfix.completion.lesson.name")) {

  private val sample = parseLessonSample("""class SomeExampleClass
  # @param string_array [Array<String>]
  def second_value(string_array)
     string_array.length > 1<caret>
  end
end
""".trimIndent())
  private val result = parseLessonSample("""class SomeExampleClass
  # @param string_array [Array<String>]
  def second_value(string_array)
    if string_array.length > 1
      <caret>
    end
  end
end
""".trimIndent()).text

  private val completionSuffix = ".if"

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      task {
        text(LessonsBundle.message("postfix.completion.intro") + " " + RubyLessonsBundle.message("ruby.postfix.completion.type",
                                                                                                 code(completionSuffix)))
        triggerByListItemAndHighlight {
          it.toString() == completionSuffix
        }
        proposeRestore {
          checkExpectedStateOfEditor(sample) { completionSuffix.startsWith(it) }
        }
        test {
          type(completionSuffix)
        }
      }
      task {
        text(RubyLessonsBundle.message("ruby.postfix.completion.apply", action("EditorChooseLookupItem")))
        triggerByListItemAndHighlight {
          it.toString() == "string_array.length > 1"
        }
        restoreByUi(delayMillis = defaultRestoreDelay)
        test(waitEditorToBeReady = false) {
          ideFrame {
            jList("if").item(0).doubleClick()
          }
        }
      }

      task("string_array.length > 1") {
        text(RubyLessonsBundle.message("ruby.postfix.completion.choose.target", code(it)))
        stateCheck { editor.document.text == result }
        restoreByUi()
        test(waitEditorToBeReady = false) {
          ideFrame {
            jList(it).click()
          }
        }
      }
    }

  override val helpLinks: Map<String, String> get() = mapOf(
    Pair(LessonsBundle.message("postfix.completion.help.link"),
         "https://www.jetbrains.com/help/${LessonUtil.helpIdeName}/auto-completing-code.html#postfix_completion"),
  )
}