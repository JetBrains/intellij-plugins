package com.jetbrains.lang.makefile.toolWindow

import com.intellij.execution.impl.*
import com.intellij.ide.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.CommonDataKeys.*
import com.intellij.openapi.project.*
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.*
import com.intellij.openapi.wm.*
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.psi.search.*
import com.intellij.ui.*
import com.intellij.ui.content.impl.*
import com.intellij.ui.treeStructure.*
import com.intellij.util.ui.tree.*
import com.jetbrains.lang.makefile.*
import com.jetbrains.lang.makefile.psi.MakefileTarget
import java.awt.*
import java.awt.event.*
import java.awt.event.MouseEvent.*
import javax.swing.*
import javax.swing.tree.*

private const val TOOLWINDOW_ID = "make" // the ID is unfortunate, but should be kept compatible with older versions

class MakeToolWindowFactory : ToolWindowFactory {
  override fun init(toolWindow: ToolWindow) {
    val project = (toolWindow as? ToolWindowEx)?.project ?: return

    StartupManager.getInstance(project).runAfterOpened {
      val manager = ToolWindowManager.getInstance(project)
      updateStripeButton(project, manager, toolWindow)
    }
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    @Suppress("DialogTitleCapitalization")
    toolWindow.title = MakefileLangBundle.message("tool.window.title")

    val contentManager = toolWindow.contentManager

    val options = MakefileToolWindowOptions(project)

    DumbService.getInstance(project).runWhenSmart {
      val model = DefaultTreeModel(options.getRootNode())

      val panel = SimpleToolWindowPanel(true)

      val tree = object : Tree(model), DataProvider {
        override fun getData(dataId: String): Any? {
          if (PlatformCoreDataKeys.BGT_DATA_PROVIDER.`is`(dataId)) {
            val selectedNodes = getSelectedNodes(MakefileTargetNode::class.java, {true})
            return DataProvider { slowData(it, selectedNodes) }
          }
          return null
        }

        private fun slowData(dataId: String, selectedNodes: Array<MakefileTargetNode>): MakefileTarget? {
          if (PSI_ELEMENT.`is`(dataId)) {
            if (selectedNodes.any()) {
              val selected = selectedNodes.first()
              return MakefileTargetIndex.getInstance().getTargets(selected.name, project,
                                                                          GlobalSearchScope.fileScope(
                                                                            selected.parent.psiFile)).firstOrNull()
            }
          }
          return null
        }

      }.apply {
        cellRenderer = MakefileCellRenderer(project)
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        isRootVisible = false
        showsRootHandles = true
      }
      TreeUtil.installActions(tree)
      TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
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

      val toolbar = ActionManager.getInstance().createActionToolbar("MakeToolWindowToolbar", group, true)
      toolbar.setTargetComponent(tree)
      toolBarPanel.add(toolbar.component)

      panel.toolbar = toolBarPanel

      contentManager.addContent(ContentImpl(panel, "", true))
    }
  }

  companion object {
    fun updateStripeButton(project: Project) {
      val manager = ToolWindowManager.getInstance(project)
      val toolWindow = getToolWindow(manager) ?: return
      updateStripeButton(project, manager, toolWindow)
    }

    private fun updateStripeButton(project: Project, manager: ToolWindowManager, toolWindow: ToolWindow) {
      manager.invokeLater {
        if (shouldDisableStripeButton(project, manager)) {
          toolWindow.isShowStripeButton = false
        }
      }
    }

    private fun getToolWindow(manager: ToolWindowManager): ToolWindowEx? {
      return manager.getToolWindow(TOOLWINDOW_ID) as? ToolWindowEx
    }

    private fun shouldDisableStripeButton(project: Project, manager: ToolWindowManager): Boolean {
      val windowInfo = (manager as ToolWindowManagerEx).getLayout().getInfo(TOOLWINDOW_ID)
      // toolwindow existed in ths project before - show it
      if (windowInfo != null && windowInfo.isFromPersistentSettings) {
        return false
      }

      // any extension reported that it's desired to hide it by default (i.e. it's CLion's non-Makefile project) - hide it
      if (MakefileToolWindowStripeController.EP_NAME.extensionList.any { it.shouldHideStripeIconFor(project) }) {
        return true
      }

      // show it otherwise
      return false
    }
  }
}
