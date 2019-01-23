package training.commands

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor

/**
 * Created by karashevich on 30/01/15.
 */
class ShowLineNumberCommand : Command(Command.CommandType.SHOWLINENUMBER) {

  override fun execute(executionList: ExecutionList) {
    //Block caret and perform next command
    //        ActionManager.getInstance().getAction()
    val editor = executionList.editor
    showLineNumber(editor)
    executionList.elements.poll()
    startNextCommand(executionList)
  }

  companion object {
    fun showLineNumber(editor: Editor) {
      ApplicationManager.getApplication().invokeAndWait {
        editor.settings.isLineNumbersShown = true
      }
    }
  }
}
