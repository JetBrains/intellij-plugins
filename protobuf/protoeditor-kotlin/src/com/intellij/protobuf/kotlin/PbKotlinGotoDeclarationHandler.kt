package com.intellij.protobuf.kotlin

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtReferenceExpression

class PbKotlinGotoDeclarationHandler : GotoDeclarationHandler {
    private val resolver = PbKotlinProtoResolver()

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        if (sourceElement == null) return null
        if (DumbService.isDumb(sourceElement.project)) return null

        val ref = sequenceOf(
            sourceElement.containingFile.findElementAt(offset),
            sourceElement
        ).filterNotNull()
            .firstNotNullOfOrNull { element ->
                PsiTreeUtil.getParentOfType(
                    element,
                    KtReferenceExpression::class.java,
                    false
                )
            }
            ?: return null

        return resolver.resolve(ref)
    }
}
