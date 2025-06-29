// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType

interface PrismaSchemaPatternCapability {
  val pattern: ElementPattern<out PsiElement>?

  fun isAcceptedByPattern(element: PsiElement?, processingContext: ProcessingContext?): Boolean =
    pattern?.accepts(element, processingContext ?: ProcessingContext()) ?: true
}

interface PrismaSchemaRefCapability {
  val ref: PrismaSchemaRef?
}

interface PrismaSchemaVariantsCapability {
  val variants: List<PrismaSchemaVariant>
}

interface PrismaSchemaDatasourcesCapability {
  val datasources: Set<PrismaDatasourceProviderType>?

  fun isAvailableForDatasources(used: Set<PrismaDatasourceProviderType>): Boolean =
    // filter only when a datasource provider is specified
    datasources.let { expected ->
      expected == null || used.isEmpty() || expected.intersect(used).isNotEmpty()
    }
}

interface PrismaSchemaDeclarationResolverCapability {
  val resolver: PrismaSchemaResolver?
}