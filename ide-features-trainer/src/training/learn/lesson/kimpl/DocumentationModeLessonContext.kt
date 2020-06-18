// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

internal class DocumentationModeLessonContext : LessonContext() {
  private val documentationModeTaskContext = DocumentationModeTaskContext()

  override fun task(taskContent: TaskContext.() -> Unit) {
    taskContent(documentationModeTaskContext)
  }

  override fun caret(offset: Int) = Unit // do nothing

  override fun caret(line: Int, column: Int) = Unit // do nothing

  override fun caret(text: String) = Unit // do nothing

  override fun caret(position: LessonSamplePosition) = Unit // do nothing

  override fun waitBeforeContinue(delayMillis: Int) = Unit // do nothing

  override fun prepareSample(sample: LessonSample) = Unit // do nothing
}

private class DocumentationModeTaskContext : TaskContext() {
  override fun before(preparation: TaskRuntimeContext.() -> Unit) = Unit // do nothing

  override fun restoreState(delayMillis: Int, checkState: TaskRuntimeContext.() -> Boolean) = Unit // do nothing

  override fun proposeRestore(restoreCheck: TaskRuntimeContext.() -> RestoreProposal) = Unit // do nothing

  override fun text(text: String) = LessonExecutorUtil.addTextToLearnPanel(text)

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
