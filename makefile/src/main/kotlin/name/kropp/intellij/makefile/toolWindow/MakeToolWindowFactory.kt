package name.kropp.intellij.makefile.toolWindow

import com.intellij.execution.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.wm.*
import com.intellij.ui.*
import com.intellij.ui.treeStructure.*
import name.kropp.intellij.makefile.*
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.tree.*

class MakeToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.title = "make"
    toolWindow.isAutoHide = true

    DumbService.getInstance(project).runWhenSmart {
      val files = MakefileTargetIndex.allTargets(project).filterNot { it.isSpecialTarget || it.isPatternTarget }.groupBy {
        it.containingFile
      }.map {
        MakefileFileNode(it.key, it.value.map(::MakefileTargetNode))
      }

      val model = DefaultTreeModel(MakefileRootNode(files))

      val panel = SimpleToolWindowPanel(true)

      val tree = Tree(model).apply {
        cellRenderer = MakefileCellRenderer()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
      }
      panel.add(ScrollPaneFactory.createScrollPane(tree))

      val toolBarPanel = JPanel(GridLayout())

      val actionGroup = DefaultActionGroup()
      val runManager = RunManagerImpl.getInstanceImpl(project)

      val runTargetAction = MakefileToolWindowRunTargetAction(tree, project, runManager)
      runTargetAction.registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_ENTER), panel)

      val goToTargetAction = MakefileToolWindowGoToTargetAction(tree, project)
      goToTargetAction.registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_F4), panel)

      actionGroup.add(runTargetAction)
      toolBarPanel.add(ActionManager.getInstance().createActionToolbar("MakeToolWindowToolbar", actionGroup, true).component)

      panel.setToolbar(toolBarPanel)

      toolWindow.component.add(panel)
    }
  }
}
