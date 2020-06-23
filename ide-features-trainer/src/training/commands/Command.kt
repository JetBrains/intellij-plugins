// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands

import com.intellij.openapi.application.ApplicationManager
import org.jdom.Element

abstract class Command(val commandType: CommandType) {

  @Suppress("SpellCheckingInspection")
  enum class CommandType {
    TEXT, TRY, ACTION, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, MOUSEBLOCK, MOUSEUNBLOCK, CARETBLOCK, CARETUNBLOCK, SHOWLINENUMBER, WIN, SETSELECTION
  }

  abstract fun execute(executionList: ExecutionList)

  protected fun startNextCommand(executionList: ExecutionList) {
    CommandFactory.buildCommand(executionList.elements.peek(), executionList.documentationMode).execute(executionList)
  }

  protected fun executeWithPollOnEdt(executionList: ExecutionList, function: (Element) -> Unit) {
    val element = executionList.elements.poll()
    ApplicationManager.getApplication().invokeAndWait {
      function(element)
    }
    startNextCommand(executionList)
  }


}
