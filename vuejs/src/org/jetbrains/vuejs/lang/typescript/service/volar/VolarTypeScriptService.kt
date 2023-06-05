// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceAnnotatorCheckerProvider
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.lsp.api.LspServerDescriptor
import com.intellij.lsp.api.LspServerSupportProvider
import com.intellij.lsp.methods.HoverMethod
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabledAndAvailable
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class VolarTypeScriptService(project: Project) : JSFrameworkLspTypeScriptService(project) {
  override fun getProviderClass(): Class<out LspServerSupportProvider> = VolarSupportProvider::class.java

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

  override fun canHighlight(file: PsiFile): Boolean {
    val provider = TypeScriptAnnotatorCheckerProvider.getCheckerProvider(file)
    if (provider !is TypeScriptLanguageServiceAnnotatorCheckerProvider) return false

    return isVolarFileTypeAcceptable(file.virtualFile ?: return false)
  }

  override fun isAcceptable(file: VirtualFile) = isVolarEnabledAndAvailable(project, file)

  override fun isServiceEnabledBySettings(project: Project): Boolean {
    return getVueSettings(project).serviceType == VueServiceSettings.VOLAR
  }

  override fun getLspServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
    return getVolarServerDescriptor(project, file)
  }
}