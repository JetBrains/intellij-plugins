@file:JvmName("DenoEntityModifications")

package com.intellij.deno.entities

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.WorkspaceEntityBuilder
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
interface DenoEntityBuilder : WorkspaceEntityBuilder<DenoEntity> {
  override var entitySource: EntitySource
  var depsFile: VirtualFileUrl?
  var denoTypes: VirtualFileUrl?
}

internal object DenoEntityType : EntityType<DenoEntity, DenoEntityBuilder>() {
  override val entityClass: Class<DenoEntity> get() = DenoEntity::class.java
  operator fun invoke(
    entitySource: EntitySource,
    init: (DenoEntityBuilder.() -> Unit)? = null,
  ): DenoEntityBuilder {
    val builder = builder()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyDenoEntity(
  entity: DenoEntity,
  modification: DenoEntityBuilder.() -> Unit,
): DenoEntity = modifyEntity(DenoEntityBuilder::class.java, entity, modification)

@JvmOverloads
@JvmName("createDenoEntity")
fun DenoEntity(
  entitySource: EntitySource,
  init: (DenoEntityBuilder.() -> Unit)? = null,
): DenoEntityBuilder = DenoEntityType(entitySource, init)
