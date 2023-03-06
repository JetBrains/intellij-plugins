package org.jetbrains.idea.perforce.actions

import com.intellij.icons.AllIcons
import com.intellij.ide.ui.ToolbarSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.UIUtil
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import java.awt.event.ActionEvent
import javax.swing.Icon

class PerforceWorkspaceComboBoxAction : ComboBoxAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    val presentation = e.presentation
    if (project == null || !ToolbarSettings.getInstance().isAvailable ||
        !PerforceManager.getInstance(project).isActive) {
      presentation.isEnabledAndVisible = false
      return
    }

    val file = e.getData(PlatformCoreDataKeys.VIRTUAL_FILE) ?: e.getData(PlatformCoreDataKeys.PROJECT_FILE_DIRECTORY)
    if (file == null) {
      presentation.isEnabledAndVisible = false
      return
    }

    val perforceSettings = PerforceSettings.getSettings(e.project)
    val connection = perforceSettings.getConnectionForFile(file)
    presentation.isEnabledAndVisible = true
    presentation.icon = getIcon(perforceSettings, connection)
    if (connection == null) {
      with (presentation) {
        text = PerforceBundle.message("connection.no.valid.connections.short")
        description = PerforceBundle.message("connection.no.valid.connections")
      }
    }
    else {
      val workspace = connection.connectionKey.client
      with (presentation) {
        setText(getText(workspace, perforceSettings.ENABLED), false)
        description = PerforceBundle.message("action.Perforce.Toolbar.WorkspaceAction.description", workspace)
      }
    }
  }

  private fun getIcon(settings: PerforceSettings, connection: P4Connection?): Icon {
    if (connection == null || !settings.ENABLED)
      return AllIcons.General.Warning
    return AllIcons.Vcs.Branch
  }

  private fun getText(workspace: String, isOnline: Boolean): @NlsSafe String {
    if (isOnline)
      return workspace

    val color = ColorUtil.toHex(UIUtil.getErrorForeground())
    val builder = HtmlBuilder().append(
      HtmlChunk.html().addText("$workspace: ").child(HtmlChunk.font(color)
                                                       .addText(PerforceBundle.message("connection.status.offline"))))
    return builder.toString()
  }

  override fun createComboBoxButton(presentation: Presentation): ComboBoxButton {
    return object : ComboBoxButton(presentation) {
      override fun isArrowVisible(): Boolean = false

      override fun fireActionPerformed(event: ActionEvent?) {}
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    // Right now we need only to show current workspace and status.
    // Not sure if multiple workspaces inside a project is a common thing
  }
}