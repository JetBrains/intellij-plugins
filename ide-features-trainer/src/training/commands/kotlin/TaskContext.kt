// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands.kotlin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.tree.TreeVisitor
import com.intellij.util.ui.tree.TreeUtil
import org.fest.swing.timing.Timeout
import org.intellij.lang.annotations.Language
import training.learn.lesson.kimpl.LessonUtil
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

abstract class TaskContext {
  /** Put here some initialization for the task */
  abstract fun before(preparation: TaskRuntimeContext.() -> Unit)

  abstract fun restoreState(delayMillis: Int = 0, checkState: TaskRuntimeContext.() -> Boolean)

  /** Shortcut */
  fun restoreByUi(delayMillis: Int = 0) {
    restoreState(delayMillis = delayMillis) {
      previous.ui?.isShowing?.not() ?: true
    }
  }

  enum class RestoreProposal {
    None,
    Caret,
    Modification
  }

  data class RestoreNotification(val type: RestoreProposal, val callback: () -> Unit)

  abstract fun proposeRestore(restoreCheck: TaskRuntimeContext.() -> RestoreProposal)

  /**
   * Write a text to the learn panel (panel with a learning tasks).
   */
  abstract fun text(@Language("HTML") text: String)

  /** Simply wait until an user perform particular action */
  abstract fun trigger(actionId: String)

  /** Simply wait until an user perform actions */
  abstract fun trigger(checkId: (String) -> Boolean)

  /** Trigger on actions start. Needs if you want to split long actions into several tasks. */
  abstract fun triggerStart(actionId: String, checkState: TaskRuntimeContext.() -> Boolean = { true })

  abstract fun triggers(vararg actionIds: String)

  /** An user need to rice an action which leads to necessary state change */
  abstract fun <T : Any?> trigger(actionId: String, calculateState: TaskRuntimeContext.() -> T, checkState: TaskRuntimeContext.(T, T) -> Boolean)

  /** An user need to rice an action which leads to appropriate end state */
  fun trigger(actionId: String, checkState: TaskRuntimeContext.() -> Boolean) {
    trigger(actionId, { Unit }, { _, _ -> checkState() })
  }

  /**
   * Check that IDE state is as expected
   * In some rare cases DSL could wish to complete a future by itself
   */
  abstract fun stateCheck(checkState: TaskRuntimeContext.() -> Boolean): CompletableFuture<Boolean>

  /**
   * Check that IDE state is fit
   * @return A feature with value associated with fit state
   */
  abstract fun <T : Any> stateRequired(requiredState: TaskRuntimeContext.() -> T?): Future<T>

  abstract fun addFutureStep(p: DoneStepContext.() -> Unit)

  abstract fun addStep(step: CompletableFuture<Boolean>)

  abstract fun test(action: TaskTestContext.() -> Unit)

  fun triggerByFoundPathAndHighlight(highlightBorder: Boolean = true, highlightInside: Boolean = false, checkPath: TaskRuntimeContext.(tree: JTree, path: TreePath) -> Boolean) {
    triggerByFoundPathAndHighlight(LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)) { tree ->
      TreeUtil.visitVisibleRows(tree, TreeVisitor { path ->
        if (checkPath(tree, path)) TreeVisitor.Action.INTERRUPT else TreeVisitor.Action.CONTINUE
      })
    }
  }

  // This method later can be converted to the public (But I'm not sure it will be ever needed in a such form)
  private fun triggerByFoundPathAndHighlight(options: LearningUiHighlightingManager.HighlightingOptions, checkTree: TaskRuntimeContext.(tree: JTree) -> TreePath?) {
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

  inline fun <reified T: Component> triggerByPartOfComponent(highlightBorder: Boolean = true, highlightInside: Boolean = false, crossinline rectangle: TaskRuntimeContext.(T) -> Rectangle?) {
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

  fun triggerByListItemAndHighlight(highlightBorder: Boolean = true, highlightInside: Boolean = false, checkList: TaskRuntimeContext.(item: Any) -> Boolean) {
    triggerByFoundListItemAndHighlight(LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)) { ui: JList<*> ->
      LessonUtil.findItem(ui) { checkList(it) }
    }
  }

  // This method later can be converted to the public (But I'm not sure it will be ever needed in a such form
  private fun triggerByFoundListItemAndHighlight(options: LearningUiHighlightingManager.HighlightingOptions, checkList: TaskRuntimeContext.(list: JList<*>) -> Int?) {
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
    highlightBorder: Boolean = true, highlightInside: Boolean = true, crossinline finderFunction: TaskRuntimeContext.(ComponentType) -> Boolean
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
  abstract fun triggerByUiComponentAndHighlight(findAndHighlight: TaskRuntimeContext.() -> (() -> Component))

  /** Show shortcut for [actionId] inside lesson step message */
  open fun action(actionId: String): String  {
    return "<action>$actionId</action>"
  }

  /** Highlight as code inside lesson step message */
  open fun code(sourceSample: String): String  {
    return "<code>${StringUtil.escapeXmlEntities(sourceSample)}</code>"
  }

  /** Show an [icon] inside lesson step message */
  open fun icon(icon: Icon): String  {
    val index = LearningUiManager.getIconIndex(icon)
    return "<icon_idx>$index</icon_idx>"
  }

  class DoneStepContext(val future: CompletableFuture<Boolean>, rt: TaskRuntimeContext): TaskRuntimeContext(rt) {
    fun completeStep() {
      assert(ApplicationManager.getApplication().isDispatchThread)
      if (!future.isDone && !future.isCancelled) {
        future.complete(true)
      }
    }
  }
}
