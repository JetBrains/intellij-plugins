/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor

class ShowLineNumberCommand : Command(Command.CommandType.SHOWLINENUMBER) {

  override fun execute(executionList: ExecutionList) {
    //Block caret and perform next command
    //        ActionManager.getInstance().getAction()
    val editor = executionList.editor
    showLineNumber(editor)
    executionList.elements.poll()
    startNextCommand(executionList)
  }

  companion object {
    fun showLineNumber(editor: Editor) {
      ApplicationManager.getApplication().invokeAndWait {
        editor.settings.isLineNumbersShown = true
      }
    }
  }
}
