package com.intellij.deno.entities

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.ModifiableWorkspaceEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
interface ModifiableDenoEntity : ModifiableWorkspaceEntity<DenoEntity> {
  override var entitySource: EntitySource
  var depsFile: VirtualFileUrl?
  var denoTypes: VirtualFileUrl?
}

internal object DenoEntityType : EntityType<DenoEntity, ModifiableDenoEntity>() {
  override val entityClass: Class<DenoEntity> get() = DenoEntity::class.java
  operator fun invoke(
    entitySource: EntitySource,
    init: (ModifiableDenoEntity.() -> Unit)? = null,
  ): ModifiableDenoEntity {
    val builder = builder()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyDenoEntity(
  entity: DenoEntity,
  modification: ModifiableDenoEntity.() -> Unit,
): DenoEntity = modifyEntity(ModifiableDenoEntity::class.java, entity, modification)

@JvmOverloads
@JvmName("createDenoEntity")
fun DenoEntity(
  entitySource: EntitySource,
  init: (ModifiableDenoEntity.() -> Unit)? = null,
): ModifiableDenoEntity = DenoEntityType(entitySource, init)
