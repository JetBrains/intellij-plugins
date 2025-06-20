// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.psi.PrismaDatasourceDeclaration
import org.intellij.prisma.lang.psi.PrismaGeneratorDeclaration
import org.intellij.prisma.lang.psi.PrismaKeyValue
import org.intellij.prisma.lang.psi.PrismaLiteralExpression
import java.util.concurrent.ConcurrentHashMap

data class PrismaSchemaMetadata(
  val datasources: Map<String, PrismaDatasourceMetadata>,
  val generators: Map<String, PrismaGeneratorMetadata>,
) {
  val datasourceTypes: Set<PrismaDatasourceProviderType> = datasources.values.mapNotNullTo(mutableSetOf()) { it.providerType }
  val generatorProviderTypes: Set<String> = generators.values.mapNotNullTo(mutableSetOf()) { it.providerType }
}

data class PrismaDatasourceMetadata(val name: String, val providerType: PrismaDatasourceProviderType?)
data class PrismaGeneratorMetadata(val name: String, val providerType: String?)

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

  val datasources = mutableListOf<PrismaDatasourceMetadata>()
  val generators = mutableListOf<PrismaGeneratorMetadata>()

  for (declaration in processor.getResults()) {
    when (declaration) {
      is PrismaDatasourceDeclaration -> {
        val datasourceName = declaration.name
        val provider = declaration.findMemberByName(PrismaConstants.DatasourceFields.PROVIDER) as? PrismaKeyValue
        val providerValue = (provider?.expression as? PrismaLiteralExpression)?.value as? String
        val datasourceType = PrismaDatasourceProviderType.fromString(providerValue)
        if (datasourceName != null && datasourceType != null) {
          datasources.add(PrismaDatasourceMetadata(datasourceName, datasourceType))
        }
      }
      is PrismaGeneratorDeclaration -> {
        val generatorName = declaration.name
        val provider = declaration.findMemberByName(PrismaConstants.GeneratorFields.PROVIDER) as? PrismaKeyValue
        val providerType = (provider?.expression as? PrismaLiteralExpression)?.value as? String
        if (generatorName != null) {
          generators.add(PrismaGeneratorMetadata(generatorName, providerType))
        }
      }
    }
  }

  return PrismaSchemaMetadata(datasources.associateBy { it.name }, generators.associateBy { it.name })
}