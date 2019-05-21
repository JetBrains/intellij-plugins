package training.commands


import training.learn.lesson.LessonManager

/**
 * Created by karashevich on 30/01/15.
 */
class MouseUnblockCommand : Command(Command.CommandType.MOUSEUNBLOCK) {

  override fun execute(executionList: ExecutionList) {
    executionList.elements.poll()
    LessonManager.instance.unblockMouse(executionList.editor)
    startNextCommand(executionList)
  }
}
