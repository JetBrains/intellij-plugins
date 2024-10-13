package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.util.text.StringUtil

object QodanaTreeNodeComparator : Comparator<QodanaTreeNode<*, *, *>> {
  override fun compare(node1: QodanaTreeNode<*, *, *>?, node2: QodanaTreeNode<*, *, *>?): Int {
    return when {
      node1 === node2 -> {
        0
      }
      node1 is QodanaTreeSeverityNode && node2 is QodanaTreeSeverityNode -> {
        compareSeverityNodes(node1, node2)
      }
      node1 is QodanaTreeInspectionCategoryNode && node2 is QodanaTreeInspectionCategoryNode -> {
        compareInspectionCategoryNodes(node1, node2)
      }
      node1 is QodanaTreeInspectionNode && node2 is QodanaTreeInspectionNode -> {
        compareInspectionNodes(node1, node2)
      }
      node1 is QodanaTreeModuleNode && node2 is QodanaTreeModuleNode -> {
        compareModuleNodes(node1, node2)
      }
      node1 is QodanaTreeModuleNode && node2 is QodanaTreeNodesWithoutModuleNode -> {
        -1
      }
      node1 is QodanaTreeNodesWithoutModuleNode && node2 is QodanaTreeNodesWithoutModuleNode -> {
        +1
      }
      node1 is QodanaTreeDirectoryNode && node2 is QodanaTreeDirectoryNode -> {
        compareDirectoryNodes(node1, node2)
      }
      node1 is QodanaTreeDirectoryNode && node2 is QodanaTreeFileNode -> {
        -1
      }
      node1 is QodanaTreeFileNode && node2 is QodanaTreeDirectoryNode -> {
        +1
      }
      node1 is QodanaTreeFileNode && node2 is QodanaTreeFileNode -> {
        compareFileNodes(node1, node2)
      }
      node1 is QodanaTreeProblemNode && node2 is QodanaTreeProblemNode -> {
        compareProblemNodes(node1, node2)
      }
      else -> 0
    }
  }
}

private fun compareSeverityNodes(node1: QodanaTreeSeverityNode, node2: QodanaTreeSeverityNode): Int {
  return node1.primaryData.qodanaSeverity.weight.compareTo(node2.primaryData.qodanaSeverity.weight) * -1
}

private fun compareInspectionCategoryNodes(node1: QodanaTreeInspectionCategoryNode, node2: QodanaTreeInspectionCategoryNode): Int {
  return StringUtil.naturalCompare(node1.primaryData.inspectionCategory, node2.primaryData.inspectionCategory)
}

private fun compareInspectionNodes(node1: QodanaTreeInspectionNode, node2: QodanaTreeInspectionNode): Int {
  return StringUtil.naturalCompare(node1.inspectionName, node2.inspectionName)
}

private fun compareModuleNodes(node1: QodanaTreeModuleNode, node2: QodanaTreeModuleNode): Int {
  return StringUtil.naturalCompare(
    node1.primaryData.moduleData.module.name,
    node2.primaryData.moduleData.module.name
  )
}

private fun compareDirectoryNodes(node1: QodanaTreeDirectoryNode, node2: QodanaTreeDirectoryNode): Int {
  return StringUtil.naturalCompare(
    node1.primaryData.ownPath.toString(),
    node2.primaryData.ownPath.toString()
  )
}

private fun compareFileNodes(node1: QodanaTreeFileNode, node2: QodanaTreeFileNode): Int {
  return StringUtil.naturalCompare(
    node1.primaryData.file.fileName.toString(),
    node2.primaryData.file.fileName.toString()
  )
}

private fun compareProblemNodes(node1: QodanaTreeProblemNode, node2: QodanaTreeProblemNode): Int {
  val node1Properties = node1.sarifProblemProperties
  val node2Properties = node2.sarifProblemProperties

  return when {
    node1Properties.isMissing && !node2Properties.isMissing -> +1
    !node1Properties.isMissing && node2Properties.isMissing -> -1

    node1Properties.isFixed && !node2Properties.isFixed -> +1
    !node1Properties.isFixed && node2Properties.isFixed -> -1

    node1Properties.line != node2Properties.line -> node1Properties.line.compareTo(node2Properties.line)
    node1Properties.column != node2Properties.column -> node1Properties.column.compareTo(node2Properties.column)

    else -> StringUtil.naturalCompare(node1.primaryData.sarifProblem.message, node2.primaryData.sarifProblem.message)
  }
}