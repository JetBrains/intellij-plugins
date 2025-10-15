package org.intellij.terraform.config.model.local

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.ModifiableWorkspaceEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
interface ModifiableTfLocalMetaEntity : ModifiableWorkspaceEntity<TfLocalMetaEntity> {
  override var entitySource: EntitySource
  var timeStampLow: Int
  var timeStampHigh: Int
  var jsonPath: String
  var lockFile: VirtualFileUrl
}

internal object TfLocalMetaEntityType : EntityType<TfLocalMetaEntity, ModifiableTfLocalMetaEntity>() {
  override val entityClass: Class<TfLocalMetaEntity> get() = TfLocalMetaEntity::class.java
  operator fun invoke(
    timeStampLow: Int,
    timeStampHigh: Int,
    jsonPath: String,
    lockFile: VirtualFileUrl,
    entitySource: EntitySource,
    init: (ModifiableTfLocalMetaEntity.() -> Unit)? = null,
  ): ModifiableTfLocalMetaEntity {
    val builder = builder()
    builder.timeStampLow = timeStampLow
    builder.timeStampHigh = timeStampHigh
    builder.jsonPath = jsonPath
    builder.lockFile = lockFile
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyTfLocalMetaEntity(
  entity: TfLocalMetaEntity,
  modification: ModifiableTfLocalMetaEntity.() -> Unit,
): TfLocalMetaEntity = modifyEntity(ModifiableTfLocalMetaEntity::class.java, entity, modification)

@JvmOverloads
@JvmName("createTfLocalMetaEntity")
fun TfLocalMetaEntity(
  timeStampLow: Int,
  timeStampHigh: Int,
  jsonPath: String,
  lockFile: VirtualFileUrl,
  entitySource: EntitySource,
  init: (ModifiableTfLocalMetaEntity.() -> Unit)? = null,
): ModifiableTfLocalMetaEntity = TfLocalMetaEntityType(timeStampLow, timeStampHigh, jsonPath, lockFile, entitySource, init)
