/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import training.learn.lesson.LessonManager

class MouseBlockCommand : Command(CommandType.MOUSEBLOCK) {

  override fun execute(executionList: ExecutionList) {
    //Block mouse and perform next
    LessonManager.instance.blockMouse(executionList.editor)
    executionList.elements.poll()
    startNextCommand(executionList)
  }
}
