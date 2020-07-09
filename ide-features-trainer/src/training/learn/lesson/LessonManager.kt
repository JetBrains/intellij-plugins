// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.awt.RelativePoint
import org.jdom.Element
import training.commands.kotlin.TaskContext
import training.editor.MouseListenerHolder
import training.editor.actions.BlockCaretAction
import training.editor.actions.LearnActions
import training.learn.ActionsRecorder
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Lesson
import training.learn.lesson.kimpl.LessonExecutor
import training.ui.LearningUiHighlightingManager
import training.ui.LearningUiManager
import training.ui.Message
import training.ui.views.LearnPanel
import training.util.createBalloon
import training.util.createNamedSingleThreadExecutor
import training.util.editorPointForBalloon
import java.util.*
import java.util.concurrent.Executor

@Service
class LessonManager {
  var currentLesson: Lesson? = null
    private set

  private val learnPanel: LearnPanel?
    get() = LearningUiManager.activeToolWindow?.learnPanel

  private var currentLessonExecutor: LessonExecutor? = null

  var shownRestoreType : TaskContext.RestoreProposal = TaskContext.RestoreProposal.None
    private set

  val testActionsExecutor: Executor by lazy {
    externalTestActionsExecutor ?: createNamedSingleThreadExecutor("TestLearningPlugin")
  }

  private val lessonListeners =  mutableListOf<LessonListener>()
  private val delegateListener = object : LessonListener {
    override fun lessonStarted(lesson: Lesson) {
      lessonListeners.forEach { it.lessonStarted(lesson) }
    }

    override fun lessonPassed(lesson: Lesson) {
      lessonListeners.forEach { it.lessonPassed(lesson) }
    }

    override fun lessonStopped(lesson: Lesson) {
      lessonListeners.forEach { it.lessonStopped(lesson) }
    }

    override fun lessonNext(lesson: Lesson) {
      lessonListeners.forEach { it.lessonNext(lesson) }
    }
  }

  init {
    val connect = ApplicationManager.getApplication().messageBus.connect()
    connect.subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
      override fun projectClosed(project: Project) {
        serviceIfCreated<LessonManager>()?.clearAllListeners()
      }
    })
    connect.subscribe(KeymapManagerListener.TOPIC, object : KeymapManagerListener {
      override fun activeKeymapChanged(keymap: Keymap?) {
        learnPanel?.lessonMessagePane?.redrawMessages()
      }

      override fun shortcutChanged(keymap: Keymap, actionId: String) {
        learnPanel?.lessonMessagePane?.redrawMessages()
      }
    })
  }

  internal fun initDslLesson(editor: Editor, cLesson: Lesson, lessonExecutor: LessonExecutor) {
    initLesson(editor, cLesson)
    currentLessonExecutor = lessonExecutor
  }

  /** Save to use in any moment (from AWT thread) */
  internal fun stopLesson() {
    currentLessonExecutor?.stopLesson()
    currentLessonExecutor?.lesson?.onStop()
    LessonProcessor.currentExecutionList?.lesson?.onStop()
    LearningUiHighlightingManager.clearHighlights()
    clearAllListeners()
  }

  @Throws(Exception::class)
  internal fun initLesson(editor: Editor, cLesson: Lesson) {
    if (!cLesson.lessonListeners.contains(delegateListener))
      cLesson.addLessonListener(delegateListener)
    val learnPanel = learnPanel ?: return
    stopLesson()
    learnPanel.clear()

    currentLesson = cLesson

    //remove mouse blocks and action recorders from last editor
    lastEditor = editor //rearrange last editor
    learnPanel.setLessonName(cLesson.name)
    val module = cLesson.module
    val moduleName = module.name
    learnPanel.setModuleName(moduleName)
    learnPanel.modulePanel.init(cLesson)
    if (cLesson.existedFile == null) {
      clearEditor(editor)
    }
    learnPanel.clearLessonPanel()

    mouseListenerHolder?.restoreMouseActions(editor)

    myLearnActions.forEach { it.unregisterAction() }
    myLearnActions.clear()

    val project = editor.project ?: throw Exception("Unable to open lesson in a null project")
    val nextLesson = CourseManager.instance.getNextNonPassedLesson(cLesson)
    if (nextLesson != null) {
      val runnable = Runnable {
        try {
          CourseManager.instance.openLesson(project, nextLesson)
        }
        catch (e: Exception) {
          LOG.error(e)
        }
      }
      val buttonText: String? = if (nextLesson.module == cLesson.module) null else nextLesson.module.name
      learnPanel.setButtonSkipActionAndText(runnable, buttonText, true)
    }
    else {
      learnPanel.setButtonSkipActionAndText(null, null, false)
    }
    learnPanel.updateButtonUi()
  }

  fun addMessages(element: Element) {
    learnPanel?.addMessages(Message.convert(element))
    LearningUiManager.activeToolWindow?.updateScrollPane()
  }

  fun resetMessagesNumber(number: Int) {
    shownRestoreType = TaskContext.RestoreProposal.None
    learnPanel?.resetMessagesNumber(number)
  }

  fun messagesNumber(): Int = learnPanel?.messagesNumber() ?: 0

  fun passExercise() {
    learnPanel?.setPreviousMessagesPassed()
  }

  fun passLesson(project: Project, cLesson: Lesson) {
    LearningUiHighlightingManager.clearHighlights()
    val learnPanel = learnPanel ?: return
    learnPanel.setLessonPassed()
    val nextLesson = CourseManager.instance.getNextNonPassedLesson(cLesson)
    if (nextLesson != null) {
      val text = if (nextLesson.module != cLesson.module)
        LearnBundle.message("learn.ui.button.next.module") + " " + nextLesson.module.name
      else null
      learnPanel.setButtonNextAction(nextLesson, text) {
        try {
          CourseManager.instance.openLesson(project, nextLesson)
        }
        catch (e: Exception) {
          LOG.error(e)
        }
      }
    }
    else {
      learnPanel.setButtonNextAction(null, LearnBundle.message("learn.ui.course.completed.caption")) {
        learnPanel.clearLessonPanel()
        learnPanel.setModuleName("")
        learnPanel.setLessonName(LearnBundle.message("learn.ui.course.completed.caption"))
        learnPanel.addMessage(LearnBundle.message("learn.ui.course.completed.description"))
        learnPanel.hideNextButton()
      }
    }
    learnPanel.updateUI()
    learnPanel.modulePanel.updateLessons(cLesson)
  }


  private fun clearEditor(editor: Editor?) {
    ApplicationManager.getApplication().runWriteAction {
      if (editor != null) {
        val document = editor.document
        try {
          document.setText("")
        }
        catch (e: Exception) {
          LOG.error(e)
          System.err.println("Unable to update text in editor!")
        }

      }
    }
  }

  private fun removeActionsRecorders() {
    for (actionsRecorder in actionsRecorders) {
      Disposer.dispose(actionsRecorder)
    }
    actionsRecorders.clear()
  }

  fun unblockCaret() {
    val myBlockActions = ArrayList<BlockCaretAction>()

    for (myLearnAction in myLearnActions) {
      if (myLearnAction is BlockCaretAction) {
        myBlockActions.add(myLearnAction)
        myLearnAction.unregisterAction()
      }
    }

    myLearnActions.removeAll(myBlockActions)
  }

  fun blockCaret(editor: Editor) {
    val blockCaretAction = BlockCaretAction(editor)
    blockCaretAction.addActionHandler(Runnable {
      try {
        showCaretBlockedBalloon()
      }
      catch (e: InterruptedException) {
        LOG.error(e)
      }
    })
    myLearnActions.add(blockCaretAction)
  }

  fun registerActionsRecorder(recorder: ActionsRecorder) {
    actionsRecorders.add(recorder)
  }

  @Throws(InterruptedException::class)
  private fun showCaretBlockedBalloon() {
    val balloon = createBalloon(LearnBundle.message("learn.ui.balloon.blockCaret.message"))
    balloon.show(RelativePoint(lastEditor?.contentComponent!!, editorPointForBalloon(lastEditor!!)), Balloon.Position.above)
  }

  private fun clearAllListeners() {
    lastEditor?.let {
      mouseListenerHolder?.restoreMouseActions(it)
      removeActionsRecorders()
      unblockCaret()
    }
  }

  fun blockMouse(editor: Editor) {
    with(MouseListenerHolder(editor)) {
      mouseListenerHolder = this
      grabMouseActions(Runnable {
        try {
          showCaretBlockedBalloon()
        }
        catch (e: InterruptedException) {
          LOG.error(e)
        }
      })
    }
  }

  fun unblockMouse(editor: Editor) {
    mouseListenerHolder?.restoreMouseActions(editor)
  }

  fun addLessonListener(lessonListener: LessonListener) {
    lessonListeners.add(lessonListener)
  }

  fun removeLessonListener(lessonListener: LessonListener) {
    lessonListeners.remove(lessonListener)
  }

  fun clearRestoreMessage() {
    if (shownRestoreType != TaskContext.RestoreProposal.None) {
      resetMessagesNumber(messagesNumber() - 1)
    }
  }

  fun addRestoreNotification(proposal: TaskContext.RestoreNotification) {
    val proposalText = when (proposal.type) {
      TaskContext.RestoreProposal.Modification -> LearnBundle.message("learn.restore.notification.modification.message")
      TaskContext.RestoreProposal.Caret -> LearnBundle.message("learn.restore.notification.caret.message")
      else -> return
    }
    val warningIconIndex = LearningUiManager.getIconIndex(AllIcons.General.NotificationWarning)
    learnPanel?.addMessages(arrayOf(Message(warningIconIndex, Message.MessageType.ICON_IDX),
                                    Message(" ${proposalText} ", Message.MessageType.TEXT_BOLD),
                                    Message("Restore", Message.MessageType.LINK).also { it.runnable = Runnable { proposal.callback() } }
    ))
    LearningUiManager.activeToolWindow?.updateScrollPane()
    shownRestoreType = proposal.type
  }


  companion object {
    @Volatile
    var externalTestActionsExecutor: Executor? = null

    private val myLearnActions = mutableListOf<LearnActions>()
    private val actionsRecorders = mutableSetOf<ActionsRecorder>()
    private var lastEditor: Editor? = null
    private var mouseListenerHolder: MouseListenerHolder? = null

    val instance: LessonManager
      get() = service()

    private val LOG = logger<LessonManager>()
  }
}
