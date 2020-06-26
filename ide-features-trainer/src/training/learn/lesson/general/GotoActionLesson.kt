// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general

import com.intellij.ide.actions.AboutPopup
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereUIBase
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.util.SystemInfo
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.components.fields.ExtendableTextField
import training.commands.kotlin.TaskRuntimeContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import javax.swing.JPanel

class GotoActionLesson(module: Module, lang: String, private val sample: LessonSample) :
  KLesson("Actions", "Search for actions", module, lang) {

  companion object {
    private const val FIND_ACTION_WORKAROUND: String = "https://intellij-support.jetbrains.com/hc/en-us/articles/360005137400-Cmd-Shift-A-hotkey-opens-Terminal-with-apropos-search-instead-of-the-Find-Action-dialog"
  }

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      actionTask("GotoAction") {
        "One of the most useful shortcuts is <strong>Find Action</strong>. It lets you search through all available actions " +
                "without having to know their individual shortcuts.\nTry it now with ${action(it)}." +
                if (SystemInfo.isMacOSMojave) "\nIf <strong>Terminal</strong> search opens instead of <strong>Find Action</strong>, " +
                        "refer to <a href=\"$FIND_ACTION_WORKAROUND\">this article</a>." else ""
      }
      actionTask("About") {
        "Let's say you want to read about the IDE, type <strong>about</strong> and press ${action("EditorEnter")}."
      }
      task {
        text("Hit ${action("EditorEscape")} to return to the editor.")
        var aboutHasBeenFocused = false
        stateCheck {
          aboutHasBeenFocused = aboutHasBeenFocused || focusOwner is AboutPopup.PopupPanel
          aboutHasBeenFocused && focusOwner is EditorComponentImpl
        }
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
        "${action(it)} can also be used to change the settings, invoke it again now."
      }
      task("show line") {
        text("Type <strong>$it</strong> and toggle the <strong>Show line number</strong> option.")
        stateCheck { checkWordInSearch(it) }
        test {
          waitComponent(SearchEverywhereUIBase::class.java)
          type(it)
        }
      }

      val lineNumbersShown = isLineNumbersShown()
      task {
        text("Switch the line numbers ${if (lineNumbersShown) "off" else "on"}.")
        stateCheck { isLineNumbersShown() == !lineNumbersShown }
        test {
          Thread.sleep(300) // there could be a more proper wait solution
          GuiTestUtil.shortcut(Key.ENTER)
        }
      }
      task {
        text("Now switch the line numbers back ${if (lineNumbersShown) "on" else "off"}.")
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

  private fun TaskRuntimeContext.checkWordInSearch(expected: String): Boolean =
    (focusOwner as? ExtendableTextField)?.text?.toLowerCase()?.contains(expected.toLowerCase()) == true
}