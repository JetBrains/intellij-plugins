package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.completion.JSInsertHandler
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationError.*
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionEntry
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptMessageBus
import com.intellij.lsp.LspServer
import com.intellij.lsp.LspServerDescriptor
import com.intellij.lsp.LspServerManager
import com.intellij.lsp.data.LspCompletionItem
import com.intellij.lsp.data.LspDiagnostic
import com.intellij.lsp.data.LspSeverity.*
import com.intellij.lsp.methods.HoverMethod
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.Future
import java.util.stream.Stream

class DenoTypeScriptServiceProvider(val project: Project) : JSLanguageServiceProvider {

  override fun isHighlightingCandidate(file: VirtualFile) = TypeScriptCompilerSettings.acceptFileType(file.fileType)

  override fun getService(file: VirtualFile) = allServices.firstOrNull()

  override fun getAllServices() =
    if (DenoSettings.getService(project).isUseDeno()) listOf(DenoTypeScriptService.getInstance(project)) else emptyList()
}

class DenoTypeScriptService(private val project: Project) : TypeScriptService, Disposable {
  companion object {
    private val LOG = Logger.getInstance(DenoTypeScriptService::class.java)
    fun getInstance(project: Project): DenoTypeScriptService = project.getService(DenoTypeScriptService::class.java)
  }

  @Volatile
  private var descriptor: LspServerDescriptor? = null
  private val openedFiles = mutableListOf<VirtualFile>()

  @Synchronized
  private fun createDescriptor(element: PsiElement): LspServerDescriptor? {
    if (descriptor != null) return descriptor

    return LspServerManager
      .getServerDescriptors(element)
      .find { it is DenoLspServerDescriptor }
      .also {
        val changed = descriptor != it
        descriptor = it
        if (changed) afterUpdatingDescriptor()
      }
  }

  private fun getDescriptor(element: PsiElement) = descriptor ?: createDescriptor(element)

  private fun getDescriptor(virtualFile: VirtualFile) = descriptor ?: PsiManager.getInstance(project).findFile(virtualFile)?.let {
    createDescriptor(it)
  }

  private fun <T> withServer(action: LspServer.() -> T): T? = descriptor?.getServer(project)?.action()

  override val name = "Deno LSP"

  override fun isDisabledByContext(context: VirtualFile) = false

  override fun isServiceCreated() = withServer { isRunning || isMalfunctioned } ?: false

  override fun showStatusBar() = withServer { totalFilesOpened != 0 } ?: false

  override fun getStatusText() = withServer {
    when {
      isRunning -> "Deno LSP"
      isMalfunctioned -> "Deno LSP ⚠"
      else -> "..."
    }
  }

  override fun openEditor(file: VirtualFile) {
    openedFiles.add(file)
  }

  override fun closeLastEditor(file: VirtualFile) {
    openedFiles.remove(file)
  }

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): CompletionMergeStrategy =
    TypeScriptLanguageServiceUtil.getCompletionMergeStrategy(parameters, file, context)

  override fun updateAndGetCompletionItems(virtualFile: VirtualFile, parameters: CompletionParameters): Future<List<CompletionEntry>?>? {
    val descriptor = getDescriptor(virtualFile) ?: return null
    return completedFuture(descriptor.getCompletionItems(parameters).map(::DenoCompletionEntry))
  }

  override fun getServiceFixes(file: PsiFile, element: PsiElement?, result: JSAnnotationError): Collection<IntentionAction> {
    if (element != null && (result is DenoAnnotationError)) {
      val virtualFile = file.virtualFile
      val descriptor = getDescriptor(virtualFile) ?: return emptyList()
      return descriptor.getCodeActions(file, result.diagnostic) { command, _ ->
        if (command == "deno.cache") {
          //or implement using deno command 
          val commandLine = GeneralCommandLine(DenoSettings.getService(project).getDenoPath(), "cache", virtualFile.path)
          try {
            ExecUtil.execAndGetOutput(commandLine)
            DaemonCodeAnalyzer.getInstance(project).restart()
          }
          catch (e: ExecutionException) {
            //skip
          }

          return@getCodeActions true
        }
        return@getCodeActions false
      }
    }
    return emptyList()
  }

  override fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                          items: List<CompletionEntry>,
                                          document: Document,
                                          positionInFileOffset: Int): Future<List<CompletionEntry>?>? {
    val descriptor = getDescriptor(virtualFile) ?: return null
    return completedFuture(items.map { DenoCompletionEntry(descriptor.getResolvedCompletionItem((it as DenoCompletionEntry).item)) })
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement): Array<PsiElement> =
    getDescriptor(sourceElement)?.getElementDefinitions(sourceElement)?.toTypedArray() ?: emptyArray()

  override fun getSignatureHelp(file: PsiFile, context: CreateParameterInfoContext): Future<Stream<JSFunctionType>?>? = null

  fun quickInfo(element: PsiElement): String? {
    val raw = getDescriptor(element)?.getServer(project)?.invokeSynchronously(HoverMethod.create(element)) ?: return null
    LOG.info("Quick info for $element : $raw")
    return raw.substring("<html><body><pre>".length, raw.length - "</pre></body></html>".length)
  }

  override fun getQuickInfoAt(element: PsiElement, originalElement: PsiElement, originalFile: VirtualFile): CompletableFuture<String?> =
    completedFuture(quickInfo(element))

  override fun restart(recreateToolWindow: Boolean) {
    val descriptor = descriptor
    if (!project.isDisposed && descriptor != null) {
      descriptor.restart()
      TypeScriptMessageBus.get(project).changed()
    }
  }

  private fun afterUpdatingDescriptor() {
    TypeScriptMessageBus.get(project).changed()
    val instance = DenoTypings.getInstance(project)
    BackgroundTaskUtil.executeOnPooledThread(this) {
      if (instance.reload()) {
        ApplicationManager.getApplication().invokeLater({
          val service = DenoSettings.getService(project)
          service.setUseDenoAndReload(service.isUseDeno(), false)
        }, project.disposed)
      }
    }
  }

  override fun highlight(file: PsiFile): CompletableFuture<List<JSAnnotationError>>? {
    val server = getDescriptor(file)?.getServer(project) ?: return completedFuture(emptyList())
    val virtualFile = file.virtualFile
    return completedFuture(server.getDiagnostics(virtualFile)?.map {
      DenoAnnotationError(it, virtualFile.canonicalPath)
    })
  }

  override fun canHighlight(file: PsiFile) = DialectDetector.isTypeScript(file)

  override fun isAcceptable(file: VirtualFile) = DialectDetector.getLanguageDialect(file, project) is TypeScriptLanguageDialect
  override fun dispose() {}
}

class DenoCompletionEntry(internal val item: LspCompletionItem) : CompletionEntry {
  override val name: String get() = item.label

  override fun intoLookupElement() = item.intoLookupElement().withInsertHandler(JSInsertHandler.DEFAULT)
}

class DenoAnnotationError(val diagnostic: LspDiagnostic, private val path: String?) : JSAnnotationError {
  override fun getLine() = diagnostic.range.start.line

  override fun getColumn() = diagnostic.range.start.character

  override fun getAbsoluteFilePath(): String? = path

  override fun getDescription(): String = DenoBundle.message("deno.inspection.message.prefix", diagnostic.message)

  override fun getCategory() = when (diagnostic.severity) {
    Error -> ERROR_CATEGORY
    Warning -> WARNING_CATEGORY
    Hint, Information -> INFO_CATEGORY
  }
}