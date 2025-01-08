// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.perforce.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.changes.committed.CommittedChangesFilterDialog
import com.intellij.openapi.vcs.changes.committed.FilterCommittedAction
import com.intellij.openapi.vcs.changes.committed.ProjectCommittedChangesPanel
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager
import org.jetbrains.idea.perforce.changesBrowser.PerforceChangeBrowserSettingsService
import javax.swing.JComponent

internal class PerforceFilterCommittedAction : DumbAwareAction() {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    val changesPanel = e.getSelectedChangesViewContent<ProjectCommittedChangesPanel>()
    with(e.presentation) {
      isEnabledAndVisible = changesPanel != null
      val project = e.project ?: return
      val settings = PerforceChangeBrowserSettingsService.getInstance(project).settings

      icon = FilterCommittedAction.getFilterIcon(settings)
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val changesPanel = e.getSelectedChangesViewContent<ProjectCommittedChangesPanel>() ?: return
    val settingsStore = PerforceChangeBrowserSettingsService.getInstance(project)
    val dialog = CommittedChangesFilterDialog(project, changesPanel.provider.createFilterUI(true), settingsStore.settings)
    if (!dialog.showAndGet()) return

    settingsStore.saveSettings(dialog.settings)
    changesPanel.refreshChanges()
  }

  companion object {
    private inline fun <reified T : JComponent> AnActionEvent.getSelectedChangesViewContent(): T? =
      project?.let { ChangesViewContentManager.getInstance(it) }?.getActiveComponent(T::class.java)
  }
}
