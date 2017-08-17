package training.learn.lesson

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import training.editor.MouseListenerHolder
import training.editor.actions.BlockCaretAction
import training.editor.actions.LearnActions
import training.learn.ActionsRecorder
import training.learn.CourseManager
import training.learn.LearnBundle
import training.ui.LearnBalloonBuilder
import training.ui.Message
import java.util.*

/**
 * Created by karashevich on 18/03/16.
 */
class LessonManager {

  private var myCurrentLesson: Lesson? = null
  private var learnBalloonBuilder: LearnBalloonBuilder? = null
  private val balloonDelay = 3000

  constructor(lesson: Lesson, editor: Editor) {
    myCurrentLesson = lesson
    mouseBlocked = false
    if (myLearnActions == null) myLearnActions = ArrayList<LearnActions>()
    learnBalloonBuilder = null
    lastEditor = editor
    mouseListenerHolder = null
  }

  constructor() {
    myCurrentLesson = null
    mouseBlocked = false
    if (myLearnActions == null) myLearnActions = ArrayList<LearnActions>()
    lastEditor = null
    mouseListenerHolder = null
  }

  private fun setCurrentLesson(lesson: Lesson) {
    myCurrentLesson = lesson
  }

  @Throws(Exception::class)
  internal fun initLesson(editor: Editor) {
    cleanEditor() //remove mouse blocks and action recorders from last editor
    lastEditor = editor //rearrange last editor

    val learnPanel = CourseManager.getInstance().learnPanel
    learnPanel.setLessonName(myCurrentLesson!!.name)
    val module = myCurrentLesson!!.module ?: throw Exception("Unable to find module for lesson: " + myCurrentLesson!!)
    val moduleName = module.name
    learnPanel.setModuleName(moduleName)
    learnPanel.modulePanel.init(myCurrentLesson)
    clearEditor(editor)
    clearLessonPanel()
    removeActionsRecorders()

    learnBalloonBuilder = LearnBalloonBuilder(editor, balloonDelay, LearnBundle
        .message("learn.ui.balloon.blockCaret.message"))

    if (mouseListenerHolder != null) mouseListenerHolder!!.restoreMouseActions(editor)

    if (myLearnActions != null) {
      for (myLearnAction in myLearnActions!!) {
        myLearnAction.unregisterAction()
      }
      myLearnActions!!.clear()
    }

    var runnable: Runnable? = null
    var buttonText: String? = null
    val lesson = CourseManager.getInstance().giveNextLesson(myCurrentLesson)
    if (lesson != null) {
      //            buttonText = lesson.getName();
      runnable = Runnable {
        try {
          CourseManager.getInstance().openLesson(editor.project, lesson)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    } else {
      val nextModule = CourseManager.getInstance().giveNextModule(myCurrentLesson)
      if (nextModule != null) {
        buttonText = nextModule.name
        runnable = Runnable {
          val notPassedLesson = nextModule.giveNotPassedLesson()
          if (notPassedLesson == null) {
            try {
              CourseManager.getInstance().openLesson(editor.project, nextModule.lessons[0])
            } catch (e: Exception) {
              e.printStackTrace()
            }

          } else {
            try {
              CourseManager.getInstance().openLesson(editor.project, notPassedLesson)
            } catch (e: Exception) {
              e.printStackTrace()
            }

          }
        }
      }
    }

    if (runnable != null) {
      CourseManager.getInstance().learnPanel.setButtonSkipAction(runnable, buttonText, true)
    } else {
      CourseManager.getInstance().learnPanel.setButtonSkipAction(null, null, false)
    }
  }

  fun addMessage(message: String) {
    CourseManager.getInstance().learnPanel.addMessage(message)
    CourseManager.getInstance().updateToolWindowScrollPane()
  }

  fun addMessages(messages: Array<Message>) {
    CourseManager.getInstance().learnPanel.addMessages(messages)
    CourseManager.getInstance().updateToolWindowScrollPane()
  }

  fun passExercise() {
    CourseManager.getInstance().learnPanel.setPreviousMessagesPassed()
  }

  fun passLesson(project: Project, editor: Editor) {
    val learnPanel = CourseManager.getInstance().learnPanel
    learnPanel.setLessonPassed()
    if (myCurrentLesson!!.module != null && myCurrentLesson!!.module!!.hasNotPassedLesson()) {
      val notPassedLesson = myCurrentLesson!!.module!!.giveNotPassedLesson()
      learnPanel.setButtonNextAction({
        try {
          CourseManager.getInstance().openLesson(project, notPassedLesson)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }, notPassedLesson)
    } else {
      val module = CourseManager.getInstance().giveNextModule(myCurrentLesson)
      if (module == null) {
        clearLessonPanel()
        learnPanel.setModuleName("")
        learnPanel.setLessonName(LearnBundle.message("learn.ui.course.completed.caption"))
        learnPanel.addMessage(LearnBundle.message("learn.ui.course.completed.description"))
        learnPanel.hideNextButton()
      } else {
        var lesson = module.giveNotPassedLesson()
        if (lesson == null) lesson = module.lessons[0]

        val lessonFromNextModule = lesson
        val nextModule = lessonFromNextModule!!.module ?: return
        learnPanel.setButtonNextAction({
          try {
            CourseManager.getInstance().openLesson(project, lessonFromNextModule)
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }, lesson, LearnBundle.message("learn.ui.button.next.module") + " " + nextModule.name)
      }
    }
    learnPanel.modulePanel.updateLessons(myCurrentLesson)
  }


  fun clearEditor(editor: Editor?) {
    ApplicationManager.getApplication().runWriteAction {
      if (editor != null) {
        val document = editor.document
        try {
          document.setText("")
        } catch (e: Exception) {
          System.err.println("Unable to update text in editor!")
        }

      }
    }
  }

  private fun clearLessonPanel() {
    CourseManager.getInstance().learnPanel.clearLessonPanel()
  }

  private fun hideButtons() {
    CourseManager.getInstance().learnPanel.hideButtons()
  }

  private fun removeActionsRecorders() {
    for (actionsRecorder in actionsRecorders) {
      actionsRecorder.dispose()
    }
    actionsRecorders.clear()
  }

  private var isMouseBlocked: Boolean
    get() = mouseBlocked
    set(mouseBlocked) {
      LessonManager.mouseBlocked = mouseBlocked
    }

  fun unblockCaret() {
    val myBlockActions = ArrayList<BlockCaretAction>()

    for (myLearnAction in myLearnActions!!) {
      if (myLearnAction is BlockCaretAction) {
        myBlockActions.add(myLearnAction)
        myLearnAction.unregisterAction()
      }
    }

    myLearnActions!!.removeAll(myBlockActions)
  }


  fun blockCaret(editor: Editor) {

    //        try {
    //            LearnUiUtil.getInstance().drawIcon(myProject, getEditor());
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }

    myLearnActions!!
        .filterIsInstance<BlockCaretAction>()
        .forEach { return }

    val blockCaretAction = BlockCaretAction(editor)
    blockCaretAction.addActionHandler(Runnable{
      try {
        showCaretBlockedBalloon()
      } catch (e: InterruptedException) {
        e.printStackTrace()
      }
    })
    myLearnActions!!.add(blockCaretAction)
  }

  fun registerActionsRecorder(recorder: ActionsRecorder) {
    actionsRecorders.add(recorder)
  }

  @Throws(InterruptedException::class)
  private fun showCaretBlockedBalloon() {
    learnBalloonBuilder!!.showBalloon()
  }

  private fun cleanEditor() {
    if (lastEditor == null) return
    if (mouseListenerHolder != null) mouseListenerHolder!!.restoreMouseActions(lastEditor!!)
    removeActionsRecorders()
    unblockCaret()
  }

  fun blockMouse(editor: Editor) {
    mouseListenerHolder = MouseListenerHolder(editor)
    mouseListenerHolder!!.grabMouseActions(Runnable{
      try {
        showCaretBlockedBalloon()
      } catch (e: InterruptedException) {
        e.printStackTrace()
      }
    })
  }

  fun unblockMouse(editor: Editor) {
    if (mouseListenerHolder != null) {
      mouseListenerHolder!!.restoreMouseActions(editor)
    }
  }

  companion object {

    private var myLearnActions: ArrayList<LearnActions>? = null
    private var mouseBlocked = false
    private val actionsRecorders = HashSet<ActionsRecorder>()
    private var lastEditor: Editor? = null
    private var mouseListenerHolder: MouseListenerHolder? = null

    private val instance: LessonManager
      get() = ServiceManager.getService(LessonManager::class.java)

    fun getInstance(lesson: Lesson): LessonManager {
      val lessonManager = instance
      lessonManager.setCurrentLesson(lesson)
      return lessonManager
    }
  }

}
