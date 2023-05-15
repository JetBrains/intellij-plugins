// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementTypes.*

open class HILTokenType(debugName: String) : IElementType(debugName, HILLanguage)

object HILTokenTypes {
  init {
    assert(OP_PLUS != null)
  }
  private val IL_BINARY_OPERATORS: TokenSet = TokenSet.create(
      OP_PLUS, OP_MINUS, OP_MUL, OP_DIV, OP_MOD,
      OP_EQUAL, OP_NOT_EQUAL,
      OP_LESS, OP_GREATER, OP_LESS_OR_EQUAL, OP_GREATER_OR_EQUAL,
      OP_AND_AND, OP_OR_OR
  )

  val IL_UNARY_OPERATORS: TokenSet = TokenSet.create(
      OP_PLUS, OP_MINUS, OP_NOT
  )

  private val IL_TERNARY_OPERATOR_TOKENS = TokenSet.create(
      OP_QUEST, OP_COLON
  )

  val IL_ALL_OPERATORS = TokenSet.orSet(IL_UNARY_OPERATORS, IL_BINARY_OPERATORS, IL_TERNARY_OPERATOR_TOKENS)

  val STRING_LITERALS: TokenSet = TokenSet.create(HILElementTypes.DOUBLE_QUOTED_STRING)

  val FILE: IFileElementType = IFileElementType(HILLanguage)

  val TIL_BRACES: TokenSet = TokenSet.create(HILElementTypes.INTERPOLATION_START, HILElementTypes.TEMPLATE_START, HILElementTypes.L_CURLY, HILElementTypes.R_CURLY)
  val TIL_BRACKETS: TokenSet = TokenSet.create(HILElementTypes.L_BRACKET, HILElementTypes.R_BRACKET)
  val TIL_PARENS: TokenSet = TokenSet.create(HILElementTypes.L_PAREN, HILElementTypes.R_PAREN)
  val TIL_BOOLEANS: TokenSet = TokenSet.create(HILElementTypes.TRUE, HILElementTypes.FALSE)
  val TIL_KEYWORDS: TokenSet = TokenSet.create(HILElementTypes.TRUE, HILElementTypes.FALSE, HILElementTypes.NULL)
  val TIL_LITERALS: TokenSet = TokenSet.create(HILElementTypes.IL_LITERAL_EXPRESSION, HILElementTypes.TRUE, HILElementTypes.FALSE)
  val TIL_VALUES: TokenSet = TokenSet.orSet(TIL_LITERALS)
}