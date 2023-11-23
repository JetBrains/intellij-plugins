package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.inspections.fixes.DtsRemovePropertyFix
import com.intellij.dts.inspections.fixes.DtsRemoveSubNodeFix
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.psi.PsiElementVisitor

class DtsDuplicateElementInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return dtsVisitor(DtsNode::class) {
      checkProperties(it, holder)
      checkSubNodes(it, holder)
    }
  }

  private fun checkProperties(node: DtsNode, holder: ProblemsHolder) {
    val properties = mutableSetOf<String>()

    for (property in node.dtsProperties) {
      if (properties.add(property.dtsName)) continue

      holder.registerProblem(
        property,
        bundleKey = "inspections.duplicate_element.property_error",
        fix = if (property.dtsIsComplete) DtsRemovePropertyFix else null
      )
    }
  }

  private fun checkSubNodes(node: DtsNode, holder: ProblemsHolder) {
    val subNodes = mutableSetOf<String>()

    for (subNode in node.dtsSubNodes) {
      if (subNodes.add(subNode.dtsName)) continue

      holder.registerProblem(
        subNode,
        bundleKey = "inspections.duplicate_element.sub_node_error",
        fix = if (subNode.dtsIsComplete) DtsRemoveSubNodeFix else null
      )
    }
  }
}