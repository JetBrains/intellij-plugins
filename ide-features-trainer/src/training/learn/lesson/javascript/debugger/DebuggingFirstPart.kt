// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.debugger

import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.debugger.BeforeDebugging.Companion.jsDebuggerSample
import training.learn.lesson.javascript.lineContainsBreakpoint
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class DebuggingFirstPart(module: Module) : KLesson("Debugging Code. Part I", module, JavaScriptLangSupport.lang) {


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(jsDebuggerSample)
        task("Run") {
          text("Now that we have a run/debug configuration in place, let’s see how to work with the built-in debugger. \n" +
               "First, let’s run our code one more time to examine what it returns as we didn't focus on this in the previous lesson. Click the <icon>AllIcons.RunConfigurations.TestState.Run</icon> button located next to the drop-down with configurations to run the currently selected one.")
          trigger(it)
        }

        task {
          text("The numbers we’re comparing, <strong>10</strong> and <strong>-20</strong>, are not equal, so we should’ve got <strong>Different!</strong> when running the code. Let’s find out why we got <strong>Equal!</strong> instead. On line 1, click the left editor gutter (empty space) between #1 and the code to put a breakpoint.")
          stateCheck {
            lineContainsBreakpoint(1)
          }
        }

        task("Debug") {
          text("So, we can use breakpoints to pause the execution of the app. The red circle you see on the left-hand editor gutter is what a breakpoint looks like in WebStorm. If you click on it again, it will be removed. You can also right-click on it to customize its behavior, e.g. set a condition for it. Let’s hit the <icon>AllIcons.Actions.StartDebugger</icon> button located at the top right-hand corner (or press ${action(it)}) to move next.")
          trigger(it)
        }

        task {
          text("Meet the <strong>Debug</strong> tool window. On its left side, you can find icons for stopping/rerunning configurations, and managing breakpoints. At its top, you can see a few tabs and a bunch of icons for stepping through the code. \n" +
            "The <strong>Debugger</strong> tab we're on is where most of the work is done. On the right, you can see all the <strong>variables</strong> grouped by scopes, along with their values. The <strong>Frames</strong> view shows the call stack. If you go through it, you’ll see the app state at every point of the execution path. Now switch to the <strong>Console</strong> tab.")
          stateCheck {
            focusOwner.toString() == "EditorComponent file=null"
          }
        }

        task {
          text("The <strong>Console</strong> tab shows the messages logged by an app, including errors. When debugging Node.js apps, WebStorm also shows the <strong>Debugger Console</strong> tab, where you can run JavaScript code snippets and view the console messages. Switch to the <strong>Scripts</strong> tab to continue.")
          stateCheck {
            focusOwner.toString().contains("treeStructure.SimpleTree")
          }
        }
        task {
          text("The <strong>Scripts</strong> tab lists all the files loaded into the currently running process. You can see the content of any file by double-clicking on it. To move to the second part of this lesson, click the button below or use ${action("learn.next.lesson")}.")
        }
      }
    }
  override val existedFile = "debugging.js"
}


