// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.learn.CourseManager
import training.learn.lesson.kimpl.ApplyTaskLessonContext
import training.learn.lesson.kimpl.KLesson
import java.awt.datatransfer.StringSelection

@Suppress("HardCodedStringLiteral")
class DumpFeaturesTrainerText : AnAction("Copy IFT Course Text to Clipboard") {
  override fun actionPerformed(e: AnActionEvent) {
    val lessonsForModules = CourseManager.instance.lessonsForModules
    val buffer = StringBuffer()
    for (x in lessonsForModules) {
      if (x is KLesson) {
        buffer.append(x.name)
        buffer.append(":\n")
        x.lessonContent(ApplyTaskLessonContext(TextCollector(buffer)))
        buffer.append('\n')
      }
    }
    CopyPasteManagerEx.getInstance().setContents(StringSelection(buffer.toString()))
  }
}


private class TextCollector(private val buffer: StringBuffer) : TaskContext() {
  override fun text(text: String) {
    buffer.append(text)
    buffer.append('\n')
  }

  override fun runtimeText(callback: TaskRuntimeContext.() -> String?) {
    // TODO: think how to dump it
  }
}