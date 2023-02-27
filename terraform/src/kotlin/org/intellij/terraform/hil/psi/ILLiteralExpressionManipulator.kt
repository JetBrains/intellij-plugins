/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
