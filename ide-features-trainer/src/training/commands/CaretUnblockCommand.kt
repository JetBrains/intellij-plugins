package training.commands

import training.learn.lesson.LessonManager

class CaretUnblockCommand : Command(Command.CommandType.CARETUNBLOCK) {

  override fun execute(executionList: ExecutionList) {
    executionList.elements.poll()
    //Unblock caret and perform next command
    LessonManager.instance.unblockCaret()
    startNextCommand(executionList)

  }
}
