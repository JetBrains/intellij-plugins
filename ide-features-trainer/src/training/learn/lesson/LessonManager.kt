// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.awt.RelativePoint
import org.jdom.Element
import training.editor.MouseListenerHolder
import training.editor.actions.BlockCaretAction
import training.editor.actions.LearnActions
import training.learn.ActionsRecorder
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Lesson
import training.learn.lesson.kimpl.LessonExecutor
import training.ui.LearningUiHighlightingManager
import training.ui.Message
import training.ui.UiManager
import training.util.createBalloon
import training.util.createNamedSingleThreadExecutor
import training.util.editorPointForBalloon
import java.util.*
import java.util.concurrent.Executor

@Service
class LessonManager {
  private var currentLessonExecutor: LessonExecutor? = null

  val testActionsExecutor: Executor by lazy {
    externalTestActionsExecutor ?: createNamedSingleThreadExecutor("TestLearningPlugin")
  }

  init {
    ApplicationManager.getApplication().messageBus.connect().subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {

      override fun projectClosed(project: Project) {
        serviceIfCreated<LessonManager>()?.clearAllListeners()
      }
    })
  }

  internal fun initDslLesson(editor: Editor, cLesson: Lesson, lessonExecutor: LessonExecutor) {
    initLesson(editor, cLesson)
    currentLessonExecutor = lessonExecutor
  }

  internal fun stopLesson() {
    currentLessonExecutor?.stopLesson()
    currentLessonExecutor?.lesson?.onStop()
    LessonProcessor.currentExecutionList?.lesson?.onStop()
    LearningUiHighlightingManager.clearHighlights()
    clearAllListeners()
  }

  @Throws(Exception::class)
  internal fun initLesson(editor: Editor, cLesson: Lesson) {
    stopLesson()
    //remove mouse blocks and action recorders from last editor
    lastEditor = editor //rearrange last editor
    UiManager.setLessonNameOnLearnPanels(cLesson.name)
    val module = cLesson.module
    val moduleName = module.name
    UiManager.setModuleNameOnLearnPanels(moduleName)
    UiManager.initLessonOnLearnPanels(cLesson)
    if (cLesson.existedFile == null) {
      clearEditor(editor)
    }
    clearLessonPanel()

    mouseListenerHolder?.restoreMouseActions(editor)

    myLearnActions.forEach { it.unregisterAction() }
    myLearnActions.clear()

    var runnable: Runnable? = null
    var buttonText: String? = null
    val lesson = CourseManager.instance.giveNextLesson(cLesson)
    val project = editor.project ?: throw Exception("Unable to open lesson in a null project")
    if (lesson != null) {
      //            buttonText = lesson.getName();
      runnable = Runnable {
        try {
          CourseManager.instance.openLesson(project, lesson)
        }
        catch (e: Exception) {
          LOG.error(e)
        }
      }
    }
    else {
      val nextModule = CourseManager.instance.giveNextModule(cLesson)
      if (nextModule != null) {
        buttonText = nextModule.name
        runnable = Runnable {
          val notPassedLesson = nextModule.giveNotPassedLesson()
          try {
            CourseManager.instance.openLesson(project, notPassedLesson ?: nextModule.lessons[0])
          }
          catch (e: Exception) {
            LOG.error(e)
          }
        }
      }
    }

    if (runnable != null)
      UiManager.setButtonSkipActionOnLearnPanels(runnable, buttonText, true)
    else
      UiManager.setButtonSkipActionOnLearnPanels(null, null, false)
  }

  fun addMessages(element: Element) {
    UiManager.addMessagesToLearnPanels(Message.convert(element))
    UiManager.updateToolWindowScrollPane()
  }

  fun resetMessagesNumber(number: Int) {
    UiManager.resetMessagesNumber(number)
  }

  fun messagesNumber(): Int = UiManager.messagesNumber()

  fun passExercise() {
    UiManager.setPreviousMessagePassedOnLearnPanels()
  }

  fun passLesson(project: Project, cLesson: Lesson) {
    UiManager.setLessonPassedOnLearnPanels()
    if (cLesson.module.hasNotPassedLesson()) {
      val notPassedLesson = cLesson.module.giveNotPassedLesson()
      UiManager.setButtonNextActionOnLearnPanels(Runnable {
        try {
          CourseManager.instance.openLesson(project, notPassedLesson)
        }
        catch (e: Exception) {
          LOG.error(e)
        }
      }, notPassedLesson)
    }
    else {
      val module = CourseManager.instance.giveNextModule(cLesson)
      if (module == null) {
        UiManager.setButtonNextActionOnLearnPanels(Runnable {
          clearLessonPanel()
          UiManager.setModuleNameOnLearnPanels("")
          UiManager.setLessonNameOnLearnPanels(LearnBundle.message("learn.ui.course.completed.caption"))
          UiManager.addMessageToLearnPanels(LearnBundle.message("learn.ui.course.completed.description"))
          UiManager.hideNextButtonOnLearnPanels()
        }, null, LearnBundle.message("learn.ui.course.completed.caption"))
      }
      else {
        var lesson = module.giveNotPassedLesson()
        if (lesson == null) lesson = module.lessons[0]

        val lessonFromNextModule = lesson
        val nextModule = lessonFromNextModule.module
        UiManager.setButtonNextActionOnLearnPanels(Runnable {
          try {
            CourseManager.instance.openLesson(project, lessonFromNextModule)
          }
          catch (e: Exception) {
            LOG.error(e)
          }
        }, lesson, LearnBundle.message("learn.ui.button.next.module") + " " + nextModule.name)
      }
    }
    UiManager.updateLessonsForPanels(cLesson)
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

  private fun clearLessonPanel() {
    UiManager.clearLessonPanels()
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
