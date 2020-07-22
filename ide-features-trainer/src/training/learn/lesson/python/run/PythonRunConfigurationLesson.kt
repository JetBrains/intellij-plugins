// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.run

import com.intellij.execution.RunManager
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jList
import com.intellij.ui.components.JBCheckBox
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.toolWindowShowed
import training.ui.LearningUiHighlightingManager
import javax.swing.JButton

class PythonRunConfigurationLesson(module: Module) : KLesson("Run Configuration", module, "Python") {
  private val demoConfigurationName = PythonRunLessonsUtils.demoConfigurationName

  private fun TaskRuntimeContext.runManager() = RunManager.getInstance(project)
  private fun TaskRuntimeContext.configurations() =
    runManager().allSettings.filter { it.name.contains(demoConfigurationName) }

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareRuntimeTask {
        configurations().forEach { runManager().removeConfiguration(it) }
      }

      prepareSample(PythonRunLessonsUtils.demoSample)

      task("RunClass") {
        text("Let's run our simple example with ${action(it)}.")
        //Wait toolwindow
        toolWindowShowed("Run")
        stateCheck {
          configurations().isNotEmpty()
        }
        test {
          actions(it)
        }
      }

      actionTask("HideActiveWindow") {
        LearningUiHighlightingManager.clearHighlights()
        "IDE automatically opened <strong>Run</strong> tool window. Just a tip, at the top of <strong>Run</strong> tool window you can see the full runned command." +
             " Now let’s hide the tool window with ${action(it)}."
      }

      task {
        triggerByUiComponentAndHighlight<JButton> { ui ->
          ui.text == demoConfigurationName
        }
      }

      task {
        text("For each new run IDE create temporary run configuration. Temporary configurations are automatically deleted if the default limit of 5 is reached. " +
             "Lets convert temporary configuration into permanent one. Open the drop-down menu with run configuration.")
        triggerByListItemAndHighlight { item ->
          item.toString() == "Save '$demoConfigurationName' Configuration"
        }
        test {
          ideFrame {
            button(demoConfigurationName).click()
          }
        }
      }

      task {
        text("Select <strong>Save '$demoConfigurationName' Configuration</strong>.")
        restoreByUi()
        stateCheck {
          val selectedConfiguration = RunManager.getInstance(project).selectedConfiguration ?: return@stateCheck false
          !selectedConfiguration.isTemporary
        }
        test {
          ideFrame {
            jList("Save '$demoConfigurationName' Configuration").click()
          }
        }
      }

      task("editRunConfigurations") {
        LearningUiHighlightingManager.clearHighlights()
        text("Suppose you want to change configuration or create another one manually. Then you need to open the the drop-down menu again and click <strong>Edit Configurations</strong>. " +
             "Alternatively you can use ${action(it)} action.")
        triggerByUiComponentAndHighlight<JBCheckBox>(highlightInside = false) { ui ->
          ui.text == "Store as project file"
        }
        test {
          actions(it)
        }
      }

      task {
        text("This is a place for managing run/debug configurations. You can set here program parameters, JVM arguments, environment variables and so on.")
        text("Just a tip. Sometimes you may want to save configuration to its own file. " +
             "Such configurations will be easy to share between colleagues (usually by version control system)." +
             "Now close the settings dialog to finish this lesson.")
        stateCheck {
          focusOwner is EditorComponentImpl
        }
        test {
          ideFrame {
            button("Cancel").click()
          }
        }
      }
    }

  override val testScriptProperties: TaskTestContext.TestScriptProperties
    get() = TaskTestContext.TestScriptProperties(duration = 20)
}
