package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionEntry
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.PathUtil
import com.intellij.util.containers.toArray
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.streams.asStream

class DenoTypeScriptService(private val project: Project) : TypeScriptService, DenoLspServer.Client, Disposable {
  private var connection: DenoLspServer? = null
  private var coroutineScope: CoroutineScope? = null
  private val openedFiles = mutableSetOf<VirtualFile>()
  private val highlights = mutableMapOf<String, List<JSAnnotationError>>()

  companion object {
    fun getInstance(project: Project): DenoTypeScriptService = project.getService(DenoTypeScriptService::class.java)
  }

  // TODO: call it somewhere
  fun start(scope: CoroutineScope = MainScope()) {
    coroutineScope = scope
    scope.launch {
      connection = DenoLspServer(this@DenoTypeScriptService).also { it.connect() }
    }
  }

  override fun isDisabledByContext(context: VirtualFile) = false

  override fun getStatusText() = connection?.let { "Deno ${it.version}" } ?: "Deno"

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement) =
    if (parameters.isExtendedCompletion) NON else MERGE

  override fun updateAndGetCompletionItems(virtualFile: VirtualFile, document: Document, positionInFileOffset: Int) =
    connection?.let {
      coroutineScope?.future {
        it.updateAndGetCompletionItems(virtualFile, document, positionInFileOffset)?.toList()
      }
    }

  override fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                          items: List<CompletionEntry>,
                                          document: Document,
                                          positionInFileOffset: Int) =
    connection?.let {
      coroutineScope?.future {
        it.getDetailedCompletionItems(virtualFile, items.asSequence(), document, positionInFileOffset)?.toList()
      }
    }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement) =
    connection?.getNavigationFor(document, sourceElement)?.toList()?.toArray(emptyArray())

  override fun getSignatureHelp(file: PsiFile, document: Document, offset: Int) =
    connection?.let {
      coroutineScope?.future {
        it.getSignatureHelp(file, document, offset)?.asStream()
      }
    }

  override fun getQuickInfoAt(element: PsiElement, originalElement: PsiElement, originalFile: VirtualFile) =
    connection?.let {
      coroutineScope?.future {
        it.getQuickInfoAt(element, originalElement, originalFile)
      }
    }

  override fun terminateStartedProcess() {
    val server = connection ?: return
    connection = null
    Disposer.dispose(server)

    val scope = coroutineScope ?: return
    coroutineScope = null
    scope.cancel("Server process was terminated")

    openedFiles.clear()
    highlights.clear()
  }

  override fun highlight(file: PsiFile) = highlights[file.virtualFile.path]?.let { CompletableFuture.completedFuture(it) }

  override fun canHighlight(file: PsiFile) = DialectDetector.isTypeScript(file)

  override fun isAcceptable(file: VirtualFile) = DialectDetector.getLanguageDialect(file, project) is TypeScriptLanguageDialect

  override fun openEditor(file: VirtualFile) {
    val connection = connection ?: return
    val document = getDocument(file) ?: return
    if (openedFiles.add(file)) {
      document.addDocumentListener(connection, connection)
      connection.openFile(file)
    }
  }

  override fun closeLastEditor(file: VirtualFile) {
    val connection = connection ?: return
    val document = getDocument(file) ?: return
    if (openedFiles.remove(file)) {
      connection.closeFile(file)
      document.removeDocumentListener(connection)
    }
  }

  override fun dispose() = terminateStartedProcess()

  override fun onPublishHighlights(uri: String, diagnostics: Sequence<JSAnnotationError>) {
    highlights[canonicalize(uri)] = diagnostics.toList()
  }
}

fun getDocument(file: VirtualFile) = FileDocumentManager.getInstance().getDocument(file)

fun canonicalize(uri: String) = PathUtil.driveLetterToLowerCase(uri.replace('\\', '/'))