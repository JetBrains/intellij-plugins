// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorNotifications
import org.fest.swing.exception.ComponentLookupException
import org.fest.swing.exception.WaitTimedOutError
import org.intellij.lang.annotations.Language
import training.check.Check
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

internal class TaskContextImpl(private val lessonExecutor: LessonExecutor,
                               private val recorder: ActionsRecorder,
                               private val taskIndex: Int,
                               private val callbackData: LessonExecutor.TaskCallbackData) : TaskContext() {
  private val runtimeContext = TaskRuntimeContext(lessonExecutor.lesson,
                                                  lessonExecutor.editor,
                                                  lessonExecutor.project,
                                                  recorder,
                                                  { lessonExecutor.getUserVisibleInfo(taskIndex) })

  val steps: MutableList<CompletableFuture<Boolean>> = mutableListOf()

  val testActions: MutableList<Runnable> = mutableListOf()

  override fun before(preparation: TaskRuntimeContext.() -> Unit) {
    preparation(runtimeContext) // just call it here
  }

  override fun restoreState(delayMillis: Int, checkState: TaskRuntimeContext.() -> Boolean) {
    if (callbackData.restoreCondition != null) throw IllegalStateException("Only one restore context per task is allowed")
    callbackData.delayMillis = delayMillis
    callbackData.restoreCondition = { checkState(runtimeContext) }
  }

  override fun proposeRestore(restoreCheck: TaskRuntimeContext.() -> RestoreProposal) {
    var restoreResult = false
    restoreState {
      val file = lessonExecutor.virtualFile
      val type = restoreCheck(runtimeContext)
      if (type == RestoreProposal.None) {
        if (LessonManager.instance.shownRestoreType != RestoreProposal.None) {
          LessonManager.instance.clearRestoreMessage()
        }
      }
      else if (type != LessonManager.instance.shownRestoreType) {
        val proposal = RestoreNotification(type) {
          restoreResult = true
          lessonExecutor.tryToCheckRestore()
        }
        EditorNotifications.getInstance(runtimeContext.project).updateNotifications(file)
        LessonManager.instance.addRestoreNotification(proposal)
      }
      return@restoreState restoreResult
    }
  }

  override fun text(@Language("HTML") text: String) = LessonExecutorUtil.addTextToLearnPanel(text, runtimeContext.project)

  override fun trigger(actionId: String) {
    addStep(recorder.futureAction(actionId))
  }

  override fun trigger(checkId: (String) -> Boolean) {
    addStep(recorder.futureAction(checkId))
  }

  override fun triggerStart(actionId: String, checkState: TaskRuntimeContext.() -> Boolean) {
    addStep(recorder.futureActionOnStart(actionId) { checkState(runtimeContext) })
  }

  override fun triggers(vararg actionIds: String) {
    addStep(recorder.futureListActions(actionIds.toList()))
  }

  override fun <T : Any?> trigger(actionId: String, calculateState: TaskRuntimeContext.() -> T, checkState: TaskRuntimeContext.(T, T) -> Boolean) {
    val check = getCheck(calculateState, checkState)
    addStep(recorder.futureActionAndCheckAround(actionId, check))
  }

  override fun stateCheck(checkState: TaskRuntimeContext.() -> Boolean): CompletableFuture<Boolean> {
    val future = recorder.futureCheck { checkState(runtimeContext) }
    addStep(future)
    return future
  }

  override fun <T : Any> stateRequired(requiredState: TaskRuntimeContext.() -> T?): Future<T> {
    val result = CompletableFuture<T>()
    val future = recorder.futureCheck {
      val state = requiredState(runtimeContext)
      if (state != null) {
        result.complete(state)
        true
      }
      else {
        false
      }
    }
    addStep(future)
    return result
  }

  override fun addFutureStep(p: DoneStepContext.() -> Unit) {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    addStep(future)
    p.invoke(DoneStepContext(future, runtimeContext))
  }

  override fun addStep(step: CompletableFuture<Boolean>) {
    steps.add(step)
  }

  private fun <T : Any?> getCheck(calculateState: TaskRuntimeContext.() -> T, checkState: TaskRuntimeContext.(T, T) -> Boolean): Check {
    return object : Check {
      var state: T? = null

      override fun before() {
        state = calculateAction()
      }

      override fun check(): Boolean = state?.let { checkState(runtimeContext, it, calculateAction()) } ?: false

      override fun set(project: Project, editor: Editor) {
        // do nothing
      }

      // Some checks are needed to be performed in EDT thread
      // For example, selection information  could not be got (for some magic reason) from another thread
      // Also we need to commit document
      private fun calculateAction() = WriteAction.computeAndWait<T, RuntimeException> {
        PsiDocumentManager.getInstance(runtimeContext.project).commitDocument(runtimeContext.editor.document)
        calculateState(runtimeContext)
      }
    }
  }

  override fun test(action: TaskTestContext.() -> Unit) {
    testActions.add(Runnable {
      DumbService.getInstance(runtimeContext.project).waitForSmartMode()
      TaskTestContext(runtimeContext).action()
    })
  }

  @Suppress("OverridingDeprecatedMember")
  override fun triggerByUiComponentAndHighlight(findAndHighlight: TaskRuntimeContext.() -> (() -> Component)) {
    val step = CompletableFuture<Boolean>()
    ApplicationManager.getApplication().executeOnPooledThread {
      while (true) {
        if (lessonExecutor.hasBeenStopped) {
          step.complete(false)
          break
        }
        try {
          val highlightFunction = findAndHighlight(runtimeContext)
          invokeLater(ModalityState.any()) {
            lessonExecutor.foundComponent = highlightFunction()
            lessonExecutor.rehighlightComponent = highlightFunction
            step.complete(true)
          }
        }
        catch (e: WaitTimedOutError) {
          continue
        }
        catch (e: ComponentLookupException) {
          continue
        }
        break
      }
    }
    steps.add(step)
  }
}