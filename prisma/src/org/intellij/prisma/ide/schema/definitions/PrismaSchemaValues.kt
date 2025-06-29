// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.definitions

import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.builder.schema
import org.intellij.prisma.lang.PrismaConstants.PrimitiveTypes
import org.intellij.prisma.lang.resolve.gatherSchemaNames

val PRISMA_SCHEMA_VALUES = schema {
  deferred(PrismaSchemaKind.SCHEMA_NAME) { ctx ->
    ctx.metadata?.schemas?.forEach { schema ->
      element {
        label = schema
        type = PrimitiveTypes.STRING
        documentation = "The name of the schema."
        resolver = { listOfNotNull(gatherSchemaNames(it.element)[schema]) }
      }
    }
  }
}