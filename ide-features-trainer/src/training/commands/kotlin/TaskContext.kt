// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands.kotlin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
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
import training.ui.LearningUiHighlightingManager
import training.ui.LearningUiUtil
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.swing.JList

class TaskContext(private val lessonExecutor: LessonExecutor,
                  private val recorder: ActionsRecorder,
                  private val restoreContent: Ref<() -> Boolean>) {
  val lesson: KLesson = lessonExecutor.lesson
  val editor: Editor = lessonExecutor.editor
  val project: Project = lessonExecutor.project

  val steps: MutableList<CompletableFuture<Boolean>> = mutableListOf()

  val testActions: MutableList<Runnable> = mutableListOf()

  fun restoreState(checkState: () -> Boolean) {
    if (!restoreContent.isNull) throw IllegalStateException("Only one restore context per task is allowed")
    restoreContent.set(checkState)
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

  fun triggerByListItem(highlightBorder: Boolean = true, highlightInside: Boolean = false, checkList: (item: Any) -> Boolean) {
    triggerByFoundListItem(LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside)) { ui: JList<*> ->
      for (i in 0 until ui.model.size) {
        val elementAt = ui.model.getElementAt(i)
        if (checkList(elementAt)) {
          return@triggerByFoundListItem i
        }
      }
      -1
    }
  }

  // This method later can be converted to the public (But I'm not sure it will be ever needed in a such form
  private fun triggerByFoundListItem(options: LearningUiHighlightingManager.HighlightingOptions, checkList: (list: JList<*>) -> Int) {
    @Suppress("DEPRECATION")
    triggerByUiComponentAndHighlight {
      val delay = Timeout.timeout(500, TimeUnit.MILLISECONDS)
      val list = LearningUiUtil.findComponentWithTimeout(null, JList::class.java, delay) {
        checkList(it) != -1
      }
      LearningUiHighlightingManager.highlightJListItem(list, { checkList(list) }, options)
    }
  }

  inline fun <reified ComponentType : Component> triggerByUiComponentAndHighlight(
    highlightBorder: Boolean = true, highlightInside: Boolean = true, crossinline finderFunction: (ComponentType) -> Boolean
  ) {
    @Suppress("DEPRECATION")
    triggerByUiComponentAndHighlight {
      val delay = Timeout.timeout(500, TimeUnit.MILLISECONDS)
      val component = LearningUiUtil.findComponentWithTimeout(null, ComponentType::class.java, delay) {
        finderFunction(it)
      }
      LearningUiHighlightingManager.highlightComponent(component, LearningUiHighlightingManager.HighlightingOptions(highlightBorder, highlightInside))
    }
  }

  @Deprecated("It is auxiliary method with explicit class parameter. Use inlined short form instead")
  fun triggerByUiComponentAndHighlight(findAndHighlight: () -> Unit) {
    val step = CompletableFuture<Boolean>()
    ApplicationManager.getApplication().executeOnPooledThread {
      while (true) {
        if (lessonExecutor.hasBeenStopped) {
          step.complete(false)
          break
        }
        try {
          findAndHighlight()
        }
        catch (e: WaitTimedOutError) {
          continue
        }
        catch (e: ComponentLookupException) {
          continue
        }
        invokeLater { step.complete(true) }
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

  companion object {
    @Volatile
    var inTestMode: Boolean = false
  }
}
