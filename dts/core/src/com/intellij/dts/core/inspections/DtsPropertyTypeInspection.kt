package com.intellij.dts.core.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.core.lang.dtsAssignableTo
import com.intellij.dts.core.lang.psi.DtsNode
import com.intellij.dts.core.lang.psi.dtsVisitor
import com.intellij.dts.core.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.psi.PsiElementVisitor

class DtsPropertyTypeInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return dtsVisitor(DtsNode::class) { checkPropertyType(it, holder) }
  }

  private fun checkPropertyType(node: DtsNode, holder: ProblemsHolder) {
    val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

    for (property in node.dtsProperties) {
      val propertyBinding = binding.properties[property.dtsName] ?: continue
      if (property.dtsAssignableTo(propertyBinding.type)) continue

      holder.registerProblem(
        property,
        bundleKey = "inspections.property_type.error",
        bundleParam = propertyBinding.type.typeName,
        rangeInElement = propertyValueRange(property),
      )
    }
  }
}