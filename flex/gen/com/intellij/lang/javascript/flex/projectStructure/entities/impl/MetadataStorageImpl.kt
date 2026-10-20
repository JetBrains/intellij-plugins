// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.entities.impl

import com.intellij.platform.workspace.storage.WorkspaceEntityInternalApi
import com.intellij.platform.workspace.storage.metadata.impl.MetadataStorageBase
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata
import com.intellij.platform.workspace.storage.metadata.model.FinalClassMetadata
import com.intellij.platform.workspace.storage.metadata.model.OwnPropertyMetadata
import com.intellij.platform.workspace.storage.metadata.model.StorageTypeMetadata
import com.intellij.platform.workspace.storage.metadata.model.ValueTypeMetadata

@OptIn(WorkspaceEntityInternalApi::class)
internal object MetadataStorageImpl : MetadataStorageBase() {
  override fun initializeMetadata() {
    val primitiveTypeStringNotNullable = ValueTypeMetadata.SimpleType.PrimitiveType(isNullable = false, type = "String")
    val primitiveTypeListNotNullable = ValueTypeMetadata.SimpleType.PrimitiveType(isNullable = false, type = "List")
    var typeMetadata: StorageTypeMetadata
    typeMetadata =
      FinalClassMetadata.ObjectMetadata(fqName = "com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntitySource",
                                        properties = listOf(OwnPropertyMetadata(isComputable = false,
                                                                                isKey = false,
                                                                                isOpen = false,
                                                                                name = "virtualFileUrl",
                                                                                valueType = ValueTypeMetadata.SimpleType.CustomType(
                                                                                  isNullable = true,
                                                                                  typeMetadata = FinalClassMetadata.KnownClass(fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                                                                                withDefault = false)),
                                        supertypes = listOf("com.intellij.platform.workspace.storage.EntitySource"))
    addMetadata(typeMetadata)
    typeMetadata = EntityMetadata(fqName = "com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntity",
                                  entityDataFqName = "com.intellij.lang.javascript.flex.projectStructure.entities.impl.FlexBuildConfigurationSdksEntityData",
                                  supertypes = listOf("com.intellij.platform.workspace.storage.WorkspaceEntity"),
                                  properties = listOf(OwnPropertyMetadata(isComputable = false,
                                                                          isKey = false,
                                                                          isOpen = false,
                                                                          name = "entitySource",
                                                                          valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = false,
                                                                                                                              typeMetadata = FinalClassMetadata.KnownClass(
                                                                                                                                fqName = "com.intellij.platform.workspace.storage.EntitySource")),
                                                                          withDefault = false),
                                                      OwnPropertyMetadata(isComputable = false,
                                                                          isKey = false,
                                                                          isOpen = false,
                                                                          name = "sdks",
                                                                          valueType = ValueTypeMetadata.ParameterizedType(generics = listOf(
                                                                            ValueTypeMetadata.SimpleType.CustomType(isNullable = false,
                                                                                                                    typeMetadata = FinalClassMetadata.ClassMetadata(
                                                                                                                      fqName = "com.intellij.platform.workspace.jps.entities.SdkId",
                                                                                                                      properties = listOf(
                                                                                                                        OwnPropertyMetadata(
                                                                                                                          isComputable = false,
                                                                                                                          isKey = false,
                                                                                                                          isOpen = false,
                                                                                                                          name = "name",
                                                                                                                          valueType = primitiveTypeStringNotNullable,
                                                                                                                          withDefault = false),
                                                                                                                        OwnPropertyMetadata(
                                                                                                                          isComputable = false,
                                                                                                                          isKey = false,
                                                                                                                          isOpen = false,
                                                                                                                          name = "presentableName",
                                                                                                                          valueType = primitiveTypeStringNotNullable,
                                                                                                                          withDefault = false),
                                                                                                                        OwnPropertyMetadata(
                                                                                                                          isComputable = false,
                                                                                                                          isKey = false,
                                                                                                                          isOpen = false,
                                                                                                                          name = "type",
                                                                                                                          valueType = primitiveTypeStringNotNullable,
                                                                                                                          withDefault = false)),
                                                                                                                      supertypes = listOf("com.intellij.platform.workspace.storage.SymbolicEntityId")))),
                                                                                                                          primitive = primitiveTypeListNotNullable),
                                                                          withDefault = false)),
                                  extProperties = listOf(),
                                  isAbstract = false)
    addMetadata(typeMetadata)
  }

  override fun initializeMetadataHash() {
    addMetadataHash(typeFqn = "com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntity",
                    metadataHash = -693934496)
    addMetadataHash(typeFqn = "com.intellij.platform.workspace.jps.entities.SdkId", metadataHash = 206069110)
    addMetadataHash(typeFqn = "com.intellij.platform.workspace.storage.EntitySource", metadataHash = 372547987)
    addMetadataHash(typeFqn = "com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntitySource",
                    metadataHash = -1970976457)
  }
}
