package com.intellij.dts.core.lang.psi.mixin

import com.intellij.dts.core.lang.DtsAffiliation
import com.intellij.dts.core.lang.psi.DtsEntry
import com.intellij.dts.core.lang.psi.DtsNodeContent
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class DtsNodeContentMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsNodeContent {
  override val isDtsRootContainer: Boolean
    get() = false

  override val dtsAffiliation: DtsAffiliation
    get() = DtsAffiliation.NODE

  override val dtsEntries: List<DtsEntry>
    get() = findChildrenByClass(DtsEntry::class.java).toList()
}