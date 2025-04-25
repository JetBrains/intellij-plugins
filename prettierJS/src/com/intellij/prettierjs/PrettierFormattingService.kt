// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.formatting.FormatTextRanges
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.lang.javascript.imports.JSModuleImportOptimizerBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.registry.Registry
import com.intellij.prettierjs.formatting.extendRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.codeStyle.CoreCodeStyleUtil
import java.util.concurrent.CountDownLatch

class PrettierFormattingService : AsyncDocumentFormattingService() {
  override fun getName(): @NlsSafe String = "Prettier"
  override fun getNotificationGroupId(): String = "Prettier"
  override fun getFeatures(): Set<FormattingService.Feature> = setOf(FormattingService.Feature.FORMAT_FRAGMENTS)

  override fun canFormat(psiFile: PsiFile): Boolean =
    Registry.`is`("prettier.use.async.formatting.service") &&
    isApplicable(psiFile) &&
    PrettierUtil.checkNodeAndPackage(psiFile, null, PrettierUtil.NOOP_ERROR_HANDLER)

  override fun createFormattingTask(formattingRequest: AsyncFormattingRequest): FormattingTask? {
    val context = formattingRequest.context
    val file = context.containingFile

    var formatterLatch = CountDownLatch(1)
    file.putUserData(JSModuleImportOptimizerBase.COUNTDOWN_LATCH_KEY, formatterLatch)

    return object : FormattingTask {
      private var cancelled = false

      override fun isRunUnderProgress(): Boolean = true

      override fun cancel(): Boolean {
        cancelled = true
        return true
      }

      override fun run() {
        try {
          val extendedRanges = computeExtendedRanges()
          processRangesWithPrettier(extendedRanges)
        }
        finally {
          formatterLatch.countDown()
        }
      }

      private fun computeExtendedRanges(): List<TextRange> {
        val extendedRanges = mutableListOf<TextRange>()
        val formatRanges = formattingRequest.formattingRanges.fold(FormatTextRanges()) { acc, range ->
          acc.apply { add(range, true) }
        }

        ApplicationManager.getApplication().runReadAction {
          val infos = CoreCodeStyleUtil.getRangeFormatInfoList(file, formatRanges)
          CoreCodeStyleUtil.postProcessRanges(infos) { range: TextRange? ->
            range?.let { extendedRanges.add(extendRange(file, it)) }
          }
        }
        return extendedRanges.sortedByDescending { it.startOffset }
      }

      private fun processRangesWithPrettier(ranges: List<TextRange>) {
        var text: String = formattingRequest.documentText

        for (range in ranges) {
          val newText = ReformatWithPrettierAction.processFileAsFormattingTask(formattingRequest.context.containingFile, text, range)
          if (cancelled) return

          if (newText == null) {
            formattingRequest.onTextReady(null)
            return
          }

          text = newText
        }

        formattingRequest.onTextReady(text)
      }
    }
  }
}

private fun isApplicable(psiFile: PsiFile): Boolean {
  val file = psiFile.virtualFile ?: return false

  val project = psiFile.project
  val configuration = PrettierConfiguration.getInstance(project)
  if (!configuration.isRunOnReformat) return false

  val fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file) as? TextEditor
  val template = fileEditor?.let { TemplateManager.getInstance(project).getActiveTemplate(it.editor) }
  if (template != null) return false

  return PrettierUtil.isFormattingAllowedForFile(project, file)
}