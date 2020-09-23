// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.debugger

import com.intellij.icons.AllIcons
import com.intellij.javascript.debugger.JSDebuggerBundle
import com.intellij.ui.UIBundle
import com.intellij.xdebugger.XDebuggerBundle
import training.lang.JavaScriptLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.debugger.BeforeDebuggingLesson.Companion.jsDebuggerSample
import training.learn.lesson.javascript.lineContainsBreakpoint
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class DebuggingFirstPartLesson(module: Module)
  : KLesson("Debugging Code. Part I", LessonsBundle.message("js.debugger.part.1.title"), module, JavaScriptLangSupport.lang) {

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(jsDebuggerSample)
        task("Run") {
          text(LessonsBundle.message("js.debugger.part.1.start", icon(AllIcons.RunConfigurations.TestState.Run)))
          trigger(it)
        }

        task {
          text(LessonsBundle.message("js.debugger.part.1.gutter", code("10"), code("-20"), code("Different!"), code("Equal!")))
          stateCheck {
            lineContainsBreakpoint(1)
          }
        }

        task("Debug") {
          text(LessonsBundle.message("js.debugger.part.1.set.breakpoint", icon(AllIcons.Actions.StartDebugger), action(it)))
          trigger(it)
        }

        task {
          text(LessonsBundle.message("js.debugger.part.1.tool.window", UIBundle.message("tool.window.name.debug"), strong(XDebuggerBundle.message("xdebugger.default.content.title")), strong(XDebuggerBundle.message("debugger.session.tab.variables.title")), strong(XDebuggerBundle.message("debugger.session.tab.frames.title")), strong(XDebuggerBundle.message("debugger.session.tab.console.content.name"))))
          stateCheck {
            val text = focusOwner.toString()
            text.contains("Terminal") 
          }
        }

        task {
          text(LessonsBundle.message("js.debugger.part.1.scripts.tab", strong(XDebuggerBundle.message("debugger.session.tab.console.content.name")), strong(JSDebuggerBundle.message("js.console.debug.name")), strong(JSDebuggerBundle.message("js.scripts.tab.title"))))
          stateCheck {
            focusOwner.toString().contains("treeStructure.SimpleTree")
          }
        }
        task {
          text(LessonsBundle.message("js.debugger.part.1.next", strong(JSDebuggerBundle.message("js.scripts.tab.title")), action("learn.next.lesson")))
        }
      }
    }
  override val existedFile = "debugging.js"
}


