// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.run

import com.intellij.icons.AllIcons
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.general.run.CommonDebugLesson
import training.learn.lesson.kimpl.LessonContext

class PythonDebugLesson(module: Module) : CommonDebugLesson(module, "python.debug.workflow", "Python") {
  override val configurationName = PythonRunLessonsUtils.demoConfigurationName
  override val sample = PythonRunLessonsUtils.demoSample
  override val confNameForWatches = "PythonConfigurationType"
  override val quickEvaluationArgument = "int"
  override val expressionToBeEvaluated = "result/len(value)"
  override val debuggingMethodName = "find_average"
  override val methodForStepInto = "extract_number"
  override val stepIntoDirection = "←"

  override fun LessonContext.applyProgramChangeTasks() {
    highlightButtonById("Rerun")

    actionTask("Rerun") {
      before {
        mayBeStopped = true
      }
      "Let's rerun our program. Just click again at ${icon(AllIcons.Actions.Restart)} or use ${action(it)}."
    }
  }

  override val testScriptProperties: TaskTestContext.TestScriptProperties
    get() = TaskTestContext.TestScriptProperties(duration = 20)
}
