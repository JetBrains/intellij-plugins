package org.jetbrains.idea.perforce.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.wm.impl.ExpandableComboAction
import com.intellij.openapi.wm.impl.ToolbarComboWidget
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import java.util.function.Function
import javax.swing.JComponent
import javax.swing.ListCellRenderer

class PerforceToolbarWidgetAction : ExpandableComboAction() {
  companion object {
    private val isConnectedKey = Key.create<Boolean>("P4_IS_CONNECTED")
    private val workspaceKey = Key.create<@NlsSafe String>("P4_WORKSPACE")
    private val statusKey = Key.create<Boolean>("P4_STATUS")
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun createPopup(event: AnActionEvent): JBPopup? {
    val project = event.project ?: return null
    val toolbarActions = ActionManager.getInstance().getAction("Perforce.Toolbar") as? ActionGroup ?: return null

    val group = DefaultActionGroup()
    group.addAll(toolbarActions)
    group.addSeparator(PerforceBundle.message("action.Perforce.Toolbar.workspaces.label"))

    val perforceSettings = PerforceSettings.getSettings(project)
    val allConnections = perforceSettings.allConnections

    for (connection in allConnections) {
      val action = PerforceToolbarWidgetHelper.WorkspaceAction(connection.connectionKey.client, connection.workingDir)
      group.add(action)
    }

    val popupFactory = JBPopupFactory.getInstance()
    val widget = event.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT) as? ToolbarComboWidget?
    val step = createStep(popupFactory, widget, group, event.dataContext)
    return createPopup(project, popupFactory, step)
  }

  private fun createStep(popupFactory: JBPopupFactory, widget: ToolbarComboWidget?, actionGroup: ActionGroup, context: DataContext): ListPopupStep<Any> {
    return popupFactory.createActionsStep(actionGroup, context, ActionPlaces.PROJECT_WIDGET_POPUP, false, true,
                                          null, widget, false, 0, false)
  }

  private fun createPopup(project: Project, popupFactory: JBPopupFactory, step: ListPopupStep<Any>): ListPopup {
    val renderer = Function<ListCellRenderer<Any>, ListCellRenderer<out Any>> { base -> base }
    return popupFactory.createListPopup(project, step, renderer)
  }

  override fun update(e: AnActionEvent) {
    val project = e.project

    if (project == null || !PerforceManager.getInstance(project).isActive) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val perforceSettings = PerforceSettings.getSettings(project)
    val connection = PerforceToolbarWidgetHelper.getConnection(e, perforceSettings)
    val workspace = connection?.connectionKey?.client
    val isNoConnections = perforceSettings.allConnections.isEmpty()

    with (e.presentation) {
      description = PerforceToolbarWidgetHelper.getDescription(workspace, isNoConnections)
      isEnabledAndVisible = true
      icon = PerforceToolbarWidgetHelper.getIcon(perforceSettings, isNoConnections, true)

      putClientProperty(workspaceKey, workspace)
      putClientProperty(isConnectedKey, !isNoConnections)
      putClientProperty(statusKey, perforceSettings.ENABLED)
    }
  }

  override fun updateCustomComponent(component: JComponent, presentation: Presentation) {
    val widget = component as? ToolbarComboWidget ?: return

    val isConnected = presentation.getClientProperty(isConnectedKey) ?: false
    val workspace = presentation.getClientProperty(workspaceKey)
    val isOnline = presentation.getClientProperty(statusKey) ?: false
    val text = PerforceToolbarWidgetHelper.getText(workspace, !isConnected, isOnline)

    widget.isExpandable = isConnected
    widget.text = text
    widget.toolTipText = presentation.description
    widget.leftIcons = listOfNotNull(presentation.icon)
  }
}