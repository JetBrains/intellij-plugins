// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType

fun PrismaSchemaElement.isAvailableForDatasources(datasourceTypes: Set<PrismaDatasourceProviderType>): Boolean =
  if (this is PrismaSchemaDatasourcesCapability) isAvailableForDatasources(datasourceTypes) else true

fun PrismaSchemaElement.isAcceptedByPattern(element: PsiElement, processingContext: ProcessingContext): Boolean =
  if (this is PrismaSchemaPatternCapability) isAcceptedByPattern(element, processingContext) else true

fun PrismaSchemaElement.resolveDeclaration(context: PsiElement): PsiElement? {
  return if (this is PrismaSchemaDeclarationResolverCapability)
    resolver?.invoke(createSchemaResolveContext(context))?.firstOrNull()
  else null
}