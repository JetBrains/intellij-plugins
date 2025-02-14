package org.intellij.terraform.config.model.local.impl

import com.intellij.platform.workspace.storage.WorkspaceEntityInternalApi
import com.intellij.platform.workspace.storage.metadata.impl.MetadataStorageBase
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata
import com.intellij.platform.workspace.storage.metadata.model.FinalClassMetadata
import com.intellij.platform.workspace.storage.metadata.model.OwnPropertyMetadata
import com.intellij.platform.workspace.storage.metadata.model.StorageTypeMetadata
import com.intellij.platform.workspace.storage.metadata.model.ValueTypeMetadata

@OptIn(WorkspaceEntityInternalApi::class)
internal object MetadataStorageImpl: MetadataStorageBase() {
  override fun initializeMetadata() {
    val primitiveTypeIntNotNullable = ValueTypeMetadata.SimpleType.PrimitiveType(isNullable = false, type = "Int")
    val primitiveTypeStringNotNullable = ValueTypeMetadata.SimpleType.PrimitiveType(isNullable = false, type = "String")

    var typeMetadata: StorageTypeMetadata

    typeMetadata = FinalClassMetadata.ObjectMetadata(fqName = "org.intellij.terraform.config.model.local.TFLocalMetaEntity\$LockEntitySource", properties = listOf(OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "virtualFileUrl", valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = true, typeMetadata = FinalClassMetadata.KnownClass(fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")), withDefault = false)), supertypes = listOf("com.intellij.platform.workspace.storage.EntitySource"))

    addMetadata(typeMetadata)

    typeMetadata = EntityMetadata(fqName = "org.intellij.terraform.config.model.local.TFLocalMetaEntity", entityDataFqName = "org.intellij.terraform.config.model.local.impl.TFLocalMetaEntityData", supertypes = listOf("com.intellij.platform.workspace.storage.WorkspaceEntity"), properties = listOf(OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "entitySource", valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = false, typeMetadata = FinalClassMetadata.KnownClass(fqName = "com.intellij.platform.workspace.storage.EntitySource")), withDefault = false),
                                                                                                                                                                                                                                                                                                         OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "timeStampLow", valueType = primitiveTypeIntNotNullable, withDefault = false),
                                                                                                                                                                                                                                                                                                         OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "timeStampHigh", valueType = primitiveTypeIntNotNullable, withDefault = false),
                                                                                                                                                                                                                                                                                                         OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "jsonPath", valueType = primitiveTypeStringNotNullable, withDefault = false),
                                                                                                                                                                                                                                                                                                         OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "lockFile", valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = false, typeMetadata = FinalClassMetadata.KnownClass(fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")), withDefault = false)), extProperties = listOf(), isAbstract = false)

    addMetadata(typeMetadata)
  }

  override fun initializeMetadataHash() {
    addMetadataHash(typeFqn = "org.intellij.terraform.config.model.local.TFLocalMetaEntity", metadataHash = 231549997)
    addMetadataHash(typeFqn = "com.intellij.platform.workspace.storage.EntitySource", metadataHash = -1739507243)
    addMetadataHash(typeFqn = "org.intellij.terraform.config.model.local.TFLocalMetaEntity\$LockEntitySource", metadataHash = -2114129321)
  }

}
