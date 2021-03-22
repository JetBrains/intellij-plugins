// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.debug.lesson

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.RunManager
import com.intellij.javascript.ift.debug.JsDebugLessonsBundle
import com.intellij.javascript.ift.debug.setLanguageLevel
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.ui.UIBundle
import com.intellij.ui.treeStructure.Tree
import training.dsl.*
import training.learn.course.KLesson
import training.ui.LearningUiHighlightingManager
import javax.swing.JButton
import javax.swing.tree.DefaultMutableTreeNode

class BeforeDebuggingLesson
  : KLesson("Before Debugging: Run/Debug Configurations", JsDebugLessonsBundle.message("js.debugger.before.title")) {

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
          text(JsDebugLessonsBundle.message("js.debugger.before.intro.1"))
          text(JsDebugLessonsBundle.message("js.debugger.before.intro.2",
                                            "https://nodejs.org/en/", strong("Different!"), strong("Equal!"),
                                            "https://nodejs.org/en/download/", action(it)))
          trigger(it)
        }
        task("HideActiveWindow") {
          text(
            JsDebugLessonsBundle.message("js.debugger.before.describe.tool.window",
                                         action("RunClass"), strong(UIBundle.message("tool.window.name.run")), action(it)))
          checkToolWindowState("Run", false)
        }

        task {
          triggerByUiComponentAndHighlight<JButton> { ui ->
            ui.text == existedFile
          }
        }
        task {
          text(JsDebugLessonsBundle.message("js.debugger.before.save.1"))
          text(JsDebugLessonsBundle.message("js.debugger.before.save.2", strong("debugging.js"), strong(ExecutionBundle.message("save.temporary.run.configuration.action.name", "debugging.js").dropMnemonic())))
          stateCheck {
            val selectedConfiguration = RunManager.getInstance(project).selectedConfiguration ?: return@stateCheck false
            !selectedConfiguration.isTemporary
          }
        }
        task {
          LearningUiHighlightingManager.clearHighlights()
          text(JsDebugLessonsBundle.message("js.debugger.before.edit", strong("debugging.js"), strong(ExecutionBundle.message("edit.configuration.action").dropMnemonic())))
          stateCheck {
            (focusOwner as? Tree)?.model?.javaClass?.name?.contains("RunConfigurable") ?: false
          }
        }

        task {
          text(JsDebugLessonsBundle.message("js.debugger.before.manage.1", strong("+")))
          text(JsDebugLessonsBundle.message("js.debugger.before.manage.2"))
          stateCheck {
            focusOwner is EditorComponentImpl
          }
        }
        text(JsDebugLessonsBundle.message("js.debugger.before.next", LessonUtil.rawEnter()))

      }
    }
  override val existedFile = "debugging.js"
}


