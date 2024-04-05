package org.intellij.prisma.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.intellij.prisma.ide.documentation.stripDocCommentLinePrefix

class PrismaVirtualDocumentationComment(val comments: List<PsiComment>) : FakePsiElement(), PsiDocCommentBase {
  init {
    check(comments.isNotEmpty()) { "Comments shouldn't be an empty list" }
  }

  override fun getParent(): PsiElement = comments.first().parent

  override fun getTokenType(): IElementType = PrismaElementTypes.DOC_COMMENT

  override fun getOwner(): PsiElement? = PsiTreeUtil.skipWhitespacesAndCommentsForward(comments.last())

  override fun getTextRange(): TextRange = TextRange.create(comments.first().startOffset, comments.last().endOffset)

  override fun getText(): String = comments.joinToString(" ") { stripDocCommentLinePrefix(it.text) }

  val content: String
    get() = text
}