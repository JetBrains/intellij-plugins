package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.AnActionEvent.createEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.LINK_ATTRIBUTES
import com.intellij.ui.render.LabelBasedRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.application
import com.intellij.util.asSafely
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.tree.TreeUtil
import com.jetbrains.cidr.cpp.embedded.platformio.*
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioExecutionTarget
import icons.ClionEmbeddedPlatformioIcons
import java.awt.Component
import java.awt.event.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

internal class PlatformioActionTree(private val project: Project, private val messageHolder: StatusText) : Tree(
  DefaultMutableTreeNode()), PlatformioUpdatesNotifier {

  private val envNode: DefaultMutableTreeNode

  init {
    isRootVisible = false
    setExpandableItemsEnabled(false)
    setCellRenderer(object : LabelBasedRenderer.Tree() {
      override fun getTreeCellRendererComponent(tree: JTree, value: Any?,
                                                selected: Boolean, expanded: Boolean,
                                                leaf: Boolean, row: Int, focused: Boolean): Component {
        val action = value.asSafely<DefaultMutableTreeNode>()?.userObject.asSafely<AnAction>()
        val labelText = action?.templatePresentation?.text ?: value.toString()
        val treeCellRendererComponent = super.getTreeCellRendererComponent(tree, labelText, selected, expanded, leaf, row, focused)
        icon = action?.templatePresentation?.icon ?: AllIcons.Nodes.EmptyNode

        @Suppress("HardCodedStringLiteral")
        treeCellRendererComponent.asSafely<JComponent>()?.toolTipText = action?.asSafely<PlatformioActionBase>()?.toolTip?.invoke()
        return treeCellRendererComponent
      }
    })
    ToolTipManager.sharedInstance().registerComponent(this)
    addKeyListener(object : KeyAdapter() {
      override fun keyPressed(e: KeyEvent) {
        if (e.keyCode == KeyEvent.VK_ENTER) {
          runAction(e)
        }
      }
    })
    addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent) {
        if (e.clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
          runAction(e)
        }
      }
    })
    val actionManager = ActionManager.getInstance()
    with(model.root as DefaultMutableTreeNode)
    {
      envNode = addNode(targetName(null))
      addNode(ClionEmbeddedPlatformioBundle.message("project.action.folder")).apply {
        addNode(actionManager.getAction("Build"))
        addNode(actionManager.getAction(DefaultDebugExecutor.EXECUTOR_ID))
        addNode(actionManager.getAction("Clean").withIcon(ClionEmbeddedPlatformioIcons.CleanPlatformIO))
      }

      addNode("PlatformIO").apply {
        addNode(actionManager.getAction("PlatformioHomeAction"))
        addNode(actionManager.getAction("PlatformioPkgUpdateAction"))
        addNode(actionManager.getAction("PlatformioMonitorAction"))
        addNode(actionManager.getAction("PlatformioCheckAction"))
      }

    }
    TreeUtil.expandAll(this)
    project.messageBus.connect().subscribe(PLATFORMIO_UPDATES_TOPIC, this)
    targetsChanged()
    projectStateChanged()
  }

  private fun DefaultMutableTreeNode.addNode(childUserObject: Any): DefaultMutableTreeNode {
    val node = DefaultMutableTreeNode(childUserObject)
    this.add(node)
    return node
  }

  private fun AnAction.withIcon(icon: Icon): AnAction {
    val presentation = this.getTemplatePresentation()
    presentation.setIcon(icon)
    return this
  }

  private fun reparseFailed(pioStartFailed: Boolean) {
    fun tryReparseControl() =
      messageHolder.appendLine(ClionEmbeddedPlatformioBundle.message("parse.again"), LINK_ATTRIBUTES) {
        invokeProjectRefreshAction()
      }

    if (pioStartFailed) {
      messageHolder.setText(ClionEmbeddedPlatformioBundle.message("status.text.pio.not.started"),
                            SimpleTextAttributes.DARK_TEXT)
      messageHolder.appendLine(AllIcons.General.ContextHelp, ClionEmbeddedPlatformioBundle.message("install.guide"),
                               LINK_ATTRIBUTES, OpenInstallGuide)
      messageHolder.appendLine(ClionEmbeddedPlatformioBundle.message("open.settings.link"),
                               LINK_ATTRIBUTES, OpenSettings(project))
      tryReparseControl()
    }
    else {
      messageHolder.setText(ClionEmbeddedPlatformioBundle.message("status.text.project.parse.failed"),
                            SimpleTextAttributes.DARK_TEXT)
      tryReparseControl()
    }
    isVisible = false
  }

  private fun notTrusted() {
    messageHolder.setText(ClionEmbeddedPlatformioBundle.message("status.text.untrusted.project.primary.text"), SimpleTextAttributes.DARK_TEXT)
    messageHolder.appendSecondaryText(ClionEmbeddedPlatformioBundle.message("status.text.untrusted.project.link"), LINK_ATTRIBUTES) {
      // No need to show the dialog here, the action should show it when invoked;
      // we want to refresh the project when the project becomes trusted anyway.
      invokeProjectRefreshAction()
    }
    @Suppress("DialogTitleCapitalization")
    messageHolder.appendSecondaryText(ClionEmbeddedPlatformioBundle.message("status.text.untrusted.project.secondary.text"), SimpleTextAttributes.DARK_TEXT, null)
  }

  private fun invokeProjectRefreshAction() {
    val action = ActionManager.getInstance().getAction(PlatformioRefreshAction::class.java.simpleName)
    ActionUtil.invokeAction(action, createEvent(SimpleDataContext.getProjectContext(project), null, ActionPlaces.UNKNOWN, ActionUiKind.NONE, null), null)
  }

  override fun projectStateChanged() {
    when (project.service<PlatformioService>().projectStatus) {
      PlatformioProjectStatus.PARSED -> isVisible = true
      PlatformioProjectStatus.PARSE_FAILED -> reparseFailed(false)
      PlatformioProjectStatus.UTILITY_FAILED -> reparseFailed(true)
      PlatformioProjectStatus.NOT_TRUSTED -> notTrusted()
      else -> {
        messageHolder.text = ClionEmbeddedPlatformioBundle.message("status.text.parsing")
        isVisible = false
      }
    }
  }

  override fun targetsChanged() {
    application.invokeLater {
      if (!project.isDisposed) {
        envNode.userObject = targetName(ExecutionTargetManager.getActiveTarget(project))
        val envNodeWasEmpty = envNode.childCount == 0
        val actionManager = ActionManager.getInstance()
        envNode.removeAllChildren()
        project.service<PlatformioService>()
          .getActiveActionIds()
          .forEach { envNode.addNode(actionManager.getAction(it)) }
        (model as DefaultTreeModel).nodeStructureChanged(envNode)
        if (envNodeWasEmpty) {
          expandPath(TreePath(envNode.path))
        }
      }
    }
  }

  private fun targetName(target: ExecutionTarget?): String {
    if (target is PlatformioExecutionTarget) return ClionEmbeddedPlatformioBundle.message("action.tree.env.name", target.displayName)
    return ClionEmbeddedPlatformioBundle.message("action.tree.unknown.env.name")
  }

  private fun runAction(e: InputEvent) {
    val action = selectionPath?.lastPathComponent?.asSafely<DefaultMutableTreeNode>()?.userObject?.asSafely<AnAction>()
    if (action != null) {
      val dataContext = SimpleDataContext.getProjectContext(project)
      val actionEvent = createEvent(dataContext, null, ActionPlaces.TOOLBAR, ActionUiKind.TOOLBAR, e)
      ActionUtil.invokeAction(action, actionEvent, null)
    }
  }

}