package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.dtsAssignableTo
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.psi.PsiElementVisitor

class DtsPropertyEnumInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return dtsVisitor(DtsNode::class) { checkPropertyType(it, holder) }
  }

  private fun checkPropertyType(node: DtsNode, holder: ProblemsHolder) {
    val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

    for (property in node.dtsProperties) {
      val propertyBinding = binding.properties[property.dtsName] ?: continue
      val enum = propertyBinding.enum ?: continue

      if (enum.isEmpty()) continue
      if (enum.any(property::dtsAssignableTo)) continue

      holder.registerProblem(
        property,
        bundleKey = "inspections.property_enum.error",
        bundleParam = enum.joinToString { it.getPresentableText(propertyBinding.type) },
        rangeInElement = propertyValueRange(property),
      )
    }
  }
}