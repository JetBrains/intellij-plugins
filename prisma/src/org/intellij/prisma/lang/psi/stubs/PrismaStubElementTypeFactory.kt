// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import com.intellij.psi.tree.IElementType

object PrismaStubElementTypeFactory {
  private val TYPES = listOf(
    PrismaStubElementTypes.TYPE_ALIAS,
    PrismaStubElementTypes.ENUM_DECLARATION,
    PrismaStubElementTypes.TYPE_DECLARATION,
    PrismaStubElementTypes.VIEW_DECLARATION,
    PrismaStubElementTypes.MODEL_DECLARATION,
    PrismaStubElementTypes.GENERATOR_DECLARATION,
    PrismaStubElementTypes.DATASOURCE_DECLARATION,
    PrismaStubElementTypes.ENUM_VALUE_DECLARATION,
    PrismaStubElementTypes.KEY_VALUE,
    PrismaStubElementTypes.FIELD_DECLARATION,
  ).associateBy { it.debugName }

  @JvmStatic
  fun create(name: String): IElementType = TYPES[name] ?: throw IllegalArgumentException("Unknown name")
}