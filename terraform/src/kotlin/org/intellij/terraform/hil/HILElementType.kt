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
package org.intellij.terraform.hil

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementTypes.*


open class HILElementType(debugName: String) : IElementType(debugName, HILLanguage) {
  companion object {
    // Ensure this token set equals to ILExpression bnf rule
    val IL_EXPRESSIONS: TokenSet = TokenSet.create(
        IL_PARENTHESIZED_EXPRESSION,
        IL_EXPRESSION_HOLDER,
        IL_INDEX_SELECT_EXPRESSION,
        IL_SELECT_EXPRESSION,
        IL_CONDITIONAL_EXPRESSION,
        IL_BINARY_RELATIONAL_EXPRESSION,
        IL_BINARY_EQUALITY_EXPRESSION,
        IL_BINARY_OR_EXPRESSION,
        IL_BINARY_AND_EXPRESSION,
        IL_BINARY_ADDITION_EXPRESSION,
        IL_BINARY_MULTIPLY_EXPRESSION,
        IL_METHOD_CALL_EXPRESSION,
        IL_LITERAL_EXPRESSION,
        IL_UNARY_EXPRESSION,
        IL_VARIABLE
    )
  }
}

object HILTypes {
  val ILBinaryNumericOnlyOperations = TokenSet.create(
      IL_BINARY_RELATIONAL_EXPRESSION,
      IL_BINARY_ADDITION_EXPRESSION,
      IL_BINARY_MULTIPLY_EXPRESSION
  )
  val ILBinaryBooleanOnlyOperations = TokenSet.create(
      IL_BINARY_AND_EXPRESSION,
      IL_BINARY_OR_EXPRESSION
  )
}