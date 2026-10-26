package com.intellij.lang.javascript.linter.eslint.service

import com.google.gson.JsonObject
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.idea.AppMode
import com.intellij.javascript.nodejs.execution.NodeTargetRun
import com.intellij.javascript.nodejs.execution.NodeTargetRun.Companion.shouldEnableRemoteDevelopmentUsingTargetsApi
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.linter.ExtendedLinterState
import com.intellij.lang.javascript.linter.JSLinterConfiguration
import com.intellij.lang.javascript.linter.JSNpmLinterState
import com.intellij.lang.javascript.linter.eslint.ESLintJsonProblemsParser
import com.intellij.lang.javascript.linter.eslint.EslintBundle
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
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry.Companion.stringValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.AnsiCsiUtil
import com.intellij.util.PathUtil
import com.intellij.webcore.util.JsonUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.absolutePathString

abstract class ESLintBasedLanguageService<TStoredState : JSNpmLinterState<TStoredState>>
  (project: Project, private val myNodePackage: NodePackage, private val myWorkingDirectory: VirtualFile) : JSLanguageServiceBase(
  project), EslintLanguageServiceClient {

  /** The exits of the service process that the IDE did not request, in the life of this service instance. */
  private val unexpectedExits = AtomicInteger()

  override fun getNodePackage(): NodePackage {
    return myNodePackage
  }

  override fun getWorkingDirectory(): VirtualFile {
    return myWorkingDirectory
  }


  override fun highlight(
    requestData: EslintRequestData,
    extraOptions: String?
  ): CompletableFuture<EslintLanguageServiceClient.Response<List<EslintError>>?>? {
    val command = createGetErrorsCommand(requestData, extraOptions) ?: return null
    return cs.async { highlightSuspending(command) }.asCompletableFuture()
  }

  suspend fun highlightSuspending(
    requestData: EslintRequestData,
    extraOptions: String?
  ): EslintLanguageServiceClient.Response<List<EslintError>>? {
    val command = createGetErrorsCommand(requestData, extraOptions) ?: return null

    return highlightSuspending(command)
  }

  private suspend fun highlightSuspending(command: GetErrorsCommand): EslintLanguageServiceClient.Response<List<EslintError>>? {
    exitLimitError()?.let { return EslintLanguageServiceClient.Response.error(it, false) }
    val process = getProcess()
    if (process == null) {
      val error = JSLanguageServiceUtil.getLanguageServiceCreationError(this)
      return EslintLanguageServiceClient.Response.error(error, false)
    }

    val answer = process.execute(command)?.answer
                 ?: return processExitError(process)?.let { EslintLanguageServiceClient.Response.error(it, false) }
    val languageServiceError: @NlsSafe String? = JsonUtil.getChildAsString(answer.element, "error")
    if (languageServiceError != null) {
      val isNoConfigFile = JsonUtil.getChildAsBoolean(answer.element, "isNoConfigFile", false)
      return EslintLanguageServiceClient.Response.error(languageServiceError, isNoConfigFile)
    }
    val parser = ESLintJsonProblemsParser.parse(answer.element)
    return EslintLanguageServiceClient.Response<List<EslintError>>(parser.fileLevelError, parser.errors, false, readStderr(answer))
  }

  /** The stderr text of the response. The Node-side plugin caps the text, and the IDE adds the note about the cap. */
  private fun readStderr(answer: JSLanguageServiceAnswer): @NlsSafe String? {
    val stderr: @NlsSafe String = JsonUtil.getChildAsString(answer.element, "stderr") ?: return null
    if (!JsonUtil.getChildAsBoolean(answer.element, "stderrTruncated", false)) return stderr
    return stderr + "\n" + EslintBundle.message("eslint.service.stderr.truncated")
  }

  private fun createGetErrorsCommand(requestData: EslintRequestData, extraOptions: String?): GetErrorsCommand? {
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

    return GetErrorsCommand(path,
                            configPath,
                            requestData.fileToLintContent,
                            extraOptions,
                            eslintIgnoreFilePath,
                            requestData.fileKind.stringValue,
                            requestData.isFlatConfigMode)
  }

  override fun fixFile(
    requestData: EslintRequestData,
    extraOptions: String?
  ): CompletableFuture<EslintLanguageServiceClient.Response<String?>?>? {
    val command = createFixErrorsCommand(requestData, extraOptions) ?: return null
    return cs.async { fixFileSuspending(command) }.asCompletableFuture()
  }

  suspend fun fixFileSuspending(
    requestData: EslintRequestData,
    extraOptions: String?
  ): EslintLanguageServiceClient.Response<String?>? {
    val command = createFixErrorsCommand(requestData, extraOptions) ?: return null

    return fixFileSuspending(command)
  }

  private suspend fun fixFileSuspending(command: FixErrorsCommand): EslintLanguageServiceClient.Response<String?>? {
    exitLimitError()?.let { return EslintLanguageServiceClient.Response.error(it, false) }
    val process = getProcess()
    if (process == null) {
      return EslintLanguageServiceClient.Response.error<String?>(JSLanguageServiceUtil.getLanguageServiceCreationError(this), false)
    }

    val answer = process.execute(command)?.answer
                 ?: return processExitError(process)?.let { EslintLanguageServiceClient.Response.error(it, false) }
    return processFixFileResponse(answer)
  }

  /** Counts an exit of the service process that the IDE did not request. */
  protected fun registerUnexpectedExit() {
    unexpectedExits.incrementAndGet()
  }

  /**
   * After [MAX_UNEXPECTED_EXITS], the service stays stopped until the user restarts it. Without the limit, each exit
   * restarts the highlighting, which starts the process again, and a runtime that crashes on a file loops forever.
   */
  private fun exitLimitError(): @InspectionMessage String? {
    val exits = unexpectedExits.get()
    if (exits < MAX_UNEXPECTED_EXITS) return null
    return EslintBundle.message("eslint.service.process.exit.limit", exits)
  }

  /**
   * The error for a request without an answer, because the process exited while it handled the request.
   * A start failure is not such an exit: [com.intellij.lang.javascript.linter.eslint.EsLintExternalRunner] reports it
   * from [getServiceCreationError].
   */
  private fun processExitError(process: JSLanguageServiceQueue): @InspectionMessage String? {
    if (process.startErrorMessage != null) return null
    val protocol = (process as? JSLanguageServiceQueueImpl)?.protocol as? ESLintBasedLanguageService<*>.Protocol ?: return null
    val exit = protocol.unexpectedExit ?: return null
    return EslintBundle.message("eslint.service.process.exited", exit.exitCode, exit.stderrTail)
  }

  private fun createFixErrorsCommand(requestData: EslintRequestData, extraOptions: String?): FixErrorsCommand? {
    val virtualFile = requestData.fileToLint
    val configPath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(requestData.specifiedConfigFile)
    val path = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(virtualFile)
    val ignoreFilePath = JSLanguageServiceUtil.normalizePathDoNotFollowSymlinks(requestData.eslintIgnoreFile)
    if (path == null) {
      return null
    }

    return FixErrorsCommand(path,
                            configPath,
                            requestData.fileToLintContent,
                            extraOptions,
                            ignoreFilePath,
                            requestData.fileKind.stringValue,
                            requestData.isFlatConfigMode)
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

    /** The last lines that the process wrote to stderr. A crash report of the runtime, such as a Bun panic, ends here. */
    private val stderrTail = ArrayDeque<String>()

    @Volatile
    private var ready = false

    @Volatile
    private var disposed = false

    /** Set when the process exits after it got ready and before the IDE disposes the protocol. */
    @Volatile
    internal var unexpectedExit: ServiceProcessExit? = null
      private set

    override fun connect(): ProcessHandler {
      val processHandler = super.connect()
      processHandler.addProcessListener(object : ProcessListener {
        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
          if (ProcessOutputType.isStderr(outputType)) {
            rememberStderr(event.text)
          }
        }

        override fun processTerminated(event: ProcessEvent) {
          if (!ready || disposed) return
          unexpectedExit = ServiceProcessExit(event.exitCode, stderrTailText())
          registerUnexpectedExit()
          failPendingCommands()
        }
      })
      return processHandler
    }

    override suspend fun awaitReadyNotification(processHandler: ProcessHandler): JsonObject? {
      val readyNotification = super.awaitReadyNotification(processHandler)
      if (readyNotification != null) {
        ready = true
      }
      return readyNotification
    }

    override fun dispose() {
      disposed = true
      super.dispose()
    }

    /** Answers each pending command with null, so a request does not wait for its timeout or for the alive checker. */
    private fun failPendingCommands() {
      for (id in myCallbacks.keys.toList()) {
        myCallbacks.remove(id)?.consume(null)
      }
    }

    @Synchronized
    private fun rememberStderr(line: String) {
      // The IDE starts Node.js with FORCE_COLOR, so a crash report comes with color codes.
      stderrTail.addLast(AnsiCsiUtil.stripAnsi(line))
      if (stderrTail.size > STDERR_TAIL_LINES) {
        stderrTail.removeFirst()
      }
    }

    @Synchronized
    private fun stderrTailText(): @NlsSafe String = stderrTail.joinToString("\n").trim()

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
        if (AppMode.isRunningFromDevBuild()) "javascript/eslint" else "javascript/eslint/plugin-resources"
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
  }

  private fun findUploadRoot(path: String): String? {
    return ReadAction.compute<String, RuntimeException?>(ThrowableComputable {
      var file = StandardFileSystems.local().findFileByPath(path)
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
    /** After this many exits that the IDE did not request, the service stays stopped until the user restarts it. */
    const val MAX_UNEXPECTED_EXITS: Int = 5

    /** The number of stderr lines of the process that an exit report keeps. */
    private const val STDERR_TAIL_LINES = 20

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

/** An exit of the service process that the IDE did not request, with the last lines that the process wrote to stderr. */
internal class ServiceProcessExit(val exitCode: Int, val stderrTail: @NlsSafe String)
