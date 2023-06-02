package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.psi.DtsInt
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

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