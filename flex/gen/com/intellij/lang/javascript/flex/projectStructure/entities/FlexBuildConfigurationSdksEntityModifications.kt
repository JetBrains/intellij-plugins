// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:JvmName("FlexBuildConfigurationSdksEntityModifications")

package com.intellij.lang.javascript.flex.projectStructure.entities

import com.intellij.lang.javascript.flex.projectStructure.entities.impl.FlexBuildConfigurationSdksEntityImpl
import com.intellij.platform.workspace.jps.entities.SdkId
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntityBuilder
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList

@GeneratedCodeApiVersion(3)
interface FlexBuildConfigurationSdksEntityBuilder : WorkspaceEntityBuilder<FlexBuildConfigurationSdksEntity> {
  override var entitySource: EntitySource
  var sdks: MutableList<SdkId>
}

internal object FlexBuildConfigurationSdksEntityType :
  EntityType<FlexBuildConfigurationSdksEntity, FlexBuildConfigurationSdksEntityBuilder>() {
  override val entityImplClass: Class<*> get() = FlexBuildConfigurationSdksEntityImpl::class.java
  override val entityImplBuilderClass: Class<*> get() = FlexBuildConfigurationSdksEntityImpl.Builder::class.java
  operator fun invoke(
    sdks: List<SdkId>,
    entitySource: EntitySource,
    init: (FlexBuildConfigurationSdksEntityBuilder.() -> Unit)? = null,
  ): FlexBuildConfigurationSdksEntityBuilder {
    val builder = builder()
    builder.sdks = sdks.toMutableWorkspaceList()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyFlexBuildConfigurationSdksEntity(
  entity: FlexBuildConfigurationSdksEntity,
  modification: FlexBuildConfigurationSdksEntityBuilder.() -> Unit,
): FlexBuildConfigurationSdksEntity = modifyEntity(FlexBuildConfigurationSdksEntityBuilder::class.java, entity, modification)

@JvmOverloads
@JvmName("createFlexBuildConfigurationSdksEntity")
fun FlexBuildConfigurationSdksEntity(
  sdks: List<SdkId>,
  entitySource: EntitySource,
  init: (FlexBuildConfigurationSdksEntityBuilder.() -> Unit)? = null,
): FlexBuildConfigurationSdksEntityBuilder = FlexBuildConfigurationSdksEntityType(sdks, entitySource, init)
