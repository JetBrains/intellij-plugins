// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementTypes.DOUBLE_QUOTED_STRING
import org.intellij.terraform.hil.HILElementTypes.ELSE_KEYWORD
import org.intellij.terraform.hil.HILElementTypes.ENDFOR_KEYWORD
import org.intellij.terraform.hil.HILElementTypes.ENDIF_KEYWORD
import org.intellij.terraform.hil.HILElementTypes.FALSE
import org.intellij.terraform.hil.HILElementTypes.FOR_KEYWORD
import org.intellij.terraform.hil.HILElementTypes.IF_KEYWORD
import org.intellij.terraform.hil.HILElementTypes.IL_LITERAL_EXPRESSION
import org.intellij.terraform.hil.HILElementTypes.INTERPOLATION_START
import org.intellij.terraform.hil.HILElementTypes.L_BRACKET
import org.intellij.terraform.hil.HILElementTypes.L_CURLY
import org.intellij.terraform.hil.HILElementTypes.L_PAREN
import org.intellij.terraform.hil.HILElementTypes.NULL
import org.intellij.terraform.hil.HILElementTypes.OP_AND_AND
import org.intellij.terraform.hil.HILElementTypes.OP_COLON
import org.intellij.terraform.hil.HILElementTypes.OP_DIV
import org.intellij.terraform.hil.HILElementTypes.OP_EQUAL
import org.intellij.terraform.hil.HILElementTypes.OP_GREATER
import org.intellij.terraform.hil.HILElementTypes.OP_GREATER_OR_EQUAL
import org.intellij.terraform.hil.HILElementTypes.OP_LESS
import org.intellij.terraform.hil.HILElementTypes.OP_LESS_OR_EQUAL
import org.intellij.terraform.hil.HILElementTypes.OP_MINUS
import org.intellij.terraform.hil.HILElementTypes.OP_MOD
import org.intellij.terraform.hil.HILElementTypes.OP_MUL
import org.intellij.terraform.hil.HILElementTypes.OP_NOT
import org.intellij.terraform.hil.HILElementTypes.OP_NOT_EQUAL
import org.intellij.terraform.hil.HILElementTypes.OP_OR_OR
import org.intellij.terraform.hil.HILElementTypes.OP_PLUS
import org.intellij.terraform.hil.HILElementTypes.OP_QUEST
import org.intellij.terraform.hil.HILElementTypes.R_BRACKET
import org.intellij.terraform.hil.HILElementTypes.R_CURLY
import org.intellij.terraform.hil.HILElementTypes.R_PAREN
import org.intellij.terraform.hil.HILElementTypes.TEMPLATE_START
import org.intellij.terraform.hil.HILElementTypes.TRUE

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

  @JvmStatic
  val IL_CONTROL_STRUCTURE_END_KEYWORDS: TokenSet = TokenSet.create(
    ENDFOR_KEYWORD, ELSE_KEYWORD, ENDIF_KEYWORD
  )

  @JvmStatic
  val IL_CONTROL_STRUCTURE_START_KEYWORDS: TokenSet = TokenSet.create(
    FOR_KEYWORD, IF_KEYWORD
  )

  private val IL_TERNARY_OPERATOR_TOKENS = TokenSet.create(
    OP_QUEST, OP_COLON
  )

  val IL_ALL_OPERATORS: TokenSet = TokenSet.orSet(IL_UNARY_OPERATORS, IL_BINARY_OPERATORS, IL_TERNARY_OPERATOR_TOKENS)

  val STRING_LITERALS: TokenSet = TokenSet.create(DOUBLE_QUOTED_STRING)

  val FILE: IFileElementType = IFileElementType(HILLanguage)

  val TIL_BRACES: TokenSet = TokenSet.create(INTERPOLATION_START, TEMPLATE_START, L_CURLY, R_CURLY)
  val TIL_BRACKETS: TokenSet = TokenSet.create(L_BRACKET, R_BRACKET)
  val TIL_PARENS: TokenSet = TokenSet.create(L_PAREN, R_PAREN)
  val TIL_KEYWORDS: TokenSet = TokenSet.create(TRUE, FALSE, NULL)
  val TIL_LITERALS: TokenSet = TokenSet.create(IL_LITERAL_EXPRESSION, TRUE, FALSE)
}