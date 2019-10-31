/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import com.intellij.openapi.application.ApplicationManager
import org.jdom.Element

abstract class Command(val commandType: Command.CommandType) {

  enum class CommandType {
    START, TEXT, TRY, TRYBLOCK, ACTION, REPLAY, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, TRAVERSECARET, MOUSEBLOCK, MOUSEUNBLOCK, WAIT, CARETBLOCK, CARETUNBLOCK, SHOWLINENUMBER, EXPANDALLBLOCKS, WIN, TEST, SETSELECTION
  }

  /**

   * @return true if button is updated
   */
  @Throws(InterruptedException::class)
  protected fun updateButton(executionList: ExecutionList): Boolean {
    return true
  }

  protected fun initAgainButton() {}

  abstract fun execute(executionList: ExecutionList)

  protected fun startNextCommand(executionList: ExecutionList) {
    CommandFactory.buildCommand(executionList.elements.peek()).execute(executionList)
  }

  protected fun executeWithPoll(executionList: ExecutionList, function: (Element) -> Unit) {
    val element = executionList.elements.poll()
    function(element)
    startNextCommand(executionList)
  }

  protected fun executeWithPollOnEdt(executionList: ExecutionList, function: (Element) -> Unit) {
    val element = executionList.elements.poll()
    ApplicationManager.getApplication().invokeAndWait {
      function(element)
    }
    startNextCommand(executionList)
  }


}
