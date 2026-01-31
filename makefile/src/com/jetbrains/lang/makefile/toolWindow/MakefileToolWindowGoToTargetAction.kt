package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.treeStructure.Tree
import com.jetbrains.lang.makefile.MakefileLangBundle
import com.jetbrains.lang.makefile.MakefileTargetIcon
import com.jetbrains.lang.makefile.MakefileTargetIndex

class MakefileToolWindowGoToTargetAction(private val tree: Tree, private val project: Project)
  : AnAction(MakefileLangBundle.message("action.go.to.target.text"),
             MakefileLangBundle.message("action.go.to.target.description"),
             MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selectedNodes = tree.getSelectedNodes(MakefileTargetNode::class.java) { true }
    if (selectedNodes.any()) {
      val selected = selectedNodes.first()
      if (selected.parent.psiFile == null) return
      val elements = MakefileTargetIndex.getInstance().getTargets(selected.name, project,
                                                                          GlobalSearchScope.fileScope(selected.parent.psiFile!!))
      elements.firstOrNull()?.navigate(true)
    }
  }
}