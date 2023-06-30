package com.intellij.dts.lang.psi.mixin

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsNodeContent

abstract class DtsNodeContentMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsNodeContent {
    override val isDtsRootContainer: Boolean
        get() = false

    override val dtsAffiliation: DtsAffiliation
        get() = DtsAffiliation.NODE

    override val dtsEntries: List<DtsEntry>
        get() = findChildrenByClass(DtsEntry::class.java).toList()
}