// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.template

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementTypes
import java.util.LinkedList

class HilTemplatingAwarePsiBuilder(builder: PsiBuilder?,
                                   state_: GeneratedParserUtilBase.ErrorState?,
                                   parser_: PsiParser?) : GeneratedParserUtilBase.Builder(builder, state_, parser_) {
  val isTemplatingSupported: Boolean
    get() = parser is HILTemplateParser

  enum class ExpectedConstruction(val expectedTokenSet: TokenSet) {
    END_FOR(TokenSet.create(HILElementTypes.ENDFOR_KEYWORD)), END_IF(TokenSet.create(HILElementTypes.ENDIF_KEYWORD, HILElementTypes.ELSE_KEYWORD))
  }

  private val expectationStack = LinkedList<ExpectedConstruction>()

  fun expectForEnd() {
    expectationStack.push(ExpectedConstruction.END_FOR)
  }

  fun expectIfEnd() {
    expectationStack.push(ExpectedConstruction.END_IF)
  }

  fun removeForEndExpectation() {
    if (expectationStack.peek() == ExpectedConstruction.END_FOR) {
      expectationStack.pop()
    }
  }

  fun removeIfEndExpectation() {
    if (expectationStack.peek() == ExpectedConstruction.END_IF) {
      expectationStack.pop()
    }
  }

  fun isControlStructureTokenExpected(tokenType: IElementType?): Boolean {
    return expectationStack.peek()?.expectedTokenSet?.contains(tokenType) ?: false
  }
}