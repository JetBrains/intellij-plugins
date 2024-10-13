package org.jetbrains.qodana.ui.problemsView.tree.model

import org.jetbrains.qodana.problem.SarifProblem

fun QodanaTreeNode<*, *, *>.buildPathToProblemNode(problem: SarifProblem, pathBuilder: QodanaTreePath.Builder): QodanaTreeProblemNode? {
  if (this is QodanaTreeProblemNode) {
    pathBuilder.addNode(this)
    return this
  }

  val childNode = children.nodesSequence.firstOrNull { it.isRelatedToProblem(problem) } ?: return null
  val problemNode = childNode.buildPathToProblemNode(problem, pathBuilder) ?: return null
  pathBuilder.addNode(this)
  return problemNode
}

data class QodanaTreePathToProblemNode(
  val problemNode: QodanaTreeProblemNode,
  val path: QodanaTreePath
)

fun findNextProblemNodePath(nodes: List<QodanaTreeNode<*, *, *>>): QodanaTreePathToProblemNode?  {
  val lastNode = nodes.lastOrNull() ?: return null
  return if (lastNode !is QodanaTreeProblemNode) firstProblemNodePathInSubtree(nodes) else findAdjacentProblemNodePath(nodes, isNext = true)
}

fun findPreviousProblemNodePath(nodes: List<QodanaTreeNode<*, *, *>>): QodanaTreePathToProblemNode? {
  return findAdjacentProblemNodePath(nodes, isNext = false)
}

private fun firstProblemNodePathInSubtree(nodes: List<QodanaTreeNode<*, *, *>>): QodanaTreePathToProblemNode? {
  val currentNode = nodes.lastOrNull() ?: return null

  val ancestorsNodes = nodes.subList(0, nodes.size - 1)
  val currentPath = QodanaTreePath.Builder().apply {
    ancestorsNodes.asReversed().forEach { addNode(it) }
  }.buildPath()

  val allNodesInCurrentSubtree = currentNode.subtreeDfsSequence(currentPath) { childrenNodes ->
    childrenNodes.filter { it.problemsCount > 0 }.sequentiallySelectOrderedGreaterThan(null)
  }
  return allNodesInCurrentSubtree
    .mapNotNull {
      val problemNode = it.second as? QodanaTreeProblemNode ?: return@mapNotNull null
      QodanaTreePathToProblemNode(problemNode, it.first)
    }
    .firstOrNull()
}

private fun findAdjacentProblemNodePath(nodes: List<QodanaTreeNode<*, *, *>>, isNext: Boolean): QodanaTreePathToProblemNode? {
  return nodes.indices.reversed().asSequence().flatMap { i ->
    val parentNode = nodes.getOrNull(i - 1) ?: return@flatMap emptySequence()
    val currentNode = nodes[i]

    val ancestorsNodes = nodes.subList(0, i)
    val currentPath = QodanaTreePath.Builder().apply {
      ancestorsNodes.asReversed().forEach { addNode(it) }
    }.buildPath()

    val filteredNodeSiblings = parentNode.children.nodesSequence.filter { it.problemsCount > 0 }
    val orderedNodeSiblings = if (isNext) {
      filteredNodeSiblings.sequentiallySelectOrderedGreaterThan(currentNode)
    } else {
      filteredNodeSiblings.sequentiallySelectOrderedLessThan(currentNode)
    }

    val allNodesInSiblingsSubtrees = orderedNodeSiblings.flatMap { siblingNode ->
      val allNodesInSiblingSubtree = siblingNode
        .subtreeDfsSequence(currentPath) { childrenNodes ->
          val filteredChildrenNodes = childrenNodes.filter { it.problemsCount > 0 }
          val orderedChildrenNodes = if (isNext) {
            filteredChildrenNodes.sequentiallySelectOrderedGreaterThan(null)
          } else {
            filteredChildrenNodes.sequentiallySelectOrderedLessThan(null)
          }
          orderedChildrenNodes
        }

      allNodesInSiblingSubtree
    }

    allNodesInSiblingsSubtrees.mapNotNull {
      val problemNode = it.second as? QodanaTreeProblemNode ?: return@mapNotNull null
      QodanaTreePathToProblemNode(problemNode, it.first)
    }
  }.firstOrNull()
}

private fun QodanaTreeNode<*, *, *>.subtreeDfsSequence(
  currentPath: QodanaTreePath,
  childrenNodesTransform: (Sequence<QodanaTreeNode<*, *, *>>) -> Sequence<QodanaTreeNode<*, *, *>>
): Sequence<Pair<QodanaTreePath, QodanaTreeNode<*, *, *>>> {
  return sequence {
    val newPath = QodanaTreePath.Builder().apply {
      addNode(this@subtreeDfsSequence)
      addPath(currentPath)
    }.buildPath()

    yield(newPath to this@subtreeDfsSequence)

    val childrenNodes = childrenNodesTransform.invoke(this@subtreeDfsSequence.children.nodesSequence)
    yieldAll(childrenNodes.flatMap { childNode -> childNode.subtreeDfsSequence(newPath, childrenNodesTransform) })
  }
}

private fun Sequence<QodanaTreeNode<*, *, *>>.sequentiallySelectOrderedGreaterThan(other: QodanaTreeNode<*, *, *>?): Sequence<QodanaTreeNode<*, *, *>> {
  return generateSequence(seed = this.minGreaterThan(other, QodanaTreeNodeComparator), ) { previousNode ->
    this.minGreaterThan(previousNode, QodanaTreeNodeComparator)
  }
}

private fun Sequence<QodanaTreeNode<*, *, *>>.sequentiallySelectOrderedLessThan(other: QodanaTreeNode<*, *, *>?): Sequence<QodanaTreeNode<*, *, *>> {
  return generateSequence(seed = this.maxLessThan(other, QodanaTreeNodeComparator), ) { previousNode ->
    this.maxLessThan(previousNode, QodanaTreeNodeComparator)
  }
}

private fun <T> Sequence<T>.maxLessThan(other: T?, comparator: Comparator<T>): T? {
  return minGreaterThan(other, comparator.reversed())
}

private fun <T> Sequence<T>.minGreaterThan(other: T?, comparator: Comparator<T>): T? {
  if (other == null) return minWithOrNull(comparator)

  var currentMin: T? = null
  for (element in this) {
    val elementIsLessThanOther = comparator.compare(element, other) <= 0
    if (elementIsLessThanOther) continue

    val elementIsLessThanCurrentMin = currentMin == null || comparator.compare(element, currentMin) < 0
    if (elementIsLessThanCurrentMin) {
      currentMin = element
    }
  }
  return currentMin
}