// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.util.Key
import com.jetbrains.python.inspections.quickfix.PyChangeSignatureQuickFix
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.JDialog
import javax.swing.JLabel

class PythonQuickFixesRefactoringLesson(module: Module) : KLesson("quick-fix-refactoring", "Quick Fix Refactoring", module, "Python") {
  private val sample = parseLessonSample("""
    def foo(x):
        print("Hello ", x)
    
    y = 20
    foo(10<caret>)
    foo(30)
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    task {
      text("Several refactorings can be performed as quick fixes. Suppose we want to add a parameter to the method ${code("foo")} " +
           "and pass the variable ${code("y")} to it. Let's type ${code(", y")} after the first argument.")
      stateCheck {
        editor.document.text == StringBuilder(sample.text).insert(sample.startOffset, ", y").toString()
      }
      test { type(", y") }
    }

    task {
      text("Wait a little bit for completion list...")
      triggerByListItemAndHighlight(highlightBorder = false, highlightInside = false) { item ->
        item.toString().contains("string=y")
      }
    }

    task {
      text("But we do not need completion now. Close it (${action("EditorEscape")}).")
      stateCheck { previous.ui?.isShowing != true }
      test { GuiTestUtil.shortcut(Key.ESCAPE) }
    }

    task("ShowIntentionActions") {
      text("As you may notice IDE is showing you a warning here. Let's invoke intentions by ${action(it)}.")
      triggerByListItemAndHighlight(highlightBorder = true, highlightInside = false) { item ->
        item.toString().contains("Change signature of")
      }
      test {
        Thread.sleep(500) // need to check the intention is ready
        actions(it)
      }
    }
    task {
      text("Choose <strong>Change signature</strong> quick fix.")

      triggerByUiComponentAndHighlight(highlightBorder = false, highlightInside = false) { dialog: JDialog ->
        dialog.title == "Change Signature"
      }
      test {
        if (false) {
          ideFrame {
            jListContains("Change signature of").item("Change signature of").doubleClick()
          }
        }
        GuiTestUtil.shortcut(Key.DOWN)
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
    task {
      text("Let's set the default value for the new parameter. Click at the new parameter line. " +
           "Alternatively you can set focus to the parameter without mouse by ${action("EditorTab")} and then ${action("EditorEnter")}.")

      triggerByUiComponentAndHighlight(highlightBorder = false, highlightInside = false) { label: JLabel ->
        label.text == "Default value:"
      }
      test {
        GuiTestUtil.shortcut(Key.TAB)
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
    task {
      lateinit var beforeRefactoring: String
      before {
        beforeRefactoring = editor.document.text
      }
      text("You may navigate through the fields (and the checkbox) by ${action("EditorTab")}. " +
           "By the checkbox you can choose whether IDE will inline the default values to the other callers or set it as default value for the new parameter. " +
           "The Signature Preview will help to understand the difference. Now set the default value as 0 and press <raw_action>Ctrl + Enter</raw_action> " +
           "(or click <strong>Do Refactor</strong>) to finish the refactoring.")

      stateCheck {
        val b = editor.document.text != beforeRefactoring
        b && Thread.currentThread().stackTrace.any {
          it.className == PyChangeSignatureQuickFix::class.java.name
        }
      }

      test {
        GuiTestUtil.shortcut(Key.TAB)
        GuiTestUtil.shortcut(Key.BACK_SPACE)
        type("0")
        GuiTestUtil.shortcut(Key.ENTER)
        with(TaskTestContext.guiTestCase) {
          dialog("Change Signature") {
            button("Refactor").click()
          }
        }
      }
    }
  }
}
