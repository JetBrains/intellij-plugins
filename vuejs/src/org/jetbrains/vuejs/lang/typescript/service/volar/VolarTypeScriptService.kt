// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.TypeScriptMessageBus
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.LspAnnotationError
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.lsp.LspServer
import com.intellij.lsp.api.LspServerManager
import com.intellij.lsp.methods.HoverMethod
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.Diagnostic
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabled
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class VolarTypeScriptService(project: Project) : BaseLspTypeScriptService(project) {
  override fun getLspServers(): Collection<LspServer> =
    LspServerManager.getInstance(project).getServersForProvider(VolarSupportProvider::class.java)

  override val name = "Volar"
  override val serverVersion = volarVersion

  private fun quickInfo(element: PsiElement): TypeScriptQuickInfoResponse? {
    val server = getServer() ?: return null
    val raw = server.invokeSynchronously(HoverMethod.create(server, element)) ?: return null
    val response = TypeScriptQuickInfoResponse().apply {
      displayString = raw.substring("<html><body><pre>".length, raw.length - "</pre></body></html>".length)
    }
    return response
  }

  override fun getQuickInfoAt(element: PsiElement,
                              originalElement: PsiElement,
                              originalFile: VirtualFile): CompletableFuture<TypeScriptQuickInfoResponse?> =
    completedFuture(quickInfo(element))

  override fun createAnnotationError(diagnostic: Diagnostic, virtualFile: VirtualFile): LspAnnotationError {
    return VolarAnnotationError(diagnostic, virtualFile.canonicalPath)
  }

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isVolarFileTypeAcceptable(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile) = isVolarEnabled(project, file)

  override fun restart(recreateToolWindow: Boolean) {
    val lspServerManager = LspServerManager.getInstance(project)
    getLspServers().forEach { lspServerManager.stopServer(it) }
    updateVolarLsp(project, true)
    TypeScriptMessageBus.get(project).changed()
  }
}

class VolarAnnotationError(diagnostic: Diagnostic, path: String?) : LspAnnotationError(diagnostic, path) {
  override fun getDescription(): String = VueBundle.message("volar.error.prefix", diagnostic.message)
}