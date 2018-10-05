package training.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Created by jetbrains on 17/03/16.
 */
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

