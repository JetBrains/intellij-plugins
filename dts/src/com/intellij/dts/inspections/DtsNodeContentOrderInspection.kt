package com.intellij.dts.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.lang.psi.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings

class DtsNodeContentOrderInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = dtsVisitor(DtsNodeContent::class) {
        checkOrder(it, holder)
    }

    private fun checkOrder(content: DtsNodeContent, holder: ProblemsHolder) {
        if (PsiTreeUtil.hasErrorElements(content)) return

        var nodeDefinition = false
        for (entry in content.dtsEntries) {
            val property = isPropertyEntry(entry)

            if (!property) {
                nodeDefinition = true
            }

            if (property && nodeDefinition) {
                holder.registerProblem(
                    entry.dtsAnnotationTarget,
                    DtsBundle.message("inspections.node_content_order.message"),
                    DtsNodeContentOrderFix,
                )
            }
        }
    }
}

private object DtsNodeContentOrderFix : LocalQuickFix {
    override fun getName(): String {
        return DtsBundle.message("inspections.node_content_order.quickfix")
    }

    override fun getFamilyName(): String = name

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
        if (!startInWriteAction()) return IntentionPreviewInfo.EMPTY

        val content = getContent(previewDescriptor) ?: return IntentionPreviewInfo.EMPTY
        val container = content.dtsContainer

        applyFix(project, previewDescriptor)

        val text = container.dtsContent?.text ?: return IntentionPreviewInfo.EMPTY
        return IntentionPreviewInfo.CustomDiff(DtsFileType, "", text)
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val content = getContent(descriptor) ?: return
        val newContent = DtsElementFactory.createNodeContent(project)

        for (child in sortChildren(project, content.firstChild.siblings())) {
            newContent.add(child)
        }

        content.replace(newContent)
    }

    private fun getContent(descriptor: ProblemDescriptor): DtsNodeContent? {
        return PsiTreeUtil.getParentOfType(descriptor.psiElement, DtsNodeContent::class.java)
    }

    private fun sortChildren(project: Project, children: Sequence<PsiElement>) = sequence {
        val nodes = mutableListOf<PsiElement>()

        // used to also sort white space and other non entry children
        var property = true
        for (child in children) {
            if (child is DtsEntry) property = isPropertyEntry(child)

            if (property) {
                yield(child)
            } else {
                nodes.add(child)
            }
        }

        // remove trailing whitespace
        for (i in (0 until nodes.size).reversed()) {
            if (nodes[i].elementType != TokenType.WHITE_SPACE) break

            nodes.removeAt(i)
        }

        // insert empty line between properties and nodes
        yield(DtsElementFactory.createWhitespace(project, "\n\n"))

        for (node in nodes) {
            yield(node)
        }
    }
}

private fun isPropertyEntry(entry: DtsEntry): Boolean {
    return when (val statement = entry.dtsStatement) {
        is DtsProperty -> true
        is DtsCompilerDirective -> statement.dtsDirectiveType == DtsTypes.DELETE_PROP
        else -> false
    }
}