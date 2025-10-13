// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

interface TfLocalMetaEntity : WorkspaceEntity {

  val timeStampLow: Int

  val timeStampHigh: Int

  val jsonPath: String

  val lockFile: VirtualFileUrl

  val timeStamp: Long
    get() {
      return timeStampLow.toLong() and 0xFFFFFFFFL or (timeStampHigh.toLong() shl 32)
    }

  object LockEntitySource : EntitySource

  //region generated code
  @GeneratedCodeApiVersion(3)
  interface Builder : WorkspaceEntity.Builder<TfLocalMetaEntity> {
    override var entitySource: EntitySource
    var timeStampLow: Int
    var timeStampHigh: Int
    var jsonPath: String
    var lockFile: VirtualFileUrl
  }

  companion object : EntityType<TfLocalMetaEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(
      timeStampLow: Int,
      timeStampHigh: Int,
      jsonPath: String,
      lockFile: VirtualFileUrl,
      entitySource: EntitySource,
      init: (Builder.() -> Unit)? = null,
    ): Builder {
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
  //endregion

}

//region generated code
fun MutableEntityStorage.modifyTfLocalMetaEntity(
  entity: TfLocalMetaEntity,
  modification: TfLocalMetaEntity.Builder.() -> Unit,
): TfLocalMetaEntity = modifyEntity(TfLocalMetaEntity.Builder::class.java, entity, modification)
//endregion
