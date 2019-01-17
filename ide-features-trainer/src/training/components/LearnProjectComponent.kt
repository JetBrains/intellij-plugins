package training.components

import com.intellij.ide.ui.UISettings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.StripeButton
import com.intellij.openapi.wm.impl.ToolWindowsPane
import com.intellij.ui.GotItMessage
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import training.lang.LangManager
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.lesson.LessonManager
import training.statistic.ActivityManager
import training.ui.LearnIcons
import training.ui.LearnToolWindowFactory
import training.ui.UiManager
import java.awt.Point
import java.util.concurrent.TimeUnit
import javax.swing.JFrame

/**
 * Created by karashevich on 17/03/16.
 */
class LearnProjectComponent private constructor(private val myProject: Project) : ProjectComponent {


  override fun projectOpened() {
    registerLearnToolWindow(myProject)
    UiManager.updateToolWindow(myProject)

    //show where learn tool window locates only on the first start
    if (!PropertiesComponent.getInstance().isTrueValue(SHOW_TOOLWINDOW_INFO)) {
      StartupManager.getInstance(myProject).registerPostStartupActivity { pluginFirstStart() }
    }

  }

  override fun projectClosed() {
    LessonManager.instance.clearAllListeners()
  }


  override fun initComponent() {}

  override fun disposeComponent() {}

  override fun getComponentName(): String = "IDE Features Trainer project level component"

  private fun registerLearnToolWindow(project: Project) {

    val toolWindowManager = ToolWindowManager.getInstance(project)

    //register tool window
    val toolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW)
    if (toolWindow == null) {
      val createdToolWindow = toolWindowManager.registerToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW, true, LangManager.getInstance().getLangSupport().getToolWindowAnchor(), myProject, true)
      createdToolWindow.icon = LearnIcons.chevronToolWindowIcon
    }
  }

  fun startTrackActivity(project: Project) {
    val alarm = Alarm()
    if (ActivityManager.instance.lastActivityTime == -1L) return
    if (CourseManager.instance.calcNotPassedLessons() == 0) return
    if (CourseManager.instance.calcPassedLessons() == 0) return
    alarm.addRequest({
      val lastActivityTime = ActivityManager.instance.lastActivityTime
      val currentTimeMillis = System.currentTimeMillis()
      val TWO_WEEKS = TimeUnit.DAYS.toMillis(14)

      if (currentTimeMillis - lastActivityTime!! > TWO_WEEKS) {
        val message = StringBuilder()
        val unpassedLessons = CourseManager.instance.calcNotPassedLessons()

        message.append(LearnBundle.message("learn.activity.message", unpassedLessons, if (unpassedLessons == 1) ""
        else LearnBundle.message("learn.activity.message.lessons"))).append("<br/>")
        val notification = Notification(CourseManager.NOTIFICATION_ID,
            LearnBundle.message("learn.activity.title"),
            message.toString(),
            NotificationType.INFORMATION)
        val learnAction = object : AnAction(LearnBundle.message("learn.activity.learn")) {
          override fun actionPerformed(e: AnActionEvent) {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW)
            toolWindow.activate(null)
            notification.expire()
          }
        }
        val laterAction = object : AnAction(LearnBundle.message("learn.activity.later")) {
          override fun actionPerformed(e: AnActionEvent) {
            notification.expire()
          }
        }
        val neverAction = object : AnAction(LearnBundle.message("learn.activity.never")) {
          override fun actionPerformed(e: AnActionEvent) {
            ActivityManager.instance.lastActivityTime = -1
            notification.expire()
          }
        }
        notification.addAction(learnAction).addAction(laterAction).addAction(neverAction).notify(project)
      }
      Disposer.dispose(alarm)
    }, 30000)
  }

  private fun pluginFirstStart() {

    //do not show popups in test mode
    if (ApplicationManager.getApplication().isUnitTestMode) return

    ApplicationManager.getApplication().invokeLater {
      val learnStripeButton = learnStripeButton
      if (learnStripeButton != null) {

        PropertiesComponent.getInstance().setValue(SHOW_TOOLWINDOW_INFO, true.toString())
        val alarm = Alarm()
        alarm.addRequest({
          pointToLearnStripeButton(learnStripeButton)
          Disposer.dispose(alarm)
        }, 5000)
      }
    }
  }

  private fun pointToLearnStripeButton(learnStripeButton: StripeButton) {

    val toolStripesAreHiddenDefault = UISettings.instance.hideToolStripes

    fun showToolStripes() {
      UISettings.instance.hideToolStripes = false
      UISettings.instance.fireUISettingsChanged()
    }

    fun hideToolStripes() {
      UISettings.instance.hideToolStripes = true
      UISettings.instance.fireUISettingsChanged()
    }

    showToolStripes()

    val toolWindowAnchor = LangManager.getInstance().getLangSupport().getToolWindowAnchor()

    if (!CourseManager.instance.showGotMessage) return
    GotItMessage.createMessage(LearnBundle.message("learn.tool.window.quick.access.title"),
        LearnBundle.message("learn.tool.window.quick.access.message", toolWindowAnchor))
        .setCallback({ if (toolStripesAreHiddenDefault) hideToolStripes() })
        .show(RelativePoint(learnStripeButton,
            Point(learnStripeButton.bounds.width, learnStripeButton.bounds.height / 2)),
            getBalloonPosition(toolWindowAnchor))
  }
  private fun getBalloonPosition(toolWindowAnchor: ToolWindowAnchor): Balloon.Position {
    return when (toolWindowAnchor) {
      ToolWindowAnchor.LEFT -> Balloon.Position.atRight
      ToolWindowAnchor.RIGHT -> Balloon.Position.atLeft
      else -> Balloon.Position.atRight
    }
  }
  private val learnStripeButton: StripeButton?
    get() {

      val wm = WindowManager.getInstance() ?: return null
      val ideFrame = wm.getIdeFrame(myProject) ?: return null

      val rootPane = (ideFrame as JFrame).rootPane
      val pane = UIUtil.findComponentOfType(rootPane, ToolWindowsPane::class.java)
      val componentsOfType = UIUtil.findComponentsOfType(pane, StripeButton::class.java)
      return componentsOfType.lastOrNull { it.text == LearnToolWindowFactory.LEARN_TOOL_WINDOW }
    }

  companion object {
    private val LOG = Logger.getInstance(LearnProjectComponent::class.java.name)
    private val SHOW_TOOLWINDOW_INFO = "learn.toolwindow.button.info.shown"
  }

}
