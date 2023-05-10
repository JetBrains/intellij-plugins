package org.jetbrains.idea.perforce.actions

import com.intellij.icons.AllIcons
import com.intellij.icons.ExpUiIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import javax.swing.Icon

class PerforceToolbarWidgetHelper {
  companion object {
    fun getConnection(e: AnActionEvent, settings: PerforceSettings): P4Connection? {
      val allConnections = settings.allConnections
      if (allConnections.count() == 1)
        return allConnections.single()

      val file = e.getData(PlatformDataKeys.LAST_ACTIVE_FILE_EDITOR)?.file
                 ?: e.getData(PlatformCoreDataKeys.VIRTUAL_FILE)
                 ?: e.getData(PlatformCoreDataKeys.PROJECT_FILE_DIRECTORY) ?: return settings.allConnections.firstOrNull()

      return settings.getConnectionForFile(file)
    }

    fun getText(@Nls workspace: String?, isNoConnections: Boolean, isOnline: Boolean): @NlsSafe String {
      if (isNoConnections)
        return PerforceBundle.message("connection.no.valid.connections.short")

      val text = workspace ?: PerforceBundle.message("action.Perforce.Toolbar.multiple.workspaces")

      if (isOnline)
        return text

      val color = ColorUtil.toHex(UIUtil.getErrorForeground())
      val builder = HtmlBuilder().append(
        HtmlChunk.html().addText("$text ").child(HtmlChunk.font(color)
                                                        .addText(PerforceBundle.message("connection.status.offline"))))
      return builder.toString()
    }

    fun getDescription(@Nls workspace: String?, isNoConnections: Boolean, isOnline: Boolean): @NlsSafe String {
      if (!isOnline)
        return PerforceBundle.message("connection.cannot.connect")
      if (isNoConnections)
        return PerforceBundle.message("connection.no.valid.connections")

      if (workspace != null)
        return PerforceBundle.message("action.Perforce.Toolbar.WorkspaceAction.description", workspace)

      return PerforceBundle.message("action.Perforce.Toolbar.multiple.workspaces.description")
    }

    fun getIcon(settings: PerforceSettings, isNoConnections: Boolean, isNewUi: Boolean): Icon {
      if (isNoConnections || !settings.ENABLED)
        return AllIcons.General.Warning

      if (isNewUi)
        return ExpUiIcons.General.Vcs
      return AllIcons.Vcs.Branch
    }

    private fun getWorkspaceLabel(workspace: String, workspaceDir: String) : String {
      val color = ColorUtil.toHex(JBColor.GRAY)
      val builder = HtmlBuilder().append(
        HtmlChunk.html().addText("$workspace ").child(HtmlChunk.font(color)
                                                        .addText("($workspaceDir)")))

      return builder.toString()
    }
  }

  class WorkspaceAction(workspace: String, workspaceDir: String) : AnAction(), DumbAware {
    private val workspaceLabel = getWorkspaceLabel(workspace, workspaceDir)

    override fun update(e: AnActionEvent) {
      e.presentation.setText(workspaceLabel, false)
      e.presentation.isEnabled = false
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) {}
  }
}