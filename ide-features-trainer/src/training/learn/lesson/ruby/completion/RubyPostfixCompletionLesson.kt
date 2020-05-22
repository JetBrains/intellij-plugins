// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.completion

import com.intellij.testGuiFramework.framework.GuiTestUtil.typeText
import com.intellij.testGuiFramework.impl.jList
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
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
        text("The IDE can offer postfix shortcuts. Type ${code(".if")}.")
        triggerByListItemAndHighlight {
          it.toString() == ".if"
        }
        proposeRestore {
          LessonUtil.checkExpectedStateOfEditor(editor, sample) {
            ".if".startsWith(it)
          }
        }
        test {
          ideFrame {
            typeText(".if")
          }
        }
      }
      task {
        text("Now just press ${action("EditorEnter")} to choose the first postfix template.")
        triggerByListItemAndHighlight {
          it.toString() == "string_array.length > 1"
        }
        restoreByUi(200)
        test {
          ideFrame {
            jList("if").item(0).doubleClick()
          }
        }
      }

      task("string_array.length > 1") {
        text("Now choose the second item, ${code(it)}.")
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