package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TextFieldWithStoredHistory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.util.IconUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import icons.ClionEmbeddedPlatformioIcons
import java.awt.BorderLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.BorderFactory

class PlatformioToolWindowFactory : ToolWindowFactory, DumbAware {
  override fun init(toolWindow: ToolWindow) {
    super.init(toolWindow)
    toolWindow.setIcon(IconUtil.resizeSquared(ClionEmbeddedPlatformioIcons.Platformio, 13))
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val panel = createPanel(project)
    val contentManager = toolWindow.contentManager
    val content = contentManager.factory.createContent(panel, null, false)
    contentManager.addContent(content)
  }

  private fun createPanel(project: Project): JBPanel<*> {
    val actionManager = ActionManager.getInstance()
    val toolbarActions = DefaultActionGroup(
      actionManager.getAction(PlatformioRefreshAction::class.java.simpleName),
      VerboseToggleAction(project)
    )
    val panel = BorderLayoutPanel()
    val actionToolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, toolbarActions, true)
    actionToolbar.targetComponent = panel

    val mainPanel = JBPanelWithEmptyText(BorderLayout())
    val tree = PlatformioActionTree(project, mainPanel.emptyText)
    mainPanel.add(tree, BorderLayout.CENTER)
    tree.isOpaque = true
    return panel
      .addToTop(actionToolbar.component)
      .addToCenter(ScrollPaneFactory.createScrollPane(mainPanel))
      .addToBottom(portComponent(project))
  }

  private fun portComponent(project: Project): DialogPanel {
    return panel {
      row {

        val autoPort = checkBox(ClionEmbeddedPlatformioBundle.message("checkbox.auto"))
          .customize(UnscaledGaps(left = 6, right = 4))
          .applyToComponent { isSelected = project.service<PlatformioService>().isUploadPortAuto }
          .onChanged { project.service<PlatformioService>().isUploadPortAuto = it.isSelected }
        val textFieldWithStoredHistory =
          TextFieldWithStoredHistory("clion.embedded.platformio.upload.port").apply {
            this.textEditor.addFocusListener(object : FocusAdapter() {
              override fun focusLost(e: FocusEvent?) {
                val userText = this@apply.text
                if (!userText.isNullOrBlank() && !this@apply.history.contains(userText)) {
                  project.service<PlatformioService>().uploadPort = userText
                  this@apply.addCurrentTextToHistory()
                }
              }
            })
            addItemListener { e -> project.service<PlatformioService>().uploadPort = e.item.toString() }
            this.text = project.service<PlatformioService>().uploadPort
          }
        cell(textFieldWithStoredHistory)
          .enabledIf(autoPort.component.selected.not())
          .customize(UnscaledGaps(top = 6, left = 0, bottom = 6, 6))
          .align(Align.FILL)
      }
    }.withBorder(BorderFactory.createTitledBorder(ClionEmbeddedPlatformioBundle.message("label.upload.port")))
  }

  override fun shouldBeAvailable(project: Project): Boolean {
    return project.service<PlatformioWorkspace>().isInitialized
  }

}

private class VerboseToggleAction(val project: Project) :
  ToggleAction(ClionEmbeddedPlatformioBundle.messagePointer("checkbox.verbose"),
               AllIcons.Actions.ToggleVisibility) {

  override fun isSelected(e: AnActionEvent): Boolean =
    project.service<PlatformioService>().verbose


  override fun setSelected(e: AnActionEvent, state: Boolean) {
    project.service<PlatformioService>().verbose = state
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}