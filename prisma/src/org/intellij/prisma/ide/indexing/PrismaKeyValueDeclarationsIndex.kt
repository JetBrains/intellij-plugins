// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.indexing

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.intellij.prisma.lang.psi.PrismaKeyValueDeclaration

class PrismaKeyValueDeclarationsIndex : StringStubIndexExtension<PrismaKeyValueDeclaration>() {
  override fun getKey(): StubIndexKey<String, PrismaKeyValueDeclaration> = PRISMA_KEY_VALUE_DECL_INDEX_KEY
}

val PRISMA_KEY_VALUE_DECL_INDEX_KEY = StubIndexKey.createIndexKey<String, PrismaKeyValueDeclaration>("prisma.key.value.declarations")
