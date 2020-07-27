// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.util.Key
import com.jetbrains.python.inspections.quickfix.PyChangeSignatureQuickFix
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.JDialog
import javax.swing.JLabel

class PythonQuickFixesRefactoringLesson(module: Module) : KLesson("refactoring.quick.fix", "Quick fix refactoring", module, "Python") {
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
      proposeMyRestore()
      test { type(", y") }
    }

    task {
      text("Wait a little bit for the completion list...")
      triggerByListItemAndHighlight(highlightBorder = false, highlightInside = false) { item ->
        item.toString().contains("string=y")
      }
      proposeMyRestore()
    }

    task {
      text("For now, we don't want to apply any completion. Close the list (${action("EditorEscape")}).")
      stateCheck { previous.ui?.isShowing != true }
      proposeMyRestore()
      test { GuiTestUtil.shortcut(Key.ESCAPE) }
    }

    prepareRuntimeTask { // restore point
      prepareSample(previous.sample)
    }

    task("ShowIntentionActions") {
      text("As you may notice, IDE is showing you a warning here. Let's invoke intentions by ${action(it)}.")
      triggerByListItemAndHighlight(highlightBorder = true, highlightInside = false) { item ->
        item.toString().contains("Change signature of")
      }
      proposeRestore {
        LessonUtil.checkExpectedStateOfEditor(editor, previous.sample) { change -> change.isEmpty() }
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
      restoreByUi(500)
      test {
        GuiTestUtil.shortcut(Key.DOWN)
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
    task {
      text("Let's set the default value for the new parameter. Click at the new parameter line. " +
           "Alternatively, you can set focus to the parameter without mouse by ${action("EditorTab")} and then ${action("EditorEnter")}.")

      triggerByUiComponentAndHighlight(highlightBorder = false, highlightInside = false) { label: JLabel ->
        label.text == "Default value:"
      }
      restoreByUi()
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
      text("You may navigate through the fields (and the checkbox) by using ${action("EditorTab")}. " +
           "With the checkbox you can let IDE inline the default value to the other callers or set it as the default value for the new parameter. " +
           "The Signature Preview will help understand the difference. Now set the default value as 0 and press <raw_action>Ctrl + Enter</raw_action> " +
           "(or click <strong>Do Refactor</strong>) to finish the refactoring.")

      stateCheck {
        val b = editor.document.text != beforeRefactoring
        b && Thread.currentThread().stackTrace.any {
          it.className == PyChangeSignatureQuickFix::class.java.name
        }
      }
      restoreByUi(500)

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

  private fun TaskContext.proposeMyRestore() {
    proposeRestore {
      LessonUtil.checkExpectedStateOfEditor(editor, sample) { change ->
        ", y".startsWith(change)
      }
    }
  }
}
