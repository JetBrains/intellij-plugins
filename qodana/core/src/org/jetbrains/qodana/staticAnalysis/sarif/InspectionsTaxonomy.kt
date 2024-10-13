package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.inspectionProfile.escapeToolGroupPathElement
import com.jetbrains.qodana.sarif.model.ReportingDescriptor

class InspectionsTaxonomy {
  class Node(val index: Int, val descriptor: ReportingDescriptor) {
    val children = mutableListOf<Node>()
  }

  private val tree = Node(-1, ReportingDescriptor(""))

  private val taxa = mutableListOf<ReportingDescriptor>()

  fun addTool(toolWrapper: InspectionToolWrapper<*, *>): Pair<Int, ReportingDescriptor> {
    val path = toolWrapper.groupPath

    var root = tree

    for (pathElement in path) {
      var node = root.children.find { it.descriptor.name == pathElement }
      if (node == null) {
        val rootId = root.descriptor.id
        val prefix = if (rootId.isNullOrEmpty()) "" else "$rootId/"
        val descriptor = ReportingDescriptor(prefix + escapeToolGroupPathElement(pathElement))
          .withName(pathElement)

        if (rootId.isNotEmpty()) {
          descriptor.withRelationships(setOf(createTaxonomyReference(root.index, rootId)))
        }

        taxa.add(descriptor)
        node = Node(taxa.size - 1, descriptor)
        root.children.add(node)
      }
      root = node
    }

    return root.index to root.descriptor
  }

  val taxonomy: List<ReportingDescriptor> = taxa
}