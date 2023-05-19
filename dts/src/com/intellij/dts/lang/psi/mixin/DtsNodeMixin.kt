package com.intellij.dts.lang.psi.mixin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.DtsSubNode

abstract class DtsRootNodeMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsRootNode {
    override val isDtsRootContainer: Boolean = false
}

abstract class DtsSubNodeMixin(node: ASTNode) : ASTWrapperPsiElement(node), DtsSubNode {
    override val isDtsRootContainer: Boolean = false
}