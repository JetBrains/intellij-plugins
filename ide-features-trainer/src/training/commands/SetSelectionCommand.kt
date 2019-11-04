/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import com.intellij.find.FindManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.LogicalPosition
import java.util.concurrent.ExecutionException

class SetSelectionCommand : Command(CommandType.SETSELECTION) {

  //always put the caret at the end of the selection
  @Throws(InterruptedException::class, ExecutionException::class, BadCommandException::class)
  override fun execute(executionList: ExecutionList) {

    var startLine: Int
    var startColumn: Int
    var endLine: Int
    var endColumn: Int
    var parkCaret = 0

    val element = executionList.elements.poll()
    val editor = executionList.editor

    if (element.getAttribute(START_SELECT_POSITION) != null) {
      var positionString = element.getAttribute(START_SELECT_POSITION)!!.value
      var splitStrings = positionString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      assert(splitStrings.size == 2)

      startLine = Integer.parseInt(splitStrings[0])
      startColumn = Integer.parseInt(splitStrings[1])

      if (element.getAttribute(END_SELECT_POSITION) != null) {
        positionString = element.getAttribute(END_SELECT_POSITION)!!.value
        splitStrings = positionString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        assert(splitStrings.size == 2)

        endLine = Integer.parseInt(splitStrings[0])
        endColumn = Integer.parseInt(splitStrings[1])

        startLine--
        startColumn--
        endLine--
        endColumn--

        ApplicationManager.getApplication().invokeAndWait {
          val blockStart = LogicalPosition(startLine, startColumn)
          val blockEnd = LogicalPosition(endLine, endColumn)

          val startPosition = editor.logicalPositionToOffset(blockStart)
          val endPosition = editor.logicalPositionToOffset(blockEnd)

          editor.selectionModel.setSelection(startPosition, endPosition)
          parkCaret = endPosition
        }
      }
      else {
        throw BadCommandException(this)
      }
    }
    else if (element.getAttribute(START_SELECT_STRING) != null && element.getAttribute(END_SELECT_STRING) != null) {
      val document = editor.document
      val project = executionList.project

      ApplicationManager.getApplication().invokeAndWait {
        val findManager = FindManager.getInstance(project)
        val model = findManager.findInFileModel.clone()
        model.isGlobal = false
        model.isReplaceState = false

        val valueStart = element.getAttribute(START_SELECT_STRING)!!.value
        model.stringToFind = valueStart
        val start = FindManager.getInstance(project).findString(document.charsSequence, 0, model)

        val valueEnd = element.getAttribute(END_SELECT_STRING)!!.value
        model.stringToFind = valueEnd
        val end = FindManager.getInstance(project).findString(document.charsSequence, 0, model)

        selectInDocument(executionList, start.startOffset, end.endOffset)
        parkCaret = end.endOffset
      }
    }
    else {
      throw BadCommandException(this)
    }

    //move caret to the end of the selection
    ApplicationManager.getApplication().invokeAndWait {
      executionList.editor.caretModel.moveToOffset(parkCaret)
    }
    startNextCommand(executionList)
  }

  private fun selectInDocument(executionList: ExecutionList, startOffset: Int, endOffset: Int) {
    val editor = executionList.editor
    editor.selectionModel.setSelection(startOffset, endOffset)
  }

  companion object {

    const val START_SELECT_POSITION = "start-position"
    const val END_SELECT_POSITION = "end-position"

    const val START_SELECT_STRING = "start-string"
    const val END_SELECT_STRING = "end-string"
  }

}
