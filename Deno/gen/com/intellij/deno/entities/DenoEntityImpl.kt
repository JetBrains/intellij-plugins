package com.intellij.deno.entities

import com.intellij.platform.workspaceModel.storage.*
import com.intellij.platform.workspaceModel.storage.EntityInformation
import com.intellij.platform.workspaceModel.storage.EntitySource
import com.intellij.platform.workspaceModel.storage.EntityStorage
import com.intellij.platform.workspaceModel.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspaceModel.storage.GeneratedCodeImplVersion
import com.intellij.platform.workspaceModel.storage.MutableEntityStorage
import com.intellij.platform.workspaceModel.storage.WorkspaceEntity
import com.intellij.platform.workspaceModel.storage.impl.ConnectionId
import com.intellij.platform.workspaceModel.storage.impl.ModifiableWorkspaceEntityBase
import com.intellij.platform.workspaceModel.storage.impl.UsedClassesCollector
import com.intellij.platform.workspaceModel.storage.impl.WorkspaceEntityBase
import com.intellij.platform.workspaceModel.storage.impl.WorkspaceEntityData
import com.intellij.platform.workspaceModel.storage.url.VirtualFileUrl
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import org.jetbrains.deft.ObjBuilder
import org.jetbrains.deft.Type

@GeneratedCodeApiVersion(1)
@GeneratedCodeImplVersion(1)
open class DenoEntityImpl(val dataSource: DenoEntityData) : DenoEntity, WorkspaceEntityBase() {

  companion object {


    val connections = listOf<ConnectionId>(
    )

  }

  override val depsFile: VirtualFileUrl?
    get() = dataSource.depsFile

  override val denoTypes: VirtualFileUrl?
    get() = dataSource.denoTypes

  override val entitySource: EntitySource
    get() = dataSource.entitySource

  override fun connectionIdList(): List<ConnectionId> {
    return connections
  }

  class Builder(result: DenoEntityData?) : ModifiableWorkspaceEntityBase<DenoEntity, DenoEntityData>(result), DenoEntity.Builder {
    constructor() : this(DenoEntityData())

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
      this.snapshot = builder
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

    fun checkInitialization() {
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

class DenoEntityData : WorkspaceEntityData<DenoEntity>() {
  var depsFile: VirtualFileUrl? = null
  var denoTypes: VirtualFileUrl? = null


  override fun wrapAsModifiable(diff: MutableEntityStorage): WorkspaceEntity.Builder<DenoEntity> {
    val modifiable = DenoEntityImpl.Builder(null)
    modifiable.diff = diff
    modifiable.snapshot = diff
    modifiable.id = createEntityId()
    return modifiable
  }

  override fun createEntity(snapshot: EntityStorage): DenoEntity {
    return getCached(snapshot) {
      val entity = DenoEntityImpl(this)
      entity.snapshot = snapshot
      entity.id = createEntityId()
      entity
    }
  }

  override fun getEntityInterface(): Class<out WorkspaceEntity> {
    return DenoEntity::class.java
  }

  override fun serialize(ser: EntityInformation.Serializer) {
  }

  override fun deserialize(de: EntityInformation.Deserializer) {
  }

  override fun createDetachedEntity(parents: List<WorkspaceEntity>): WorkspaceEntity {
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

  override fun collectClassUsagesData(collector: UsedClassesCollector) {
    this.depsFile?.let { collector.add(it::class.java) }
    this.denoTypes?.let { collector.add(it::class.java) }
    collector.sameForAllEntities = false
  }
}
