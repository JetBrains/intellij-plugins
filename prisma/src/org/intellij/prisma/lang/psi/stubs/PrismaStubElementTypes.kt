// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.impl.*

const val EXTERNAL_PREFIX_ID = "PRISMA:"

interface PrismaStubElementTypes {
  companion object {
    @JvmField
    val TYPE_ALIAS = object : PrismaNamedStubElementType<PrismaTypeAlias>("TYPE_ALIAS") {
      override fun createPsi(stub: PrismaNamedStub<PrismaTypeAlias>): PrismaTypeAlias =
        PrismaTypeAliasImpl(stub, this)
    }

    @JvmField
    val ENUM_DECLARATION = object : PrismaNamedStubElementType<PrismaEnumDeclaration>("ENUM_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaEnumDeclaration>): PrismaEnumDeclaration =
        PrismaEnumDeclarationImpl(stub, this)
    }

    @JvmField
    val TYPE_DECLARATION = object : PrismaNamedStubElementType<PrismaTypeDeclaration>("TYPE_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaTypeDeclaration>): PrismaTypeDeclaration =
        PrismaTypeDeclarationImpl(stub, this)
    }

    @JvmField
    val VIEW_DECLARATION = object : PrismaNamedStubElementType<PrismaViewDeclaration>("VIEW_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaViewDeclaration>): PrismaViewDeclaration =
        PrismaViewDeclarationImpl(stub, this)
    }

    @JvmField
    val MODEL_DECLARATION = object : PrismaNamedStubElementType<PrismaModelDeclaration>("MODEL_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaModelDeclaration>): PrismaModelDeclaration =
        PrismaModelDeclarationImpl(stub, this)
    }

    @JvmField
    val GENERATOR_DECLARATION = object : PrismaNamedStubElementType<PrismaGeneratorDeclaration>("GENERATOR_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaGeneratorDeclaration>): PrismaGeneratorDeclaration =
        PrismaGeneratorDeclarationImpl(stub, this)
    }

    @JvmField
    val DATASOURCE_DECLARATION = object : PrismaNamedStubElementType<PrismaDatasourceDeclaration>("DATASOURCE_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaDatasourceDeclaration>): PrismaDatasourceDeclaration =
        PrismaDatasourceDeclarationImpl(stub, this)
    }

    @JvmField
    val ENUM_VALUE_DECLARATION = object : PrismaNamedStubElementType<PrismaEnumValueDeclaration>("ENUM_VALUE_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaEnumValueDeclaration>): PrismaEnumValueDeclaration =
        PrismaEnumValueDeclarationImpl(stub, this)
    }

    @JvmField
    val KEY_VALUE = object : PrismaNamedStubElementType<PrismaKeyValue>("KEY_VALUE") {
      override fun createPsi(stub: PrismaNamedStub<PrismaKeyValue>): PrismaKeyValue =
        PrismaKeyValueImpl(stub, this)
    }

    @JvmField
    val FIELD_DECLARATION = object : PrismaNamedStubElementType<PrismaFieldDeclaration>("FIELD_DECLARATION") {
      override fun createPsi(stub: PrismaNamedStub<PrismaFieldDeclaration>): PrismaFieldDeclaration =
        PrismaFieldDeclarationImpl(stub, this)
    }
  }
}