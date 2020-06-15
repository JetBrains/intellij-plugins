// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands.kotlin

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
import com.intellij.ui.tree.TreeVisitor
import com.intellij.util.ui.tree.TreeUtil
import org.fest.swing.exception.ComponentLookupException
import org.fest.swing.exception.WaitTimedOutError
import org.fest.swing.timing.Timeout
import org.intellij.lang.annotations.Language
import org.jdom.input.SAXBuilder
import training.check.Check
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonExecutor
import training.learn.lesson.kimpl.LessonUtil
import training.ui.IncorrectLearningStateNotificationProvider
import training.ui.LearningUiHighlightingManager
import training.ui.LearningUiManager
import training.ui.LearningUiUtil
import java.awt.Component
import java.awt.Rectangle
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.JList
import javax.swing.JTree
import javax.swing.tree.TreePath

class TaskContext(private val lessonExecutor: LessonExecutor,
                  private val recorder: ActionsRecorder,
                  private val taskIndex: Int,
                  private val callbackData: LessonExecutor.TaskCallbackData) {
  val lesson: KLesson = lessonExecutor.lesson
  val editor: Editor = lessonExecutor.editor
  val project: Project = lessonExecutor.project

  val disposable: Disposable = recorder

  val steps: MutableList<CompletableFuture<Boolean>> = mutableListOf()

  val testActions: MutableList<Runnable> = mutableListOf()

  fun restoreState(delayMillis: Int = 0, checkState: () -> Boolean) {
    if (callbackData.restoreCondition != null) throw IllegalStateException("Only one restore context per task is allowed")
    callbackData.delayMillis = delayMillis
    callbackData.restoreCondition = checkState
  }

  /** Shortcut */
  fun restoreByUi(delayMillis: Int = 0) {
    restoreState(delayMillis = delayMillis) {
      previous.ui?.isShowing?.not() ?: true
    }
  }

  val previous: PreviousTaskInfo
    get() = lessonExecutor.getUserVisibleInfo(taskIndex)

  enum class RestoreProposal {
    None,
    Caret,
    Modification,
    Force
  }

  fun proposeRestore(restoreCheck: () -> RestoreProposal) {
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

  /**
   * Write a text to the learn panel (panel with a learning tasks).
   */
  fun text(@Language("HTML") text: String) {
    val wrappedText = "<root><text>$text</text></root>"
    val textAsElement = SAXBuilder().build(wrappedText.byteInputStream()).rootElement.getChild("text")
                        ?: throw IllegalStateException("Can't parse as XML:\n$text")
    LessonManager.instance.addMessages(textAsElement)
  }

  /** Simply wait until an user perform particular action */
  fun trigger(actionId: String) {
    addStep(recorder.futureAction(actionId))
  }

  /** Simply wait until an user perform actions */
  fun trigger(checkId: (String) -> Boolean) {
    addStep(recorder.futureAction(checkId))
  }

  /** Trigger on actions start. Needs if you want to split long actions into several tasks. */
  fun triggerStart(actionId: String, checkState: () -> Boolean = { true }) {
    addStep(recorder.futureActionOnStart(actionId, checkState))
  }

  fun triggers(vararg actionIds: String) {
    addStep(recorder.futureListActions(actionIds.toList()))
  }

  /** An user need to rice an action which leads to necessary state change */
  fun <T : Any?> trigger(actionId: String, calculateState: () -> T, checkState: (T, T) -> Boolean) {
    val check = getCheck(calculateState, checkState)
    addStep(recorder.futureActionAndCheckAround(actionId, check))
  }

  /** An user need to rice an action which leads to appropriate end state */
  fun trigger(actionId: String, checkState: () -> Boolean) {
    trigger(actionId, { Unit }, { _, _ -> checkState() })
  }

  /**
   * Check that IDE state is as expected
   * In some rare cases DSL could wish to complete a future by itself
   */
  fun stateCheck(checkState: () -> Boolean): CompletableFuture<Boolean> {
    val future = recorder.futureCheck { checkState() }
    addStep(future)
    return future
  }

  /**
   * Check that IDE state is fit
   * @return A feature with value associated with fit state
   */
  fun <T : Any> stateRequired(requiredState: () -> T?): Future<T> {
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

  fun addFutureStep(p: DoneStepContext.() -> Unit) {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    addStep(future)
    p.invoke(DoneStepContext(future))
  }

  fun addStep(step: CompletableFuture<Boolean>) {
    steps.add(step)
  }

  val focusOwner: Component?
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

  fun test(action: TaskTestContext.() -> Unit) {
    testActions.add(Runnable {
      DumbService.getInstance(project).waitForSmartMode()
      TaskTestContext(this).action()
    })
  }


  fun triggerByFoundPathAndHighlight(highlightBorder: Boolean = true, highlightInside: Boolean = false, checkPath: (tree: JTree, path: TreePath) -> Boolean) {
    triggerByFoundPathAndHighlight(LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)) { tree ->
      TreeUtil.visitVisibleRows(tree, TreeVisitor { path ->
        if (checkPath(tree, path)) TreeVisitor.Action.INTERRUPT else TreeVisitor.Action.CONTINUE
      })
    }
  }

  // This method later can be converted to the public (But I'm not sure it will be ever needed in a such form)
  private fun triggerByFoundPathAndHighlight(options: LearningUiHighlightingManager.HighlightingOptions, checkTree: (tree: JTree) -> TreePath?) {
    @Suppress("DEPRECATION")
    triggerByUiComponentAndHighlight {
      val delay = Timeout.timeout(500, TimeUnit.MILLISECONDS)
      val tree = LearningUiUtil.findShowingComponentWithTimeout(null, JTree::class.java, delay) {
        checkTree(it) != null
      }
      return@triggerByUiComponentAndHighlight {
        LearningUiHighlightingManager.highlightJTreeItem(tree, options) {
          checkTree(tree)
        }
        tree
      }
    }
  }

  inline fun <reified T: Component> triggerByPartOfComponent(highlightBorder: Boolean = true, highlightInside: Boolean = false, crossinline rectangle: (T) -> Rectangle?) {
    val options = LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)
    @Suppress("DEPRECATION")
    triggerByUiComponentAndHighlight {
      val delay = Timeout.timeout(500, TimeUnit.MILLISECONDS)
      val whole = LearningUiUtil.findShowingComponentWithTimeout(null, T::class.java, delay) {
        rectangle(it) != null
      }
      return@triggerByUiComponentAndHighlight {
        LearningUiHighlightingManager.highlightPartOfComponent(whole, options) { rectangle(it) }
        whole
      }
    }
  }


  fun triggerByListItemAndHighlight(highlightBorder: Boolean = true, highlightInside: Boolean = false, checkList: (item: Any) -> Boolean) {
    triggerByFoundListItemAndHighlight(LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)) { ui: JList<*> ->
      LessonUtil.findItem(ui, checkList)
    }
  }

  // This method later can be converted to the public (But I'm not sure it will be ever needed in a such form
  private fun triggerByFoundListItemAndHighlight(options: LearningUiHighlightingManager.HighlightingOptions, checkList: (list: JList<*>) -> Int?) {
    @Suppress("DEPRECATION")
    triggerByUiComponentAndHighlight {
      val delay = Timeout.timeout(500, TimeUnit.MILLISECONDS)
      val list = LearningUiUtil.findShowingComponentWithTimeout(null, JList::class.java, delay) l@{
        val index = checkList(it)
        index != null && it.visibleRowCount > index
      }
      return@triggerByUiComponentAndHighlight {
        LearningUiHighlightingManager.highlightJListItem(list, options) {
          checkList(list)
        }
        list
      }
    }
  }

  inline fun <reified ComponentType : Component> triggerByUiComponentAndHighlight(
    highlightBorder: Boolean = true, highlightInside: Boolean = true, crossinline finderFunction: (ComponentType) -> Boolean
  ) {
    @Suppress("DEPRECATION")
    triggerByUiComponentAndHighlight l@{
      val delay = Timeout.timeout(500, TimeUnit.MILLISECONDS)
      val component = LearningUiUtil.findShowingComponentWithTimeout(null, ComponentType::class.java, delay) {
        finderFunction(it)
      }
      return@l {
        val options = LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)
        LearningUiHighlightingManager.highlightComponent(component, options)
        component
      }
    }
  }

  @Deprecated("It is auxiliary method with explicit class parameter. Use inlined short form instead")
  fun triggerByUiComponentAndHighlight(findAndHighlight: () -> (() -> Component)) {
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

  fun action(id: String): String {
    return "<action>$id</action>"
  }

  fun code(sourceSample: String): String {
    return "<code>${StringUtil.escapeXmlEntities(sourceSample)}</code>"
  }

  fun icon(icon: Icon): String {
    var index = LearningUiManager.iconMap.getKeysByValue(icon)?.firstOrNull()
    if (index == null) {
      index = LearningUiManager.iconMap.size.toString()
      LearningUiManager.iconMap[index] = icon
    }
    return "<icon_idx>$index</icon_idx>"
  }

  class DoneStepContext(val future: CompletableFuture<Boolean>) {
    fun completeStep() {
      assert(ApplicationManager.getApplication().isDispatchThread)
      if (!future.isDone && !future.isCancelled) {
        future.complete(true)
      }
    }
  }
}
