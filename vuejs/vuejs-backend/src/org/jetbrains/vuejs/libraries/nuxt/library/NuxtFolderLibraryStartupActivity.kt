// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.javascript.nodejs.library.yarn.pnp.workspaceModel.createStorageFrom
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

private class NuxtFolderLibraryStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    NuxtFolderModelSynchronizer.create(project).sync()
  }
}

internal class NuxtFolderModelSynchronizer internal constructor(
  private val workspaceModel: WorkspaceModel,
  private val nuxtFolderManager: NuxtFolderManager,
) {

  suspend fun sync() {
    val actualEntities: List<ModifiableNuxtFolderEntity> = buildActualEntities()
    val workspaceModelEntities: List<NuxtFolderEntity> = getWorkspaceModelEntities()
    if (areEntitiesOutdated(actualEntities, workspaceModelEntities)) {
      LOG.info("Syncing outdated .nuxt/ workspace model (old count: ${workspaceModelEntities.size}, new count: ${actualEntities.size})")
      updateEntities(actualEntities)
    }
  }

  private suspend fun buildActualEntities(): List<ModifiableNuxtFolderEntity> {
    val libraries = nuxtFolderManager.nuxtFolders.map {
      NuxtFolderReadyLibrary.create(it)
    }
    return libraries.map {
      NuxtFolderManager.createEntity(it, workspaceModel.getVirtualFileUrlManager())
    }
  }

  private fun areEntitiesOutdated(
    actualEntities: List<ModifiableNuxtFolderEntity>,
    workspaceModelEntities: List<NuxtFolderEntity>,
  ): Boolean {
    if (workspaceModelEntities.size != actualEntities.size) return true
    val wrappedWorkspaceModelEntities = workspaceModelEntities.mapTo(HashSet()) { NuxtFolderEntityWrapper(it) }
    val wrappedActualEntities = actualEntities.mapTo(HashSet()) { NuxtFolderEntityWrapper(it) }
    return wrappedWorkspaceModelEntities != wrappedActualEntities
  }

  private fun getWorkspaceModelEntities(): List<NuxtFolderEntity> {
    return workspaceModel.currentSnapshot.entities(NuxtFolderEntity::class.java).toList()
  }

  private suspend fun updateEntities(actualEntities: List<ModifiableNuxtFolderEntity>) {
    val entitiesStorage = createStorageFrom(actualEntities)
    nuxtFolderManager.updateWorkspaceModel("sync outdated .nuxt/") { _, storage ->
      storage.replaceBySource({ it === NuxtFolderEntity.MyEntitySource }, entitiesStorage)
    }
  }

  companion object {
    suspend fun create(project: Project): NuxtFolderModelSynchronizer = NuxtFolderModelSynchronizer(
      project.serviceAsync<WorkspaceModel>(),
      NuxtFolderManager.serviceAsync(project),
    )
  }
}

private class NuxtFolderEntityWrapper(
  entity: Any,
) {
  private val nuxtFolderUrl: VirtualFileUrl = (entity as? NuxtFolderEntity)?.nuxtFolderUrl
                                              ?: (entity as ModifiableNuxtFolderEntity).nuxtFolderUrl
  private val libraryFileUrls: Set<VirtualFileUrl> = (entity as? NuxtFolderEntity)?.libraryFileUrls?.toHashSet()
                                                     ?: (entity as ModifiableNuxtFolderEntity).libraryFileUrls.toHashSet()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as NuxtFolderEntityWrapper

    return nuxtFolderUrl == other.nuxtFolderUrl && libraryFileUrls == other.libraryFileUrls
  }

  override fun hashCode(): Int {
    var result = nuxtFolderUrl.hashCode()
    result = 31 * result + libraryFileUrls.hashCode()
    return result
  }
}
