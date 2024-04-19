// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

