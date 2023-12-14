package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.dtsAssignableTo
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.psi.PsiElementVisitor

class DtsPropertyConstInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return dtsVisitor(DtsNode::class) { checkPropertyType(it, holder) }
  }

  private fun checkPropertyType(node: DtsNode, holder: ProblemsHolder) {
    val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

    for (property in node.dtsProperties) {
      val propertyBinding = binding.properties[property.dtsName] ?: continue
      val const = propertyBinding.const ?: continue

      // if the binding is valid this check should not fail
      if (propertyBinding.type !in const.assignableTo) continue
      if (property.dtsAssignableTo(const)) continue

      holder.registerProblem(
        property,
        bundleKey = "inspections.property_const.error",
        bundleParam = propertyBinding.const.getPresentableText(propertyBinding.type),
        rangeInElement = propertyValueRange(property),
      )
    }
  }
}