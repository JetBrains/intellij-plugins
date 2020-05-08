/*
 * Copyright 2018 The authors
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


// Generated from ognl.bnf, do not modify
package com.intellij.lang.ognl;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.psi.OgnlTokenType;
import com.intellij.lang.ognl.psi.impl.*;

public interface OgnlTypes {

  IElementType BINARY_EXPRESSION = new OgnlTokenType("BINARY_EXPRESSION");
  IElementType CONDITIONAL_EXPRESSION = new OgnlTokenType("CONDITIONAL_EXPRESSION");
  IElementType EXPRESSION = new OgnlTokenType("EXPRESSION");
  IElementType FQN_TYPE_EXPRESSION = new OgnlTokenType("FQN_TYPE_EXPRESSION");
  IElementType INDEXED_EXPRESSION = new OgnlTokenType("INDEXED_EXPRESSION");
  IElementType LAMBDA_EXPRESSION = new OgnlTokenType("LAMBDA_EXPRESSION");
  IElementType LITERAL_EXPRESSION = new OgnlTokenType("LITERAL_EXPRESSION");
  IElementType MAP_ENTRY_ELEMENT = new OgnlTokenType("MAP_ENTRY_ELEMENT");
  IElementType MAP_EXPRESSION = new OgnlTokenType("MAP_EXPRESSION");
  IElementType METHOD_CALL_EXPRESSION = new OgnlTokenType("METHOD_CALL_EXPRESSION");
  IElementType NEW_ARRAY_EXPRESSION = new OgnlTokenType("NEW_ARRAY_EXPRESSION");
  IElementType NEW_EXPRESSION = new OgnlTokenType("NEW_EXPRESSION");
  IElementType PARAMETER_LIST = new OgnlTokenType("PARAMETER_LIST");
  IElementType PARENTHESIZED_EXPRESSION = new OgnlTokenType("PARENTHESIZED_EXPRESSION");
  IElementType PROJECTION_EXPRESSION = new OgnlTokenType("PROJECTION_EXPRESSION");
  IElementType REFERENCE_EXPRESSION = new OgnlTokenType("REFERENCE_EXPRESSION");
  IElementType SELECTION_EXPRESSION = new OgnlTokenType("SELECTION_EXPRESSION");
  IElementType SEQUENCE_EXPRESSION = new OgnlTokenType("SEQUENCE_EXPRESSION");
  IElementType UNARY_EXPRESSION = new OgnlTokenType("UNARY_EXPRESSION");
  IElementType VARIABLE_ASSIGNMENT_EXPRESSION = new OgnlTokenType("VARIABLE_ASSIGNMENT_EXPRESSION");
  IElementType VARIABLE_EXPRESSION = new OgnlTokenType("VARIABLE_EXPRESSION");

  IElementType AND = new OgnlTokenType("&");
  IElementType AND_AND = new OgnlTokenType("&&");
  IElementType AND_KEYWORD = new OgnlTokenType("and");
  IElementType AT = new OgnlTokenType("@");
  IElementType BAND_KEYWORD = new OgnlTokenType("band");
  IElementType BIG_DECIMAL_LITERAL = new OgnlTokenType("BIG_DECIMAL_LITERAL");
  IElementType BIG_INTEGER_LITERAL = new OgnlTokenType("BIG_INTEGER_LITERAL");
  IElementType BOR_KEYWORD = new OgnlTokenType("bor");
  IElementType CHARACTER_LITERAL = new OgnlTokenType("CHARACTER_LITERAL");
  IElementType COLON = new OgnlTokenType(":");
  IElementType COMMA = new OgnlTokenType(",");
  IElementType DIVISION = new OgnlTokenType("/");
  IElementType DOLLAR = new OgnlTokenType("$");
  IElementType DOT = new OgnlTokenType(".");
  IElementType DOUBLE_LITERAL = new OgnlTokenType("DOUBLE_LITERAL");
  IElementType EQ = new OgnlTokenType("=");
  IElementType EQUAL = new OgnlTokenType("==");
  IElementType EQ_KEYWORD = new OgnlTokenType("eq");
  IElementType EXPRESSION_END = new OgnlTokenType("EXPRESSION_END");
  IElementType EXPRESSION_START = new OgnlTokenType("%{");
  IElementType FALSE_KEYWORD = new OgnlTokenType("false");
  IElementType GREATER = new OgnlTokenType(">");
  IElementType GREATER_EQUAL = new OgnlTokenType(">=");
  IElementType GT_EQ_KEYWORD = new OgnlTokenType("gte");
  IElementType GT_KEYWORD = new OgnlTokenType("gt");
  IElementType HASH = new OgnlTokenType("#");
  IElementType IDENTIFIER = new OgnlTokenType("IDENTIFIER");
  IElementType INSTANCEOF_KEYWORD = new OgnlTokenType("instanceof");
  IElementType INTEGER_LITERAL = new OgnlTokenType("INTEGER_LITERAL");
  IElementType IN_KEYWORD = new OgnlTokenType("in");
  IElementType LBRACE = new OgnlTokenType("{");
  IElementType LBRACKET = new OgnlTokenType("[");
  IElementType LESS = new OgnlTokenType("<");
  IElementType LESS_EQUAL = new OgnlTokenType("<=");
  IElementType LPARENTH = new OgnlTokenType("(");
  IElementType LT_EQ_KEYWORD = new OgnlTokenType("lte");
  IElementType LT_KEYWORD = new OgnlTokenType("lt");
  IElementType MINUS = new OgnlTokenType("-");
  IElementType MODULO = new OgnlTokenType("%");
  IElementType MULTIPLY = new OgnlTokenType("*");
  IElementType NEGATE = new OgnlTokenType("!");
  IElementType NEQ_KEYWORD = new OgnlTokenType("neq");
  IElementType NEW_KEYWORD = new OgnlTokenType("new");
  IElementType NOT = new OgnlTokenType("~");
  IElementType NOT_EQUAL = new OgnlTokenType("!=");
  IElementType NOT_IN_KEYWORD = new OgnlTokenType("not in");
  IElementType NOT_KEYWORD = new OgnlTokenType("not");
  IElementType NULL_KEYWORD = new OgnlTokenType("null");
  IElementType OR = new OgnlTokenType("|");
  IElementType OR_KEYWORD = new OgnlTokenType("or");
  IElementType OR_OR = new OgnlTokenType("||");
  IElementType PLUS = new OgnlTokenType("+");
  IElementType QUESTION = new OgnlTokenType("?");
  IElementType RBRACE = new OgnlTokenType("}");
  IElementType RBRACKET = new OgnlTokenType("]");
  IElementType RPARENTH = new OgnlTokenType(")");
  IElementType SHIFT_LEFT = new OgnlTokenType("<<");
  IElementType SHIFT_LEFT_KEYWORD = new OgnlTokenType("shl");
  IElementType SHIFT_RIGHT = new OgnlTokenType(">>");
  IElementType SHIFT_RIGHT_KEYWORD = new OgnlTokenType("shr");
  IElementType SHIFT_RIGHT_LOGICAL = new OgnlTokenType(">>>");
  IElementType SHIFT_RIGHT_LOGICAL_KEYWORD = new OgnlTokenType("ushr");
  IElementType STRING_LITERAL = new OgnlTokenType("STRING_LITERAL");
  IElementType TRUE_KEYWORD = new OgnlTokenType("true");
  IElementType XOR = new OgnlTokenType("^");
  IElementType XOR_KEYWORD = new OgnlTokenType("xor");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BINARY_EXPRESSION) {
        return new OgnlBinaryExpressionImpl(node);
      }
      else if (type == CONDITIONAL_EXPRESSION) {
        return new OgnlConditionalExpressionImpl(node);
      }
      else if (type == FQN_TYPE_EXPRESSION) {
        return new OgnlFqnTypeExpressionImpl(node);
      }
      else if (type == INDEXED_EXPRESSION) {
        return new OgnlIndexedExpressionImpl(node);
      }
      else if (type == LAMBDA_EXPRESSION) {
        return new OgnlLambdaExpressionImpl(node);
      }
      else if (type == LITERAL_EXPRESSION) {
        return new OgnlLiteralExpressionImpl(node);
      }
      else if (type == MAP_ENTRY_ELEMENT) {
        return new OgnlMapEntryElementImpl(node);
      }
      else if (type == MAP_EXPRESSION) {
        return new OgnlMapExpressionImpl(node);
      }
      else if (type == METHOD_CALL_EXPRESSION) {
        return new OgnlMethodCallExpressionImpl(node);
      }
      else if (type == NEW_ARRAY_EXPRESSION) {
        return new OgnlNewArrayExpressionImpl(node);
      }
      else if (type == NEW_EXPRESSION) {
        return new OgnlNewExpressionImpl(node);
      }
      else if (type == PARAMETER_LIST) {
        return new OgnlParameterListImpl(node);
      }
      else if (type == PARENTHESIZED_EXPRESSION) {
        return new OgnlParenthesizedExpressionImpl(node);
      }
      else if (type == PROJECTION_EXPRESSION) {
        return new OgnlProjectionExpressionImpl(node);
      }
      else if (type == REFERENCE_EXPRESSION) {
        return new OgnlReferenceExpressionImpl(node);
      }
      else if (type == SELECTION_EXPRESSION) {
        return new OgnlSelectionExpressionImpl(node);
      }
      else if (type == SEQUENCE_EXPRESSION) {
        return new OgnlSequenceExpressionImpl(node);
      }
      else if (type == UNARY_EXPRESSION) {
        return new OgnlUnaryExpressionImpl(node);
      }
      else if (type == VARIABLE_ASSIGNMENT_EXPRESSION) {
        return new OgnlVariableAssignmentExpressionImpl(node);
      }
      else if (type == VARIABLE_EXPRESSION) {
        return new OgnlVariableExpressionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
