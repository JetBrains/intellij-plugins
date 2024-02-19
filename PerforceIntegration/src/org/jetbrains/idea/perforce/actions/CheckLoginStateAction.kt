package org.jetbrains.idea.perforce.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager

class CheckLoginStateAction : DumbAwareAction() {
  override fun update(e: AnActionEvent) {
    val project = e.project
    e.presentation.isVisible = project != null &&
                               ProjectLevelVcsManager.getInstance(project).checkVcsIsActive(PerforceVcs.NAME) &&
                               PerforceSettings.getSettings(project).ENABLED
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    PerforceLoginManager.getInstance(project).checkAndRepairAll()
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}