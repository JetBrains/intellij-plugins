package com.intellij.deno.roots

import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.excludeCondition
import com.intellij.openapi.util.registry.Registry
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileKind
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileSetRegistrar
import com.intellij.workspaceModel.core.fileIndex.impl.ModuleOrLibrarySourceRootData
import com.intellij.workspaceModel.ide.virtualFile
import com.intellij.platform.workspace.storage.EntityStorage

class DenoWorkspaceFileIndexContributor : WorkspaceFileIndexContributor<DenoEntity> {
  override val entityClass: Class<DenoEntity>
    get() = DenoEntity::class.java

  override fun registerFileSets(entity: DenoEntity, registrar: WorkspaceFileSetRegistrar, storage: EntityStorage) {
    if (!useWorkspaceModel() || !useWorkspaceFileIndexContributor()) return
    if (entity.denoTypes == null && entity.depsFile == null) return
    val allRootUrls = listOfNotNull(entity.denoTypes, entity.depsFile)
    val predicate = excludeCondition.transformToCondition(allRootUrls.mapNotNull { it.virtualFile })
    allRootUrls.forEach { url ->
      registrar.registerFileSet(url, WorkspaceFileKind.EXTERNAL_SOURCE, entity, DenoFileSetData())
      registrar.registerExclusionCondition(url, { predicate.value(it) }, entity)
    }
  }
}

internal fun useWorkspaceFileIndexContributor() = Registry.`is`("deno.use.workspace.file.index.contributor.api")
private class DenoFileSetData : ModuleOrLibrarySourceRootData