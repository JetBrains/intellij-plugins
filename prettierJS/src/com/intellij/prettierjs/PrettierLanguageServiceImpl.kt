// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.service.*
import com.intellij.lang.javascript.service.protocol.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.webcore.util.JsonUtil
import kotlinx.coroutines.future.future
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class PrettierLanguageServiceImpl(
  project: Project,
  private val workDir: VirtualFile,
) : JSLanguageServiceBase(project), PrettierLanguageService {

  override fun format(
    filePath: String,
    ignoreFilePath: String?,
    text: String,
    prettierPackage: NodePackage,
    range: TextRange?,
  ): CompletableFuture<PrettierLanguageService.FormatResult?>? {
    val process = process
    if (process == null || !process.isValid) {
      return CompletableFuture.completedFuture(
        PrettierLanguageService.FormatResult.error(PrettierBundle.message("service.not.started.message")))
    }

    // Prettier may remove a trailing line break in Vue (WEB-56144, WEB-52196, https://github.com/prettier/prettier/issues/13399),
    // even if the range doesn't include that line break. `forceLineBreakAtEof` helps to work around the problem.
    val forceLineBreakAtEof = range != null && range.endOffset < text.length && text.endsWith("\n")
    val command = ReformatFileCommand(myProject, filePath, prettierPackage, ignoreFilePath, text, range, false)
    return project.service<PrettierLanguageServiceManager>().cs.future {
      val answer = process.execute(command)?.answer ?: return@future null
      parseReformatResponse(answer, forceLineBreakAtEof)
    }
  }

  override fun resolveConfig(
    filePath: String,
    prettierPackage: NodePackage,
  ): CompletableFuture<PrettierLanguageService.ResolveConfigResult?>? {
    val filePath = JSLanguageServiceUtil.normalizeNameAndPath(filePath)
    val process = process
    if (process == null || !process.isValid) {
      return CompletableFuture.completedFuture(
        PrettierLanguageService.ResolveConfigResult.error(PrettierBundle.message("service.not.started.message")))
    }

    val command = ResolveConfigCommand(myProject, filePath, prettierPackage, false)
    return project.service<PrettierLanguageServiceManager>().cs.future {
      val response = process.execute(command)?.answer ?: return@future null
      parseResolveConfigResponse(response)
    }
  }

  private fun parseReformatResponse(response: JSLanguageServiceAnswer, forceLineBreakAtEof: Boolean): PrettierLanguageService.FormatResult {
    val jsonObject = response.element
    val error = JsonUtil.getChildAsString(jsonObject, "error")
    if (error?.isNotEmpty() == true) {
      return PrettierLanguageService.FormatResult.error(error)
    }
    if (JsonUtil.getChildAsBoolean(jsonObject, "ignored", false)) {
      return PrettierLanguageService.FormatResult.IGNORED
    }
    if (JsonUtil.getChildAsBoolean(jsonObject, "unsupported", false)) {
      return PrettierLanguageService.FormatResult.UNSUPPORTED
    }

    var formattedResult = JsonUtil.getChildAsString(jsonObject, "formatted")!!
    if (forceLineBreakAtEof && !formattedResult.endsWith("\n")) {
      // Prettier may remove a trailing line break in Vue (WEB-56144, WEB-52196, https://github.com/prettier/prettier/issues/13399),
      // even if the range doesn't include that line break. `forceLineBreakAtEof` helps to work around the problem.
      formattedResult += '\n'
    }
    return PrettierLanguageService.FormatResult.formatted(formattedResult)
  }

  private fun parseResolveConfigResponse(response: JSLanguageServiceAnswer): PrettierLanguageService.ResolveConfigResult {
    val jsonObject = response.element
    val error = JsonUtil.getChildAsString(jsonObject, "error")
    if (error?.isNotEmpty() == true) {
      return PrettierLanguageService.ResolveConfigResult.error(error)
    }

    val config = JsonUtil.getChildAsObject(jsonObject, "config")
    val prettierConfig = PrettierConfig.createFromJson(config)

    return PrettierLanguageService.ResolveConfigResult.config(prettierConfig)
  }

  override fun createLanguageServiceQueueBlocking(): JSLanguageServiceQueue =
    JSLanguageServiceQueueImpl(myProject,
                               Protocol(myProject, Consumer { o: Any? -> }),
                               null,
                               myDefaultReporter,
                               JSLanguageServiceDefaultCacheData())

  private inner class Protocol(
    project: Project,
    readyConsumer: Consumer<*>,
  ) : JSLanguageServiceNodeStdProtocolBase("prettier", project, readyConsumer) {
    override fun addNodeProcessAdditionalArguments(targetRun: NodeTargetRun) {
      super.addNodeProcessAdditionalArguments(targetRun)
      targetRun.path(JSLanguageServiceUtil.getPluginDirectory(this.javaClass, "prettierLanguageService")!!.absolutePath)
    }

    override val workingDirectory: String?
      get() {
        if (ApplicationManager.getApplication()?.isUnitTestMode() == true) {
          // `myProject.getBasePath()` returns a non-existent directory in unit test mode
          // The problem is that Yarn PnP can't detect .pnp.cjs when the process is started in a non-existent directory.
          myProject.guessProjectDir()?.let { return it.path }
        }
        return FileUtil.toSystemDependentName(workDir.getPath())
      }

    // PrettierPostFormatProcessor runs under write action. Read action here is not needed and it would block the service startup
    override fun needReadActionToCreateState(): Boolean = false

    override fun createState(): JSLanguageServiceInitialState {
      val service = File(JSLanguageServiceUtil.getPluginDirectory(this.javaClass, "prettierLanguageService"), "prettier-plugin-provider.js")
      if (!service.exists()) {
        thisLogger().error("prettier language service plugin not found")
      }
      return JSLanguageServiceInitialState().also {
        it.pluginName = "prettier"
        it.pluginPath = LocalFilePath.create(service.absolutePath)
      }
    }

    override fun dispose() {}

    override fun getNodeCommandLineConfiguratorOptions(project: Project): NodeCommandLineConfigurator.Options =
      NodeCommandLineConfigurator.defaultOptions(myProject)
  }


  @Suppress("unused")
  private class ReformatFileCommand(
    project: Project,
    filePath: String,
    prettierPackage: NodePackage,
    ignoreFilePath: String?,
    val content: String,
    range: TextRange?,
    val flushConfigCache: Boolean,
  ) : JSLanguageServiceObject, JSLanguageServiceSimpleCommand {
    val path: LocalFilePath = LocalFilePath.create(FileUtil.toSystemDependentName(filePath))
    val prettierPath: LocalFilePath =
      LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.name ?: prettierPackage.systemDependentPath)
    val packageJsonPath: LocalFilePath? = LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.let {
      FileUtil.toSystemDependentName(it.getPackageJsonPath(project)!!)
    })
    val ignoreFilePath: String? = ignoreFilePath?.let { FileUtil.toSystemDependentName(it) }
    val start: Int? = range?.startOffset
    val end: Int? = range?.endOffset

    override fun toSerializableObject(): JSLanguageServiceObject = this
    override val command: String = "reformat"
    override fun getPresentableText(project: Project): String = PrettierBundle.message("progress.title")
  }

  @Suppress("unused")
  private class ResolveConfigCommand(
    project: Project,
    filePath: String,
    prettierPackage: NodePackage,
    val flushConfigCache: Boolean,
  ) : JSLanguageServiceObject, JSLanguageServiceSimpleCommand {
    val path: LocalFilePath = LocalFilePath.create(FileUtil.toSystemDependentName(filePath))
    val prettierPath: LocalFilePath =
      LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.name ?: prettierPackage.systemDependentPath)
    val packageJsonPath: LocalFilePath? = LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.let {
      FileUtil.toSystemDependentName(it.getPackageJsonPath(project)!!)
    })

    override fun toSerializableObject(): JSLanguageServiceObject = this
    override val command: String = "resolveConfig"
    override fun getPresentableText(project: Project): String = PrettierBundle.message("progress.title")
  }
}
