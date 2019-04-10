package training.learn.lesson.general

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import javax.swing.JPanel

class GotoActionLesson(module: Module, lang: String, private val sample: LessonSample) :
    KLesson("Actions", module, lang) {
  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      actionTask("GotoAction") {
        "One of the most important IDE feature is feature discoverability. " +
            "All shortcut actions and much more could be found by ${action(it)}. " +
            "Try to use now."
      }
      actionTask("About") {
        "Suppose you want to read about IDE. Type <strong>about</strong> now and press Enter."
      }
      task {
        text("Return to the editor.")
        stateCheck { focusOwner is EditorComponentImpl }
        test {
          ideFrame {
            waitComponent(JPanel::class.java, "InfoSurface")
            // Note 1: it is editor from test IDE fixture
            // Note 2: In order to pass this task without interference with later task I need to firstly focus lesson
            // and only then press Escape
            editor.requestFocus()
            GuiTestUtil.shortcut(Key.ESCAPE)
          }
        }
      }
      actionTask("GotoAction") {
        "Also ${action(it)} could be used to change some settings. Use it one more time."
      }
      task("line num") {
        text("Type <strong>$it</strong> and try to toggle <strong>Show line number</strong> option.")
        stateCheck { checkWordInSearch(it) }
        test {
          waitComponent(SearchEverywhereUI::class.java, "SearchEverywhere")
          type(it)
        }
      }

      // This code works for "Enter" but does not work for mouse click option change.
      // There currently no way to trigger on the setting change in IDEA :(
      val lineNumbersShown = isLineNumbersShown()
      task {
        text("Try to switch ${if (lineNumbersShown) "off" else "on"} line numbers.")
        stateCheck { isLineNumbersShown() == !lineNumbersShown }
        test {
          Thread.sleep(300) // there could be a more proper wait solution
          GuiTestUtil.shortcut(Key.ENTER)
        }
      }
      task {
        text("Switch ${if (lineNumbersShown) "on" else "off"} back line numbers.")
        stateCheck { isLineNumbersShown() == lineNumbersShown }
        test {
          Thread.sleep(300) // there could be a more proper wait solution
          GuiTestUtil.shortcut(Key.ENTER)
        }
      }

      task {
        text("Awesome! Click the button below to start the next lesson, or use ${action("learn.next.lesson")}.")
      }
  }

  private fun isLineNumbersShown() = EditorSettingsExternalizable.getInstance().isLineNumbersShown

  private fun TaskContext.checkWordInSearch(expected: String): Boolean =
      (focusOwner as? ExtendableTextField)?.text?.toLowerCase() == expected.toLowerCase()
}