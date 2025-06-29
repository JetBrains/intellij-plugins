// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.symbols.declarations

import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import org.intellij.prisma.ide.schema.PrismaSchemaKind
import org.intellij.prisma.ide.schema.PrismaSchemaProvider
import org.intellij.prisma.lang.psi.PrismaPsiPatterns
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression
import org.intellij.prisma.lang.symbols.PrismaSchemaSymbol
import org.jetbrains.annotations.Unmodifiable

class PrismaSchemaSymbolDeclarationProvider : PsiSymbolDeclarationProvider {
  override fun getDeclarations(element: PsiElement, offsetInElement: Int): @Unmodifiable Collection<PsiSymbolDeclaration> {
    if (element is PrismaStringLiteralExpression && PrismaPsiPatterns.prismaSchemaDeclaration.accepts(element)) {
      val schemaElement = PrismaSchemaProvider.getEvaluatedSchema(element)
                            .getElement(PrismaSchemaKind.SCHEMA_NAME, ElementManipulators.getValueText(element))
                          ?: return emptyList()
      return listOf(PrismaSchemaSymbolDeclaration(element, PrismaSchemaSymbol(element, schemaElement)))
    }

    return emptyList()
  }
}
