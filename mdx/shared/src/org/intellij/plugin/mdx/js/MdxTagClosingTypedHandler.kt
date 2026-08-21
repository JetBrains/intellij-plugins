package org.intellij.plugin.mdx.js

import com.intellij.application.options.editor.WebEditorOptions
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.lang.javascript.DialectDetector
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtilEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.xml.util.HtmlUtil
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.intellij.plugin.mdx.lang.template.MdxFileViewProvider

/**
 * Auto-closes JSX/HTML tags (and the `<>` fragment) when `>` is typed in an MDX file. Neither
 * [com.intellij.lang.javascript.editing.JavaScriptTypedHandler] (gates on the base file being a JS
 * dialect, which MDX is not) nor `XmlGtTypedHandler` (gates on
 * `XMLLanguage`, but JSX tag leaves report the MdxJS dialect) fires here, so this bridges the gap.
 */
internal class MdxTagClosingTypedHandler : TypedHandlerDelegate() {
  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    if (c != '>') return Result.CONTINUE
    val webEditorOptions = WebEditorOptions.getInstance()
    if (webEditorOptions == null || !webEditorOptions.isAutomaticallyInsertClosingTag) return Result.CONTINUE

    val viewProvider = file.viewProvider
    if (viewProvider !is MdxFileViewProvider) return Result.CONTINUE

    PsiDocumentManager.getInstance(project).commitAllDocuments()
    val mdxJs = viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return Result.CONTINUE
    if (!DialectDetector.isJSX(mdxJs)) return Result.CONTINUE

    val offset = editor.caretModel.offset
    val text = editor.document.charsSequence
    val closingTag = closingTagToInsert(text, offset) ?: return Result.CONTINUE

    // Don't insert twice if an earlier handler in the chain already added it.
    if (text.subSequence(offset, text.length).startsWith(closingTag)) return Result.DEFAULT

    // Insert without moving the caret, then DEFAULT so the typed `>` still lands normally between it and
    // the close, but no later delegate in the chain (e.g. JavaScriptTypedHandler, registered "after" this
    // one) also tries to insert its own closing tag for the same keystroke. WEB-78468.
    EditorModificationUtilEx.insertStringAtCaret(editor, closingTag, false, 0)
    return Result.DEFAULT
  }

  private fun closingTagToInsert(text: CharSequence, caretOffset: Int): String? {
    if (caretOffset <= 0 || caretOffset > text.length) return null
    val tagStart = findTagStart(text, caretOffset) ?: return null
    if (text.getOrNull(tagStart + 1) == '/') return null
    if (tagStart == caretOffset - 1) return "</>"

    var nameEnd = tagStart + 1
    if (!isJsxNameStart(text.getOrNull(nameEnd))) return null
    nameEnd++
    while (nameEnd < caretOffset && isJsxNamePart(text[nameEnd])) {
      nameEnd++
    }
    val name = text.subSequence(tagStart + 1, nameEnd).toString()
    if (caretInsideTagExpressionOrQuote(text, nameEnd, caretOffset)) return null
    if (text.getOrNull(caretOffset - 1) == '/') return null
    if (HtmlUtil.isSingleHtmlTag(name, true)) return null
    return "</$name>"
  }

  /**
   * Walks back over complete expression and quoted attribute values so their internal tag-like
   * characters do not hide the opening `<`.
   */
  private fun findTagStart(text: CharSequence, caretOffset: Int): Int? {
    var index = caretOffset - 1
    while (index >= 0) {
      when (text[index]) {
        '}' -> index = (matchingExpressionStart(text, index) ?: return null) - 1
        '\'', '"', '`' -> index = (matchingQuoteStart(text, index) ?: return null) - 1
        '>' -> return null
        '<' -> return index
        '\n' -> return null
        else -> index--
      }
    }
    return null
  }

  private fun caretInsideTagExpressionOrQuote(text: CharSequence, from: Int, caretOffset: Int): Boolean {
    var offset = from
    while (offset < caretOffset) {
      when (text[offset]) {
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, text.length)
          if (expressionEnd == -1 || caretOffset < expressionEnd) return true
          offset = expressionEnd
        }
        '\'', '"' -> {
          val quoteEnd = scanQuoted(text, offset, text.length, text[offset])
          if (quoteEnd == -1 || caretOffset < quoteEnd) return true
          offset = quoteEnd
        }
        else -> offset++
      }
    }
    return false
  }

  private fun matchingExpressionStart(text: CharSequence, closeBrace: Int): Int? {
    var depth = 0
    var offset = closeBrace
    while (offset >= 0) {
      when (text[offset]) {
        '}' -> depth++
        '{' -> {
          depth--
          if (depth == 0) {
            return if (MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, text.length) == closeBrace + 1) offset else null
          }
        }
      }
      offset--
    }
    return null
  }

  private fun matchingQuoteStart(text: CharSequence, closeQuote: Int): Int? {
    val quote = text[closeQuote]
    var offset = closeQuote - 1
    while (offset >= 0) {
      if (text[offset] == '\n') return null
      if (text[offset] == quote && scanQuoted(text, offset, text.length, quote) == closeQuote + 1) return offset
      offset--
    }
    return null
  }

  private fun scanQuoted(text: CharSequence, start: Int, limit: Int, quote: Char): Int {
    var offset = start + 1
    while (offset < limit) {
      when (text[offset]) {
        '\\' -> offset += 2
        quote -> return offset + 1
        else -> offset++
      }
    }
    return -1
  }

  private fun isJsxNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_')
  }

  private fun isJsxNamePart(char: Char): Boolean {
    return char.isLetterOrDigit() || char == '_' || char == '-' || char == '.' || char == ':'
  }
}
