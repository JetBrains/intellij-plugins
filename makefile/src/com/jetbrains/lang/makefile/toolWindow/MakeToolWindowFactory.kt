package com.jetbrains.lang.makefile.toolWindow

import com.intellij.execution.impl.RunManagerImpl
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.AutoScrollToSourceHandler
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.content.impl.ContentImpl
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import com.jetbrains.lang.makefile.MakefileLangBundle
import com.jetbrains.lang.makefile.MakefileTargetIndex
import java.awt.GridLayout
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.BUTTON1
import javax.swing.JPanel
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

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

      val tree = object : Tree(model), UiDataProvider {
        override fun uiDataSnapshot(sink: DataSink) {
          val selectedNodes = getSelectedNodes(MakefileTargetNode::class.java) { true }
          val selected = selectedNodes.firstOrNull() ?: return
          sink.lazy(PSI_ELEMENT) {
            val psi = selected.parent.psiFile ?: return@lazy null
            MakefileTargetIndex.getInstance().getTargets(
              selected.name, project, GlobalSearchScope.fileScope(psi))
              .firstOrNull()
          }
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

      val refreshTargets = MakefileToolWindowRefreshAction(model, options)
      refreshTargets.registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_F5), panel)

      val group = DefaultActionGroup()
      group.add(runTargetAction)
      group.add(refreshTargets )
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
