// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.psi.impl.HCLIdentifierMixin

class HCLIdentifierManipulator : AbstractElementManipulator<HCLIdentifierMixin>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: HCLIdentifierMixin, range: TextRange, newContent: String): HCLIdentifierMixin {
    val replacement = range.replace(element.text, newContent)
    element.setName(replacement)
    return element
  }
}
