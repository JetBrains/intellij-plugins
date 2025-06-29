// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.findUsages.search

import com.intellij.find.usages.api.Usage
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.util.Query
import org.intellij.prisma.lang.symbols.PrismaSchemaSymbol

class PrismaSchemaUsageSearcher : UsageSearcher {
  override fun collectSearchRequest(parameters: UsageSearchParameters): Query<out Usage>? {
    val (project, target, searchScope) = parameters
    return if (target is PrismaSchemaSymbol) buildSchemaUsagesQuery(project, target, searchScope) else null
  }
}
