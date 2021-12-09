package org.jetbrains.idea.perforce.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ChangesViewModifier
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNode
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserStringNode
import com.intellij.openapi.vcs.changes.ui.ChangesViewModelBuilder
import com.intellij.openapi.vcs.changes.ui.TreeModelBuilder
import com.intellij.util.ui.tree.TreeUtil
import org.jetbrains.idea.perforce.PerforceBundle

class PerforceShelveChangesViewModifier(private val project: Project) : ChangesViewModifier {
  override fun modifyTreeModelBuilder(builder: ChangesViewModelBuilder) {
    if (builder !is TreeModelBuilder) return
    val changeLists = ChangeListManager.getInstance(project).changeLists
    for (changeList in changeLists) {
      val shelvedChanges = PerforceManager.getInstance(project).shelf.getShelvedChanges(changeList).map { it.toIdeaChange(project) }
      if (shelvedChanges.isEmpty()) continue
      val changeListNode = TreeUtil.findNodeWithObject(builder.myRoot, changeList) as? ChangesBrowserNode<*> ?: continue
      val shelvedChangesRoot = ChangesBrowserStringNode(PerforceBundle.message("shelf.changes.node.title"))
      builder.insertSubtreeRoot(shelvedChangesRoot, changeListNode)
      builder.insertChanges(shelvedChanges, shelvedChangesRoot)
    }
  }
}