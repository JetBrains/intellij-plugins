package com.intellij.deno.service

import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionEntry
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class DenoLspServer(client: Client) : DocumentListener, Disposable {
  private var client: Client? = client
  private var _version: String? = null

  val version get() = _version ?: "..."

  suspend fun connect() {
    TODO("start process")
    _version = TODO()
  }

  override fun dispose() {
    client = null
    TODO("stop process")
  }

  override fun beforeDocumentChange(event: DocumentEvent) {
    TODO()
  }

  fun openFile(file: VirtualFile) {
    TODO()
  }

  fun closeFile(file: VirtualFile) {
    TODO()
  }

  suspend fun updateAndGetCompletionItems(virtualFile: VirtualFile,
                                          document: Document,
                                          positionInFileOffset: Int): Sequence<CompletionEntry>? {
    TODO("Not yet implemented")
  }

  suspend fun getDetailedCompletionItems(virtualFile: VirtualFile,
                                         items: Sequence<CompletionEntry>,
                                         document: Document,
                                         positionInFileOffset: Int): Sequence<CompletionEntry>? {
    TODO("Not yet implemented")
  }

  fun getNavigationFor(document: Document, sourceElement: PsiElement): Sequence<PsiElement>? {
    TODO("Not yet implemented")
  }

  suspend fun getSignatureHelp(file: PsiFile, document: Document, offset: Int): Sequence<JSFunctionType>? {
    TODO("Not yet implemented")
  }

  suspend fun getQuickInfoAt(element: PsiElement, originalElement: PsiElement, originalFile: VirtualFile): String? {
    TODO("Not yet implemented")
  }

  interface Client {
    fun onPublishHighlights(uri: String, diagnostics: Sequence<JSAnnotationError>)
  }
}