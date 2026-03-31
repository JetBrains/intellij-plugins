// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.typescript.compiler.TypeScriptService.CompletionMergeStrategy
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.lang.typescript.lsp.LspAnnotationError
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServerManagerListener
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.application

class AstroLspTypeScriptService(project: Project)
  : JSFrameworkLspTypeScriptService(project, AstroLspServerSupportProvider::class.java, AstroLspServerActivationRule) {
  override val name: String = "Astro LSP"
  override val prefix: String = "Astro"

  init {
    // Volar-based language server sends publishDiagnostics in two phases (syntactic, then semantic).
    // The platform's awaitPendingDiagnostics mechanism cannot reliably synchronize with multi-phase delivery,
    // so we restart the daemon when diagnostics arrive to trigger JSLanguageServiceHighlightingPass with fresh data.
    // See: https://github.com/volarjs/volar.js/blob/master/packages/language-service/lib/features/provideDiagnostics.ts
    LspServerManager.getInstance(project).addLspServerManagerListener(object : LspServerManagerListener {
      override fun diagnosticsReceived(lspServer: LspServer, file: VirtualFile) {
        if (lspServer.providerClass != AstroLspServerSupportProvider::class.java) return
        application.runReadAction {
          PsiManager.getInstance(project).findFile(file)?.let { psiFile ->
            DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "AstroLspTypeScriptService.diagnosticsReceived")
          }
        }
      }
    }, this)
  }

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): CompletionMergeStrategy = CompletionMergeStrategy.MERGE

  override fun isServiceNavigationEnabled(): Boolean = true

  override fun getServiceFixes(file: PsiFile, element: PsiElement?, result: JSAnnotationError): Collection<IntentionAction> {
    return (result as? LspAnnotationError)?.quickFixes ?: emptyList()
  }

  override fun getNavigationFor(document: Document, sourceElement: PsiElement, offsetInSourceElement: Int): Array<PsiElement> {
    if (shouldIgnoreNamespacedComponent(sourceElement)) {
      return emptyArray()
    }
    return super.getNavigationFor(document, sourceElement, offsetInSourceElement)
  }

  private fun shouldIgnoreNamespacedComponent(element: PsiElement): Boolean {
    val type = element.node?.elementType ?: return false
    if (element.parent !is XmlTag) return false
    return (type == XmlTokenType.XML_NAME
            || type == XmlTokenType.XML_START_TAG_START
            || type == XmlTokenType.XML_TAG_NAME)
           && element.text.contains('.')
  }
}