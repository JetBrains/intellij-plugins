package org.intellij.plugin.mdx.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.Result
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils

/** Registered order="first": must win over the platform XML enter handler, which would otherwise expand tags directly on the injected fragment. */
internal class MdxEnterHandler : EnterHandlerDelegate {
  private val rules: List<(MdxEnterContext) -> Boolean> = listOf(
    ::insertLineAfterFenceInFlow,
    ::insertLineBetweenEmptyJsxTagsInEsmBlock,
    ::insertLineSplittingJsxText,
  )

  override fun preprocessEnter(file: PsiFile,
                               editor: Editor,
                               caretOffset: Ref<Int>,
                               caretAdvance: Ref<Int>,
                               dataContext: DataContext,
                               originalHandler: EditorActionHandler?): Result {
    if (MdxCodeFenceSandbox.replay(editor, "EditorEnter")) return Result.Stop
    val context = MdxEnterContext.resolve(file, editor) ?: return Result.Continue
    return if (rules.any { it(context) }) Result.Stop else Result.Continue
  }

  /** Enter right after a fence closes inside a JSX flow element: the default XML formatter would over-indent it a level. */
  private fun insertLineAfterFenceInFlow(context: MdxEnterContext): Boolean {
    val document = context.document
    val caret = context.caret

    val fence = MarkdownCodeFenceUtils.getCodeFence(context.mdxFile.findElementAt((caret - 1).coerceAtLeast(0)) ?: return false)
                ?: return false
    if (caret < fence.textRange.endOffset) return false // still inside the fence body (sandbox handles it)
    if (document.charsSequence.subSequence(fence.textRange.endOffset, caret).any { it != ' ' && it != '\t' }) return false
    if (!isInsideFlowElement(fence)) return false // top-level fences already indent correctly

    val fenceLineStart = document.getLineStartOffset(document.getLineNumber(fence.textRange.startOffset))
    val indent = " ".repeat(leadingSpaces(document.charsSequence, fenceLineStart))
    context.insertAndMoveCaret("\n$indent", 1 + indent.length)
    return true
  }

  private fun isInsideFlowElement(element: PsiElement): Boolean {
    val flowType = MdxElementTypes.MDX_JSX_FLOW_ELEMENT
    var parent = element.parent
    while (parent != null) {
      if (parent.node?.elementType === flowType) return true
      parent = parent.parent
    }
    return false
  }

  /** `<div><caret></div>` inside an ESM block: MDX_ESM_BLOCK is an opaque leaf, so the default handling drops the closing tag to column 0. */
  private fun insertLineBetweenEmptyJsxTagsInEsmBlock(context: MdxEnterContext): Boolean {
    val document = context.document
    val caret = context.caret
    if (caret == 0 || document.charsSequence[caret - 1] != '>') return false

    val jsFile = context.jsFile ?: return false
    val tag = emptyJsxTagEndingAt(jsFile, caret) ?: return false
    if (!isInsideEsmBlock(context.mdxFile, tag.textRange.startOffset)) return false

    val lineStart = document.getLineStartOffset(document.getLineNumber(caret))
    val baseIndent = " ".repeat(leadingSpaces(document.charsSequence, lineStart))
    val childIndent = baseIndent + " ".repeat(CodeStyle.getIndentSize(jsFile))
    context.insertAndMoveCaret("\n$childIndent\n$baseIndent", 1 + childIndent.length)
    return true
  }

  private fun emptyJsxTagEndingAt(jsFile: PsiFile, offset: Int): XmlTag? {
    val tag = PsiTreeUtil.getParentOfType(jsFile.findElementAt(offset - 1), XmlTag::class.java) ?: return null
    if (tag.isEmpty) return null // self-closing <div/>: no separate closing tag to split away from
    val value = tag.value.textRange
    return if (value.isEmpty && value.startOffset == offset) tag else null
  }

  // walks the raw host AST, not PSI: PsiFile.findElementAt here would resolve straight into the foreign JS tree
  private fun isInsideEsmBlock(mdxFile: PsiFile, offset: Int): Boolean {
    val esmBlockType = MdxElementTypes.MDX_ESM_BLOCK
    var node = mdxFile.node?.findLeafElementAt(offset)
    while (node != null) {
      if (node.elementType === esmBlockType) return true
      node = node.treeParent
    }
    return false
  }

  /** Enter splitting a run of JSX text (`<div>ab<caret>cd</div>`): the default XML/JS formatter over-indents it a level once any reformat has already run earlier in the session (WEB-78468) — inserting the line directly sidesteps that. */
  private fun insertLineSplittingJsxText(context: MdxEnterContext): Boolean {
    val document = context.document
    val caret = context.caret

    val jsFile = context.jsFile ?: return false
    val before = jsFile.findElementAt(caret - 1) ?: return false
    val after = jsFile.findElementAt(caret) ?: return false
    val owner = jsxTextRunOwner(before) ?: return false
    if (jsxTextRunOwner(after) != owner) return false

    val lineStart = document.getLineStartOffset(document.getLineNumber(caret))
    val indent = " ".repeat(leadingSpaces(document.charsSequence, lineStart))
    context.insertAndMoveCaret("\n$indent", 1 + indent.length)
    return true
  }

  // Text runs split into separate XmlText siblings around embedded whitespace, with the whitespace itself a
  // standalone sibling belonging to neither — so caret needs matching owners on both sides, not one XmlText's interior.
  private fun jsxTextRunOwner(element: PsiElement): PsiElement? {
    PsiTreeUtil.getParentOfType(element, XmlText::class.java)?.let { return it.parent }
    return (element as? PsiWhiteSpace)?.parent
  }

  private fun leadingSpaces(text: CharSequence, lineStart: Int): Int {
    var offset = lineStart
    while (offset < text.length && text[offset] == ' ') offset++
    return offset - lineStart
  }
}

private class MdxEnterContext private constructor(
  val editor: Editor,
  val document: Document,
  val mdxFile: MdxFile,
  val caret: Int,
) {
  val project: Project get() = mdxFile.project
  val jsFile: PsiFile? by lazy { mdxFile.viewProvider.getPsi(MdxJSLanguage.INSTANCE) }

  companion object {
    fun resolve(file: PsiFile, editor: Editor): MdxEnterContext? {
      if (editor.caretModel.caretCount != 1) return null
      val mdxFile = file.viewProvider.allFiles.firstOrNull { it is MdxFile } as? MdxFile ?: return null
      val document = editor.document
      PsiDocumentManager.getInstance(mdxFile.project).commitDocument(document)
      return MdxEnterContext(editor, document, mdxFile, editor.caretModel.offset)
    }
  }
}

private fun MdxEnterContext.insertAndMoveCaret(text: String, caretOffset: Int, at: Int = caret) {
  document.insertString(at, text)
  PsiDocumentManager.getInstance(project).commitDocument(document)
  editor.caretModel.moveToOffset(at + caretOffset)
}
