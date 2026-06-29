// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptQuickInfoResponse
import com.intellij.lang.typescript.lsp.JSFrameworkLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.eclipse.lsp4j.MarkupContent
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage

class VueLspTakeoverModeTypeScriptService(
  project: Project,
) : JSFrameworkLspTypeScriptService(
  project = project,
  providerClass = VueLspIntegrationTakeoverModeProvider::class.java,
  descriptor = VueLspClientTakeoverModeDescriptor(project),
  activationRule = VueLspServerTakeoverModeActivationRule,
) {
  override val diagnosticsConfiguration: DiagnosticsConfiguration get() = PublishDiagnostics(2)

  override val name: String
    get() = VueBundle.message("vue.service.name")

  override val prefix: String
    get() = VueBundle.message("vue.service.prefix")

  override fun createQuickInfoResponse(
    markupContent: MarkupContent,
  ): TypeScriptQuickInfoResponse {
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

  override fun supportsTypeEvaluation(
    virtualFile: VirtualFile,
    element: PsiElement,
  ): Boolean {
    return /*virtualFile.extension == "vue"
           || */super.supportsTypeEvaluation(virtualFile, element)
  }

  override fun supportsInjectedFile(file: PsiFile): Boolean {
    return file.language is VueJSLanguage
           || file.language is VueTSLanguage
  }
}
