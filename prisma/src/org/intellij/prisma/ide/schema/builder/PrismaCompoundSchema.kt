// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import org.intellij.prisma.ide.schema.PrismaSchemaKind

class PrismaCompoundSchema(
  private val groups: List<PrismaSchemaElementGroup>,
  private val deferred: List<PrismaSchemaDeferredElementGroup>,
) {
  fun evaluate(evaluationContext: PrismaSchemaEvaluationContext): PrismaEvaluatedSchema {
    val newGroups = mutableMapOf<PrismaSchemaKind, PrismaSchemaElementGroup.Builder>()

    deferred.asSequence()
      .map { it.invoke(evaluationContext) }
      .plus(groups)
      .forEach { group ->
        newGroups
          .computeIfAbsent(group.kind) { PrismaSchemaElementGroup.Builder(group.kind) }
          .compose(group)
      }

    return PrismaEvaluatedSchema(newGroups.mapValues { it.value.build() })
  }

  class Builder : SchemaDslBuilder<PrismaCompoundSchema> {
    private val groups: MutableList<PrismaSchemaElementGroup> = mutableListOf()
    private val deferred: MutableList<PrismaSchemaDeferredElementGroup> = mutableListOf()

    fun group(kind: PrismaSchemaKind, block: PrismaSchemaElementGroup.Builder.() -> Unit) {
      val groupBuilder = PrismaSchemaElementGroup.Builder(kind)
      groupBuilder.block()
      val group = groupBuilder.build()
      groups.add(group)
    }

    fun deferred(kind: PrismaSchemaKind, block: PrismaSchemaElementGroup.Builder.(PrismaSchemaEvaluationContext) -> Unit) {
      deferred.add { ctx ->
        val builder = PrismaSchemaElementGroup.Builder(kind)
        builder.block(ctx)
        builder.build()
      }
    }

    fun compose(schema: PrismaCompoundSchema) {
      groups.addAll(schema.groups)
      deferred.addAll(schema.deferred)
    }

    override fun build(): PrismaCompoundSchema {
      return PrismaCompoundSchema(groups, deferred)
    }
  }
}

typealias PrismaSchemaDeferredElementGroup = (PrismaSchemaEvaluationContext) -> PrismaSchemaElementGroup