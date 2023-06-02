package com.intellij.dts.lang.psi.mixin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsContent
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsNodeContent
import com.intellij.dts.util.DtsUtil

interface IDtsContent : PsiElement {
    val dtsContainer: DtsContainer

    val dtsEntries: List<DtsEntry>

    val isDtsNodeContent: Boolean
}

abstract class DtsContentMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsContent {
    override val dtsContainer: DtsContainer
        get() = parent as DtsContainer

    override val dtsEntries: List<DtsEntry>
        get() = DtsUtil.children(this).filterIsInstance<DtsEntry>().toList()

    override val isDtsNodeContent: Boolean = false
}

abstract class DtsNodeContentMixin(node: ASTNode) : DtsContentMixin(node), DtsNodeContent {
    override val isDtsNodeContent: Boolean = true
}