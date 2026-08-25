package org.intellij.plugin.mdx.editor

import com.intellij.ide.DataManager
import com.intellij.injected.editor.DocumentWindow
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.CustomizedDataContext
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiUtilCore
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils
import org.intellij.plugins.markdown.injection.aliases.CodeFenceLanguageGuesser

/**
 * Replays an editor action that lands inside an MDX code fence in a throwaway file of the fence language.
 *
 * Editing the injected fence fragment directly is unreliable, since the injected document is a standalone
 * 0-based file whose formatter/indenter fights the host indentation. Instead the fence body is copied into
 * a standalone file of the fence language (stripped of the indent its lines carry — see [fenceIndent] — since
 * indented input confuses the language formatter), the same action is replayed there, and the result is written
 * back with that indent restored, with caret and selection mapped both ways.
 */
object MdxCodeFenceSandbox {
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

    val element = PsiUtilCore.getElementAtOffset(hostFile, (caret - 1).coerceAtLeast(0))
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
    // Every line of the fence carries what its opening line has in front of the backticks, so that is what the
    // round trip strips and puts back. With no body at all the fence's opening line is the caret's own line.
    val hasCode = content.lineSequence().any { it.isNotBlank() }
    // A language-less fence has no formatter to reflow anything, so on Enter the platform's own plain-text indent
    // copying already carries the caret line's indentation onto the line it opens. Applying the fence indent on top
    // would add it a second time, so leave the body exactly as the host has it.
    val preservesHostIndentation = isPlainTextFallback && actionId == "EditorEnter" && hasCode &&
                                   lineAt(content, caret - contentStart).isBlank()
    val indent = if (preservesHostIndentation) ""
                 else fenceIndent(hostDocument, if (hasBody) startLine else hostDocument.getLineNumber(caret))
    val offsets = intArrayOf(caret - contentStart, selectionStart - contentStart, selectionEnd - contentStart)
    val (dedented, dedentedOffsets) = shift(content, offsets, indent, strip = true, startsHostLine = hasBody)

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
      val (reindented, hostOffsets) = shift(sandboxDocument.text, resultOffsets, indent,
                                            strip = false, startsHostLine = hasBody)
      changedContent(content, reindented)?.let { replacement ->
        runWriteAction {
          hostDocument.replaceString(
            contentStart + replacement.start,
            contentStart + replacement.end,
            replacement.text,
          )
        }
      }
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

  private fun changedContent(original: String, updated: String): ContentReplacement? {
    if (original == updated) return null
    var prefixLength = 0
    while (prefixLength < original.length &&
           prefixLength < updated.length &&
           original[prefixLength] == updated[prefixLength]) {
      prefixLength++
    }

    var suffixLength = 0
    while (suffixLength < original.length - prefixLength &&
           suffixLength < updated.length - prefixLength &&
           original[original.lastIndex - suffixLength] == updated[updated.lastIndex - suffixLength]) {
      suffixLength++
    }

    return ContentReplacement(
      prefixLength,
      original.length - suffixLength,
      updated.substring(prefixLength, updated.length - suffixLength),
    )
  }

  /**
   * Strips [indent] off every line of [text] ([strip]) or puts it back in front of every line, returning the text
   * along with the given [offsets] mapped into it.
   *
   * [startsHostLine] is false for a fence with no body lines at all, where the replaced region is the caret's own
   * position rather than whole lines: the first line back from the sandbox continues the fence's opening line
   * there, so it must not be given an indent of its own.
   */
  private fun shift(text: String,
                    offsets: IntArray,
                    indent: String,
                    strip: Boolean,
                    startsHostLine: Boolean): Pair<String, IntArray> {
    if (indent.isEmpty()) return text to offsets
    val out = StringBuilder()
    val mapped = offsets.copyOf()
    var lineStart = 0
    for ((index, line) in text.split('\n').withIndex()) {
      if (index > 0) out.append('\n')
      val applies = index > 0 || startsHostLine
      val delta = when {
        !applies -> 0
        strip -> -indent.commonPrefixWith(line).length
        else -> indent.length
      }
      val base = out.length
      for ((k, offset) in offsets.withIndex()) {
        if (offset in lineStart..(lineStart + line.length)) {
          mapped[k] = base + maxOf(0, offset - lineStart + delta)
        }
      }
      if (strip) {
        out.append(line, -delta, line.length)
      }
      else {
        if (applies) out.append(indent)
        out.append(line)
      }
      lineStart += line.length + 1
    }
    return out.toString() to mapped
  }

  private data class ContentReplacement(val start: Int, val end: Int, val text: String)
}
