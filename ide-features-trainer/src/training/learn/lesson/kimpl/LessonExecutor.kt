// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.kimpl

import com.intellij.find.FindManager
import com.intellij.find.FindResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.BasicUndoableAction
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.Alarm
import com.intellij.util.DocumentUtil
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import training.ui.IncorrectLearningStateNotificationProvider

class LessonExecutor(val lesson: KLesson, val editor: Editor, val project: Project) {
  private data class TaskInfo(val content: (Int) -> Unit, var restoreIndex: Int = 0, var messagesNumberBeforeStart: Int = 0)

  private var isUnderTaskProcessing = false
  private val taskActions: MutableList<TaskInfo> = ArrayList()

  private var currentRecorder: ActionsRecorder? = null

  @Volatile
  var hasBeenStopped = false
    private set

  private fun addTaskAction(content: (Int) -> Unit) {
    taskActions.add(TaskInfo(content, taskActions.size - 1))
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
    addSimpleTaskAction { editor.caretModel.moveToOffset(offset) }
  }

  fun caret(line: Int, column: Int) {
    addSimpleTaskAction {
      editor.caretModel.moveToLogicalPosition(LogicalPosition(line - 1, column - 1))
    }
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
    IncorrectLearningStateNotificationProvider.clearMessage(virtualFile, project)
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (taskIndex == taskActions.size) {
      lesson.pass()
      LessonManager.instance.passLesson(project, lesson)
      return
    }
    val taskInfo = taskActions[taskIndex]
    taskInfo.messagesNumberBeforeStart = LessonManager.instance.messagesNumber()
    taskInfo.content(taskIndex)
  }

  private fun processTask(taskContent: TaskContext.() -> Unit, taskIndex: Int) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    val recorder = ActionsRecorder(project, editor.document)
    currentRecorder = recorder
    val restoreContentRef = Ref<() -> Boolean>()
    val taskContext = TaskContext(this, recorder, restoreContentRef)
    isUnderTaskProcessing = true
    taskContext.apply(taskContent)
    isUnderTaskProcessing = false

    if (taskContext.steps.isEmpty()) {
      processNextTask(taskIndex + 1)
      return
    }

    chainNextTask(taskContext, taskIndex, recorder, restoreContentRef.get())

    processTestActions(taskContext)
  }

  private fun checkForRestore(taskContext: TaskContext,
                              taskIndex: Int,
                              stepsRecorder: ActionsRecorder,
                              restoreContent: (() -> Boolean)?): () -> Unit {
    restoreContent ?: return {}
    val restoreRecorder = ActionsRecorder(project, editor.document)
    val restoreStep = restoreRecorder.futureCheck { restoreContent() }
    val clearRestore = {
      // We can be now inside some listener registered by recorder
      disposeRecorderLater(restoreRecorder)
      if (!restoreStep.isDone) {
        restoreStep.cancel(true)
      }
    }
    restoreStep.thenAccept {
      if (!isTaskCompleted(taskContext)) {
        clearRestore()
        disposeRecorderLater(stepsRecorder)
        taskContext.steps.forEach { it.cancel(true) }
        val restoreIndex = taskActions[taskIndex].restoreIndex
        LessonManager.instance.resetMessagesNumber(taskActions[restoreIndex].messagesNumberBeforeStart)
        processNextTask(restoreIndex)
      }
    }
    return clearRestore
  }

  private fun chainNextTask(taskContext: TaskContext,
                            taskIndex: Int,
                            recorder: ActionsRecorder,
                            restoreContent: (() -> Boolean)?) {
    val clearRestore = checkForRestore(taskContext, taskIndex, recorder, restoreContent)

    taskContext.steps.forEach { step ->
      step.thenAccept {
        assert(ApplicationManager.getApplication().isDispatchThread)
        val taskHasBeenDone = isTaskCompleted(taskContext)
        if (taskHasBeenDone) {
          disposeRecorderLater(recorder)
          clearRestore()
          currentRecorder = null
          LessonManager.instance.passExercise()
          processNextTask(taskIndex + 1)
        }
      }
    }
  }

  private fun disposeRecorderLater(recorder: ActionsRecorder) {
    // Now we are inside some listener registered by recorder
    ApplicationManager.getApplication().invokeLater {
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
    setDocumentCode(sample.text)
    sample.selection?.let { editor.selectionModel.setSelection(it.first, it.second) }
    editor.caretModel.moveToOffset(sample.startOffset)
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