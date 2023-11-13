package com.intellij.dts.inspections.fixes

import com.intellij.dts.DtsBundle
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

object DtsRemoveSubNodeFix : PsiUpdateModCommandQuickFix() {
    override fun getFamilyName(): String {
        return DtsBundle.message("inspections.fix.remove_sub_node")
    }

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        element.parent.delete()
    }
}