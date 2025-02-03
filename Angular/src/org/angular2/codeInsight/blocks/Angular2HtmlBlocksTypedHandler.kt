// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.asSafely
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock

private class Angular2HtmlBlocksTypedHandler : TypedHandlerDelegate() {

  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (charTyped == '@' && Angular2TemplateSyntax.of(file)?.enableBlockSyntax == true) {
      // TODO do as much as possible through highlighter iterator
      val at = file.findElementAt(editor.getCaretModel().offset)
      if (at != null &&
          (at.parent.let { it is XmlDocument || it is XmlText }
           || at.elementType == XmlTokenType.XML_END_TAG_START)) {
        AutoPopupController.getInstance(project)
          .scheduleAutoPopup(editor, CompletionType.BASIC, null)
      }
    }
    else if (charTyped == ' ') {
      val at = file.findElementAt(editor.getCaretModel().offset - 1)
      if (at != null && (afterSemicolon(at) || afterParameterName(at)
                         || afterVarDefinitionInForBlock(at) || afterOfKeywordInForBlock(at)
                         || afterEqInForBlock(at))) {
        AutoPopupController.getInstance(project)
          .scheduleAutoPopup(editor, CompletionType.BASIC, null)
      }
    }
    return Result.CONTINUE
  }

  override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (c == '(') {
      val offset = editor.caretModel.offset
      if (offset >= 2) {
        val iterator = editor.highlighter.createIterator(offset - 2)
        while (!iterator.atEnd() && JSElementTypes.WHITE_SPACES.contains(iterator.tokenType)) {
          iterator.retreat()
        }
        if (!iterator.atEnd() && iterator.tokenType == Angular2HtmlTokenTypes.BLOCK_NAME) {
          val fwdIterator = editor.highlighter.createIterator(offset)
          if (!fwdIterator.atEnd() && fwdIterator.tokenType == JSTokenTypes.LT) {
            editor.document.insertString(offset, ")")
          }
        }
      }
    }
    return Result.CONTINUE
  }

  private fun afterSemicolon(at: PsiElement): Boolean =
    at.elementType == Angular2HtmlTokenTypes.BLOCK_SEMICOLON

  private fun afterVarDefinitionInForBlock(at: PsiElement): Boolean =
    at.elementType == JSTokenTypes.IDENTIFIER
    && at.parent is Angular2BlockParameterVariableImpl
    && at.parent.parent.parent.let { it is Angular2BlockParameter && it.block?.getName() == BLOCK_FOR }

  private fun afterOfKeywordInForBlock(at: PsiElement): Boolean =
    at.elementType == JSTokenTypes.IDENTIFIER
    && at.textMatches("of")
    && at.parent.asSafely<Angular2BlockParameter>()
      ?.takeIf { it.isPrimaryExpression }
      ?.parentOfType<Angular2HtmlBlock>()
      ?.getName() == BLOCK_FOR

  private fun afterEqInForBlock(at: PsiElement): Boolean =
    at.elementType == JSTokenTypes.EQ
    && at.parent.asSafely<Angular2BlockParameterVariableImpl>()
      ?.parentOfType<Angular2BlockParameter>()
      ?.takeIf { it.name == PARAMETER_LET }
      ?.parentOfType<Angular2HtmlBlock>()
      ?.getName() == BLOCK_FOR

  private fun afterParameterName(at: PsiElement): Boolean =
    (at.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME
     || at.elementType == Angular2TokenTypes.BLOCK_PARAMETER_PREFIX
    ) && at.parent.asSafely<Angular2BlockParameter>()?.definition?.hasContent != false

}