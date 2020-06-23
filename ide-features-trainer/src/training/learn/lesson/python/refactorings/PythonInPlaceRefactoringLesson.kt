// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.icons.AllIcons
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*
import javax.swing.JLabel
import javax.swing.JPanel

class PythonInPlaceRefactoringLesson(module: Module) : KLesson("In Place Refactoring", module, "Python") {
  private val template = """
    def fibonacci(stop):
        first = 0
        <name><caret> = 1
        while <name> < stop:
            print(<name>)
            first, <name> = <name>, first + <name>

    n = int(input("n = "))
    fibonacci(n)
  """.trimIndent() + '\n'

  private val variableName = "s"

  private val sample = parseLessonSample(template.replace("<name>", variableName))

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    task {
      text("Let's consider an alternative approach to perform refactorings. Suppose we want to rename local variable ${code(variableName)} " +
           "to ${code("second")}. Just start typing the new name.")
      stateCheck {
        editor.document.text != sample.text
      }
      test { type("econd") }
    }

    task("ShowIntentionActions") {
      text("IDE is guessing that you are going to rename the variable. " +
           "You can notice it by the icon ${icon(AllIcons.Gutter.SuggestedRefactoringBulb)} at the left editor gutter. " +
           "Invoke intentions by ${action(it)} when you finish to type the new name.")
      triggerByListItemAndHighlight(highlightBorder = true, highlightInside = false) { ui -> // no highlighting
        ui.toString().contains("Rename usages")
      }
      test {
        Thread.sleep(500) // need to check the intention is ready
        actions(it)
      }
    }

    task {
      val prefix = template.indexOf("<name>")
      text("Press ${action("EditorEnter")} to finish rename.")
      stateCheck {
        val newName = newName(editor.document.charsSequence, prefix)
        val expected = template.replace("<caret>", "").replace("<name>", newName)
        newName != variableName && editor.document.text == expected
      }
      test { GuiTestUtil.shortcut(Key.ENTER) }
    }

    waitBeforeContinue(500)

    caret(template.indexOf("stop") + 4)

    lateinit var secondSample: LessonSample
    prepareRuntimeTask {
      secondSample = prepareSampleFromCurrentState(editor)
    }

    task {
      text("Let's add an argument to this method. We place the editor caret just after the first parameter. " +
           "Now type comma and parameter name: ${code(", start")} .")
      stateCheck {
        val text = editor.document.text
        val parameter = text.substring(secondSample.startOffset, text.indexOf(')'))
        val parts = parameter.split(" ")
        parts.size == 2 && parts[0] == "," && parts[1].isNotEmpty() && parts[1].all { it.isJavaIdentifierPart() }
      }
      test { type(", start") }
    }

    task("ShowIntentionActions") {
      text("IDE is guessing that you are going to change the method signature. " +
           "You can notice it by the same icon ${icon(AllIcons.Gutter.SuggestedRefactoringBulb)} at the left editor gutter. " +
           "Invoke intentions by ${action(it)} when you finish to type the new parameter.")
      triggerByListItemAndHighlight(highlightBorder = true, highlightInside = false) { item ->
        item.toString().contains("Update usages to")
      }
      test {
        Thread.sleep(500) // need to check the intention is ready
        actions(it)
      }
    }

    task {
      text("Press ${action("EditorEnter")} to update the callers.")
      triggerByUiComponentAndHighlight<JPanel>(highlightBorder = false, highlightInside = false) { ui -> // no highlighting
        ui.javaClass.name.contains("ChangeSignaturePopup")
      }
      test { GuiTestUtil.shortcut(Key.ENTER) }
    }


    task {
      text("IDE is showing you the short signature preview. Press ${action("EditorEnter")} to continue.")
      triggerByUiComponentAndHighlight<JLabel>(highlightBorder = false, highlightInside = false) { ui -> // no highlighting
        ui.text == "Add values for new parameters:"
      }
      test { GuiTestUtil.shortcut(Key.ENTER) }
    }
    task {
      lateinit var beforeSecondRefactoring: String
      before {
        beforeSecondRefactoring = editor.document.text
      }
      text("Now you need to type the value which will be inserted as an argument to the each call. " +
           "You can choose ${code("0")} for this sample. Then press ${action("EditorEnter")} to continue.")
      stateCheck {
        editor.document.text != beforeSecondRefactoring && Thread.currentThread().stackTrace.any {
          it.className.contains("PySuggestedRefactoringExecution") && it.methodName == "performChangeSignature"
        }
      }
      test {
        type("0")
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
    task { text("A small note for the end. In-place refactoring may be applied only in the definition point whiles direct invocation" +
                " of rename or change-signature refactorings may be called from both definition and usage.") }
  }

  private fun newName(text: CharSequence, prefix: Int): String {
    var i = prefix
    val result = StringBuffer()
    while (i < text.length && text[i].isJavaIdentifierPart()) {
      result.append(text[i])
      i++
    }
    return result.toString()
  }
}
