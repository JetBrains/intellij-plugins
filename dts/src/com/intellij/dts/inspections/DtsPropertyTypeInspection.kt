package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.dtsAssignableTo
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.util.relativeTo
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

class DtsPropertyTypeInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return dtsVisitor(DtsNode::class) { checkPropertyType(it, holder) }
    }

    private fun rangeInProperty(property: DtsProperty): TextRange? {
        val values = property.dtsValues
        if (values.isEmpty()) return null

        val valuesRange = TextRange(values.first().startOffset, values.last().endOffset)
        return valuesRange.relativeTo(property.textRange)
    }

    private fun checkPropertyType(node: DtsNode, holder: ProblemsHolder) {
        val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

        for (property in node.dtsProperties) {
            val propertyBinding = binding.properties[property.dtsName] ?: continue
            if (property.dtsAssignableTo(propertyBinding.type)) continue

            holder.registerError(
                property,
                bundleKey = "inspections.property_type.error",
                bundleParam = propertyBinding.type.typeName,
                rangeInElement = rangeInProperty(property),
            )
        }
    }
}