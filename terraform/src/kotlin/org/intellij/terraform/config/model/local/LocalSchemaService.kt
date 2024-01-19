// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.getProjectDataPath
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.backend.workspace.getVirtualFileUrlManager
import com.intellij.platform.backend.workspace.toVirtualFileUrl
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.childScope
import com.intellij.platform.util.progress.indeterminateStep
import com.intellij.platform.workspace.storage.entities
import com.intellij.psi.PsiManager
import com.intellij.util.concurrency.annotations.RequiresBlockingContext
import com.intellij.util.containers.ContainerUtil
import kotlinx.coroutines.*
import org.intellij.terraform.ExecuteLatest
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.loader.TerraformMetadataLoader
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.io.path.readText


@Service(Service.Level.PROJECT)
class LocalSchemaService(val project: Project, val scope: CoroutineScope) {

  private val modelBuildScope = scope.childScope()

  private val modelComputationCache = ContainerUtil.createConcurrentWeakMap<VirtualFile, Deferred<TypeModel?>>()

  @OptIn(ExperimentalCoroutinesApi::class)
  @RequiresBlockingContext
  fun getModel(lock: VirtualFile): TypeModel? {
    val myDeferred = modelComputationCache[lock]

    if (myDeferred == null || myDeferred.isCancelled) {
      scheduleModelRebuild(setOf(lock))
      return null
    }

    if (!myDeferred.isCompleted) {
      return null
    }

    return try {
      myDeferred.getCompleted()
    }
    catch (e: Exception) {
      null
    }
  }

  private val daemonRestarter: ExecuteLatest<Unit> = ExecuteLatest(scope) {
    delay(300) // debounce
    awaitModelsReady()
    readAction {
      val openTerraformFiles = ProjectManager.getInstance().openProjects.asSequence().flatMap { project ->
        FileEditorManager.getInstance(project).openFiles.asSequence().mapNotNull { openFile ->
          PsiManager.getInstance(project).findFile(openFile)?.takeIf { it.language.isKindOf(HCLLanguage) }
        }
      }.toSet()
      logger<LocalSchemaService>().info("openTerraformFiles to restart: $openTerraformFiles")
      for (openTerraformFile in openTerraformFiles) {
        DaemonCodeAnalyzer.getInstance(openTerraformFile.project).restart(openTerraformFile)
      }
    }
  }

  fun scheduleModelRebuild(locks: Set<VirtualFile>) {
    for (lock in locks) {
      modelComputationCache[lock]?.cancel()
      buildModel(lock).also {
        modelComputationCache[lock] = it
      }
    }
    if (locks.isNotEmpty()) {
      daemonRestarter.restart()
    }
  }

  suspend fun awaitModelsReady() {
    modelBuildScope.coroutineContext.job.children.forEach { it.join() }
  }

  private fun buildModel(lock: VirtualFile): Deferred<TypeModel?> {
    return modelBuildScope.async {
      try {
        withBackgroundProgress(project, HCLBundle.message("rebuilding.local.schema"), false) {
          logger<LocalSchemaService>().info("building local model: $lock")
          val json = indeterminateStep(HCLBundle.message("progress.text.retrieving.json.schema")) {
            retrieveJsonForTFLock(lock)
          } ?: return@withBackgroundProgress null
          indeterminateStep(HCLBundle.message("progress.text.building.local.schema")) {
            buildModelFromJson(json)
          }
        }
      }
      catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger<LocalSchemaService>().error("Cannot build model: $lock", e)
        throw e
      }
    }

  }

  private suspend fun retrieveJsonForTFLock(lock: VirtualFile): String? {
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
        return withContext(Dispatchers.IO) {
          localModelPath.resolve(lockData.jsonPath).readText()
        }
      }
      catch (e: Exception) {
        if (e is CancellationException) throw e
        logger<LocalSchemaService>().warn("Cannot load model json: $lock", e)
      }
    }

    val jsonFilePath = generateNewJsonFile(lock) ?: lockData?.jsonPath
    if (jsonFilePath != null) {
      writeAction {
        updateWorkspaceModel(lock, lockData, jsonFilePath)
      }
    }

    if (jsonFilePath == null) {
      logger<LocalSchemaService>().info("empty local model: ${lock.name}, switching to global")
      return null
    }
    return withContext(Dispatchers.IO) {
      localModelPath.resolve(jsonFilePath).readText()
    }
  }

  val localModelPath: Path
    get() {
      val localModelsPath = project.getProjectDataPath("terraform-local-models")
      Files.createDirectories(localModelsPath)
      return localModelsPath
    }

  private suspend fun generateNewJsonFile(lock: VirtualFile): @NlsSafe String? {
    val jsonFromProcess = buildJsonFromTerraformProcess(project, lock) ?: return null
    return withContext(Dispatchers.IO) {
      val uuid = UUID.randomUUID().toString()
      val jsonFile = localModelPath.resolve("$uuid.json")
      Files.writeString(jsonFile, jsonFromProcess)
      orphanCollector.restart()
      localModelPath.relativize(jsonFile).toString()
    }
  }


  private val orphanCollector: ExecuteLatest<Unit> = ExecuteLatest(scope) {
    delay(3000) // debounce
    awaitModelsReady()
    withBackgroundProgress(project, "Removing unused metadata") {
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

  fun scheduleOrphanMetadataCollection(): Job {
    return orphanCollector.restart()
  }

  private fun updateWorkspaceModel(lock: VirtualFile, prevLockData: TFLocalMetaEntity?, newJson: @NlsSafe String) {
    val low = (lock.timeStamp and 0xFFFFFFFFL).toInt()
    val high = (lock.timeStamp shr 32).toInt()
    WorkspaceModel.getInstance(project).updateProjectModel("Update TF Local Model from $lock") { storage ->
      if (prevLockData != null)
        storage.removeEntity(prevLockData)
      storage.addEntity(TFLocalMetaEntity(low, high, newJson,
                                          lock.toVirtualFileUrl(getVirtualFileUrlManager(project)),
                                          TFLocalMetaEntity.LockEntitySource

      ))
    }
  }

  private fun buildModelFromJson(json: String): TypeModel {
    val loader = TerraformMetadataLoader()
    json.byteInputStream().use { input ->
      loader.loadOne("local-schema.json", input)
    }
    return loader.loadDefaults()!!
  }

  private suspend fun buildJsonFromTerraformProcess(project: Project, lock: VirtualFile): @NlsSafe String? {
    logger<LocalSchemaService>().info("building local model buildJsonFromTerraformProcess: $lock")
    val capturingProcessAdapter = CapturingProcessAdapter()

    val success = TFExecutor.`in`(project, null)
      .withPresentableName(HCLBundle.message("rebuilding.local.schema"))
      .withParameters("providers", "schema", "-json")
      .withWorkDirectory(lock.parent.path)
      .withPassParentEnvironment(true)
      //.showOutputOnError()
      .withProcessListener(capturingProcessAdapter)
      .executeSuspendable()

    logger<LocalSchemaService>().info(
      "building local model buildJsonFromTerraformProcess result: ${coroutineContext.isActive}, $success  $lock")

    if (!success) {
      logger<LocalSchemaService>().warn("failed tp build model for $lock")
      return null
    }

    return capturingProcessAdapter.output.stdout.takeIf { it.isNotEmpty() }
  }

}


