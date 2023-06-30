package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.*
import com.intellij.psi.util.PsiTreeUtil

class DtsStatementOrderInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsContainer::class) {
        if (it.dtsAffiliation.isNode()) {
            checkOrder(it, holder)
        }
    }

    private fun checkOrder(container: DtsContainer, holder: ProblemsHolder) {
        val statements = container.dtsStatements

        // abort if this container contains any errors or statements with unknown kind
        if (statements.any { it.dtsStatementKind.isUnknown() }) return
        if (PsiTreeUtil.hasErrorElements(container)) return

        // check the order of property and node definitions
        var nodeDefinition = false
        for (statement in container.dtsStatements) {
            val kind = statement.dtsStatementKind

            if (kind.isNode()) {
                nodeDefinition = true
            }

            if (kind.isProperty() && nodeDefinition) {
                holder.registerProblem(
                    statement.dtsAnnotationTarget,
                    DtsBundle.message("inspections.statement_order.message"),
                )

                return
            }
        }
    }
}