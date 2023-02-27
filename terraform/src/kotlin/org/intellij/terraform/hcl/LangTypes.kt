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
package org.intellij.terraform.hcl

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

open class HCLElementType(debugName: String) : IElementType(debugName, HCLLanguage)
open class HCLTokenType(debugName: String) : IElementType(debugName, HCLLanguage)

object HCLTokenTypes {
  init {
    assert(HCLElementTypes.OP_PLUS != null)
  }
  private val HCL_BINARY_OPERATORS: TokenSet = TokenSet.create(
      HCLElementTypes.OP_PLUS, HCLElementTypes.OP_MINUS, HCLElementTypes.OP_MUL, HCLElementTypes.OP_DIV, HCLElementTypes.OP_MOD,
      HCLElementTypes.OP_EQUAL, HCLElementTypes.OP_NOT_EQUAL,
      HCLElementTypes.OP_LESS, HCLElementTypes.OP_GREATER, HCLElementTypes.OP_LESS_OR_EQUAL, HCLElementTypes.OP_GREATER_OR_EQUAL,
      HCLElementTypes.OP_AND_AND, HCLElementTypes.OP_OR_OR
  )

  @JvmStatic val HCL_UNARY_OPERATORS: TokenSet = TokenSet.create(
      HCLElementTypes.OP_PLUS, HCLElementTypes.OP_MINUS, HCLElementTypes.OP_NOT
  )

  private val HCL_TERNARY_OPERATOR_TOKENS = TokenSet.create(
      HCLElementTypes.OP_QUEST, HCLElementTypes.OP_COLON
  )

  val HCL_ALL_OPERATORS = TokenSet.orSet(HCL_UNARY_OPERATORS, HCL_BINARY_OPERATORS, HCL_TERNARY_OPERATOR_TOKENS)

  @JvmField
  val STRING_LITERALS: TokenSet = TokenSet.create(HCLElementTypes.SINGLE_QUOTED_STRING, HCLElementTypes.DOUBLE_QUOTED_STRING)

  @JvmField
  val IDENTIFYING_LITERALS: TokenSet = TokenSet.create(HCLElementTypes.SINGLE_QUOTED_STRING, HCLElementTypes.DOUBLE_QUOTED_STRING,
                                                       HCLElementTypes.ID)

  @JvmField
  val HCL_BRACES: TokenSet = TokenSet.create(HCLElementTypes.L_CURLY, HCLElementTypes.R_CURLY)

  @JvmField
  val HCL_BRACKETS: TokenSet = TokenSet.create(HCLElementTypes.L_BRACKET, HCLElementTypes.R_BRACKET)

  @JvmField
  val HCL_CONTAINERS: TokenSet = TokenSet.create(HCLElementTypes.OBJECT, HCLElementTypes.BLOCK_OBJECT, HCLElementTypes.ARRAY,
                                                 HCLElementTypes.FOR_ARRAY_EXPRESSION, HCLElementTypes.FOR_OBJECT_EXPRESSION)

  @JvmField
  val HCL_BOOLEANS: TokenSet = TokenSet.create(HCLElementTypes.TRUE, HCLElementTypes.FALSE)

  @JvmField
  val HCL_KEYWORDS: TokenSet = TokenSet.create(HCLElementTypes.TRUE, HCLElementTypes.FALSE, HCLElementTypes.NULL)

  @JvmField
  val HCL_LITERALS: TokenSet = TokenSet.create(HCLElementTypes.STRING_LITERAL, HCLElementTypes.NUMBER_LITERAL,
                                               HCLElementTypes.NULL_LITERAL, HCLElementTypes.TRUE, HCLElementTypes.FALSE)

  @JvmField
  val HCL_VALUES: TokenSet = TokenSet.orSet(HCL_CONTAINERS, HCL_LITERALS)

  @JvmField
  val HCL_LINE_COMMENTS: TokenSet = TokenSet.create(HCLElementTypes.LINE_C_COMMENT, HCLElementTypes.LINE_HASH_COMMENT)

  @JvmField
  val HCL_COMMENTARIES: TokenSet = TokenSet.create(HCLElementTypes.BLOCK_COMMENT, HCLElementTypes.LINE_C_COMMENT,
                                                   HCLElementTypes.LINE_HASH_COMMENT)
}
