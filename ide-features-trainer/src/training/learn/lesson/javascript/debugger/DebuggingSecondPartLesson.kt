// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.debugger

import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.xdebugger.XDebuggerBundle
import training.lang.JavaScriptLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil.productName
import training.learn.lesson.kimpl.dropMnemonic

class DebuggingSecondPartLesson(module: Module)
  : KLesson("Debugging Code. Part II", LessonsBundle.message("js.debugger.part.2.title"), module, JavaScriptLangSupport.lang) {

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(BeforeDebuggingLesson.jsDebuggerSample)
        task("StepInto") {
          text(LessonsBundle.message("js.debugger.part.2.step.into", 
                                     action("DebugClass"), 
                                     strong(XDebuggerBundle.message("xdebugger.debugger.tab.title")), 
                                     productName, 
                                     ActionsBundle.message("action.Resume.text").dropMnemonic(), 
                                     icon(AllIcons.Actions.Resume), 
                                     action(it), ActionsBundle.message("action.StepInto.text").dropMnemonic(), 
                                     icon(AllIcons.Actions.TraceInto)))
          trigger(it)
        }

        task("EvaluateExpression") {
          text(LessonsBundle.message("js.debugger.part.2.buttons", 
                                     ActionsBundle.message("action.StepOver.text").dropMnemonic(), 
                                     action("StepOver"), 
                                     strong(ActionsBundle.message("action.SmartStepInto.text").dropMnemonic()), 
                                     action("SmartStepInto"), 
                                     strong(ActionsBundle.message("action.StepOut.text").dropMnemonic()), 
                                     action("StepOut"), productName, 
                                     strong(XDebuggerBundle.message("xdebugger.dialog.title.evaluate.expression")), 
                                     action(it)))
          trigger(it)
        }

        task {
          text(LessonsBundle.message("js.debugger.part.2.evaluate", strong("a === b"), strong(XDebuggerBundle.message("xdebugger.button.evaluate").dropMnemonic()), strong("false"), strong("true (a !== b)"), strong("()"), strong("a === b"), strong("a !== b")))
          stateCheck {
            textOnLine(1, "a !== b")
          }
        }

        task("Stop") {
          text(LessonsBundle.message("js.debugger.part.2.stop", action(it), icon(AllIcons.Actions.Suspend), action("HideActiveWindow")))
          trigger(it)
        }

        task {
          text(LessonsBundle.message("js.debugger.part.2.end", strong(LessonsBundle.message("js.debugger.module.title", productName))))
        }
      }
    }
  override val existedFile = "debugging.js"
}


