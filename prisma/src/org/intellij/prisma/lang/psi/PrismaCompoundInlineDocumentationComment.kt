package org.intellij.prisma.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.intellij.prisma.ide.documentation.stripDocCommentLinePrefix
import org.intellij.prisma.lang.psi.PrismaElementTypes.TRIPLE_COMMENT

class PrismaCompoundInlineDocumentationComment(val comments: List<PsiComment>) : FakePsiElement(), PrismaDocComment {
  init {
    check(comments.isNotEmpty()) { "Comments shouldn't be an empty list" }
    check(comments.all { it.elementType == TRIPLE_COMMENT }) { "Comments should be of the TRIPLE_COMMENT type" }
  }

  override fun getNode(): ASTNode? = comments.firstNotNullOfOrNull { it.node }

  override fun getParent(): PsiElement = comments.first().parent

  override fun getTokenType(): IElementType = TRIPLE_COMMENT

  override fun getOwner(): PsiElement? = PsiTreeUtil.skipWhitespacesAndCommentsForward(comments.last())

  override fun getTextRange(): TextRange = TextRange.create(comments.first().startOffset, comments.last().endOffset)

  override fun getText(): String = comments.joinToString(" ") { stripDocCommentLinePrefix(it.text) }

  override val content: String
    get() = text
}