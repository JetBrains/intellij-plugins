package com.intellij.dts.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsPHandle
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.util.relativeTo
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType


class DtsPHandleWhitespaceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsPHandle::class) {
        checkForWhitespace(it, holder)
    }

    private fun checkForWhitespace(pHandle: DtsPHandle, holder: ProblemsHolder) {
        if (PsiTreeUtil.hasErrorElements(pHandle)) return

        for (child in childWhitespace(pHandle)) {
            holder.registerError(
                pHandle,
                bundleKey = "inspections.phandle_whitespace.error",
                rangeInElement = child.textRange.relativeTo(pHandle.textRange),
                fix = RemoveWhitespaceFix
            )
        }
    }
}

private object RemoveWhitespaceFix : PsiUpdateModCommandQuickFix() {
    override fun getFamilyName(): String {
        return DtsBundle.message("inspections.phandle_whitespace.fix")
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
        // custom diff is word based, therefore diff calculation does not work correctly for this
        return IntentionPreviewInfo.EMPTY
    }

    override fun applyFix(project: Project, element: PsiElement, updater: ModPsiUpdater) {
        for (child in childWhitespace(element)) {
            child.delete()
        }
    }
}

private fun childWhitespace(element: PsiElement): List<PsiElement> {
    return DtsUtil.children(element, unfiltered = true)
        .filter { child -> child.elementType == TokenType.WHITE_SPACE }
        .toList()
}
