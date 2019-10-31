package training.commands

import training.learn.lesson.LessonManager

class MouseBlockCommand : Command(Command.CommandType.MOUSEBLOCK) {

  override fun execute(executionList: ExecutionList) {
    //Block mouse and perform next
    LessonManager.instance.blockMouse(executionList.editor)
    executionList.elements.poll()
    startNextCommand(executionList)

  }
}
