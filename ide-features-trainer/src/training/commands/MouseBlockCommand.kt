package training.commands

import training.learn.lesson.LessonManager

/**
 * Created by karashevich on 30/01/15.
 */
class MouseBlockCommand : Command(Command.CommandType.MOUSEBLOCK) {

  override fun execute(executionList: ExecutionList) {
    //Block mouse and perform next
    LessonManager.instance.blockMouse(executionList.editor)
    executionList.elements.poll()
    startNextCommand(executionList)

  }
}
