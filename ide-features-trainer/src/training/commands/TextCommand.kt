/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import training.learn.lesson.LessonManager
import training.ui.Message

class TextCommand : Command(Command.CommandType.TEXT) {

  @Throws(InterruptedException::class)
  override fun execute(executionList: ExecutionList) {


    val element = executionList.elements.poll()
    val lesson = executionList.lesson

    LessonManager.instance.addMessages(Message.convert(element))

    startNextCommand(executionList)

  }

}
