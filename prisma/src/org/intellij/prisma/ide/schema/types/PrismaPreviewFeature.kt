// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.types

import com.intellij.openapi.util.text.StringUtil
import org.intellij.prisma.ide.schema.types.PrismaDatasourceProviderType.POSTGRESQL

enum class PrismaPreviewFeature(val datasources: Set<PrismaDatasourceProviderType> = PrismaDatasourceProviderType.ALL) {
  DriverAdapters,
  NativeDistinct,
  PostgresqlExtensions,
  QueryCompiler,
  RelationJoins,
  StrictUndefinedChecks,
  Views,
  ShardKeys,
  FullTextSearchPostgres(setOf(POSTGRESQL));

  val presentation: String = StringUtil.decapitalize(name)
}