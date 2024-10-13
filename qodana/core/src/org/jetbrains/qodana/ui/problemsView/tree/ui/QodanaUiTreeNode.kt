package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.analysis.problemsView.toolWindow.Node
import com.intellij.openapi.util.NlsActions
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNode

/**
 * UI tree nodes generally should access model nodes by requesting them via [primaryData].
 *
 * [modelTreeNode] is cached latest model tree node
 */
interface QodanaUiTreeNode<out ModelTreeNodeT : QodanaTreeNode<ModelTreeNodeT, *, PrimaryDataT>, out PrimaryDataT : QodanaTreeNode.PrimaryData> {
  val parent: QodanaUiTreeNode<*, *>?

  val primaryData: PrimaryDataT

  val modelTreeNode: ModelTreeNodeT?

  fun computeModelTreeNode(): ModelTreeNodeT?

  fun computeAncestorsAndThisModelNodes(): List<QodanaTreeNode<*, *, *>>

  fun getExcludeActionsDescriptors(): List<ExcludeActionDescriptor>

  class ExcludeActionDescriptor(
    val actionName: @NlsActions.ActionText String,
    val configExcludeItem: ConfigExcludeItem
  )

  fun asViewNode(): Node

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}