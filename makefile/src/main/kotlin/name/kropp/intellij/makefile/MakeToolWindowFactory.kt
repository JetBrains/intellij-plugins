package name.kropp.intellij.makefile

import com.intellij.execution.*
import com.intellij.execution.actions.*
import com.intellij.execution.impl.*
import com.intellij.execution.runners.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.wm.*
import com.intellij.ui.*
import com.intellij.ui.treeStructure.*
import com.intellij.util.enumeration.*
import name.kropp.intellij.makefile.psi.*
import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.tree.*

class MakeToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    toolWindow.title = "make"

    val files = MakefileTargetIndex.allTargets(project).filterNot { it.isSpecialTarget || it.isPatternTarget }.groupBy {
      it.containingFile
    }.map {
      MakefileFileNode(it.key.name, it.value.map { MakefileTargetNode(it) }.toTypedArray())
    }

    val model = DefaultTreeModel(MakefileRootNode(files.toTypedArray()))

    val panel = SimpleToolWindowPanel(true)

    val tree = Tree(model).apply {
      cellRenderer = MakefileCellRenderer()
      selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    }
    panel.add(ScrollPaneFactory.createScrollPane(tree))

    val toolBarPanel = JPanel(GridLayout())

    val actionGroup = DefaultActionGroup()
    val runManager = RunManagerImpl.getInstanceImpl(project)
    val runTargetAction = MakefileRunTargetAction2(tree, runManager)
    runTargetAction.registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_ENTER), panel)
    actionGroup.add(runTargetAction)
    toolBarPanel.add(ActionManager.getInstance().createActionToolbar("MakeToolWindowToolbar", actionGroup, true).component)

    panel.setToolbar(toolBarPanel)

    toolWindow.component.add(panel)
  }
}


class MakefileRunTargetAction2(private val tree: Tree, private val runManager: RunManagerImpl) : AnAction("Run target", "Run target", MakefileTargetIcon) {
  override fun actionPerformed(event: AnActionEvent) {
    val selected = tree.getSelectedNodes(MakefileTargetNode::class.java, {true})
    if (selected.any()) {
      val target = selected.first().target

      val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY.name, PsiLocation(target), event.dataContext)

      val context = ConfigurationContext.getFromContext(dataContext)

      val producer = MakefileRunConfigurationFactory(MakefileRunConfigurationType())
      val configuration = RunnerAndConfigurationSettingsImpl(runManager, producer.createConfigurationFromTarget(target) ?: return)

      (context.runManager as RunManagerEx).setTemporaryConfiguration(configuration)
      ExecutionUtil.runConfiguration(configuration, ExecutorRegistry.getInstance().registeredExecutors.first())
    }
  }
}
class MakefileCellRenderer : ColoredTreeCellRenderer() {
  override fun customizeCellRenderer(tree: JTree, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
    value as MakefileTreeNode
    icon = value.icon
    append(value.name)
  }
}


abstract class MakefileTreeNode(val name: String) : TreeNode {
  abstract val icon: Icon
}

class MakefileRootNode(private val files: Array<MakefileFileNode>) : MakefileTreeNode("make") {
  init {
    for (file in files) {
      file.parent = this
    }
  }

  override val icon: Icon
    get() = MakefileIcon

  override fun children() = ArrayEnumeration(files)

  override fun isLeaf() = false

  override fun getChildCount() = files.size

  override fun getParent() = null

  override fun getChildAt(i: Int) = files[i]

  override fun getIndex(node: TreeNode) = files.indexOf(node)

  override fun getAllowsChildren() = true
}

class MakefileFileNode(name: String, private val targets: Array<MakefileTargetNode>) : MakefileTreeNode(name) {
  init {
    for (target in targets) {
      target.parent = this
    }
  }

  internal lateinit var parent: MakefileRootNode

  override val icon: Icon
    get() = MakefileIcon

  override fun children(): Enumeration<*> = ArrayEnumeration(targets)

  override fun isLeaf() = false

  override fun getChildCount() = targets.size

  override fun getParent() = parent

  override fun getChildAt(i: Int) = targets[i]

  override fun getIndex(node: TreeNode) = targets.indexOf(node)

  override fun getAllowsChildren() = true
}

class MakefileTargetNode(val target: MakefileTarget) : MakefileTreeNode(target.name ?: "") {
  override val icon: Icon
    get() = MakefileTargetIcon

  internal lateinit var parent: MakefileFileNode

  override fun children() = EmptyEnumeration.INSTANCE

  override fun isLeaf() = true

  override fun getChildCount() = 0

  override fun getParent() = parent

  override fun getChildAt(i: Int) = null

  override fun getIndex(node: TreeNode) = 0

  override fun getAllowsChildren() = false
}
