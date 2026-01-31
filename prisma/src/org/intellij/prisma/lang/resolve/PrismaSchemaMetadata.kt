// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.resolve

import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.asSafely
import com.intellij.util.text.nullize
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.lang.psi.PrismaArrayExpression
import org.intellij.prisma.lang.psi.PrismaDatasourceDeclaration
import org.intellij.prisma.lang.psi.PrismaGeneratorDeclaration
import org.intellij.prisma.lang.psi.PrismaKeyValue
import org.intellij.prisma.lang.psi.PrismaLiteralExpression

private val schemaMetadataCacheKey = createSchemaScopedCacheKey<PrismaSchemaMetadata>("schemaMetadata")

fun resolveSchemaMetadata(context: PsiElement): PrismaSchemaMetadata =
  computeWithSchemaScopedCache(context, schemaMetadataCacheKey, ::buildMetadata)

data class PrismaSchemaMetadata(
  val datasources: Map<String, PrismaDatasourceMetadata>,
  val generators: Map<String, PrismaGeneratorMetadata>,
) {
  val datasourceTypes: Set<PrismaDatasourceProviderType> = datasources.values.mapNotNullTo(mutableSetOf()) { it.providerType }
  val generatorProviderTypes: Set<String> = generators.values.mapNotNullTo(mutableSetOf()) { it.providerType }
  val schemas: Set<String> = datasources.values.flatMapTo(mutableSetOf()) { it.schemas }
}

data class PrismaDatasourceMetadata(val name: String, val providerType: PrismaDatasourceProviderType?, val schemas: Set<String>)
data class PrismaGeneratorMetadata(val name: String, val providerType: String?)

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

        val providerKeyValue = declaration.findMemberByName(PrismaConstants.DatasourceFields.PROVIDER) as? PrismaKeyValue
        val providerValue = (providerKeyValue?.expression as? PrismaLiteralExpression)?.value as? String
        val datasourceType = PrismaDatasourceProviderType.fromString(providerValue)

        val schemasKeyValue = declaration.findMemberByName(PrismaConstants.DatasourceFields.SCHEMAS) as? PrismaKeyValue
        val schemas = schemasKeyValue?.expression?.asSafely<PrismaArrayExpression>()?.expressionList
                        ?.mapNotNull {
                          it.asSafely<PrismaLiteralExpression>()?.value?.asSafely<String>()?.nullize(true)
                        }
                        ?.toSet()
                      ?: emptySet()

        if (datasourceName != null && datasourceType != null) {
          datasources.add(PrismaDatasourceMetadata(datasourceName, datasourceType, schemas))
        }
      }
      is PrismaGeneratorDeclaration -> {
        val generatorName = declaration.name
        val providerKeyValue = declaration.findMemberByName(PrismaConstants.GeneratorFields.PROVIDER) as? PrismaKeyValue
        val providerType = (providerKeyValue?.expression as? PrismaLiteralExpression)?.value as? String
        if (generatorName != null) {
          generators.add(PrismaGeneratorMetadata(generatorName, providerType))
        }
      }
    }
  }

  return PrismaSchemaMetadata(datasources.associateBy { it.name }, generators.associateBy { it.name })
}