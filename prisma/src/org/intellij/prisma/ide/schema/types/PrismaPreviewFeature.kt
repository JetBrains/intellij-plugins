// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.types

import com.intellij.openapi.util.text.StringUtil

enum class PrismaPreviewFeature(val datasources: Set<PrismaDatasourceType> = PrismaDatasourceType.ALL) {
  DriverAdapters,
  Metrics,
  MultiSchema,
  NativeDistinct,
  PostgresqlExtensions,
  QueryCompiler,
  RelationJoins,
  StrictUndefinedChecks,
  Views,
  FullTextSearchPostgres(setOf(PrismaDatasourceType.POSTGRESQL));

  val presentation: String = StringUtil.decapitalize(name)
}