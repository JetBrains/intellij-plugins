package name.kropp.intellij.makefile.toolWindow

import com.intellij.execution.impl.*
import com.intellij.ide.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.CommonDataKeys.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.wm.*
import com.intellij.psi.search.*
import com.intellij.ui.*
import com.intellij.ui.content.impl.*
import com.intellij.ui.treeStructure.*
import com.intellij.util.ui.tree.*
import name.kropp.intellij.makefile.*
import java.awt.*
import java.awt.event.*
import java.awt.event.MouseEvent.*
import javax.swing.*
import javax.swing.tree.*

class MakeToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.title = "make"

    val contentManager = toolWindow.contentManager

    val options = MakefileToolWindowOptions(project)

    DumbService.getInstance(project).runWhenSmart {
      val model = DefaultTreeModel(options.getRootNode())

      val panel = SimpleToolWindowPanel(true)

      val tree = object : Tree(model), DataProvider {
        override fun getData(dataId: String): Any? {
          if (PSI_ELEMENT.`is`(dataId)) {
            val selectedNodes = getSelectedNodes(MakefileTargetNode::class.java, {true})
            if (selectedNodes.any()) {
              val selected = selectedNodes.first()
              return MakefileTargetIndex.get(selected.name, project, GlobalSearchScope.fileScope(selected.parent.psiFile)).firstOrNull()
            }
          }
          return null
        }
      }.apply {
        cellRenderer = MakefileCellRenderer()
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isRootVisible = false
        showsRootHandles = true
      }
      TreeUtil.installActions(tree)
      TreeSpeedSearch(tree)
      panel.add(ScrollPaneFactory.createScrollPane(tree))

      val toolBarPanel = JPanel(GridLayout())

      val runManager = RunManagerImpl.getInstanceImpl(project)

      val autoScrollHandler = object : AutoScrollToSourceHandler() {
        override fun isAutoScrollMode(): Boolean = options.autoScrollToSource
        override fun setAutoScrollMode(state: Boolean) {
          options.autoScrollToSource = state
        }
      }
      autoScrollHandler.install(tree)

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
      group.addAction(MakefileToolWindowAutoscrollToSourceAction(options, autoScrollHandler, tree))
      group.addSeparator()
      group.addAction(MakefileToolWindowSortAlphabeticallyAction(options, model))
      group.addAction(MakefileToolWindowShowSpecialAction(options, model))

      toolBarPanel.add(ActionManager.getInstance().createActionToolbar("MakeToolWindowToolbar", group, true).component)

      panel.toolbar = toolBarPanel

      contentManager.addContent(ContentImpl(panel, "", true))
    }
  }
}
