package com.intellij.dts.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.psi.DtsContent
import com.intellij.dts.lang.psi.dtsVisitor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt

class DtsContentInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsFile.Source::class) {
        it.dtsContent?.let { content -> checkContent(content, holder) }
    }

    private fun checkContent(content: DtsContent, holder: ProblemsHolder) {
        if (!content.isDtsNodeContent) return

        content.dtsEntries.firstOrNull()?.let {
            holder.registerProblem(
                it.dtsStatement.dtsAnnotationTarget,
                DtsBundle.message("inspections.dts_content.message"),
                DtsContentFix
            )
        }
    }
}

private object DtsContentFix : LocalQuickFix {
    override fun getName(): String {
        return DtsBundle.message("inspections.dts_content.quickfix")
    }

    override fun getFamilyName(): String = name

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
        val file = previewDescriptor.psiElement.containingFile
        return IntentionPreviewInfo.rename(file, "${FileUtilRt.getNameWithoutExtension(file.name)}.dtsi")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val file = descriptor.psiElement.containingFile.virtualFile
        file.rename(this, "${file.nameWithoutExtension}.dtsi")
    }
}