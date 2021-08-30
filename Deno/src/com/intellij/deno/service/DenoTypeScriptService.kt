package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
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
import com.intellij.lsp.LspServer
import com.intellij.lsp.LspServerDescriptor
import com.intellij.lsp.LspServerManager
import com.intellij.lsp.data.LspCompletionItem
import com.intellij.lsp.data.LspDiagnostic
import com.intellij.lsp.data.LspSeverity.*
import com.intellij.lsp.methods.ForceDidChangeMethod
import com.intellij.lsp.methods.HoverMethod
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.containers.toArray
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

class DenoTypeScriptService(private val project: Project) : TypeScriptService {
  companion object {
    private val LOG = Logger.getInstance(DenoTypeScriptService::class.java)
    fun getInstance(project: Project): DenoTypeScriptService = project.getService(DenoTypeScriptService::class.java)
  }

  private var descriptor: LspServerDescriptor? = null

  private fun createDescriptor(element: PsiElement) =
    LspServerManager.getServerDescriptors(element).find { it is DenoLspServerDescriptor }!!.also { descriptor = it }

  private fun getDescriptor(element: PsiElement) = descriptor ?: createDescriptor(element)

  private fun getDescriptor(virtualFile: VirtualFile) = descriptor ?: PsiManager.getInstance(project).findFile(virtualFile)?.let {
    createDescriptor(it)
  }

  fun start(file: PsiFile) = getDescriptor(file).getServer(project).start()

  override fun isDisabledByContext(context: VirtualFile) = false

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): CompletionMergeStrategy =
    TypeScriptLanguageServiceUtil.getCompletionMergeStrategy(parameters, file, context)

  override fun updateAndGetCompletionItems(virtualFile: VirtualFile, parameters: CompletionParameters): Future<List<CompletionEntry>?>? {
    val descriptor = getDescriptor(virtualFile) ?: return null
    forceUpdate(descriptor.getServer(project), virtualFile)
    return completedFuture(descriptor.getCompletionItems(parameters).map(::DenoCompletionEntry))
  }

  override fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                          items: List<CompletionEntry>,
                                          document: Document,
                                          positionInFileOffset: Int): Future<List<CompletionEntry>?>? {
    val descriptor = getDescriptor(virtualFile) ?: return null
    return completedFuture(items.map { DenoCompletionEntry(descriptor.getResolvedCompletionItem((it as DenoCompletionEntry).item)) })
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement) =
    getDescriptor(sourceElement).getElementDefinitions(sourceElement).toArray(emptyArray())

  override fun getSignatureHelp(file: PsiFile, context: CreateParameterInfoContext): Future<Stream<JSFunctionType>?>? = null

  fun quickInfo(element: PsiElement): String? {
    val raw = getDescriptor(element).getServer(project).invokeSynchronously(HoverMethod.create(element)) ?: return null
    LOG.info("Quick info for $element : $raw")
    return raw.substring("<html><body><pre>".length, raw.length - "</pre></body></html>".length)
  }

  override fun getQuickInfoAt(element: PsiElement, originalElement: PsiElement, originalFile: VirtualFile): CompletableFuture<String?> =
    completedFuture(quickInfo(element))

  override fun terminateStartedProcess() {
    if (!project.isDisposed) {
      descriptor?.stopServer()
    }
  }

  override fun highlight(file: PsiFile): CompletableFuture<List<JSAnnotationError>>? {
    LOG.info("highlight")
    val server = getDescriptor(file).getServer(project)
    val virtualFile = file.virtualFile
    forceUpdate(server, virtualFile)
    return completedFuture(server.getDiagnostics(virtualFile)?.map {
      DenoAnnotationError(it, virtualFile.canonicalPath)
    })
  }

  override fun canHighlight(file: PsiFile) = DialectDetector.isTypeScript(file)

  override fun isAcceptable(file: VirtualFile) = DialectDetector.getLanguageDialect(file, project) is TypeScriptLanguageDialect
}

fun forceUpdate(server: LspServer, virtualFile: VirtualFile) {
  FileDocumentManager.getInstance().getDocument(virtualFile)?.let { document ->
    server.invoke(ForceDidChangeMethod(virtualFile, document))
  }
}

class DenoCompletionEntry(internal val item: LspCompletionItem): CompletionEntry {
  override val name: String get() = item.label

  override fun intoLookupElement() = item.intoLookupElement()
    .withTailText(" (Deno LSP)", true)
    .withInsertHandler(JSInsertHandler.DEFAULT)
}

class DenoAnnotationError(private val diagnostic: LspDiagnostic, private val path: String?) : JSAnnotationError {
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