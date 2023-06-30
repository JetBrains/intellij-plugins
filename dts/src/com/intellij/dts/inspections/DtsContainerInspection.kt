package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.dtsVisitor

class DtsContainerInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsContainer::class) {
        if (it.dtsAffiliation.isRoot()) {
            checkRootContainer(it, holder)
        } else if (it.dtsAffiliation.isNode()) {
            checkNodeContainer(it, holder)
        }
    }

    private fun checkRootContainer(container: DtsContainer, holder: ProblemsHolder) {
        for (statement in container.dtsStatements) {
            if (statement.dtsAffiliation.isRoot() || statement.dtsAffiliation.isUnknown()) continue

            holder.registerProblem(
                statement.dtsAnnotationTarget,
                DtsBundle.message("inspections.container.message_root"),
            )

            return
        }
    }

    private fun checkNodeContainer(container: DtsContainer, holder: ProblemsHolder) {
        for (statement in container.dtsStatements) {
            if (!statement.dtsAffiliation.isRoot()) continue

            holder.registerProblem(
                statement.dtsAnnotationTarget,
                DtsBundle.message("inspections.container.message_node"),
            )

            return
        }
    }
}