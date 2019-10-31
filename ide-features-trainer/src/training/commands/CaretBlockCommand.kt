package training.commands

import training.learn.lesson.LessonManager

class CaretBlockCommand : Command(Command.CommandType.CARETBLOCK) {

  override fun execute(executionList: ExecutionList) {
    executionList.elements.poll()
    //Block caret and perform next command
    LessonManager.instance.blockCaret(executionList.editor)
    startNextCommand(executionList)
  }
}
