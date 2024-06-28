// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.intellij.prisma.ide.schema.types.PrismaDatasourceType
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.psi.PrismaDatasourceDeclaration
import org.intellij.prisma.lang.psi.PrismaKeyValue
import org.intellij.prisma.lang.psi.PrismaLiteralExpression
import java.util.concurrent.ConcurrentHashMap

data class PrismaSchemaMetadata(val datasources: Map<String, PrismaSchemaDatasource> = emptyMap()) {
  val datasourceTypes: Set<PrismaDatasourceType> = datasources.values.map { it.type }.toSet()
}

data class PrismaSchemaDatasource(val name: String, val type: PrismaDatasourceType)

fun resolveSchemaMetadata(context: PsiElement): PrismaSchemaMetadata {
  val cache = CachedValuesManager.getManager(context.project).getCachedValue(context.project) {
    create(ConcurrentHashMap<GlobalSearchScope, PrismaSchemaMetadata>(), PsiModificationTracker.MODIFICATION_COUNT)
  }
  val scope = getSchemaScope(context)
  cache[scope]?.let { return it }

  val metadata = buildMetadata(context, scope)
  return cache.getOrPut(scope) { metadata }
}

private fun buildMetadata(
  context: PsiElement,
  scope: GlobalSearchScope,
): PrismaSchemaMetadata {
  val processor = PrismaProcessor()
  processKeyValueDeclarations(context.project, processor, scope)

  val datasources = mutableListOf<PrismaSchemaDatasource>()

  for (declaration in processor.getResults()) {
    if (declaration is PrismaDatasourceDeclaration) {
      val datasourceName = declaration.name
      val provider = declaration.findMemberByName(PrismaConstants.DatasourceFields.PROVIDER) as? PrismaKeyValue
      val providerValue = (provider?.expression as? PrismaLiteralExpression)?.value as? String
      val datasourceType = PrismaDatasourceType.fromString(providerValue)
      if (datasourceName != null && datasourceType != null) {
        datasources.add(PrismaSchemaDatasource(datasourceName, datasourceType))
      }
    }
  }

  return PrismaSchemaMetadata(datasources.associateBy { it.name })
}