// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.psi.impl.HCLIdentifierMixin
import org.intellij.terraform.hcl.psi.impl.HCLVariableMixin

class HCLVariableManipulator : AbstractElementManipulator<HCLVariableMixin>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: HCLVariableMixin, range: TextRange, newContent: String): HCLVariableMixin {
    val replacement = range.replace(element.text, newContent)
    return element.setName(replacement) as HCLVariableMixin
  }
}
