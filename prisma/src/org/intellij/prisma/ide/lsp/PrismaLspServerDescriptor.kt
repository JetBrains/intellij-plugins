package org.intellij.prisma.ide.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.lang.PrismaFileType

class PrismaLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Prisma") {

  override fun isSupportedFile(file: VirtualFile) = file.fileType == PrismaFileType

  override fun createCommandLine(): GeneralCommandLine {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
    if (interpreter !is NodeJsLocalInterpreter) {
      // shouldn't happen, checked in PrismaLspServerSupportProvider
      throw ExecutionException(PrismaBundle.message("prisma.interpreter.not.configured"))
    }

    val lsp = JSLanguageServiceUtil.getPluginDirectory(javaClass, "language-server/prisma-language-server.js")
    if (lsp == null || !lsp.exists()) {
      // broken plugin installation?
      throw ExecutionException(PrismaBundle.message("prisma.language.server.not.found"))
    }

    return GeneralCommandLine().apply {
      withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
      withCharset(Charsets.UTF_8)
      addParameter(lsp.path)
      addParameter("--stdio")

      NodeCommandLineConfigurator.find(interpreter)
        .configure(this, NodeCommandLineConfigurator.defaultOptions(project))
    }
  }

  // references resolution is implemented without using the LSP server
  override val lspGoToDefinitionSupport = false

  // code completion is implemented without using the LSP server
  override val lspCompletionSupport = null
}
