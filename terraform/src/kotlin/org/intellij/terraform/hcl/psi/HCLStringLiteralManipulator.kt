// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.psi.impl.HCLQuoter
import org.intellij.terraform.hcl.psi.impl.HCLStringLiteralMixin

class HCLStringLiteralManipulator : AbstractElementManipulator<HCLStringLiteralMixin>() {
  @Throws(IncorrectOperationException::class)
  override fun handleContentChange(element: HCLStringLiteralMixin, range: TextRange, newContent: String): HCLStringLiteralMixin {
    val escaped = HCLQuoter.escape(newContent, element.isInHCLFileWithInterpolations())
    val replacement = range.replace(element.text, escaped)
    return element.updateText(replacement)
  }

  override fun getRangeInElement(element: HCLStringLiteralMixin): TextRange {
    if (element.textLength == 0) return TextRange.EMPTY_RANGE
    else if (element.textLength == 1) return TextRange(0, element.textLength)
    return TextRange(1, element.textLength - 1)
  }
}
