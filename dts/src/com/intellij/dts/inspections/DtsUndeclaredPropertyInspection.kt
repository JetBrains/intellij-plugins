package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.zephyr.DtsZephyrBinding
import com.intellij.dts.zephyr.DtsZephyrBindingProvider
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class DtsUndeclaredPropertyInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = dtsVisitor(DtsNode::class) {
        checkProperties(it, holder)
    }

    // from zephyr devicetree.edtlib.Node._check_undeclared_props
    private fun isPropertyUndeclared(binding: DtsZephyrBinding, name: String): Boolean {
        val isSpecialProperty = name.endsWith("-controller") || name.startsWith("#") || name.startsWith("pinctrl-")

        return if (isSpecialProperty) {
            false
        } else {
            !binding.properties.containsKey(name)
        }
    }

    private fun checkProperties(node: DtsNode, holder: ProblemsHolder) {
        val binding = DtsZephyrBindingProvider.bindingFor(node, fallbackBinding = false) ?: return
        if (binding.allowUndeclaredProperties) return

        val undeclaredProperties = node.dtsProperties.filter { property -> isPropertyUndeclared(binding, property.dtsName) }

        for (property in undeclaredProperties) {
            holder.registerError(
                property,
                bundleKey = "inspections.undeclared_property.error",
                fix = if (property.dtsIsComplete) RemovePropertyFix else null
            )
        }
    }
}

private object RemovePropertyFix : PsiUpdateModCommandQuickFix() {
    override fun getFamilyName(): String {
        return DtsBundle.message("inspections.undeclared_property.fix")
    }

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        element.parent.delete()
    }
}
