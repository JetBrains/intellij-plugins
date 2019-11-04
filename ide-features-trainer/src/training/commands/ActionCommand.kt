/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import training.util.PerformActionUtil

class ActionCommand : Command(CommandType.ACTION) {

  override fun execute(executionList: ExecutionList) {
    val element = executionList.elements.poll()
    val editor = executionList.editor
    val project = executionList.project
    val actionType = element.getAttribute("action")!!.value
    PerformActionUtil.performAction(actionType, editor, project, Runnable { startNextCommand(executionList) })
  }

}
