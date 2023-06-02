package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.psi.DtsSubNode
import com.intellij.dts.lang.psi.dtsVisitor

private val rx = Regex("[a-zA-Z0-9,._+@-]")

class DtsNodeNameInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsSubNode::class) {
        checkNodeName(it, holder)
    }

    private fun checkNodeName(node: DtsSubNode, holder: ProblemsHolder) {
        val name = node.dtsName

        val invalidName = firstNotMatching(name, rx) {
            holder.registerError(
                node.dtsNameElement,
                bundleKey = "inspections.node_name.bad_char",
                bundleParam = it,
            )
        }
        if (invalidName) return

        if (name.count { it == '@' } > 1) {
            holder.registerError(
                node.dtsNameElement,
                bundleKey = "inspections.node_name.multiple_at",
            )
        }
    }
}