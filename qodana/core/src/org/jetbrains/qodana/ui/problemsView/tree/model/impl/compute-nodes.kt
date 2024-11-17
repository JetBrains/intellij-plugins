package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.ui.problemsView.tree.model.*

fun <T : QodanaTreeNode<T, ChildrenT, *>, ChildrenT : QodanaTreeNode.Children<ChildrenT>> T.processTreeEventBase(
  event: QodanaTreeEvent,
  pathBuilder: QodanaTreePath.Builder,
  excludeDataFilter: (Set<ConfigExcludeItem>) -> Set<ConfigExcludeItem>,
  childrenExcludeDataFilter: ((Set<ConfigExcludeItem>) -> Set<ConfigExcludeItem>)? = null,
  newNodeProvider: (ChildrenT, Set<ConfigExcludeItem>) -> T
): T {
  return when(event) {
    is QodanaTreeProblemEvent -> {
      val newChildren = children.computeNewChildren(event, pathBuilder) ?: return this
      newNodeProvider.invoke(newChildren, excludedData)
    }
    is QodanaTreeExcludeEvent -> {
      processExcludeEventBase(event, excludeDataFilter, childrenExcludeDataFilter, newNodeProvider)
    }
  }
}

fun <T : QodanaTreeNode<T, ChildrenT, *>, ChildrenT : QodanaTreeNode.Children<ChildrenT>> T.processExcludeEventBase(
  event: QodanaTreeExcludeEvent,
  excludeDataFilter: (Set<ConfigExcludeItem>) -> Set<ConfigExcludeItem>,
  childrenExcludeDataFilter: ((Set<ConfigExcludeItem>) -> Set<ConfigExcludeItem>)? = null,
  newNodeProvider: (ChildrenT, Set<ConfigExcludeItem>) -> T
): T {
  val newExcludeData = excludeDataFilter.invoke(event.excludedData)
  if (newExcludeData == excludedData) return this

  val childrenExcludeData = childrenExcludeDataFilter?.invoke(newExcludeData) ?: newExcludeData
  val newEvent = event.copy(excludedData = childrenExcludeData)
  val newChildren = children.computeNewChildren(newEvent, QodanaTreePath.Builder()) ?: children
  return newNodeProvider.invoke(newChildren, newExcludeData)
}

fun <T : QodanaTreeNode<T, *, *>> computeNewQodanaChildrenNodesProblemEvent(
  pathBuilder: QodanaTreePath.Builder,
  nodesToEvents: Map<T, QodanaTreeProblemEvent>,
  currentNodes: Collection<T>,
  updatePath: Boolean = true
): List<T>? {
  if (nodesToEvents.isEmpty()) return null

  var anyUpdated = false
  val newNodes = currentNodes.toMutableSet()
  nodesToEvents.forEach { (node, event) ->
    if (event.sarifProblemsToProperties.isEmpty()) return@forEach

    val newPathBuilder = QodanaTreePath.Builder()
    val newChildNode = node.processTreeEvent(event, newPathBuilder)
    if (newChildNode === node) return@forEach

    anyUpdated = true
    newNodes.apply {
      remove(node)
      if (!newChildNode.isValid) return@forEach
      add(newChildNode)
      if (updatePath) pathBuilder.addParent(newChildNode, newPathBuilder.buildPath())
    }
  }
  return if (anyUpdated) newNodes.toList() else null
}

fun <T : QodanaTreeNode<T, *, *>> computeNewQodanaChildrenNodesExcludeEvent(
  event: QodanaTreeExcludeEvent,
  currentNodes: Collection<T>,
): List<T> {
  val pathBuilder = QodanaTreePath.Builder()
  var anyUpdated = false
  val newNodes = currentNodes.toMutableSet()
  currentNodes.forEach { node ->
    val newChildNode = node.processTreeEvent(event, pathBuilder)
    if (newChildNode === node) return@forEach

    anyUpdated = true
    newNodes.apply {
      remove(node)
      if (!newChildNode.isValid) return@forEach
      add(newChildNode)
    }
  }
  return if (anyUpdated) newNodes.toList() else currentNodes.toList()
}

fun <NodeT : QodanaTreeNode<NodeT, *, PrimaryDataT>, PrimaryDataT> List<NodeT>.nodesToEvents(
  primaryDataToEvents: Map<PrimaryDataT, QodanaTreeProblemEvent>,
  pathBuilder: QodanaTreePath.Builder,
  newNodeProvider: (PrimaryDataT, QodanaTreeProblemEvent) -> NodeT?
): Map<NodeT, QodanaTreeProblemEvent> {
  if (primaryDataToEvents.isEmpty()) return emptyMap()

  val alreadyPresentNodesByPrimaryData = associateBy { it.primaryData }
  val nodesToEvents = mutableMapOf<NodeT, QodanaTreeProblemEvent>()
  primaryDataToEvents.forEach { (primaryData, event) ->
    val alreadyPresentNode = alreadyPresentNodesByPrimaryData[primaryData]
    if (alreadyPresentNode != null){
      nodesToEvents[alreadyPresentNode] = event
      return@forEach
    }
    val newNode = newNodeProvider.invoke(primaryData, event) ?: return@forEach
    pathBuilder.excludeNode(newNode)
    nodesToEvents[newNode] = event
  }

  return nodesToEvents
}

fun QodanaTreeNode.Children<*>.lazyNodeByPrimaryDataFinder(
  nodesCount: Int,
  useMapThreshold: Int = 100
): Lazy<(QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>?> {
  return lazy {
    if (nodesCount <= useMapThreshold) {
      return@lazy { primaryData: QodanaTreeNode.PrimaryData -> nodesSequence.find { it.primaryData == primaryData } }
    }

    val primaryDataToNode = nodesSequence.associateBy { it.primaryData }
    return@lazy { primaryData: QodanaTreeNode.PrimaryData -> primaryDataToNode[primaryData] }
  }
}

fun QodanaTreeNode<*, *, *>.lazyChildrenProblemsCount(): Lazy<Int> {
  return lazy {
    children.nodesSequence.filter { !it.excluded }.map { it.problemsCount }.sum()
  }
}

fun <T : QodanaTreeNode.Children<T>, R : QodanaTreeNode.Children<R>> delegateToChildren(
  otherChildren: T,
  transform: (T) -> R
) : QodanaTreeNode.Children<R> {
  return object : QodanaTreeNode.Children<R> {
    override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
      get() = otherChildren.nodesSequence

    override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>?
      get() = otherChildren.nodeByPrimaryDataFinder

    override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): R? {
      return otherChildren.computeNewChildren(event, pathBuilder)?.let(transform)
    }
  }
}