package com.intellij.deno.service

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.deno.*
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.execution.withBackgroundProgress
import com.intellij.lang.typescript.compiler.TypeScriptServiceRestarter
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptServiceCompletionSupport
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.NlsSafe
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
    object : LspServerWidgetItem(lspServer, currentFile, DenoUtil.getDefaultDenoIcon(), DenoConfigurable::class.java) {
      override val versionPostfix: @NlsSafe String
        get() {
          val postfix = super.versionPostfix
          val indexOf = postfix.indexOf("(")
          return if (indexOf > 1) postfix.substring(0, indexOf) else postfix
        }
    }
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
    catch (_: IndexNotReadyException) {
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

  override val lspSemanticTokensSupport = null
  override val lspGoToDefinitionSupport = false
  override val lspGoToTypeDefinitionSupport = false
  override val lspHoverSupport = false
  override val lspCompletionSupport = BaseLspTypeScriptServiceCompletionSupport()
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
      if (command.command != "deno.cache") {
        super.executeCommand(server, contextFile, command)
        return
      }

      val manager = FileDocumentManager.getInstance()
      val document = manager.getDocument(contextFile) ?: return
      if (manager.isDocumentUnsaved(document)) {
        FileDocumentManager.getInstance().saveDocument(document)
      }

      ApplicationManager.getApplication().executeOnPooledThread {
        val workingDirectory = findDenoConfig(project, contextFile)?.parent ?: project.guessProjectDir()

        val commandLine = GeneralCommandLine(DenoSettings.getService(server.project).getDenoPath(), "cache", contextFile.path)
          .withWorkingDirectory(workingDirectory?.toNioPath())
        val processHandler = withBackgroundProgress(project, DenoBundle.message("deno.cache.name")) {
          KillableColoredProcessHandler(commandLine)
        }

        processHandler.addProcessListener(object : ProcessListener {
          override fun processTerminated(event: ProcessEvent) {
            processHandler.notifyTextAvailable(DenoBundle.message("deno.cache.done"), ProcessOutputTypes.SYSTEM)

            ApplicationManager.getApplication().invokeLater(Runnable {
              DenoSettings.getService(project).updateLibraries()
              TypeScriptServiceRestarter.restartServices(project)
              DaemonCodeAnalyzer.getInstance(project).restart()
            }, project.disposed)
          }
        })

        ApplicationManager.getApplication().invokeLater {
          NodeCommandLineUtil.showConsole(processHandler, "DenoConsole", project, emptyList<Filter>(), DenoBundle.message("deno.cache.title"))
        }
      }
    }
  }
}