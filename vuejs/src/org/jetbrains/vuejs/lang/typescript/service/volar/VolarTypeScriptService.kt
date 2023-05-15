// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSInsertHandler
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationError.*
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionEntry
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptMessageBus
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lsp.LspServer
import com.intellij.lsp.api.LspServerManager
import com.intellij.lsp.methods.HoverMethod
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabled
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.Future
import java.util.stream.Stream

class VolarTypeScriptService(private val project: Project) : TypeScriptService, Disposable {


  private fun <T> withServer(action: LspServer.() -> T): T? =
    LspServerManager.getInstance(project).getServersForProvider(VolarSupportProvider::class.java).firstOrNull()?.action()

  override val name = "Volar"

  override fun isDisabledByContext(context: VirtualFile) = false

  override fun isServiceCreated() = withServer { isRunning || isMalfunctioned } ?: false

  override fun showStatusBar() = withServer { isRunning } ?: false

  override fun getStatusText() = withServer {
    when {
      isRunning -> "Volar $volarVersion"
      isMalfunctioned -> "Volar ⚠"
      else -> "..."
    }
  }

  override fun openEditor(file: VirtualFile) {}
  override fun closeLastEditor(file: VirtualFile) {}

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): CompletionMergeStrategy {
    return TypeScriptLanguageServiceUtil.getCompletionMergeStrategy(parameters, file, context)
  }

  override fun updateAndGetCompletionItems(virtualFile: VirtualFile, parameters: CompletionParameters): Future<List<CompletionEntry>?>? {
    return withServer { completedFuture(getCompletionItems(parameters).map(::VolarCompletionEntry)) }
  }

  override fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                          items: List<CompletionEntry>,
                                          document: Document,
                                          positionInFileOffset: Int): Future<List<CompletionEntry>?>? {
    return withServer { completedFuture(items.map { VolarCompletionEntry(getResolvedCompletionItem((it as VolarCompletionEntry).item)) }) }
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement): Array<PsiElement> =
    withServer { getElementDefinitions(sourceElement).toTypedArray() } ?: emptyArray()

  override fun getSignatureHelp(file: PsiFile, context: CreateParameterInfoContext): Future<Stream<JSFunctionType>?>? = null

  private fun quickInfo(element: PsiElement): TypeScriptQuickInfoResponse? {
    val server = LspServerManager.getInstance(project).getServersForProvider(VolarSupportProvider::class.java).firstOrNull()
                 ?: return null
    val raw = server.invokeSynchronously(HoverMethod.create(server, element)) ?: return null
    val response = TypeScriptQuickInfoResponse()
    response.displayString = raw.substring("<html><body><pre>".length, raw.length - "</pre></body></html>".length)
    return response
  }

  override fun getQuickInfoAt(element: PsiElement,
                              originalElement: PsiElement,
                              originalFile: VirtualFile): CompletableFuture<TypeScriptQuickInfoResponse?> =
    completedFuture(quickInfo(element))

  override fun restart(recreateToolWindow: Boolean) {
    val lspServerManager = LspServerManager.getInstance(project)
    val lspServers = lspServerManager.getServersForProvider(VolarSupportProvider::class.java)
    lspServers.forEach { lspServerManager.stopServer(it) }
    updateVolarLsp(project, true)
    TypeScriptMessageBus.get(project).changed()
  }

  override fun highlight(file: PsiFile): CompletableFuture<List<JSAnnotationError>>? {
    val server = LspServerManager.getInstance(project).getServersForProvider(VolarSupportProvider::class.java).firstOrNull()
                 ?: return completedFuture(emptyList())
    val virtualFile = file.virtualFile
    return completedFuture(server.getDiagnostics(virtualFile)?.map {
      VolarAnnotationError(it, virtualFile.canonicalPath)
    })
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isVolarFileTypeAcceptable(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile) = isVolarEnabled(project, file)

  override fun dispose() {}
}

class VolarCompletionEntry(internal val item: CompletionItem) : CompletionEntry {
  override val name: String get() = item.label

  override fun intoLookupElement() =
    LookupElementBuilder.create(item.label)
      .withTypeText(item.detail, true)
      .withInsertHandler(JSInsertHandler.DEFAULT)
}

class VolarAnnotationError(val diagnostic: Diagnostic, private val path: String?) : JSAnnotationError {
  override fun getLine() = diagnostic.range.start.line

  override fun getColumn() = diagnostic.range.start.character

  override fun getAbsoluteFilePath(): String? = path

  override fun getDescription(): String = VueBundle.message("volar.error.prefix", diagnostic.message)

  override fun getCategory() = when (diagnostic.severity) {
    DiagnosticSeverity.Error -> ERROR_CATEGORY
    DiagnosticSeverity.Warning -> WARNING_CATEGORY
    else -> INFO_CATEGORY
  }
}