// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local.impl

import com.intellij.platform.workspace.storage.ConnectionId
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.GeneratedCodeImplVersion
import com.intellij.platform.workspace.storage.ModifiableWorkspaceEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.WorkspaceEntityInternalApi
import com.intellij.platform.workspace.storage.impl.ModifiableWorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityData
import com.intellij.platform.workspace.storage.instrumentation.EntityStorageInstrumentation
import com.intellij.platform.workspace.storage.instrumentation.EntityStorageInstrumentationApi
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import org.intellij.terraform.config.model.local.ModifiableTfLocalMetaEntity
import org.intellij.terraform.config.model.local.TfLocalMetaEntity

@GeneratedCodeApiVersion(3)
@GeneratedCodeImplVersion(7)
@OptIn(WorkspaceEntityInternalApi::class)
internal class TfLocalMetaEntityImpl(private val dataSource: TfLocalMetaEntityData) : TfLocalMetaEntity, WorkspaceEntityBase(dataSource) {

  private companion object {


    private val connections = listOf<ConnectionId>(
    )

  }

  override val timeStampLow: Int
    get() {
      readField("timeStampLow")
      return dataSource.timeStampLow
    }
  override val timeStampHigh: Int
    get() {
      readField("timeStampHigh")
      return dataSource.timeStampHigh
    }
  override val jsonPath: String
    get() {
      readField("jsonPath")
      return dataSource.jsonPath
    }

  override val lockFile: VirtualFileUrl
    get() {
      readField("lockFile")
      return dataSource.lockFile
    }

  override val entitySource: EntitySource
    get() {
      readField("entitySource")
      return dataSource.entitySource
    }

  override fun connectionIdList(): List<ConnectionId> {
    return connections
  }


  internal class Builder(result: TfLocalMetaEntityData?) : ModifiableWorkspaceEntityBase<TfLocalMetaEntity, TfLocalMetaEntityData>(
    result), ModifiableTfLocalMetaEntity {
    internal constructor() : this(TfLocalMetaEntityData())

    override fun applyToBuilder(builder: MutableEntityStorage) {
      if (this.diff != null) {
        if (existsInBuilder(builder)) {
          this.diff = builder
          return
        }
        else {
          error("Entity TfLocalMetaEntity is already created in a different builder")
        }
      }

      this.diff = builder
      addToBuilder()
      this.id = getEntityData().createEntityId()
      // After adding entity data to the builder, we need to unbind it and move the control over entity data to builder
      // Builder may switch to snapshot at any moment and lock entity data to modification
      this.currentEntityData = null

      index(this, "lockFile", this.lockFile)
      // Process linked entities that are connected without a builder
      processLinkedEntities(builder)
      checkInitialization() // TODO uncomment and check failed tests
    }

    private fun checkInitialization() {
      val _diff = diff
      if (!getEntityData().isEntitySourceInitialized()) {
        error("Field WorkspaceEntity#entitySource should be initialized")
      }
      if (!getEntityData().isJsonPathInitialized()) {
        error("Field TfLocalMetaEntity#jsonPath should be initialized")
      }
      if (!getEntityData().isLockFileInitialized()) {
        error("Field TfLocalMetaEntity#lockFile should be initialized")
      }
    }

    override fun connectionIdList(): List<ConnectionId> {
      return connections
    }

    // Relabeling code, move information from dataSource to this builder
    override fun relabel(dataSource: WorkspaceEntity, parents: Set<WorkspaceEntity>?) {
      dataSource as TfLocalMetaEntity
      if (this.entitySource != dataSource.entitySource) this.entitySource = dataSource.entitySource
      if (this.timeStampLow != dataSource.timeStampLow) this.timeStampLow = dataSource.timeStampLow
      if (this.timeStampHigh != dataSource.timeStampHigh) this.timeStampHigh = dataSource.timeStampHigh
      if (this.jsonPath != dataSource.jsonPath) this.jsonPath = dataSource.jsonPath
      if (this.lockFile != dataSource.lockFile) this.lockFile = dataSource.lockFile
      updateChildToParentReferences(parents)
    }


    override var entitySource: EntitySource
      get() = getEntityData().entitySource
      set(value) {
        checkModificationAllowed()
        getEntityData(true).entitySource = value
        changedProperty.add("entitySource")

      }

    override var timeStampLow: Int
      get() = getEntityData().timeStampLow
      set(value) {
        checkModificationAllowed()
        getEntityData(true).timeStampLow = value
        changedProperty.add("timeStampLow")
      }

    override var timeStampHigh: Int
      get() = getEntityData().timeStampHigh
      set(value) {
        checkModificationAllowed()
        getEntityData(true).timeStampHigh = value
        changedProperty.add("timeStampHigh")
      }

    override var jsonPath: String
      get() = getEntityData().jsonPath
      set(value) {
        checkModificationAllowed()
        getEntityData(true).jsonPath = value
        changedProperty.add("jsonPath")
      }

    override var lockFile: VirtualFileUrl
      get() = getEntityData().lockFile
      set(value) {
        checkModificationAllowed()
        getEntityData(true).lockFile = value
        changedProperty.add("lockFile")
        val _diff = diff
        if (_diff != null) index(this, "lockFile", value)
      }

    override fun getEntityClass(): Class<TfLocalMetaEntity> = TfLocalMetaEntity::class.java
  }
}

@OptIn(WorkspaceEntityInternalApi::class)
internal class TfLocalMetaEntityData : WorkspaceEntityData<TfLocalMetaEntity>() {
  var timeStampLow: Int = 0
  var timeStampHigh: Int = 0
  lateinit var jsonPath: String
  lateinit var lockFile: VirtualFileUrl


  internal fun isJsonPathInitialized(): Boolean = ::jsonPath.isInitialized
  internal fun isLockFileInitialized(): Boolean = ::lockFile.isInitialized

  override fun wrapAsModifiable(diff: MutableEntityStorage): ModifiableWorkspaceEntity<TfLocalMetaEntity> {
    val modifiable = TfLocalMetaEntityImpl.Builder(null)
    modifiable.diff = diff
    modifiable.id = createEntityId()
    return modifiable
  }

  @OptIn(EntityStorageInstrumentationApi::class)
  override fun createEntity(snapshot: EntityStorageInstrumentation): TfLocalMetaEntity {
    val entityId = createEntityId()
    return snapshot.initializeEntity(entityId) {
      val entity = TfLocalMetaEntityImpl(this)
      entity.snapshot = snapshot
      entity.id = entityId
      entity
    }
  }

  override fun getMetadata(): EntityMetadata {
    return MetadataStorageImpl.getMetadataByTypeFqn("org.intellij.terraform.config.model.local.TfLocalMetaEntity") as EntityMetadata
  }

  override fun getEntityInterface(): Class<out WorkspaceEntity> {
    return TfLocalMetaEntity::class.java
  }

  override fun createDetachedEntity(parents: List<ModifiableWorkspaceEntity<*>>): ModifiableWorkspaceEntity<*> {
    return TfLocalMetaEntity(timeStampLow, timeStampHigh, jsonPath, lockFile, entitySource) {
    }
  }

  override fun getRequiredParents(): List<Class<out WorkspaceEntity>> {
    val res = mutableListOf<Class<out WorkspaceEntity>>()
    return res
  }

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as TfLocalMetaEntityData

    if (this.entitySource != other.entitySource) return false
    if (this.timeStampLow != other.timeStampLow) return false
    if (this.timeStampHigh != other.timeStampHigh) return false
    if (this.jsonPath != other.jsonPath) return false
    if (this.lockFile != other.lockFile) return false
    return true
  }

  override fun equalsIgnoringEntitySource(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as TfLocalMetaEntityData

    if (this.timeStampLow != other.timeStampLow) return false
    if (this.timeStampHigh != other.timeStampHigh) return false
    if (this.jsonPath != other.jsonPath) return false
    if (this.lockFile != other.lockFile) return false
    return true
  }

  override fun hashCode(): Int {
    var result = entitySource.hashCode()
    result = 31 * result + timeStampLow.hashCode()
    result = 31 * result + timeStampHigh.hashCode()
    result = 31 * result + jsonPath.hashCode()
    result = 31 * result + lockFile.hashCode()
    return result
  }

  override fun hashCodeIgnoringEntitySource(): Int {
    var result = javaClass.hashCode()
    result = 31 * result + timeStampLow.hashCode()
    result = 31 * result + timeStampHigh.hashCode()
    result = 31 * result + jsonPath.hashCode()
    result = 31 * result + lockFile.hashCode()
    return result
  }
}
