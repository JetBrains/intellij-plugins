/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.intellij.terraform.hcl.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.terraform.hcl.psi.HCLHeredocContent
import org.intellij.terraform.hcl.psi.HCLHeredocContentManipulator

abstract class HCLHeredocContentMixin(node: ASTNode) : HCLValueWithReferencesMixin(node), PsiLanguageInjectionHost, HCLHeredocContent {
  override fun isValidHost() = true

  override fun updateText(text: String): PsiLanguageInjectionHost {
    return HCLHeredocContentManipulator().handleContentChange(this, text) as PsiLanguageInjectionHost
  }

  override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
    return object : LiteralTextEscaper<HCLHeredocContentMixin>(this) {
      override fun isOneLine(): Boolean {
        return false
      }

      override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        outChars.append(rangeInsideHost.subSequence(myHost.text))
        return true
      }

      override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val offset = offsetInDecoded + rangeInsideHost.startOffset
        if (offset < rangeInsideHost.startOffset) {
          return -1
        }
        if (offset > rangeInsideHost.endOffset) {
          return -1
        }
        return offset
      }

      override fun getRelevantTextRange(): TextRange {
        return myHost.getInnerRange()
      }

    }
  }

  fun getInnerRange(): TextRange {
    if (this.textLength == 0) return TextRange.EMPTY_RANGE
    // Everything except last EOL
    val lastChild = this.lastChild
    if (lastChild != null) {
      return TextRange.create(0, lastChild.startOffsetInParent)
    }
    return TextRange.from(0, this.textLength)
  }
}
