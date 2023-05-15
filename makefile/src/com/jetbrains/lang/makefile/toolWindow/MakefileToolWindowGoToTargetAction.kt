package com.jetbrains.lang.makefile.toolWindow

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.ui.treeStructure.*
import com.jetbrains.lang.makefile.*

class MakefileToolWindowGoToTargetAction(private val tree: Tree, private val project: Project)
  : AnAction(MakefileLangBundle.message("action.go.to.target.text"),
             MakefileLangBundle.message("action.go.to.target.description"),
             MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selectedNodes = tree.getSelectedNodes(MakefileTargetNode::class.java, {true})
    if (selectedNodes.any()) {
      val selected = selectedNodes.first()
      val elements = MakefileTargetIndex.getInstance().get(selected.name, project, GlobalSearchScope.fileScope(selected.parent.psiFile))
      elements.first().navigate(true)
    }
  }
}