// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.text.TextWithMnemonic
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.messages.Topic
import com.intellij.xdebugger.XDebuggerManager
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.keymap.KeymapUtil
import training.learn.LearnBundle
import java.awt.event.KeyEvent
import javax.swing.JList
import javax.swing.KeyStroke

object LessonUtil {
  val productName: String
    get() = ApplicationNamesInfo.getInstance().fullProductName

  fun insertIntoSample(sample: LessonSample, inserted: String): String {
    return sample.text.substring(0, sample.startOffset) + inserted + sample.text.substring(sample.startOffset)
  }

  fun TaskRuntimeContext.checkPositionOfEditor(sample: LessonSample): TaskContext.RestoreNotification? {
    fun invalidCaret(): Boolean {
      val selection = sample.selection
      val currentCaret = editor.caretModel.currentCaret
      return if (selection != null && selection.first != selection.second) {
        currentCaret.selectionStart != selection.first || currentCaret.selectionEnd != selection.second
      }
      else currentCaret.offset != sample.startOffset
    }

    return checkExpectedStateOfEditor(sample, false)
           ?: if (invalidCaret()) sampleRestoreNotification(TaskContext.CaretRestoreProposal, sample) else null
  }

  fun TaskRuntimeContext.checkExpectedStateOfEditor(sample: LessonSample,
                                                    checkPosition: Boolean = true,
                                                    checkModification: (String) -> Boolean = { it.isEmpty() }): TaskContext.RestoreNotification? {
    val prefix = sample.text.substring(0, sample.startOffset)
    val postfix = sample.text.substring(sample.startOffset)

    val docText = editor.document.charsSequence
    val message = if (docText.startsWith(prefix) && docText.endsWith(postfix)) {
      val middle = docText.subSequence(prefix.length, docText.length - postfix.length).toString()
      if (checkModification(middle)) {
        val offset = editor.caretModel.offset
        if (!checkPosition || (prefix.length <= offset && offset <= prefix.length + middle.length)) {
          null
        }
        else {
          TaskContext.CaretRestoreProposal
        }
      }
      else {
        TaskContext.ModificationRestoreProposal
      }
    }
    else {
      TaskContext.ModificationRestoreProposal
    }

    return if (message != null) sampleRestoreNotification(message, sample) else null
  }

  fun TaskRuntimeContext.sampleRestoreNotification(message: String, sample: LessonSample) =
    TaskContext.RestoreNotification(message) { setSample(sample) }

  fun findItem(ui: JList<*>, checkList: (item: Any) -> Boolean): Int? {
    for (i in 0 until ui.model.size) {
      val elementAt = ui.model.getElementAt(i)
      if (checkList(elementAt)) {
        return i
      }
    }
    return null
  }

  fun setEditorReadOnly(editor: Editor) {
    if (editor !is EditorEx) return
    editor.isViewer = true
    EditorModificationUtil.setReadOnlyHint(editor, LearnBundle.message("learn.task.read.only.hint"))
  }

  fun actionName(actionId: String): @NlsActions.ActionText String {
    val name = ActionManager.getInstance().getAction(actionId).templatePresentation.text ?: error("No action with ID $actionId")
    return "<strong>${name}</strong>"
  }

  fun rawEnter(): String {
    val keyStrokeEnter = KeymapUtil.getKeyStrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))
    return "<raw_action>$keyStrokeEnter</raw_action>"
  }

  fun rawCtrlEnter(): String {
    return "<raw_action>${if (SystemInfo.isMacOSMojave) "\u2318\u23CE" else "Ctrl + Enter"}</raw_action>"
  }
}

fun TaskContext.toolWindowShowed(toolWindowId: String) {
  addFutureStep {
    subscribeForMessageBus(ToolWindowManagerListener.TOPIC, object: ToolWindowManagerListener {
      override fun toolWindowShown(toolWindow: ToolWindow) {
        if (toolWindow.id == toolWindowId)
          completeStep()
      }
    })
  }
}

fun <L> TaskRuntimeContext.subscribeForMessageBus(topic: Topic<L>, handler: L) {
  project.messageBus.connect(taskDisposable).subscribe(topic, handler)
}

fun TaskRuntimeContext.lineWithBreakpoints(): Set<Int> {
  val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
  return breakpointManager.allBreakpoints.filter {
    val file = FileDocumentManager.getInstance().getFile(editor.document)
    it.sourcePosition?.file == file
  }.mapNotNull {
    it.sourcePosition?.line
  }.toSet()
}

fun String.dropMnemonic(): String {
  return TextWithMnemonic.parse(this).dropMnemonic(true).text
}