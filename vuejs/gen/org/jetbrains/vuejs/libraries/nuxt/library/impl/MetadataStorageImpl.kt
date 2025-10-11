// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.library.impl

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
    val primitiveTypeListNotNullable = ValueTypeMetadata.SimpleType.PrimitiveType(isNullable = false, type = "List")

    var typeMetadata: StorageTypeMetadata

    typeMetadata = FinalClassMetadata.ObjectMetadata(fqName = "org.jetbrains.vuejs.libraries.nuxt.library.NuxtFolderEntity\$MyEntitySource",
                                                     properties = listOf(
                                                       OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false,
                                                                           name = "virtualFileUrl",
                                                                           valueType = ValueTypeMetadata.SimpleType.CustomType(
                                                                             isNullable = true,
                                                                             typeMetadata = FinalClassMetadata.KnownClass(
                                                                               fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                                                                           withDefault = false)),
                                                     supertypes = listOf("com.intellij.platform.workspace.storage.EntitySource"))

    addMetadata(typeMetadata)

    typeMetadata = EntityMetadata(fqName = "org.jetbrains.vuejs.libraries.nuxt.library.NuxtFolderEntity",
                                  entityDataFqName = "org.jetbrains.vuejs.libraries.nuxt.library.impl.NuxtFolderEntityData",
                                  supertypes = listOf("com.intellij.platform.workspace.storage.WorkspaceEntity"), properties = listOf(
        OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "entitySource",
                            valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = false,
                                                                                typeMetadata = FinalClassMetadata.KnownClass(
                                                                                  fqName = "com.intellij.platform.workspace.storage.EntitySource")),
                            withDefault = false),
        OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "nuxtFolderUrl",
                            valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = false,
                                                                                typeMetadata = FinalClassMetadata.KnownClass(
                                                                                  fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                            withDefault = false),
        OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "libraryFileUrls",
                            valueType = ValueTypeMetadata.ParameterizedType(generics = listOf(
                              ValueTypeMetadata.SimpleType.CustomType(isNullable = false, typeMetadata = FinalClassMetadata.KnownClass(
                                fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl"))),
                                                                            primitive = primitiveTypeListNotNullable),
                            withDefault = false)), extProperties = listOf(), isAbstract = false)

    addMetadata(typeMetadata)
  }

  override fun initializeMetadataHash() {
    addMetadataHash(typeFqn = "org.jetbrains.vuejs.libraries.nuxt.library.NuxtFolderEntity", metadataHash = 2072087004)
    addMetadataHash(typeFqn = "com.intellij.platform.workspace.storage.EntitySource", metadataHash = 1489809438)
    addMetadataHash(typeFqn = "org.jetbrains.vuejs.libraries.nuxt.library.NuxtFolderEntity\$MyEntitySource", metadataHash = 1571223598)
  }

}
