// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.impl.*
import org.intellij.prisma.lang.psi.stubs.impl.*

const val EXTERNAL_PREFIX_ID = "PRISMA:"

interface PrismaStubElementTypes {
  companion object {
    @JvmField
    val TYPE_ALIAS = object : PrismaNamedStubElementType<PrismaTypeAliasStub, PrismaTypeAlias>(
      "TYPE_ALIAS", ::PrismaTypeAliasImpl, ::PrismaTypeAliasStubImpl
    ) {}

    @JvmField
    val ENUM_DECLARATION = object : PrismaNamedStubElementType<PrismaEnumDeclarationStub, PrismaEnumDeclaration>(
      "ENUM_DECLARATION", ::PrismaEnumDeclarationImpl, ::PrismaEnumDeclarationStubImpl
    ) {}

    @JvmField
    val TYPE_DECLARATION = object : PrismaNamedStubElementType<PrismaTypeDeclarationStub, PrismaTypeDeclaration>(
      "TYPE_DECLARATION", ::PrismaTypeDeclarationImpl, ::PrismaTypeDeclarationStubImpl
    ) {}

    @JvmField
    val VIEW_DECLARATION = object : PrismaNamedStubElementType<PrismaViewDeclarationStub, PrismaViewDeclaration>(
      "VIEW_DECLARATION", ::PrismaViewDeclarationImpl, ::PrismaViewDeclarationStubImpl
    ) {}

    @JvmField
    val MODEL_DECLARATION = object : PrismaNamedStubElementType<PrismaModelDeclarationStub, PrismaModelDeclaration>(
      "MODEL_DECLARATION", ::PrismaModelDeclarationImpl, ::PrismaModelDeclarationStubImpl
    ) {}

    @JvmField
    val GENERATOR_DECLARATION = object : PrismaNamedStubElementType<PrismaGeneratorDeclarationStub, PrismaGeneratorDeclaration>(
      "GENERATOR_DECLARATION", ::PrismaGeneratorDeclarationImpl, ::PrismaGeneratorDeclarationStubImpl
    ) {}

    @JvmField
    val DATASOURCE_DECLARATION = object : PrismaNamedStubElementType<PrismaDatasourceDeclarationStub, PrismaDatasourceDeclaration>(
      "DATASOURCE_DECLARATION", ::PrismaDatasourceDeclarationImpl, ::PrismaDatasourceDeclarationStubImpl
    ) {}

    @JvmField
    val ENUM_VALUE_DECLARATION = object : PrismaNamedStubElementType<PrismaEnumValueDeclarationStub, PrismaEnumValueDeclaration>(
      "ENUM_VALUE_DECLARATION", ::PrismaEnumValueDeclarationImpl, ::PrismaEnumValueDeclarationStubImpl
    ) {}

    @JvmField
    val KEY_VALUE = object : PrismaNamedStubElementType<PrismaKeyValueStub, PrismaKeyValue>(
      "KEY_VALUE", ::PrismaKeyValueImpl, ::PrismaKeyValueStubImpl
    ) {}

    @JvmField
    val FIELD_DECLARATION = object : PrismaNamedStubElementType<PrismaFieldDeclarationStub, PrismaFieldDeclaration>(
      "FIELD_DECLARATION", ::PrismaFieldDeclarationImpl, ::PrismaFieldDeclarationStubImpl
    ) {}
  }
}