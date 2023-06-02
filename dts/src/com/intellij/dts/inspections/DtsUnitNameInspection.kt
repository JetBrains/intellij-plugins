package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.psi.DtsSubNode
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.util.DtsUtil
import com.intellij.openapi.util.TextRange

private val leading0s = Regex("0[0-9a-f]")

class DtsUnitNameInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsSubNode::class) {
        checkNodeName(it, holder)
    }

    private fun checkNodeName(node: DtsSubNode, holder: ProblemsHolder) {
        val (baseName, addr) = DtsUtil.splitName(node.dtsName)
        if (addr == null) return

        if (addr.startsWith("0x")) {
            holder.registerWarning(
                node.dtsNameElement,
                bundleKey = "inspections.unit_name.leading_0x",
                rangeInElement = TextRange.from(baseName.length + 1, addr.length),
            )

            return
        }

        if (leading0s.matchesAt(addr, 0)) {
            holder.registerWarning(
                node.dtsNameElement,
                bundleKey = "inspections.unit_name.leading_0s",
                rangeInElement = TextRange.from(baseName.length + 1, addr.length),
            )
        }
    }
}