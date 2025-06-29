// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.impl.manipulators

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulator
import org.intellij.prisma.lang.psi.PrismaElementFactory
import org.intellij.prisma.lang.psi.PrismaStringLiteralExpression


class PrismaStringLiteralExpressionManipulator : ElementManipulator<PrismaStringLiteralExpression> {
  override fun handleContentChange(element: PrismaStringLiteralExpression, range: TextRange, newContent: String?): PrismaStringLiteralExpression? {
    val newElement = PrismaElementFactory.createStringLiteralExpression(element.project, newContent ?: "")
    return element.replace(newElement) as? PrismaStringLiteralExpression
  }

  override fun handleContentChange(element: PrismaStringLiteralExpression, newContent: String?): PrismaStringLiteralExpression? {
    return handleContentChange(element, getRangeInElement(element), newContent)
  }

  override fun getRangeInElement(element: PrismaStringLiteralExpression): TextRange {
    return when {
      element.textLength >= 2 -> TextRange.create(1, element.textLength - 1)
      else -> TextRange.from(0, element.textLength)
    }
  }
}
