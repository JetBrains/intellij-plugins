package org.jetbrains.idea.perforce.actions

import com.intellij.ide.ui.ToolbarSettings.Companion.getInstance
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.JBUI
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import java.awt.event.ActionEvent

class PerforceWorkspaceComboBoxAction : ComboBoxAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    val presentation = e.presentation
    if (project == null || project.isDisposed || !project.isOpen) {
      presentation.isEnabledAndVisible = false
      return
    }

    if (!getInstance().isAvailable) {
      presentation.isEnabledAndVisible = false
      return
    }

    if (!PerforceManager.getInstance(project).isActive) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val file = e.getData(PlatformCoreDataKeys.VIRTUAL_FILE) ?: e.getData(PlatformCoreDataKeys.PROJECT_FILE_DIRECTORY)
    if (file == null) {
      presentation.isEnabledAndVisible = false
      return
    }

    val perforceSettings = PerforceSettings.getSettings(e.project)
    val connection = perforceSettings.getConnectionForFile(file)
    if (connection == null) {
      with (presentation) {
        isEnabledAndVisible = true
        text = PerforceBundle.message("connection.no.valid.connections.short")
        description = PerforceBundle.message("connection.no.valid.connections")
      }
    } else {
      with (presentation) {
        isEnabledAndVisible = true
        text = getText(connection, perforceSettings.ENABLED)
        description = PerforceBundle.message("action.Perforce.Toolbar.WorkspaceAction.description")
      }
    }

  }

  private fun getText(connection: P4Connection, isOnline: Boolean): @NlsSafe String {
    val workspace = connection.connectionKey.client
    if (isOnline)
      return workspace

    val color = ColorUtil.toHex(JBUI.CurrentTheme.Focus.errorColor(true))
    return "<html>$workspace: <font color=$color>${PerforceBundle.message("connection.offlineStatus")}</font></html>"
  }

  override fun createComboBoxButton(presentation: Presentation): ComboBoxButton {
    return object : ComboBoxButton(presentation) {
      override fun isArrowVisible(): Boolean = false

      override fun fireActionPerformed(event: ActionEvent?) {}
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    return
  }
}