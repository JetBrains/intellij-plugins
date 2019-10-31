/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

@Deprecated("")
class ExpandAllBlocksCommand : Command(Command.CommandType.EXPANDALLBLOCKS) {

  override fun execute(executionList: ExecutionList) {
    //Block caret and perform next command
    //        ActionManager.getInstance().getAction()
    executionList.elements.poll()
    executionList.editor.settings.isAutoCodeFoldingEnabled = false
    executionList.editor.settings.isAllowSingleLogicalLineFolding = false
    startNextCommand(executionList)

  }
}
