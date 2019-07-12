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
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.Alarm
import com.intellij.util.DocumentUtil
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager

class LessonExecutor(val lesson: KLesson, val editor: Editor, val project: Project) {
  private var isUnderTaskProcessing = false
  private val taskActions: MutableList<() -> Unit> = ArrayList()

  private var currentRecorder: ActionsRecorder? = null

  fun waitBeforeContinue(delayMillis: Int) {
    if(isUnderTaskProcessing) {
      throw IllegalStateException("Delay should be specified between tasks!")
    }

    taskActions.add {
      Alarm().addRequest({ processNextTask() }, delayMillis)
    }
  }

  fun task(taskContent: TaskContext.() -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if(isUnderTaskProcessing) {
      throw IllegalStateException("Nested tasks are not permitted!")
    }

    taskActions.add { processTask(taskContent) }
  }

  fun stopLesson() {
    assert(ApplicationManager.getApplication().isDispatchThread)
    currentRecorder?.let { Disposer.dispose(it) }
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

  fun processNextTask() {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (taskActions.size == 0) {
      lesson.pass()
      LessonManager.instance.passLesson(project, lesson)
      return
    }
    val content = taskActions[0]
    taskActions.removeAt(0)
    content()
  }

  private fun processTask(taskContent: TaskContext.() -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    val recorder = ActionsRecorder(project, editor.document)
    currentRecorder = recorder
    val taskContext = TaskContext(lesson, editor, project, recorder)
    isUnderTaskProcessing = true
    taskContext.apply(taskContent)
    isUnderTaskProcessing = false

    if (TaskContext.inTestMode) {
      LessonManager.instance.testActionsExecutor.execute {
        taskContext.testActions.forEach { it.run() }
      }
    }

    if (taskContext.steps.isEmpty()) {
      processNextTask()
      return
    }

    chainNextTask(taskContext, recorder)
  }

  private fun chainNextTask(taskContext: TaskContext, recorder: ActionsRecorder) {
    taskContext.steps.forEach { step ->
      step.thenAccept {
        assert(ApplicationManager.getApplication().isDispatchThread)
        val taskHasBeenDone = taskContext.steps.all { it.isDone }
        if (taskHasBeenDone) {
          // Now we are inside some listener registered by recorder
          ApplicationManager.getApplication().invokeLater {
            // So better to exit from all callbacks and then clear all related data
            Disposer.dispose(recorder)
            currentRecorder = null
            LessonManager.instance.passExercise()

            processNextTask()
          }
        }
      }
    }
  }

  private fun addSimpleTaskAction(taskAction: () -> Unit) {
    assert(ApplicationManager.getApplication().isDispatchThread)
    if (!isUnderTaskProcessing) {
      taskActions.add {
        taskAction()
        processNextTask()
      }
    }
    else {
      // allow some simple tasks like caret move and so on...
      taskAction()
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