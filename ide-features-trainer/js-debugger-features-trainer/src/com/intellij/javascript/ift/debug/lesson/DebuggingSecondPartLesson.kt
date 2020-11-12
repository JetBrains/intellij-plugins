// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.debug.lesson

import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.javascript.ift.debug.JsDebugLessonsBundle
import com.intellij.javascript.ift.debug.setLanguageLevel
import com.intellij.xdebugger.XDebuggerBundle
import training.learn.interfaces.Module
import training.learn.js.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil.productName
import training.learn.lesson.kimpl.dropMnemonic

class DebuggingSecondPartLesson(module: Module)
  : KLesson("Debugging Code. Part II", JsDebugLessonsBundle.message("js.debugger.part.2.title"), module, "JavaScript") {

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(BeforeDebuggingLesson.jsDebuggerSample)
        task("StepInto") {
          text(JsDebugLessonsBundle.message("js.debugger.part.2.step.into",
                                            action("DebugClass"),
                                            strong(XDebuggerBundle.message("xdebugger.debugger.tab.title")),
                                            ActionsBundle.message("action.Resume.text").dropMnemonic(),
                                            icon(AllIcons.Actions.Resume),
                                            action(it), ActionsBundle.message("action.StepInto.text").dropMnemonic(),
                                            icon(AllIcons.Actions.TraceInto)))
          trigger(it)
        }

        task("EvaluateExpression") {
          text(JsDebugLessonsBundle.message("js.debugger.part.2.buttons",
                                            ActionsBundle.message("action.StepOver.text").dropMnemonic(),
                                            action("StepOver"),
                                            strong(ActionsBundle.message("action.SmartStepInto.text").dropMnemonic()),
                                            action("SmartStepInto"),
                                            strong(ActionsBundle.message("action.StepOut.text").dropMnemonic()),
                                            action("StepOut"),
                                            strong(XDebuggerBundle.message("xdebugger.dialog.title.evaluate.expression")),
                                            action(it)))
          trigger(it)
        }

        task {
          text(JsDebugLessonsBundle.message("js.debugger.part.2.evaluate",
                                            code("a === b"), strong(XDebuggerBundle.message("xdebugger.button.evaluate").dropMnemonic()),
                                            code("false"), code("true (a !== b)"),
                                            code("()"), code("a === b"), code("a !== b")))
          stateCheck {
            textOnLine(1, "a !== b")
          }
        }

        task("Stop") {
          text(JsDebugLessonsBundle.message("js.debugger.part.2.stop", action(it), icon(AllIcons.Actions.Suspend), action("HideActiveWindow")))
          trigger(it)
        }

        text(JsDebugLessonsBundle.message("js.debugger.part.2.end",
                                          strong(JsDebugLessonsBundle.message("js.debugger.module.name", productName)),
                                          "https://www.jetbrains.com/help/webstorm/2019.2/debugging-code.html",
                                          "https://blog.jetbrains.com/webstorm/tag/debug/"))
      }
    }
  override val existedFile = "debugging.js"
}


