/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.psi;

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlTokenTypes implements TokenType {

  // expression boundaries
  public static final OgnlTokenType EXPRESSION_START = new OgnlTokenType(OgnlLanguage.EXPRESSION_PREFIX);
  public static final OgnlTokenType EXPRESSION_END = new OgnlTokenType(OgnlLanguage.EXPRESSION_SUFFIX);

  // names
  public static final OgnlTokenType IDENTIFIER = new OgnlTokenType("IDENTIFIER");

  // numbers
  public static final OgnlTokenType INTEGER_LITERAL = new OgnlTokenType("INTEGER_LITERAL");
  public static final OgnlTokenType BIG_INTEGER_LITERAL = new OgnlTokenType("BIG_INTEGER_LITERAL");
  public static final OgnlTokenType DOUBLE_LITERAL = new OgnlTokenType("DOUBLE_LITERAL");
  public static final OgnlTokenType BIG_DECIMAL_LITERAL = new OgnlTokenType("BIG_DECIMAL_LITERAL");
  public static final TokenSet NUMBERS = TokenSet.create(INTEGER_LITERAL,
                                                         BIG_INTEGER_LITERAL,
                                                         DOUBLE_LITERAL,
                                                         BIG_DECIMAL_LITERAL);

  // literals
  public static final OgnlTokenType CHARACTER_LITERAL = new OgnlTokenType("CHARACTER_LITERAL");
  public static final OgnlTokenType STRING_LITERAL = new OgnlTokenType("STRING_LITERAL");
  public static final TokenSet TEXT = TokenSet.create(CHARACTER_LITERAL, STRING_LITERAL);

  // keywords
  public static final OgnlTokenType SHIFT_LEFT_KEYWORD = new OgnlTokenType("SHIFT_LEFT_KEYWORD");
  public static final OgnlTokenType SHIFT_RIGHT_KEYWORD = new OgnlTokenType("SHIFT_RIGHT_KEYWORD");
  public static final OgnlTokenType SHIFT_RIGHT_LOGICAL_KEYWORD = new OgnlTokenType("SHIFT_RIGHT_LOGICAL_KEYWORD");

  public static final OgnlTokenType AND_KEYWORD = new OgnlTokenType("AND_KEYWORD");
  public static final OgnlTokenType OR_KEYWORD = new OgnlTokenType("OR_KEYWORD");

  public static final OgnlTokenType BAND_KEYWORD = new OgnlTokenType("BAND_KEYWORD");
  public static final OgnlTokenType BOR_KEYWORD = new OgnlTokenType("BOR_KEYWORD");
  public static final OgnlTokenType XOR_KEYWORD = new OgnlTokenType("XOR_KEYWORD");

  public static final OgnlTokenType EQ_KEYWORD = new OgnlTokenType("EQ_KEYWORD");
  public static final OgnlTokenType NEQ_KEYWORD = new OgnlTokenType("NEQ_KEYWORD");

  public static final OgnlTokenType LT_KEYWORD = new OgnlTokenType("LT_KEYWORD");
  public static final OgnlTokenType LT_EQ_KEYWORD = new OgnlTokenType("LT_EQ_KEYWORD");
  public static final OgnlTokenType GT_KEYWORD = new OgnlTokenType("GT_KEYWORD");
  public static final OgnlTokenType GT_EQ_KEYWORD = new OgnlTokenType("GT_EQ_KEYWORD");

  public static final OgnlTokenType NEGATE = new OgnlTokenType("NEGATE");
  public static final OgnlTokenType NOT_KEYWORD = new OgnlTokenType("NOT_KEYWORD");

  public static final OgnlTokenType NOT_IN_KEYWORD = new OgnlTokenType("NOT_IN_KEYWORD");
  public static final OgnlTokenType IN_KEYWORD = new OgnlTokenType("IN_KEYWORD");
  public static final OgnlTokenType NEW_KEYWORD = new OgnlTokenType("NEW_KEYWORD");

  public static final OgnlTokenType TRUE_KEYWORD = new OgnlTokenType("TRUE_KEYWORD");
  public static final OgnlTokenType FALSE_KEYWORD = new OgnlTokenType("FALSE_KEYWORD");
  public static final OgnlTokenType NULL_KEYWORD = new OgnlTokenType("NULL_KEYWORD");
  public static final OgnlTokenType INSTANCEOF_KEYWORD = new OgnlTokenType("INSTANCEOF_KEYWORD");

  public static final TokenSet KEYWORDS = TokenSet.create(
      NEW_KEYWORD, TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD, INSTANCEOF_KEYWORD);

  // bit-shift
  public static final OgnlTokenType SHIFT_LEFT = new OgnlTokenType("SHIFT_LEFT");
  public static final OgnlTokenType SHIFT_RIGHT = new OgnlTokenType("SHIFT_RIGHT");
  public static final OgnlTokenType SHIFT_RIGHT_LOGICAL = new OgnlTokenType("SHIFT_RIGHT_LOGICAL");

  // simple tokens
  public static final OgnlTokenType DOT = new OgnlTokenType("DOT");
  public static final OgnlTokenType COMMA = new OgnlTokenType("COMMA");
  public static final OgnlTokenType EQ = new OgnlTokenType("EQ");

  // special expressions
  public static final OgnlTokenType COLON = new OgnlTokenType("COLON");
  public static final OgnlTokenType QUESTION = new OgnlTokenType("QUESTION");
  public static final OgnlTokenType HASH = new OgnlTokenType("HASH");
  public static final OgnlTokenType AT = new OgnlTokenType("AT");
  public static final OgnlTokenType DOLLAR = new OgnlTokenType("DOLLAR");

  // math
  public static final OgnlTokenType MULTIPLY = new OgnlTokenType("MULTIPLY");
  public static final OgnlTokenType DIVISION = new OgnlTokenType("DIVISION");
  public static final OgnlTokenType PLUS = new OgnlTokenType("PLUS");
  public static final OgnlTokenType MINUS = new OgnlTokenType("MINUS");
  public static final OgnlTokenType MODULO = new OgnlTokenType("MODULO");

  // comparison
  public static final OgnlTokenType EQUAL = new OgnlTokenType("EQUAL");
  public static final OgnlTokenType NOT_EQUAL = new OgnlTokenType("NOT_EQUAL");

  public static final OgnlTokenType LESS = new OgnlTokenType("LESS");
  public static final OgnlTokenType GREATER = new OgnlTokenType("GREATER");
  public static final OgnlTokenType LESS_EQUAL = new OgnlTokenType("LESS_EQUAL");
  public static final OgnlTokenType GREATER_EQUAL = new OgnlTokenType("GREATER_EQUAL");

  // boolean ops
  public static final OgnlTokenType OR = new OgnlTokenType("OR");
  public static final OgnlTokenType XOR = new OgnlTokenType("XOR");
  public static final OgnlTokenType AND = new OgnlTokenType("AND");

  // logical ops
  public static final OgnlTokenType AND_AND = new OgnlTokenType("AND_AND");
  public static final OgnlTokenType OR_OR = new OgnlTokenType("OR_OR");

  public static final OgnlTokenType NOT = new OgnlTokenType("NOT");

  public static final TokenSet OPERATION_SIGNS = TokenSet.create(
      QUESTION, EQ,
      MULTIPLY, DIVISION, PLUS, MINUS, MODULO,
      NEGATE, EQUAL, NOT_EQUAL, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL,
      OR, XOR, AND, NOT, AND_AND, OR_OR,
      SHIFT_LEFT, SHIFT_RIGHT, SHIFT_RIGHT_LOGICAL);

  public static final TokenSet OPERATION_KEYWORDS = TokenSet.create(
      NOT_KEYWORD, NOT_IN_KEYWORD, IN_KEYWORD,
      SHIFT_LEFT_KEYWORD, SHIFT_RIGHT_KEYWORD, SHIFT_RIGHT_LOGICAL_KEYWORD,
      AND_KEYWORD, BAND_KEYWORD, OR_KEYWORD, BOR_KEYWORD, XOR_KEYWORD, EQ_KEYWORD, NEQ_KEYWORD,
      LT_KEYWORD, LT_EQ_KEYWORD, GT_KEYWORD, GT_EQ_KEYWORD);

  public static final TokenSet OPERATIONS = TokenSet.orSet(OPERATION_SIGNS,
                                                           OPERATION_KEYWORDS);

  // bracing
  public static final OgnlTokenType LBRACKET = new OgnlTokenType("[");
  public static final OgnlTokenType RBRACKET = new OgnlTokenType("]");

  public static final OgnlTokenType LBRACE = new OgnlTokenType("{");
  public static final OgnlTokenType RBRACE = new OgnlTokenType("}");

  public static final OgnlTokenType LPARENTH = new OgnlTokenType("(");
  public static final OgnlTokenType RPARENTH = new OgnlTokenType(")");

}