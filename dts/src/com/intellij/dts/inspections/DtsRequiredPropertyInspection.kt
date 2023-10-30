package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.api.DtsNodeVisitor
import com.intellij.dts.api.dtsAccept
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.zephyr.DtsZephyrBindingProvider
import com.intellij.psi.PsiElementVisitor

class DtsRequiredPropertyInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = dtsVisitor(DtsNode::class) { node ->
        if (node.containingFile is DtsFile.Source || node.containingFile is DtsFile.Overlay) {
            checkProperties(node, holder)
        }
    }

    private fun collectProperties(node: DtsNode): Set<String> {
        val properties = mutableSetOf<String>()

        val visitor = object : DtsNodeVisitor {
            override fun visitDelete() {
                properties.clear()
            }

            override fun visitProperty(property: DtsProperty) {
                properties.add(property.dtsName)
            }

            override fun visitDeleteProperty(name: String) {
               properties.remove(name)
            }
        }
        node.dtsAccept(visitor, strict = false)

        return properties
    }

    private fun checkProperties(node: DtsNode, holder: ProblemsHolder) {
        val propertyBindings = DtsZephyrBindingProvider.bindingFor(node, fallbackBinding = false)?.properties ?: return

        val requiredProperties = propertyBindings.values.filter { it.required }.toMutableList()
        if (requiredProperties.isEmpty()) return

        val declaredProperties = collectProperties(node)
        requiredProperties.removeAll { declaredProperties.contains(it.name) }
        if (requiredProperties.isEmpty()) return

        holder.registerError(
            node,
            bundleKey = "inspections.required_property.error",
            bundleParam = requiredProperties.joinToString { "\"${it.name}\"" },
        )
    }
}
