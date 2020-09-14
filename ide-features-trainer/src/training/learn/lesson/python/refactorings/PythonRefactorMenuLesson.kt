// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.refactorings

import com.intellij.idea.ActionsBundle
import com.intellij.refactoring.rename.inplace.InplaceRefactoring
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import training.commands.kotlin.TaskRuntimeContext
import training.learn.LearnBundle
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*
import javax.swing.JList

class PythonRefactorMenuLesson(module: Module)
  : KLesson("Refactoring menu", LessonsBundle.message("refactoring.menu.lesson.name"), module, "Python") {
  private val sample = parseLessonSample("""
    # Need to think about better sample!
    import random
    
    
    def foo(x):
        print(x + <select>random</select>)
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    actionTask("Refactorings.QuickListPopupAction") {
      LessonsBundle.message("python.refactoring.menu.show.refactoring.list", LessonUtil.productName, action(it))
    }
    task(ActionsBundle.message("action.IntroduceParameter.text").dropMnemonic()) {
      val filter = LearnBundle.message("refactoring.menu.introduce.parameter.filter")
      text(LessonsBundle.message("python.refactoring.menu.introduce.parameter", strong(it), strong(filter)))
      triggerByUiComponentAndHighlight(highlightBorder = false, highlightInside = false) { ui: JList<*> ->
        ui.model.size > 0 && ui.model.getElementAt(0).toString().contains(it)
      }
      test {
        type(filter)
      }
    }

    task {
      text(LessonsBundle.message("python.refactoring.menu.start.refactoring", action("EditorChooseLookupItem")))
      trigger("IntroduceParameter")
      stateCheck { hasInplaceRename() }
      test {
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }

    task {
      text(LessonsBundle.message("python.refactoring.menu.finish.refactoring", LessonUtil.rawEnter()))
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
