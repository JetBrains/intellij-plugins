// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.lsp.JSFrameworkLspClientDescriptor
import com.intellij.lang.typescript.lsp.JSFrameworkLspClientProvider
import com.intellij.lang.typescript.lsp.JSLspClientWidgetItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspDiagnosticsDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentColorDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentHighlightsDisabled
import com.intellij.platform.lsp.api.customization.LspDocumentLinkDisabled
import com.intellij.platform.lsp.api.customization.LspFindReferencesDisabled
import com.intellij.platform.lsp.api.customization.LspFoldingRangeDisabled
import com.intellij.platform.lsp.api.customization.LspFormattingDisabled
import com.intellij.platform.lsp.api.customization.LspGoToDefinitionDisabled
import com.intellij.platform.lsp.api.customization.LspGoToTypeDefinitionDisabled
import com.intellij.platform.lsp.api.customization.LspHoverDisabled
import com.intellij.platform.lsp.api.customization.LspInlayHintDisabled
import com.intellij.platform.lsp.api.customization.LspSemanticTokensDisabled
import com.intellij.platform.lsp.api.customization.LspSignatureHelpDisabled
import com.intellij.platform.lsp.api.customization.LspWorkspaceSymbolDisabled
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import org.eclipse.lsp4j.ConfigurationItem
import org.jetbrains.astro.AstroIcons
import org.jetbrains.astro.service.settings.AstroServiceConfigurable
import org.jetbrains.astro.service.settings.AstroServiceSettings


class AstroLspClientProvider : JSFrameworkLspClientProvider(AstroLspServerActivationRule) {
  override fun createLspServerDescriptor(project: Project): JSFrameworkLspClientDescriptor = AstroLspClientDescriptor(project)

  override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?): LspClientWidgetItem =
    JSLspClientWidgetItem(lspClient, currentFile, AstroIcons.Astro, AstroIcons.Astro, AstroServiceConfigurable::class.java)
}

class AstroLspClientDescriptor(project: Project) : JSFrameworkLspClientDescriptor(project, AstroLspServerActivationRule, "Astro") {
  override val lspCustomization: LspCustomization = object : LspCustomization() {
    override val semanticTokensCustomizer = LspSemanticTokensDisabled
    override val goToDefinitionCustomizer = LspGoToDefinitionDisabled
    override val goToTypeDefinitionCustomizer = LspGoToTypeDefinitionDisabled
    override val documentLinkCustomizer = LspDocumentLinkDisabled
    override val hoverCustomizer = LspHoverDisabled
    override val completionCustomizer = AstroLspCompletionSupport()
    override val diagnosticsCustomizer = LspDiagnosticsDisabled
    override val formattingCustomizer = LspFormattingDisabled
    override val findReferencesCustomizer = LspFindReferencesDisabled
    override val documentColorCustomizer = LspDocumentColorDisabled
    override val foldingRangeCustomizer = LspFoldingRangeDisabled
    override val inlayHintCustomizer = LspInlayHintDisabled
    override val workspaceSymbolCustomizer = LspWorkspaceSymbolDisabled
    override val documentHighlightsCustomizer = LspDocumentHighlightsDisabled
    override val signatureHelpCustomizer = LspSignatureHelpDisabled
  }

  override fun getWorkspaceConfiguration(item: ConfigurationItem): Any? {
    val section = item.section ?: return super.getWorkspaceConfiguration(item)
    val root = AstroServiceSettings.getParsedWorkspaceConfigurationGson(project)
    val value = root[section] ?: return super.getWorkspaceConfiguration(item)
    return value
  }
}
