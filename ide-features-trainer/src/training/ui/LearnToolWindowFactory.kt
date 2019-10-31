/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class LearnToolWindowFactory : ToolWindowFactory, DumbAware {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val learnToolWindow = LearnToolWindow()
    learnToolWindow.init(project)
    val contentManager = toolWindow.contentManager
    val content = contentManager.factory.createContent(learnToolWindow, null, false)
    content.isCloseable = false
    contentManager.addContent(content)
    learnWindowPerProject.put(project, learnToolWindow)
    Disposer.register(project, learnToolWindow)
  }

  companion object {
    val LEARN_TOOL_WINDOW = "Learn"
    val learnWindowPerProject = HashMap<Project, LearnToolWindow>()
  }
}

