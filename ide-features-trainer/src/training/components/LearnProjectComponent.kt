// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import icons.FeaturesTrainerIcons
import training.lang.LangManager
import training.learn.lesson.LessonManager
import training.ui.LearnToolWindowFactory
import training.ui.UiManager

class LearnProjectComponent private constructor(private val project: Project) : ProjectComponent {

  override fun projectOpened() {
    registerLearnToolWindow()
    UiManager.updateToolWindow(project)
  }

  override fun projectClosed() {
    LessonManager.instance.clearAllListeners()
  }

  override fun initComponent() {}

  override fun disposeComponent() {}

  override fun getComponentName(): String = "IDE Features Trainer project level component"

  private fun registerLearnToolWindow() {
    val toolWindowManager = ToolWindowManager.getInstance(project)
    //register tool window
    val toolWindow = toolWindowManager.getToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW)
    if (toolWindow == null) {
      val anchor = LangManager.getInstance().getLangSupport()?.getToolWindowAnchor() ?: ToolWindowAnchor.LEFT
      val createdToolWindow = toolWindowManager
        .registerToolWindow(LearnToolWindowFactory.LEARN_TOOL_WINDOW, true, anchor, project, true)
      createdToolWindow.setIcon(FeaturesTrainerIcons.FeatureTrainerToolWindow)
    }
  }

}
