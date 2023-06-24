package com.intellij.deno.entities

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import com.intellij.platform.workspace.storage.EntityType

internal object DenoEntitySource : EntitySource

interface DenoEntity : WorkspaceEntity {
  val depsFile: VirtualFileUrl?
  val denoTypes: VirtualFileUrl?

  //region generated code
  @GeneratedCodeApiVersion(2)
  interface Builder : DenoEntity, WorkspaceEntity.Builder<DenoEntity> {
    override var entitySource: EntitySource
    override var depsFile: VirtualFileUrl?
    override var denoTypes: VirtualFileUrl?
  }

  companion object : EntityType<DenoEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(entitySource: EntitySource, init: (Builder.() -> Unit)? = null): DenoEntity {
      val builder = builder()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion
}

//region generated code
fun MutableEntityStorage.modifyEntity(entity: DenoEntity, modification: DenoEntity.Builder.() -> Unit) = modifyEntity(
  DenoEntity.Builder::class.java, entity, modification)
//endregion
