package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.analysis.problemsView.toolWindow.Node
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.model.openFileDescriptor
import java.nio.file.Path

abstract class QodanaUiTreeNodeBase<ModelTreeNodeT : QodanaTreeNode<ModelTreeNodeT, *, PrimaryDataT>, PrimaryDataT : QodanaTreeNode.PrimaryData>(
  override val parent: QodanaUiTreeNode<*, *>,
  override val primaryData: PrimaryDataT
) : Node(parent.asViewNode()), QodanaUiTreeNode<ModelTreeNodeT, PrimaryDataT> {
  private var _modelTreeNode: ModelTreeNodeT? = null

  override val modelTreeNode: ModelTreeNodeT?
    get() = _modelTreeNode

  override val descriptor: OpenFileDescriptor?
    get() {
      val project = project ?: return null
      return modelTreeNode?.openFileDescriptor(project)
    }

  override fun asViewNode(): Node = this

  override fun update(project: Project, presentation: PresentationData) {
    val newModelTreeNode = computeModelTreeNode()
    _modelTreeNode = newModelTreeNode
    if (newModelTreeNode != null) {
      doUpdate(project, presentation, newModelTreeNode)
    }
  }

  override fun getChildren(): Collection<Node> {
    return computeModelTreeNode()?.children?.nodesSequence?.mapNotNull { it.toUiNode(this)?.asViewNode() }?.toList() ?: emptyList()
  }

  abstract fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: ModelTreeNodeT)

  override fun computeAncestorsAndThisModelNodes(): List<QodanaTreeNode<*, *, *>> {
    val modelNodesToParent = parent.computeAncestorsAndThisModelNodes()
    val parentModelNode = modelNodesToParent.lastOrNull() ?: return emptyList()

    val thisModelNode = parentModelNode.children.nodeByPrimaryDataFinder.invoke(primaryData) ?: return emptyList()
    return modelNodesToParent + listOf(thisModelNode)
  }

  protected inline fun <reified T : QodanaTreeNode<*, *, *>> computeModelTreeNodeThroughParent(): T? {
    return computeModelTreeNodeThroughParentViaClazz(T::class.java)
  }

  protected fun <T : QodanaTreeNode<*, *, *>> computeModelTreeNodeThroughParentViaClazz(clazz: Class<T>): T? {
    val modelTreeNodeParent = parent.computeModelTreeNode() ?: return null
    val matchingChildNode = modelTreeNodeParent.children.nodeByPrimaryDataFinder.invoke(primaryData) ?: return null

    return if (clazz.isInstance(matchingChildNode)) clazz.cast(matchingChildNode) else null
  }

  override fun hashCode(): Int = this.primaryData.hashCode()

  override fun equals(other: Any?): Boolean {
    if (other !is QodanaUiTreeNodeBase<*, *>) return false
    return this === other || (this.primaryData == other.primaryData && this.parent == other.parent)
  }
}

fun QodanaUiTreeNode<*, *>.excludeActionsDescriptorsForPathNode(
  path: Path,
  pathOnlyTextProvider: () -> @NlsActions.ActionText String,
  pathWithInspectionTextProvider: (inspectionId: String) -> @NlsActions.ActionText String
): List<QodanaUiTreeNode.ExcludeActionDescriptor> {
  if (modelTreeNode?.excluded != false) return emptyList()

  val pathString = path.toString()
  val inspectionNodeAncestor = findUiTreeInspectionNodeAncestor()

  return listOfNotNull(
    QodanaUiTreeNode.ExcludeActionDescriptor(
      actionName = pathOnlyTextProvider.invoke(),
      ConfigExcludeItem(inspectionId = null, pathString)
    ),
    inspectionNodeAncestor?.let {
      val inspectionId = it.primaryData.inspectionId
      QodanaUiTreeNode.ExcludeActionDescriptor(
        actionName = pathWithInspectionTextProvider.invoke(inspectionId),
        ConfigExcludeItem(inspectionId, pathString)
      )
    }
  )
}

private fun QodanaUiTreeNode<*, *>.findUiTreeInspectionNodeAncestor(): QodanaUiTreeInspectionNode? {
  return generateSequence(parent) { it.parent }
    .filterIsInstance<QodanaUiTreeInspectionNode>()
    .firstOrNull()
}