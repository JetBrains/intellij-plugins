// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.messages.Topic
import com.intellij.xdebugger.XDebuggerManager
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import javax.swing.JList

object LessonUtil {
  fun insertIntoSample(sample: LessonSample, inserted: String): String {
    return sample.text.substring(0, sample.startOffset) + inserted + sample.text.substring(sample.startOffset)
  }

  fun checkExpectedStateOfEditor(editor: Editor,
                                 sample: LessonSample,
                                 checkModification: (String) -> Boolean): TaskContext.RestoreProposal {
    val prefix = sample.text.substring(0, sample.startOffset)
    val postfix = sample.text.substring(sample.startOffset)

    val docText = editor.document.charsSequence
    return if (docText.startsWith(prefix) && docText.endsWith(postfix)) {
      val middle = docText.subSequence(prefix.length, docText.length - postfix.length).toString()
      if (checkModification(middle)) {
        val offset = editor.caretModel.offset
        if (prefix.length <= offset && offset <= prefix.length + middle.length) {
          TaskContext.RestoreProposal.None
        }
        else {
          TaskContext.RestoreProposal.Caret
        }
      }
      else {
        TaskContext.RestoreProposal.Modification
      }
    }
    else {
      TaskContext.RestoreProposal.Modification
    }
  }

  fun findItem(ui: JList<*>, checkList: (item: Any) -> Boolean): Int? {
    for (i in 0 until ui.model.size) {
      val elementAt = ui.model.getElementAt(i)
      if (checkList(elementAt)) {
        return i
      }
    }
    return null
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
  project.messageBus.connect(disposable).subscribe(topic, handler)
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
