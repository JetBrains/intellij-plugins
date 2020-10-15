// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.debugger

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.RunManager
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.ui.UIBundle
import com.intellij.ui.treeStructure.Tree
import training.lang.JavaScriptLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.dropMnemonic
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.tree.DefaultMutableTreeNode

class BeforeDebuggingLesson(module: Module)
  : KLesson("Before Debugging: Run/Debug Configurations", LessonsBundle.message("js.debugger.before.title"), module, JavaScriptLangSupport.lang) {

  companion object {
    val jsDebuggerSample = parseLessonSample("""
        function compareNumbers(a, b) {
            if (a === b) {
                return "Different!";
            } else {
                return "Equal!"
            }
        }

        console.log(compareNumbers(10, -20));
        """.trimIndent())
  }


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(jsDebuggerSample)
        task("RunClass") {
          text(LessonsBundle.message("js.debugger.before.intro",
                                     "https://nodejs.org/en/", strong("Different!"), strong("Equal!"),
                                     "https://nodejs.org/en/download/", action(it)))
          trigger(it)
        }
        task("HideActiveWindow") {
          text(
            LessonsBundle.message("js.debugger.before.describe.tool.window",
                                  action("RunClass"), strong(UIBundle.message("tool.window.name.run")), action(it)))
          trigger(it)
        }
        task {
          text(LessonsBundle.message("js.debugger.before.save", strong("debugging.js"), strong(ExecutionBundle.message("save.temporary.run.configuration.action.name", "debugging.js").dropMnemonic())))
          stateCheck {
            val selectedConfiguration = RunManager.getInstance(project).selectedConfiguration ?: return@stateCheck false
            !selectedConfiguration.isTemporary
          }

        }
        task {
          text(LessonsBundle.message("js.debugger.before.edit", strong("debugging.js"), strong(ExecutionBundle.message("edit.configuration.action").dropMnemonic())))
          stateCheck {
            ((focusOwner as? Tree)?.model?.root as? DefaultMutableTreeNode)?.lastChild.toString() == "Templates"
          }
        }

        task {
          text(LessonsBundle.message("js.debugger.before.manage", strong("+")))
          stateCheck {
            focusOwner is EditorComponentImpl
          }
        }
        text(LessonsBundle.message("js.debugger.before.next", action("learn.next.lesson")))

      }
    }
  override val existedFile = "debugging.js"
}


