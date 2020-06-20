// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.commands

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import training.check.Check
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.learn.lesson.LessonProcessor
import training.ui.LearnToolWindowFactory
import training.ui.LearningUiManager
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

class TryCommand : Command(CommandType.TRY) {

  @Throws(Exception::class)
  override fun execute(executionList: ExecutionList) {
    LearningUiManager.activeToolWindow?.learnPanel?.updateLessonProgress(LessonProcessor.tasksNumber, LessonProcessor.currentTaskIndex)
    LessonProcessor.currentTaskIndex++

    val element = executionList.elements.poll()
    val check: Check?

    val editor = executionList.editor

    var checkFuture: CompletableFuture<Boolean>? = null
    var triggerFuture: CompletableFuture<Boolean>? = null

    LessonManager.instance.addMessages(element)

    val project = editor.project!!
    val parentDisposable = LearnToolWindowFactory.learnWindowPerProject[project]?.parentDisposable ?: project
    val recorder = ActionsRecorder(project, editor.document, parentDisposable)
    LessonManager.instance.registerActionsRecorder(recorder)

    if (element.getAttribute("check") != null) {
      val checkClassString = element.getAttribute("check")!!.value
      try {
        val myCheck = Class.forName(checkClassString, true, executionList.lesson.classLoader)
        check = myCheck.newInstance() as Check
        check.set(executionList.project, editor)
        check.before()
        checkFuture = recorder.futureCheck { check.check() }
      }
      catch (e: Exception) {
        LOG.error(e)
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

  companion object {
    private val LOG = Logger.getInstance(TryCommand::class.java)
  }
}
