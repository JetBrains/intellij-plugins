package org.intellij.plugin.mdx.codeInspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import org.intellij.plugin.mdx.lang.psi.MdxFile

class MdxInspectionSuppressor : InspectionSuppressor {
    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return emptyArray()
    }

    override fun isSuppressedFor(element: PsiElement, name: String): Boolean {
        if (element is MdxFile && (name == "JSXNamespaceValidation" || name == "BadExpressionStatementJS")) {
            return true
        }
        return false
    }
}
