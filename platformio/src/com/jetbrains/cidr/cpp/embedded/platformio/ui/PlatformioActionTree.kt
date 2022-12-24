package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.LINK_ATTRIBUTES
import com.intellij.ui.render.LabelBasedRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.asSafely
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.tree.TreeUtil
import com.jetbrains.cidr.cpp.embedded.platformio.*
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioExecutionTarget
import java.awt.Component
import java.awt.event.*
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.ToolTipManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

internal class PlatformioActionTree(private val project: Project, private val messageHolder: StatusText) : Tree(
  DefaultMutableTreeNode()), PlatformioUpdatesNotifier {

  private val envNode: DefaultMutableTreeNode

  init {
    project.messageBus.connect().subscribe(PLATFORMIO_UPDATES_TOPIC, this)
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

        treeCellRendererComponent.asSafely<JComponent>()?.toolTipText = action?.asSafely<PlatformioActionBase>()?.toolTip?.get()
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
        addNode(actionManager.getAction("Debug"))
        addNode(actionManager.getAction("Clean"))
      }

      addNode("PlatformIO").apply {
        addNode(actionManager.getAction("PlatformioHomeAction"))
        addNode(actionManager.getAction("PlatformioPkgUpdateAction"))
        addNode(actionManager.getAction("PlatformioMonitorAction"))
        addNode(actionManager.getAction("PlatformioCheckAction"))
      }

    }
    TreeUtil.expandAll(this)
  }

  private fun DefaultMutableTreeNode.addNode(childUserObject: Any): DefaultMutableTreeNode {
    val node = DefaultMutableTreeNode(childUserObject)
    this.add(node)
    return node
  }

  private fun DefaultMutableTreeNode.addEnvNodes(actionManager: ActionManager,
                                                 targets: List<PlatformioTargetData> = emptyList()): DefaultMutableTreeNode {
    //todo check nodes visibility
    val visibleActions = mutableSetOf<String>()
    targets.forEach { targetData ->

      if (targetData.name != "debug") {
        val id = "target-platformio-${targetData.name}"
        var action = actionManager.getAction(id) as PlatformioTargetAction?
        if (action == null) {
          @NlsSafe val toolTip = "pio run -t ${targetData.name}"
          @NlsSafe val text = if (!targetData.title.isNullOrEmpty()) targetData.title else toolTip
          action = PlatformioTargetAction(targetData.name, { text }, { toolTip })
          actionManager.registerAction(id, action)
        }
        addNode(action)
        visibleActions.add(targetData.name)
        if (targetData.name == "upload") {
          addNode(PlatformioUploadMonitorAction)
          visibleActions.add(PlatformioUploadMonitorAction.target)
        }
      }
      project.service<PlatformioService>().visibleActions = visibleActions
    }
    return this
  }

  override fun reparseFailed(pioStartFailed: Boolean) {
    fun tryReparseControl() =
      messageHolder.appendLine(ClionEmbeddedPlatformioBundle.message("parse.again"),
                               LINK_ATTRIBUTES) { _ ->
        val action = ActionManager.getInstance().getAction(PlatformioRefreshAction::class.java.name)
        ActionUtil.invokeAction(action, SimpleDataContext.getProjectContext(project), ActionPlaces.UNKNOWN, null, null)
      }

    if (pioStartFailed) {
      messageHolder.setText(ClionEmbeddedPlatformioBundle.message("status.text.pio.not.started"),
                            SimpleTextAttributes.DARK_TEXT)
      messageHolder.appendLine(ClionEmbeddedPlatformioBundle.message("install.guide"),
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


  override fun reparseStarted() {
    messageHolder.text = ClionEmbeddedPlatformioBundle.message("status.text.parsing")
    isVisible = false
  }

  override fun reparseSuccess() {
    isVisible = true
  }

  override fun targetsChanged(newTargets: List<PlatformioTargetData>) {
    envNode.userObject = targetName(ExecutionTargetManager.getActiveTarget(project))
    val envNodeWasEmpty = envNode.childCount == 0
    val actionManager = ActionManager.getInstance()
    envNode.removeAllChildren()
    envNode.addEnvNodes(actionManager, newTargets)
    (model as DefaultTreeModel).nodeStructureChanged(envNode)
    if (envNodeWasEmpty) {
      expandPath(TreePath(envNode.path))
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
      action.actionPerformed(AnActionEvent.createFromAnAction(action, e, ActionPlaces.UNKNOWN, dataContext))
    }
  }

}