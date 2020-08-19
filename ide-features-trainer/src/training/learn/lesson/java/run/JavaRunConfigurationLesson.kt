// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.run

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import training.learn.interfaces.Module
import training.learn.lesson.general.run.CommonRunConfigurationLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.toolWindowShowed
import java.awt.Rectangle

class JavaRunConfigurationLesson(module: Module) : CommonRunConfigurationLesson(module, "java.run.configuration", "JAVA") {
  override val sample: LessonSample = JavaRunLessonsUtils.demoSample
  override val demoConfigurationName: String = JavaRunLessonsUtils.demoClassName

  override fun LessonContext.runTask() {
    task {
      triggerByPartOfComponent<EditorGutterComponentEx> l@{ ui ->
        if (CommonDataKeys.EDITOR.getData(ui as DataProvider) != editor) return@l null
        val y = editor.visualLineToY(0)
        return@l Rectangle(25, y, ui.width - 40, editor.lineHeight * 2)
      }
    }

    task("RunClass") {
      text("Any code marked with ${icon(AllIcons.Actions.Execute)} can be run. Let's run our simple example with ${action(it)}. " +
           "Alternatively you can click at ${icon(AllIcons.Actions.Execute)} and select <strong>Run</strong> item.")
      //Wait toolwindow
      toolWindowShowed("Run")
      stateCheck {
        configurations().isNotEmpty()
      }
      test {
        actions(it)
      }
    }
  }

  override val fileName: String = "${JavaRunLessonsUtils.demoClassName}.java"
}
