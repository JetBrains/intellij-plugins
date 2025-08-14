// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.formatting.service.FormattingServiceUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

class PrettierCopyPastePostProcessor : CopyPastePostProcessor<TextBlockTransferableData>() {
  private object DumbData : TextBlockTransferableData {
    private val FLAVOR = DataFlavor(PrettierCopyPastePostProcessor::class.java, "class: PrettierCopyPastePostProcessor")
    override fun getFlavor(): DataFlavor = FLAVOR
  }

  private val DATA_LIST: List<TextBlockTransferableData> = listOf(DumbData)

  override fun collectTransferableData(
    file: PsiFile,
    editor: Editor,
    startOffsets: IntArray,
    endOffsets: IntArray,
  ): List<TextBlockTransferableData> = emptyList()

  // if this list is empty, processTransferableData won't be called
  override fun extractTransferableData(content: Transferable): List<TextBlockTransferableData> = DATA_LIST

  override fun processTransferableData(
    project: Project,
    editor: Editor,
    bounds: RangeMarker,
    caretOffset: Int,
    indented: Ref<in Boolean>,
    values: MutableList<out TextBlockTransferableData>,
  ) {
    val document = editor.document
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
    val virtualFile = psiFile.virtualFile ?: return

    val configuration = PrettierConfiguration.getInstance(project)
    if (!configuration.isRunOnPaste) return
    if (!PrettierUtil.isFormattingAllowedForFile(project, virtualFile)) return

    val service = FormattingServiceUtil.findService(PrettierFormattingService::class.java) ?: return

    PsiDocumentManager.getInstance(project).commitDocument(document)

    service.formatElement(psiFile, bounds.textRange, true)
  }

  override fun requiresAllDocumentsToBeCommitted(editor: Editor, project: Project): Boolean = false
}
