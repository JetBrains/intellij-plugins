package name.kropp.intellij.makefile.toolWindow

import com.intellij.execution.impl.*
import com.intellij.ide.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.wm.*
import com.intellij.ui.*
import com.intellij.ui.treeStructure.*
import java.awt.*
import java.awt.event.*
import java.awt.event.MouseEvent.*
import javax.swing.*
import javax.swing.tree.*

class MakeToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.title = "make"
    toolWindow.isAutoHide = true

    val options = MakefileToolWindowOptions(project)

    DumbService.getInstance(project).runWhenSmart {
      val model = DefaultTreeModel(options.getRootNode())

      val panel = SimpleToolWindowPanel(true)

      val tree = Tree(model).apply {
        cellRenderer = MakefileCellRenderer()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
      }
      panel.add(ScrollPaneFactory.createScrollPane(tree))

      val toolBarPanel = JPanel(GridLayout())

      val runManager = RunManagerImpl.getInstanceImpl(project)

      val runTargetAction = MakefileToolWindowRunTargetAction(tree, project, runManager)
      runTargetAction.registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_ENTER), panel)
      tree.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
          if (e?.clickCount == 2 && e.button == BUTTON1) {
            ActionManager.getInstance().tryToExecute(runTargetAction, e, tree, "", true)
          }
        }
      })

      val goToTargetAction = MakefileToolWindowGoToTargetAction(tree, project)
      goToTargetAction.registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_F4), panel)

      val group = DefaultActionGroup()
      group.add(runTargetAction)
      group.addSeparator()
      val treeExpander = DefaultTreeExpander(tree)
      group.add(CommonActionsManager.getInstance().createExpandAllAction(treeExpander, tree))
      group.add(CommonActionsManager.getInstance().createCollapseAllAction(treeExpander, tree))
      group.addSeparator()
      group.addAction(MakefileToolWindowShowSpecialAction(options, model))

      toolBarPanel.add(ActionManager.getInstance().createActionToolbar("MakeToolWindowToolbar", group, true).component)

      panel.toolbar = toolBarPanel

      toolWindow.component.add(panel)
    }
  }
}
