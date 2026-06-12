// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.actions.FileTreeIterator
import com.intellij.codeInsight.actions.VcsFacade
import com.intellij.lang.javascript.service.JSLanguageServiceUtil.awaitFuture
import com.intellij.lang.javascript.service.JSLanguageServiceUtil.convertLineSeparatorsToFileOriginal
import com.intellij.lang.javascript.service.JSLanguageServiceUtil.timeout
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorScrollingPositionKeeper
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.PrettierLanguageService.Companion.getInstance
import com.intellij.prettierjs.formatting.PrettierFormattingApplier.Companion.from
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.ArrayUtil
import com.intellij.util.SmartList
import com.intellij.util.ThrowableRunnable
import org.jetbrains.annotations.Nls
import java.util.concurrent.CompletableFuture
import kotlin.io.path.absolutePathString
import kotlin.math.abs

class ReformatWithPrettierAction : AnAction(), DumbAware {
  override fun update(e: AnActionEvent) {
    val project = e.project
    if (project == null) {
      e.presentation.setEnabledAndVisible(false)
      return
    }
    val psiFile = e.getData(CommonDataKeys.PSI_FILE)
    val nodePackage = PrettierConfiguration.getInstance(project).getPackage(psiFile)
    e.presentation.setEnabledAndVisible(!nodePackage.isEmptyPath && isAcceptableFileContext(e))
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    if (project == null) {
      return
    }
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor != null) {
      processFileInEditor(project, editor, null)
    }
    else {
      val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
      if (!ArrayUtil.isEmpty(virtualFiles)) {
        processVirtualFiles(project, virtualFiles?.toList() ?: listOf())
      }
    }
  }

  companion object {
    private val LOG = Logger.getInstance(ReformatWithPrettierAction::class.java)
    private const val EDT_TIMEOUT_MS: Long = 2000

    private fun isAcceptableFileContext(e: AnActionEvent): Boolean {
      val editor = e.getData(CommonDataKeys.EDITOR)
      if (editor != null) {
        return true
      }
      val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
      return !ArrayUtil.isEmpty(virtualFiles)
    }

    fun processFileInEditor(
      project: Project,
      editor: Editor,
      targetRange: TextRange?,
    ) {
      val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument())
      if (file == null) {
        return
      }

      val vFile = file.getVirtualFile()
      if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(mutableListOf(vFile))
          .hasReadonlyFiles()) {
        return
      }

      val range = targetRange
                  ?: if (editor.getSelectionModel().hasSelection())
                    TextRange(editor.getSelectionModel().selectionStart, editor.getSelectionModel().selectionEnd)
                  else
                    null

      ensureConfigsSaved(mutableListOf(vFile), project)
      val computable: ThrowableComputable<PrettierLanguageService.FormatResult, RuntimeException> =
        ThrowableComputable { performRequestForFile(file, range, null) }

      val result = ProgressManager.getInstance().runProcessWithProgressSynchronously(
        computable,
        PrettierBundle.message("progress.title"),
        true,
        project
      )
      // timed out. show notification?
      if (result == null) {
        return
      }

      if (result.ignored) {
        PrettierUtil.showHintLater(editor, PrettierBundle.message("file.was.ignored.hint", file.getName()), false, null)
      }
      else if (result.result != null) {
        val document = editor.getDocument()
        val textBefore = document.getImmutableCharSequence()
        val newContent = result.result

        /**
         * This checks only the first line break, but given that we don't handle mixed line separators,
         * this is enough to detect if separators were changed by the external process
         */
        val lineSeparatorUpdated = Ref(false)
        val strategy = from(document, file, newContent)

        EditorScrollingPositionKeeper.perform(editor, true, Runnable {
          runWriteCommandAction(project, Runnable {
            val isLineSeparatorChanged = strategy.apply(project, file)
            lineSeparatorUpdated.set(isLineSeparatorChanged)
          })
        })

        PrettierUtil.showHintLater(
          editor,
          buildNotificationMessage(document, textBefore, lineSeparatorUpdated.get()),
          false,
          null
        )
      }
    }

    fun processFileAsPostFormatProcessor(file: PsiFile, range: TextRange): TextRange {
      // PostFormatProcessors are invoked in EDT under write action. So we can't show progress and need to block for a while waiting for the result.
      LOG.assertTrue(ApplicationManager.getApplication().isWriteAccessAllowed())

      val project = file.getProject()

      val vFile = file.getVirtualFile()
      ensureConfigsSaved(mutableListOf(vFile), project)
      val result: PrettierLanguageService.FormatResult? = performRequestForFile(file, range, null)
      if (result != null) {
        val delta: Int = applyFormatResult(project, file, result)
        if (delta < 0 && range.length < abs(delta)) {
          return TextRange.from(range.startOffset, 0)
        }
        return range.grown(delta)
      }
      return range
    }

    fun processVirtualFiles(
      project: Project,
      virtualFiles: List<VirtualFile>,
    ) {
      val readonlyStatus = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFiles)
      if (readonlyStatus.hasReadonlyFiles()) {
        return
      }
      ensureConfigsSaved(virtualFiles, project)
      val psiManager = PsiManager.getInstance(project)
      if (virtualFiles.size == 1 && virtualFiles.first().isDirectory()) {
        val psiDirectory = psiManager.findDirectory(virtualFiles.first())
        if (psiDirectory == null) {
          return
        }
        processFileIterator(
          project,
          FileTreeIterator(psiDirectory),
          false,
        )
      }
      else {
        processFileIterator(
          project,
          FileTreeIterator(PsiUtilCore.toPsiFiles(psiManager, virtualFiles)),
          true,
        )
      }
    }

    private fun processFileIterator(
      project: Project,
      fileIterator: FileTreeIterator,
      reportSkippedFiles: Boolean,
    ) {
      val results: MutableMap<PsiFile, PrettierLanguageService.FormatResult> =
        executeUnderProgress(project) { indicator ->
          val reformattedResults = HashMap<PsiFile, PrettierLanguageService.FormatResult>()

          val files: MutableList<PsiFile> = SmartList<PsiFile>()
          ReadAction.run(ThrowableRunnable {
            while (fileIterator.hasNext()) {
              files.add(fileIterator.next())
            }
          })

          for (currentFile in files) {
            indicator.setText(PrettierBundle.message("processing.0.progress", currentFile.getName()))

            val result: PrettierLanguageService.FormatResult? = performRequestForFile(currentFile, null, null)
            // timed out. show notification?
            if (result == null) {
              continue
            }
            if (result.unsupported && reportSkippedFiles) {
              val errorResult: PrettierLanguageService.FormatResult =
                PrettierLanguageService.FormatResult
                  .error(PrettierBundle.message("not.supported.file", currentFile.getName()))
              reformattedResults[currentFile] = errorResult
            }
            if (result.ignored) {
              val errorResult: PrettierLanguageService.FormatResult =
                PrettierLanguageService.FormatResult.error(
                  PrettierBundle.message(
                    "file.was.ignored",
                    currentFile.getName()
                  )
                )
              reformattedResults[currentFile] = errorResult
              continue
            }
            reformattedResults[currentFile] = result
          }
          reformattedResults
        }

      runWriteCommandAction(project, Runnable {
        for (entry in results.entries) {
          val virtualFile = entry.key.getVirtualFile()
          if (virtualFile == null) {
            continue
          }
          applyFormatResult(project, entry.key, entry.value)
        }
      })
    }

    fun processFileAsFormattingTask(
      psiFile: PsiFile,
      text: String,
      range: TextRange,
    ): PrettierLanguageService.FormatResult? {
      ProgressManager.checkCanceled()

      val vFile = psiFile.getVirtualFile()
      if (vFile == null) return null

      val project = psiFile.getProject()

      ApplicationManager.getApplication().invokeAndWait(Runnable {
        ensureConfigsSaved(mutableListOf(vFile), project)
      })

      return performRequestForFile(psiFile, range, text)
    }

    /**
     * @param result (new text length) - (old text length)
     */
    fun applyFormatResult(
      project: Project,
      file: PsiFile,
      result: PrettierLanguageService.FormatResult,
    ): Int {
      val document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile())
      if (document != null && StringUtil.isEmpty(result.error) && !result.ignored && !result.unsupported && (result.result != null)) {
        val strategy = from(document, file, result.result)
        val diff = result.result.length - document.textLength
        strategy.apply(project, file)
        return diff
      }
      return 0
    }

    fun performRequestForFileAsync(
      currentFile: PsiFile,
      range: TextRange?,
      forcedInitialText: String?,
    ): CompletableFuture<PrettierLanguageService.FormatResult?> {
      val project = currentFile.getProject()
      val text = Ref.create<String?>()
      val filePath = Ref.create<String?>()
      val ignoreFilePath = Ref.create<String?>()
      val rangeForRequest = Ref.create<TextRange?>(range)

      ReadAction.run<RuntimeException>(ThrowableRunnable {
        if (!currentFile.isValid()) return@ThrowableRunnable
        val currentVFile = currentFile.getVirtualFile()
        filePath.set(currentVFile.toNioPath().absolutePathString())

        val content: CharSequence?
        if (forcedInitialText != null) {
          content = forcedInitialText
        }
        else {
          // PsiFile might be not committed at this point, take text from document
          val document = PsiDocumentManager.getInstance(project).getDocument(currentFile)
          if (document == null) return@ThrowableRunnable
          content = document.getImmutableCharSequence()
        }

        if (range != null && range.startOffset == 0 && range.length == content.length) {
          // Prettier may remove trailing line break in Vue (WEB-56144, https://github.com/prettier/prettier/issues/13399).
          // It's safer not pass a range when there's no need to.
          rangeForRequest.set(null)
        }

        var offsetsToKeep: IntArray? = null

        val editor = FileEditorManager.getInstance(project).getSelectedTextEditor()
        if (editor != null && !editor.isDisposed() && editor.virtualFile == currentVFile) {
          offsetsToKeep = intArrayOf(editor.getCaretModel().offset)
        }
        val convertedText = convertLineSeparatorsToFileOriginal(project, content, currentVFile, offsetsToKeep)

        text.set(convertedText.toString())
        val ignoreVFile = PrettierUtil.findIgnoreFile(project, currentVFile)
        if (ignoreVFile != null) {
          ignoreFilePath.set(ignoreVFile.getPath())
        }
      })

      if (text.isNull) {
        return CompletableFuture.completedFuture<PrettierLanguageService.FormatResult?>(PrettierLanguageService.FormatResult.UNSUPPORTED)
      }

      val nodePackage = PrettierConfiguration.getInstance(project).getPackage(currentFile)
      val service = getInstance(project, currentFile.getVirtualFile(), nodePackage)

      return service.format(filePath.get()!!, ignoreFilePath.get(), text.get()!!, nodePackage, rangeForRequest.get())
    }

    fun performRequestForFile(
      currentFile: PsiFile,
      range: TextRange?,
      forcedInitialText: String?,
    ): PrettierLanguageService.FormatResult? {
      val edt = ApplicationManager.getApplication().isDispatchThread()
      if (!edt && ApplicationManager.getApplication().isReadAccessAllowed()) {
        LOG.error("JSLanguageServiceUtil.awaitFuture() under read action may cause deadlock")
      }

      val formatFuture: CompletableFuture<PrettierLanguageService.FormatResult?> =
        performRequestForFileAsync(currentFile, range, forcedInitialText)
      val timeout = if (edt) EDT_TIMEOUT_MS else timeout
      return awaitFuture(formatFuture, timeout, true, null, edt)
    }

    private fun <T> executeUnderProgress(
      project: Project,
      handler: (ProgressIndicator) -> T,
    ): T {
      return ProgressManager.getInstance().runProcessWithProgressSynchronously<T, RuntimeException?>(
        { handler(ProgressManager.getInstance().getProgressIndicator()) },
        PrettierBundle.message("progress.title"),
        true,
        project
      )
    }

    private fun runWriteCommandAction(project: Project, runnable: Runnable) {
      WriteCommandAction.runWriteCommandAction(project, PrettierBundle.message("reformat.with.prettier.command.name"), null, runnable)
    }

    @Nls
    private fun buildNotificationMessage(
      document: Document,
      textBefore: CharSequence,
      lineSeparatorsUpdated: Boolean,
    ): @Nls String {
      val number = VcsFacade.getInstance().calculateChangedLinesNumber(document, textBefore)
      if (number == 0) {
        return if (lineSeparatorsUpdated)
          PrettierBundle.message("line.endings.were.updated")
        else
          PrettierBundle.message("no.lines.changed")
      }
      return PrettierBundle.message("formatted.0.lines", number)
    }
  }
}
