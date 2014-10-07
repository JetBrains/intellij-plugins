/*
 * Copyright 2014 The authors
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
package com.intellij.lang.ognl.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.lang.ognl.OgnlTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class OgnlParser implements PsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parse_only_(t, b);
    return b.getTreeBuilt();
  }

  public void parse_only_(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == BINARY_EXPRESSION) {
      r = expression(b, 0, 5);
    }
    else if (t == CONDITIONAL_EXPRESSION) {
      r = expression(b, 0, 4);
    }
    else if (t == EXPRESSION) {
      r = expression(b, 0, -1);
    }
    else if (t == INDEXED_EXPRESSION) {
      r = indexedExpression(b, 0);
    }
    else if (t == LAMBDA_EXPRESSION) {
      r = lambdaExpression(b, 0);
    }
    else if (t == LITERAL_EXPRESSION) {
      r = literalExpression(b, 0);
    }
    else if (t == MAP_ENTRY_ELEMENT) {
      r = mapEntryElement(b, 0);
    }
    else if (t == MAP_EXPRESSION) {
      r = mapExpression(b, 0);
    }
    else if (t == METHOD_CALL_EXPRESSION) {
      r = expression(b, 0, 8);
    }
    else if (t == NEW_ARRAY_EXPRESSION) {
      r = newArrayExpression(b, 0);
    }
    else if (t == NEW_EXPRESSION) {
      r = newExpression(b, 0);
    }
    else if (t == PARAMETER_LIST) {
      r = parameterList(b, 0);
    }
    else if (t == PARENTHESIZED_EXPRESSION) {
      r = parenthesizedExpression(b, 0);
    }
    else if (t == PROJECTION_EXPRESSION) {
      r = projectionExpression(b, 0);
    }
    else if (t == REFERENCE_EXPRESSION) {
      r = referenceExpression(b, 0);
    }
    else if (t == SELECTION_EXPRESSION) {
      r = selectionExpression(b, 0);
    }
    else if (t == SEQUENCE_EXPRESSION) {
      r = sequenceExpression(b, 0);
    }
    else if (t == UNARY_EXPRESSION) {
      r = unaryExpression(b, 0);
    }
    else if (t == VARIABLE_ASSIGNMENT_EXPRESSION) {
      r = variableAssignmentExpression(b, 0);
    }
    else if (t == VARIABLE_EXPRESSION) {
      r = variableExpression(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(BINARY_EXPRESSION, CONDITIONAL_EXPRESSION, EXPRESSION, INDEXED_EXPRESSION,
      LAMBDA_EXPRESSION, LITERAL_EXPRESSION, MAP_EXPRESSION, METHOD_CALL_EXPRESSION,
      NEW_ARRAY_EXPRESSION, NEW_EXPRESSION, PARENTHESIZED_EXPRESSION, PROJECTION_EXPRESSION,
      REFERENCE_EXPRESSION, SELECTION_EXPRESSION, SEQUENCE_EXPRESSION, UNARY_EXPRESSION,
      VARIABLE_ASSIGNMENT_EXPRESSION, VARIABLE_EXPRESSION),
  };

  /* ********************************************************** */
  // '[' expression? ']' sequenceExpression?
  static boolean arrayConstructorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayConstructorExpression")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, arrayConstructorExpression_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RBRACKET)) && r;
    r = p && arrayConstructorExpression_3(b, l + 1) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // expression?
  private static boolean arrayConstructorExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayConstructorExpression_1")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  // sequenceExpression?
  private static boolean arrayConstructorExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayConstructorExpression_3")) return false;
    sequenceExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // plusMinusOperations |
  //                            divideMultiplyOperations |
  //                            bitwiseBooleanOperations |
  //                            instanceOfOperation |
  //                            shiftOperations |
  //                            booleanOperations |
  //                            equalityOperations |
  //                            relationalOperations |
  //                            setOperations
  static boolean binaryOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binaryOperations")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<operator>");
    r = plusMinusOperations(b, l + 1);
    if (!r) r = divideMultiplyOperations(b, l + 1);
    if (!r) r = bitwiseBooleanOperations(b, l + 1);
    if (!r) r = instanceOfOperation(b, l + 1);
    if (!r) r = shiftOperations(b, l + 1);
    if (!r) r = booleanOperations(b, l + 1);
    if (!r) r = equalityOperations(b, l + 1);
    if (!r) r = relationalOperations(b, l + 1);
    if (!r) r = setOperations(b, l + 1);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "|" | "^" | "&" | "band" | "bor" | "xor"
  static boolean bitwiseBooleanOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseBooleanOperations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, BAND_KEYWORD);
    if (!r) r = consumeToken(b, BOR_KEYWORD);
    if (!r) r = consumeToken(b, XOR_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '!' | "~"
  static boolean bitwiseOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseOperations")) return false;
    if (!nextTokenIs(b, "", NEGATE, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NEGATE);
    if (!r) r = consumeToken(b, NOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "true" | "false"
  static boolean booleanLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "booleanLiteralExpression")) return false;
    if (!nextTokenIs(b, "", FALSE_KEYWORD, TRUE_KEYWORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TRUE_KEYWORD);
    if (!r) r = consumeToken(b, FALSE_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "&&" | "||" |
  //                               "and" | "or" |
  //                               "not"
  static boolean booleanOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "booleanOperations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND_AND);
    if (!r) r = consumeToken(b, OR_OR);
    if (!r) r = consumeToken(b, AND_KEYWORD);
    if (!r) r = consumeToken(b, OR_KEYWORD);
    if (!r) r = consumeToken(b, NOT_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ':' expression
  static boolean conditionalExpressionTail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalExpressionTail")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, COLON);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '(' parameterList ')'
  static boolean constructorExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constructorExpression")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, parameterList(b, l + 1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '*' | '/' | '%'
  static boolean divideMultiplyOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "divideMultiplyOperations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MULTIPLY);
    if (!r) r = consumeToken(b, DIVISION);
    if (!r) r = consumeToken(b, MODULO);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "==" | "!=" |
  //                                "eq" | "neq"
  static boolean equalityOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityOperations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQUAL);
    if (!r) r = consumeToken(b, NOT_EQUAL);
    if (!r) r = consumeToken(b, EQ_KEYWORD);
    if (!r) r = consumeToken(b, NEQ_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression (',' expression)+
  static boolean expressionSequenceRequired(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionSequenceRequired")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = expression(b, l + 1, -1);
    p = r; // pin = 1
    r = r && expressionSequenceRequired_1(b, l + 1);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // (',' expression)+
  private static boolean expressionSequenceRequired_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionSequenceRequired_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expressionSequenceRequired_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!expressionSequenceRequired_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionSequenceRequired_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // ',' expression
  private static boolean expressionSequenceRequired_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionSequenceRequired_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER ('.' IDENTIFIER)*
  public static boolean fqnTypeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fqnTypeExpression")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && fqnTypeExpression_1(b, l + 1);
    exit_section_(b, l, m, EXPRESSION, r, p, null);
    return r || p;
  }

  // ('.' IDENTIFIER)*
  private static boolean fqnTypeExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fqnTypeExpression_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!fqnTypeExpression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fqnTypeExpression_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // '.' IDENTIFIER
  private static boolean fqnTypeExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fqnTypeExpression_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, DOT);
    p = r; // pin = 1
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "instanceof"
  static boolean instanceOfOperation(PsiBuilder b, int l) {
    return consumeToken(b, INSTANCEOF_KEYWORD);
  }

  /* ********************************************************** */
  // expression ':' expression
  public static boolean mapEntryElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapEntryElement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<map entry>");
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, COLON);
    p = r; // pin = 2
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, MAP_ENTRY_ELEMENT, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // mapEntryElement (',' mapEntryElement)*
  static boolean mapExpressionSequence(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpressionSequence")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mapEntryElement(b, l + 1);
    r = r && mapExpressionSequence_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' mapEntryElement)*
  private static boolean mapExpressionSequence_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpressionSequence_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!mapExpressionSequence_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mapExpressionSequence_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' mapEntryElement
  private static boolean mapExpressionSequence_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpressionSequence_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && mapEntryElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INTEGER_LITERAL | BIG_INTEGER_LITERAL | DOUBLE_LITERAL | BIG_DECIMAL_LITERAL
  static boolean numberLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numberLiteralExpression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    if (!r) r = consumeToken(b, BIG_INTEGER_LITERAL);
    if (!r) r = consumeToken(b, DOUBLE_LITERAL);
    if (!r) r = consumeToken(b, BIG_DECIMAL_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expression? (',' expression)*
  public static boolean parameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<parameter list>");
    r = parameterList_0(b, l + 1);
    r = r && parameterList_1(b, l + 1);
    exit_section_(b, l, m, PARAMETER_LIST, r, false, null);
    return r;
  }

  // expression?
  private static boolean parameterList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_0")) return false;
    expression(b, l + 1, -1);
    return true;
  }

  // (',' expression)*
  private static boolean parameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!parameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameterList_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ',' expression
  private static boolean parameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '+' | '-'
  static boolean plusMinusOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plusMinusOperations")) return false;
    if (!nextTokenIs(b, "", PLUS, MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' expression '}'
  public static boolean projectionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "projectionExpression")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, PROJECTION_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // '<'  | "<="  | '>'  | ">=" |
  //                                  "lt" | "lte" | "gt" | "gte"
  static boolean relationalOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LESS);
    if (!r) r = consumeToken(b, LESS_EQUAL);
    if (!r) r = consumeToken(b, GREATER);
    if (!r) r = consumeToken(b, GREATER_EQUAL);
    if (!r) r = consumeToken(b, LT_KEYWORD);
    if (!r) r = consumeToken(b, LT_EQ_KEYWORD);
    if (!r) r = consumeToken(b, GT_KEYWORD);
    if (!r) r = consumeToken(b, GT_EQ_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // EXPRESSION_START rootElement EXPRESSION_END
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    if (!nextTokenIs(b, EXPRESSION_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeToken(b, EXPRESSION_START);
    p = r; // pin = 1
    r = r && report_error_(b, rootElement(b, l + 1));
    r = p && consumeToken(b, EXPRESSION_END) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // expression
  static boolean rootElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rootElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = expression(b, l + 1, -1);
    exit_section_(b, l, m, null, r, false, rootRecover_parser_);
    return r;
  }

  /* ********************************************************** */
  // !(EXPRESSION_END)
  static boolean rootRecover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rootRecover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_, null);
    r = !consumeToken(b, EXPRESSION_END);
    exit_section_(b, l, m, null, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '{' ('?' | '^' | '$') expression '}'
  public static boolean selectionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selectionExpression")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && selectionExpression_1(b, l + 1);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, SELECTION_EXPRESSION, r);
    return r;
  }

  // '?' | '^' | '$'
  private static boolean selectionExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selectionExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUESTION);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, DOLLAR);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "not in" | "in"
  static boolean setOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setOperations")) return false;
    if (!nextTokenIs(b, "", IN_KEYWORD, NOT_IN_KEYWORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOT_IN_KEYWORD);
    if (!r) r = consumeToken(b, IN_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "<<" | ">>" | ">>>" |
  //                             "shl" | "shr" | "ushr"
  static boolean shiftOperations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftOperations")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SHIFT_LEFT);
    if (!r) r = consumeToken(b, SHIFT_RIGHT);
    if (!r) r = consumeToken(b, SHIFT_RIGHT_LOGICAL);
    if (!r) r = consumeToken(b, SHIFT_LEFT_KEYWORD);
    if (!r) r = consumeToken(b, SHIFT_RIGHT_KEYWORD);
    if (!r) r = consumeToken(b, SHIFT_RIGHT_LOGICAL_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING_LITERAL | CHARACTER_LITERAL
  static boolean textLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "textLiteralExpression")) return false;
    if (!nextTokenIs(b, "", CHARACTER_LITERAL, STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING_LITERAL);
    if (!r) r = consumeToken(b, CHARACTER_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // bitwiseOperations |
  //                           '+' | '-' | "not"
  static boolean unaryOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryOperator")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = bitwiseOperations(b, l + 1);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, NOT_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: expression
  // Operator priority table:
  // 0: ATOM(lambdaExpression)
  // 1: ATOM(mapExpression)
  // 2: ATOM(sequenceExpression)
  // 3: ATOM(parenthesizedExpression)
  // 4: ATOM(variableAssignmentExpression)
  // 5: BINARY(conditionalExpression)
  // 6: BINARY(binaryExpression)
  // 7: ATOM(newArrayExpression)
  // 8: ATOM(newExpression)
  // 9: POSTFIX(methodCallExpression)
  // 10: ATOM(indexedExpression)
  // 11: ATOM(referenceExpression)
  // 12: ATOM(variableExpression)
  // 13: PREFIX(unaryExpression)
  // 14: ATOM(literalExpression)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = lambdaExpression(b, l + 1);
    if (!r) r = mapExpression(b, l + 1);
    if (!r) r = sequenceExpression(b, l + 1);
    if (!r) r = parenthesizedExpression(b, l + 1);
    if (!r) r = variableAssignmentExpression(b, l + 1);
    if (!r) r = newArrayExpression(b, l + 1);
    if (!r) r = newExpression(b, l + 1);
    if (!r) r = indexedExpression(b, l + 1);
    if (!r) r = referenceExpression(b, l + 1);
    if (!r) r = variableExpression(b, l + 1);
    if (!r) r = unaryExpression(b, l + 1);
    if (!r) r = literalExpression(b, l + 1);
    p = r;
    r = r && expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 5 && consumeTokenSmart(b, QUESTION)) {
        r = report_error_(b, expression(b, l, 5));
        r = conditionalExpressionTail(b, l + 1) && r;
        exit_section_(b, l, m, CONDITIONAL_EXPRESSION, r, true, null);
      }
      else if (g < 6 && binaryOperations(b, l + 1)) {
        r = expression(b, l, 6);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 9 && leftMarkerIs(b, REFERENCE_EXPRESSION) && methodCallExpression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, METHOD_CALL_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // ':' '[' expression "]"
  public static boolean lambdaExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lambdaExpression")) return false;
    if (!nextTokenIsFast(b, COLON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, COLON);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, LBRACKET));
    r = p && report_error_(b, expression(b, l + 1, -1)) && r;
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, LAMBDA_EXPRESSION, r, p, null);
    return r || p;
  }

  // ('#{' | '#@' fqnTypeExpression '@{') mapExpressionSequence '}'
  public static boolean mapExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<map expression>");
    r = mapExpression_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, mapExpressionSequence(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, MAP_EXPRESSION, r, p, null);
    return r || p;
  }

  // '#{' | '#@' fqnTypeExpression '@{'
  private static boolean mapExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, "#{");
    if (!r) r = mapExpression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '#@' fqnTypeExpression '@{'
  private static boolean mapExpression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpression_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, "#@");
    r = r && fqnTypeExpression(b, l + 1);
    r = r && consumeToken(b, "@{");
    exit_section_(b, m, null, r);
    return r;
  }

  // '{' expressionSequenceRequired '}'
  public static boolean sequenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "sequenceExpression")) return false;
    if (!nextTokenIsFast(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, expressionSequenceRequired(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, SEQUENCE_EXPRESSION, r, p, null);
    return r || p;
  }

  // '(' expression ')'
  public static boolean parenthesizedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedExpression")) return false;
    if (!nextTokenIsFast(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, PARENTHESIZED_EXPRESSION, r, p, null);
    return r || p;
  }

  // '#' IDENTIFIER '=' expression
  public static boolean variableAssignmentExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableAssignmentExpression")) return false;
    if (!nextTokenIsFast(b, HASH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, HASH);
    r = r && consumeToken(b, IDENTIFIER);
    r = r && consumeToken(b, EQ);
    p = r; // pin = 3
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, VARIABLE_ASSIGNMENT_EXPRESSION, r, p, null);
    return r || p;
  }

  // "new" fqnTypeExpression arrayConstructorExpression
  public static boolean newArrayExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newArrayExpression")) return false;
    if (!nextTokenIsFast(b, NEW_KEYWORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, NEW_KEYWORD);
    r = r && fqnTypeExpression(b, l + 1);
    r = r && arrayConstructorExpression(b, l + 1);
    exit_section_(b, m, NEW_ARRAY_EXPRESSION, r);
    return r;
  }

  // "new" fqnTypeExpression constructorExpression
  public static boolean newExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression")) return false;
    if (!nextTokenIsFast(b, NEW_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, NEW_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, fqnTypeExpression(b, l + 1));
    r = p && constructorExpression(b, l + 1) && r;
    exit_section_(b, l, m, NEW_EXPRESSION, r, p, null);
    return r || p;
  }

  // '(' parameterList ')'
  private static boolean methodCallExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodCallExpression_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LPARENTH);
    p = r; // pin = '\('
    r = r && report_error_(b, parameterList(b, l + 1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  // (referenceExpression | variableExpression) '[' expression ']'
  public static boolean indexedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexedExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<indexed expression>");
    r = indexedExpression_0(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, INDEXED_EXPRESSION, r, p, null);
    return r || p;
  }

  // referenceExpression | variableExpression
  private static boolean indexedExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexedExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = referenceExpression(b, l + 1);
    if (!r) r = variableExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (variableExpression | ('@')? IDENTIFIER)
  //                         ('.' IDENTIFIER)* ('@' IDENTIFIER)?
  //                         ('.'  selectionExpression)?
  //                         ('.'  projectionExpression )?
  public static boolean referenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, "<reference expression>");
    r = referenceExpression_0(b, l + 1);
    r = r && referenceExpression_1(b, l + 1);
    r = r && referenceExpression_2(b, l + 1);
    r = r && referenceExpression_3(b, l + 1);
    r = r && referenceExpression_4(b, l + 1);
    exit_section_(b, l, m, REFERENCE_EXPRESSION, r, false, null);
    return r;
  }

  // variableExpression | ('@')? IDENTIFIER
  private static boolean referenceExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableExpression(b, l + 1);
    if (!r) r = referenceExpression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('@')? IDENTIFIER
  private static boolean referenceExpression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = referenceExpression_0_1_0(b, l + 1);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('@')?
  private static boolean referenceExpression_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_0_1_0")) return false;
    referenceExpression_0_1_0_0(b, l + 1);
    return true;
  }

  // ('@')
  private static boolean referenceExpression_0_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_0_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, AT);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' IDENTIFIER)*
  private static boolean referenceExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!referenceExpression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "referenceExpression_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // '.' IDENTIFIER
  private static boolean referenceExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('@' IDENTIFIER)?
  private static boolean referenceExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_2")) return false;
    referenceExpression_2_0(b, l + 1);
    return true;
  }

  // '@' IDENTIFIER
  private static boolean referenceExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, AT);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.'  selectionExpression)?
  private static boolean referenceExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_3")) return false;
    referenceExpression_3_0(b, l + 1);
    return true;
  }

  // '.'  selectionExpression
  private static boolean referenceExpression_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && selectionExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.'  projectionExpression )?
  private static boolean referenceExpression_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_4")) return false;
    referenceExpression_4_0(b, l + 1);
    return true;
  }

  // '.'  projectionExpression
  private static boolean referenceExpression_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && projectionExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '#' IDENTIFIER
  public static boolean variableExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableExpression")) return false;
    if (!nextTokenIsFast(b, HASH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, HASH);
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, m, VARIABLE_EXPRESSION, r);
    return r;
  }

  public static boolean unaryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryOperator(b, l + 1);
    p = r;
    r = p && expression(b, l, 13);
    exit_section_(b, l, m, UNARY_EXPRESSION, r, p, null);
    return r || p;
  }

  // numberLiteralExpression |
  //                       textLiteralExpression |
  //                       booleanLiteralExpression |
  //                       "null"
  public static boolean literalExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, "<literal expression>");
    r = numberLiteralExpression(b, l + 1);
    if (!r) r = textLiteralExpression(b, l + 1);
    if (!r) r = booleanLiteralExpression(b, l + 1);
    if (!r) r = consumeTokenSmart(b, NULL_KEYWORD);
    exit_section_(b, l, m, LITERAL_EXPRESSION, r, false, null);
    return r;
  }

  final static Parser rootRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return rootRecover(b, l + 1);
    }
  };
}
