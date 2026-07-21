package org.intellij.plugin.mdx.editor

import com.intellij.injected.editor.DocumentWindow
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils
import org.intellij.plugins.markdown.injection.aliases.CodeFenceLanguageGuesser

/**
 * Replays an editor action that lands inside an MDX code fence in a throwaway file of the fence language.
 *
 * Editing the injected fence fragment directly is unreliable, since the injected document is a standalone
 * 0-based file whose formatter/indenter fights the host indentation. Instead the fence body is copied into
 * a standalone file of the fence language (dedented to column zero, since indented input confuses the
 * language formatter), the same action is replayed there, and the result is written back re-indented, with
 * caret and selection mapped both ways.
 */
internal object MdxCodeFenceSandbox {
  /**
   * If the caret (and any selection) sits inside an MDX code fence body, runs [actionId] against a sandbox of
   * the fence language and writes the result back, returning true. Returns false if the action does not apply
   * (no fence, multiple carets, selection crossing the fence boundary, ...) so the caller can fall back.
   */
  fun replay(editor: Editor, actionId: String): Boolean {
    val project = editor.project ?: return false
    if (!editor.document.isWritable || editor.isViewer || editor.caretModel.caretCount != 1) return false

    val hostEditor = (editor as? EditorWindow)?.delegate ?: editor
    val hostDocument = hostEditor.document
    val documentManager = PsiDocumentManager.getInstance(project)
    documentManager.commitDocument(hostDocument)
    val hostFile = documentManager.getPsiFile(hostDocument) as? MdxFile ?: return false

    val documentWindow = editor.document as? DocumentWindow
    fun toHost(offset: Int) = documentWindow?.injectedToHost(offset) ?: offset
    val caret = toHost(editor.caretModel.offset)
    val selectionStart = toHost(editor.selectionModel.selectionStart)
    val selectionEnd = toHost(editor.selectionModel.selectionEnd)

    val element = hostFile.findElementAt((caret - 1).coerceAtLeast(0)) ?: return false
    val fence = MarkdownCodeFenceUtils.getCodeFence(element) ?: return false
    val languageAndExtension = CodeFenceLanguageGuesser.guessLanguageWithExtensionForInjection(fence.fenceLanguage ?: return false)
                               ?: return false

    val startLine = hostDocument.getLineNumber(fence.textRange.startOffset)
    val endLine = hostDocument.getLineNumber((fence.textRange.endOffset - 1).coerceAtLeast(fence.textRange.startOffset))
    if (endLine <= startLine) return false
    val contentStart = hostDocument.getLineStartOffset(startLine + 1)
    val contentEnd = hostDocument.getLineEndOffset(endLine - 1)
    if (caret !in contentStart..contentEnd || selectionStart < contentStart || selectionEnd > contentEnd) return false

    val content = hostDocument.getText(TextRange(contentStart, contentEnd))
    val base = commonIndent(content)
    val offsets = intArrayOf(caret - contentStart, selectionStart - contentStart, selectionEnd - contentStart)
    val (dedented, dedentedOffsets) = shift(content, offsets, -base)

    val extension = languageAndExtension.second ?: languageAndExtension.first.associatedFileType?.defaultExtension ?: "txt"
    val file = PsiFileFactory.getInstance(project)
      .createFileFromText("fence.$extension", languageAndExtension.first, dedented)
    val sandboxDocument = documentManager.getDocument(file) ?: return false
    val sandboxEditor = EditorFactory.getInstance().createEditor(sandboxDocument, project, file.viewProvider.virtualFile, false)
    try {
      val length = sandboxDocument.textLength
      sandboxEditor.caretModel.moveToOffset(dedentedOffsets[0].coerceIn(0, length))
      if (dedentedOffsets[1] != dedentedOffsets[2]) {
        sandboxEditor.selectionModel.setSelection(dedentedOffsets[1].coerceIn(0, length), dedentedOffsets[2].coerceIn(0, length))
      }
      val sandboxContext = DataContext { dataId ->
        when (dataId) {
          CommonDataKeys.EDITOR.name -> sandboxEditor
          CommonDataKeys.PSI_FILE.name -> file
          CommonDataKeys.PROJECT.name -> project
          else -> null
        }
      }
      // The action handler asserts a current command; one is active in the intercepted action, so this names it.
      CommandProcessor.getInstance().executeCommand(project, {
        EditorActionManager.getInstance().getActionHandler(actionId)
          .execute(sandboxEditor, sandboxEditor.caretModel.currentCaret, sandboxContext)
      }, null, null)
      documentManager.commitDocument(sandboxDocument)

      val resultOffsets = intArrayOf(sandboxEditor.caretModel.offset,
                                     sandboxEditor.selectionModel.selectionStart,
                                     sandboxEditor.selectionModel.selectionEnd)
      val (reindented, hostOffsets) = shift(sandboxDocument.text, resultOffsets, base)
      hostDocument.replaceString(contentStart, contentEnd, reindented)
      documentManager.commitDocument(hostDocument)
      hostEditor.caretModel.moveToOffset(contentStart + hostOffsets[0])
      if (hostOffsets[1] != hostOffsets[2]) {
        hostEditor.selectionModel.setSelection(contentStart + hostOffsets[1], contentStart + hostOffsets[2])
      }
    }
    finally {
      EditorFactory.getInstance().releaseEditor(sandboxEditor)
    }
    return true
  }

  /** Minimum leading-space indentation across the non-blank lines of [content]. */
  private fun commonIndent(content: String): Int =
    content.split('\n').filter { it.isNotBlank() }.minOfOrNull { line -> line.takeWhile { it == ' ' }.length } ?: 0

  /**
   * Shifts the indentation of every line by [delta] spaces — negative removes leading spaces (dedent), positive
   * prepends them to non-blank lines (and lines bearing a tracked offset). Returns the shifted text and the
   * given [offsets] mapped into it.
   */
  private fun shift(text: String, offsets: IntArray, delta: Int): Pair<String, IntArray> {
    if (delta == 0) return text to offsets
    val indent = if (delta > 0) " ".repeat(delta) else ""
    val out = StringBuilder()
    val mapped = offsets.copyOf()
    var lineStart = 0
    for ((index, line) in text.split('\n').withIndex()) {
      if (index > 0) out.append('\n')
      val bearsOffset = offsets.any { it in lineStart..(lineStart + line.length) }
      val leading = line.takeWhile { it == ' ' }.length
      val removed = if (delta < 0) minOf(-delta, leading) else 0
      val prepend = delta > 0 && (line.isNotBlank() || bearsOffset)
      val base = out.length
      for ((k, offset) in offsets.withIndex()) {
        if (offset in lineStart..(lineStart + line.length)) {
          val column = offset - lineStart
          mapped[k] = base + if (delta > 0) column + (if (prepend) delta else 0) else maxOf(0, column - removed)
        }
      }
      if (prepend) out.append(indent)
      out.append(if (delta < 0) line.substring(removed) else if (line.isBlank() && !bearsOffset) "" else line)
      lineStart += line.length + 1
    }
    return out.toString() to mapped
  }
}
