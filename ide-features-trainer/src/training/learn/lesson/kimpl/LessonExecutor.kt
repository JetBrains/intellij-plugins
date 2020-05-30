// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.find.FindManager
import com.intellij.find.FindResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.BasicUndoableAction
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.Alarm
import com.intellij.util.DocumentUtil
import training.commands.kotlin.PreviousTaskInfo
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.ui.IncorrectLearningStateNotificationProvider
import java.awt.Component
import java.util.concurrent.CompletableFuture

class LessonExecutor(val lesson: KLesson, val editor: Editor, val project: Project) {
  private data class TaskInfo(val content: (Int) -> Unit,
                              var restoreIndex: Int = 0,
                              var messagesNumberBeforeStart: Int = 0,
                              var rehighlightComponent: (() -> Component)? = null,
                              var userVisibleInfo: PreviousTaskInfo? = null)

  data class TaskCallbackData(var restoreCondition: (() -> Boolean)? = null,
                              var delayMillis: Int = 0)

  private var isUnderTaskProcessing = false
  private val taskActions: MutableList<TaskInfo> = ArrayList()

  var foundComponent: Component? = null
  var rehighlightComponent: (() -> Component)? = null

  private var currentRecorder: ActionsRecorder? = null

  @Volatile
  var hasBeenStopped = false
    private set

  private fun addTaskAction(content: (Int) -> Unit) {
    taskActions.add(TaskInfo(content, taskActions.size - 1))
  }

  fun getUserVisibleInfo(index: Int): PreviousTaskInfo {
    return taskActions[index].userVisibleInfo ?: throw IllegalArgumentException("No information available for task $index")
  }

  fun waitBeforeContinue(delayMillis: Int) {
    if (isUnderTaskProcessing) {
      throw IllegalStateException("Delay should be specified between tasks!")
    }

    addTaskAction { taskIndex ->
      Alarm().addRequest({ processNextTask(taskIndex + 1) }, delayMillis)
    }
  }

  fun task(taskContent: TaskContext.() -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (isUnderTaskProcessing) {
      throw IllegalStateException("Nested tasks are not permitted!")
    }

    addTaskAction { processTask(taskContent, it) }
  }

  fun stopLesson() {
    assert(ApplicationManager.getApplication().isDispatchThread)
    currentRecorder?.let { Disposer.dispose(it) }
    hasBeenStopped = true
    taskActions.clear()
    currentRecorder = null
  }

  fun prepareSample(sample: LessonSample) {
    addSimpleTaskAction {
      setSample(sample)
    }
  }

  fun caret(offset: Int) {
    addSimpleTaskAction { OpenFileDescriptor(project, virtualFile, offset).navigateIn(editor) }
  }

  fun caret(line: Int, column: Int) {
    addSimpleTaskAction { OpenFileDescriptor(project, virtualFile, line - 1, column - 1).navigateIn(editor) }
  }

  fun caret(text: String) {
    addSimpleTaskAction {
      val start = getStartOffsetForText(text, editor, project)
      editor.caretModel.moveToOffset(start.startOffset)
    }
  }

  val virtualFile: VirtualFile
    get() = FileDocumentManager.getInstance().getFile(editor.document) ?: error("No Virtual File")

  fun processNextTask(taskIndex: Int) {
    invokeLater(ModalityState.any()) {
      processNextTask2(taskIndex)
    }
  }

  private fun processNextTask2(taskIndex: Int) {
    IncorrectLearningStateNotificationProvider.clearMessage(virtualFile, project)
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (taskIndex == taskActions.size) {
      lesson.pass()
      LessonManager.instance.passLesson(project, lesson)
      return
    }
    val taskInfo = taskActions[taskIndex]
    taskInfo.messagesNumberBeforeStart = LessonManager.instance.messagesNumber()
    setUserVisibleInfo(taskIndex)
    taskInfo.content(taskIndex)
  }

  private fun setUserVisibleInfo(taskIndex: Int) {
    val taskInfo = taskActions[taskIndex]
    // do not reset information from the previous tasks if it is available already
    if (taskInfo.userVisibleInfo == null) {
      taskInfo.userVisibleInfo = object : PreviousTaskInfo {
        override val text: String = editor.document.text
        override val position: LogicalPosition = editor.caretModel.currentCaret.logicalPosition
        override val sample: LessonSample = prepareSampleFromCurrentState(editor)
        override val ui: Component? = foundComponent
      }
      taskInfo.rehighlightComponent = rehighlightComponent
    }
    //Clear user visible information for later tasks
    for (i in taskIndex + 1 until taskActions.size) {
      taskActions[i].userVisibleInfo = null
      taskActions[i].rehighlightComponent = null
    }
    foundComponent = null
    rehighlightComponent = null
  }

  private fun processTask(taskContent: TaskContext.() -> Unit, taskIndex: Int) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    val recorder = ActionsRecorder(project, editor.document)
    currentRecorder = recorder
    val taskCallbackData = TaskCallbackData()
    val taskContext = TaskContext(this, recorder, taskIndex, taskCallbackData)
    isUnderTaskProcessing = true
    taskContext.apply(taskContent)
    isUnderTaskProcessing = false

    if (taskContext.steps.isEmpty()) {
      processNextTask(taskIndex + 1)
      return
    }

    chainNextTask(taskContext, taskIndex, recorder, taskCallbackData)

    processTestActions(taskContext)
  }

  /** @return a callback to clear resources used to track restore */
  private fun checkForRestore(taskContext: TaskContext,
                              taskIndex: Int,
                              stepsRecorder: ActionsRecorder,
                              taskCallbackData: TaskCallbackData): () -> Unit {
    fun restoreTask() {
      disposeRecorderLater(stepsRecorder)
      taskContext.steps.forEach { it.cancel(true) }
      val restoreIndex = taskActions[taskIndex].restoreIndex
      val restoreInfo = taskActions[restoreIndex]
      restoreInfo.rehighlightComponent?.let { it() }
      LessonManager.instance.resetMessagesNumber(restoreInfo.messagesNumberBeforeStart)
      processNextTask(restoreIndex)
    }

    val restoreCondition = taskCallbackData.restoreCondition ?: return {}

    if (restoreCondition()) {
      // check restore condition
      restoreTask()
      return {}
    }

    val restoreRecorder = ActionsRecorder(project, editor.document)
    lateinit var clearRestore: () -> Unit
    val checkRestoreCondition = restoreRecorder.futureCheck {
      if (hasBeenStopped) {
        // Strange situation
        clearRestore()
        return@futureCheck false
      }
      restoreCondition()
    }
    val restoreFuture = if (taskCallbackData.delayMillis != 0) {
      val waited = CompletableFuture<Boolean>()
      // we need to wait some times and only then restore if the task has not been passed
      checkRestoreCondition.thenAccept {
        Alarm().addRequest({ waited.complete(true) }, taskCallbackData.delayMillis)
      }
      waited
    }
    else {
      checkRestoreCondition
    }
    clearRestore = {
      disposeRecorderLater(restoreRecorder)
      if (!restoreFuture.isDone) {
        restoreFuture.cancel(true)
      }
      if (!checkRestoreCondition.isDone && checkRestoreCondition != restoreFuture) {
        restoreFuture.cancel(true)
      }
    }
    restoreFuture.thenAccept {
      clearRestore()
      invokeLater(ModalityState.any()) { // restore check must be done after pass conditions (and they will be done during current event processing)
        if (!isTaskCompleted(taskContext)) {
          restoreTask()
        }
      }
    }
    return clearRestore
  }

  private fun chainNextTask(taskContext: TaskContext,
                            taskIndex: Int,
                            recorder: ActionsRecorder,
                            taskCallbackData: TaskCallbackData) {
    val clearRestore = checkForRestore(taskContext, taskIndex, recorder, taskCallbackData)

    recorder.tryToCheckCallback()

    taskContext.steps.forEach { step ->
      step.thenAccept {
        assert(ApplicationManager.getApplication().isDispatchThread)
        val taskHasBeenDone = isTaskCompleted(taskContext)
        if (taskHasBeenDone) {
          disposeRecorderLater(recorder)
          clearRestore()
          currentRecorder = null
          LessonManager.instance.passExercise()
          if(foundComponent == null) foundComponent = taskActions[taskIndex].userVisibleInfo?.ui
          if(rehighlightComponent == null) rehighlightComponent = taskActions[taskIndex].rehighlightComponent
          processNextTask(taskIndex + 1)
        }
      }
    }
  }

  private fun disposeRecorderLater(recorder: ActionsRecorder) {
    // Now we are inside some listener registered by recorder
    invokeLater(ModalityState.any()) {
      // So better to exit from all callbacks and then clear all related data
      Disposer.dispose(recorder)
    }
  }

  private fun isTaskCompleted(taskContext: TaskContext) = taskContext.steps.all { it.isDone && it.get() }

  private fun addSimpleTaskAction(taskAction: () -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (!isUnderTaskProcessing) {
      addTaskAction { taskIndex ->
        taskAction()
        processNextTask(taskIndex + 1)
      }
    }
    else {
      // allow some simple tasks like caret move and so on...
      taskAction()
    }
  }

  private fun processTestActions(taskContext: TaskContext) {
    if (TaskContext.inTestMode) {
      LessonManager.instance.testActionsExecutor.execute {
        taskContext.testActions.forEach { it.run() }
      }
    }
  }

  private fun setSample(sample: LessonSample) {
    invokeLater(ModalityState.NON_MODAL) {
      setDocumentCode(sample.text)
      sample.selection?.let { editor.selectionModel.setSelection(it.first, it.second) }
      editor.caretModel.moveToOffset(sample.startOffset)
    }
  }

  private fun setDocumentCode(code: String) {
    val document = editor.document
    DocumentUtil.writeInRunUndoTransparentAction {
      val documentReference = DocumentReferenceManager.getInstance().create(document)
      UndoManager.getInstance(project).nonundoableActionPerformed(documentReference, false)
      document.replaceString(0, document.textLength, code)
    }
    PsiDocumentManager.getInstance(project).commitDocument(document)
    doUndoableAction(project)
    updateGutter(editor)
  }

  private fun getStartOffsetForText(text: String, editor: Editor, project: Project): FindResult {
    val document = editor.document

    val findManager = FindManager.getInstance(project)
    val model = findManager.findInFileModel.clone()
    model.isGlobal = false
    model.isReplaceState = false
    model.stringToFind = text
    return FindManager.getInstance(project).findString(document.charsSequence, 0, model)
  }

  private fun doUndoableAction(project: Project) {
    CommandProcessor.getInstance().executeCommand(project, {
      UndoManager.getInstance(project).undoableActionPerformed(object : BasicUndoableAction() {
        override fun undo() {}
        override fun redo() {}
      })
    }, null, null)
  }

  private fun updateGutter(editor: Editor) {
    val editorGutterComponentEx = editor.gutter as EditorGutterComponentEx
    editorGutterComponentEx.revalidateMarkup()
  }
}