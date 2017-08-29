package training.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import training.ui.views.LearnPanel
import training.ui.views.ModulesPanel

object UiManager {

  var learnPanel: LearnPanel? = null
    get() {
      field?.updateButtonUi()
      return field
    }
  var modulesPanel: ModulesPanel? = null

  fun updateToolWindow(project: Project) {
    val windowManager = ToolWindowManager.getInstance(project)
    val learnToolWindow = LearnToolWindowFactory.LEARN_TOOL_WINDOW
    windowManager.getToolWindow(learnToolWindow).contentManager.removeAllContents(false)

    val factory = LearnToolWindowFactory()
    factory.createToolWindowContent(project, windowManager.getToolWindow(learnToolWindow))
  }

  fun setLessonView() {
    val myLearnToolWindow = LearnToolWindowFactory.myLearnToolWindow!!
    val scrollPane = myLearnToolWindow.scrollPane!!
    scrollPane.setViewportView(learnPanel)
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun setModulesView() {
    val modulesPanel = modulesPanel
    modulesPanel!!.updateMainPanel()
    val myLearnToolWindow = LearnToolWindowFactory.myLearnToolWindow!!
    val scrollPane = myLearnToolWindow.scrollPane!!
    scrollPane.setViewportView(modulesPanel)
    scrollPane.revalidate()
    scrollPane.repaint()
  }

  fun updateToolWindowScrollPane() {
    val myLearnToolWindow = LearnToolWindowFactory.myLearnToolWindow ?: return
    val scrollPane = myLearnToolWindow.scrollPane
    scrollPane!!.viewport.revalidate()
    scrollPane.viewport.repaint()
    scrollPane.revalidate()
    scrollPane.repaint()
  }

}