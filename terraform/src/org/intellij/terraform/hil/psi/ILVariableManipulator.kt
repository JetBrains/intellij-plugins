// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException

class ILVariableManipulator : AbstractElementManipulator<ILVariable>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: ILVariable, range: TextRange, newContent: String): ILVariable {
    val replacement = range.replace(element.text, newContent)
    return element.setName(replacement) as ILVariable
  }

}
