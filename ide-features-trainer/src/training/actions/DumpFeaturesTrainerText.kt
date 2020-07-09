// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.actions

import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.learn.CourseManager
import training.learn.lesson.kimpl.ApplyTaskLessonContext
import training.learn.lesson.kimpl.KLesson
import java.awt.Component
import java.awt.datatransfer.StringSelection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

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
  override fun before(preparation: TaskRuntimeContext.() -> Unit) = Unit // do nothing

  override fun restoreState(delayMillis: Int, checkState: TaskRuntimeContext.() -> Boolean) = Unit // do nothing

  override fun proposeRestore(restoreCheck: TaskRuntimeContext.() -> RestoreProposal) = Unit // do nothing

  override fun text(text: String) {
    buffer.append(text)
    buffer.append('\n')
  }

  override fun trigger(actionId: String) = Unit // do nothing

  override fun trigger(checkId: (String) -> Boolean) = Unit // do nothing

  override fun <T> trigger(actionId: String, calculateState: TaskRuntimeContext.() -> T, checkState: TaskRuntimeContext.(T, T) -> Boolean) = Unit // do nothing

  override fun triggerStart(actionId: String, checkState: TaskRuntimeContext.() -> Boolean) = Unit // do nothing

  override fun triggers(vararg actionIds: String) = Unit // do nothing

  override fun stateCheck(checkState: TaskRuntimeContext.() -> Boolean): CompletableFuture<Boolean> = CompletableFuture<Boolean>()

  override fun <T : Any> stateRequired(requiredState: TaskRuntimeContext.() -> T?): Future<T> {
    return CompletableFuture()
  }

  override fun addFutureStep(p: DoneStepContext.() -> Unit)= Unit // do nothing

  override fun addStep(step: CompletableFuture<Boolean>)= Unit // do nothing

  @Suppress("OverridingDeprecatedMember")
  override fun triggerByUiComponentAndHighlight(findAndHighlight: TaskRuntimeContext.() -> () -> Component) = Unit // do nothing

  override fun test(action: TaskTestContext.() -> Unit) = Unit // do nothing
}