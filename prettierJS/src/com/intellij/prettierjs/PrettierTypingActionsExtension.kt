// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.DefaultTypingActionsExtension
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager

class PrettierTypingActionsExtension : DefaultTypingActionsExtension() {

  override fun startPaste(project: Project, editor: Editor) {
    super.startPaste(project, editor)
    editor.setPasteActionFlag(project, true)
  }

  override fun endPaste(project: Project, editor: Editor) {
    super.endPaste(project, editor)
    editor.setPasteActionFlag(project, null)
  }

  override fun isSuitableContext(project: Project, editor: Editor): Boolean {
    val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()) ?: return false
    val virtualFile = file.virtualFile ?: return false

    val configuration = PrettierConfiguration.getInstance(project)
    if (!configuration.isRunOnPaste) return false
    if (!PrettierUtil.checkNodeAndPackage(file, null, PrettierUtil.NOOP_ERROR_HANDLER)) return false

    return PrettierUtil.isFormattingAllowedForFile(project, virtualFile)
  }

  override fun format(
    project: Project,
    editor: Editor,
    howtoReformat: Int,
    startOffset: Int,
    endOffset: Int,
    anchorColumn: Int,
    indentBeforeReformat: Boolean,
    formatInjected: Boolean,
  ) { // Always use REFORMAT_BLOCK regardless of the howtoReformat parameter when Prettier is enabled for paste.
    // The format function will call the async formatting service, as Prettier.
    super.format(project, editor, CodeInsightSettings.REFORMAT_BLOCK, startOffset, endOffset, anchorColumn, indentBeforeReformat, formatInjected)
  }
}

// Transient flag to let PrettierFormattingService know the request was triggered by paste
val IN_PASTE_ACTION_KEY: Key<Boolean> = Key.create("com.intellij.prettierjs.IN_PASTE_ACTION_KEY")

fun Editor.setPasteActionFlag(project: Project, value: Boolean?) {
  val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
  psiFile?.putUserData(IN_PASTE_ACTION_KEY, value)
}