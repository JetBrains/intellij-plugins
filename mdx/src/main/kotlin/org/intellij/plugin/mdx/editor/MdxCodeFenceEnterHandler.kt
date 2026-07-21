package org.intellij.plugin.mdx.editor

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils
import org.intellij.plugins.markdown.lang.MarkdownElementType

/**
 * Enter inside an MDX code fence, replayed in a sandbox of the fence language (see [MdxCodeFenceSandbox]).
 * Registered order="first" so it wins over the platform XML enter handler, which would otherwise expand
 * tags directly on the injected fragment.
 */
internal class MdxCodeFenceEnterHandler : EnterHandlerDelegate {
  override fun preprocessEnter(file: PsiFile,
                               editor: Editor,
                               caretOffset: Ref<Int>,
                               caretAdvance: Ref<Int>,
                               dataContext: DataContext,
                               originalHandler: EditorActionHandler?): Result {
    if (MdxCodeFenceSandbox.replay(editor, "EditorEnter")) return Result.Stop
    if (insertLineAfterFenceInFlow(file, editor)) return Result.Stop
    return Result.Continue
  }

  /**
   * Handles Enter right after a code fence nested in a JSX flow element: such a fence is projected as an
   * OUTER block inside a real XmlTag, so the platform's default Enter routes through the XML formatter and
   * indents the new line one level too deep. Inserting the newline here at the fence's own column stops
   * that over-indenting default from running.
   */
  private fun insertLineAfterFenceInFlow(file: PsiFile, editor: Editor): Boolean {
    if (editor.caretModel.caretCount != 1) return false
    // [file] may be the MdxJS (JS) view; reach the MDX host root through the shared view provider.
    val mdxFile = file.viewProvider.allFiles.firstOrNull { it is MdxFile } ?: return false

    val document = editor.document
    val caret = editor.caretModel.offset
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)

    val fence = MarkdownCodeFenceUtils.getCodeFence(mdxFile.findElementAt((caret - 1).coerceAtLeast(0)) ?: return false)
                ?: return false
    if (caret < fence.textRange.endOffset) return false // caret still inside the fence body (sandbox handles it)
    // The caret must be at the end of the fence's closing line (only trailing spaces after the closer).
    if (document.charsSequence.subSequence(fence.textRange.endOffset, caret).any { it != ' ' && it != '\t' }) return false
    if (!isInsideFlowElement(fence)) return false // top-level fences already indent correctly

    val fenceLineStart = document.getLineStartOffset(document.getLineNumber(fence.textRange.startOffset))
    val indent = " ".repeat(leadingSpaces(document.charsSequence, fenceLineStart))
    document.insertString(caret, "\n$indent")
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)
    editor.caretModel.moveToOffset(caret + 1 + indent.length)
    return true
  }

  private fun isInsideFlowElement(element: PsiElement): Boolean {
    val flowType = MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_FLOW_ELEMENT)
    var parent = element.parent
    while (parent != null) {
      if (parent.node?.elementType === flowType) return true
      parent = parent.parent
    }
    return false
  }

  private fun leadingSpaces(text: CharSequence, lineStart: Int): Int {
    var offset = lineStart
    while (offset < text.length && text[offset] == ' ') offset++
    return offset - lineStart
  }
}
