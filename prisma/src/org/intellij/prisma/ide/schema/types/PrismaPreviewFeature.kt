// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.types

import com.intellij.openapi.util.text.StringUtil

enum class PrismaPreviewFeature {
  Deno,
  DriverAdapters,
  FullTextIndex,
  FullTextSearch,
  Metrics,
  MultiSchema,
  NativeDistinct,
  PostgresqlExtensions,
  Tracing,
  Views,
  RelationJoins,
  OmitApi,
  PrismaSchemaFolder;

  val presentation: String = StringUtil.decapitalize(name)
}