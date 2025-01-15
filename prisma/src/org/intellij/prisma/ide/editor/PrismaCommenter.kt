package org.intellij.prisma.ide.editor

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import com.intellij.psi.tree.IElementType
import org.intellij.prisma.ide.documentation.isDocComment
import org.intellij.prisma.lang.psi.DOC_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.*
import org.jetbrains.annotations.Unmodifiable

class PrismaCommenter : CodeDocumentationAwareCommenter {
  override fun getLineCommentPrefix(): String = "//"

  override fun getBlockCommentPrefix(): String? = "/*"

  override fun getBlockCommentSuffix(): String? = "*/"

  override fun getCommentedBlockCommentPrefix(): String? = null

  override fun getCommentedBlockCommentSuffix(): String? = null

  override fun getLineCommentTokenType(): IElementType? = DOUBLE_COMMENT

  override fun getLineCommentTokenTypes(): @Unmodifiable List<IElementType> =
    listOf(DOUBLE_COMMENT, TRIPLE_COMMENT)

  override fun getBlockCommentTokenType(): IElementType? = BLOCK_COMMENT

  override fun getDocumentationCommentTokenType(): IElementType? = DOC_COMMENT

  override fun getDocumentationCommentPrefix(): String? = "/**"

  override fun getDocumentationCommentLinePrefix(): String? = "*"

  override fun getDocumentationCommentSuffix(): String? = "*/"

  override fun isDocumentationComment(element: PsiComment): Boolean = element.isDocComment
}