// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.CodeStyleBundle
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.formatting.FormatTextRanges
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.CoreFormattingService
import com.intellij.formatting.service.FormattingService
import com.intellij.lang.javascript.imports.JSModuleImportOptimizerBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.formatting.PrettierFormattingApplier
import com.intellij.prettierjs.formatting.extendRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.codeStyle.CoreCodeStyleUtil
import java.util.concurrent.CountDownLatch

private val LOG = logger<PrettierFormattingService>()

class PrettierFormattingService : AsyncDocumentFormattingService() {
  // The Prettier API works with content passed as parameters; it doesn't work directly with a file.
  // We dont need to save the file.
  override fun prepareForFormatting(document: Document, formattingContext: FormattingContext): Unit = Unit
  override fun getName(): @NlsSafe String = "Prettier"
  override fun getNotificationGroupId(): String = "Prettier"
  override fun getFeatures(): Set<FormattingService.Feature> = setOf(FormattingService.Feature.FORMAT_FRAGMENTS)

  override fun canFormat(psiFile: PsiFile): Boolean = Registry.`is`("prettier.use.async.formatting.service") && isApplicable(psiFile)

  override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
    val context = request.context
    val project = context.project
    val file = context.containingFile
    val virtualFile = context.virtualFile ?: return null

    val formatterLatch = CountDownLatch(1)
    file.putUserData(JSModuleImportOptimizerBase.COUNTDOWN_LATCH_KEY, formatterLatch)

    val skipInContentCheck = file.consumeSkipInContentScopeCheck()
    return PrettierFormattingTask(request, file, virtualFile, project, formatterLatch, skipInContentCheck)
  }

  private fun isApplicable(psiFile: PsiFile): Boolean {
    val file = psiFile.virtualFile ?: return false

    val project = psiFile.project
    val configuration = PrettierConfiguration.getInstance(project)
    if (!configuration.isRunOnReformat) return false

    val fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(file) as? TextEditor
    val template = fileEditor?.let { TemplateManager.getInstance(project).getActiveTemplate(it.editor) }
    if (template != null) return false

    val isOffEdt = !ApplicationManager.getApplication().isDispatchThread
    val isAllowed = isPrettierFormattingAllowedFor(project, file, checkIsInContent = isOffEdt)

    if (isOffEdt && isAllowed) psiFile.setSkipInContentScopeCheck() else psiFile.clearSkipInContentScopeCheck()

    return isAllowed
  }

  private class PrettierFormattingTask(
    private val request: AsyncFormattingRequest,
    private val file: PsiFile,
    private val virtualFile: VirtualFile,
    private val project: Project,
    private val formatterLatch: CountDownLatch,
    private val skipInContentCheck: Boolean,
  ) : FormattingTask {
    private var cancelled = false

    override fun isRunUnderProgress(): Boolean = true

    override fun cancel(): Boolean {
      cancelled = true
      return true
    }

    override fun run() {
      try {
        if (skipInContentCheck) {
          formatWithPrettier()
        }
        else {
          val fileIndex = ProjectFileIndex.getInstance(project)
          val isInContent = runReadAction { fileIndex.isInContent(virtualFile) }
          if (isInContent) {
            formatWithPrettier()
          }
          else {
            formatWithCoreFormatter()
          }
        }
      }
      catch (e: Exception) {
        LOG.warn("Error during Prettier formatting", e)
      }
      finally {
        request.onTextReady(null)
        formatterLatch.countDown()
      }
    }

    private fun formatWithCoreFormatter() {
      val coreFormattingService = EP_NAME.extensionList.find { it is CoreFormattingService }

      WriteCommandAction.writeCommandAction(project, request.context.containingFile)
        .withName(CodeStyleBundle.message("process.reformat.code"))
        .shouldRecordActionForActiveDocument(false)
        .run<RuntimeException> {
          coreFormattingService?.formatRanges(
            file,
            FormatTextRanges(file.textRange, true),
            request.canChangeWhitespaceOnly(),
            false,
          )
        }
    }

    private fun formatWithPrettier() {
      val extendedRanges = computeExtendedRanges()
      processRangesWithPrettier(extendedRanges)
    }

    private fun computeExtendedRanges(): List<TextRange> {
      val extendedRanges = mutableListOf<TextRange>()
      val formatRanges = request.formattingRanges.fold(FormatTextRanges()) { acc, range ->
        acc.apply { add(range, true) }
      }

      runReadAction {
        val infos = CoreCodeStyleUtil.getRangeFormatInfoList(file, formatRanges)
        CoreCodeStyleUtil.postProcessRanges(infos) { range: TextRange? ->
          range?.let { extendedRanges.add(extendRange(file, it)) }
        }
      }
      val sortedRanges = extendedRanges.sortedByDescending { it.startOffset }
      return sortedRanges.takeIf { it.isNotEmpty() } ?: request.formattingRanges
    }

    private fun processRangesWithPrettier(ranges: List<TextRange>) {
      val (document, initialModificationStamp) = runReadAction {
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        Pair(document, document?.modificationStamp)
      }

      if (document == null || initialModificationStamp == null) return

      var text: String = request.documentText
      var cursorOffset = -1

      for (range in ranges) {
        val formatted = ReformatWithPrettierAction.processFileAsFormattingTask(file, text, range) ?: return
        if (cancelled) return

        text = formatted.result ?: return
        if (formatted.cursorOffset >= 0 && cursorOffset < 0) {
          cursorOffset = formatted.cursorOffset
        }
      }

      val strategy = runReadAction {
        PrettierFormattingApplier.from(document, file, text)
      }

      WriteCommandAction.writeCommandAction(project, request.context.containingFile)
        .withName(PrettierBundle.message("reformat.with.prettier.command.name"))
        .shouldRecordActionForActiveDocument(false)
        .run<RuntimeException> {
          if (document.modificationStamp == initialModificationStamp) {
            strategy.apply(project, file)
          }
        }
    }
  }
}


private val SKIP_IN_CONTENT_CHECK: Key<Boolean> = Key.create("prettier.skip.isInContent.check")

private fun PsiFile.setSkipInContentScopeCheck() {
  putUserData(SKIP_IN_CONTENT_CHECK, true)
}

private fun PsiFile.clearSkipInContentScopeCheck() {
  putUserData(SKIP_IN_CONTENT_CHECK, null)
}

private fun PsiFile.consumeSkipInContentScopeCheck(): Boolean {
  val value = getUserData(SKIP_IN_CONTENT_CHECK) == true
  if (value) clearSkipInContentScopeCheck()
  return value
}