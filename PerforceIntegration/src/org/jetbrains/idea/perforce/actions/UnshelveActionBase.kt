package org.jetbrains.idea.perforce.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.VcsDataKeys
import org.jetbrains.idea.perforce.actions.ShelveAction.Handler.getConnections
import org.jetbrains.idea.perforce.actions.ShelveAction.Handler.supportsShelve
import org.jetbrains.idea.perforce.application.ShelvedChange

open class UnshelveActionBase(val delete: Boolean) : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    val changes = e.getData(VcsDataKeys.CHANGES)
    if (changes == null || project == null) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val connections = getConnections(project, changes.filterIsInstance<ShelvedChange.IdeaChange>()).keySet()
    e.presentation.isEnabledAndVisible = connections.isNotEmpty() && connections.all { supportsShelve(project, it) }
  }

  override fun actionPerformed(e: AnActionEvent) {
    FileDocumentManager.getInstance().saveAllDocuments()
    val changes = e.getData(VcsDataKeys.CHANGES) ?: return

    ShelfUtils.unshelveChanges(changes.map { (it as ShelvedChange.IdeaChange).original }, e.project!!, delete)
  }
}

class UnshelveAction : UnshelveActionBase(false)

class UnshelveAndDeleteAction : UnshelveActionBase(true)