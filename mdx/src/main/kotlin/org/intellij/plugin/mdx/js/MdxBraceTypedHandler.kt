package org.intellij.plugin.mdx.js

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtilEx
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.plugin.mdx.lang.psi.MdxFileViewProvider
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner

/**
 * Auto-closes `{` to `{}` when typed inside a JSX element body in an MDX file. The standard JS
 * `{`→`{}` handler gates on the base file being a JS dialect, which MDX (template-data, not
 * injection) never is, so this bridges the gap by detecting the JSX-body context directly.
 */
internal class MdxBraceTypedHandler : TypedHandlerDelegate() {
  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    if (c != '{') return Result.CONTINUE
    if (file.viewProvider !is MdxFileViewProvider) return Result.CONTINUE

    val offset = editor.caretModel.offset
    val text = editor.document.charsSequence

    // Don't double-close: if `}` is already the next character, skip.
    if (offset < text.length && text[offset] == '}') return Result.CONTINUE

    // Scan document text rather than PSI: the platform may pass the MdxJS virtual file for the caret's context.
    if (!isInsideJsxElementBody(text, offset)) return Result.CONTINUE

    EditorModificationUtilEx.insertStringAtCaret(editor, "}", false, 0)
    return Result.CONTINUE
  }

  // True if the nearest `>` closing a line-start JSX opening tag before [offset] has no matching
  // `</tag>` between it and [offset].
  private fun isInsideJsxElementBody(text: CharSequence, offset: Int): Boolean {
    var depth = 0
    var i = offset - 1
    while (i >= 0) {
      if (text[i] == '>') {
        val ltPos = findMatchingLt(text, i)
        if (ltPos >= 0) {
          if (ltPos + 1 < text.length && text[ltPos + 1] == '/') {
            depth++  // closing tag </...>
          } else if (i > 0 && text[i - 1] == '/') {
            // self-closing <.../>: skip
          } else if (depth > 0) {
            depth--
          } else if (MdxJsxScanner.isLineStartJsx(text, ltPos)) {
            return true
          }
          i = ltPos - 1
          continue
        }
      }
      i--
    }
    return false
  }

  private fun findMatchingLt(text: CharSequence, gtPos: Int): Int {
    var i = gtPos - 1
    while (i >= 0) {
      if (text[i] == '<') return i
      if (text[i] == '\n') break  // don't scan across lines
      i--
    }
    return -1
  }
}
