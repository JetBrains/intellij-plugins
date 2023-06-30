package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsInt
import com.intellij.dts.lang.psi.DtsString
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
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

abstract class DtsStringMixin(node: ASTNode): ASTWrapperPsiElement(node), DtsString {
    override fun dtsParse(): String {
        val value = findChildByType<PsiElement>(DtsTypes.STRING_VALUE) ?: return ""

        return value.text
    }
}