package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.jetbrains.lang.makefile.MakefileLangBundle
import icons.MakefileIcons

private const val TOOLWINDOW_ID = "make" // the ID is unfortunate, but should be kept compatible with older versions

internal class MakefileToolWindowRegistrar : StartupActivity {
  override fun runActivity(project: Project) {
    val toolWindowManager = ToolWindowManager.getInstance(project)

    toolWindowManager.invokeLater {
      val toolWindow = toolWindowManager.registerToolWindow(RegisterToolWindowTask(
        id = TOOLWINDOW_ID,
        stripeTitle = MakefileLangBundle.messagePointer("tool.window.title"),
        icon = MakefileIcons.MakefileToolWindow,
        contentFactory = MakeToolWindowFactory(),
        anchor = ToolWindowAnchor.RIGHT,
        sideTool = true))


      if (shouldDisableStripeButton(project, toolWindowManager)) {
        toolWindow.isShowStripeButton = false
      }
    }
  }

  private fun shouldDisableStripeButton(project: Project, manager: ToolWindowManager): Boolean {
    val windowInfo = (manager as ToolWindowManagerEx).getLayout().getInfo(TOOLWINDOW_ID)
    // toolwindow existed in ths project before - show it
    if (windowInfo != null && windowInfo.isFromPersistentSettings) {
      return false
    }

    // any extension reported that it's desired to hide it by default (i.e. it's CLion's non-Makefile project) - hide it
    if (MakefileToolWindowStripeController.EP_NAME.extensionList.any { it.shouldHideStripeIconFor(project) }) {
      return true
    }

    // show it otherwise
    return false
  }
}
