package com.intellij.mdx.backend.format

import com.intellij.application.options.CodeStyle
import com.intellij.lang.Language
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.injection.aliases.CodeFenceLanguageGuesser
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFence

/**
 * Formats a code fence body by round-tripping it through a temporary standalone file of the fence
 * language (like [MdxTablePostFormatProcessor] does for GFM tables), rather than relying on the more
 * fragile injection-based formatting.
 */
internal class MdxCodeFencePostFormatProcessor : PostFormatProcessor {
  override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
    val file = source.containingFile as? MdxFile ?: return source
    val document = file.viewProvider.document ?: return source
    val fences = if (source is MarkdownCodeFence) listOf(source)
    else PsiTreeUtil.findChildrenOfType(source, MarkdownCodeFence::class.java).toList()
    processFences(fences, file, document)
    return source
  }

  override fun processText(source: PsiFile, rangeToReformat: TextRange, settings: CodeStyleSettings): TextRange {
    if (source !is MdxFile) return rangeToReformat
    val document = source.viewProvider.document ?: return rangeToReformat
    val fences = PsiTreeUtil.findChildrenOfType(source, MarkdownCodeFence::class.java)
      .filter { rangeToReformat.intersects(it.textRange) }
    processFences(fences.toList(), source, document)
    return source.textRange
  }

  private fun processFences(fences: List<MarkdownCodeFence>, file: MdxFile, document: Document) {
    // Replacing a fence body wholesale while a live template is running inside it (e.g. right after a
    // 'do' keyword completion starts a template) can throw PatchException, since the template's
    // not-yet-parsed content has no counterpart in the injected PSI to patch onto. Simplest safe option:
    // skip the rewrite and let the next non-template-triggered reformat pick the fence up.
    if (isReformattingLiveTemplate()) return
    val documentManager = PsiDocumentManager.getInstance(file.project)
    documentManager.commitDocument(document)
    // Bottom-up so replacing one fence does not shift the offsets of the fences above it.
    for (fence in fences.sortedByDescending { it.textRange.startOffset }) {
      val startLine = document.getLineNumber(fence.textRange.startOffset)
      val endLine = document.getLineNumber((fence.textRange.endOffset - 1).coerceAtLeast(fence.textRange.startOffset))
      val replacement = buildFormattedFence(fence, file, document, startLine, endLine) ?: continue
      val start = document.getLineStartOffset(startLine)
      val end = document.getLineEndOffset(endLine)
      if (document.getText(TextRange(start, end)) == replacement) continue
      document.replaceString(start, end, replacement)
      documentManager.commitDocument(document)
    }
  }

  /** Whether we are running inside [com.intellij.codeInsight.template.impl.TemplateState.doReformat]. */
  private fun isReformattingLiveTemplate(): Boolean =
    Thread.currentThread().stackTrace.any {
      it.className == "com.intellij.codeInsight.template.impl.TemplateState" && it.methodName == "doReformat"
    }

  private fun buildFormattedFence(fence: MarkdownCodeFence, file: MdxFile, document: Document, startLine: Int, endLine: Int): String? {
    if (endLine <= startLine) return null // opener and closer must be on different lines
    val info = fence.fenceLanguage ?: return null
    val language = CodeFenceLanguageGuesser.guessLanguageForInjection(info) ?: return null

    val lineText = { line: Int ->
      document.charsSequence.subSequence(document.getLineStartOffset(line), document.getLineEndOffset(line)).toString()
    }
    val opener = lineText(startLine).trim()
    val closer = lineText(endLine).trim()
    val contentLines = (startLine + 1 until endLine).map { lineText(it) }

    val baseIndent = " ".repeat(jsxDepth(fence) * CodeStyle.getIndentOptions(file).INDENT_SIZE)
    // Formatting "" would yield a spurious blank line between the fences, so keep an empty body empty.
    val formatted = if (contentLines.isEmpty()) emptyList() else formatCode(file, language, dedent(contentLines))

    val builder = StringBuilder(baseIndent).append(opener)
    for (line in formatted) {
      builder.append('\n')
      if (line.isNotEmpty()) builder.append(baseIndent)
      builder.append(line)
    }
    builder.append('\n').append(baseIndent).append(closer)
    return builder.toString()
  }

  /** Number of MDX JSX flow elements the fence is nested in; each level adds one indent step. */
  private fun jsxDepth(fence: MarkdownCodeFence): Int {
    val flowType = MdxElementTypes.MDX_JSX_FLOW_ELEMENT
    var depth = 0
    var parent = fence.parent
    while (parent != null) {
      if (parent.elementType == flowType) depth++
      parent = parent.parent
    }
    return depth
  }

  /** Removes the common leading whitespace from [lines] so the sandbox formatter starts at column zero. */
  private fun dedent(lines: List<String>): List<String> {
    val common = lines.filter { it.isNotBlank() }.minOfOrNull { it.takeWhile { c -> c == ' ' || c == '\t' }.length } ?: 0
    return lines.map { if (it.length >= common) it.substring(common) else it.trimStart() }
  }

  private fun formatCode(file: MdxFile, language: Language, lines: List<String>): List<String> {
    val code = lines.joinToString("\n")
    val sandbox = PsiFileFactory.getInstance(file.project).createFileFromText("fence.dummy", language, code)
    CodeStyleManager.getInstance(file.project).reformat(sandbox)
    return sandbox.text.split("\n")
  }
}
