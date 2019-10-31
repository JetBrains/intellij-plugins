package training.commands


import training.learn.lesson.LessonManager

class MouseUnblockCommand : Command(Command.CommandType.MOUSEUNBLOCK) {

  override fun execute(executionList: ExecutionList) {
    executionList.elements.poll()
    LessonManager.instance.unblockMouse(executionList.editor)
    startNextCommand(executionList)
  }
}
