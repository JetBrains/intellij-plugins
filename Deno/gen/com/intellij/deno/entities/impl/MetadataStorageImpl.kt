package com.intellij.deno.entities.impl

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

    var typeMetadata: StorageTypeMetadata

    typeMetadata = FinalClassMetadata.ObjectMetadata(fqName = "com.intellij.deno.entities.DenoEntitySource",
                                                     properties = listOf(OwnPropertyMetadata(isComputable = false,
                                                                                             isKey = false,
                                                                                             isOpen = false,
                                                                                             name = "virtualFileUrl",
                                                                                             valueType = ValueTypeMetadata.SimpleType.CustomType(
                                                                                               isNullable = true,
                                                                                               typeMetadata = FinalClassMetadata.KnownClass(
                                                                                                 fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                                                                                             withDefault = false)),
                                                     supertypes = listOf("com.intellij.platform.workspace.storage.EntitySource"))

    addMetadata(typeMetadata)

    typeMetadata = EntityMetadata(fqName = "com.intellij.deno.entities.DenoEntity",
                                  entityDataFqName = "com.intellij.deno.entities.impl.DenoEntityData",
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
                                                                          name = "depsFile",
                                                                          valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = true,
                                                                                                                              typeMetadata = FinalClassMetadata.KnownClass(
                                                                                                                                fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                                                                          withDefault = false),
                                                      OwnPropertyMetadata(isComputable = false,
                                                                          isKey = false,
                                                                          isOpen = false,
                                                                          name = "denoTypes",
                                                                          valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = true,
                                                                                                                              typeMetadata = FinalClassMetadata.KnownClass(
                                                                                                                                fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                                                                          withDefault = false)),
                                  extProperties = listOf(),
                                  isAbstract = false)

    addMetadata(typeMetadata)
  }

  override fun initializeMetadataHash() {
    addMetadataHash(typeFqn = "com.intellij.deno.entities.DenoEntity", metadataHash = 2083686760)
    addMetadataHash(typeFqn = "com.intellij.platform.workspace.storage.EntitySource", metadataHash = 332274491)
    addMetadataHash(typeFqn = "com.intellij.deno.entities.DenoEntitySource", metadataHash = 718852721)
  }
}
