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
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.template.MdxFileViewProvider

/**
 * Auto-closes JSX/HTML tags (and the `<>` fragment) when `>` is typed in an MDX file. Neither
 * [com.intellij.lang.javascript.editing.JavaScriptTypedHandler] (gates on the base file being a JS
 * dialect, which MDX is not) nor [com.intellij.codeInsight.editorActions.XmlGtTypedHandler] (gates on
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
    val closingTag = MdxJsxScanner.closingTagToInsert(text, offset) ?: return Result.CONTINUE

    // Don't insert twice if an earlier handler in the chain already added it.
    if (text.subSequence(offset, text.length).startsWith(closingTag)) return Result.DEFAULT

    // Insert without moving the caret, then DEFAULT so the typed `>` still lands normally between it and
    // the close, but no later delegate in the chain (e.g. JavaScriptTypedHandler, registered "after" this
    // one) also tries to insert its own closing tag for the same keystroke. WEB-78468.
    EditorModificationUtilEx.insertStringAtCaret(editor, closingTag, false, 0)
    return Result.DEFAULT
  }
}
