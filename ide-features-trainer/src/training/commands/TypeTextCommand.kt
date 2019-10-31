/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import com.intellij.openapi.command.WriteCommandAction

class TypeTextCommand : Command(CommandType.TYPETEXT) {

  override fun execute(executionList: ExecutionList) {
    executeWithPollOnEdt(executionList) {
      element ->

      val textToType = if (element.content.isEmpty()) "" else element.content[0].value
      val startOffset = executionList.editor.caretModel.offset

      var isTyping = true
      val i = intArrayOf(0)

      while (isTyping) {
        val finalI = i[0]
        WriteCommandAction.runWriteCommandAction(executionList.project, Runnable {
          executionList.editor.document.insertString(finalI + startOffset, textToType.subSequence(i[0], i[0] + 1))
          executionList.editor.caretModel.moveToOffset(finalI + 1 + startOffset)
        })
        isTyping = ++i[0] < textToType.length
      }
    }
  }

}
