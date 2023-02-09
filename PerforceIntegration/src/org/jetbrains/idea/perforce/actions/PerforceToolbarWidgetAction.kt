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
import com.intellij.ui.popup.util.PopupImplUtil
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

    val actionGroup = ActionManager.getInstance().getAction("VcsToolbarActions") as? ActionGroup ?: return null
    val popupFactory = JBPopupFactory.getInstance()
    val widget = event.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT) as? ToolbarComboWidget?
    val step = createStep(popupFactory, widget, actionGroup, event.dataContext)
    return createPopup(project, popupFactory, widget, step)
  }

  private fun createStep(popupFactory: JBPopupFactory, widget: ToolbarComboWidget?, actionGroup: ActionGroup, context: DataContext): ListPopupStep<Any> {
    return popupFactory.createActionsStep(actionGroup, context, ActionPlaces.PROJECT_WIDGET_POPUP, false, false,
                                          null, widget, false, 0, false)
  }

  private fun createPopup(project: Project, popupFactory: JBPopupFactory, widget: ToolbarComboWidget?, step: ListPopupStep<Any>): ListPopup {
    val renderer = Function<ListCellRenderer<Any>, ListCellRenderer<out Any>> { base -> base }
    val popup = popupFactory.createListPopup(project, step, renderer)
    PopupImplUtil.setPopupToggleButton(popup, widget)
    return popup
  }

  override fun update(e: AnActionEvent) {
    val project = e.project

    if (project == null || !PerforceManager.getInstance(project).isActive) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val file = e.getData(PlatformCoreDataKeys.VIRTUAL_FILE)
               ?: e.getData(PlatformCoreDataKeys.VIRTUAL_FILE_ARRAY)?.firstOrNull()
               ?: e.getData(PlatformCoreDataKeys.PROJECT_FILE_DIRECTORY)

    if (file == null) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    e.presentation.isEnabledAndVisible = true
    val perforceSettings = PerforceSettings.getSettings(project)
    val connection = perforceSettings.getConnectionForFile(file)
    if (connection == null) {
      with(e.presentation) {
        putClientProperty(isConnectedKey, false)
        putClientProperty(workspaceKey, "")
        putClientProperty(statusKey, false)
        description = PerforceBundle.message("connection.no.valid.connections")
      }
    }
    else {
      val isOnline = perforceSettings.ENABLED
      with(e.presentation) {
        putClientProperty(isConnectedKey, true)
        putClientProperty(workspaceKey, connection.connectionKey.client)
        putClientProperty(statusKey, isOnline)

        description = PerforceBundle.message("action.Perforce.Toolbar.WorkspaceAction.description")
      }
    }
  }

  override fun updateCustomComponent(component: JComponent, presentation: Presentation) {
    val widget = component as? ToolbarComboWidget ?: return

    val isConnected = presentation.getClientProperty(isConnectedKey) ?: false
    val workspace = presentation.getClientProperty(workspaceKey)
    val isOnline = presentation.getClientProperty(statusKey) ?: false
    val text = when {
      !isConnected -> PerforceBundle.message("connection.no.valid.connections.short")
      isOnline -> "$workspace"
      else -> "$workspace: ${PerforceBundle.message("connection.status.offline")}"
    }

    widget.isExpandable = isConnected
    widget.text = text
    widget.toolTipText = presentation.description
    widget.leftIcons = listOfNotNull(presentation.icon)
  }
}