package training.commands.kotlin

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import org.jdom.input.SAXBuilder
import training.check.Check
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.learn.lesson.kimpl.KLesson
import training.ui.Message
import java.awt.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class TaskContext(val lesson: KLesson, val editor: Editor, val project: Project,
                  private val recorder: ActionsRecorder) {
  val steps : MutableList<CompletableFuture<Boolean>> = mutableListOf()

  val testActions: MutableList<Runnable> = mutableListOf()

  /**
   * Write a text to the learn panel (panel with a learning tasks).
   */
  fun text(text: String) {
    val wrappedText = "<root><text>$text</text></root>"
    val textAsElement = SAXBuilder().build(wrappedText.byteInputStream()).rootElement.getChild("text")
    LessonManager.instance.addMessages(Message.convert(textAsElement)) //support old format
  }

  /** Simply wait until an user perform particular action */
  fun trigger(actionId: String) {
    steps.add(recorder.futureAction(actionId))
  }

  /** Trigger on actions start. Needs if you want to split long actions into several tasks. */
  fun triggerStart(actionId: String, checkState: () -> Boolean = {true}) {
    steps.add(recorder.futureActionOnStart(actionId, checkState))
  }

  fun triggers(vararg actionIds: String) {
    steps.add(recorder.futureListActions(actionIds.toList()))
  }

  /** An user need to rice an action which leads to necessary state change */
  fun <T : Any?> trigger(actionId: String, calculateState: () -> T, checkState: (T, T) -> Boolean) {
    val check = getCheck(calculateState, checkState)
    steps.add(recorder.futureActionAndCheckAround(actionId, check))
  }

  /** An user need to rice an action which leads to appropriate end state */
  fun trigger(actionId: String, checkState: () -> Boolean) {
    trigger(actionId, { Unit }, { _, _ -> checkState() })
  }

  /** Check that IDE state have been changed by single action as expected */
  fun <T : Any> changeCheck(calculateState: () -> T, checkState: (T, T) -> Boolean) {
    val check = getCheck(calculateState, checkState)
    steps.add(recorder.futureCheck { check.check() })
  }

  /**
   * Check that IDE state is as expected
   * In some rare cases DSL could wish to complete a future by itself
   */
  fun stateCheck(checkState: () -> Boolean): CompletableFuture<Boolean> {
    val future = recorder.futureCheck { checkState() }
    steps.add(future)
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
      } else {
        false
      }
    }
    steps.add(future)
    return result
  }

  val focusOwner: Component?
    get() = IdeFocusManager.getInstance(project).focusOwner

  private fun <T : Any?> getCheck(calculateState: () -> T, checkState: (T, T) -> Boolean) : Check {
    return object : Check {
      var state: T? = null

      override fun before() {
        state = calculateAction()
      }

      override fun check(): Boolean = checkState(state!!, calculateAction())

      override fun set(project: Project?, editor: Editor?) {
        // do nothing
      }

      override fun listenAllKeys(): Boolean = false

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

  fun action(id: String): String {
    return "<action>$id</action>"
  }

  fun code(sourceSample: String): String {
    return "<code>${StringUtil.escapeXmlEntities(sourceSample)}</code>"
  }

  companion object {
    @Volatile var inTestMode : Boolean = false
  }
}
