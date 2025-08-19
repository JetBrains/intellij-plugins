// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.lang.javascript.editor.JSCopyPasteService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.registry.Registry
import com.intellij.prettierjs.formatting.PrettierFormattingApplier
import com.intellij.prettierjs.formatting.createFormattingContext
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import kotlin.time.Duration.Companion.milliseconds

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

    if (!shouldRunPrettierOnPaste(project, psiFile)) return

    PsiDocumentManager.getInstance(project).commitDocument(document)

    val psiPtr = psiFile.createSmartPointer()

    project.service<JSCopyPasteService>().scheduleOnPasteProcessing(
      psiFile,
      PrettierBundle.message("progress.title"),
      PrettierBundle.message("reformat.with.prettier.command.name"),
      collectChanges = {
        val psiFile = psiPtr.dereference() ?: return@scheduleOnPasteProcessing emptyList()
        val response = runBlockingCancellable {
          val timeout = Registry.intValue("prettier.on.paste.timeout.ms", 500).milliseconds
          withTimeoutOrNull(timeout) {
            ReformatWithPrettierAction.performRequestForFileAsync(psiFile, bounds.textRange, psiFile.text)?.await()
          }
        } ?: return@scheduleOnPasteProcessing emptyList()

        if (response.result == null) return@scheduleOnPasteProcessing emptyList()
        val formattingContext = createFormattingContext(
          document,
          response.result,
          response.cursorOffset,
        )

        listOfNotNull(PrettierFormattingApplier.from(formattingContext))
      },
      applyChanges = { waCallbacks ->
        val psiFile = psiPtr.dereference() ?: return@scheduleOnPasteProcessing
        waCallbacks.forEach { it.apply(project, psiFile.virtualFile) }
      }
    )
  }

  override fun requiresAllDocumentsToBeCommitted(editor: Editor, project: Project): Boolean = false

  private fun shouldRunPrettierOnPaste(project: Project, psiFile: PsiFile): Boolean {
    val configuration = PrettierConfiguration.getInstance(project)
    if (!configuration.isRunOnPaste) return false
    val vFile = psiFile.virtualFile ?: return false
    return PrettierUtil.isFormattingAllowedForFile(project, vFile)
  }
}
