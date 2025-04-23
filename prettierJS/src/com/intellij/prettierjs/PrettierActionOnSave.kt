// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptions
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.formatting.PrettierApplyFormattingStrategy
import com.intellij.prettierjs.formatting.createFormattingContext
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private class PrettierActionOnSave : ActionsOnSaveFileDocumentManagerListener.DocumentUpdatingActionOnSave() {
  override val presentableName: @NlsSafe String = "Prettier"

  override fun isEnabledForProject(project: Project): Boolean = PrettierConfiguration.getInstance(project).isRunOnSave

  override suspend fun updateDocument(project: Project, document: Document) {
    val (file, psiFile) = readAction { getFileToProcess(project, document) } ?: return
    runPrettierAsActionOnSave(file, psiFile, document)
  }

  @RequiresReadLock
  private fun getFileToProcess(project: Project, document: Document): Pair<VirtualFile, PsiFile>? {
    val prettierConfiguration = PrettierConfiguration.getInstance(project).takeIf { it.isRunOnSave } ?: return null
    val file = FileDocumentManager.getInstance().getFile(document) ?: return null
    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
    if (!PrettierUtil.checkNodeAndPackage(psiFile, null, PrettierUtil.NOOP_ERROR_HANDLER)) return null

    if (prettierConfiguration.isRunOnReformat) {
      val onSaveOptions = FormatOnSaveOptions.getInstance(project)
      if (onSaveOptions.isRunOnSaveEnabled && onSaveOptions.isFileTypeSelected(file.fileType)) { // already processed as com.intellij.prettierjs.PrettierPostFormatProcessor
        return null
      }
    }

    if (!PrettierUtil.isFormattingAllowedForFile(project, file)) return null

    return file to psiFile
  }

  private suspend fun runPrettierAsActionOnSave(file: VirtualFile, psiFile: PsiFile, document: Document) {
    val project = psiFile.project

    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
      ensureConfigsSaved(listOf(file), project)
    }

    val result = ReformatWithPrettierAction.performRequestForFile(psiFile, null) ?: return
    val formattedContent = result.result ?: return

    val formattingContext = createFormattingContext(
      document,
      formattedContent,
      result.cursorOffset,
    )

    val strategy = PrettierApplyFormattingStrategy.from(formattingContext)
    writeCommandAction(project, PrettierBundle.message("reformat.with.prettier.command.name")) {
      strategy.apply(project, file, formattingContext)
      moveCursor(file, psiFile, formattingContext.cursorOffset)
    }
  }

  private fun moveCursor(file: VirtualFile, psiFile: PsiFile, cursorOffset: Int) {
    val editor = FileEditorManager.getInstance(psiFile.project).selectedTextEditor ?: return
    if (!editor.isDisposed && editor.virtualFile == file && cursorOffset >= 0) {
      editor.caretModel.moveToOffset(cursorOffset)
    }
  }
}
