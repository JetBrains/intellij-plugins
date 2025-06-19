package org.intellij.prisma.ide.lsp

import com.intellij.application.options.CodeStyle
import com.intellij.lang.typescript.lsp.JSLspServerDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.customization.*
import org.eclipse.lsp4j.ConfigurationItem
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.formatter.settings.PrismaCodeStyleSettings
import org.intellij.prisma.lang.PrismaFileType

class PrismaLspServerDescriptor(project: Project)
  : JSLspServerDescriptor(project, PrismaLspServerActivationRule, PrismaBundle.message("prisma.framework.name")) {

  // code highlighting, references resolution, code completion, and hover info are implemented without using the LSP server
  override val lspSemanticTokensSupport = null
  override val lspGoToDefinitionSupport = false
  override val lspGoToTypeDefinitionSupport = false
  override val lspCompletionSupport = null
  override val lspHoverSupport = false

  override val lspFormattingSupport = object : LspFormattingSupport() {
    override fun shouldFormatThisFileExclusivelyByServer(file: VirtualFile, ideCanFormatThisFileItself: Boolean, serverExplicitlyWantsToFormatThisFile: Boolean): Boolean {
      return file.fileType == PrismaFileType &&
             CodeStyle.getSettings(project).getCustomSettings(PrismaCodeStyleSettings::class.java).RUN_PRISMA_FMT_ON_REFORMAT
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
