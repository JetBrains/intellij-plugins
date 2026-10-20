// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:OptIn(EntityStorageInstrumentationApi::class)

package com.intellij.lang.javascript.flex.projectStructure.entities.impl

import com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntity
import com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntityBuilder
import com.intellij.platform.workspace.jps.entities.SdkId
import com.intellij.platform.workspace.storage.ConnectionId
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.GeneratedCodeImplVersion
import com.intellij.platform.workspace.storage.SymbolicEntityId
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.WorkspaceEntityBuilder
import com.intellij.platform.workspace.storage.WorkspaceEntityInternalApi
import com.intellij.platform.workspace.storage.impl.ModifiableWorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.SoftLinkable
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityData
import com.intellij.platform.workspace.storage.impl.containers.MutableWorkspaceList
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceList
import com.intellij.platform.workspace.storage.impl.indices.WorkspaceMutableIndex
import com.intellij.platform.workspace.storage.instrumentation.EntityStorageInstrumentationApi
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata

@GeneratedCodeApiVersion(3)
@GeneratedCodeImplVersion(7)
@OptIn(WorkspaceEntityInternalApi::class)
internal class FlexBuildConfigurationSdksEntityImpl(private val dataSource: FlexBuildConfigurationSdksEntityData) :
  FlexBuildConfigurationSdksEntity, WorkspaceEntityBase(dataSource) {

  override val sdks: List<SdkId>
    get() {
      readField("sdks")
      return dataSource.sdks
    }
  override val entitySource: EntitySource
    get() {
      readField("entitySource")
      return dataSource.entitySource
    }

  override fun connectionIdList(): List<ConnectionId> {
    return emptyList()
  }

  internal class Builder(result: FlexBuildConfigurationSdksEntityData?) :
    ModifiableWorkspaceEntityBase<FlexBuildConfigurationSdksEntity, FlexBuildConfigurationSdksEntityData>(result),
    FlexBuildConfigurationSdksEntityBuilder {
    internal constructor() : this(FlexBuildConfigurationSdksEntityData())

    override fun checkInitialization() {
      val _diff = diff
      if (!getEntityData().isEntitySourceInitialized()) {
        error("Field WorkspaceEntity#entitySource should be initialized")
      }
      if (!getEntityData().isSdksInitialized()) {
        error("Field FlexBuildConfigurationSdksEntity#sdks should be initialized")
      }
    }

    override fun connectionIdList(): List<ConnectionId> {
      return emptyList()
    }

    override fun afterModification() {
      val collection_sdks = getEntityData().sdks
      if (collection_sdks is MutableWorkspaceList<*>) {
        collection_sdks.cleanModificationUpdateAction()
      }
    }

    // Relabeling code, move information from dataSource to this builder
    override fun relabel(dataSource: WorkspaceEntity, parents: Set<WorkspaceEntity>?) {
      dataSource as FlexBuildConfigurationSdksEntity
      if (this.entitySource != dataSource.entitySource) this.entitySource = dataSource.entitySource
      if (this.sdks != dataSource.sdks) this.sdks = dataSource.sdks.toMutableList()
      updateChildToParentReferences(parents)
    }

    override var entitySource: EntitySource
      get() = getEntityData().entitySource
      set(value) {
        checkModificationAllowed()
        getEntityData(true).entitySource = value
        changedProperty.add("entitySource")
      }
    private val sdksUpdater: (value: List<SdkId>) -> Unit = { value ->
      changedProperty.add("sdks")
    }
    override var sdks: MutableList<SdkId>
      get() {
        val collection_sdks = getEntityData().sdks
        if (collection_sdks !is MutableWorkspaceList) return collection_sdks
        if (diff == null || modifiable.get()) {
          collection_sdks.setModificationUpdateAction(sdksUpdater)
        }
        else {
          collection_sdks.cleanModificationUpdateAction()
        }
        return collection_sdks
      }
      set(value) {
        checkModificationAllowed()
        getEntityData(true).sdks = value
        sdksUpdater.invoke(value)
      }

    override fun getEntityClass(): Class<FlexBuildConfigurationSdksEntity> = FlexBuildConfigurationSdksEntity::class.java
  }
}

@OptIn(WorkspaceEntityInternalApi::class)
internal class FlexBuildConfigurationSdksEntityData : WorkspaceEntityData<FlexBuildConfigurationSdksEntity>(), SoftLinkable {
  lateinit var sdks: MutableList<SdkId>
  internal fun isSdksInitialized(): Boolean = ::sdks.isInitialized
  override fun getLinks(): Set<SymbolicEntityId<*>> {
    val result = HashSet<SymbolicEntityId<*>>()
    for (item in sdks) {
      result.add(item)
    }
    return result
  }

  override fun index(index: WorkspaceMutableIndex<SymbolicEntityId<*>>) {
    for (item in sdks) {
      index.index(this, item)
    }
  }

  override fun updateLinksIndex(prev: Set<SymbolicEntityId<*>>, index: WorkspaceMutableIndex<SymbolicEntityId<*>>) {
    val mutablePreviousSet = HashSet(prev)
    for (item in sdks) {
      val removedItem_item = mutablePreviousSet.remove(item)
      if (!removedItem_item) {
        index.index(this, item)
      }
    }
    for (removed in mutablePreviousSet) {
      index.remove(this, removed)
    }
  }

  override fun updateLink(oldLink: SymbolicEntityId<*>, newLink: SymbolicEntityId<*>): Boolean {
    var changed = false
    val sdks_data = sdks.map {
      val it_data = if (it == oldLink) {
        changed = true
        newLink as SdkId
      }
      else {
        null
      }
      if (it_data != null) {
        it_data
      }
      else {
        it
      }
    }

    if (sdks_data != null) {
      sdks = sdks_data as MutableList<SdkId>
    }
    return changed
  }

  override fun newInstance(): FlexBuildConfigurationSdksEntity = FlexBuildConfigurationSdksEntityImpl(this)
  override fun newBuilderInstance(): ModifiableWorkspaceEntityBase<FlexBuildConfigurationSdksEntity, *> =
    FlexBuildConfigurationSdksEntityImpl.Builder(null)

  override fun getMetadata(): EntityMetadata {
    return MetadataStorageImpl.getMetadataByTypeFqn("com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntity") as EntityMetadata
  }

  override fun clone(): FlexBuildConfigurationSdksEntityData {
    val clonedEntity = super.clone()
    clonedEntity as FlexBuildConfigurationSdksEntityData
    clonedEntity.sdks = clonedEntity.sdks.toMutableWorkspaceList()
    return clonedEntity
  }

  override fun getEntityInterface(): Class<out WorkspaceEntity> {
    return FlexBuildConfigurationSdksEntity::class.java
  }

  override fun createDetachedEntity(parents: List<WorkspaceEntityBuilder<*>>): WorkspaceEntityBuilder<*> {
    return FlexBuildConfigurationSdksEntity(sdks, entitySource)
  }

  override fun getRequiredParents(): List<Class<out WorkspaceEntity>> {
    val res = mutableListOf<Class<out WorkspaceEntity>>()
    return res
  }

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false
    other as FlexBuildConfigurationSdksEntityData
    if (this.entitySource != other.entitySource) return false
    if (this.sdks != other.sdks) return false
    return true
  }

  override fun equalsIgnoringEntitySource(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false
    other as FlexBuildConfigurationSdksEntityData
    if (this.sdks != other.sdks) return false
    return true
  }

  override fun hashCode(): Int {
    var result = entitySource.hashCode()
    result = 31 * result + sdks.hashCode()
    return result
  }

  override fun hashCodeIgnoringEntitySource(): Int {
    var result = javaClass.hashCode()
    result = 31 * result + sdks.hashCode()
    return result
  }
}
