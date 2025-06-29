// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.symbols.declarations

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolDeclaration
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement

class PrismaSchemaSymbolDeclaration(private val element: PsiElement, private val symbol: Symbol) : PsiSymbolDeclaration {
  override fun getDeclaringElement(): PsiElement = element
  override fun getRangeInDeclaringElement(): TextRange = ElementManipulators.getValueTextRange(element)
  override fun getSymbol(): Symbol = symbol
}