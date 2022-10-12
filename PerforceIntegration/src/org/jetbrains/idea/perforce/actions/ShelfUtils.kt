package org.jetbrains.idea.perforce.actions

import com.google.common.annotations.VisibleForTesting
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vcs.AbstractVcsHelper
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vcs.changes.ui.SimpleChangesBrowser
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.MultiMap
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.ShelvedChange
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider
import org.jetbrains.idea.perforce.perforce.PerforceRunner
import org.jetbrains.idea.perforce.perforce.connections.P4Connection

object ShelfUtils {

  private val LOG = Logger.getInstance(ShelfUtils::class.java)

  @JvmStatic
  fun browseShelf(project: Project, shelvedChanges: List<ShelvedChange>) {
    val changes = shelvedChanges.map { it.toIdeaChange(project) }

    val dialogBuilder = DialogBuilder(project)
    val browser = SimpleChangesBrowser(project, true, false)
    browser.setInclusionChangedListener { dialogBuilder.setOkActionEnabled(browser.includedChanges.isNotEmpty()) }

    browser.setChangesToDisplay(changes)
    browser.setIncludedChanges(changes)
    browser.addToolbarAction(object : DumbAwareAction(PerforceBundle.messagePointer("shelf.unshelve"),
                                                      PerforceBundle.messagePointer("shelf.unshelve.action.description"),
                                                      AllIcons.Vcs.Unshelve) {
      override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = browser.includedChanges.isNotEmpty()
      }

      override fun actionPerformed(e: AnActionEvent) {
        FileDocumentManager.getInstance().saveAllDocuments() // to ensure Perforce will see up-to-date file content

        unshelveChanges(browser.includedChanges.map { (it as ShelvedChange.IdeaChange).original }, project, false)
      }
    })

    dialogBuilder.setTitle(PerforceBundle.message("shelf.changes"))
    dialogBuilder.setActionDescriptors(DialogBuilder.OkActionDescriptor(), DialogBuilder.CloseDialogAction())
    dialogBuilder.okAction.setText(PerforceBundle.message("shelf.unshelve.and.delete"))
    dialogBuilder.setOkOperation {
      FileDocumentManager.getInstance().saveAllDocuments() // to ensure Perforce will see up-to-date file content
      val selected = browser.includedChanges
      if (selected.isNotEmpty()) {
        unshelveChanges(selected.map { (it as ShelvedChange.IdeaChange).original }, project, true)
        dialogBuilder.dialogWrapper.close(DialogWrapper.OK_EXIT_CODE)
      }
    }
    dialogBuilder.setCenterPanel(browser)
    dialogBuilder.setPreferredFocusComponent(browser.preferredFocusedComponent)
    dialogBuilder.showNotModal()
  }

  @JvmStatic
  fun groupByChangeList(selected: List<ShelvedChange>): MultiMap<Pair<P4Connection, Long>, String> {
    val paths = MultiMap.create<Pair<P4Connection, Long>, String>()
    for (original in selected) {
      paths.putValue(original.connection to original.changeList, original.depotPath)
    }
    return paths
  }

  @VisibleForTesting
  @JvmStatic
  fun unshelveChanges(selected: List<ShelvedChange>, project: Project, delete: Boolean) {
    val paths = groupByChangeList(selected)

    doUnshelve(paths, project)

    if (delete) {
      deleteFromShelf(paths, project)
    }

    val dirtyFiles = selected.mapNotNull { it.file?.let { file -> VcsUtil.getFilePath(file) } }

    dirtyFiles.forEach {
      val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(it.path)
      if (file != null) {
        VfsUtil.markDirtyAndRefresh(false, false, false, file)
      }
    }

    VcsDirtyScopeManager.getInstance(project).filePathsDirty(dirtyFiles, emptyList())
  }

  private fun doUnshelve(paths: MultiMap<Pair<P4Connection, Long>, String>, project: Project) {
    for ((pair, specs) in paths.entrySet()) {
      val (connection, changeList) = pair
      try {
        PerforceRunner.getInstance(project).unshelve(connection, changeList, specs)
      }
      catch (e: VcsException) {
        AbstractVcsHelper.getInstance(project).showError(e, PerforceBundle.message("shelf.unshelve"))
      }
    }
  }

  private fun handleUnshelveException(connection: P4Connection,
                                      unshelveException: VcsException,
                                      project: Project,
                                      specs: Collection<String>?) {
    val msg = unshelveException.message
    if ("needs resolve" in msg) {
      val toResolve: LinkedHashSet<VirtualFile>
      try {
        toResolve = PerforceRunner.getInstance(project).getResolvedWithConflicts(connection, specs)
      }
      catch (resolveException: VcsException) {
        AbstractVcsHelper.getInstance(project).showError(unshelveException, PerforceBundle.message("shelf.unshelve"))
        AbstractVcsHelper.getInstance(project).showError(resolveException, PerforceBundle.message("shelf.unshelve"))
        return
      }

      if (toResolve.isNotEmpty()) {
        VfsUtil.markDirtyAndRefresh(false, false, false, *toResolve.toTypedArray())
        LOG.info(unshelveException)
        PerforceMergeProvider(project).showMergeDialog(toResolve.toList())
        return
      }
    }

    AbstractVcsHelper.getInstance(project).showError(unshelveException, PerforceBundle.message("shelf.unshelve"))
  }

  @JvmStatic
  fun deleteFromShelf(paths: MultiMap<Pair<P4Connection, Long>, String>, project: Project) {
    for ((pair, specs) in paths.entrySet()) {
      val (connection, changeList) = pair
      try {
        PerforceRunner.getInstance(project).deleteFromShelf(connection, changeList, specs)
      }
      catch (e: VcsException) {
        handleUnshelveException(connection, e, project, specs)
      }
    }
  }

  @JvmStatic
  fun deleteFromShelf(changes: List<ShelvedChange>, project: Project) {
    deleteFromShelf(groupByChangeList(changes), project)
  }
}
