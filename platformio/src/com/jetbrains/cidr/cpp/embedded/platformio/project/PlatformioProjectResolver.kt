package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.Gson
import com.intellij.execution.DefaultExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.process.*
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
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.*
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioProjectStatus.*
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioProjectResolvePolicy
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManager
import com.jetbrains.cidr.cpp.external.system.project.attachExternalModule
import com.jetbrains.cidr.external.system.model.ExternalModule
import com.jetbrains.cidr.external.system.model.impl.ExternalModuleImpl
import com.jetbrains.cidr.external.system.model.impl.ExternalResolveConfigurationBuilder
import org.jetbrains.annotations.Nls
import java.io.File
import java.util.*

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
    platformioService.projectStatus = PARSING
    try {
      val projectFile = LocalFileSystem.getInstance().findFileByPath(projectPath)!!
      val projectDir = if (projectFile.isDirectory) projectFile else projectFile.parent
      val boardInfo = project.getUserData(PROJECT_INIT_KEY)
      if (boardInfo != null) {
        cliGenerateProject(project, listener, id, projectDir, boardInfo)
      }
      checkCancelled()
      if (resolverPolicy.asSafely<PlatformioProjectResolvePolicy>()?.cleanCache != false) {
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
        val buildSrcFilter = scanner.gatherBuildSrcFilter(configMap, activeEnvName)

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
        platformioService.targets = (pioActiveMetadata["targets"]?.asSafely<List<Map<String, String?>>>())?.map {
          val name = it["name"] ?: throw ExternalSystemException("Malformed metadata targets section")
          PlatformioTargetData(name, it["title"], it["description"], it["group"])
        } ?: emptyList()

        val srcFolderPath = projectDir.toNioPath().resolve(platformioSection["src_dir"].asSafely<String>() ?: "src")
        val srcFolder: VirtualFile = VfsUtil.findFile(srcFolderPath, false) ?: throw ExternalSystemException(
          ClionEmbeddedPlatformioBundle.message("source.folder.not.found", srcFolderPath))

        val buildDirectory: File = calcBuildDir(projectDir, platformioSection)
        platformioService.buildDirectory = buildDirectory.toPath()
        val name = pioActiveMetadata["env_name"] as String
        val confBuilder = ExternalResolveConfigurationBuilder(id = name, configName = "PlatformIO", buildWorkingDir = buildDirectory)
          .withVariants(name)

        platformioService.librariesPaths = scanner.parseResolveConfig(confBuilder, pioActiveMetadata, srcFolder, buildSrcFilter)
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
    catch (e: ProcessNotCreatedException) {
      platformioService.projectStatus = PARSED
      platformioService.projectStatus = UTILITY_FAILED
      LOG.error(e)
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

  private fun runPio(id: ExternalSystemTaskId,
                     pioRunEventId: String,
                     project: Project,
                     listener: ExternalSystemTaskNotificationListener,
                     @Nls taskDescription: String,
                     parameters: List<String>,
                     logStdout: Boolean = false): ProcessOutput {
    checkCancelled()

    val commandLine = PlatfromioCliBuilder(project).withParams(parameters).withVerboseAllowed(false)
    val processHandler = CapturingAnsiEscapesAwareProcessHandler(commandLine.build())
    processHandlerToKill = processHandler

    val configTaskDescriptor = TaskOperationDescriptorImpl(taskDescription, System.currentTimeMillis(), "pio-project-config")
    val configStartEvent = ExternalSystemStartEventImpl(pioRunEventId, null, configTaskDescriptor)
    listener.onStatusChange(ExternalSystemTaskExecutionEvent(id, configStartEvent))

    processHandler.addProcessListener(object : ProcessListener {

      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (logStdout || outputType != ProcessOutputType.STDOUT) {
          listener.onTaskOutput(id, event.text, outputType == ProcessOutputType.STDOUT)
        }
      }
    })
    var operationResult: OperationResult? = null
    try {
      val pioOutput = processHandler.runProcess()
      if (pioOutput.exitCode != 0) {
        operationResult = FailureResultImpl(configStartEvent.eventTime, System.currentTimeMillis(), emptyList())
        throw ExternalSystemException(ClionEmbeddedPlatformioBundle.message("platformio.utility.exit.code", pioOutput.exitCode))
      }
      return pioOutput
    }
    catch (e: Throwable) {
      operationResult = FailureResultImpl(configStartEvent.eventTime, System.currentTimeMillis(),
                                          listOf(FailureImpl(e.localizedMessage, null, emptyList())))
      throw ExternalSystemException(e)
    }
    finally {
      processHandlerToKill = null
      if (operationResult == null) {
        operationResult = SuccessResultImpl(configStartEvent.eventTime, System.currentTimeMillis(), true)
      }
      listener.onStatusChange(
        ExternalSystemTaskExecutionEvent(id, ExternalSystemFinishEventImpl(pioRunEventId, null,
                                                                           configStartEvent.descriptor, operationResult)))
    }
  }

}
