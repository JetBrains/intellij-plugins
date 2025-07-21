// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.javascript.nodejs.library.yarn.pnp.workspaceModel.createStorageFrom
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import org.jetbrains.annotations.TestOnly

private class NuxtFolderLibraryStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    NuxtFolderModelSynchronizer(
      project = project,
      workspaceModel = project.serviceAsync<WorkspaceModel>(),
      nuxtFolderManager = project.serviceAsync<NuxtFolderManager>(),
    ).sync()
  }
}

internal class NuxtFolderModelSynchronizer internal constructor(
  private val project: Project,
  private val workspaceModel: WorkspaceModel,
  nuxtFolderManager: NuxtFolderManager,
) {
  private val libraries: List<NuxtFolderLibrary> = nuxtFolderManager.nuxtFolders.map {
    NuxtFolderLibrary(it)
  }

  @TestOnly
  constructor(project: Project) : this(project, WorkspaceModel.getInstance(project), NuxtFolderManager.getInstance(project))

  fun sync() {
    val actualEntities = buildActualEntities()
    if (areEntitiesOutdated(actualEntities)) {
      updateEntities(actualEntities)
    }
  }

  private fun buildActualEntities(): List<NuxtFolderEntity.Builder> {
    return libraries.map {
      NuxtFolderManager.createEntity(it, workspaceModel.getVirtualFileUrlManager())
    }
  }

  private fun areEntitiesOutdated(actualEntities: List<NuxtFolderEntity.Builder>): Boolean {
    val workspaceModelEntities: List<NuxtFolderEntity> = getWorkspaceModelEntities()
    if (workspaceModelEntities.size != actualEntities.size) return true
    val wrappedWorkspaceModelEntities = workspaceModelEntities.mapTo(HashSet()) { NuxtFolderEntityWrapper(it) }
    val wrappedActualEntities = actualEntities.mapTo(HashSet()) { NuxtFolderEntityWrapper(it) }
    return wrappedWorkspaceModelEntities != wrappedActualEntities
  }

  private fun getWorkspaceModelEntities(): List<NuxtFolderEntity> {
    return workspaceModel.currentSnapshot.entities(NuxtFolderEntity::class.java).toList()
  }

  private fun updateEntities(actualEntities: List<NuxtFolderEntity.Builder>) {
    val entitiesStorage = createStorageFrom(actualEntities)
    NuxtFolderManager.getInstance(project).updateWorkspaceModel(".nuxt outdated (new count: ${actualEntities.size})") { _, storage ->
      storage.replaceBySource({ it === NuxtFolderEntity.MyEntitySource }, entitiesStorage)
    }
  }
}

private class NuxtFolderEntityWrapper(
  entity: Any,
) {
  private val nuxtFolderUrl: VirtualFileUrl = (entity as? NuxtFolderEntity)?.nuxtFolderUrl
                                              ?: (entity as NuxtFolderEntity.Builder).nuxtFolderUrl
  private val libraryFileUrls: Set<VirtualFileUrl> = (entity as? NuxtFolderEntity)?.libraryFileUrls?.toHashSet()
                                                     ?: (entity as NuxtFolderEntity.Builder).libraryFileUrls.toHashSet()

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
