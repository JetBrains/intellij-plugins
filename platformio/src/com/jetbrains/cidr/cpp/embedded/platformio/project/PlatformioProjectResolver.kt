package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.execution.DefaultExecutionTarget
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.process.*
import com.intellij.ide.impl.isTrusted
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.event.*
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.asSafely
import com.intellij.util.ui.EDT
import com.jetbrains.cidr.cpp.embedded.platformio.*
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioProjectStatus.*
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioProjectResolvePolicy
import com.jetbrains.cidr.cpp.embedded.platformio.ui.showUntrustedProjectLoadDialog
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManager
import com.jetbrains.cidr.cpp.external.system.project.attachExternalModule
import com.jetbrains.cidr.external.system.model.ExternalLanguageConfiguration
import com.jetbrains.cidr.external.system.model.ExternalModule
import com.jetbrains.cidr.external.system.model.impl.ExternalLanguageConfigurationImpl
import com.jetbrains.cidr.external.system.model.impl.ExternalModuleImpl
import com.jetbrains.cidr.external.system.model.impl.ExternalResolveConfigurationBuilder
import com.jetbrains.cidr.lang.CLanguageKind
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompilerKind
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind
import com.jetbrains.cidr.lang.workspace.compiler.UnknownCompilerKind
import org.jetbrains.annotations.Nls
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.io.FileNotFoundException
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

open class PlatformioProjectResolver : ExternalSystemProjectResolver<PlatformioExecutionSettings> {

  //todo (low priority) leave *.ini files in the project node even if platformio.ini is broken

  @Volatile
  private var cancelled = false

  @Volatile
  private var processHandlerToKill: CapturingAnsiEscapesAwareProcessHandler? = null
  override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
    cancelled = true
    processHandlerToKill?.destroyProcess()
    return true
  }

  private fun checkCancelled() {
    if (cancelled) throw ProcessCanceledException()
  }

  @Throws(ExternalSystemException::class,
          IllegalArgumentException::class,
          IllegalStateException::class)
  @Synchronized
  override fun resolveProjectInfo(id: ExternalSystemTaskId,
                                  projectPath: String,
                                  isPreviewMode: Boolean,
                                  settings: PlatformioExecutionSettings?,
                                  resolverPolicy: ProjectResolverPolicy?,
                                  listener: ExternalSystemTaskNotificationListener): DataNode<ProjectData>? {
    cancelled = false
    val project = id.findProject()!!
    val platformioService = project.service<PlatformioService>()

    if (!project.isTrusted()) {
      // To prevent a deadlock
      assert(!EDT.isCurrentThreadEdt())
      runBlockingCancellable {
        if (!showUntrustedProjectLoadDialog(project)) {
          platformioService.projectStatus = NOT_TRUSTED
          throw ExternalSystemException(ClionEmbeddedPlatformioBundle.message("project.not.trusted"))
        }
      }
    }

    platformioService.projectStatus = PARSING
    try {
      val projectFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(projectPath) ?: throw ExternalSystemException(FileNotFoundException(ClionEmbeddedPlatformioBundle.message("project.not.found", projectPath)))
      val projectDir = if (projectFile.isDirectory) projectFile else projectFile.parent
      val boardInfo = project.getUserData(PROJECT_INIT_KEY)
      if (boardInfo != null) {
        cliGenerateProject(project, listener, id, projectDir, boardInfo)
      }
      checkCancelled()
      val pioResolvePolicy = resolverPolicy.asSafely<PlatformioProjectResolvePolicy>()
      if (pioResolvePolicy?.cleanCache != false) {
        platformioService.cleanCache()
      }
      val projectName = project.name
      val ideProjectPath = ExternalSystemApiUtil.toCanonicalPath(project.basePath ?: projectDir.path)
      val projectData = ProjectData(ID, projectName, ideProjectPath,
                                    ExternalSystemApiUtil.toCanonicalPath(projectDir.path))
      checkCancelled()
      var configJson = platformioService.configJson
      if (configJson == null) {
        configJson = gatherConfigJson(id, "pio-run:${UUID.randomUUID()}", project, listener)
        platformioService.configJson = configJson
      }

      val envs: Map<String, PlatformioExecutionTarget>
      val defaultEnv: PlatformioExecutionTarget?
      checkCancelled()
      val configMap: Map<String, List<Any>> =
        @Suppress("UNCHECKED_CAST")
        (Gson().fromJson(configJson, List::class.java) as List<List<Any>>)
          .associate { it[0] as String to it[1] as List<Any> }
      val platformioSection: Map<String, Any> =
        @Suppress("UNCHECKED_CAST")
        (configMap["platformio"] as List<List<Any>>?)?.associate { it[0] as String to it[1] } ?: emptyMap()

      envs = configMap.keys
        .filter { it.startsWith("env:") }
        .associate {
          val envName = it.removePrefix("env:")
          envName to PlatformioExecutionTarget(envName)
        }
      when (val defaultEnvs = platformioSection["default_envs"]) {
        is List<*> -> defaultEnv = envs[defaultEnvs[0].asSafely<String>() ?: ""]
        is String -> defaultEnv = envs[defaultEnvs]
        else -> defaultEnv = envs.values.firstOrNull()
      }
      val scanner = PlatformioFileScanner(projectDir, listener, id, this::checkCancelled)
      platformioService.iniFiles = scanner.findConfigs(platformioSection)

      var projectNode: DataNode<ProjectData>
      do {
        val activeExecutionTarget = ExecutionTargetManager.getActiveTarget(project)
        var activeEnv = activeExecutionTarget as? PlatformioExecutionTarget
        platformioService.envs = envs.values.toList()
        createRunConfigurationIfRequired(project)
        if (activeEnv == null || !envs.containsKey(activeEnv.id)) {
          activeEnv = defaultEnv
          ApplicationManager.getApplication().invokeAndWait {
            ExecutionTargetManager.setActiveTarget(project, activeEnv ?: DefaultExecutionTarget.INSTANCE)
          }
        }
        val activeEnvName = ExecutionTargetManager.getActiveTarget(project).asSafely<PlatformioExecutionTarget>()?.id ?: defaultEnv?.id
        if (activeEnvName == null) throw ExternalSystemException("No active platformio environment")

        var pioActiveMetadataText = platformioService.metadataJson[activeEnvName]
        if (pioActiveMetadataText == null) {
          pioActiveMetadataText = gatherEnvMetadata(id, "pio-run:${UUID.randomUUID()}", project, activeEnvName, listener)
          platformioService.setMetadataJson(activeEnvName, pioActiveMetadataText)
        }
        val pioActiveMetadata = Gson()
                                  .fromJson<Map<String, Any>>(pioActiveMetadataText, Map::class.java)
                                  ?.get(activeEnvName)
                                  ?.asSafely<Map<String, Any>>()
                                ?: throw ExternalSystemException("Project metadata does not contain expected '${activeEnv}' section")
        platformioService.targetExecutablePath = pioActiveMetadata["prog_path"] as? String
        platformioService.svdPath = pioActiveMetadata["svd_path"] as? String
        val targets = (pioActiveMetadata["targets"]?.asSafely<List<Map<String, String?>>>())?.map {
          val name = it["name"] ?: throw ExternalSystemException("Malformed metadata targets section")
          PlatformioTargetData(name, it["title"], it["description"], it["group"])
        }
        platformioService.setTargets(addUploadIfMissing(targets.orEmpty()))

        val buildDirectory: File = calcBuildDir(projectDir, platformioSection)
        platformioService.buildDirectory = buildDirectory.toPath()
        val name = pioActiveMetadata["env_name"] as String
        val confBuilder = ExternalResolveConfigurationBuilder(id = name, configName = "PlatformIO", buildWorkingDir = projectDir.toNioPath().toFile())
          .withVariants(name)

        checkCancelled()

        val languageConfigurations = configureLanguages(pioActiveMetadata, confBuilder)

        checkCancelled()

        val compDbJson = getCompDbJson(pioResolvePolicy, platformioService, id, project, activeEnvName, listener, projectPath)

        checkCancelled()
        scanner.scanSources(compDbJson, project.service<PlatformioWorkspace>(), languageConfigurations, confBuilder)
        checkCancelled()

        platformioService.librariesPaths = scanner.scanLibraries(pioActiveMetadata)

        checkCancelled()
        val module = ExternalModuleImpl(mutableSetOf(confBuilder.invoke()))
        val cppModuleNode = DataNode(ExternalModule.OC_MODULE_KEY, module, null)
        projectNode = DataNode(ProjectKeys.PROJECT, projectData, null)
        attachExternalModule(project, ID, projectDir.toNioPath(), projectNode, cppModuleNode)
        val needsToBeReparsed = with(ExecutionTargetManager.getActiveTarget(project)) {
          this is PlatformioExecutionTarget && this.id != activeEnvName
        }
      }
      while (needsToBeReparsed)
      platformioService.projectStatus = PARSED
      project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).projectStateChanged()
      return projectNode
    }
    catch (e: ProcessCanceledException) {
      platformioService.projectStatus = PARSE_FAILED
      throw e
    }
    catch (e: ExecutionException) {
      platformioService.projectStatus = PARSED
      platformioService.projectStatus = UTILITY_FAILED
      LOG.warn(e)
      throw ExternalSystemException(e)
    }
    catch (e: ExternalSystemException) {
      platformioService.projectStatus = PARSE_FAILED
      throw e
    }
    catch (e: Throwable) {
      platformioService.projectStatus = PARSE_FAILED
      LOG.error(e)
      throw ExternalSystemException(e)
    }
  }

  private fun isPathFromPackage(path: Path): Boolean {
    // Package paths are absolute and contain ".platformio/packages" somewhere in them
    if (!path.isAbsolute) return false
    val pioDirIndex = path.indexOf(Path.of(".platformio"))
    if (pioDirIndex < 0 || pioDirIndex >= path.count() - 1) return false
    return path.getName(pioDirIndex + 1) == Path.of("packages")
  }

  private fun getCompDbJson(
    pioResolvePolicy: PlatformioProjectResolvePolicy?,
    platformioService: PlatformioService,
    id: ExternalSystemTaskId,
    project: Project,
    activeEnvName: String,
    listener: ExternalSystemTaskNotificationListener,
    projectPath: String,
  ): List<PlatformioFileScanner.CompDbEntry> {

    val isInitial = pioResolvePolicy?.isInitial == true
    val compDbInitialText = if (isInitial) platformioService.compileDbDeflatedBase64?.inflate() else null
    checkCancelled()
    val compDbText: String = compDbInitialText ?: gatherCompDB(id, "pio-run:${UUID.randomUUID()}", project, activeEnvName, listener, projectPath)
    checkCancelled()

    val compDbTokenType = object : TypeToken<List<Map<String, String>>>() {}.type
    val compDbJson = Gson().fromJson<List<Map<String, String>>>(compDbText, compDbTokenType)?.mapNotNull {
      val command: String
      val file: String
      val directory: String
      try {
        command = it["command"]!!
        file = it["file"]!!
        directory = it["directory"]!!
      } catch(_: NullPointerException) {
        throw ExternalSystemException("Malformed Compilation Database entry! $it")
      }

      if (isPathFromPackage(Path.of(file))) {
        // Skip platformio package files
        // We don't want to include them in the ProjectView.
        // Do it here so we don't save data we won't use into platformioService.
        return@mapNotNull null
      }
      PlatformioFileScanner.CompDbEntry(file, command, directory.intern())
    } ?: emptyList()

    checkCancelled()

    if (!isInitial) {
      platformioService.compileDbDeflatedBase64 = Gson().toJson(compDbJson).deflate()
    }

    return compDbJson
  }

  private fun configureLanguages(
    jsonConfig: Map<String, Any>,
    confBuilder: ExternalResolveConfigurationBuilder
  ): List<ExternalLanguageConfiguration> {
    val compilerKind: OCCompilerKind = if (jsonConfig["compiler_type"] == "gcc") GCCCompilerKind else UnknownCompilerKind

    fun extractCompilerSwitches(key: String, includeSwitches: List<String>, defineSwitches: List<String>): MutableList<String> {
      val switches = when (val rawSwitches = jsonConfig[key]) {
        is String -> rawSwitches.split(' ')
        is List<*> -> rawSwitches.map { it.toString() }.toList()
        else -> emptyList()
      }
      return switches.toMutableList().apply { addAll(includeSwitches); addAll(defineSwitches) }
    }

    val includeSwitches: List<String> = jsonConfig["includes"]
                                          .asSafely<Map<String, List<String>>>()
                                          ?.flatMap { it.value }
                                          ?.toSet()
                                          ?.map { "-I$it" } ?: emptyList()
    val defineSwitches: List<String> = jsonConfig["defines"]
                                         .asSafely<List<String>>()
                                         ?.map { "-D$it" } ?: emptyList()
    val cLanguageConfiguration = ExternalLanguageConfigurationImpl(
      languageKind = CLanguageKind.C, compilerKind = compilerKind,
      compilerExecutable = jsonConfig["cc_path"].asSafely<String>()?.let(::File),
      compilerSwitches = extractCompilerSwitches("cc_flags", includeSwitches, defineSwitches)
    )
    confBuilder.withLanguageConfiguration(cLanguageConfiguration)
    val cxxLanguageConfiguration = ExternalLanguageConfigurationImpl(
      languageKind = CLanguageKind.CPP, compilerKind = compilerKind,
      compilerExecutable = jsonConfig["cxx_path"].asSafely<String>()?.let(::File),
      compilerSwitches = extractCompilerSwitches("cxx_flags", includeSwitches, defineSwitches)
    )
    confBuilder.withLanguageConfiguration(cxxLanguageConfiguration)
    return listOf(cLanguageConfiguration, cxxLanguageConfiguration)
  }

  private fun calcBuildDir(projectDir: VirtualFile, platformioSection: Map<String, Any>): File {
    val buildDirName = platformioSection["build_dir"].asSafely<String>()

    if (buildDirName != null) {
      return projectDir.toNioPath().resolve(buildDirName).toFile()
    }
    val workspaceDirName = platformioSection["workspace_dir"].asSafely<String>() ?: ".pio"
    return projectDir.toNioPath().resolve(workspaceDirName).resolve("build").toFile()
  }

  private fun cliGenerateProject(project: Project,
                                 listener: ExternalSystemTaskNotificationListener,
                                 id: ExternalSystemTaskId,
                                 baseDir: VirtualFile,
                                 boardInfo: BoardInfo) {

    PlatformioUsagesCollector.NEW_PROJECT.log(project)
    runPio(id, id.toString(), project, listener, ClionEmbeddedPlatformioBundle.message("initializing.the.project"),
           listOf("init") + boardInfo.parameters, true)
    baseDir.refresh(false, true)
    if (boardInfo.template !== SourceTemplate.NONE) {
      val srcFolder = baseDir.findChild("src")
      if (srcFolder != null && srcFolder.isDirectory) {
        if (srcFolder.findChild("main.cpp") == null && srcFolder.findChild("main.c") == null) {
          ApplicationManager.getApplication().invokeLater {
            WriteAction.run<Throwable> {
              if (!project.isDisposed) {
                val virtualFile = srcFolder.createChildData(this, boardInfo.template.fileName)
                virtualFile.setBinaryContent(boardInfo.template.content.toByteArray(Charsets.US_ASCII))
                val descriptor = OpenFileDescriptor(project, virtualFile)
                FileEditorManager.getInstance(project).openEditor(descriptor, true)
              }
            }
          }
        }
      }
    }
  }

  /** Interprets this string as a Base64 encoded ByteArray and decodes it to a String using an Inflater. */
  private fun String.inflate(): String {
    val byteArrayIS = ByteArrayInputStream(Base64.getDecoder().decode(this))
    val inflaterIS = InflaterInputStream(byteArrayIS)
    val bytes = inflaterIS.readBytes()
    inflaterIS.close()
    return String(bytes, Charsets.UTF_8)
  }

  /** Encodes this string using a Deflater and encodes the deflated ByteArray using Base64. */
  private fun String.deflate(): String {
    val byteArrayOS = ByteArrayOutputStream()
    val deflaterOS = DeflaterOutputStream(byteArrayOS)
    deflaterOS.write(this.toByteArray(Charsets.UTF_8))
    deflaterOS.close()
    return Base64.getEncoder().encodeToString(byteArrayOS.toByteArray())
  }

  protected open fun createRunConfigurationIfRequired(project: Project) {
    ApplicationManager.getApplication().invokeLater {
      WriteAction.run<Throwable> {
        if (!project.isDisposed) {
          CLionRunConfigurationManager.getInstance(project).updateRunConfigurations(PlatformioRunConfigurationManagerHelper)
        }
      }
    }
  }

  protected open fun gatherEnvMetadata(id: ExternalSystemTaskId,
                                       pioRunEventId: String,
                                       project: Project, activeEnvName: String,
                                       listener: ExternalSystemTaskNotificationListener): String {

    val metadataFile = FileUtil.createTempFile("pio", ".json")
    try {
      val parameters = mutableListOf("project", "metadata", "--json-output")
      if (!activeEnvName.isBlank()) parameters.apply { add("-e"); add(activeEnvName) }
      runPio(id, pioRunEventId, project, listener, ClionEmbeddedPlatformioBundle.message("configuring.environment", activeEnvName),
             parameters + listOf("--json-output-path", metadataFile.absolutePath))
      return metadataFile.readText()
    }
    finally {
      FileUtil.delete(metadataFile)
    }
  }

  protected open fun gatherConfigJson(id: ExternalSystemTaskId, pioRunEventId: String, project: Project,
                                      listener: ExternalSystemTaskNotificationListener): @NlsSafe String {
    val configOutput = runPio(id, pioRunEventId, project, listener, ClionEmbeddedPlatformioBundle.message("configuring.project"),
                              listOf("project", "config", "--json-output"))
    return configOutput.stdout
  }

  protected open fun gatherCompDB(id: ExternalSystemTaskId,
                                  pioRunEventId: String,
                                  project: Project, activeEnvName: String,
                                  listener: ExternalSystemTaskNotificationListener,
                                  projectPath: String): String {
    val compDbFile = Path.of(projectPath).resolve("compile_commands.json").toFile()
    val compDbPresent = compDbFile.isFile

    val parameters = mutableListOf("run", "-t", "compiledb")
    if (!activeEnvName.isBlank()) parameters.apply { add("-e"); add(activeEnvName) }

    try {
      runPio(id, pioRunEventId, project, listener, ClionEmbeddedPlatformioBundle.message("configuring.compdb"), parameters)
      return compDbFile.readText()
    }
    finally {
      if (!compDbPresent) {
        // Don't delete the file if it was present
        // we still update it though
        FileUtil.delete(compDbFile)
      }
    }
  }

  private fun runPio(id: ExternalSystemTaskId,
                     pioRunEventId: String,
                     project: Project,
                     listener: ExternalSystemTaskNotificationListener,
                     @Nls taskDescription: String,
                     parameters: List<String>,
                     logStdout: Boolean = false): ProcessOutput {
    checkCancelled()

    val commandLine = PlatformioCliBuilder(false, project).withParams(parameters).withVerboseAllowed(false)
    val processHandler = CapturingAnsiEscapesAwareProcessHandler(commandLine.build())
    processHandlerToKill = processHandler

    val configTaskDescriptor = TaskOperationDescriptor(taskDescription, System.currentTimeMillis(), "pio-project-config")
    val configStartEvent = ExternalSystemStartEvent(pioRunEventId, null, configTaskDescriptor)
    listener.onStatusChange(ExternalSystemTaskExecutionEvent(id, configStartEvent))

    processHandler.addProcessListener(object : ProcessListener {

      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (logStdout || outputType != ProcessOutputType.STDOUT) {
          listener.onTaskOutput(id, event.text, !ProcessOutputType.isStderr(outputType))
        }
      }
    })
    var operationResult: OperationResult? = null
    try {
      val pioOutput = processHandler.runProcess()
      if (pioOutput.exitCode != 0) {
        operationResult = FailureResult(configStartEvent.eventTime, System.currentTimeMillis(), emptyList())
        throw ExternalSystemException(ClionEmbeddedPlatformioBundle.message("platformio.utility.exit.code", pioOutput.exitCode))
      }
      return pioOutput
    }
    catch (e: Throwable) {
      operationResult = FailureResult(configStartEvent.eventTime, System.currentTimeMillis(),
                                                                                                              listOf(Failure(e.localizedMessage, null, emptyList())))
      throw ExternalSystemException(e)
    }
    finally {
      processHandlerToKill = null
      if (operationResult == null) {
        operationResult = SuccessResult(configStartEvent.eventTime, System.currentTimeMillis(), true)
      }
      listener.onStatusChange(
        ExternalSystemTaskExecutionEvent(id,
                                         ExternalSystemFinishEvent(
                                           pioRunEventId, null,
                                           configStartEvent.descriptor, operationResult)))
    }
  }

  private fun addUploadIfMissing(targets: List<PlatformioTargetData>): List<PlatformioTargetData> =
    if (targets.any { it.name == "upload" }) {
      targets
    }
    else {
      targets + PlatformioTargetData("upload", "Upload", null, "Platform")
    }
}
