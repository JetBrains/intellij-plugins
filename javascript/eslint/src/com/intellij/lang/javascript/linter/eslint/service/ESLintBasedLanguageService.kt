package com.intellij.lang.javascript.linter.eslint.service

import com.intellij.execution.process.ProcessHandler
import com.intellij.idea.AppMode
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRun.Companion.shouldEnableRemoteDevelopmentUsingTargetsApi
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.lang.javascript.linter.ExtendedLinterState
import com.intellij.lang.javascript.linter.JSLinterConfiguration
import com.intellij.lang.javascript.linter.JSNpmLinterState
import com.intellij.lang.javascript.linter.eslint.ESLintJsonProblemsParser
import com.intellij.lang.javascript.linter.eslint.EslintError
import com.intellij.lang.javascript.linter.eslint.EslintRequestData
import com.intellij.lang.javascript.linter.eslint.EslintUtil
import com.intellij.lang.javascript.linter.eslint.service.protocol.ESLintLanguageServiceInitialState
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSUtil
import com.intellij.lang.javascript.psi.util.JSPluginPathManager
import com.intellij.lang.javascript.service.JSLanguageServiceBase
import com.intellij.lang.javascript.service.JSLanguageServiceQueue
import com.intellij.lang.javascript.service.JSLanguageServiceQueue.ProcessConnector
import com.intellij.lang.javascript.service.JSLanguageServiceQueueImpl
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceNodeStdProtocolBase
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.javascript.service.protocol.LocalFilePath.Companion.create
import com.intellij.lang.javascript.service.protocol.LocalFilePath.Companion.getNioPath
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry.Companion.stringValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import com.intellij.webcore.util.JsonUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.absolutePathString

abstract class ESLintBasedLanguageService<TStoredState : JSNpmLinterState<TStoredState>>
  (project: Project, private val myNodePackage: NodePackage, private val myWorkingDirectory: VirtualFile) : JSLanguageServiceBase(
  project), EslintLanguageServiceClient {
  override fun getNodePackage(): NodePackage {
    return myNodePackage
  }

  override fun getWorkingDirectory(): VirtualFile {
    return myWorkingDirectory
  }


  override fun highlight(
    requestData: EslintRequestData,
    extraOptions: String?
  ): CompletableFuture<EslintLanguageServiceClient.Response<MutableList<EslintError?>?>?>? {
    if (requestData.fileToLintContent.isBlank()) {
      return null
    }
    val virtualFile = requestData.fileToLint

    val configPath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(requestData.specifiedConfigFile)
    val path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile)
    val eslintIgnoreFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(requestData.eslintIgnoreFile)

    if (path == null) {
      return null
    }

    val process = process
    if (process == null) {
      val error = JSLanguageServiceUtil.getLanguageServiceCreationError(this)
      return CompletableFuture.completedFuture<EslintLanguageServiceClient.Response<MutableList<EslintError?>?>?>(
        EslintLanguageServiceClient.Response.error<MutableList<EslintError?>?>(error, false))
    }

    val command = GetErrorsCommand(path,
                                   configPath,
                                   requestData.fileToLintContent,
                                   extraOptions,
                                   eslintIgnoreFilePath,
                                   requestData.fileKind.stringValue,
                                   requestData.isFlatConfigMode)
    return cs.async {
      val answer = process.execute(command)?.answer ?: return@async null
      val languageServiceError: @NlsSafe String? = JsonUtil.getChildAsString(answer.element, "error")
      if (languageServiceError != null) {
        val isNoConfigFile = JsonUtil.getChildAsBoolean(answer.element, "isNoConfigFile", false)
        return@async EslintLanguageServiceClient.Response.error<MutableList<EslintError?>?>(languageServiceError, isNoConfigFile)
      }
      val parser = ESLintJsonProblemsParser.parse(answer.element)
      EslintLanguageServiceClient.Response<MutableList<EslintError?>?>(parser.fileLevelError, parser.errors, false)
    }.asCompletableFuture()
  }

  override fun fixFile(
    requestData: EslintRequestData,
    extraOptions: String?
  ): CompletableFuture<EslintLanguageServiceClient.Response<String?>?>? {
    val virtualFile = requestData.fileToLint
    val configPath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(requestData.specifiedConfigFile)
    val path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile)
    val ignoreFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(requestData.eslintIgnoreFile)
    if (path == null) {
      return null
    }

    val process = process
    if (process == null) {
      return CompletableFuture.completedFuture<EslintLanguageServiceClient.Response<String?>?>(
        EslintLanguageServiceClient.Response.error<String?>(JSLanguageServiceUtil.getLanguageServiceCreationError(this), false))
    }

    val command = FixErrorsCommand(path,
                                   configPath,
                                   requestData.fileToLintContent,
                                   extraOptions,
                                   ignoreFilePath,
                                   requestData.fileKind.stringValue,
                                   requestData.isFlatConfigMode)
    return cs.async {
      val answer = process.execute(command)?.answer ?: return@async null
      processFixFileResponse(answer)
    }.asCompletableFuture()
  }


  protected abstract fun getConfigurationClass(): Class<out JSLinterConfiguration<TStoredState?>?>

  protected open fun fillInitialProtocolState(
    protocolState: ESLintLanguageServiceInitialState,
    storedState: TStoredState
  ) {
    val packagePath: String?
    if (myNodePackage is YarnPnpNodePackage) {
      packagePath = myNodePackage.name
      val packageJsonPath: String? = myNodePackage.getPackageJsonPath(myProject)
      checkNotNull(packageJsonPath) { "Cannot find package.json path for " + myNodePackage }
      protocolState.packageJsonPath = LocalFilePath.create(FileUtil.toSystemDependentName(packageJsonPath))
    }
    else {
      packagePath = myNodePackage.systemDependentPath
    }
    val localFilePackagePath = LocalFilePath.create(packagePath)
    if (myNodePackage.name == StandardJSUtil.PACKAGE_NAME) {
      protocolState.standardPackagePath = localFilePackagePath
    }
    else {
      protocolState.eslintPackagePath = localFilePackagePath
    }
    val version = myNodePackage.getVersion(myProject)
    protocolState.linterPackageVersion = if (version != null) version.rawVersion else ""
  }

  override suspend fun createLanguageServiceQueue(): JSLanguageServiceQueue? {
    var processConnector: ProcessConnector? = null
    if (shouldEnableRemoteDevelopmentUsingTargetsApi()) {
      // Start ESLint service under progress. It will show run target initialization progress, e.g. useful for SSH.
      processConnector = object : ProcessConnector {
        override fun connectToProcessHandler(handler: ProcessHandler) {}

        override fun disconnectFromProcessHandler(closeAssociatedConsoleView: Boolean) {}
      }
    }
    return JSLanguageServiceQueueImpl(
      myProject,
      Protocol(StringUtil.toLowerCase(debugName), myProject),
      processConnector, myDefaultReporter)
  }

  private val extendedState: ExtendedLinterState<TStoredState?>
    get() = JSLinterConfiguration.getInstance(myProject, this.getConfigurationClass()).getExtendedState()

  private abstract class BaseCommand(
    fileName: String?,
    configPath: String?,
    var content: String?,
    var extraOptions: String?,
    ignoreFilePath: String?,
    var fileKind: String?,
    var flatConfig: Boolean
  ) : JSLanguageServiceCommand, JSLanguageServiceSimpleCommand, JSLanguageServiceObject {
    var fileName: LocalFilePath?
    var configPath: LocalFilePath?
    var ignoreFilePath: LocalFilePath?

    init {
      this.fileName = create(fileName)
      this.configPath = create(configPath)
      this.ignoreFilePath = create(ignoreFilePath)
    }


    override val timeout: Long
      get() = EslintUtil.getTimeout()

    override fun toSerializableObject(): JSLanguageServiceObject {
      return this
    }
  }

  private class GetErrorsCommand(
    fileName: String?,
    configPath: String?,
    content: String?,
    extraOptions: String?,
    ignoreFilePath: String?,
    fileKind: String?,
    flatConfig: Boolean
  ) : BaseCommand(fileName, configPath, content, extraOptions, ignoreFilePath, fileKind, flatConfig) {
    override val command: String
      get() = "GetErrors"
  }

  private class FixErrorsCommand(
    fileName: String?,
    configPath: String?,
    content: String?,
    extraOptions: String?,
    ignoreFilePath: String?,
    fileKind: String?,
    flatConfig: Boolean
  ) : BaseCommand(fileName, configPath, content, extraOptions, ignoreFilePath, fileKind, flatConfig) {
    override val command: String
      get() = "FixErrors"
  }

  inner class Protocol(serviceName: String, project: Project)
    : JSLanguageServiceNodeStdProtocolBase(serviceName, project) {

    override val workingDirectory: String?
      get() = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(this@ESLintBasedLanguageService.myWorkingDirectory)

    override fun addNodeProcessAdditionalArguments(targetRun: NodeTargetRun) {
      super.addNodeProcessAdditionalArguments(targetRun)
      JSLanguageServiceUtil.addNodeProcessArgumentsFromRegistry(targetRun.commandLineBuilder, serviceName) {
        stringValue("eslint.service.node.arguments")
      }
      JSLanguageServiceUtil.addNodePathFromRegistry(targetRun.commandLineBuilder) {
        stringValue("eslint.service.node.path")
      }
      val initialState = createState()
      targetRun.path(PathUtil.getParentPath(initialState.pluginPath.path))
      val uploadRootPath: String?
      if (myNodePackage is YarnPnpNodePackage) {
        uploadRootPath = findUploadRoot(initialState.packageJsonPath!!.path)
      }
      else {
        uploadRootPath = findUploadRoot(myNodePackage.systemIndependentPath)
      }
      if (uploadRootPath != null) {
        targetRun.path(uploadRootPath)
      }
      val additionalRootDirectory = getNioPath(initialState.additionalRootDirectory)
      if (additionalRootDirectory != null && additionalRootDirectory.isAbsolute && Files.isDirectory(additionalRootDirectory)) {
        targetRun.path(additionalRootDirectory.toString())
      }
    }

    private val eslintPluginDir: Path
      get() = JSPluginPathManager.getPluginResource(
        this.javaClass,
        "languageService/eslint",
        if (AppMode.isRunningFromDevBuild()) "javascript/eslint" else "javascript/eslint/resources"
      )

    override fun createState(): ESLintLanguageServiceInitialState {
      val protocolState = ESLintLanguageServiceInitialState()
      fillInitialProtocolState(protocolState, this@ESLintBasedLanguageService.extendedState.getState())
      protocolState.pluginName = "ESLint"
      val service = eslintPluginDir.resolve("bin/eslint-plugin-provider.js")
      if (!Files.isRegularFile(service)) JSLanguageServiceQueue.Holder.LOGGER.info("ESLint plugin not found at '$service'")
      protocolState.pluginPath = LocalFilePath.create(service.absolutePathString())
      return protocolState
    }

    override fun getNodeCommandLineConfiguratorOptions(project: Project): NodeCommandLineConfigurator.Options {
      return NodeCommandLineConfigurator.defaultOptions(project).withRequiredNodePackage(myNodePackage)
    }
  }

  private fun findUploadRoot(path: String): String? {
    return ReadAction.compute<String, RuntimeException?>(ThrowableComputable {
      var file = LocalFileSystem.getInstance().findFileByPath(path)
      if (file == null) {
        JSLanguageServiceQueue.Holder.LOGGER.info("Cannot find virtual file by $path")
        return@ThrowableComputable null
      }
      if (!file.isDirectory()) {
        file = file.getParent()
        if (file == null) {
          JSLanguageServiceQueue.Holder.LOGGER.error("file.getParent() is null for $path")
          return@ThrowableComputable null
        }
      }
      val root = ProjectFileIndex.getInstance(myProject).getContentRootForFile(file, false)
      if (root == null) {
        JSLanguageServiceQueue.Holder.LOGGER.info("Cannot find content root for $file")
        return@ThrowableComputable file.getPath()
      }
      root.getPath()
    })
  }

  companion object {

    private fun processFixFileResponse(answer: JSLanguageServiceAnswer): EslintLanguageServiceClient.Response<String?> {
      val responseObject = answer.element
      val languageServiceError: @NlsSafe String? = JsonUtil.getChildAsString(responseObject, "error")
      if (languageServiceError != null) {
        return EslintLanguageServiceClient.Response.error<String?>(languageServiceError, false)
      }
      val body = JsonUtil.getChildAsObject(responseObject, "body")
      val response = JsonUtil.getChildAsArray(body, "results")
      //file was ignored or no compatible parser to process this file
      if (response == null || response.isEmpty) {
        return EslintLanguageServiceClient.Response.ok<String?>(null)
      }

      val element = if (response.get(0).isJsonObject) response.get(0).getAsJsonObject() else null
      if (element == null) {
        return EslintLanguageServiceClient.Response.error<String?>(
          EslintBundle.message("eslint.inspections.error.unexpected.language.service.response",
                                   responseObject.toString()), false)
      }
      return EslintLanguageServiceClient.Response.ok<String?>(JsonUtil.getChildAsString(element, "output"))
    }
  }
}
