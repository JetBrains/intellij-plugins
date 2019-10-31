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
