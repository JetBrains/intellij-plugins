/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.model.formatter;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlFormatterUtil implements CfmlElementTypes {
  public static final TokenSet ASSIGNMENT_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.ADD_EQ,
    CfscriptTokenTypes.MINUS_EQ,
    CfscriptTokenTypes.MUL_EQ,
    CfscriptTokenTypes.DEV_EQ,
    CfscriptTokenTypes.CONCAT_EQ,
    CfmlTokenTypes.ASSIGN);
  public static final TokenSet LOGICAL_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.AND,
    CfscriptTokenTypes.OR);
  public static final TokenSet EQUALITY_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.EQEQ,
    CfscriptTokenTypes.NEQ);
  public static final TokenSet RELATIONAL_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.LT,
    CfscriptTokenTypes.LTE,
    CfscriptTokenTypes.GT,
    CfscriptTokenTypes.GTE);
  public static final TokenSet ADDITIVE_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.ADD,
    CfscriptTokenTypes.MINUS);
  public static final TokenSet MULTIPLICATIVE_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.MUL,
    CfscriptTokenTypes.DEV,
    CfscriptTokenTypes.POW,
    CfscriptTokenTypes.MOD);
  public static final TokenSet UNARY_OPERATORS = TokenSet.create(
    CfscriptTokenTypes.NOT,
    CfscriptTokenTypes.INC,
    CfscriptTokenTypes.DEC);

  public static final TokenSet BINARY_OPERATORS = TokenSet.orSet(MULTIPLICATIVE_OPERATORS, ADDITIVE_OPERATORS, LOGICAL_OPERATORS,
                                                                 EQUALITY_OPERATORS, RELATIONAL_OPERATORS);
  private static final TokenSet ALIGNABLE_ELEMENTS = TokenSet
    .create(ASSIGNMENT, FUNCTION_CALL_EXPRESSION, NONE, STRING_LITERAL, NEW_EXPRESSION, REFERENCE_EXPRESSION);
  private static final TokenSet PRIMITIVE_TYPE = TokenSet.create(CfmlElementTypes.INTEGER_LITERAL,
                                                                 CfmlElementTypes.DOUBLE_LITERAL,
                                                                 CfmlElementTypes.STRING_LITERAL,
                                                                 CfmlElementTypes.BOOLEAN_LITERAL);

  public static boolean isAssignmentExpression(IElementType elType) {
    return elType != null && elType.equals(CfmlElementTypes.ASSIGNMENT);
  }

  public static boolean isAlignable(IElementType childType) {
    return childType == UNARY_EXPRESSION ||
           childType == BINARY_EXPRESSION ||
           ALIGNABLE_ELEMENTS.contains(childType) ||
           childType == TERNARY_EXPRESSION;
  }


  public static boolean isAssignmentOperator(IElementType elType) {
    return ASSIGNMENT_OPERATORS.contains(elType);
  }

  public static boolean isLogicalOperator(IElementType elType) {
    return LOGICAL_OPERATORS.contains(elType);
  }

  public static boolean isEqualityOperator(IElementType elType) {
    return EQUALITY_OPERATORS.contains(elType);
  }

  public static boolean isRelationalOperator(IElementType elType) {
    return RELATIONAL_OPERATORS.contains(elType);
  }

  public static boolean isAdditiveOperator(IElementType elType) {
    return ADDITIVE_OPERATORS.contains(elType);
  }

  public static boolean isMultiplicativeOperator(IElementType elType) {
    return MULTIPLICATIVE_OPERATORS.contains(elType);
  }

  public static boolean isUnaryOperator(IElementType elType) {
    return UNARY_OPERATORS.contains(elType);
  }

  public static boolean isBinaryOperator(IElementType elType) {
    return BINARY_OPERATORS.contains(elType);
  }

  public static boolean isPrimitiveType(IElementType myType) {
    return PRIMITIVE_TYPE.contains(myType);
  }
}
