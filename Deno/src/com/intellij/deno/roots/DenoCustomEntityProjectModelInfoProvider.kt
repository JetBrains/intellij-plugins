package com.intellij.deno.roots

import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.excludeCondition
import com.intellij.openapi.roots.impl.CustomEntityProjectModelInfoProvider
import com.intellij.workspaceModel.ide.virtualFile
import com.intellij.platform.workspace.storage.EntityStorage

class DenoCustomEntityProjectModelInfoProvider : CustomEntityProjectModelInfoProvider<DenoEntity> {
  override fun getEntityClass(): Class<DenoEntity> = DenoEntity::class.java

  override fun getLibraryRoots(entities: Sequence<DenoEntity>,
                               entityStorage: EntityStorage): Sequence<CustomEntityProjectModelInfoProvider.LibraryRoots<DenoEntity>> {
    if (!useWorkspaceModel() || useWorkspaceFileIndexContributor()) return emptySequence()
    return entities.mapNotNull {
      if (it.denoTypes == null && it.depsFile == null) return@mapNotNull null

      CustomEntityProjectModelInfoProvider.LibraryRoots(
        it,
        listOfNotNull(it.denoTypes?.virtualFile, it.depsFile?.virtualFile),
        emptyList(),
        emptyList(),
        excludeCondition,
      )
    }
  }
}