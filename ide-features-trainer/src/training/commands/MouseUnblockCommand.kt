/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands


import training.learn.lesson.LessonManager

class MouseUnblockCommand : Command(Command.CommandType.MOUSEUNBLOCK) {

  override fun execute(executionList: ExecutionList) {
    executionList.elements.poll()
    LessonManager.instance.unblockMouse(executionList.editor)
    startNextCommand(executionList)
  }
}
