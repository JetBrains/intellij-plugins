// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.findUsages.search

import com.intellij.find.usages.api.PsiUsage
import com.intellij.model.Pointer
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.model.search.LeafOccurrence
import com.intellij.model.search.LeafOccurrenceMapper
import com.intellij.model.search.SearchContext
import com.intellij.model.search.SearchService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.startOffset
import com.intellij.util.Query
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression
import org.intellij.prisma.lang.symbols.PrismaSchemaSymbol
import org.intellij.prisma.lang.symbols.declarations.PrismaSchemaSymbolDeclarationProvider

class PrismaSchemaUsage(override val file: PsiFile, override val range: TextRange, override val declaration: Boolean) : PsiUsage {
  override fun createPointer(): Pointer<out PsiUsage> {
    val declaration = declaration
    return Pointer.fileRangePointer(file, range) { file, range ->
      PrismaSchemaUsage(file, range, declaration)
    }
  }
}

internal fun buildSchemaUsagesQuery(
  project: Project,
  target: PrismaSchemaSymbol,
  searchScope: SearchScope,
): Query<out PsiUsage> =
  SearchService.getInstance()
    .searchWord(project, target.targetName)
    .inScope(searchScope.intersectWith(target.maximalSearchScope))
    .inContexts(SearchContext.inCodeHosts())
    .inFilesWithLanguage(PrismaLanguage)
    .buildQuery(LeafOccurrenceMapper.withPointer(target.createPointer(), ::findSchemaUsages))

private fun findSchemaUsages(target: PrismaSchemaSymbol, occurrence: LeafOccurrence): Collection<PrismaSchemaUsage> {
  val literalExpr = occurrence.start.parent as? PrismaStringLiteralExpression ?: return emptyList()

  val declarations = PrismaSchemaSymbolDeclarationProvider().getDeclarations(literalExpr, occurrence.offsetInStart)
    .filter { it.symbol == target }
    .map { PrismaSchemaUsage(it.declaringElement.containingFile, it.absoluteRange, true) }
  if (declarations.isNotEmpty()) {
    return declarations
  }

  return PsiSymbolReferenceService.getService().getReferences(literalExpr)
    .filter { occurrence.offsetInStart in it.rangeInElement }
    .filter { it.resolvesTo(target) }
    .map {
      PrismaSchemaUsage(
        literalExpr.containingFile,
        it.rangeInElement.shiftRight(it.element.startOffset),
        false
      )
    }
}