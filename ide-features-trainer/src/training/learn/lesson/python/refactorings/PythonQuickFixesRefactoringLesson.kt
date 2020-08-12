// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.util.Key
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.table.JBTableRow
import com.jetbrains.python.inspections.quickfix.PyChangeSignatureQuickFix
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
import training.learn.lesson.kimpl.LessonUtil.checkExpectedStateOfEditor
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.JDialog
import javax.swing.JTable

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
        checkExpectedStateOfEditor(previous.sample)
      }
      test {
        Thread.sleep(500) // need to check the intention is ready
        actions(it)
      }
    }
    task {
      text("Choose <strong>Change signature</strong> quick fix.")

      triggerByPartOfComponent { table : JTable ->
        val model = table.model
        if (model.rowCount >= 2 && (model.getValueAt(1, 0) as? JBTableRow)?.getValueAt(0) == "y") {
          table.getCellRect(1, 0, true)
        }
        else null
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

      val selector = { collection: Collection<EditorComponentImpl> ->
        collection.takeIf { it.size > 2 }?.maxBy { it.locationOnScreen.x }
      }
      triggerByUiComponentAndHighlight(selector = selector) { editor : EditorComponentImpl ->
        UIUtil.getParentOfType(JDialog::class.java, editor) != null
      }
      restoreByUi()
      test {
        invokeAndWaitIfNeeded(ModalityState.any()) {
          val ui = previous.ui ?: return@invokeAndWaitIfNeeded
          IdeFocusManager.getInstance(project).requestFocus(ui, true)
        }
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
    task {
      text("You may navigate through the fields (and the checkbox) by using ${action("EditorTab")}. " +
           "With the checkbox you can let IDE inline the default value to the other callers or set it as the default value for the new parameter. " +
           "The Signature Preview will help understand the difference. Now set the default value as 0.")
      restoreByUi()
      stateCheck {
        (previous.ui as? EditorComponentImpl)?.text == "0"
      }
      test {
        invokeAndWaitIfNeeded(ModalityState.any()) {
          val ui = previous.ui ?: return@invokeAndWaitIfNeeded
          IdeFocusManager.getInstance(project).requestFocus(ui, true)
        }
        GuiTestUtil.shortcut(Key.BACK_SPACE)
        type("0")
        //ideFrame {
        //  previous.ui?.let { usagesTab -> jComponent(usagesTab).click() }
        //}
      }
    }

    task {
      lateinit var beforeRefactoring: String
      before {
        beforeRefactoring = editor.document.text
      }
      text("Press <raw_action>${if (SystemInfo.isMacOSMojave) "\u2318\u23CE" else "Ctrl + Enter"}</raw_action> " +
           "(or click <strong>Refactor</strong>) to finish the refactoring.")

      stateCheck {
        val b = editor.document.text != beforeRefactoring
        b && stackInsideDialogRefactoring()
      }
      restoreState(500) {
        !stackInsideDialogRefactoring()
      }

      test {
        with(TaskTestContext.guiTestCase) {
          dialog("Change Signature") {
            button("Refactor").click()
          }
        }
      }
    }
  }

  private fun stackInsideDialogRefactoring(): Boolean {
    return Thread.currentThread().stackTrace.any {
      it.className == PyChangeSignatureQuickFix::class.java.name
    }
  }

  private fun TaskContext.proposeMyRestore() {
    proposeRestore {
      checkExpectedStateOfEditor(sample) { ", y".startsWith(it) }
    }
  }
}
