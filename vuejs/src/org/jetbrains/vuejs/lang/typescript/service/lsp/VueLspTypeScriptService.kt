// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.google.gson.JsonElement
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetElementTypeRequestArgs
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetSymbolTypeRequestArgs
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptGetTypePropertiesRequestArgs
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.lang.typescript.lsp.LspAnnotationErrorFilter
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.platform.lsp.impl.LspServerImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import kotlinx.coroutines.delay
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MarkupContent
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.typescript.service.VueServiceSetActivationRule
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.VueSettings

class VueLspTypeScriptService(project: Project) : BaseLspTypeScriptService(project, VueLspServerSupportProvider::class.java) {
  override val name: String
    get() = VueBundle.message("vue.service.name")
  override val prefix: String
    get() = VueBundle.message("vue.service.prefix")

  override fun isAcceptable(file: VirtualFile) = VueServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)

  override fun createQuickInfoResponse(markupContent: MarkupContent): TypeScriptQuickInfoResponse {
    // Feel free to replace with the longer version
    // ```typescript
    // let __VLS_ctx: CreateComponentPublicInstance<Readonly<ExtractPropTypes<__VLS_TypePropsToOption<{
    val internalVolarLeakMarker = """
      ```typescript
      let __VLS_ctx
    """.trimIndent()
    val hrMarker = "\n\n---\n\n"
    if (markupContent.value.startsWith(internalVolarLeakMarker)) {
      val index = markupContent.value.indexOf(hrMarker)
      if (index > -1 && index + hrMarker.length < markupContent.value.length) {
        val mc = MarkupContent(
          markupContent.kind,
          markupContent.value.substring(index + hrMarker.length)
        )
        return super.createQuickInfoResponse(mc)
      }
    }
    return super.createQuickInfoResponse(markupContent)
  }

  override fun createAnnotationErrorFilter() = VueLspAnnotationErrorFilter(project)

  override suspend fun getIdeType(args: TypeScriptGetElementTypeRequestArgs): JsonElement? {
    val server = getServer() ?: return null
    if (!isNewEvalModeServer(server)) return null
    awaitServerRunningState(server)
    return server.sendRequest { (it as JSFrameworkLsp4jServer).getElementType(args) }
  }

  override suspend fun getIdeSymbolType(args: TypeScriptGetSymbolTypeRequestArgs): JsonElement? {
    val server = getServer() ?: return null
    if (!isNewEvalModeServer(server)) return null
    awaitServerRunningState(server)
    return server.sendRequest { (it as JSFrameworkLsp4jServer).getSymbolType(args) }
  }

  override suspend fun getIdeTypeProperties(args: TypeScriptGetTypePropertiesRequestArgs): JsonElement? {
    val server = getServer() ?: return null
    if (!isNewEvalModeServer(server)) return null
    awaitServerRunningState(server)
    return server.sendRequest { (it as JSFrameworkLsp4jServer).getTypeProperties(args) }
  }

  private fun isNewEvalModeServer(server: LspServer): Boolean {
    // the lifecycle of LspServer is shorter than of TypeScriptService
    val descriptor = server.descriptor
    return descriptor is VueLspServerDescriptor && descriptor.newEvalMode
  }

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean {
    return virtualFile.extension == "vue" || super.supportsTypeEvaluation(virtualFile, element)
  }

  override fun isTypeEvaluationEnabled(): Boolean =
    project.service<VueSettings>().let { it.serviceType != VueServiceSettings.DISABLED && it.useTypesFromServer }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is VueJSLanguage || file.language is VueTSLanguage
  }

  private suspend fun awaitServerRunningState(server: LspServerImpl) {
    while (true) {
      when (server.state) {
        LspServerState.Initializing -> delay(10L)
        else -> return
      }
    }
  }
}

class VueLspAnnotationErrorFilter(project: Project) : LspAnnotationErrorFilter(project) {
  override fun isProblemEnabled(diagnostic: Diagnostic): Boolean {
    if (diagnostic.message == "Unknown at rule @apply") return false
    return super.isProblemEnabled(diagnostic)
  }
}