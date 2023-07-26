package com.intellij.deno.roots

import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.excludeCondition
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileKind
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileSetRegistrar
import com.intellij.workspaceModel.core.fileIndex.impl.ModuleOrLibrarySourceRootData

class DenoWorkspaceFileIndexContributor : WorkspaceFileIndexContributor<DenoEntity> {
  override val entityClass: Class<DenoEntity>
    get() = DenoEntity::class.java

  override fun registerFileSets(entity: DenoEntity, registrar: WorkspaceFileSetRegistrar, storage: EntityStorage) {
    if (!useWorkspaceModel()) return
    if (entity.denoTypes == null && entity.depsFile == null) return
    val allRootUrls = listOfNotNull(entity.denoTypes, entity.depsFile)
    val predicate = excludeCondition.transformToCondition(allRootUrls.mapNotNull { it.virtualFile })
    allRootUrls.forEach { url ->
      registrar.registerFileSet(url, WorkspaceFileKind.EXTERNAL_SOURCE, entity, DenoFileSetData())
      registrar.registerExclusionCondition(url, { predicate.value(it) }, entity)
    }
  }
}
private class DenoFileSetData : ModuleOrLibrarySourceRootData