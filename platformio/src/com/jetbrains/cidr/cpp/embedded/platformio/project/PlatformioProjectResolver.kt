package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.google.gson.Gson
import com.intellij.build.events.MessageEvent
import com.intellij.build.events.impl.MessageEventImpl
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
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.event.*
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PLATFORMIO_UPDATES_TOPIC
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioTargetData
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioProjectResolvePolicy
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManager
import com.jetbrains.cidr.cpp.external.system.project.attachExternalModule
import com.jetbrains.cidr.external.system.model.ExternalModule
import com.jetbrains.cidr.external.system.model.impl.ExternalModuleImpl
import com.jetbrains.cidr.external.system.model.impl.ExternalResolveConfigurationBuilder
import java.io.File
import java.util.*
import javax.swing.SwingUtilities

open class PlatformioProjectResolver : ExternalSystemProjectResolver<PlatformioExecutionSettings> {

  //todo (low priority) leave *.ini files in the project node even if platformio.ini is broken
  //todo (low priority) roots generator

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
    project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).reparseStarted()
    val platformioService = project.service<PlatformioService>()
    if (resolverPolicy.asSafely<PlatformioProjectResolvePolicy>()?.cleanCache != false) {
      platformioService.cleanCache()
    }
    val projectFile = LocalFileSystem.getInstance().findFileByPath(projectPath)!!
    val projectDir = if (projectFile.isDirectory) projectFile else projectFile.parent
    val projectName = project.name
    val ideProjectPath = ExternalSystemApiUtil.toCanonicalPath(project.basePath ?: projectDir.path)
    val projectData = ProjectData(ID, projectName, ideProjectPath,
                                  ExternalSystemApiUtil.toCanonicalPath(projectDir.path))
    var pioRunEventIdConfig: Any = id
    try {
      checkCancelled()
      var configJson = platformioService.configJson
      if (configJson == null) {
        pioRunEventIdConfig = "pio-run:${UUID.randomUUID()}"
        configJson = gatherConfigJson(id, pioRunEventIdConfig, project, listener)
        platformioService.configJson = configJson
      }
      listener.onStatusChange(ExternalSystemBuildEvent(id, MessageEventImpl(pioRunEventIdConfig, MessageEvent.Kind.SIMPLE, null,
                                                                            ClionEmbeddedPlatformioBundle.message(
                                                                              "build.event.message.parse.project.config"), null)))

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
        var pioRunEventIdEnv: Any = id
        if (pioActiveMetadataText == null) {
          pioRunEventIdEnv = "pio-run:${UUID.randomUUID()}"
          pioActiveMetadataText = gatherEnvMetadata(id, pioRunEventIdEnv, project, activeEnvName, listener)
          platformioService.setMetadataJson(activeEnvName, pioActiveMetadataText)
        }
        listener.onStatusChange(
          ExternalSystemBuildEvent(id, MessageEventImpl(pioRunEventIdEnv, MessageEvent.Kind.SIMPLE, null,
                                                        ClionEmbeddedPlatformioBundle.message("build.event.message.parse.environment",
                                                                                              activeEnv),
                                                        null)))
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

        val buildWorkingDir = File(VfsUtil.virtualToIoFile(projectDir), ".pio")
        val name = pioActiveMetadata["env_name"] as String
        val confBuilder = ExternalResolveConfigurationBuilder(id = name, configName = "PlatformIO", buildWorkingDir = buildWorkingDir)
          .withVariants(name)

        scanner.parseResolveConfig(confBuilder, pioActiveMetadata, srcFolder, buildSrcFilter)
        val module = ExternalModuleImpl(mutableSetOf(confBuilder.invoke()))
        val cppModuleNode = DataNode(ExternalModule.OC_MODULE_KEY, module, null)
        projectNode = DataNode(ProjectKeys.PROJECT, projectData, null)
        attachExternalModule(project, ID, projectDir.toNioPath(), projectNode, cppModuleNode)
        val needsToBeReparsed = with(ExecutionTargetManager.getActiveTarget(project)) {
          this is PlatformioExecutionTarget && this.id != activeEnvName
        }
      }
      while (needsToBeReparsed)
      project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).reparseSuccess()
      return projectNode
    }
    catch (e: ProcessNotCreatedException) {
      project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).reparseFailed(true)
      throw ExternalSystemException(e)
    }
    catch (e: ExternalSystemException) {
      project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).reparseFailed(false)
      throw e
    }
    catch (e: Throwable) {
      project.messageBus.syncPublisher(PLATFORMIO_UPDATES_TOPIC).reparseFailed(false)
      throw ExternalSystemException(e)
    }
  }

  protected open fun createRunConfigurationIfRequired(project: Project) {
    SwingUtilities.invokeLater {
      WriteAction.run<Throwable> {
        CLionRunConfigurationManager.getInstance(project).updateRunConfigurations(PlatformioRunConfigurationManagerHelper)
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
      runPio(id, pioRunEventId, project, listener, parameters,
             listOf("--json-output-path", metadataFile.absolutePath))
      return metadataFile.readText()
    }
    finally {
      FileUtil.delete(metadataFile)
    }
  }

  protected open fun gatherConfigJson(id: ExternalSystemTaskId, pioRunEventId: String, project: Project,
                                      listener: ExternalSystemTaskNotificationListener): @NlsSafe String {
    val configOutput = runPio(id, pioRunEventId, project, listener, listOf("project", "config", "--json-output"))
    return configOutput.stdout
  }

  private fun runPio(id: ExternalSystemTaskId,
                     pioRunEventId: String,
                     project: Project,
                     listener: ExternalSystemTaskNotificationListener, parameters: List<String>,
                     nonLoggableParameters: List<String> = emptyList()): ProcessOutput {
    checkCancelled()

    val commandLine = PlatfromioCliBuilder(project).withParams(parameters).withParams(nonLoggableParameters).withVerboseAllowed(false)
    val processHandler = CapturingAnsiEscapesAwareProcessHandler(commandLine.build())
    processHandlerToKill = processHandler

    val configTaskDescriptor = TaskOperationDescriptorImpl(
      ClionEmbeddedPlatformioBundle.message("build.event.message.run.pio",
                                            parameters.joinToString(
                                              separator = " ")),
      System.currentTimeMillis(),
      "pio-project-config")
    val configStartEvent = ExternalSystemStartEventImpl(pioRunEventId, null, configTaskDescriptor)
    listener.onStatusChange(ExternalSystemTaskExecutionEvent(id, configStartEvent))

    processHandler.addProcessListener(object : ProcessListener {

      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (outputType != ProcessOutputType.STDOUT) {
          listener.onTaskOutput(id, event.text, false)
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
