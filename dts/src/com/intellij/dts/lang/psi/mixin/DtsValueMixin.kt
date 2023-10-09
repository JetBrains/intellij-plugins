package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsInt
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.lang.resolve.DtsBindingReference
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.util.trimEnds
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

abstract class DtsIntMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsInt {
    override fun dtsParse(): Int? {
        val stripedText = text.replace("U", "").replace("L", "")

        return try {
            Integer.decode(stripedText)
        } catch (e: NumberFormatException) {
            null
        }
    }
}

abstract class DtsStringMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsString {
    private val value: PsiElement?
        get() = findChildByType(DtsTypes.STRING_VALUE)

    override val dtsValueRange: TextRange
        get() = value?.textRange ?: textRange.trimEnds()

    override fun dtsParse(): String = value?.text ?: ""

    override fun getOwnReferences(): Collection<PsiSymbolReference> {
        return DtsUtil.singleResult {
            val property = DtsTreeUtil.parentProperty(this) ?: return@singleResult null
            if (property.dtsName != "compatible") return@singleResult null

            DtsBindingReference(this)
        }
    }
}