// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.lang.javascript.library.JSLibraryToResolveScopeContributor
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileKind
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileSetRegistrar
import com.intellij.workspaceModel.storage.EntityStorage

class NuxtFolderLibraryContributor : WorkspaceFileIndexContributor<NuxtFolderEntity>, JSLibraryToResolveScopeContributor {
  override val entityClass: Class<NuxtFolderEntity>
    get() = NuxtFolderEntity::class.java

  override fun registerFileSets(entity: NuxtFolderEntity, registrar: WorkspaceFileSetRegistrar, storage: EntityStorage) {
    registrar.registerExcludedRoot(entity.nuxtFolderUrl, entity)
    entity.libraryFileUrls.forEach {
      registrar.registerFileSet(it, WorkspaceFileKind.EXTERNAL_SOURCE, entity, null)
    }
  }
}