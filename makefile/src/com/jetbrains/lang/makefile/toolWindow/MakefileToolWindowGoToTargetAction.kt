package name.kropp.intellij.makefile.toolWindow

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import com.intellij.ui.treeStructure.*
import name.kropp.intellij.makefile.*

class MakefileToolWindowGoToTargetAction(private val tree: Tree, private val project: Project) : AnAction("Go to target", "Go to target", MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selectedNodes = tree.getSelectedNodes(MakefileTargetNode::class.java, {true})
    if (selectedNodes.any()) {
      val selected = selectedNodes.first()
      val elements = MakefileTargetIndex.get(selected.name, project, GlobalSearchScope.fileScope(selected.parent.psiFile))
      elements.first().navigate(true)
    }
  }
}