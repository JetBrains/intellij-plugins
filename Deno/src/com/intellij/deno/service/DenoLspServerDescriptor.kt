package com.intellij.deno.service

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import com.intellij.deno.*
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCommandsSupport
import com.intellij.platform.lsp.api.customization.LspFormattingSupport
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.text.nullize
import org.eclipse.lsp4j.Command
import java.nio.file.Files
import java.nio.file.Paths

class DenoLspSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
    if (isDenoFileTypeAcceptable(file) && isDenoEnableForContextDirectory(project, file)) {
      serverStarter.ensureServerStarted(DenoLspServerDescriptor(project))
      if (!useDenoLibrary(project)) {
        setUseDenoLibrary(project)
      }
    }
  }

  override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
    LspServerWidgetItem(lspServer, currentFile, settingsPageClass = DenoConfigurable::class.java)
}


class DenoLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "Deno") {

  private val initObject: JsonElement?

  init {
    initObject = calculateInitializationOptions()
  }

  override fun isSupportedFile(file: VirtualFile) = isDenoFileTypeAcceptable(file)


  override fun createCommandLine(): GeneralCommandLine {
    return DenoSettings.getService(project).getDenoPath().ifEmpty { null }?.let {
      GeneralCommandLine(it, "lsp")
    }.also { DenoTypings.getInstance(project).reloadAsync() } ?: throw RuntimeException("deno is not installed")
  }

  override fun createInitializationOptions(): JsonElement? {
    return initObject
  }

  private fun calculateInitializationOptions(): JsonElement? {
    val pathMacroManager = PathMacroManager.getInstance(project)
    val denoInit = pathMacroManager.expandPath(DenoSettings.getService(project).getDenoInit())

    try {
      val result = JsonParser.parseString(denoInit)
      tryExpandPath(result, "importMap")
      tryExpandPath(result, "config")

      return result
    }
    catch (e: JsonSyntaxException) {
      Logger.getInstance(this.javaClass).warn(e)
    }

    return null
  }


  private fun tryExpandPath(jsonElement: JsonElement, name: String) {
    val basePath = project.basePath
    if (!jsonElement.isJsonObject) return
    val jsonObject = jsonElement.asJsonObject
    val importMap = jsonObject.get(name)
    if (importMap == null || !importMap.isJsonPrimitive) return

    val primitive = importMap.asJsonPrimitive
    if (!primitive.isString) return
    val text = primitive.asString ?: return
    var normalizeAndValidatePath = normalizeAndValidatePath(text, basePath)
    if (normalizeAndValidatePath == null && text == DENO_CONFIG_JSON_NAME) {
      normalizeAndValidatePath = normalizeAndValidatePath(DENO_CONFIG_JSONC_NAME, basePath)
      if (normalizeAndValidatePath == null) {
        normalizeAndValidatePath = tryGetFromIndex(DENO_CONFIG_JSON_NAME) ?: tryGetFromIndex(DENO_CONFIG_JSONC_NAME)
      }
    }

    jsonObject.remove(name)
    if (normalizeAndValidatePath != null) {
      jsonObject.add(name, JsonPrimitive(normalizeAndValidatePath))
    }
  }

  private fun tryGetFromIndex(name: String): String? {
    try {
      val denoJsonFiles = FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.projectScope(project))
      if (denoJsonFiles.size == 1) {
        return FileUtil.toSystemDependentName(denoJsonFiles.first().path)
      }
    }
    catch (e: IndexNotReadyException) {
      //skip
    }
    return null
  }

  private fun normalizeAndValidatePath(text: String, basePath: String?): String? {
    val path = Paths.get(text)
    if (path.isAbsolute) {
      if (Files.exists(path)) return text else return null
    }

    val anotherPath = FileUtil.toSystemDependentName("$basePath/$text")
    return if (Files.exists(Paths.get(anotherPath))) anotherPath else null
  }

  override val lspGoToDefinitionSupport = false
  override val lspHoverSupport = false
  override val lspCompletionSupport = null
  override val lspDiagnosticsSupport = null
  override val lspFindReferencesSupport = null

  override val lspFormattingSupport = object : LspFormattingSupport() {
    override fun shouldFormatThisFileExclusivelyByServer(file: VirtualFile, ideCanFormatThisFileItself: Boolean, serverExplicitlyWantsToFormatThisFile: Boolean): Boolean {
      return DenoSettings.getService(project).isDenoFormattingEnabled()
             && isDenoEnableForContextDirectory(project, file)
    }
  }

  override val lspCommandsSupport: LspCommandsSupport = object : LspCommandsSupport() {
    override fun executeCommand(server: LspServer, contextFile: VirtualFile, command: Command) {
      if (command.command == "deno.cache") {
        ApplicationManager.getApplication().executeOnPooledThread {
          val commandLine = GeneralCommandLine(DenoSettings.getService(server.project).getDenoPath(), "cache", contextFile.path)
          try {
            val text = ExecUtil.execAndGetOutput(commandLine)
            val notification = Notification("LSP window/showMessage", DenoBundle.message("deno.cache.name"),
                                            text.stdout.nullize(true) ?: text.stderr,
                                            NotificationType.INFORMATION)
            notification.notify(project)
            //not the best solution but it works
            ApplicationManager.getApplication().invokeLater {
              TypeScriptService.restartServices(project)
            }
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