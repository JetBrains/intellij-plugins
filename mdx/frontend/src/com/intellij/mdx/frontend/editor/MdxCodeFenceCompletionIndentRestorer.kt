package com.intellij.mdx.frontend.editor

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupEvent
import com.intellij.codeInsight.lookup.LookupListener
import com.intellij.codeInsight.lookup.LookupManagerListener
import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFence

/**
 * Restores the code-fence indentation stripped by completion.
 *
 * A fence body is language-injected with its leading indentation as part of the injected document, so the
 * platform's LookupUtil takes the whole `    conso` as the completion prefix and drops the indent when it
 * replaces it with `console`. Until that's fixed upstream, re-indent the caret line to the fence's base
 * indent after an item is inserted.
 */
internal class MdxCodeFenceCompletionIndentRestorer : LookupManagerListener {
  override fun activeLookupChanged(oldLookup: Lookup?, newLookup: Lookup?) {
    val lookup = newLookup ?: return
    val editor = lookup.editor
    val project = editor.project ?: return
    val psiFile = lookup.psiFile ?: return
    if (InjectedLanguageManager.getInstance(project).getTopLevelFile(psiFile) !is MdxFile) return

    lookup.addLookupListener(object : LookupListener {
      override fun itemSelected(event: LookupEvent) {
        val hostEditor = (editor as? EditorWindow)?.delegate ?: editor
        runWriteAction { restoreFenceIndent(project, hostEditor) }
      }
    })
  }

  private fun restoreFenceIndent(project: Project, hostEditor: Editor) {
    val document = hostEditor.document
    if (!document.isWritable) return
    val documentManager = PsiDocumentManager.getInstance(project)
    // Resolve PSI fresh from the document: a reference captured when the lookup opened would be stale.
    documentManager.commitDocument(document)
    val hostFile = documentManager.getPsiFile(document) as? MdxFile ?: return

    val caret = hostEditor.caretModel.offset.coerceIn(0, document.textLength)
    val element = PsiUtilCore.getElementAtOffset(hostFile, (caret - 1).coerceAtLeast(0))
    val fence = PsiTreeUtil.getParentOfType(element, MarkdownCodeFence::class.java) ?: return

    val startLine = document.getLineNumber(fence.textRange.startOffset)
    val endLine = document.getLineNumber((fence.textRange.endOffset - 1).coerceAtLeast(fence.textRange.startOffset))
    val caretLine = document.getLineNumber(caret)
    if (caretLine !in (startLine + 1)..<endLine) return // only lines strictly between the fences

    val baseIndent = lineText(document, startLine).takeWhile { it == ' ' }.length
    if (baseIndent == 0) return
    val currentIndent = lineText(document, caretLine).takeWhile { it == ' ' }.length
    if (currentIndent >= baseIndent) return // completion left the indent (or more) intact

    val missing = baseIndent - currentIndent
    document.insertString(document.getLineStartOffset(caretLine), " ".repeat(missing))
    documentManager.commitDocument(document)
    hostEditor.caretModel.moveToOffset((caret + missing).coerceAtMost(document.textLength))
  }

  private fun lineText(document: Document, line: Int): String =
    document.immutableCharSequence.subSequence(document.getLineStartOffset(line), document.getLineEndOffset(line)).toString()
}
