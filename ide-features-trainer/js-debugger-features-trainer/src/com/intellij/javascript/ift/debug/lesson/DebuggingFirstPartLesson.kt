// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.debug.lesson

import com.intellij.icons.AllIcons
import com.intellij.javascript.debugger.JSDebuggerBundle
import com.intellij.javascript.ift.debug.JsDebugLessonsBundle
import com.intellij.javascript.ift.debug.lesson.BeforeDebuggingLesson.Companion.jsDebuggerSample
import com.intellij.javascript.ift.debug.lineContainsBreakpoint
import com.intellij.javascript.ift.debug.setLanguageLevel
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.ui.UIBundle
import com.intellij.ui.tabs.impl.SingleHeightTabs
import com.intellij.xdebugger.XDebuggerBundle
import training.dsl.LessonContext
import training.dsl.LessonUtil
import training.dsl.LessonUtil.highlightBreakpointGutter
import training.dsl.highlightButtonById
import training.learn.course.KLesson
import training.util.isToStringContains

class DebuggingFirstPartLesson
  : KLesson("Debugging Code. Part I", JsDebugLessonsBundle.message("js.debugger.part.1.title")) {

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(jsDebuggerSample)

        highlightButtonById("Run")
        task("Run") {
          text(JsDebugLessonsBundle.message("js.debugger.part.1.start.1"))
          text(JsDebugLessonsBundle.message("js.debugger.part.1.start.2", icon(AllIcons.RunConfigurations.TestState.Run)))
          trigger(it)
        }

        highlightBreakpointGutter(xRange = { IntRange(13, it - 17) }) { LogicalPosition(0, 0) }
        task {
          text(JsDebugLessonsBundle.message("js.debugger.part.1.gutter", code("10"), code("-20"), code("Different!"), code("Equal!")))
          stateCheck {
            lineContainsBreakpoint(1)
          }
        }

        highlightButtonById("Debug")
        task("Debug") {
          text(JsDebugLessonsBundle.message("js.debugger.part.1.set.breakpoint", icon(AllIcons.Actions.StartDebugger), action(it)))
          trigger(it)
        }

        task {
          triggerAndFullHighlight().component { ui: SingleHeightTabs.SingleHeightLabel ->
            ui.info.text == JSDebuggerBundle.message("js.console.node.process.name")
          }
        }
        task {
          text(JsDebugLessonsBundle.message("js.debugger.part.1.tool.window.1", UIBundle.message("tool.window.name.debug")))
          text(JsDebugLessonsBundle.message("js.debugger.part.1.tool.window.2", strong(XDebuggerBundle.message("xdebugger.default.content.title")), 
                                            strong(XDebuggerBundle.message("debugger.session.tab.variables.title")), 
                                            strong(XDebuggerBundle.message("debugger.session.tab.frames.title")), 
                                            strong(JSDebuggerBundle.message("js.console.node.process.name"))))
          stateCheck {
            focusOwner.isToStringContains("Terminal")
          }
        }

        task {
          triggerAndFullHighlight().component { ui: SingleHeightTabs.SingleHeightLabel ->
            ui.info.text == JSDebuggerBundle.message("js.scripts.tab.title")
          }
        }
        task {
          text(JsDebugLessonsBundle.message("js.debugger.part.1.scripts.tab",
                                            strong(JSDebuggerBundle.message("js.console.node.process.name")),
                                            strong(JSDebuggerBundle.message("js.console.debug.name")),
                                            strong(JSDebuggerBundle.message("js.scripts.tab.title"))))
          stateCheck {
            focusOwner.isToStringContains("treeStructure.SimpleTree")
          }
        }
        text(JsDebugLessonsBundle.message("js.debugger.part.1.next", strong(JSDebuggerBundle.message("js.scripts.tab.title")),
                                          LessonUtil.rawEnter()))
      }
    }
  override val sampleFilePath = "debugging.js"
}


