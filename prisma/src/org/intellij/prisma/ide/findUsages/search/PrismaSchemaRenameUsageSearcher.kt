// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.findUsages.search

import com.intellij.refactoring.rename.api.PsiModifiableRenameUsage
import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.util.Query
import org.intellij.prisma.lang.symbols.PrismaSchemaSymbol

class PrismaSchemaRenameUsageSearcher : RenameUsageSearcher {
  override fun collectSearchRequest(parameters: RenameUsageSearchParameters): Query<out RenameUsage>? {
    val (project, target, searchScope) = parameters
    return if (target is PrismaSchemaSymbol)
      buildSchemaUsagesQuery(project, target, searchScope)
        .mapping { PsiModifiableRenameUsage.defaultPsiModifiableRenameUsage(it) }
    else null
  }
}
