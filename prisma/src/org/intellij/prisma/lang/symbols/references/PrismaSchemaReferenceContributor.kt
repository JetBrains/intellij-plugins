// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.symbols.references

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceProvider
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression
import org.jetbrains.annotations.Unmodifiable

class PrismaSchemaSymbolReferenceProvider : PsiSymbolReferenceProvider {
  override fun getReferences(element: PsiExternalReferenceHost, hints: PsiSymbolReferenceHints): @Unmodifiable Collection<PsiSymbolReference> {
    if (element is PrismaStringLiteralExpression) {
      val schemaElement = PrismaSchemaProvider.getEvaluatedSchema(element).match(element) ?: return emptyList()
      return listOf(PrismaSchemaSymbolReference(element, schemaElement))
    }

    return emptyList()
  }

  override fun getSearchRequests(project: Project, target: Symbol): @Unmodifiable Collection<SearchRequest> {
    return emptyList()
  }
}