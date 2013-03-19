/*
 * Copyright 2013 The authors
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

import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.psi.tree.TokenSet;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlTokenGroups implements OgnlTypes {

  public static final TokenSet NUMBERS = TokenSet.create(INTEGER_LITERAL,
                                                         BIG_INTEGER_LITERAL,
                                                         DOUBLE_LITERAL,
                                                         BIG_DECIMAL_LITERAL);

  // literals
  public static final TokenSet TEXT = TokenSet.create(CHARACTER_LITERAL, STRING_LITERAL);

  // keywords
  public static final TokenSet KEYWORDS = TokenSet.create(NEW_KEYWORD, TRUE_KEYWORD, FALSE_KEYWORD, NULL_KEYWORD);

  // bit-shift
  public static final TokenSet SHIFT_OPS = TokenSet.create(SHIFT_LEFT, SHIFT_RIGHT, SHIFT_RIGHT_LOGICAL);

  public static final TokenSet ADDITION_OPS = TokenSet.create(PLUS, MINUS);

  public static final TokenSet MULTIPLICATION_OPS = TokenSet.create(MULTIPLY, DIVISION, MODULO);

  // comparison
  public static final TokenSet EQUALITY_OPS = TokenSet.create(EQUAL, NOT_EQUAL);

  public static final TokenSet RELATIONAL_OPS = TokenSet.create(LESS, GREATER, LESS_EQUAL, GREATER_EQUAL);

  // boolean ops
  public static final TokenSet BITWISE_OPS = TokenSet.create(AND, OR, XOR);

  // logical ops
  public static final TokenSet LOGICAL_OPS = TokenSet.create(AND_AND, OR_OR);

  public static final TokenSet UNARY_OPERATION_SIGNS = TokenSet.create(PLUS, MINUS, NEGATE, NOT);
  public static final TokenSet UNARY_OPS = TokenSet.orSet(UNARY_OPERATION_SIGNS, TokenSet.create(NOT_KEYWORD));

  public static final TokenSet OPERATION_SIGNS = TokenSet.orSet(
    SHIFT_OPS, ADDITION_OPS, MULTIPLICATION_OPS, EQUALITY_OPS, RELATIONAL_OPS, BITWISE_OPS, LOGICAL_OPS, UNARY_OPERATION_SIGNS);

  public static final TokenSet OPERATION_KEYWORDS = TokenSet.create(
    NOT_KEYWORD, NOT_IN_KEYWORD, IN_KEYWORD, INSTANCEOF_KEYWORD,
    SHIFT_LEFT_KEYWORD, SHIFT_RIGHT_KEYWORD, SHIFT_RIGHT_LOGICAL_KEYWORD,
    AND_KEYWORD, BAND_KEYWORD, OR_KEYWORD, BOR_KEYWORD, XOR_KEYWORD, EQ_KEYWORD, NEQ_KEYWORD,
    LT_KEYWORD, LT_EQ_KEYWORD, GT_KEYWORD, GT_EQ_KEYWORD);

  public static final TokenSet OPERATIONS = TokenSet.orSet(OPERATION_SIGNS,
                                                           OPERATION_KEYWORDS);
}
