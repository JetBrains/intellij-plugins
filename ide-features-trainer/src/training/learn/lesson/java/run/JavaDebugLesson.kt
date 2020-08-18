// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.run

import com.intellij.icons.AllIcons
import com.intellij.testGuiFramework.impl.button
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.general.run.CommonDebugLesson
import training.learn.lesson.kimpl.LessonContext

class JavaDebugLesson(module: Module) : CommonDebugLesson(module, "java.debug.workflow", "JAVA") {
  val demoClassName = JavaRunLessonsUtils.demoClassName
  override val configurationName: String = demoClassName
  override val sample = JavaRunLessonsUtils.demoSample

  override val confNameForWatches: String = "Application"
  override val quickEvaluationArgument = "Integer.parseInt"
  override val expressionToBeEvaluated = "result/input.length"
  override val debuggingMethodName = "findAverage"
  override val methodForStepInto: String = "extractNumber"
  override val stepIntoDirection = "→"

  override fun LessonContext.applyProgramChangeTasks() {
    highlightButtonById("CompileDirty")

    task("CompileDirty") {
      text("For big programs rerun can take too much time. " +
           "When you find some mistake in pure method you can just rebuild the project and apply <strong>Hot Swap</strong> JVM feature. " +
           "Let's build project: ${action(it)} or ${icon(AllIcons.Actions.Compile)}.")
      stateCheck {
        inHotSwapDialog()
      }
      proposeModificationRestore(afterFixText)
      test { actions(it) }
    }

    task {
      text("Confirm <strong>Hot Swap</strong> replacement")
      stateCheck {
        !inHotSwapDialog()
      }
      proposeModificationRestore(afterFixText)
      test {
        with(TaskTestContext.guiTestCase) {
          dialog(null, needToKeepDialog = true) {
            button("Yes").click()
          }
        }
      }
    }

    highlightButtonById("Debugger.PopFrame")

    actionTask("Debugger.PopFrame") {
      proposeModificationRestore(afterFixText)
      "We patched our method, but right now we are still executing old obsolete ${code("extractNumber")} and it will throw " +
      "the exception again. Let's drop the frame and return to the state before ${code("extractNumber")} call. " +
      "Click ${icon(AllIcons.Actions.PopFrame)} at the debug panel or use ${action(it)}."
    }
  }

  private fun inHotSwapDialog(): Boolean {
    return Thread.currentThread().stackTrace.any { traceElement ->
      traceElement.className.contains("HotSwapUIImpl")
    }
  }

  override val testScriptProperties: TaskTestContext.TestScriptProperties
    get() = TaskTestContext.TestScriptProperties(duration = 20)

  override val fileName: String = "$demoClassName.java"
}
