// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema

val PrismaSchemaPath.parents: Sequence<PrismaSchemaPath>
  get() = sequence {
    var current: PrismaSchemaPath? = this@parents
    while (current != null) {
      yield(current)

      current = when (current) {
        is PrismaSchemaParameterPath -> current.parent
        is PrismaSchemaVariantPath -> current.parent
        else -> null
      }
    }
  }