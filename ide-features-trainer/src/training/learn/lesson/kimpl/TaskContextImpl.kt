// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorNotifications
import org.fest.swing.exception.ComponentLookupException
import org.fest.swing.exception.WaitTimedOutError
import org.intellij.lang.annotations.Language
import org.jdom.input.SAXBuilder
import training.check.Check
import training.commands.kotlin.PreviousTaskInfo
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskTestContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.ui.IncorrectLearningStateNotificationProvider
import training.ui.LearningUiManager
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import javax.swing.Icon

internal class TaskContextImpl(private val lessonExecutor: LessonExecutor,
                               private val recorder: ActionsRecorder,
                               private val taskIndex: Int,
                               private val callbackData: LessonExecutor.TaskCallbackData) : TaskContext() {
  override val lesson: KLesson = lessonExecutor.lesson
  override val editor: Editor = lessonExecutor.editor
  override val project: Project = lessonExecutor.project

  override val disposable: Disposable = recorder

  val steps: MutableList<CompletableFuture<Boolean>> = mutableListOf()

  val testActions: MutableList<Runnable> = mutableListOf()


  override fun restoreState(delayMillis: Int, checkState: () -> Boolean) {
    if (callbackData.restoreCondition != null) throw IllegalStateException("Only one restore context per task is allowed")
    callbackData.delayMillis = delayMillis
    callbackData.restoreCondition = checkState
  }

  override val previous: PreviousTaskInfo
    get() = lessonExecutor.getUserVisibleInfo(taskIndex)

  override fun proposeRestore(restoreCheck: () -> RestoreProposal) {
    val key = IncorrectLearningStateNotificationProvider.INCORRECT_LEARNING_STATE_NOTIFICATION
    var restoreResult = false
    restoreState {
      val file = lessonExecutor.virtualFile
      val type = restoreCheck()
      if (type == RestoreProposal.Force) {
        return@restoreState true
      }
      if (type == RestoreProposal.None) {
        if (file.getUserData(key) != null) {
          IncorrectLearningStateNotificationProvider.clearMessage(file, project)
        }
      }
      else if (type != file.getUserData(key)?.type) {
        val proposal = IncorrectLearningStateNotificationProvider.RestoreNotification(type) {
          restoreResult = true
        }
        file.putUserData(key, proposal)
        EditorNotifications.getInstance(project).updateNotifications(file)
      }
      return@restoreState restoreResult
    }
  }

  override fun text(@Language("HTML") text: String) {
    val wrappedText = "<root><text>$text</text></root>"
    val textAsElement = SAXBuilder().build(wrappedText.byteInputStream()).rootElement.getChild("text")
                        ?: throw IllegalStateException("Can't parse as XML:\n$text")
    LessonManager.instance.addMessages(textAsElement)
  }

  override fun trigger(actionId: String) {
    addStep(recorder.futureAction(actionId))
  }

  override fun trigger(checkId: (String) -> Boolean) {
    addStep(recorder.futureAction(checkId))
  }

  override fun triggerStart(actionId: String, checkState: () -> Boolean) {
    addStep(recorder.futureActionOnStart(actionId, checkState))
  }

  override fun triggers(vararg actionIds: String) {
    addStep(recorder.futureListActions(actionIds.toList()))
  }

  override fun <T : Any?> trigger(actionId: String, calculateState: () -> T, checkState: (T, T) -> Boolean) {
    val check = getCheck(calculateState, checkState)
    addStep(recorder.futureActionAndCheckAround(actionId, check))
  }

  override fun stateCheck(checkState: () -> Boolean): CompletableFuture<Boolean> {
    val future = recorder.futureCheck { checkState() }
    addStep(future)
    return future
  }

  override fun <T : Any> stateRequired(requiredState: () -> T?): Future<T> {
    val result = CompletableFuture<T>()
    val future = recorder.futureCheck {
      val state = requiredState()
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
    p.invoke(DoneStepContext(future))
  }

  override fun addStep(step: CompletableFuture<Boolean>) {
    steps.add(step)
  }

  override val focusOwner: Component?
    get() = IdeFocusManager.getInstance(project).focusOwner

  private fun <T : Any?> getCheck(calculateState: () -> T, checkState: (T, T) -> Boolean): Check {
    return object : Check {
      var state: T? = null

      override fun before() {
        state = calculateAction()
      }

      override fun check(): Boolean = state?.let { checkState(it, calculateAction()) } ?: false

      override fun set(project: Project, editor: Editor) {
        // do nothing
      }

      // Some checks are needed to be performed in EDT thread
      // For example, selection information  could not be got (for some magic reason) from another thread
      // Also we need to commit document
      private fun calculateAction() = WriteAction.computeAndWait<T, RuntimeException> {
        PsiDocumentManager.getInstance(project).commitDocument(editor.document)
        calculateState()
      }
    }
  }

  override fun test(action: TaskTestContext.() -> Unit) {
    testActions.add(Runnable {
      DumbService.getInstance(project).waitForSmartMode()
      TaskTestContext(this).action()
    })
  }

  @Suppress("OverridingDeprecatedMember")
  override fun triggerByUiComponentAndHighlight(findAndHighlight: () -> (() -> Component)) {
    val step = CompletableFuture<Boolean>()
    ApplicationManager.getApplication().executeOnPooledThread {
      while (true) {
        if (lessonExecutor.hasBeenStopped) {
          step.complete(false)
          break
        }
        try {
          val highlightFunction = findAndHighlight()
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

  override fun action(actionId: String): String {
    return "<action>$actionId</action>"
  }

  override fun code(sourceSample: String): String {
    return "<code>${StringUtil.escapeXmlEntities(sourceSample)}</code>"
  }

  override fun icon(icon: Icon): String {
    var index = LearningUiManager.iconMap.getKeysByValue(icon)?.firstOrNull()
    if (index == null) {
      index = LearningUiManager.iconMap.size.toString()
      LearningUiManager.iconMap[index] = icon
    }
    return "<icon_idx>$index</icon_idx>"
  }
}