package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.integration.JSAnnotationError.*
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionEntry
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy.MERGE
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy.NON
import com.intellij.lsp.LspServer
import com.intellij.lsp.data.LspCompletionItem
import com.intellij.lsp.data.LspDiagnostic
import com.intellij.lsp.data.LspSeverity.*
import com.intellij.lsp.methods.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.containers.toArray
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.Future
import java.util.stream.Stream

class DenoTypeScriptService(private val project: Project) : TypeScriptService, DocumentListener, Disposable {
  private var connection: LspServer? = null
  private val openedFiles = mutableSetOf<VirtualFile>()

  companion object {
    fun getInstance(project: Project): DenoTypeScriptService = project.getService(DenoTypeScriptService::class.java)
  }

  private fun getConnection(file: VirtualFile) =
    connection ?: LspServer(DenoLspServerDescriptor(project, file)).also {
      connection = it
      it.start()
    }

  override fun isDisabledByContext(context: VirtualFile) = false

  override fun isServiceCreated() = connection != null

  override fun getStatusText() = when {
    connection!!.isRunning -> "Deno LSP: running"
    connection!!.isMalfunctioned -> "Deno LSP: error"
    else -> "Deno LSP"
  }

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement) =
    if (parameters.isExtendedCompletion) NON else MERGE

  override fun updateAndGetCompletionItems(virtualFile: VirtualFile, parameters: CompletionParameters): Future<List<CompletionEntry>?>? =
    completedFuture(getConnection(virtualFile).invokeSynchronously(CompletionMethod(parameters))?.map(::DenoCompletionEntry))

  override fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                          items: List<CompletionEntry>,
                                          document: Document,
                                          positionInFileOffset: Int): Future<List<CompletionEntry>?>? {
    val connection = getConnection(virtualFile)
    return completedFuture(items.map {
      connection
        .invokeSynchronously(ResolveCompletionItemMethod((it as DenoCompletionEntry).item))
        ?.let(::DenoCompletionEntry) ?: it
    })
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement) = getFile(document)?.let {
    getConnection(it).invokeSynchronously(ElementDefinitionMethod.create(sourceElement))?.toArray(emptyArray())
  }

  override fun getSignatureHelp(file: PsiFile, context: CreateParameterInfoContext): Future<Stream<JSFunctionType>?>? = null

  override fun getQuickInfoAt(element: PsiElement, originalElement: PsiElement, originalFile: VirtualFile): CompletableFuture<String?> =
    completedFuture(getConnection(element.containingFile.virtualFile).invokeSynchronously(HoverMethod.create(element)))

  override fun terminateStartedProcess() {
    val server = connection ?: return
    connection = null
    server.stop(true, false)
    openedFiles.clear()
  }

  override fun highlight(file: PsiFile): CompletableFuture<List<JSAnnotationError>>? {
    val virtualFile = file.virtualFile
    return completedFuture(getConnection(virtualFile).getDiagnostics(virtualFile)?.map {
      DenoAnnotationError(it, virtualFile.canonicalPath)
    })
  }

  override fun canHighlight(file: PsiFile) = DialectDetector.isTypeScript(file)

  override fun isAcceptable(file: VirtualFile) = DialectDetector.getLanguageDialect(file, project) is TypeScriptLanguageDialect

  override fun openEditor(file: VirtualFile) {
    val document = getDocument(file) ?: return
    if (openedFiles.add(file)) {
      document.addDocumentListener(this, this)
      getConnection(file).fileOpened(file)
    }
  }

  override fun closeLastEditor(file: VirtualFile) {
    val connection = connection ?: return
    val document = getDocument(file) ?: return
    if (openedFiles.remove(file)) {
      connection.fileClosed(file)
      document.removeDocumentListener(this)
    }
  }

  override fun beforeDocumentChange(event: DocumentEvent) {
    connection?.invoke(DidChangeMethod.create(event))
  }

  override fun dispose() = terminateStartedProcess()
}

class DenoCompletionEntry(val item: LspCompletionItem) : CompletionEntry {
  override val name: String get() = item.label

  override fun intoLookupElement() = item.intoLookupElement()
}

fun getDocument(file: VirtualFile) = FileDocumentManager.getInstance().getDocument(file)

fun getFile(document: Document) = FileDocumentManager.getInstance().getFile(document)

class DenoAnnotationError(private val diagnostic: LspDiagnostic, private val path: String?) : JSAnnotationError {
  override fun getLine() = diagnostic.range.start.line

  override fun getColumn() = diagnostic.range.start.character

  override fun getAbsoluteFilePath(): String? = path

  override fun getDescription(): String = diagnostic.message

  override fun getCategory() = when (diagnostic.severity) {
    Error -> ERROR_CATEGORY
    Warning -> WARNING_CATEGORY
    Hint, Information -> INFO_CATEGORY
  }
}