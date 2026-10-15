package com.intellij.dts.core.lang.psi.mixin

import com.intellij.dts.core.lang.psi.DtsCellArrayBits
import com.intellij.dts.core.lang.psi.DtsValue
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

interface IDtsCellArrayBits : PsiElement {
  val dtsBitsValue: DtsValue?
}

abstract class DtsCellArrayBitsMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsCellArrayBits {
  override val dtsBitsValue: DtsValue?
    get() = findChildByClass(DtsValue::class.java)
}