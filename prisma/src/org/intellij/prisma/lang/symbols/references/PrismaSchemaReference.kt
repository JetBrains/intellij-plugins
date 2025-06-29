// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.symbols.references

import com.intellij.model.SingleTargetReference
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.intellij.prisma.ide.schema.builder.PrismaSchemaElement
import org.intellij.prisma.ide.schema.builder.resolveDeclaration
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression
import org.intellij.prisma.lang.symbols.PrismaSchemaSymbol

class PrismaSchemaSymbolReference(private val element: PsiElement, val schemaElement: PrismaSchemaElement) : PsiSymbolReference, SingleTargetReference() {
  override fun getElement(): PsiElement = element

  override fun getRangeInElement(): TextRange = ElementManipulators.getValueTextRange(element)

  override fun resolveSingleTarget(): Symbol? =
    schemaElement.resolveDeclaration(element)
      .asSafely<PrismaStringLiteralExpression>()
      ?.let { PrismaSchemaSymbol(it, schemaElement) }
}