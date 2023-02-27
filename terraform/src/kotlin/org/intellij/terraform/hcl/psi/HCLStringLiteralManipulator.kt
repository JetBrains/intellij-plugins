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
