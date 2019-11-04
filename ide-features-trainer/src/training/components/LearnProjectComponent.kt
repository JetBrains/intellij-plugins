/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.components

import com.intellij.ide.ui.UISettings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.IdeFrameImpl
import com.intellij.openapi.wm.impl.ProjectFrameHelper
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
import training.ui.LearnIcons
import training.ui.LearnToolWindowFactory
import training.ui.UiManager
import java.awt.Point

class LearnProjectComponent private constructor(private val myProject: Project) : ProjectComponent {


  override fun projectOpened() {
    registerLearnToolWindow(myProject)
    UiManager.updateToolWindow(myProject)

    //do not show popups in test mode
    if (ApplicationManager.getApplication().isUnitTestMode) return
    if (!CourseManager.instance.showGotMessage) return


    //show where learn tool window locates only on the first start
    if (!PropertiesComponent.getInstance().isTrueValue(SHOW_TOOLWINDOW_INFO)) {
      StartupManager.getInstance(myProject).registerPostStartupActivity {
        showGotMessage(
            LearnBundle.message("learn.tool.window.quick.access.title"),
            LearnBundle.message("learn.tool.window.quick.access.message", toolWindowAnchor)) {
          PropertiesComponent.getInstance().setValue(SHOW_TOOLWINDOW_INFO, true)
        }
      }
      return
    }

    val languageName = LangManager.getInstance().state.languageName  ?: return
    val oldLessonsMap = LangManager.getInstance().state.languageToLessonsNumberMap
    val thisLessonsMap = LangManager.getInstance().getLanguageToLessonsNumberMap()
    val oldNum = oldLessonsMap[languageName] ?: return
    val thisNum = thisLessonsMap[languageName] ?: return
    val newNum = thisNum - oldNum
    if (newNum <= 0) return
    StartupManager.getInstance(myProject).registerPostStartupActivity {
      notify(LearnBundle.message("learn.tool.window.updated.title"),
          LearnBundle.message("learn.tool.window.updated.message", newNum))
      LangManager.getInstance().state.languageToLessonsNumberMap = thisLessonsMap
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
      val createdToolWindow = toolWindowManager.registerToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW, true, LangManager.getInstance().getLangSupport()?.getToolWindowAnchor() ?: ToolWindowAnchor.LEFT, myProject, true)
      createdToolWindow.icon = LearnIcons.chevronToolWindowIcon
    }
  }

  private fun showGotMessage(tittle: String, message: String, gotMessageClicked: () -> Unit) {

    ApplicationManager.getApplication().invokeLater {
      val alarm = Alarm()
      alarm.addRequest({
        pointToLearnStripeButton(tittle, message, gotMessageClicked)
        Disposer.dispose(alarm)
      }, 5000)
    }
  }

  private val toolWindowAnchor: ToolWindowAnchor
    get() = LangManager.getInstance().getLangSupport()?.getToolWindowAnchor() ?: ToolWindowAnchor.LEFT

  private fun pointToLearnStripeButton(title: String, message: String, gotMessageClicked: () -> Unit) {
    val learnStripeButton = learnStripeButton ?: return
    if (!learnStripeButton.isVisible) {
      notify(title, message)
      gotMessageClicked()
      return
    }
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

    GotItMessage.createMessage(title, message)
        .setCallback { gotMessageClicked(); if (toolStripesAreHiddenDefault) hideToolStripes() }
        .show(RelativePoint(learnStripeButton,
            Point(learnStripeButton.bounds.width, learnStripeButton.bounds.height / 2)),
            getBalloonPosition(toolWindowAnchor))
  }

  private fun notify(title: String, message: String) {
    val notification = NOTIFICATION_GROUP.createNotification(
        LearnBundle.message("learn.plugin.name"), title, message, NotificationType.INFORMATION)
        .setIcon(LearnIcons.chevronToolWindowIcon)

    notification.addAction(object : AnAction(LearnBundle.message("learn.tool.window.open.action.message")) {
          override fun actionPerformed(e: AnActionEvent) {
            ToolWindowManager.getInstance(myProject).getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW)?.show(null)
            notification.hideBalloon()
          }
        })
    notification.notify(myProject)
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

      val frame= if (ideFrame is ProjectFrameHelper) {
        (wm.getIdeFrame(myProject) as ProjectFrameHelper).frame
      } else {
        (ideFrame as IdeFrameImpl)
      }
      val rootPane = frame.rootPane
      val pane = UIUtil.findComponentOfType(rootPane, ToolWindowsPane::class.java)
      val componentsOfType = UIUtil.findComponentsOfType(pane, StripeButton::class.java)
      return componentsOfType.lastOrNull { it.text == LearnToolWindowFactory.LEARN_TOOL_WINDOW }
    }

  companion object {
    private const val SHOW_TOOLWINDOW_INFO = "learn.toolwindow.button.info.shown"

    private val NOTIFICATION_GROUP : NotificationGroup by lazy {
      NotificationGroup(LearnBundle.message("learn.plugin.name"),
          NotificationDisplayType.STICKY_BALLOON,
          false)
    }
  }

}
