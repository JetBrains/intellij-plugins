package org.intellij.plugin.mdx.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils
import org.intellij.plugins.markdown.lang.MarkdownElementType

/**
 * Enter inside an MDX code fence, replayed in a sandbox of the fence language (see [MdxCodeFenceSandbox]).
 * Registered order="first" so it wins over the platform XML enter handler, which would otherwise expand
 * tags directly on the injected fragment.
 */
internal class MdxEnterHandler : EnterHandlerDelegate {
  override fun preprocessEnter(file: PsiFile,
                               editor: Editor,
                               caretOffset: Ref<Int>,
                               caretAdvance: Ref<Int>,
                               dataContext: DataContext,
                               originalHandler: EditorActionHandler?): Result {
    if (MdxCodeFenceSandbox.replay(editor, "EditorEnter")) return Result.Stop
    if (insertLineInsideOpaqueFence(file, editor)) return Result.Stop
    if (insertLineAfterFenceInFlow(file, editor)) return Result.Stop
    if (insertLineBetweenEmptyJsxTagsInEsmBlock(file, editor)) return Result.Stop
    if (insertLineSplittingJsxText(file, editor)) return Result.Stop
    return Result.Continue
  }

  /**
   * Handles Enter inside a fence the sandbox above could not replay — one whose info string names no language or
   * a language with no injection support, and any caret on the fence's own opening line — by carrying the current
   * line's indentation over itself. On the opening line that indentation is the fence's own, which is exactly what
   * the first body line needs.
   */
  private fun insertLineInsideOpaqueFence(file: PsiFile, editor: Editor): Boolean {
    if (editor.caretModel.caretCount != 1) return false
    val mdxFile = file.viewProvider.allFiles.firstOrNull { it is MdxFile } ?: return false

    val document = editor.document
    val caret = editor.caretModel.offset
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)

    val fence = MarkdownCodeFenceUtils.getCodeFence(mdxFile.findElementAt((caret - 1).coerceAtLeast(0)) ?: return false)
                ?: return false
    // Anywhere within the fence, including its opening line — Enter there opens the first body line and needs the
    // same treatment. Past the closing backticks belongs to insertLineAfterFenceInFlow instead.
    if (caret <= fence.textRange.startOffset || caret >= fence.textRange.endOffset) return false

    val line = document.getLineNumber(caret)
    val lineEnd = document.getLineEndOffset(line)
    val indent = " ".repeat(leadingSpaces(document.charsSequence, document.getLineStartOffset(line)))

    if (caret == lineEnd && lineEnd < document.textLength) {
      document.insertString(lineEnd + 1, "$indent\n")
    }
    else {
      document.insertString(caret, "\n$indent")
    }
    editor.caretModel.moveToOffset(caret + 1 + indent.length)
    return true
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

  /**
   * Handles Enter between an empty JSX tag pair (`<div><caret></div>`) nested inside an ESM statement's
   * function/expression body: MdxFormattingModelBuilder deliberately treats the whole MDX_ESM_BLOCK as one
   * opaque leaf (to leave import/export syntax untouched), so it has no structural indent info for the
   * nested tags and the platform's default Enter handling drops the closing tag to column 0 instead of
   * aligning it with the opening tag's line.
   */
  private fun insertLineBetweenEmptyJsxTagsInEsmBlock(file: PsiFile, editor: Editor): Boolean {
    if (editor.caretModel.caretCount != 1) return false
    val document = editor.document
    val caret = editor.caretModel.offset
    if (caret == 0 || document.charsSequence[caret - 1] != '>') return false

    val mdxFile = file.viewProvider.allFiles.firstOrNull { it is MdxFile } ?: return false
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)

    val jsFile = file.viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return false
    val tag = emptyJsxTagEndingAt(jsFile, caret) ?: return false
    if (!isInsideEsmBlock(mdxFile, tag.textRange.startOffset)) return false

    val lineStart = document.getLineStartOffset(document.getLineNumber(caret))
    val baseIndent = " ".repeat(leadingSpaces(document.charsSequence, lineStart))
    val childIndent = baseIndent + " ".repeat(CodeStyle.getIndentSize(jsFile))
    document.insertString(caret, "\n$childIndent\n$baseIndent")
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)
    editor.caretModel.moveToOffset(caret + 1 + childIndent.length)
    return true
  }

  private fun emptyJsxTagEndingAt(jsFile: PsiFile, offset: Int): XmlTag? {
    val tag = PsiTreeUtil.getParentOfType(jsFile.findElementAt(offset - 1), XmlTag::class.java) ?: return null
    if (tag.isEmpty) return null // self-closing <div/>: no separate closing tag to split away from
    val value = tag.value.textRange
    return if (value.isEmpty && value.startOffset == offset) tag else null
  }

  // The offset sits inside embedded JS/JSX content, which PsiFile.findElementAt would resolve straight
  // into the foreign JS tree (the host's OUTER_ELEMENT_TYPE delegation) instead of the host AST — so this
  // walks the raw host ASTNode tree directly to see whether an MDX_ESM_BLOCK actually covers the offset.
  private fun isInsideEsmBlock(mdxFile: PsiFile, offset: Int): Boolean {
    val esmBlockType = MarkdownElementType.platformType(MdxElementTypes.MDX_ESM_BLOCK)
    var node = mdxFile.node?.findLeafElementAt(offset)
    while (node != null) {
      if (node.elementType === esmBlockType) return true
      node = node.treeParent
    }
    return false
  }

  /**
   * Handles Enter splitting an existing run of JSX text (e.g. `<div>ab<caret>cd</div>`): the platform's
   * default Enter routes the split through the XML/JS formatter's adjustLineIndent, which indents the moved
   * half one level too deep once any other reformat has already run earlier in the session — a stateful bug
   * in the platform's generic template-language indent-resolution machinery (MdxBlock never contributes an
   * indent of its own for this content; it defers entirely to the foreign XmlTagBlock). Inserting the line
   * here, copying the split line's own indentation, sidesteps that path entirely. WEB-78468.
   */
  private fun insertLineSplittingJsxText(file: PsiFile, editor: Editor): Boolean {
    if (editor.caretModel.caretCount != 1) return false
    val document = editor.document
    val caret = editor.caretModel.offset

    val mdxFile = file.viewProvider.allFiles.firstOrNull { it is MdxFile } ?: return false
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)

    val jsFile = file.viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return false
    val xmlText = PsiTreeUtil.getParentOfType(jsFile.findElementAt(caret - 1), XmlText::class.java) ?: return false
    if (caret <= xmlText.textRange.startOffset || caret >= xmlText.textRange.endOffset) return false

    val lineStart = document.getLineStartOffset(document.getLineNumber(caret))
    val indent = " ".repeat(leadingSpaces(document.charsSequence, lineStart))
    document.insertString(caret, "\n$indent")
    PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)
    editor.caretModel.moveToOffset(caret + 1 + indent.length)
    return true
  }

  private fun leadingSpaces(text: CharSequence, lineStart: Int): Int {
    var offset = lineStart
    while (offset < text.length && text[offset] == ' ') offset++
    return offset - lineStart
  }
}
