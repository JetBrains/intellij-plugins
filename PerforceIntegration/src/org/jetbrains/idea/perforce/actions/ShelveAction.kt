package org.jetbrains.idea.perforce.actions

import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.AbstractVcsHelper
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.actions.VcsContext
import com.intellij.openapi.vcs.changes.*
import com.intellij.openapi.vcs.changes.actions.AbstractCommitChangesAction
import com.intellij.util.containers.MultiMap
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.*
import org.jetbrains.idea.perforce.operations.RefreshForVcs
import org.jetbrains.idea.perforce.perforce.P4File
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.connections.P4Connection
import javax.swing.JComponent

/**
 * @author peter
 */
class ShelveAction : AbstractCommitChangesAction() {

  private fun supportsShelve(project: Project, connection: P4Connection): Boolean {
    try {
      return PerforceManager.getInstance(project).getServerVersion(connection)?.supportsShelve() == true
    }
    catch (e: VcsException) {
      return false
    }
  }

  override fun update(vcsContext: VcsContext, presentation: Presentation) {
    val changes = vcsContext.selectedChanges
    val project = vcsContext.project
    if (changes == null || project == null) {
      presentation.isEnabledAndVisible = false
    }
    else {
      val connections = getConnections(project, changes.filterNot { it is ShelvedChange.IdeaChange }).keySet()
      presentation.isEnabledAndVisible = connections.isNotEmpty() && connections.all { supportsShelve(project, it) }
    }
  }

  override fun getExecutor(project: Project): CommitExecutor {
    return object : CommitExecutor {
      override fun getActionText(): String = PerforceBundle.message("shelve")

      override fun createCommitSession(commitContext: CommitContext): CommitSession {
        return object : CommitSession {
          override fun getAdditionalConfigurationUI(changes: Collection<Change>, commitMessage: String?): JComponent? = null
          override fun canExecute(changes: Collection<Change>, commitMessage: String): Boolean = true

          override fun execute(changes: Collection<Change>, commitMessage: String?) {
            shelveChanges(project, commitMessage, changes)
          }
        }
      }
    }
  }

  companion object {
    @VisibleForTesting @JvmStatic fun shelveChanges(project: Project, commitMessage: String?, changes: Collection<Change>) {
      for ((list, listChanges) in changes.groupBy { ChangeListManager.getInstance(project).getChangeList(it) }) {
        shelveChanges(project, commitMessage, listChanges, list)
      }
    }

    private fun shelveChanges(project: Project, commitMessage: String?, changes: Collection<Change>, changeList: LocalChangeList?) {
      val runner = PerforceRunner.getInstance(project)
      val connections = getConnections(project, changes)

      val addedChanges = changes.mapNotNull { change -> if (change.beforeRevision == null) change.afterRevision!!.file.ioFile else null }

      val refreshForVcs = RefreshForVcs()

      for ((connection, filePaths) in connections.entrySet()) {
        try {
          val existingList = when (commitMessage) {
            changeList?.name -> PerforceNumberNameSynchronizer.getInstance(project).getNumber(connection.connectionKey, commitMessage.orEmpty())
            else -> null
          }
          val list = existingList ?: runner.createChangeList(commitMessage, connection, null)

          val p4Paths = filePaths.map { path -> P4File.create(path).escapedPath }

          runner.reopen(connection, list, p4Paths)
          runner.shelve(connection, list, p4Paths)
          runner.revertAll(p4Paths, connection)

          for (file in filePaths.map { it.ioFile }) {
            if (addedChanges.contains(file)) {
              FileUtil.delete(file)
              refreshForVcs.addDeletedFile(file)
            }
            else {
              refreshForVcs.refreshFile(file)
            }
          }
        }
        catch (e: VcsException) {
          AbstractVcsHelper.getInstance(project).showError(e, PerforceBundle.message("shelve"))
        }

        refreshForVcs.run(project)
        VcsDirtyScopeManager.getInstance(project).filePathsDirty(filePaths, emptyList())
      }
    }

    private fun getConnections(project: Project, changes: Collection<Change>): MultiMap<P4Connection, FilePath> {
      return FileGrouper.distributePathsByConnection(changes.flatMapTo(linkedSetOf<FilePath>()) { change ->
        val beforeFile = change.beforeRevision?.file
        val afterFile = change.afterRevision?.file
        val file = afterFile ?: beforeFile!!
        when {
          VcsUtil.getVcsFor(project, file) is PerforceVcs -> listOfNotNull(beforeFile, afterFile)
          else -> emptyList()
        }
      }, project)
    }
  }
}
