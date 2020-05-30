// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands

import com.intellij.find.FindManager
import com.intellij.find.FindResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.LogicalPosition
import org.jdom.Element

class MoveCaretCommand : Command(CommandType.MOVECARET) {

  override fun execute(executionList: ExecutionList) {
    val element = executionList.elements.poll()

    ApplicationManager.getApplication().invokeAndWait {
      when {
        element.getAttribute(MOVECARET_OFFSET) != null -> {
          val offsetString = element.getAttribute(MOVECARET_OFFSET)!!.value
          val offset = Integer.parseInt(offsetString)
          executionList.editor.caretModel.moveToOffset(offset)
        }
        element.getAttribute(MOVECARET_POSITION) != null -> {
          val (line, column) = getLineAndColumn(element)
          executionList.editor.caretModel.moveToLogicalPosition(LogicalPosition(line - 1, column - 1))
        }
        element.getAttribute(MOVECARET_STRING) != null -> {
          val start = getStartOffsetForText(executionList, element)
          executionList.editor.caretModel.moveToOffset(start.startOffset)
        }
      }
    }

    startNextCommand(executionList)
  }

  private fun getStartOffsetForText(executionList: ExecutionList, element: Element): FindResult {
    val editor = executionList.editor
    val document = editor.document
    val project = executionList.project

    val findManager = FindManager.getInstance(project)
    val model = findManager.findInFileModel.clone()
    model.isGlobal = false
    model.isReplaceState = false

    val valueStart = element.getAttribute(MOVECARET_STRING)!!.value
    model.stringToFind = valueStart
    return FindManager.getInstance(project).findString(document.charsSequence, 0, model)
  }

  private fun getLineAndColumn(element: Element): Pair<Int, Int> {
    val positionString = element.getAttribute(MOVECARET_POSITION)!!.value
    val splitStrings = positionString.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    assert(splitStrings.size == 2)
    return Pair(Integer.parseInt(splitStrings[0]), Integer.parseInt(splitStrings[1]))
  }

  companion object {

    const val MOVECARET_OFFSET = "offset"
    const val MOVECARET_POSITION = "position"
    const val MOVECARET_STRING = "string"
  }
}
