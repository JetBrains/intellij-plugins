// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.debugger

import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext

class DebuggingSecondPart(module: Module) : KLesson("Debugging Code. Part II", module, JavaScriptLangSupport.lang) {

  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(BeforeDebugging.jsDebuggerSample)
        task("StepInto") {
          text("<strong>Important</strong>: Please make sure that there’s a breakpoint on line 1 and that the debugger is launched (<action>DebugClass</action>) and opened on the <strong>Debugger</strong> tab before moving forward.\nLet's continue with locating a bug in our code and learn a few more things that come in handy when debugging in WebStorm." +
            "To better understand how our code is executed, we could put a few more breakpoints in it and then move from one to another using <strong>Resume Program</strong> button (<icon>AllIcons.Actions.Resume</icon>), but there’s a faster way. Let’s step to the next executed line by pressing ${action(it)} and using <strong>Step Into</strong> (<icon>AllIcons.Actions.TraceInto</icon>).")
          trigger(it)
        }

        task("EvaluateExpression") {
          text("Depending on the situation, you may also like <strong>Step over action</strong> (<action>StepOver</action>), which moves the execution in the current file, line by line, without stepping into any function calls. <strong>Smart Step Into</strong> (<action>SmartStepInto</action>) lets you select the chained or nested call to step into. Finally, <strong>Step Out</strong> (<action>StepOut</action>) finishes the execution of the current function and stops at the next statement after the call. \n" +
            "Now, what if we want to check the value of an expression? WebStorm lets you do it quickly with the <strong>Evaluate Expression</strong> popup. Press ${action(it)} to call it.")
          trigger(it)
        }

        task {
          text("Let's add <strong>a === b</strong> as an expression and hit <strong>Evaluate</strong>. Look at the result: it equals <strong>false</strong>. This is where the problem lies. In order for the function to catch different numbers, we need to slightly change the expression so that its result would equal <strong>true (a !== b)</strong>.\n" +
            "Now let's close the popup and fix the problem we've found in the code. Inside <strong>()</strong> on line 2, replace <strong>a === b</strong> with <strong>a !== b</strong>.")
          stateCheck {
            textOnLine(1, "a !== b")
          }
        }

        task("Stop") {
          text("Finally, let’s learn how to stop the debugger when you no longer need it. First, click the breakpoint we added to remove it. Then, stop the debugger with ${action(it)} (<icon>AllIcons.Actions.Suspend</icon>) and close its tool window by pressing <action>HideActiveWindow</action>. ")
          trigger(it)
        }

        task {
          text("Congratulations! You’ve made it to the end of <strong>WebStorm Debugger 101</strong> and learned some basic ways to debug all kinds of apps. If you’d like, you can run the code one more time to confirm that everything works fine now. To dive deeper into debugging specific types of apps, take a look at our <a href='https://www.jetbrains.com/help/webstorm/2019.2/debugging-code.html'>web help</a> and <a href='https://blog.jetbrains.com/webstorm/tag/debug/'>blog posts</a>. Click the button below to move to the next module.")
        }
      }
    }
  override val existedFile = "debugging.js"
}


