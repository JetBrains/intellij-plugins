// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.intellij.prisma.ide.documentation.getMultilineDocCommentText
import org.intellij.prisma.lang.psi.PrismaDocComment

class PrismaDocCommentImpl(node: ASTNode) : ASTWrapperPsiElement(node), PrismaDocComment {
  override fun getTokenType(): IElementType = node.elementType

  override fun getOwner(): PsiElement? = null

  override val content: String
    get() = getMultilineDocCommentText(this)
}