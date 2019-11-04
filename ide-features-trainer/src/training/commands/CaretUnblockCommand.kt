/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import training.learn.lesson.LessonManager

class CaretUnblockCommand : Command(CommandType.CARETUNBLOCK) {

  override fun execute(executionList: ExecutionList) {
    executionList.elements.poll()
    //Unblock caret and perform next command
    LessonManager.instance.unblockCaret()
    startNextCommand(executionList)

  }
}
