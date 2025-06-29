// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import org.intellij.prisma.ide.schema.PrismaSchemaKind

class PrismaSchemaElementGroup(val kind: PrismaSchemaKind, val elements: List<PrismaSchemaDeclaration>) {
  private val elementsByLabel = lazy(LazyThreadSafetyMode.PUBLICATION) {
    elements.associateBy { it.label }
  }

  fun getByLabel(label: String?): PrismaSchemaDeclaration? =
    label?.let { elementsByLabel.value[it] }

  class Builder(private val kind: PrismaSchemaKind) : SchemaDslBuilder<PrismaSchemaElementGroup> {
    private val elements: MutableList<PrismaSchemaDeclaration> = mutableListOf()

    fun element(block: PrismaSchemaDeclaration.Builder.() -> Unit) {
      val elementBuilder = PrismaSchemaDeclaration.Builder(kind)
      elementBuilder.block()
      val schemaElement = elementBuilder.build()
      elements.add(schemaElement)
    }

    fun compose(group: PrismaSchemaElementGroup) {
      require(kind == group.kind)
      elements.addAll(group.elements)
    }

    override fun build(): PrismaSchemaElementGroup {
      return PrismaSchemaElementGroup(kind, elements)
    }
  }
}