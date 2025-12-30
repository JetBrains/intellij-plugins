package org.angular2.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlBlockContents

class AngularBlockExtendWordSelectionHandler : ExtendWordSelectionHandlerBase() {

  override fun canSelect(e: PsiElement): Boolean =
    e is Angular2HtmlBlock || e is Angular2HtmlBlockContents

  override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? =
    when (e) {
      is Angular2HtmlBlock -> {
        val primaryBlock = e.primaryBlock
        val firstBlock = primaryBlock ?: e
        val lastBlock = primaryBlock?.blockSiblingsForward()?.lastOrNull() ?: primaryBlock ?: e
        expandToWholeLine(editorText, e.textRange, false)
          .plus(expandToWholeLine(editorText, TextRange(firstBlock.textRange.startOffset, lastBlock.textRange.endOffset), false))
      }
      is Angular2HtmlBlockContents -> {
        val textRange = e.textRange
        val outerRanges = expandToWholeLine(editorText, textRange, false)
        val innerRanges = if (e.firstChild?.elementType == Angular2HtmlTokenTypes.BLOCK_START
                              && e.lastChild?.elementType == Angular2HtmlTokenTypes.BLOCK_END) {
          expandToWholeLine(editorText, TextRange(textRange.startOffset + 1, textRange.endOffset - 1), false)
        }
        else {
          emptyList()
        }
        innerRanges + outerRanges
      }
      else -> null
    }
}