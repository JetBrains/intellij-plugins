package training.commands

import training.check.Check
import training.keymap.KeymapUtil
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.ui.Message

/**
 * Created by karashevich on 30/01/15.
 */
class TryCommand : Command(Command.CommandType.TRY) {

  @Throws(Exception::class)
  override fun execute(executionList: ExecutionList) {

    val element = executionList.elements.poll()
    var check: Check? = null
    //        updateDescription(element, infoPanel, editor);

    val lesson = executionList.lesson
    val editor = executionList.editor

    LessonManager.getInstance(lesson)?.addMessages(Message.convert(element))

    val recorder = ActionsRecorder(editor.project!!, editor.document)
    LessonManager.getInstance(lesson).registerActionsRecorder(recorder)

    if (element.getAttribute("check") != null) {
      val checkClassString = element.getAttribute("check")!!.value
      try {
        val myCheck = Class.forName(checkClassString)
        check = myCheck.newInstance() as Check
        check.set(executionList.project, editor)
        check.before()
      }
      catch (e: Exception) {
        e.printStackTrace()
      }

    }
    if (element.getAttribute("trigger") != null) {
      val actionId = element.getAttribute("trigger")!!.value
      startRecord(executionList, recorder, actionId, check)
    }
    else if (element.getAttribute("triggers") != null) {
      val actionIds = element.getAttribute("triggers")!!.value
      val actionIdArray = actionIds.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      startRecord(executionList, recorder, actionIdArray, check)
    }
    else {
      startRecord(executionList, recorder, check)
    }

  }

  @Throws(Exception::class)
  private fun startRecord(executionList: ExecutionList, recorder: ActionsRecorder, check: Check?) {
    recorder.startRecording(getDoWhenDone(executionList), null as Array<String>?, check)
  }

  @Throws(Exception::class)
  private fun startRecord(executionList: ExecutionList, recorder: ActionsRecorder, actionId: String, check: Check?) {
    recorder.startRecording(getDoWhenDone(executionList), actionId, check)
  }

  @Throws(Exception::class)
  private fun startRecord(executionList: ExecutionList, recorder: ActionsRecorder, actionIdArray: Array<String>, check: Check?) {
    recorder.startRecording(getDoWhenDone(executionList), actionIdArray, check)

  }

  private fun getDoWhenDone(executionList: ExecutionList): Runnable
    = Runnable { pass(executionList) }

  private fun pass(executionList: ExecutionList) {
    val lesson = executionList.lesson
    LessonManager.getInstance(lesson).passExercise()
    val lessonLog = lesson.lessonLog
    lesson.passItem()
    startNextCommand(executionList)
  }


  private fun resolveShortcut(text: String, actionId: String): String {
    val shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId)
    val shortcutText = KeymapUtil.getKeyStrokeText(shortcutByActionId)
    return substitution(text, shortcutText)
  }

  companion object {

    fun substitution(text: String, shortcutString: String): String {
      if (text.contains(ActionCommand.SHORTCUT)) {
        return text.replace(ActionCommand.SHORTCUT, shortcutString)
      }
      else {
        return text
      }
    }
  }

}
