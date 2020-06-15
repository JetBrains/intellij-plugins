// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.refactoring.rename.inplace.InplaceRefactoring
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import training.commands.kotlin.TaskRuntimeContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.JList

class PythonRefactorMenuLesson(module: Module) : KLesson("Refactoring Menu", module, "Python") {
  private val sample = parseLessonSample("""
    # Need to think about better sample!
    import random
    
    
    def foo(x):
        print(x + <select>random</select>)
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    actionTask("Refactorings.QuickListPopupAction") {
      "PyCharm supports a variety of refactorings. Many of them have own shortcuts. " +
      "But for rare refactorings you can use ${action(it)} and see a partial list of them."
    }
    task("Introduce Parameter") {
      text("Suppose we want to replace this expression by parameter. So we need to choose <strong>$it...</strong>. " +
           "Now simply type ${code("pa")} (as prefix of Parameter) to reduce proposed list.")
      triggerByUiComponentAndHighlight(highlightBorder = false, highlightInside = false) { ui: JList<*> ->
        ui.model.size > 0 && ui.model.getElementAt(0).toString().contains(it)
      }
      test {
        type("pa")
      }
    }

    task {
      text("Press ${action("EditorEnter")} to start Introduce Parameter refactoring.")
      trigger("IntroduceParameter")
      stateCheck { hasInplaceRename() }
      test {
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }

    task {
      text("To complete refactoring you need to choose some name or leave it as default and press ${action("EditorEnter")}.")
      stateCheck {
        !hasInplaceRename()
      }
      test {
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
  }

  private fun TaskRuntimeContext.hasInplaceRename() = editor.getUserData(InplaceRefactoring.INPLACE_RENAMER) != null
}
