package org.intellij.prisma.ide.lsp

import com.intellij.application.options.CodeStyle
import com.intellij.lang.typescript.lsp.JSNodeLspServerDescriptor
import com.intellij.markdown.utils.convertMarkdownToHtml
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.customization.*
import org.eclipse.lsp4j.ConfigurationItem
import org.eclipse.lsp4j.Diagnostic
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.formatter.settings.PrismaCodeStyleSettings
import org.intellij.prisma.lang.PrismaFileType

class PrismaLspServerDescriptor(project: Project)
  : JSNodeLspServerDescriptor(project, PrismaLspServerActivationRule, PrismaBundle.message("prisma.framework.name")) {

  // code highlighting, references resolution, code completion, and hover info are implemented without using the LSP server
  override val lspCustomization: LspCustomization = object : LspCustomization() {
    override val semanticTokensCustomizer = LspSemanticTokensDisabled
    override val goToDefinitionCustomizer = LspGoToDefinitionDisabled
    override val goToTypeDefinitionCustomizer = LspGoToTypeDefinitionDisabled
    override val completionCustomizer = LspCompletionDisabled
    override val hoverCustomizer = LspHoverDisabled
    override val findReferencesCustomizer = LspFindReferencesDisabled
    override val foldingRangeCustomizer = LspFoldingRangeDisabled
    override val inlayHintCustomizer: LspInlayHintCustomizer = LspInlayHintDisabled
    override val documentHighlightsCustomizer: LspDocumentHighlightsCustomizer = LspDocumentHighlightsDisabled
    override val documentSymbolCustomizer: LspDocumentSymbolCustomizer = LspDocumentSymbolDisabled
    override val signatureHelpCustomizer: LspSignatureHelpCustomizer = LspSignatureHelpDisabled

    override val diagnosticsCustomizer: LspDiagnosticsCustomizer = object : LspDiagnosticsSupport() {
      override fun getTooltip(diagnostic: Diagnostic): @NlsSafe String = convertMarkdownToHtml(diagnostic.message)
    }

    override val formattingCustomizer = object : LspFormattingSupport() {
      override fun shouldFormatThisFileExclusivelyByServer(file: VirtualFile, ideCanFormatThisFileItself: Boolean, serverExplicitlyWantsToFormatThisFile: Boolean): Boolean {
        return file.fileType == PrismaFileType &&
               CodeStyle.getSettings(project).getCustomSettings(PrismaCodeStyleSettings::class.java).RUN_PRISMA_FMT_ON_REFORMAT
      }
    }
  }

  override fun getWorkspaceConfiguration(item: ConfigurationItem): Any? {
    if (item.section == "prisma") {
      return object {
        @Suppress("unused")
        val enableDiagnostics: Boolean = true
      }
    }
    return super.getWorkspaceConfiguration(item)
  }
}
