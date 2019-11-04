/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import training.learn.lesson.LessonManager

class WinCommand : Command(CommandType.WIN) {

  @Throws(InterruptedException::class)
  override fun execute(executionList: ExecutionList) {

    executionList.elements.poll()

    val project = executionList.project
    val lesson = executionList.lesson
    lesson.pass()
    LessonManager.instance.passLesson(project, lesson)
  }
}
