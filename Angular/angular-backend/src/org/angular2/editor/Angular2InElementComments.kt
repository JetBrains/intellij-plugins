// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.generation.IndentedCommenter
import com.intellij.lang.ASTNode
import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.LanguageCommenters
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.MultipleLangCommentProvider
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.text.CharArrayUtil
import org.angular2.lang.html.Angular2TemplateSyntax

/**
 * Comments a line of a start tag header with `//` and a selection in it with a block comment.
 * Angular 22+ accepts these in-element comments in place of an attribute, and `<!-- -->` is not valid there.
 * All other lines use the commenter that the platform selects.
 */
internal class Angular2InElementCommentProvider : MultipleLangCommentProvider {

  override fun canProcess(file: PsiFile, viewProvider: FileViewProvider): Boolean =
    supportsInElementComments(file)

  override fun getLineCommenter(file: PsiFile, editor: Editor, lineStartLanguage: Language, lineEndLanguage: Language): Commenter? {
    if (isLineInStartTagHeader(file, editor)) {
      val platformAddsSpace = CodeStyle.getLanguageSettings(file, lineStartLanguage).isLineCommentFollowedWithSpace
      return Angular2InElementCommenter(platformAddsSpace)
    }
    // The same choice as in CommentByBlockCommentHandler.getCommenter, which this provider replaces for the file.
    val fileLanguage = file.language
    val language = if (LanguageCommenters.INSTANCE.forLanguage(lineStartLanguage) == null
                       || fileLanguage.baseLanguage == lineStartLanguage)
      fileLanguage
    else
      lineStartLanguage
    return LanguageCommenters.INSTANCE.forLanguage(language)
  }

  /**
   * Tells if the first non-blank character of the line, at which the action starts, is in a start tag header.
   * The line that starts with the `>` or `/>` of the tag is not in the header.
   */
  private fun isLineInStartTagHeader(file: PsiFile, editor: Editor): Boolean {
    val caret = editor.caretModel.currentCaret
    val document = editor.document
    val actionOffset = if (caret.hasSelection()) caret.selectionStart else caret.offset
    val lineStart = document.getLineStartOffset(document.getLineNumber(actionOffset))
    val offset = CharArrayUtil.shiftForward(document.charsSequence, lineStart, " \t")
    val element = file.findElementAt(offset) ?: return false
    val tag = startTagHeaderOwner(element) ?: return false
    val headerStart = startTagHeaderStart(tag.node) ?: return false
    val headerEnd = startTagEndToken(tag.node)?.startOffset ?: tag.textRange.endOffset
    return offset in headerStart until headerEnd
  }

  /**
   * The editor uses the HTML code style here, which puts a line comment in the first column without a trailing space.
   * This commenter puts the comment after the indent and adds the space itself when the code style does not.
   */
  private class Angular2InElementCommenter(platformAddsSpace: Boolean) : IndentedCommenter {
    private val lineCommentPrefix = if (platformAddsSpace) "//" else "// "

    override fun getLineCommentPrefix(): String = lineCommentPrefix

    override fun getBlockCommentPrefix(): String = "/*"

    override fun getBlockCommentSuffix(): String = "*/"

    override fun getCommentedBlockCommentPrefix(): String = "/!*"

    override fun getCommentedBlockCommentSuffix(): String = "*!/"

    override fun forceIndentedLineComment(): Boolean = true

    override fun forceIndentedBlockComment(): Boolean = true
  }
}

/**
 * Inserts a `/` typed inside a closed start tag header as a plain character, because it can start an in-element comment.
 *
 * Without this handler, [com.intellij.codeInsight.editorActions.XmlSlashTypedHandler] sees a broken tag after the `/`
 * and adds a second `>` in the middle of the tag. Directly at the `>` or `/>` of the tag, and in an unclosed tag,
 * the platform behavior stays the same.
 */
internal class Angular2InElementCommentTypedHandler : TypedHandlerDelegate() {
  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    if (c != '/' || !supportsInElementComments(file)) return Result.CONTINUE

    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
    val offset = editor.caretModel.offset
    val element = file.findElementAt(offset) ?: return Result.CONTINUE
    val tag = startTagHeaderOwner(element) ?: return Result.CONTINUE
    val headerStart = startTagHeaderStart(tag.node) ?: return Result.CONTINUE
    val tagEnd = startTagEndToken(tag.node) ?: return Result.CONTINUE
    if (offset < headerStart || offset >= tagEnd.startOffset) return Result.CONTINUE

    EditorModificationUtil.insertStringAtCaret(editor, "/")
    return Result.STOP
  }
}

private val TAG_END_TOKENS = TokenSet.create(XmlTokenType.XML_TAG_END, XmlTokenType.XML_EMPTY_ELEMENT_END)

private fun supportsInElementComments(file: PsiFile): Boolean =
  Angular2TemplateSyntax.of(file)?.enableInElementComments == true

/** The tag of the element, or null when the element is in an attribute value. */
private fun startTagHeaderOwner(element: PsiElement): XmlTag? =
  if (element.parentOfType<XmlAttributeValue>(withSelf = true) != null)
    null
  else
    element.parentOfType<XmlTag>(withSelf = true)

/** The offset after the tag name, where the start tag header begins, or null for a tag without a name. */
private fun startTagHeaderStart(tagNode: ASTNode): Int? =
  tagNode.findChildByType(XmlTokenType.XML_NAME)?.textRange?.endOffset

/**
 * The first `>` or `/>` child of the tag, or null when the tag has no such token.
 * Usually it ends the start tag header.
 * When a typed `/` breaks the header, the start tag has no `>`, and the result is the `>` of the end tag.
 * The header then still ends before that token, so the result is correct for the callers.
 */
private fun startTagEndToken(tagNode: ASTNode): ASTNode? =
  tagNode.findChildByType(TAG_END_TOKENS)
