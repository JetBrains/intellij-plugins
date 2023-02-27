/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
package org.intellij.terraform.hcl.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.util.IncorrectOperationException

class HCLNumberManipulator : AbstractElementManipulator<HCLNumberLiteral>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: HCLNumberLiteral, range: TextRange, newContent: String): HCLNumberLiteral {
    val replacement = range.replace(element.text, newContent)
    val node = element.firstChild.node
    assert(node is LeafElement)
    (node as LeafElement).replaceWithText(replacement)
    return element
  }
}

