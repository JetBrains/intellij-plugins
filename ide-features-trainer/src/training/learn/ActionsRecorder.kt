package training.learn

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.messages.MessageBusConnection
import training.check.Check
import java.awt.event.KeyEvent
import java.util.concurrent.CompletableFuture

/**
 * Created by karashevich on 18/12/14.
 */
class ActionsRecorder(private val project: Project,
                      private val document: Document) : Disposable {

  private val documentListeners: MutableList<DocumentListener> = mutableListOf()
  private val actionListeners: MutableList<AnActionListener> = mutableListOf()
  private val eventDispatchers: MutableList<IdeEventQueue.EventDispatcher> = mutableListOf()

  private var disposed = false

  init {
    Disposer.register(project, this)
  }

  override fun dispose() {
    removeListeners(document, ActionManager.getInstance())
    disposed = true
    Disposer.dispose(this)
  }

  fun futureActionAndCheckAround(actionId: String, check: Check): CompletableFuture<Boolean> {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    val actionListener = object : AnActionListener {
      override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        val caughtActionId : String? = ActionManager.getInstance().getId(action)
        if (actionId == caughtActionId) {
          check.before()
        }
        else if (caughtActionId != null) {
          // remove additional state listener to check caret positions and so on
          commandListener = null
        }
      }

      override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        if (actionId == ActionManager.getInstance().getId(action)) {
          ApplicationManager.getApplication().invokeLater {
            val complete = checkComplete()
            if (!complete) {
              commandListener = object : CommandListener {
                override fun commandFinished(event: CommandEvent) {
                  checkComplete()
                }
              }
            }
          }
        }
      }

      override fun beforeEditorTyping(c: Char, dataContext: DataContext) {}

      private fun checkComplete() : Boolean {
        val complete = check.check()
        if (complete) {
          future.complete(true)
        }
        return complete
      }
    }
    actionListeners.add(actionListener)
    ActionManager.getInstance().addAnActionListener(actionListener, this)
    return future
  }

  fun futureAction(actionId: String): CompletableFuture<Boolean> {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    val listener = createActionListener { caughtActionId, _ -> if (actionId == caughtActionId) future.complete(true) }
    ActionManager.getInstance().addAnActionListener(listener, this)
    return future
  }

  fun futureListActions(listOfActions: List<String>): CompletableFuture<Boolean> {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    val mutableListOfActions = listOfActions.toMutableList()
    val listener = createActionListener { caughtActionId, _ ->
      if (mutableListOfActions.isNotEmpty() && mutableListOfActions.first() == caughtActionId) mutableListOfActions.removeAt(0)
      if (mutableListOfActions.isEmpty()) future.complete(true)
    }
    ActionManager.getInstance().addAnActionListener(listener, this)
    // Message bus is here to track switching to other files in lessons; do not remove it.
    // One can specify the file name as a trigger. It means that specific step won't be marked as completed until user navigates to the correct file.
    myMessageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
      override fun selectionChanged(event: FileEditorManagerEvent) {
        System.out.println(event.newFile?.name)
        event.newFile?.name.let {
          if (mutableListOfActions.isNotEmpty() && mutableListOfActions.first() == event.newFile?.name) mutableListOfActions.removeAt(0)
          if (mutableListOfActions.isEmpty()) future.complete(true)
        }
      }
    })
    return future
  }

  fun futureCheck(checkFunction: () -> Boolean): CompletableFuture<Boolean> {
    val future: CompletableFuture<Boolean> = CompletableFuture()
    val listener = createDocumentListener {
      if (!future.isDone && !future.isCancelled && checkFunction())
        future.complete(true)
    }
    addKeyEventListener {
      if (!future.isDone && !future.isCancelled && checkFunction()) {
        future.complete(true)
      }
    }
    document.addDocumentListener(listener)
    return future
  }

  private val myMessageBusConnection: MessageBusConnection
    get() {
      return project.messageBus.connect()
    }

  private fun addKeyEventListener(onKeyEvent: () -> Unit) {
    val myEventDispatcher: IdeEventQueue.EventDispatcher = IdeEventQueue.EventDispatcher { e ->
      if (e is KeyEvent) onKeyEvent()
      false
    }
    eventDispatchers.add(myEventDispatcher)
    IdeEventQueue.getInstance().addDispatcher(myEventDispatcher, this)
  }

  private fun createDocumentListener(onDocumentChange: () -> Unit): DocumentListener {
    val documentListener = object : DocumentListener {

      override fun beforeDocumentChange(event: DocumentEvent) {}

      override fun documentChanged(event: DocumentEvent) {
        if (PsiDocumentManager.getInstance(project).isUncommited(document)) {
          ApplicationManager.getApplication().invokeLater {

            if (!disposed && !project.isDisposed) {
              PsiDocumentManager.getInstance(project).commitAndRunReadAction { onDocumentChange() }
            }
          }
        }
      }
    }
    documentListeners.add(documentListener)
    return documentListener
  }

  private fun createActionListener(processAction: (actionId: String, project: Project) -> Unit): AnActionListener {
    val actionListener = object : AnActionListener {

      private var projectAvailable: Boolean = false

      override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        projectAvailable = event.project != null
      }

      override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        processAction(getActionId(action), project)
      }

      override fun beforeEditorTyping(c: Char, dataContext: DataContext) {}
    }
    actionListeners.add(actionListener)
    return actionListener
  }


  fun removeListeners(document: Document, actionManager: ActionManager) {
    if (actionListeners.isNotEmpty()) actionListeners.forEach { actionManager.removeAnActionListener(it) }
    if (documentListeners.isNotEmpty()) documentListeners.forEach { document.removeDocumentListener(it) }
    if (eventDispatchers.isNotEmpty()) eventDispatchers.forEach { IdeEventQueue.getInstance().removeDispatcher(it) }
    myMessageBusConnection.disconnect()
    actionListeners.clear()
    documentListeners.clear()
    eventDispatchers.clear()
    commandListener = null
  }

  private fun getActionId(action: AnAction): String =
      ActionManager.getInstance().getId(action) ?: action.javaClass.name


  companion object {
    @Volatile
    var commandListener: CommandListener? = null

    init {
      // This listener allows to track a lot of IDE state changes
      val messageBus = ApplicationManager.getApplication().messageBus
      messageBus.connect().subscribe(CommandListener.TOPIC, object: CommandListener {
        override fun commandStarted(event: CommandEvent) {
          commandListener?.commandStarted(event)
        }

        override fun beforeCommandFinished(event: CommandEvent) {
          commandListener?.beforeCommandFinished(event)
        }

        override fun commandFinished(event: CommandEvent) {
          commandListener?.commandFinished(event)
        }

        override fun undoTransparentActionStarted() {
          commandListener?.undoTransparentActionStarted()
        }

        override fun beforeUndoTransparentActionFinished() {
          commandListener?.beforeUndoTransparentActionFinished()
        }

        override fun undoTransparentActionFinished() {
          commandListener?.undoTransparentActionFinished()
        }
      })
    }
  }
}
