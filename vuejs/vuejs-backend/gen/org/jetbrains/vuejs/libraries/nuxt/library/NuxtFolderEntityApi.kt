// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library

import com.intellij.platform.workspace.storage.*
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
internal interface ModifiableNuxtFolderEntity : ModifiableWorkspaceEntity<NuxtFolderEntity> {
  override var entitySource: EntitySource
  var nuxtFolderUrl: VirtualFileUrl
  var libraryFileUrls: MutableList<VirtualFileUrl>
}

internal object NuxtFolderEntityType : EntityType<NuxtFolderEntity, ModifiableNuxtFolderEntity>() {
  override val entityClass: Class<NuxtFolderEntity> get() = NuxtFolderEntity::class.java
  operator fun invoke(
    nuxtFolderUrl: VirtualFileUrl,
    libraryFileUrls: List<VirtualFileUrl>,
    entitySource: EntitySource,
    init: (ModifiableNuxtFolderEntity.() -> Unit)? = null,
  ): ModifiableNuxtFolderEntity {
    val builder = builder()
    builder.nuxtFolderUrl = nuxtFolderUrl
    builder.libraryFileUrls = libraryFileUrls.toMutableWorkspaceList()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

internal fun MutableEntityStorage.modifyNuxtFolderEntity(
  entity: NuxtFolderEntity,
  modification: ModifiableNuxtFolderEntity.() -> Unit,
): NuxtFolderEntity = modifyEntity(ModifiableNuxtFolderEntity::class.java, entity, modification)

@JvmOverloads
@JvmName("createNuxtFolderEntity")
internal fun NuxtFolderEntity(
  nuxtFolderUrl: VirtualFileUrl,
  libraryFileUrls: List<VirtualFileUrl>,
  entitySource: EntitySource,
  init: (ModifiableNuxtFolderEntity.() -> Unit)? = null,
): ModifiableNuxtFolderEntity = NuxtFolderEntityType(nuxtFolderUrl, libraryFileUrls, entitySource, init)
