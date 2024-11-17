package org.jetbrains.qodana.ui.problemsView.tree.model

import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNode.Children
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode

/**
 * Node in qodana problems tree model
 *
 * - [children]: Children nodes as a separate class, this allows to define multiple possible children nodes types.
 *   (when different grouping is applied, type of children can change, example:
 *   with grouping by directory – [Children] with directory or file nodes, without grouping – [Children] only with file nodes)
 *
 * - [primaryData]: Identifies node across its current siblings in tree,
 *   i.e. parent can't have multiple children nodes with same [primaryData].
 *   Shouldn't be changed on some minor changes (like problems counter update or change in some representation data).
 *   UI tree nodes store [primaryData] and search for actual model nodes ([QodanaTreeNode]) using this [primaryData] as a key
 *
 * See [QodanaTreeRoot]
 */
interface QodanaTreeNode<out T : QodanaTreeNode<T, ChildrenT, PrimaryDataT>, out ChildrenT : Children<ChildrenT>, out PrimaryDataT : QodanaTreeNode.PrimaryData> {
  interface PrimaryData {
    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean
  }

  interface Children<out T : Children<T>> {
    val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>

    /**
     * Used by UI tree nodes to find child model node
     */
    val nodeByPrimaryDataFinder: (PrimaryData) -> QodanaTreeNode<*, *, *>?

    /**
     * Return `null` if node's children shouldn't be changed
     *
     * [pathBuilder] is used to create a path with nodes which should be updated in UI
     */
    fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): T?
  }

  val primaryData : PrimaryDataT

  val children: ChildrenT

  val problemsCount: Int

  val excluded: Boolean

  val isValid: Boolean
    get() = children.nodesSequence.any()

  // TODO – introduce separate class for such data
  //  merge with QodanaTreeContext, but be aware that QodanaTreeContext doesn't change in one tree
  //  (at least currently it doesn't)
  val excludedData: Set<ConfigExcludeItem>

  /**
   * Used to search for a node in a tree by [SarifProblem]
   */
  fun isRelatedToProblem(problem: SarifProblem): Boolean

  /**
   * Compute new node after processing [event], or return current (`this`) instance if node shouldn't be changed
   *
   * [pathBuilder] is used to create a path with nodes which should be updated in UI
   */
  fun processTreeEvent(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): T

  fun toUiNode(parent: QodanaUiTreeNode<*, *>): QodanaUiTreeNode<T, PrimaryDataT>?
}