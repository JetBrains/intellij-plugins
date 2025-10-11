package com.intellij.deno.entities

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

internal object DenoEntitySource : EntitySource

interface DenoEntity : WorkspaceEntity {
  val depsFile: VirtualFileUrl?
  val denoTypes: VirtualFileUrl?

  //region generated code
  @GeneratedCodeApiVersion(3)
  interface Builder : WorkspaceEntity.Builder<DenoEntity> {
    override var entitySource: EntitySource
    var depsFile: VirtualFileUrl?
    var denoTypes: VirtualFileUrl?
  }

  companion object : EntityType<DenoEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(
      entitySource: EntitySource,
      init: (Builder.() -> Unit)? = null,
    ): Builder {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion
}

//region generated code
fun MutableEntityStorage.modifyDenoEntity(
  entity: DenoEntity,
  modification: DenoEntity.Builder.() -> Unit,
): DenoEntity = modifyEntity(DenoEntity.Builder::class.java, entity, modification)
//endregion
