/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.commands

import com.intellij.openapi.application.ApplicationManager
import training.check.Check
import training.keymap.KeymapUtil
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.ui.Message
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

class TryCommand : Command(Command.CommandType.TRY) {

  @Throws(Exception::class)
  override fun execute(executionList: ExecutionList) {

    val element = executionList.elements.poll()
    var check: Check? = null
    //        updateDescription(element, infoPanel, editor);

    val lesson = executionList.lesson
    val editor = executionList.editor

    var checkFuture: CompletableFuture<Boolean>? = null
    var triggerFuture: CompletableFuture<Boolean>? = null

    LessonManager.instance.addMessages(Message.convert(element))

    val recorder = ActionsRecorder(editor.project!!, editor.document)
    LessonManager.instance.registerActionsRecorder(recorder)

    if (element.getAttribute("check") != null) {
      val checkClassString = element.getAttribute("check")!!.value
      try {
        val myCheck = Class.forName(checkClassString)
        check = myCheck.newInstance() as Check
        check.set(executionList.project, editor)
        check.before()
        checkFuture = recorder.futureCheck { check.check() }
      } catch (e: Exception) {
        e.printStackTrace()
      }

    }
    when {
      element.getAttribute("trigger") != null -> {
        val actionId = element.getAttribute("trigger")!!.value
        triggerFuture = recorder.futureAction(actionId)
      }
      element.getAttribute("triggers") != null -> {
        val actionIds = element.getAttribute("triggers")!!.value
        val listOfActionIds = actionIds.split(";".toRegex()).dropLastWhile { it.isEmpty() }
        triggerFuture = recorder.futureListActions(listOfActionIds)
      }
    }
    thread(name = "IdeFeaturesTrainer Result") {
      checkFuture?.get()
      triggerFuture?.get()
      LessonManager.instance.passExercise()
      ApplicationManager.getApplication().invokeLater { startNextCommand(executionList) }
    }
  }

  private fun resolveShortcut(text: String, actionId: String): String {
    val shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId)
    val shortcutText = KeymapUtil.getKeyStrokeText(shortcutByActionId)
    return substitution(text, shortcutText)
  }

  companion object {

    fun substitution(text: String, shortcutString: String): String {
      return if (text.contains(ActionCommand.SHORTCUT)) {
        text.replace(ActionCommand.SHORTCUT, shortcutString)
      } else {
        text
      }
    }
  }

}
