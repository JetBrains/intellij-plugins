// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.util.IncorrectOperationException

class ILLiteralExpressionManipulator : AbstractElementManipulator<ILLiteralExpression>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: ILLiteralExpression, range: TextRange, newContent: String): ILLiteralExpression {
    val replacement = StringBuilder(range.replace(element.text, newContent))
    if (replacement[0] != '"') replacement.insert(0, '"')
    if (replacement.last() != '"') replacement.append('"')
    (element.node.firstChildNode as LeafElement).replaceWithText(replacement.toString())
    return element
  }

  override fun getRangeInElement(element: ILLiteralExpression): TextRange {
    val text = element.text
    var start = 0
    var end = text.length
    if (text.startsWith('"')) start++
    if (text.endsWith('"')) end--
    return TextRange.create(start, end)
  }
}
