package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.dtsVisitor

class DtsPropertyNameInspection : LocalInspectionTool() {
    companion object {
        private val rx = Regex("[a-zA-Z0-9,._+*#?-]")
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsProperty::class) {
        checkPropertyName(it, holder)
    }

    private fun checkPropertyName(property: DtsProperty, holder: ProblemsHolder) {
        val name = property.dtsName

        firstNotMatching(name, rx) {
            holder.registerError(
                property.dtsNameElement,
                bundleKey = "inspections.property_name.bad_char",
                bundleParam = it,
            )
        }
    }
}