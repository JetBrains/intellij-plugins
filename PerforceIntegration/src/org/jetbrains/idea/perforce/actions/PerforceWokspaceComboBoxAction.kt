package org.jetbrains.idea.perforce.actions

import com.intellij.ide.ui.ToolbarSettings
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import java.awt.event.ActionEvent
import javax.swing.JComponent

class PerforceWorkspaceComboBoxAction : ComboBoxAction(), DumbAware {
  companion object {
    private val multipleWorkspacesKey = Key.create<Boolean>("P4_MULTIPLE_WORKSPACES")
  }

  override fun shouldShowDisabledActions(): Boolean = true

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    val presentation = e.presentation
    if (project == null || !ToolbarSettings.getInstance().isAvailable ||
        !PerforceManager.getInstance(project).isActive) {
      presentation.isEnabledAndVisible = false
      return
    }

    val perforceSettings = PerforceSettings.getSettings(e.project)
    val connection = PerforceToolbarWidgetHelper.getConnection(e, perforceSettings)
    val isNoConnections = perforceSettings.allConnections.isEmpty()
    val workspace = connection?.connectionKey?.client

    with (presentation) {
      isEnabledAndVisible = true
      icon = PerforceToolbarWidgetHelper.getIcon(perforceSettings, isNoConnections, false)
      description = PerforceToolbarWidgetHelper.getDescription(workspace, isNoConnections, perforceSettings.ENABLED)

      val text = PerforceToolbarWidgetHelper.getText(workspace, isNoConnections, perforceSettings.ENABLED)
      setText(text, false)

      putClientProperty(multipleWorkspacesKey, perforceSettings.allConnections.count() > 1)
    }
  }

  override fun createComboBoxButton(presentation: Presentation): ComboBoxButton = PerforceWorkspaceComboBox(presentation)

  override fun updateCustomComponent(component: JComponent, presentation: Presentation) {
    val workspaceComboBox = component as? PerforceWorkspaceComboBox ?: return
    val isMultipleWorkspaces = presentation.getClientProperty(multipleWorkspacesKey) ?: return
    workspaceComboBox.isMultipleWorkspaces = isMultipleWorkspaces
  }

  private inner class PerforceWorkspaceComboBox(presentation: Presentation) : ComboBoxButton(presentation) {
    var isMultipleWorkspaces = false

    override fun isArrowVisible(): Boolean = isMultipleWorkspaces

    override fun fireActionPerformed(event: ActionEvent?) {
      if (isMultipleWorkspaces)
        super.fireActionPerformed(event)
    }
  }

  override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
    val project = dataContext.getData(CommonDataKeys.PROJECT)
    val perforceSettings = PerforceSettings.getSettings(project)

    val actionGroup = DefaultActionGroup()

    for (connection in perforceSettings.allConnections) {
      val action = PerforceToolbarWidgetHelper.WorkspaceAction(connection.connectionKey.client, connection.workingDir)
      actionGroup.add(action)
    }

    return actionGroup
  }
}