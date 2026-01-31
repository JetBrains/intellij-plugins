// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.generation.IndentedCommenter
import com.intellij.codeInsight.generation.SelfManagingCommenter
import com.intellij.codeInsight.generation.SelfManagingCommenterUtil
import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.util.text.CharArrayUtil
import org.intellij.terraform.hcl.HCLElementTypes.BLOCK_COMMENT
import org.intellij.terraform.hcl.HCLElementTypes.LINE_C_COMMENT
import org.intellij.terraform.hcl.HCLElementTypes.LINE_HASH_COMMENT
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings
import org.intellij.terraform.hcl.formatter.LineCommenterPrefix

class HCLCommenter : CodeDocumentationAwareCommenter, SelfManagingCommenter<HCLCommenterDataHolder>, IndentedCommenter {
  private val BLOCK_COMMENT_PREFIX = "/*"
  private val BLOCK_COMMENT_SUFFIX = "*/"
  private val LINE_POUND_COMMENT = LineCommenterPrefix.POUND_SIGN.prefix
  private val LINE_DOUBLE_SLASH_COMMENT = LineCommenterPrefix.DOUBLE_SLASHES.prefix

  override fun getLineCommentPrefix(): String = LINE_POUND_COMMENT

  override fun getLineCommentPrefixes(): List<String> = listOf(LINE_POUND_COMMENT, LINE_DOUBLE_SLASH_COMMENT)

  override fun getBlockCommentPrefix(): String = BLOCK_COMMENT_PREFIX

  override fun getBlockCommentSuffix(): String = BLOCK_COMMENT_SUFFIX

  override fun getCommentedBlockCommentPrefix(): String? = null

  override fun getCommentedBlockCommentSuffix(): String? = null

  override fun getLineCommentTokenType(): IElementType? = LINE_HASH_COMMENT

  override fun getLineCommentTokenTypes(): List<IElementType> = listOf(LINE_HASH_COMMENT, LINE_C_COMMENT)

  override fun getBlockCommentTokenType(): IElementType? = BLOCK_COMMENT

  override fun getDocumentationCommentTokenType(): IElementType? = null

  override fun getDocumentationCommentPrefix(): String? = null

  override fun getDocumentationCommentLinePrefix(): String? = null

  override fun getDocumentationCommentSuffix(): String? = null

  override fun isDocumentationComment(element: PsiComment?): Boolean = false

  override fun createLineCommentingState(startLine: Int,
                                         endLine: Int,
                                         document: Document,
                                         file: PsiFile): HCLCommenterDataHolder = HCLCommenterDataHolder(file)

  override fun createBlockCommentingState(selectionStart: Int,
                                          selectionEnd: Int,
                                          document: Document,
                                          file: PsiFile): HCLCommenterDataHolder = HCLCommenterDataHolder(file)

  override fun insertBlockComment(startOffset: Int, endOffset: Int, document: Document, data: HCLCommenterDataHolder?): TextRange =
    SelfManagingCommenterUtil.insertBlockComment(startOffset, endOffset, document, BLOCK_COMMENT_PREFIX, BLOCK_COMMENT_SUFFIX)

  override fun uncommentBlockComment(startOffset: Int, endOffset: Int, document: Document, data: HCLCommenterDataHolder?): Unit =
    SelfManagingCommenterUtil.uncommentBlockComment(startOffset, endOffset, document, BLOCK_COMMENT_PREFIX, BLOCK_COMMENT_SUFFIX)

  override fun getBlockCommentSuffix(selectionEnd: Int, document: Document, data: HCLCommenterDataHolder): String = blockCommentSuffix

  override fun getBlockCommentPrefix(selectionStart: Int, document: Document, data: HCLCommenterDataHolder): String = blockCommentPrefix

  override fun getBlockCommentRange(selectionStart: Int, selectionEnd: Int, document: Document, data: HCLCommenterDataHolder): TextRange? =
    SelfManagingCommenterUtil.getBlockCommentRange(selectionStart, selectionEnd, document, BLOCK_COMMENT_PREFIX, BLOCK_COMMENT_SUFFIX)

  override fun getCommentPrefix(line: Int, document: Document, data: HCLCommenterDataHolder): String = lineCommentPrefix

  override fun isLineCommented(line: Int, offset: Int, document: Document, data: HCLCommenterDataHolder): Boolean =
    isPoundSignComment(offset, document) || isDoubleSlashesComment(offset, document)

  override fun uncommentLine(line: Int, offset: Int, document: Document, data: HCLCommenterDataHolder) {
    if (isLineCommented(line, offset, document, data)) {
      val lengthOfComment = if (isPoundSignComment(offset, document)) LINE_POUND_COMMENT.length else LINE_DOUBLE_SLASH_COMMENT.length
      val hasSpace = CharArrayUtil.regionMatches(document.charsSequence, offset + lengthOfComment, " ")
      document.deleteString(offset, offset + lengthOfComment + (if (hasSpace) 1 else 0))
    }
  }

  override fun commentLine(line: Int, offset: Int, document: Document, data: HCLCommenterDataHolder): Unit =
    document.insertString(offset, lineCommentPrefix(data))

  override fun forceIndentedLineComment(): Boolean = true

  private fun isPoundSignComment(offset: Int, document: Document): Boolean =
    CharArrayUtil.regionMatches(document.charsSequence, offset, LINE_POUND_COMMENT)

  private fun isDoubleSlashesComment(offset: Int, document: Document): Boolean =
    CharArrayUtil.regionMatches(document.charsSequence, offset, LINE_DOUBLE_SLASH_COMMENT)

  private fun lineCommentPrefix(data: HCLCommenterDataHolder): String {
    val file = data.psiFile ?: return "$LINE_POUND_COMMENT "
    val settings = CodeStyle.getCustomSettings(file, HclCodeStyleSettings::class.java)
    val lineCommenterChar = LineCommenterPrefix.entries.find { it == settings.LINE_COMMENTER_CHARACTER } ?: LineCommenterPrefix.POUND_SIGN
    return "${lineCommenterChar.prefix} "
  }
}
