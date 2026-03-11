// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.intellij.lang.typescript.lsp.BaseLspTypeScriptServiceCompletionSupport
import com.intellij.lang.typescript.lsp.JSFrameworkLspServerDescriptor
import com.intellij.lang.typescript.lsp.getTypeScriptServiceDirectory
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.Lsp4jServer
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.customization.LspCompletionCustomizer
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspDiagnosticsCustomizer
import com.intellij.platform.lsp.api.customization.LspDiagnosticsSupport
import com.intellij.platform.lsp.api.customization.LspDocumentColorDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentLinkDisabled
import com.intellij.platform.lsp.api.customization.LspFindReferencesDisabled
import com.intellij.platform.lsp.api.customization.LspFoldingRangeDisabled
import com.intellij.platform.lsp.api.customization.LspFormattingDisabled
import com.intellij.platform.lsp.api.customization.LspGoToDefinitionDisabled
import com.intellij.platform.lsp.api.customization.LspGoToTypeDefinitionDisabled
import com.intellij.platform.lsp.api.customization.LspHoverDisabled
import com.intellij.platform.lsp.api.customization.LspInlayHintCustomizer
import com.intellij.platform.lsp.api.customization.LspInlayHintDisabled
import com.intellij.platform.lsp.api.customization.LspSemanticTokensDisabled
import com.intellij.platform.lsp.api.customization.LspWorkspaceSymbolCustomizer
import com.intellij.platform.lsp.api.customization.LspWorkspaceSymbolDisabled
import org.eclipse.lsp4j.Diagnostic
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime

class VueLspServerHybridModeDescriptor(
  project: Project,
  private val runtime: VueServiceRuntime,
) : JSFrameworkLspServerDescriptor(
  project = project,
  activationRule = VueLspServerHybridModeActivationRule(runtime),
  presentableName = "Vue",
) {
  override val lsp4jServerClass: Class<out Lsp4jServer>
    get() = VueHMLsp4jServer::class.java

  override val lspCustomization: LspCustomization
    get() = object : LspCustomization() {
      override val semanticTokensCustomizer: LspSemanticTokensDisabled =
        LspSemanticTokensDisabled

      override val goToDefinitionCustomizer: LspGoToDefinitionDisabled =
        LspGoToDefinitionDisabled

      override val goToTypeDefinitionCustomizer: LspGoToTypeDefinitionDisabled =
        LspGoToTypeDefinitionDisabled

      override val documentLinkCustomizer: LspDocumentLinkDisabled =
        LspDocumentLinkDisabled

      override val hoverCustomizer: LspHoverDisabled =
        LspHoverDisabled

      override val formattingCustomizer: LspFormattingDisabled =
        LspFormattingDisabled

      override val findReferencesCustomizer: LspFindReferencesDisabled =
        LspFindReferencesDisabled

      override val documentColorCustomizer: LspDocumentColorDisabled =
        LspDocumentColorDisabled

      override val foldingRangeCustomizer: LspFoldingRangeDisabled =
        LspFoldingRangeDisabled

      override val inlayHintCustomizer: LspInlayHintCustomizer =
        LspInlayHintDisabled

      override val workspaceSymbolCustomizer: LspWorkspaceSymbolCustomizer =
        LspWorkspaceSymbolDisabled

      override val diagnosticsCustomizer: LspDiagnosticsCustomizer =
        object : LspDiagnosticsSupport() {
          override fun getMessage(diagnostic: Diagnostic): String {
            return VueBundle.message("vue.configurable.title") + ": " + super.getTooltip(diagnostic)
          }
        }

      override val completionCustomizer: LspCompletionCustomizer =
        BaseLspTypeScriptServiceCompletionSupport()
    }

  override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient {
    return VueHybridModeLsp4jClient(
      project = project,
      handler = handler,
      lspServerSupportProvider = VueLspServerHybridModeSupportProvider.getProviderClass(runtime),
      runtime = runtime,
    )
  }

  override fun getArgumentsForLspServer(): List<String> {
    val tsPath = runReadActionBlocking { getTypeScriptServiceDirectory(project) }
    return listOf("--tsdk=$tsPath") + super.getArgumentsForLspServer()
  }
}
