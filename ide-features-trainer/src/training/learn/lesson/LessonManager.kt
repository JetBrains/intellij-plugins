// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManagerListener
import com.intellij.openapi.project.Project
import org.jdom.Element
import training.commands.kotlin.TaskContext
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Lesson
import training.learn.lesson.kimpl.LessonExecutor
import training.ui.LearningUiHighlightingManager
import training.ui.LearningUiManager
import training.ui.Message
import training.ui.views.LearnPanel
import training.util.createNamedSingleThreadExecutor
import java.util.concurrent.Executor

@Service
class LessonManager {
  var currentLesson: Lesson? = null
    private set

  private val learnPanel: LearnPanel?
    get() = LearningUiManager.activeToolWindow?.learnPanel

  private var currentLessonExecutor: LessonExecutor? = null

  var shownRestoreNotification : TaskContext.RestoreNotification? = null
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
    shownRestoreNotification = null
    currentLessonExecutor?.stopLesson()
    currentLessonExecutor?.lesson?.onStop()
    LearningUiHighlightingManager.clearHighlights()
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
    shownRestoreNotification = null
    learnPanel?.resetMessagesNumber(number)
  }

  fun messagesNumber(): Int = learnPanel?.messagesNumber() ?: 0

  fun passExercise() {
    learnPanel?.setPreviousMessagesPassed()
  }

  fun passLesson(project: Project, cLesson: Lesson) {
    cLesson.pass()
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

  fun addLessonListener(lessonListener: LessonListener) {
    lessonListeners.add(lessonListener)
  }

  fun removeLessonListener(lessonListener: LessonListener) {
    lessonListeners.remove(lessonListener)
  }

  fun clearRestoreMessage() {
    if (shownRestoreNotification != null) {
      resetMessagesNumber(messagesNumber() - 1)
    }
  }

  fun setRestoreNotification(notification: TaskContext.RestoreNotification) {
    clearRestoreMessage()
    val proposalText = notification.message
    val warningIconIndex = LearningUiManager.getIconIndex(AllIcons.General.NotificationWarning)
    val callback = Runnable {
      notification.callback()
      invokeLater {
        clearRestoreMessage()
      }
    }
    learnPanel?.addMessages(arrayOf(Message(warningIconIndex, Message.MessageType.ICON_IDX),
                                    Message(" ${proposalText} ", Message.MessageType.TEXT_BOLD),
                                    Message("Restore", Message.MessageType.LINK).also { it.runnable = callback }
    ))
    LearningUiManager.activeToolWindow?.updateScrollPane()
    shownRestoreNotification = notification
  }


  companion object {
    @Volatile
    var externalTestActionsExecutor: Executor? = null
    private var lastEditor: Editor? = null

    val instance: LessonManager
      get() = service()

    private val LOG = logger<LessonManager>()
  }
}
