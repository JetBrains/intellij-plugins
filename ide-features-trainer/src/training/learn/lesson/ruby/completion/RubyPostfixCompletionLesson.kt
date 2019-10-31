/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.ruby.completion

import com.intellij.testGuiFramework.framework.GuiTestUtil.shortcut
import com.intellij.testGuiFramework.framework.GuiTestUtil.typeText
import com.intellij.testGuiFramework.impl.jList
import com.intellij.testGuiFramework.util.Key
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RubyPostfixCompletionLesson(module: Module) : KLesson("Postfix Completion", module, "ruby") {
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
        text("The IDE can offer postfix shortcuts. Type ${code(".if")} and press ${action("EditorEnter")}.")
        trigger("EditorChooseLookupItem")
        test {
          ideFrame {
            typeText(".if")
            jList("if")
            shortcut(Key.ENTER)
          }
        }
      }
      task("string_array.length > 1") {
        text("Now choose the second item, ${code(it)}.")
        stateCheck { editor.document.text == result }
        test {
          ideFrame {
            jList(it)
            shortcut(Key.DOWN)
            shortcut(Key.ENTER)
          }
        }
      }
    }

  override val existedFile = RubyLangSupport.sandboxFile
}