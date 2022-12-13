package org.intellij.prisma.lang.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.intellij.prisma.ide.documentation.stripDocCommentLinePrefix
import org.intellij.prisma.lang.parser.PrismaParserDefinition

class PrismaVirtualDocumentationComment(val comments: List<PsiComment>) : FakePsiElement(), PsiDocCommentBase {
  init {
    check(comments.isNotEmpty()) { "Comments shouldn't be an empty list" }
  }

  override fun getParent(): PsiElement = comments.first().parent

  override fun getTokenType(): IElementType = PrismaParserDefinition.DOC_COMMENT

  override fun getOwner(): PsiElement? = PsiTreeUtil.skipWhitespacesAndCommentsForward(comments.last())

  override fun getTextRange(): TextRange = TextRange.create(comments.first().startOffset, comments.last().endOffset)

  override fun getText(): String = comments.joinToString(" ") { stripDocCommentLinePrefix(it.text) }

  val content: String
    get() = text
}