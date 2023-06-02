package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

private val rx = Regex("[a-zA-Z0-9_]")
private val startRx = Regex("[a-zA-Z_]")

class DtsLabelNameInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsTypes.LABEL) {
        checkLabel(it, holder)
    }

    private fun checkLabel(label: PsiElement, holder: ProblemsHolder) {
        val name = label.text.trimEnd(':')

        if (!startRx.matches(name.substring(0..0))) {
            holder.registerError(
                label,
                bundleKey = "inspections.label_name.bad_first_char",
                bundleParam = name[0],
                rangeInElement = TextRange.from(0, name.length),
            )

            return
        }

        firstNotMatching(name, rx) {
            holder.registerError(
                label,
                bundleKey = "inspections.label_name.bad_char",
                bundleParam = it,
                rangeInElement = TextRange.from(0, name.length),
            )
        }
    }
}