package training.learn.lesson.go.release

import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkVersion
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerManagerListener
import training.commands.kotlin.TaskContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import java.util.concurrent.CompletableFuture
import javax.swing.JButton
import javax.swing.text.JTextComponent

class GoDebugFunctionCallsLesson(module: Module) : KLesson("Debug function calls", module, "go") {
  private val sample = parseLessonSample("""
package main

import "runtime"

func main() {
	<caret>_ = Factorial(10, false)
	runtime.Breakpoint()
	println("ok")
}

func Factorial(n int, withBreak bool) int {
	if withBreak {
		runtime.Breakpoint()
	}
	if n == 0 {
		return 1
	} else {
		return n * Factorial(n - 1, withBreak)
	}
}
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      task("ToggleLineBreakpoint") {
        caret(6, 5)
        text("In the debug mode, you can pass different values to a function and see what the function returns. Read more about the Evaluate expression feature in <a href=\"https://www.jetbrains.com/help/go/debugging-code.html#evaluate_expression_procedure\">GoLand documentation</a>.\n" +
                (if (debuggerSupportsFunctionCalls()) "" else "<control>Note</control>: for this feature, you need to install Go 1.11 or later.\n") + 
                "To start debugging, you need to create a breakpoint. Press ${action(it)} to toggle a breakpoint.")
        trigger("ToggleLineBreakpoint")
      }
      task("DebugClass") {
        text("Run the debugging session by pressing ${action(it)}." +
                if (SystemInfo.isMac) " Keep in mind that a system might ask for a password to start the debug server." else "")
        trigger("DebugClass")
        hitBreakpoint()
      }
      task("RunToCursor")
      {
        caret(18, 47)
        text("The caret now is at the end of the line 18. Press ${action(it)} to see how the <strong>Run to cursor</strong> action works.")
        trigger("RunToCursor")
      }
      task("StepInto") {
        text("Try to step into the function by using ${action(it)}.")
        trigger("StepInto")
      }
      task("EvaluateExpression") {
        text("Invoke the <strong>Evaluate Expression</strong> action by pressing ${action(it)}.")
        trigger("EvaluateExpression")
      }
      task("EditorChooseLookupItem") {
        text("In the <strong>Expression</strong> field, start typing <code>Factorial</code>, select <code>Factorial(n int, withBreak bool)</code> from the suggestion list.")
        trigger("EditorChooseLookupItem")
      }
      task("Factorial(6, true)") {
        text("In parentheses, type <code>6, true</code>. The field must display $it.")
        stateCheck { checkWordInTextField(it) }
      }
      task("Evaluate") {
        text("Click <strong>Evaluate</strong>.")
        stateCheck {
          checkButtonIsPressed(it)
        }
      }
      task("Close") {
        text("Close the dialog by clicking the <strong>Close</strong> button.")
        stateCheck {
          checkButtonIsPressed(it)
        }
      }
      task("Stop") {
        text("Press ${action(it)} to stop debugging and finish the lesson.")
        trigger("Stop")
      }
    }

  private fun TaskContext.debuggerSupportsFunctionCalls() =
          GoSdkService.getInstance(project).getSdk(null).majorVersion.isAtLeast(GoSdkVersion.GO_1_11)

  private fun TaskContext.checkWordInTextField(expected: String): Boolean =
          (focusOwner as? JTextComponent)?.text?.replace(" ", "")?.toLowerCase() == expected.toLowerCase().replace(" ", "")

  private fun TaskContext.checkButtonIsPressed(expected: String): Boolean =
          (focusOwner as? JButton)?.text?.toLowerCase() == expected.toLowerCase()

  private fun TaskContext.hitBreakpoint() {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    val disposable = Disposable { }
    Disposer.register(project, disposable)
    project.messageBus.connect(disposable).subscribe(XDebuggerManager.TOPIC, object : XDebuggerManagerListener {
      override fun processStarted(debugProcess: XDebugProcess) {
        debugProcess.session.addSessionListener(object : XDebugSessionListener {
          override fun stackFrameChanged() {
            ApplicationManager.getApplication().invokeLater {
              if (!future.isDone && !future.isCancelled) {
                future.complete(true)
              }
            }
          }
        })
        Disposer.dispose(disposable)
      }
    })
    steps.add(future)
  }

}
