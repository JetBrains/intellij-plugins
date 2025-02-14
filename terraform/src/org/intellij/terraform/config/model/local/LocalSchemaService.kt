// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.readAndWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.RuntimeExceptionWithAttachments
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.getProjectDataPath
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.childScope
import com.intellij.platform.util.coroutines.compute.BatchAsyncProcessor
import com.intellij.platform.util.coroutines.compute.completeByMapping
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.SuspendingLazy
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.suspendingLazy
import kotlinx.coroutines.*
import org.intellij.terraform.LatestInvocationRunner
import org.intellij.terraform.config.Constants.PROVIDER_VERSION
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.ProviderTier
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.getVFSParents
import org.intellij.terraform.config.model.loader.TfMetadataLoader
import org.intellij.terraform.config.util.TfExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLFileType
import org.intellij.terraform.opentofu.OpenTofuFileType
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext
import kotlin.io.path.readText


const val TERRAFORM_LOCK_FILE_NAME: String = ".terraform.lock.hcl"

private const val DAEMON_RESTART_DEBOUNCE_TIMEOUT: Long = 300

private const val ORPHAN_COLLECTOR_DEBOUNCE_TIMEOUT: Long = 3000

@Service(Service.Level.PROJECT)
class LocalSchemaService(val project: Project, val scope: CoroutineScope) {

  private val modelBuildScope = scope.childScope()

  private val modelComputationCache = VirtualFileMap<Deferred<TypeModel>>(project)

  @OptIn(ExperimentalCoroutinesApi::class)
  fun getModel(virtualFile: VirtualFile): TypeModel? {
    val lock = findLockFile(virtualFile) ?: return null
    val myDeferred = modelComputationCache[lock]

    if (myDeferred == null || myDeferred.isCompleted && myDeferred.getCompletionExceptionOrNull() is CancellationException) {
      if (buildLocalMetadataAutomatically) {
        scheduleModelRebuild(setOf(lock)).let { scope.launch { it.getValue() } }
      }
      return null
    }

    if (!myDeferred.isCompleted) {
      scope.launch {
        myDeferred.join() // not myDeferred.start() because it logs exceptions
      }
      return null
    }

    return try {
      myDeferred.getCompleted()
    }
    catch (e: Exception) {
      fileLogger().warn("Failed to load local model for $lock", e)
      null
    }
  }

  suspend fun clearLocalModel(virtualFile: VirtualFile) {
    val lock = findLockFile(virtualFile) ?: return
    modelComputationCache.remove(lock)?.cancel()

    readAndWriteAction {
      val relatedEntities = WorkspaceModel.getInstance(project).currentSnapshot.entities<TFLocalMetaEntity>().filter {
        it.lockFile.virtualFile == lock
      }.toList()
      if (relatedEntities.isEmpty()) return@readAndWriteAction value(Unit)

      writeAction {
        WorkspaceModel.getInstance(project).updateProjectModel("remove related entities") { mutableEntityStorage ->
          for (relatedEntity in relatedEntities) {
            mutableEntityStorage.removeEntity(relatedEntity)
          }
        }
      }
    }
  }

  fun findLockFile(file: VirtualFile): VirtualFile? {
    if (isTFLock(file)) return file
    return getVFSParents(file, project).filter { it.isDirectory }.firstNotNullOfOrNull {
      it.findChild(TERRAFORM_LOCK_FILE_NAME)
    }
  }

  @RequiresReadLock
  fun getLockFilePsi(file: VirtualFile?): PsiFile? {
    if (!isTFLock(file) || file?.isValid != true) return null
    return PsiManager.getInstance(project).findFile(file).takeIf { it?.isValid == true }
  }

  private val daemonRestarter = LatestInvocationRunner {
    delay(DAEMON_RESTART_DEBOUNCE_TIMEOUT)
    awaitModelsReady()
    readAction {
      val openTerraformFiles = getOpenTerraformFiles()
      logger<LocalSchemaService>().info("openTerraformFiles to restart: $openTerraformFiles")
      for (openTerraformFile in openTerraformFiles) {
        DaemonCodeAnalyzer.getInstance(openTerraformFile.project).restart(openTerraformFile)
      }
    }
  }

  private fun getOpenTerraformFiles(): Set<PsiFile> {
    val fileTypeManager = FileTypeManager.getInstance()
    val fileTypes = setOf(TerraformFileType, OpenTofuFileType, HCLFileType)
    return ProjectManager.getInstance().openProjects.asSequence().flatMap { project ->
      FileEditorManager.getInstance(project).openFiles.asSequence()
        .filter { virtualFile -> fileTypes.any { fileTypeManager.isFileOfType(virtualFile, it) } }
        .mapNotNull { openFile -> PsiManager.getInstance(project).findFile(openFile) }
    }.toSet()
  }

  fun scheduleModelRebuild(virtualFiles: Set<VirtualFile>, explicitlyAllowRunningProcess: Boolean = false): SuspendingLazy<List<TypeModel>> {
    val scheduled = mutableListOf<Deferred<TypeModel>>()
    val locks = virtualFiles.mapNotNullTo(mutableSetOf()) { findLockFile(it) }
    for (lock in locks) {
      modelComputationCache[lock]?.cancel()
      if (lock.exists()) {
        buildModel(lock, explicitlyAllowRunningProcess).also {
          modelComputationCache[lock] = it
          scheduled.add(it)
        }
      }
      else {
        modelComputationCache.remove(lock)
      }
    }
    if (locks.isNotEmpty()) {
      scope.launch { daemonRestarter.cancelPreviousAndRun() }
    }
    return scope.suspendingLazy {
      scheduled.awaitAll()
    }
  }

  suspend fun awaitModelsReady() {
    modelBuildScope.coroutineContext.job.children.filter { it.isActive }.forEach { it.join() }
  }

  private val batchModelBuilder = BatchAsyncProcessor<Pair<VirtualFile, Boolean>, TypeModel>(scope) { batch ->
    withBackgroundProgress(project, HCLBundle.message("rebuilding.local.schema"), true) {
      val parallelism = RegistryManager.getInstance().intValue("terraform.registry.metadata.parallelism", 4)
      batch.completeByMapping(parallelism) { (lock, explicitlyAllowRunningProcess) ->
        logger<LocalSchemaService>().info("building local model: $lock")
        buildModelFromJson(retrieveJsonForTFLock(lock, explicitlyAllowRunningProcess))
      }
    }
  }

  private fun buildProviderMeta(providers: Collection<ProviderInfo>): String? {
    val mapper = ObjectMapper()
    val metadataNode = mapper.createObjectNode()
    providers.forEach { providerInfo ->
      val info = mapper.createObjectNode()
      info.put("type", "providers")
      val attributes = mapper.createObjectNode()
      attributes.put("name", providerInfo.name)
      attributes.put("namespace", providerInfo.namespace)
      attributes.put("full-name", providerInfo.fullName)
      attributes.put("tier", ProviderTier.TIER_LOCAL.label)
      attributes.put(PROVIDER_VERSION, providerInfo.version)
      info.set<ObjectNode>("attributes", attributes)
      metadataNode.set<ObjectNode>(providerInfo.fullName.lowercase(), info)
    }
    return mapper.writeValueAsString(metadataNode)
  }

  private fun buildModel(lock: VirtualFile, explicitlyAllowRunningProcess: Boolean): Deferred<TypeModel> {
    val startMode = if (buildLocalMetadataEagerly || explicitlyAllowRunningProcess) CoroutineStart.DEFAULT else CoroutineStart.LAZY
    return modelBuildScope.async(start = startMode) {
      batchModelBuilder.submit(coroutineContext, lock to explicitlyAllowRunningProcess).await()
    }
  }

  private suspend fun retrieveJsonForTFLock(lock: VirtualFile, explicitlyAllowRunningProcess: Boolean): String {
    val lockData = readAction {
      WorkspaceModel.getInstance(project).currentSnapshot.entities<TFLocalMetaEntity>().firstOrNull {
        it.lockFile.virtualFile == lock
      }.also {
        logger<LocalSchemaService>().info("building local model lockData: ${it?.lockFile?.virtualFile?.name} among ${
          WorkspaceModel.getInstance(project).currentSnapshot.entities<TFLocalMetaEntity>().count()
        }")
      }
    }

    if (lockData != null && lockData.timeStamp >= lock.timeStamp) {
      try {
        return readLockDataJsonFile(lockData.jsonPath)
      }
      catch (e: Exception) {
        if (e is CancellationException) throw e
        logger<LocalSchemaService>().warn("Cannot load model json: $lock", e)
      }
    }

    val generateResult = runCatching { generateNewJsonFile(lock, explicitlyAllowRunningProcess) }
    if (generateResult.isFailure) {
      logger<LocalSchemaService>().info(
        "failed to generate new model for lock: ${lock.name}",
        generateResult.exceptionOrNull()
      )
    }

    val jsonFilePath: String = generateResult.getOrNull() ?: lockData?.let { ld ->
      try {
        readLockDataJsonFile(ld.jsonPath)
        logger<LocalSchemaService>().info("using previous logData for: ${lock.name}")
        ld.jsonPath
      }
      catch (lockDataException: Exception) {
        logger<LocalSchemaService>().info("failed to load previous lock data: ${lock.name}", lockDataException)
        val generateException = generateResult.exceptionOrNull()
        if (generateException != null) {
          generateException.addSuppressed(lockDataException)
          throw generateException
        }
        else {
          throw lockDataException
        }
      }
    } ?: generateResult.getOrThrow()

    updateWorkspaceModel(lock, lockData, jsonFilePath)

    return readLockDataJsonFile(jsonFilePath)
  }

  private suspend fun readLockDataJsonFile(path: String): String {
    return withContext(Dispatchers.IO) {
      localModelPath.resolve(path).readText()
    }
  }

  private fun addLockFileDataString(lockFileData: String?, localModelJson: String): String {
    return """
    { "metadata": ${lockFileData ?: "{}"}, "schemas": $localModelJson }
    """.trimIndent()
  }

  val localModelPath: Path
    get() {
      val localModelsPath = project.getProjectDataPath("terraform-local-models")
      Files.createDirectories(localModelsPath)
      return localModelsPath
    }

  private suspend fun generateNewJsonFile(lock: VirtualFile, explicitlyAllowRunningProcess: Boolean): @NlsSafe String {
    if (!explicitlyAllowRunningProcess && !buildLocalMetadataAutomatically) throw IllegalStateException("generateNewJsonFile is not enabled")
    val jsonFromProcess = buildJsonFromTerraformProcess(project, lock)
    val lockFileProviders = readAction { getLockFilePsi(lock)?.let { collectProviders(it).values } }
    val lockFileDataString = lockFileProviders?.let { buildProviderMeta(lockFileProviders) }
    val modelJson = addLockFileDataString(lockFileDataString, jsonFromProcess)
    return withContext(Dispatchers.IO) {
      val uuid = UUID.randomUUID().toString()
      val jsonFile = localModelPath.resolve("$uuid.json")
      Files.writeString(jsonFile, modelJson)
      scope.launch { orphanCollector.cancelPreviousAndRun() }
      localModelPath.relativize(jsonFile).toString()
    }
  }


  private val orphanCollector = LatestInvocationRunner {
    delay(ORPHAN_COLLECTOR_DEBOUNCE_TIMEOUT)
    awaitModelsReady()
    withBackgroundProgress(project, HCLBundle.message("progress.title.removing.unused.metadata")) {
      val localModelPath = localModelPath
      val allModelFiles = withContext(Dispatchers.IO) {
        Files.list(localModelPath).use { paths -> paths.map { localModelPath.relativize(it) }.toList() }
      }

      val usedMeta = readAction {
        WorkspaceModel.getInstance(project).currentSnapshot.entities<TFLocalMetaEntity>().mapTo(mutableSetOf()) { it.jsonPath }
      }

      logger<LocalSchemaService>().info("OrphanMetadataCollection: $localModelPath allModelFiles = $allModelFiles, usedMeta = $usedMeta")

      withContext(Dispatchers.IO) {
        for (file in allModelFiles) {
          if (file.toString() !in usedMeta) {
            Files.deleteIfExists(localModelPath.resolve(file))
          }
        }
      }
    }
  }

  private suspend fun updateWorkspaceModel(lock: VirtualFile, prevLockData: TFLocalMetaEntity?, newJson: @NlsSafe String) {
    val low = (lock.timeStamp and 0xFFFFFFFFL).toInt()
    val high = (lock.timeStamp shr 32).toInt()
    val workspaceModel = WorkspaceModel.getInstance(project)
    workspaceModel.update("Update TF Local Model from $lock") { storage ->
      if (prevLockData != null) storage.removeEntity(prevLockData)
      storage.addEntity(TFLocalMetaEntity(low, high, newJson,
                                          lock.toVirtualFileUrl(workspaceModel.getVirtualFileUrlManager()),
                                          TFLocalMetaEntity.LockEntitySource

      ))
    }
  }

  private fun buildModelFromJson(json: String): TypeModel {
    val loader = TfMetadataLoader()
    json.byteInputStream().use { input ->
      loader.loadOne("local-schema.json", input)
    }
    loader.loadFrom(TypeModelProvider.globalModel)
    return loader.buildModel()
  }

  private suspend fun buildJsonFromTerraformProcess(project: Project, lock: VirtualFile): @NlsSafe String {
    logger<LocalSchemaService>().info("building local model buildJsonFromTerraformProcess: $lock")
    val capturingProcessAdapter = CapturingProcessAdapter()

    val toolType = getApplicableToolType(lock)
    val success = TfExecutor.`in`(project, toolType)
      .withPresentableName(HCLBundle.message("rebuilding.local.schema"))
      .withParameters("providers", "schema", "-json")
      .withWorkDirectory(lock.parent.path)
      .withPassParentEnvironment(true)
      //.showOutputOnError()
      .withProcessListener(capturingProcessAdapter)
      .executeSuspendable()

    logger<LocalSchemaService>().info(
      "building local model buildJsonFromTerraformProcess result: ${coroutineContext.isActive}, $success  $lock")
    coroutineContext.ensureActive()

    val stdout = capturingProcessAdapter.output.stdout
    if (!success || stdout.isEmpty()) {
      val truncatedOutput = StringUtil.shortenTextWithEllipsis(stdout, 1024, 256)
      val stderr = capturingProcessAdapter.output.stderr
      logger<LocalSchemaService>().warn("failed to build model for $lock: \n$truncatedOutput\n$stderr")

      throw RuntimeExceptionWithAttachments(
        HCLBundle.message("dialog.message.failed.to.get.output.terraform.providers.command.for",
                          lock,
                          capturingProcessAdapter.output.exitCode, toolType.executableName),
        Attachment("truncatedOutput.txt", truncatedOutput),
        Attachment("stderror.txt", stderr)
      )
    }

    return stdout
  }

}

internal val buildLocalMetadataAutomatically: Boolean
  get() = AdvancedSettings.getBoolean("org.intellij.terraform.config.build.metadata.auto")

internal val buildLocalMetadataEagerly: Boolean
  get() = AdvancedSettings.getBoolean("org.intellij.terraform.config.build.metadata.eagerly")

private class VirtualFileMap<T>(project: Project) {

  private val innerCache = ConcurrentHashMap<VirtualFileUrl, T>()

  private val vfm = WorkspaceModel.getInstance(project).getVirtualFileUrlManager()

  operator fun set(key: VirtualFile, value: T) {
    innerCache[key.toVirtualFileUrl(vfm)] = value
  }

  operator fun get(key: VirtualFile): T? = innerCache[key.toVirtualFileUrl(vfm)]

  fun remove(key: VirtualFile): T? = innerCache.remove(key.toVirtualFileUrl(vfm))

}

internal fun isTFLock(virtualFile: VirtualFile?): Boolean = virtualFile?.name == TERRAFORM_LOCK_FILE_NAME

