// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.BasicUndoableAction
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.DocumentUtil

class CopyTextCommand internal constructor() : Command(CommandType.COPYTEXT) {

  @Throws(InterruptedException::class)
  override fun execute(executionList: ExecutionList) {

    val element = executionList.elements.poll()

    val finalText = if (element.content.isEmpty()) "" else element.content[0].value
    ApplicationManager.getApplication().invokeAndWait {
      DocumentUtil.writeInRunUndoTransparentAction {
        val documentReference = DocumentReferenceManager.getInstance().create(executionList.editor.document)
        UndoManager.getInstance(executionList.project).nonundoableActionPerformed(documentReference, false)
        executionList.editor.document.insertString(0, finalText)
      }

      PsiDocumentManager.getInstance(executionList.project).commitDocument(executionList.editor.document)
      doUndoableAction(executionList)
      updateGutter(executionList)
    }

    startNextCommand(executionList)
  }

  private fun doUndoableAction(executionList: ExecutionList) {
    CommandProcessor.getInstance().executeCommand(executionList.project, {
      UndoManager.getInstance(executionList.project).undoableActionPerformed(object : BasicUndoableAction() {
        override fun undo() {}
        override fun redo() {}
      })
    }, null, null)
  }

  private fun updateGutter(executionList: ExecutionList) {
    val editorGutter = executionList.editor.gutter
    val editorGutterComponentEx = editorGutter as EditorGutterComponentEx
    editorGutterComponentEx.revalidateMarkup()
  }
}
