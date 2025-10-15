package com.intellij.deno.entities.impl

import com.intellij.deno.entities.DenoEntity
import com.intellij.deno.entities.DenoEntityBuilder
import com.intellij.platform.workspace.storage.ConnectionId
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.GeneratedCodeImplVersion
import com.intellij.platform.workspace.storage.WorkspaceEntityBuilder
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

@GeneratedCodeApiVersion(3)
@GeneratedCodeImplVersion(7)
@OptIn(WorkspaceEntityInternalApi::class)
internal class DenoEntityImpl(private val dataSource: DenoEntityData) : DenoEntity, WorkspaceEntityBase(dataSource) {

  private companion object {


    private val connections = listOf<ConnectionId>(
    )

  }

  override val depsFile: VirtualFileUrl?
    get() {
      readField("depsFile")
      return dataSource.depsFile
    }

  override val denoTypes: VirtualFileUrl?
    get() {
      readField("denoTypes")
      return dataSource.denoTypes
    }

  override val entitySource: EntitySource
    get() {
      readField("entitySource")
      return dataSource.entitySource
    }

  override fun connectionIdList(): List<ConnectionId> {
    return connections
  }


  internal class Builder(result: DenoEntityData?) : ModifiableWorkspaceEntityBase<DenoEntity, DenoEntityData>(result), DenoEntityBuilder {
    internal constructor() : this(DenoEntityData())

    override fun applyToBuilder(builder: MutableEntityStorage) {
      if (this.diff != null) {
        if (existsInBuilder(builder)) {
          this.diff = builder
          return
        }
        else {
          error("Entity DenoEntity is already created in a different builder")
        }
      }

      this.diff = builder
      addToBuilder()
      this.id = getEntityData().createEntityId()
      // After adding entity data to the builder, we need to unbind it and move the control over entity data to builder
      // Builder may switch to snapshot at any moment and lock entity data to modification
      this.currentEntityData = null

      index(this, "depsFile", this.depsFile)
      index(this, "denoTypes", this.denoTypes)
      // Process linked entities that are connected without a builder
      processLinkedEntities(builder)
      checkInitialization() // TODO uncomment and check failed tests
    }

    private fun checkInitialization() {
      val _diff = diff
      if (!getEntityData().isEntitySourceInitialized()) {
        error("Field WorkspaceEntity#entitySource should be initialized")
      }
    }

    override fun connectionIdList(): List<ConnectionId> {
      return connections
    }

    // Relabeling code, move information from dataSource to this builder
    override fun relabel(dataSource: WorkspaceEntity, parents: Set<WorkspaceEntity>?) {
      dataSource as DenoEntity
      if (this.entitySource != dataSource.entitySource) this.entitySource = dataSource.entitySource
      if (this.depsFile != dataSource?.depsFile) this.depsFile = dataSource.depsFile
      if (this.denoTypes != dataSource?.denoTypes) this.denoTypes = dataSource.denoTypes
      updateChildToParentReferences(parents)
    }


    override var entitySource: EntitySource
      get() = getEntityData().entitySource
      set(value) {
        checkModificationAllowed()
        getEntityData(true).entitySource = value
        changedProperty.add("entitySource")

      }

    override var depsFile: VirtualFileUrl?
      get() = getEntityData().depsFile
      set(value) {
        checkModificationAllowed()
        getEntityData(true).depsFile = value
        changedProperty.add("depsFile")
        val _diff = diff
        if (_diff != null) index(this, "depsFile", value)
      }

    override var denoTypes: VirtualFileUrl?
      get() = getEntityData().denoTypes
      set(value) {
        checkModificationAllowed()
        getEntityData(true).denoTypes = value
        changedProperty.add("denoTypes")
        val _diff = diff
        if (_diff != null) index(this, "denoTypes", value)
      }

    override fun getEntityClass(): Class<DenoEntity> = DenoEntity::class.java
  }
}

@OptIn(WorkspaceEntityInternalApi::class)
internal class DenoEntityData : WorkspaceEntityData<DenoEntity>() {
  var depsFile: VirtualFileUrl? = null
  var denoTypes: VirtualFileUrl? = null


  override fun wrapAsModifiable(diff: MutableEntityStorage): WorkspaceEntityBuilder<DenoEntity> {
    val modifiable = DenoEntityImpl.Builder(null)
    modifiable.diff = diff
    modifiable.id = createEntityId()
    return modifiable
  }

  @OptIn(EntityStorageInstrumentationApi::class)
  override fun createEntity(snapshot: EntityStorageInstrumentation): DenoEntity {
    val entityId = createEntityId()
    return snapshot.initializeEntity(entityId) {
      val entity = DenoEntityImpl(this)
      entity.snapshot = snapshot
      entity.id = entityId
      entity
    }
  }

  override fun getMetadata(): EntityMetadata {
    return MetadataStorageImpl.getMetadataByTypeFqn("com.intellij.deno.entities.DenoEntity") as EntityMetadata
  }

  override fun getEntityInterface(): Class<out WorkspaceEntity> {
    return DenoEntity::class.java
  }

  override fun createDetachedEntity(parents: List<WorkspaceEntityBuilder<*>>): WorkspaceEntityBuilder<*> {
    return DenoEntity(entitySource) {
      this.depsFile = this@DenoEntityData.depsFile
      this.denoTypes = this@DenoEntityData.denoTypes
    }
  }

  override fun getRequiredParents(): List<Class<out WorkspaceEntity>> {
    val res = mutableListOf<Class<out WorkspaceEntity>>()
    return res
  }

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as DenoEntityData

    if (this.entitySource != other.entitySource) return false
    if (this.depsFile != other.depsFile) return false
    if (this.denoTypes != other.denoTypes) return false
    return true
  }

  override fun equalsIgnoringEntitySource(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as DenoEntityData

    if (this.depsFile != other.depsFile) return false
    if (this.denoTypes != other.denoTypes) return false
    return true
  }

  override fun hashCode(): Int {
    var result = entitySource.hashCode()
    result = 31 * result + depsFile.hashCode()
    result = 31 * result + denoTypes.hashCode()
    return result
  }

  override fun hashCodeIgnoringEntitySource(): Int {
    var result = javaClass.hashCode()
    result = 31 * result + depsFile.hashCode()
    result = 31 * result + denoTypes.hashCode()
    return result
  }
}
