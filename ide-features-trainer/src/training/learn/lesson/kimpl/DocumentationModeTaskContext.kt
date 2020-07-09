// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.project.Project
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

internal class DocumentationModeTaskContext(private val project: Project) : TaskContext() {
  override fun before(preparation: TaskRuntimeContext.() -> Unit) = Unit // do nothing

  override fun restoreState(delayMillis: Int, checkState: TaskRuntimeContext.() -> Boolean) = Unit // do nothing

  override fun proposeRestore(restoreCheck: TaskRuntimeContext.() -> RestoreProposal) = Unit // do nothing

  override fun text(text: String) = LessonExecutorUtil.addTextToLearnPanel(text, project)

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