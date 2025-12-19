// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:JvmName("TfLocalMetaEntityModifications")

package org.intellij.terraform.config.model.local

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntityBuilder
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
interface TfLocalMetaEntityBuilder : WorkspaceEntityBuilder<TfLocalMetaEntity> {
  override var entitySource: EntitySource
  var timeStampLow: Int
  var timeStampHigh: Int
  var jsonPath: String
  var lockFile: VirtualFileUrl
}

internal object TfLocalMetaEntityType : EntityType<TfLocalMetaEntity, TfLocalMetaEntityBuilder>() {
  override val entityClass: Class<TfLocalMetaEntity> get() = TfLocalMetaEntity::class.java
  operator fun invoke(
    timeStampLow: Int,
    timeStampHigh: Int,
    jsonPath: String,
    lockFile: VirtualFileUrl,
    entitySource: EntitySource,
    init: (TfLocalMetaEntityBuilder.() -> Unit)? = null,
  ): TfLocalMetaEntityBuilder {
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
  modification: TfLocalMetaEntityBuilder.() -> Unit,
): TfLocalMetaEntity = modifyEntity(TfLocalMetaEntityBuilder::class.java, entity, modification)

@JvmOverloads
@JvmName("createTfLocalMetaEntity")
fun TfLocalMetaEntity(
  timeStampLow: Int,
  timeStampHigh: Int,
  jsonPath: String,
  lockFile: VirtualFileUrl,
  entitySource: EntitySource,
  init: (TfLocalMetaEntityBuilder.() -> Unit)? = null,
): TfLocalMetaEntityBuilder = TfLocalMetaEntityType(timeStampLow, timeStampHigh, jsonPath, lockFile, entitySource, init)
