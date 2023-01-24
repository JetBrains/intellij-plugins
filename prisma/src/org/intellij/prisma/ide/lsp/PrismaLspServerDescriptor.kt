package org.intellij.prisma.ide.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.wsl.WslNodeInterpreter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lsp.LanguageServerConnector
import com.intellij.lsp.LspServerDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.prisma.PrismaBundle
import java.io.File

class PrismaLspServerDescriptor(project: Project, root: VirtualFile) : LspServerDescriptor(project, root) {
  override fun createCommandLine(): GeneralCommandLine {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
    if (interpreter !is NodeJsLocalInterpreter && interpreter !is WslNodeInterpreter) {
      throw ExecutionException(PrismaBundle.message("prisma.interpreter.not.configured"))
    }

    val lsp = JSLanguageServiceUtil.getPluginDirectory(javaClass, "language-server/prisma-language-server.js")
    if (lsp == null || !lsp.exists()) {
      throw ExecutionException(PrismaBundle.message("prisma.language.server.not.found"))
    }

    return GeneralCommandLine().apply {
      withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
      withCharset(Charsets.UTF_8)
      workDirectory = File(root.path)
      addParameter(lsp.path)
      addParameter("--stdio")

      NodeCommandLineConfigurator.find(interpreter)
        .configure(this, NodeCommandLineConfigurator.defaultOptions(project))
    }
  }

  override fun createServerConnector(): LanguageServerConnector {
    val startingCommandLine = createCommandLine()
    LOG.debug("$this: starting server process using: $startingCommandLine")
    return PrismaServerConnector(this, OSProcessHandler(startingCommandLine))
  }

  override fun useGenericCompletion(): Boolean = false

  override fun useGenericNavigation(): Boolean = false
}