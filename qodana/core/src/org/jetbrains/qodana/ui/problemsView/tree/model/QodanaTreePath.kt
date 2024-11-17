package org.jetbrains.qodana.ui.problemsView.tree.model

data class QodanaTreePath(val primaryDataPath: List<QodanaTreeNode.PrimaryData>,
                          val primaryDataPaths: List<List<QodanaTreeNode.PrimaryData>> = emptyList()) {
  class Builder {
    private val excludedNodesPrimaryData = mutableSetOf<QodanaTreeNode.PrimaryData>()
    private val primaryDataPath = mutableListOf<QodanaTreeNode.PrimaryData>()
    private val primaryDataPaths = mutableListOf<List<QodanaTreeNode.PrimaryData>>()

    fun addNode(node: QodanaTreeNode<*, *, *>) {
      primaryDataPath.add(node.primaryData)
    }

    fun excludeNode(node: QodanaTreeNode<*, *, *>) {
      excludedNodesPrimaryData.add(node.primaryData)
    }

    fun addPath(treePath: QodanaTreePath) {
      treePath.primaryDataPath.asReversed().forEach { primaryDataPath.add(it) }
    }

    fun addParent(node: QodanaTreeNode<*, *, *>, anotherPath: QodanaTreePath) {
      if (anotherPath.primaryDataPaths.isEmpty()) {
        if (node.primaryData !in excludedNodesPrimaryData)
          primaryDataPaths.add(listOf(node.primaryData))
        return
      }
      anotherPath.primaryDataPaths.forEach { primaryDataPaths.add(listOf(node.primaryData) + it) }
    }

    fun buildPath(): QodanaTreePath = QodanaTreePath(primaryDataPath.filter { it !in excludedNodesPrimaryData }.reversed(), primaryDataPaths)
  }

  fun startsWith(other: QodanaTreePath): Boolean {
    for (i in other.primaryDataPath.indices) {
      val thisPrimaryData = this.primaryDataPath.getOrNull(i)
      val otherPrimaryData = other.primaryDataPath[i]

      if (otherPrimaryData != thisPrimaryData) return false
    }
    return true
  }
}