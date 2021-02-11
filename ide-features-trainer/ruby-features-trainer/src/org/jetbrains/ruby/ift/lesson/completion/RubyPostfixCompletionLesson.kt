// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.completion

import com.intellij.testGuiFramework.framework.GuiTestUtil.typeText
import com.intellij.testGuiFramework.impl.jList
import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.learn.LessonsBundle
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil.checkExpectedStateOfEditor
import training.learn.lesson.kimpl.defaultRestoreDelay
import training.learn.lesson.kimpl.parseLessonSample

class RubyPostfixCompletionLesson
  : KLesson("Postfix completion", LessonsBundle.message("postfix.completion.lesson.name"), "ruby") {

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

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      task {
        text(LessonsBundle.message("postfix.completion.type.template", code(".if")))
        triggerByListItemAndHighlight {
          it.toString() == ".if"
        }
        proposeRestore {
          checkExpectedStateOfEditor(sample) { ".if".startsWith(it) }
        }
        test {
          ideFrame {
            typeText(".if")
          }
        }
      }
      task {
        text(RubyLessonsBundle.message("ruby.postfix.completion.apply", action("EditorChooseLookupItem")))
        triggerByListItemAndHighlight {
          it.toString() == "string_array.length > 1"
        }
        restoreByUi(delayMillis = defaultRestoreDelay)
        test {
          ideFrame {
            jList("if").item(0).doubleClick()
          }
        }
      }

      task("string_array.length > 1") {
        text(RubyLessonsBundle.message("ruby.postfix.completion.choose.target", code(it)))
        stateCheck { editor.document.text == result }
        restoreByUi()
        test {
          ideFrame {
            jList(it).click()
          }
        }
      }
    }
}