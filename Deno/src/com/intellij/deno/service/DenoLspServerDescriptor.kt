package com.intellij.deno.service

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.intellij.deno.DenoSettings
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCommandsSupport
import org.eclipse.lsp4j.Command
import java.io.File

class DenoLspSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
    if (DenoSettings.getService(project).isUseDeno()) {
      serverStarter.ensureServerStarted(DenoLspServerDescriptor(project))
    }
  }
}

class DenoLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Deno") {

  override fun isSupportedFile(file: VirtualFile) = file.fileType == TypeScriptFileType.INSTANCE

  override fun createCommandLine(): GeneralCommandLine {
    return DenoSettings.getService(project).getDenoPath().ifEmpty { null }?.let {
      GeneralCommandLine(it, "lsp")
    }.also { DenoTypings.getInstance(project).reloadAsync() } ?: throw RuntimeException("deno is not installed")
  }

  override fun createInitializationOptions(): Any {
    val pathMacroManager = PathMacroManager.getInstance(project)
    val denoInit = pathMacroManager.expandPath(DenoSettings.getService(project).getDenoInit())
    val result = JsonParser.parseString(denoInit)
    expandRelativePath(result, "importMap")
    expandRelativePath(result, "config")

    return result
  }

  private fun expandRelativePath(jsonElement: JsonElement, name: String) {
    val basePath = project.basePath
    if (!jsonElement.isJsonObject) return
    val jsonObject = jsonElement.asJsonObject
    val importMap = jsonObject.get(name)
    if (importMap == null || !importMap.isJsonPrimitive) return

    val primitive = importMap.asJsonPrimitive
    if (!primitive.isString) return
    val text = primitive.asString ?: return
    if (File(text).isAbsolute) return
    jsonObject.remove(name)
    jsonObject.add(name, JsonPrimitive(FileUtil.toSystemDependentName("$basePath/$text")))
  }

  override val lspGoToDefinitionSupport = false
  override val lspCompletionSupport = null
  override val lspDiagnosticsSupport = null

  override val lspCommandsSupport: LspCommandsSupport = object : LspCommandsSupport() {
    override fun executeCommand(server: LspServer, contextFile: VirtualFile, command: Command) {
      if (command.command == "deno.cache") {
        ApplicationManager.getApplication().executeOnPooledThread {
          val commandLine = GeneralCommandLine(DenoSettings.getService(server.project).getDenoPath(), "cache", contextFile.path)
          try {
            ExecUtil.execAndGetOutput(commandLine)
            // TODO fix WEB-60761: ask server to send updated diagnostics (daemon restart doesn't make sense at this point)
          }
          catch (e: ExecutionException) {
            LOG.info("deno cache ${contextFile.path}\ncommand failed: $e")
          }
        }

        return
      }

      super.executeCommand(server, contextFile, command)
    }
  }
}