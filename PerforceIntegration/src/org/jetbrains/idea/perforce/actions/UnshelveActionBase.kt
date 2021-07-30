package org.jetbrains.idea.perforce.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.vcs.VcsDataKeys
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.actions.ShelveAction.Companion.getConnections
import org.jetbrains.idea.perforce.actions.ShelveAction.Companion.supportsShelve
import org.jetbrains.idea.perforce.application.ShelvedChange
import java.util.function.Supplier
import javax.swing.Icon

open class UnshelveActionBase(val delete: Boolean,
                              dynamicText: Supplier<@NlsActions.ActionText String>,
                              dynamicDescription: Supplier<@NlsActions.ActionDescription String>,
                              icon: Icon?) : DumbAwareAction(dynamicText, dynamicDescription, icon) {

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
    val changes = e.getRequiredData(VcsDataKeys.CHANGES)

    ShelfUtils.unshelveChanges(changes.map { (it as ShelvedChange.IdeaChange).original }, e.project!!, delete)
  }
}

class UnshelveAction : UnshelveActionBase(false, PerforceBundle.messagePointer("shelf.unshelve"),
                                          PerforceBundle.messagePointer("shelf.unshelve.action.description"),
                                          null)

class UnshelveAndDeleteAction : UnshelveActionBase(true, PerforceBundle.messagePointer("shelf.unshelve.and.delete"),
                                                   PerforceBundle.messagePointer("shelf.unshelve.and.delete.action.description"),
                                                   AllIcons.Vcs.Unshelve)