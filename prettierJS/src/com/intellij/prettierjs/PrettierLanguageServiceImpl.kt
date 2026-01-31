// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.ServiceInactivityTracker
import com.intellij.lang.javascript.service.JSLanguageServiceBase
import com.intellij.lang.javascript.service.JSLanguageServiceQueue
import com.intellij.lang.javascript.service.JSLanguageServiceQueueImpl
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceInitialState
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceNodeStdProtocolBase
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.prettierjs.PrettierUtil.getPrettierLanguageServicePath
import com.intellij.util.PathUtilRt
import com.intellij.webcore.util.JsonUtil
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile

internal class PrettierLanguageServiceImpl(
  project: Project,
  private val workDir: VirtualFile,
) : JSLanguageServiceBase(project), PrettierLanguageService {
  private val inactivityTracker: ServiceInactivityTracker

  @Volatile
  var error: PrettierError? = null

  init {
    val manager = PrettierLanguageServiceManager.getInstance(project)
    inactivityTracker = ServiceInactivityTracker.startTracking(manager.cs, manager.inactivityTimeoutMs) {
      PrettierLanguageServiceManager.getInstance(project).terminateInactiveService(this)
    }
  }

  override fun dispose() {
    inactivityTracker.stopTracking()
    updateError(null)
    super.dispose()
  }

  override fun format(
    filePath: String,
    ignoreFilePath: String?,
    text: String,
    prettierPackage: NodePackage,
    range: TextRange?,
  ): CompletableFuture<PrettierLanguageService.FormatResult?> { // Prettier may remove a trailing line break in Vue (WEB-56144, WEB-52196, https://github.com/prettier/prettier/issues/13399),
    // even if the range doesn't include that line break. `forceLineBreakAtEof` helps to work around the problem.
    val forceLineBreakAtEof = range != null && range.endOffset < text.length && text.endsWith("\n")

    return withServiceAndEnv(prettierPackage, { msg -> PrettierLanguageService.FormatResult.error(msg) }) { process ->
      val command = ReformatFileCommand(project, filePath, prettierPackage, ignoreFilePath, text, range, false, -1)
      val answer = process.execute(command)?.answer ?: return@withServiceAndEnv null
      val res = parseReformatResponse(answer, forceLineBreakAtEof)
      val newError = when {
        res.error != null -> PrettierError.ShowDetails(res.error)
        res.unsupported -> PrettierError.Unsupported(PrettierBundle.message("not.supported.file", PathUtilRt.getFileName(filePath)))
        else -> null
      }
      updateError(newError)
      res
    }
  }

  override fun resolveConfig(
    filePath: String,
    prettierPackage: NodePackage,
  ): CompletableFuture<PrettierLanguageService.ResolveConfigResult?> {
    val normalizedPath = JSLanguageServiceUtil.normalizeNameAndPath(filePath)

    return withServiceAndEnv(prettierPackage, { msg -> PrettierLanguageService.ResolveConfigResult.error(msg) }) { process ->
      val command = ResolveConfigCommand(project, normalizedPath, prettierPackage, false)
      val response = process.execute(command)?.answer ?: return@withServiceAndEnv null
      val res = parseResolveConfigResponse(response)
      updateError(res.error?.let { PrettierError.ShowDetails(it) })
      res
    }
  }

  private fun <R> withServiceAndEnv(
    prettierPackage: NodePackage,
    errorResult: (String) -> R,
    body: suspend (process: JSLanguageServiceQueue) -> R?,
  ): CompletableFuture<R?> {
    checkEnvAndSetError(prettierPackage)?.let { err ->
      return CompletableFuture.completedFuture(errorResult(err.message))
    }
    return inactivityTracker.useService {
      PrettierLanguageServiceManager.getInstance(project).cs.future {
        val process = getProcess()
        if (process == null || !process.isValid) {
          val msg = PrettierBundle.message("service.not.started.message")
          updateError(PrettierError.ShowDetails(msg))
          return@future errorResult(msg)
        }
        body(process)
      }
    }
  }

  private fun checkEnvAndSetError(prettierPackage: NodePackage): PrettierError? {
    val err = checkNodeAndPackage(project, prettierPackage)
    if (err != null) updateError(err)
    return err
  }

  private fun checkNodeAndPackage(project: Project, nodePackage: NodePackage): PrettierError? {
    val interpreterRef = NodeJsInterpreterRef.createProjectRef()
    val nodeJsInterpreter = try {
      NodeInterpreterUtil.getValidInterpreterOrThrow(interpreterRef.resolve(project))
    }
    catch (_: com.intellij.execution.ExecutionException) {
      return PrettierError.NodeSettings()
    }

    if (nodePackage.isEmptyPath) {
      return PrettierError.EditSettings()
    }
    if (!nodePackage.isValid(project, nodeJsInterpreter)) {
      return PrettierError.InstallPackage()
    }
    val nodePackageVersion = nodePackage.getVersion(project)
    if (nodePackageVersion != null && nodePackageVersion < PrettierUtil.MIN_VERSION) {
      return PrettierError.ShowDetails(
        PrettierBundle.message("error.unsupported.version", PrettierUtil.MIN_VERSION.rawVersion),
      )
    }
    return null
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
    val cursorOffset = JsonUtil.getChildAsInteger(jsonObject, "cursorOffset", -1)
    if (forceLineBreakAtEof && !formattedResult.endsWith("\n")) {
      // Prettier may remove a trailing line break in Vue (WEB-56144, WEB-52196, https://github.com/prettier/prettier/issues/13399),
      // even if the range doesn't include that line break. `forceLineBreakAtEof` helps to work around the problem.
      formattedResult += '\n'
    }
    return PrettierLanguageService.FormatResult.formatted(formattedResult, cursorOffset)
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


  private fun updateError(newError: PrettierError?) {
    if (error == newError) return
    error = newError
    PrettierLanguageServiceManager.getInstance(myProject).jsLinterStateChanged()
  }

  override suspend fun createLanguageServiceQueue(): JSLanguageServiceQueue = JSLanguageServiceQueueImpl(myProject, Protocol(myProject), null, myDefaultReporter)

  private inner class Protocol(
    project: Project,
  ) : JSLanguageServiceNodeStdProtocolBase("prettier", project) {
    override fun addNodeProcessAdditionalArguments(targetRun: NodeTargetRun) {
      super.addNodeProcessAdditionalArguments(targetRun)
      JSLanguageServiceUtil.addNodeProcessArgumentsFromRegistry(targetRun.commandLineBuilder, serviceName) {
        Registry.stringValue("prettier.service.node.arguments")
      }

      // Check for Node.js version and add experimental strip types if needed
      PrettierUtil.addExperimentalStripTypesIfNeeded(targetRun, serviceName)
      targetRun.path(getPrettierLanguageServicePath().absolutePathString())
    }

    override val workingDirectory: String?
      get() {
        if (ApplicationManager.getApplication()?.isUnitTestMode() == true) { // `myProject.getBasePath()` returns a non-existent directory in unit test mode
          // The problem is that Yarn PnP can't detect .pnp.cjs when the process is started in a non-existent directory.
          myProject.guessProjectDir()?.let { return it.path }
        }
        return JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(workDir)
      }

    // PrettierPostFormatProcessor runs under write action. Read action here is not needed and it would block the service startup
    override fun needReadActionToCreateState(): Boolean = false

    override fun createState(): JSLanguageServiceInitialState {
      val service = getPrettierLanguageServicePath().resolve("prettier-plugin-provider.js")
      if (!service.isRegularFile()) {
        thisLogger().error("prettier language service plugin not found")
      }
      return JSLanguageServiceInitialState().also {
        it.pluginName = "prettier"
        it.pluginPath = LocalFilePath.create(service.absolutePathString())
      }
    }

    override fun getNodeCommandLineConfiguratorOptions(project: Project): NodeCommandLineConfigurator.Options = NodeCommandLineConfigurator.defaultOptions(myProject)
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
    val cursorOffset: Int,
  ) : JSLanguageServiceObject, JSLanguageServiceSimpleCommand {
    val path: LocalFilePath = LocalFilePath.create(FileUtil.toSystemDependentName(filePath))
    val prettierPath: LocalFilePath = LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.name
                                                           ?: prettierPackage.systemDependentPath)
    val packageJsonPath: LocalFilePath? = LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.let {
      FileUtil.toSystemDependentName(it.getPackageJsonPath(project)!!)
    })
    val ignoreFilePath: LocalFilePath? = ignoreFilePath?.let { LocalFilePath.create(FileUtil.toSystemDependentName(it)) }
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
    val prettierPath: LocalFilePath = LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.name
                                                           ?: prettierPackage.systemDependentPath)
    val packageJsonPath: LocalFilePath? = LocalFilePath.create((prettierPackage as? YarnPnpNodePackage)?.let {
      FileUtil.toSystemDependentName(it.getPackageJsonPath(project)!!)
    })

    override fun toSerializableObject(): JSLanguageServiceObject = this
    override val command: String = "resolveConfig"
    override fun getPresentableText(project: Project): String = PrettierBundle.message("progress.title")
  }
}
