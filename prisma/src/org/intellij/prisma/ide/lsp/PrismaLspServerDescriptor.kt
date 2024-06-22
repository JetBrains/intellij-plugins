package org.intellij.prisma.ide.lsp

import com.intellij.lang.typescript.lsp.JSLspServerDescriptor
import com.intellij.openapi.project.Project
import org.intellij.prisma.PrismaBundle

class PrismaLspServerDescriptor(project: Project)
  : JSLspServerDescriptor(project, PrismaServiceSetActivationRule, PrismaBundle.message("prisma.framework.name")) {

  // references resolution is implemented without using the LSP server
  override val lspGoToDefinitionSupport = false

  // code completion is implemented without using the LSP server
  override val lspCompletionSupport = null

  // code formatting is implemented without using the LSP server
  override val lspFormattingSupport = null

  override val lspHoverSupport = false
}
