// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import javax.swing.Icon

internal object LessonExecutorUtil {
  fun isRealTask(taskContent: TaskContext.() -> Unit): Boolean {
    val fakeTaskContext = FakeTaskContext()
    taskContent(fakeTaskContext)
    return fakeTaskContext.hasDetection && fakeTaskContext.hasText
  }
}

private class FakeTaskContext : TaskContext() {
  var hasText = false
  var hasDetection = false

  override fun before(preparation: TaskRuntimeContext.() -> Unit) = Unit // do nothing

  override fun restoreState(delayMillis: Int, checkState: TaskRuntimeContext.() -> Boolean) = Unit // do nothing

  override fun proposeRestore(restoreCheck: TaskRuntimeContext.() -> RestoreProposal) = Unit // do nothing

  override fun text(text: String) {
    hasText = true
  }

  override fun trigger(actionId: String) {
    hasDetection = true
  }

  override fun trigger(checkId: (String) -> Boolean) {
    hasDetection = true
  }
  override fun <T> trigger(actionId: String, calculateState: TaskRuntimeContext.() -> T, checkState: TaskRuntimeContext.(T, T) -> Boolean) {
    hasDetection = true
  }

  override fun triggerStart(actionId: String, checkState: TaskRuntimeContext.() -> Boolean) {
    hasDetection = true
  }

  override fun triggers(vararg actionIds: String) {
    hasDetection = true
  }

  override fun stateCheck(checkState: TaskRuntimeContext.() -> Boolean): CompletableFuture<Boolean> {
    hasDetection = true
    return CompletableFuture<Boolean>()
  }

  override fun <T : Any> stateRequired(requiredState: TaskRuntimeContext.() -> T?): Future<T> {
    hasDetection = true
    return CompletableFuture()
  }

  override fun addFutureStep(p: DoneStepContext.() -> Unit) {
    hasDetection = true
  }

  override fun addStep(step: CompletableFuture<Boolean>) {
    hasDetection = true
  }

  @Suppress("OverridingDeprecatedMember")
  override fun triggerByUiComponentAndHighlight(findAndHighlight: TaskRuntimeContext.() -> () -> Component)  {
    hasDetection = true
  }

  override fun test(action: TaskTestContext.() -> Unit) = Unit // do nothing

  override fun action(actionId: String): String = "" //Doesn't matter what to return

  override fun code(sourceSample: String): String = "" //Doesn't matter what to return

  override fun icon(icon: Icon): String = "" //Doesn't matter what to return
}