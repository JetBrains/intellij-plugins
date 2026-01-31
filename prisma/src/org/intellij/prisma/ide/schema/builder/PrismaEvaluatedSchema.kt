// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.intellij.prisma.ide.schema.PrismaSchemaDeclarationPath
import org.intellij.prisma.ide.schema.PrismaSchemaDefaultParameterPath
import org.intellij.prisma.ide.schema.PrismaSchemaFakeElementPath
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaParameterPath
import org.intellij.prisma.ide.schema.PrismaSchemaPath
import org.intellij.prisma.ide.schema.PrismaSchemaVariantPath

class PrismaEvaluatedSchema(private val groups: Map<PrismaSchemaKind, PrismaSchemaElementGroup>) {
  fun getElement(kind: PrismaSchemaKind, label: String?): PrismaSchemaDeclaration? =
    groups[kind]?.getByLabel(label)

  fun getElements(kind: PrismaSchemaKind): Collection<PrismaSchemaDeclaration> =
    groups[kind]?.elements ?: emptyList()

  fun getElements(ref: PrismaSchemaRef?): Collection<PrismaSchemaDeclaration> =
    when {
      ref == null -> emptyList()
      ref.label == null -> getElements(ref.kind)
      else -> listOfNotNull(getElement(ref.kind, ref.label))
    }

  fun match(element: PsiElement?): PrismaSchemaElement? =
    match(PrismaSchemaPath.forElement(element))

  fun match(path: PrismaSchemaPath?): PrismaSchemaElement? = when (path) {
    is PrismaSchemaFakeElementPath -> path.element.schemaElement

    is PrismaSchemaDeclarationPath -> getElement(path.kind, path.label)

    is PrismaSchemaParameterPath ->
      match(path.parent).asSafely<PrismaSchemaDeclaration>()
        ?.params
        ?.let { parameters ->
          when (path) {
            is PrismaSchemaDefaultParameterPath -> parameters.firstOrNull()
            else -> parameters.find { it.label == path.label }
          }
        }

    is PrismaSchemaVariantPath ->
      match(path.parent).asSafely<PrismaSchemaVariantsCapability>()
        ?.variants
        ?.let { substituteRefs(it) }
        ?.find { it.label == path.label }

    null -> null
  }

  fun substituteRefs(elements: List<PrismaSchemaElement>): List<PrismaSchemaElement> = elements.flatMap {
    if (it is PrismaSchemaRefCapability && it.ref != null) getElements(it.ref) else listOf(it)
  }
}

