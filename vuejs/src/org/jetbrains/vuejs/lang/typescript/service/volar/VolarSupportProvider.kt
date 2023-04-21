// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.volar

import com.google.gson.JsonParser
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.concurrency.SensitiveProgressWrapper
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.nodejs.interpreter.wsl.WslNodeInterpreter
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.library.typings.TypeScriptExternalDefinitionsRegistry
import com.intellij.lang.javascript.library.typings.TypeScriptPackageName
import com.intellij.lsp.api.LspServerDescriptor
import com.intellij.lsp.api.LspServerManager
import com.intellij.lsp.api.LspServerSupportProvider
import com.intellij.lsp.api.LspServerSupportProvider.LspServerStarter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.util.ProgressWrapper
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.getTypeScriptServiceDirectory
import org.jetbrains.vuejs.lang.typescript.service.isVolarEnabled
import org.jetbrains.vuejs.lang.typescript.service.isVolarFileTypeAcceptable
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.BiFunction

private val volarVersion = SemVer.parseFromText("1.4.0")
private const val volarPackage = "@volar/vue-language-server"


class VolarSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerStarter) {
    getVueLspServerDescriptor(project, file)?.let { serverStarter.ensureServerStarted(it) }
  }
}

fun getVueLspServerDescriptor(project: Project, file: VirtualFile): LspServerDescriptor? {
  if (!isVolarEnabled(project, file)) return null
  val projectDir = project.guessProjectDir() ?: return null
  return VolarLspServerDescriptor(project, projectDir)
}

class VolarLspServerDescriptor(project: Project, vararg roots: VirtualFile) : LspServerDescriptor(project, "Vue", *roots) {

  override fun isSupportedFile(file: VirtualFile): Boolean {
    return isVolarEnabled(project, file)
  }

  override fun createCommandLine(): GeneralCommandLine {
    val interpreter = NodeJsInterpreterManager.getInstance(project).interpreter
    if (interpreter !is NodeJsLocalInterpreter && interpreter !is WslNodeInterpreter) {
      throw ExecutionException(VueBundle.message("volar.interpreter.error"))
    }
    val volarExecutable = getVolarExecutable()
    if (volarExecutable == null) {
      throw ExecutionException(VueBundle.message("volar.executable.error"))
    }

    return GeneralCommandLine().apply {
      withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
      withCharset(Charsets.UTF_8)
      addParameter(volarExecutable)
      addParameter("--stdio")

      NodeCommandLineConfigurator.find(interpreter).configure(this, NodeCommandLineConfigurator.defaultOptions(project))
    }
  }

  override fun createInitializationOptions(): Any {
    val tsPath = runReadAction { getTypeScriptServiceDirectory(project) }
    return JsonParser.parseString("{'typescript': { 'tsdk': '${tsPath}' }}")
  }
}

fun getVolarExecutable(): String? {
  val packageName = createPackage()
  val path = TypeScriptExternalDefinitionsRegistry.getExactModuleTypingsPath(packageName) ?: return null
  return if (!File(path).isDirectory) null else "$path${FileUtil.toSystemDependentName("/bin/vue-language-server.js")}"
}

private fun createPackage() = TypeScriptPackageName(volarPackage, volarVersion)

fun getVolarExecutableAndRefresh(project: Project): String? {
  val executable = getVolarExecutable()
  if (executable != null) return executable
  scheduleVolarDownloading(project)
  return null
}

fun scheduleVolarDownloading(project: Project) {
  object : Task.Backgroundable(project, VueBundle.message("volar.package.download", volarPackage), true,
                               ALWAYS_BACKGROUND) {
    override fun run(indicator: ProgressIndicator) {
      indicator.isIndeterminate = true
      val future = downloadVolar(project, indicator)
      future.handleAsync(BiFunction { volarPath, _ ->
        if (volarPath != null) {
          runReadAction {

            updateVolarLsp(project, isVolarSettingEnabled(project))
          }
        }
      })
    }
  }.queue()
}

fun isVolarSettingEnabled(project: Project): Boolean {
  val vueSettings = getVueSettings(project)
  return vueSettings.serviceType == VueServiceSettings.AUTO || vueSettings.serviceType == VueServiceSettings.VOLAR
}

fun downloadVolar(project: Project, indicator: ProgressIndicator): CompletableFuture<VirtualFile?> {
  return CompletableFuture.supplyAsync(
    {
      val definitionsRegistry = TypeScriptExternalDefinitionsRegistry.instance
      val installProgress: ProgressWrapper = SensitiveProgressWrapper(indicator)
      val future = definitionsRegistry.installPackage(createPackage(), project, installProgress)
      try {
        future[2, TimeUnit.MINUTES]
      }
      catch (e: InterruptedException) {
        throw RuntimeException(
          JavaScriptBundle.message("npm.failed_to_install_package.title.message", volarPackage), e)
      }
      catch (e: java.util.concurrent.ExecutionException) {
        throw RuntimeException(
          JavaScriptBundle.message("npm.failed_to_install_package.title.message", volarPackage), e)
      }
      catch (e: TimeoutException) {
        installProgress.cancel()
        throw RuntimeException(
          JavaScriptBundle.message("npm.failed_to_install_package.title.message", volarPackage), e)
      }
    }, AppExecutorUtil.getAppExecutorService())
}

fun updateVolarLsp(project: Project, enabled: Boolean) {
  if (enabled) {
    for (openFile in FileEditorManager.getInstance(project).openFiles) {
      val lspServerDescriptor = getVueLspServerDescriptor(project, openFile)
      if (lspServerDescriptor != null) {
        val lspServerManager = LspServerManager.getInstance(project)
        lspServerManager.ensureServerStarted(VolarSupportProvider::class.java, lspServerDescriptor)
        DaemonCodeAnalyzer.getInstance(project).restart()
        return
      }
    }

    if (getVueSettings(project).serviceType == VueServiceSettings.VOLAR) return
  }

  //in all other cases disable volar
  val lspServerManager = LspServerManager.getInstance(project)
  lspServerManager.getServersForProvider(VolarSupportProvider::class.java).forEach { lspServerManager.stopServer(it) }
  DaemonCodeAnalyzer.getInstance(project).restart()
}

fun updateVolarLspAsync(project: Project) {
  ApplicationManager.getApplication().invokeLater(Runnable {
    updateVolarLsp(project, isVolarSettingEnabled(project))
  }, project.disposed)
}

