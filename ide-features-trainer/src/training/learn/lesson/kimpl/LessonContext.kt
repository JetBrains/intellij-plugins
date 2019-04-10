package training.learn.lesson.kimpl

import com.intellij.find.FindManager
import com.intellij.find.FindResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.undo.BasicUndoableAction
import com.intellij.openapi.command.undo.DocumentReferenceManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.DocumentUtil
import training.commands.kotlin.TaskContext
import training.learn.ActionsRecorder
import training.learn.lesson.LessonManager
import kotlin.concurrent.thread

class LessonContext(val lesson: KLesson, val editor: Editor, val project: Project) {
  val recorder = ActionsRecorder(project, editor.document)

  init {
    LessonManager.getInstance(lesson).registerActionsRecorder(recorder)
  }

  /**
   * Start a new task in a lesson context
   */
  fun task(taskContent: TaskContext.() -> Unit) {
    recorder.removeListeners(editor.document)
    val taskContext = TaskContext(lesson, editor, project, recorder)
    taskContext.apply(taskContent)

    if (TaskContext.inTestMode) {
      thread(name = "TestLearningPlugin") {
        taskContext.testActions.forEach { it.run() }
      }
    }

    taskContext.steps.all { it.get() }
    LessonManager.getInstance(lesson).passExercise()
  }

  /** Describe a simple task: just one action required */
  fun actionTask(action: String, getText: TaskContext.(action: String) -> String) {
    task {
      text(getText(action))
      trigger(action)
      test { actions(action) }
    }
  }

  /**
   * Just shortcut to write action name once
   * @see task
   */
  fun task(action: String, taskContent: TaskContext.(action: String) -> Unit) {
    task {
      taskContent(action)
    }
  }


  fun setDocumentCode(code: String) {
    ApplicationManager.getApplication().invokeAndWait {
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
  }

  fun caret(offset: Int) {
    runInEdt { editor.caretModel.moveToOffset(offset) }
  }

  fun caret(line: Int, column: Int) {
    runInEdt { editor.caretModel.moveToLogicalPosition(LogicalPosition(line - 1, column - 1)) }
  }

  fun caret(text: String) {
    runInEdt {
      val start = getStartOffsetForText(text, editor, project)
      editor.caretModel.moveToOffset(start.startOffset)
    }
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

  private inline fun runInEdt(crossinline runnable: () -> Unit) {
    val app = ApplicationManager.getApplication()
    if (app.isDispatchThread) {
      runnable()
    } else {
      app.invokeLater({ runnable() }, ModalityState.defaultModalityState())
    }
  }

  fun prepareSample(sample: LessonSample) {
    setDocumentCode(sample.text)
    caret(sample.getInfo(START_TAG).startOffset)
  }
}
