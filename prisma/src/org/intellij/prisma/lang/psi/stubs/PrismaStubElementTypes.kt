// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import org.intellij.prisma.lang.psi.PrismaDatasourceDeclaration
import org.intellij.prisma.lang.psi.PrismaEnumDeclaration
import org.intellij.prisma.lang.psi.PrismaEnumValueDeclaration
import org.intellij.prisma.lang.psi.PrismaFieldDeclaration
import org.intellij.prisma.lang.psi.PrismaGeneratorDeclaration
import org.intellij.prisma.lang.psi.PrismaKeyValue
import org.intellij.prisma.lang.psi.PrismaModelDeclaration
import org.intellij.prisma.lang.psi.PrismaTypeAlias
import org.intellij.prisma.lang.psi.PrismaTypeDeclaration
import org.intellij.prisma.lang.psi.PrismaViewDeclaration
import org.intellij.prisma.lang.psi.impl.PrismaDatasourceDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaEnumDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaEnumValueDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaFieldDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaGeneratorDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaKeyValueImpl
import org.intellij.prisma.lang.psi.impl.PrismaModelDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaTypeAliasImpl
import org.intellij.prisma.lang.psi.impl.PrismaTypeDeclarationImpl
import org.intellij.prisma.lang.psi.impl.PrismaViewDeclarationImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaDatasourceDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaEnumDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaEnumValueDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaFieldDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaGeneratorDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaKeyValueStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaModelDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaTypeAliasStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaTypeDeclarationStubImpl
import org.intellij.prisma.lang.psi.stubs.impl.PrismaViewDeclarationStubImpl

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