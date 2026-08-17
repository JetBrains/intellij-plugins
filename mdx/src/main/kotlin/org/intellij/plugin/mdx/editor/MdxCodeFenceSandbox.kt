package org.intellij.plugin.mdx.editor

import com.intellij.ide.DataManager
import com.intellij.injected.editor.DocumentWindow
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.CustomizedDataContext
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextLanguage
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
    val guessedLanguageAndExtension = fence.fenceLanguage?.let(CodeFenceLanguageGuesser::guessLanguageWithExtensionForInjection)
    val languageAndExtension = guessedLanguageAndExtension
                               ?: (PlainTextLanguage.INSTANCE to "text")
    val isPlainTextFallback = guessedLanguageAndExtension == null

    val startLine = hostDocument.getLineNumber(fence.textRange.startOffset)
    val endLine = hostDocument.getLineNumber((fence.textRange.endOffset - 1).coerceAtLeast(fence.textRange.startOffset))

    val hasBody = endLine > startLine + 1

    val contentStart: Int
    val contentEnd: Int
    if (hasBody) {
      contentStart = hostDocument.getLineStartOffset(startLine + 1)
      contentEnd = hostDocument.getLineEndOffset(endLine - 1)
      if (caret !in contentStart..contentEnd || selectionStart < contentStart || selectionEnd > contentEnd) return false
    }
    else {
      // past the closing backticks, or before the fence starts, belongs to whatever runs after this delegate
      if (caret <= fence.textRange.startOffset || caret >= fence.textRange.endOffset) return false
      if (selectionStart != caret || selectionEnd != caret) return false // nothing to sandbox a real selection into
      contentStart = caret
      contentEnd = caret
    }

    val content = if (hasBody) hostDocument.getText(TextRange(contentStart, contentEnd)) else ""
    // A body with no code in it yet — an empty fence, or just the blank line the caret sits on — carries no
    // indentation to infer the base from, so it comes from the fence's own opening line instead (or, with no
    // body at all, the caret's own current line — on the opening line that's the same line). Otherwise the base
    // would be zero, shift() would be a no-op both ways, and the replayed action's output (indented from column
    // zero, as the sandbox file is) would be written straight back at column zero.
    val bodyIndent = commonIndent(content)
    val preservesHostIndentation = isPlainTextFallback && actionId == "EditorEnter" &&
      bodyIndent != null && lineAt(content, caret - contentStart).isBlank()
    val base = if (preservesHostIndentation) 0
               else bodyIndent ?: lineIndent(hostDocument, if (hasBody) startLine else hostDocument.getLineNumber(caret))
    val offsets = intArrayOf(caret - contentStart, selectionStart - contentStart, selectionEnd - contentStart)
    val (dedented, dedentedOffsets) = shift(content, offsets, -base, dedentBlankLines = bodyIndent == null)

    val extension = languageAndExtension.second ?: languageAndExtension.first.associatedFileType?.defaultExtension ?: "text"
    val file = PsiFileFactory.getInstance(project)
      .createFileFromText("fence.$extension", languageAndExtension.first, dedented)
    val sandboxDocument = documentManager.getDocument(file) ?: return false
    val virtualFile = file.viewProvider.virtualFile
    val sandboxEditor = EditorFactory.getInstance().createEditor(sandboxDocument, project, virtualFile, false)
    // createEditor(document, project, file, isViewer) uses `file` only to build the highlighter — it does NOT set
    // the editor's own virtualFile. Set it so the sandbox is a genuine file-backed editor: language line-indent
    // providers dereference editor.virtualFile and would otherwise crash on Enter (TextMate fences NPE). WEB-78468.
    (sandboxEditor as EditorEx).setFile(virtualFile)
    try {
      val length = sandboxDocument.textLength
      sandboxEditor.caretModel.moveToOffset(dedentedOffsets[0].coerceIn(0, length))
      if (dedentedOffsets[1] != dedentedOffsets[2]) {
        sandboxEditor.selectionModel.setSelection(dedentedOffsets[1].coerceIn(0, length), dedentedOffsets[2].coerceIn(0, length))
      }
      // Built on a DataManager context rather than as a bare DataContext lambda: dispatching without a caret makes
      // the platform re-wrap the context (per caret, and again inside the paste handler), and it only knows how to
      // snapshot context kinds it produced itself. The sandbox editor's own component supplies little beyond the
      // editor, so the keys the replayed handlers read are set explicitly.
      val sandboxContext = CustomizedDataContext.withSnapshot(
        DataManager.getInstance().getDataContext(sandboxEditor.contentComponent)
      ) { sink ->
        sink[CommonDataKeys.EDITOR] = sandboxEditor
        sink[CommonDataKeys.PSI_FILE] = file
        sink[CommonDataKeys.PROJECT] = project
      }
      // The action handler asserts a current command; one is active in the intercepted action, so this names it.
      CommandProcessor.getInstance().executeCommand(project, {
        EditorActionManager.getInstance().getActionHandler(actionId).execute(sandboxEditor, null, sandboxContext)
      }, null, null)
      documentManager.commitDocument(sandboxDocument)

      val resultOffsets = intArrayOf(sandboxEditor.caretModel.offset,
                                     sandboxEditor.selectionModel.selectionStart,
                                     sandboxEditor.selectionModel.selectionEnd)
      val (reindented, hostOffsets) = shift(sandboxDocument.text, resultOffsets, base)
      runWriteAction { hostDocument.replaceString(contentStart, contentEnd, reindented) }
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

  /** Minimum leading-space indentation across the non-blank lines of [content], or null if it has none. */
  private fun commonIndent(content: String): Int? =
    content.split('\n').filter { it.isNotBlank() }.minOfOrNull { line -> line.takeWhile { it == ' ' }.length }

  /** The line of [text] containing character offset [offset]. */
  private fun lineAt(text: String, offset: Int): String {
    var lineStart = 0
    for (line in text.split('\n')) {
      val lineEnd = lineStart + line.length
      if (offset in lineStart..lineEnd) return line
      lineStart = lineEnd + 1
    }
    return ""
  }

  /** Leading-space indentation of [line] in [document]. */
  private fun lineIndent(document: Document, line: Int): Int =
    document.immutableCharSequence
      .subSequence(document.getLineStartOffset(line), document.getLineEndOffset(line))
      .takeWhile { it == ' ' }.length

  /**
   * Shifts the indentation of every line by [delta] spaces — negative removes leading spaces (dedent), positive
   * prepends them to non-blank lines (and lines bearing a tracked offset). Returns the shifted text and the
   * given [offsets] mapped into it.
   *
   * A whitespace-only line keeps its own whitespace: dedent leaves it alone, and reindent re-emits it as it came
   * back from the sandbox (only a blank line the caret landed on is indented, so a newly opened line gets the fence
   * base). The round trip has to be lossless for lines the replayed action never touches — rewriting them makes
   * [replay] write back text above the caret, and such a host change spans whole fence-content lines, so it
   * invalidates the shreds of the injected DocumentWindow the caret lives in: the window's length then collapses
   * below the offset `EnterHandler` snapshotted before calling the delegate, tripping "Wrong caret offset change".
   * [dedentBlankLines] opts out of that protection for a body whose lines are *all* blank, where their whitespace
   * is the fence indentation rather than content, and where there is no other line left to protect.
   */
  private fun shift(text: String, offsets: IntArray, delta: Int, dedentBlankLines: Boolean = false): Pair<String, IntArray> {
    if (delta == 0) return text to offsets
    val indent = if (delta > 0) " ".repeat(delta) else ""
    val out = StringBuilder()
    val mapped = offsets.copyOf()
    var lineStart = 0
    for ((index, line) in text.split('\n').withIndex()) {
      if (index > 0) out.append('\n')
      val bearsOffset = offsets.any { it in lineStart..(lineStart + line.length) }
      val blank = line.isBlank() && !dedentBlankLines
      val leading = line.takeWhile { it == ' ' }.length
      val removed = if (delta < 0 && !blank) minOf(-delta, leading) else 0
      val prepend = delta > 0 && (!blank || bearsOffset)
      val base = out.length
      for ((k, offset) in offsets.withIndex()) {
        if (offset in lineStart..(lineStart + line.length)) {
          val column = offset - lineStart
          mapped[k] = base + if (delta > 0) column + (if (prepend) delta else 0) else maxOf(0, column - removed)
        }
      }
      if (prepend) out.append(indent)
      out.append(if (delta < 0) line.substring(removed) else line)
      lineStart += line.length + 1
    }
    return out.toString() to mapped
  }
}
