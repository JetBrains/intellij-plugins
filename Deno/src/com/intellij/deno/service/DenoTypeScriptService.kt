package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.completion.JSInsertHandler
import com.intellij.lang.javascript.frameworks.modules.JSUrlImportsUtil
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationError.*
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionEntry
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptMessageBus
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.lsp.LspServer
import com.intellij.lsp.api.LspServerManager
import com.intellij.lsp.methods.HoverMethod
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
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

  private fun <T> withServer(action: LspServer.() -> T): T? =
    LspServerManager.getInstance(project).getServersForProvider(DenoLspSupportProvider::class.java).firstOrNull()?.action()

  override val name = "Deno LSP"

  override fun isDisabledByContext(context: VirtualFile) = false

  override fun isServiceCreated() = withServer { isRunning || isMalfunctioned } ?: false

  override fun showStatusBar() = withServer { isRunning } ?: false

  override fun getStatusText() = withServer {
    when {
      isRunning -> "Deno LSP"
      isMalfunctioned -> "Deno LSP ⚠"
      else -> "..."
    }
  }

  override fun openEditor(file: VirtualFile) {}
  override fun closeLastEditor(file: VirtualFile) {}

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): CompletionMergeStrategy {
    if (JSTokenTypes.STRING_LITERALS.contains(context.node.elementType)) {
      JSFileReferencesUtil.getReferenceModuleText(context.parent)?.let {
        if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(JSStringUtil.unquoteStringLiteralValue(it))) {
          return CompletionMergeStrategy.MERGE
        }
      }
    }

    return TypeScriptLanguageServiceUtil.getCompletionMergeStrategy(parameters, file, context)
  }

  override fun updateAndGetCompletionItems(virtualFile: VirtualFile, parameters: CompletionParameters): Future<List<CompletionEntry>?>? {
    return withServer { completedFuture(getCompletionItems(parameters).map(::DenoCompletionEntry)) }
  }

  override fun getServiceFixes(file: PsiFile, element: PsiElement?, result: JSAnnotationError): Collection<IntentionAction> {
    if (element != null && (result is DenoAnnotationError)) {
      val virtualFile = file.virtualFile
      return withServer {
        getCodeActions(file, result.diagnostic) { command, _ ->
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
      } ?: return emptyList()
    }
    return emptyList()
  }

  override fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                          items: List<CompletionEntry>,
                                          document: Document,
                                          positionInFileOffset: Int): Future<List<CompletionEntry>?>? {
    return withServer { completedFuture(items.map { DenoCompletionEntry(getResolvedCompletionItem((it as DenoCompletionEntry).item)) }) }
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement): Array<PsiElement> =
    withServer { getElementDefinitions(sourceElement).toTypedArray() } ?: emptyArray()

  override fun getSignatureHelp(file: PsiFile, context: CreateParameterInfoContext): Future<Stream<JSFunctionType>?>? = null

  fun quickInfo(element: PsiElement): TypeScriptQuickInfoResponse? {
    val server = LspServerManager.getInstance(project).getServersForProvider(DenoLspSupportProvider::class.java).firstOrNull()
                 ?: return null
    val raw = server.invokeSynchronously(HoverMethod.create(server, element)) ?: return null
    LOG.info("Quick info for $element : $raw")
    val response = TypeScriptQuickInfoResponse()
    response. displayString = raw.substring("<html><body><pre>".length, raw.length - "</pre></body></html>".length)
    return response
  }

  override fun getQuickInfoAt(element: PsiElement, originalElement: PsiElement, originalFile: VirtualFile): CompletableFuture<TypeScriptQuickInfoResponse?> =
    completedFuture(quickInfo(element))

  override fun restart(recreateToolWindow: Boolean) {
    LspServerManager.getInstance(project).stopAndRestartIfNeeded(DenoLspSupportProvider::class.java)
    TypeScriptMessageBus.get(project).changed()
  }

  override fun highlight(file: PsiFile): CompletableFuture<List<JSAnnotationError>>? {
    val server = LspServerManager.getInstance(project).getServersForProvider(DenoLspSupportProvider::class.java).firstOrNull()
                 ?: return completedFuture(emptyList())
    val virtualFile = file.virtualFile
    return completedFuture(server.getDiagnostics(virtualFile)?.map {
      DenoAnnotationError(it, virtualFile.canonicalPath)
    })
  }

  override fun canHighlight(file: PsiFile) = DialectDetector.isTypeScript(file)

  override fun isAcceptable(file: VirtualFile) =
    TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file) &&
    !JSCorePredefinedLibrariesProvider.isCoreLibraryFile(file) &&
    !DenoTypings.getInstance(project).isDenoTypings(file) &&
    !TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, file)

  override fun dispose() {}
}

class DenoCompletionEntry(internal val item: CompletionItem) : CompletionEntry {
  override val name: String get() = item.label

  override fun intoLookupElement() =
    LookupElementBuilder.create(item.label)
      .withTypeText(item.detail, true)
      .withInsertHandler(JSInsertHandler.DEFAULT)
}

class DenoAnnotationError(val diagnostic: Diagnostic, private val path: String?) : JSAnnotationError {
  override fun getLine() = diagnostic.range.start.line

  override fun getColumn() = diagnostic.range.start.character

  override fun getAbsoluteFilePath(): String? = path

  override fun getDescription(): String = DenoBundle.message("deno.inspection.message.prefix", diagnostic.message)

  override fun getCategory() = when (diagnostic.severity) {
    DiagnosticSeverity.Error -> ERROR_CATEGORY
    DiagnosticSeverity.Warning -> WARNING_CATEGORY
    else -> INFO_CATEGORY
  }
}