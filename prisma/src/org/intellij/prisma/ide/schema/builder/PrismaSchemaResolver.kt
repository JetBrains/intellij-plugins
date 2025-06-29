// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import com.intellij.psi.PsiElement

typealias PrismaSchemaResolver = (PrismaSchemaResolveContext) -> Collection<PsiElement>

interface PrismaSchemaResolveContext {
  val element: PsiElement
}

private data class PrismaSchemaResolveContextImpl(override val element: PsiElement) : PrismaSchemaResolveContext

fun createSchemaResolveContext(element: PsiElement): PrismaSchemaResolveContext =
  PrismaSchemaResolveContextImpl(element)
