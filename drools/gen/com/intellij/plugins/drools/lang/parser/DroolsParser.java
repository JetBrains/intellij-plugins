// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DroolsParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return compilationUnit(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(INSERT_LOGICAL_RHS_STATEMENT, INSERT_RHS_STATEMENT, JAVA_RHS_STATEMENT, MODIFY_RHS_STATEMENT,
      RETRACT_RHS_STATEMENT, UPDATE_RHS_STATEMENT),
    create_token_set_(ADDITIVE_EXPR, AND_EXPR, ASSIGNMENT_EXPR, BOOLEAN_LITERAL,
      CAST_EXPR, CONDITIONAL_AND_EXPR, CONDITIONAL_EXPR, CONDITIONAL_OR_EXPR,
      EQUALITY_EXPR, EXCLUSIVE_OR_EXPR, EXPRESSION, INCLUSIVE_OR_EXPR,
      INSTANCE_OF_EXPR, IN_EXPR, MODIFY_PAR_EXPR, MULTIPLICATIVE_EXPR,
      NULL_LITERAL, NUMBER_LITERAL, PAR_EXPR, PRIMARY_EXPR,
      RELATIONAL_EXPR, SHIFT_EXPR, STRING_LITERAL, UNARY_2_EXPR,
      UNARY_ASSIGN_EXPR, UNARY_EXPR, UNARY_NOT_PLUS_MINUS_EXPR),
  };

  /* ********************************************************** */
  // label? functionName accumulateParameters
  public static boolean accumulateFunction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateFunction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ACCUMULATE_FUNCTION, "<accumulate function>");
    r = accumulateFunction_0(b, l + 1);
    r = r && functionName(b, l + 1);
    r = r && accumulateParameters(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // label?
  private static boolean accumulateFunction_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateFunction_0")) return false;
    label(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // label accumulateFunction
  public static boolean accumulateFunctionBinding(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateFunctionBinding")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = label(b, l + 1);
    r = r && accumulateFunction(b, l + 1);
    exit_section_(b, m, ACCUMULATE_FUNCTION_BINDING, r);
    return r;
  }

  /* ********************************************************** */
  // "(" (conditionalExpr ("," conditionalExpr)* )? ")"
  public static boolean accumulateParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateParameters")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && accumulateParameters_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ACCUMULATE_PARAMETERS, r);
    return r;
  }

  // (conditionalExpr ("," conditionalExpr)* )?
  private static boolean accumulateParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateParameters_1")) return false;
    accumulateParameters_1_0(b, l + 1);
    return true;
  }

  // conditionalExpr ("," conditionalExpr)*
  private static boolean accumulateParameters_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateParameters_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, 0);
    r = r && accumulateParameters_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("," conditionalExpr)*
  private static boolean accumulateParameters_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateParameters_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!accumulateParameters_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "accumulateParameters_1_0_1", c)) break;
    }
    return true;
  }

  // "," conditionalExpr
  private static boolean accumulateParameters_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "accumulateParameters_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, 0);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // singleRestriction ( "&&" singleRestriction ) *
  static boolean andRestriction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "andRestriction")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = singleRestriction(b, l + 1);
    r = r && andRestriction_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "&&" singleRestriction ) *
  private static boolean andRestriction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "andRestriction_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!andRestriction_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "andRestriction_1", c)) break;
    }
    return true;
  }

  // "&&" singleRestriction
  private static boolean andRestriction_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "andRestriction_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COND_AND);
    r = r && singleRestriction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "@" identifier (elementValuePairs | chunk )?
  public static boolean annotation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation")) return false;
    if (!nextTokenIs(b, OP_AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_AT);
    r = r && identifier(b, l + 1);
    r = r && annotation_2(b, l + 1);
    exit_section_(b, m, ANNOTATION, r);
    return r;
  }

  // (elementValuePairs | chunk )?
  private static boolean annotation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2")) return false;
    annotation_2_0(b, l + 1);
    return true;
  }

  // elementValuePairs | chunk
  private static boolean annotation_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "annotation_2_0")) return false;
    boolean r;
    r = elementValuePairs(b, l + 1);
    if (!r) r = chunk(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // "(" expressionList? ")"
  public static boolean arguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && arguments_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ARGUMENTS, r);
    return r;
  }

  // expressionList?
  private static boolean arguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "[" ( "]" ( "[" "]" )* arrayInitializer ) | ( expression "]" ( "[" expression "]" )* ( "[" "]" )* )
  public static boolean arrayCreatorRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_CREATOR_REST, "<array creator rest>");
    r = arrayCreatorRest_0(b, l + 1);
    if (!r) r = arrayCreatorRest_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "[" ( "]" ( "[" "]" )* arrayInitializer )
  private static boolean arrayCreatorRest_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && arrayCreatorRest_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "]" ( "[" "]" )* arrayInitializer
  private static boolean arrayCreatorRest_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RBRACKET);
    r = r && arrayCreatorRest_0_1_1(b, l + 1);
    r = r && arrayInitializer(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "[" "]" )*
  private static boolean arrayCreatorRest_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_0_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayCreatorRest_0_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayCreatorRest_0_1_1", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean arrayCreatorRest_0_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_0_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression "]" ( "[" expression "]" )* ( "[" "]" )*
  private static boolean arrayCreatorRest_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACKET);
    r = r && arrayCreatorRest_1_2(b, l + 1);
    r = r && arrayCreatorRest_1_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "[" expression "]" )*
  private static boolean arrayCreatorRest_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_1_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayCreatorRest_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayCreatorRest_1_2", c)) break;
    }
    return true;
  }

  // "[" expression "]"
  private static boolean arrayCreatorRest_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "[" "]" )*
  private static boolean arrayCreatorRest_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_1_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayCreatorRest_1_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayCreatorRest_1_3", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean arrayCreatorRest_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayCreatorRest_1_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "{" ( variableInitializer ( "," variableInitializer )* ","? )? "}"
  public static boolean arrayInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayInitializer")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && arrayInitializer_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, ARRAY_INITIALIZER, r);
    return r;
  }

  // ( variableInitializer ( "," variableInitializer )* ","? )?
  private static boolean arrayInitializer_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayInitializer_1")) return false;
    arrayInitializer_1_0(b, l + 1);
    return true;
  }

  // variableInitializer ( "," variableInitializer )* ","?
  private static boolean arrayInitializer_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayInitializer_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variableInitializer(b, l + 1);
    r = r && arrayInitializer_1_0_1(b, l + 1);
    r = r && arrayInitializer_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "," variableInitializer )*
  private static boolean arrayInitializer_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayInitializer_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayInitializer_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayInitializer_1_0_1", c)) break;
    }
    return true;
  }

  // "," variableInitializer
  private static boolean arrayInitializer_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayInitializer_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && variableInitializer(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ","?
  private static boolean arrayInitializer_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayInitializer_1_0_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // "=" | "+=" | "-=" | "*=" | "/=" | "&=" | "|=" | "^=" | "%=" | "<<=" | ">>>=" | ">>="
  public static boolean assignmentOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentOperator")) return false;
    if (!nextTokenIs(b, "<assignment operator>", OP_ASSIGN, OP_BIT_AND_ASSIGN,
      OP_BIT_OR_ASSIGN, OP_BIT_XOR_ASSIGN, OP_BSR_ASSIGN, OP_DIV_ASSIGN, OP_MINUS_ASSIGN, OP_MUL_ASSIGN,
      OP_PLUS_ASSIGN, OP_REMAINDER_ASSIGN, OP_SL_ASSIGN, OP_SR_ASSIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_OPERATOR, "<assignment operator>");
    r = consumeToken(b, OP_ASSIGN);
    if (!r) r = consumeToken(b, OP_PLUS_ASSIGN);
    if (!r) r = consumeToken(b, OP_MINUS_ASSIGN);
    if (!r) r = consumeToken(b, OP_MUL_ASSIGN);
    if (!r) r = consumeToken(b, OP_DIV_ASSIGN);
    if (!r) r = consumeToken(b, OP_BIT_AND_ASSIGN);
    if (!r) r = consumeToken(b, OP_BIT_OR_ASSIGN);
    if (!r) r = consumeToken(b, OP_BIT_XOR_ASSIGN);
    if (!r) r = consumeToken(b, OP_REMAINDER_ASSIGN);
    if (!r) r = consumeToken(b, OP_SL_ASSIGN);
    if (!r) r = consumeToken(b, OP_BSR_ASSIGN);
    if (!r) r = consumeToken(b, OP_SR_ASSIGN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // conditionalAttrName conditionalExpr
  //     | booleanAttrName  booleanLiteral?
  //     | stringAttrName stringLiteral
  //     | stringsAttrName stringLiteral ( "," stringLiteral)*
  //     | decimalOrChunkAttrName ( decimal | chunk )
  public static boolean attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute")) return false;
    if (!nextTokenIs(b, "<attribute>", ACTIVATION_GROUP, AGENDA_GROUP,
      AUTO_FOCUS, CALENDARS, DATE_EFFECTIVE, DATE_EXPIRES, DIALECT, DURATION,
      ENABLED, LOCK_ON_ACTIVE, NO_LOOP, REFRACT, RULEFLOW_GROUP, SALIENCE, TIMER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTRIBUTE, "<attribute>");
    r = attribute_0(b, l + 1);
    if (!r) r = attribute_1(b, l + 1);
    if (!r) r = attribute_2(b, l + 1);
    if (!r) r = attribute_3(b, l + 1);
    if (!r) r = attribute_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // conditionalAttrName conditionalExpr
  private static boolean attribute_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditionalAttrName(b, l + 1);
    r = r && expression(b, l + 1, 0);
    exit_section_(b, m, null, r);
    return r;
  }

  // booleanAttrName  booleanLiteral?
  private static boolean attribute_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = booleanAttrName(b, l + 1);
    r = r && attribute_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // booleanLiteral?
  private static boolean attribute_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_1_1")) return false;
    booleanLiteral(b, l + 1);
    return true;
  }

  // stringAttrName stringLiteral
  private static boolean attribute_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringAttrName(b, l + 1);
    r = r && stringLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringsAttrName stringLiteral ( "," stringLiteral)*
  private static boolean attribute_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringsAttrName(b, l + 1);
    r = r && stringLiteral(b, l + 1);
    r = r && attribute_3_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "," stringLiteral)*
  private static boolean attribute_3_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_3_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!attribute_3_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "attribute_3_2", c)) break;
    }
    return true;
  }

  // "," stringLiteral
  private static boolean attribute_3_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_3_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && stringLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // decimalOrChunkAttrName ( decimal | chunk )
  private static boolean attribute_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = decimalOrChunkAttrName(b, l + 1);
    r = r && attribute_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // decimal | chunk
  private static boolean attribute_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_4_1")) return false;
    boolean r;
    r = decimal(b, l + 1);
    if (!r) r = chunk(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // BLOCK_EXPRESSION
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!nextTokenIs(b, BLOCK_EXPRESSION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BLOCK_EXPRESSION);
    exit_section_(b, m, BLOCK, r);
    return r;
  }

  /* ********************************************************** */
  // "no-loop" | "auto-focus" | "lock-on-active"| "refract"
  static boolean booleanAttrName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "booleanAttrName")) return false;
    if (!nextTokenIs(b, "", AUTO_FOCUS, LOCK_ON_ACTIVE, NO_LOOP, REFRACT)) return false;
    boolean r;
    r = consumeToken(b, NO_LOOP);
    if (!r) r = consumeToken(b, AUTO_FOCUS);
    if (!r) r = consumeToken(b, LOCK_ON_ACTIVE);
    if (!r) r = consumeToken(b, REFRACT);
    return r;
  }

  /* ********************************************************** */
  // FALSE | TRUE
  public static boolean booleanLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "booleanLiteral")) return false;
    if (!nextTokenIs(b, "<boolean literal>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOLEAN_LITERAL, "<boolean literal>");
    r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, TRUE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "(" CHUNK_BLOCK ")"
  public static boolean chunk(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "chunk")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LPAREN, CHUNK_BLOCK, RPAREN);
    exit_section_(b, m, CHUNK, r);
    return r;
  }

  /* ********************************************************** */
  // arguments
  public static boolean classCreatorRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classCreatorRest")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arguments(b, l + 1);
    exit_section_(b, m, CLASS_CREATOR_REST, r);
    return r;
  }

  /* ********************************************************** */
  // packageStatement? ( statement ";"? )*
  static boolean compilationUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compilationUnit_0(b, l + 1);
    r = r && compilationUnit_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // packageStatement?
  private static boolean compilationUnit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_0")) return false;
    packageStatement(b, l + 1);
    return true;
  }

  // ( statement ";"? )*
  private static boolean compilationUnit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!compilationUnit_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "compilationUnit_1", c)) break;
    }
    return true;
  }

  // statement ";"?
  private static boolean compilationUnit_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement(b, l + 1);
    r = r && compilationUnit_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ";"?
  private static boolean compilationUnit_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilationUnit_1_0_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // salience | enabled
  static boolean conditionalAttrName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalAttrName")) return false;
    if (!nextTokenIs(b, "", ENABLED, SALIENCE)) return false;
    boolean r;
    r = consumeToken(b, SALIENCE);
    if (!r) r = consumeToken(b, ENABLED);
    return r;
  }

  /* ********************************************************** */
  // ("(" lhsPatternBind (("or"|"||"| "and"|"&&") lhsPatternBind)* ")") | lhsPatternBind
  public static boolean conditionalElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalElement")) return false;
    if (!nextTokenIs(b, "<conditional element>", JAVA_IDENTIFIER, LPAREN, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_ELEMENT, "<conditional element>");
    r = conditionalElement_0(b, l + 1);
    if (!r) r = lhsPatternBind(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "(" lhsPatternBind (("or"|"||"| "and"|"&&") lhsPatternBind)* ")"
  private static boolean conditionalElement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalElement_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && lhsPatternBind(b, l + 1);
    r = r && conditionalElement_0_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (("or"|"||"| "and"|"&&") lhsPatternBind)*
  private static boolean conditionalElement_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalElement_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!conditionalElement_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "conditionalElement_0_2", c)) break;
    }
    return true;
  }

  // ("or"|"||"| "and"|"&&") lhsPatternBind
  private static boolean conditionalElement_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalElement_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = conditionalElement_0_2_0_0(b, l + 1);
    r = r && lhsPatternBind(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "or"|"||"| "and"|"&&"
  private static boolean conditionalElement_0_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalElement_0_2_0_0")) return false;
    boolean r;
    r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, OP_COND_OR);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OP_COND_AND);
    return r;
  }

  /* ********************************************************** */
  // nameId
  public static boolean consequenceId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "consequenceId")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nameId(b, l + 1);
    exit_section_(b, m, CONSEQUENCE_ID, r);
    return r;
  }

  /* ********************************************************** */
  // expression
  public static boolean constraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constraint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTRAINT, "<constraint>");
    r = expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // constraint ("," constraint)*
  static boolean constraints(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constraints")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constraint(b, l + 1);
    r = r && constraints_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("," constraint)*
  private static boolean constraints_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constraints_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!constraints_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constraints_1", c)) break;
    }
    return true;
  }

  // "," constraint
  private static boolean constraints_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constraints_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && constraint(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // createdQualifiedIdentifier | primitiveType
  static boolean createdName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "createdName")) return false;
    if (!nextTokenIs(b, "", BOOLEAN, BYTE,
      CHAR, DOUBLE, FLOAT, INT, JAVA_IDENTIFIER, LONG, SHORT, VOID)) return false;
    boolean r;
    r = createdQualifiedIdentifier(b, l + 1);
    if (!r) r = primitiveType(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // qualifiedIdentifier typeArguments?
  public static boolean createdQualifiedIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "createdQualifiedIdentifier")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier(b, l + 1);
    r = r && createdQualifiedIdentifier_1(b, l + 1);
    exit_section_(b, m, CREATED_QUALIFIED_IDENTIFIER, r);
    return r;
  }

  // typeArguments?
  private static boolean createdQualifiedIdentifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "createdQualifiedIdentifier_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // nonWildcardTypeArguments? createdName ( arrayCreatorRest | classCreatorRest )
  public static boolean creator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "creator")) return false;
    if (!nextTokenIs(b, "<creator>", BOOLEAN, BYTE,
      CHAR, DOUBLE, FLOAT, INT, JAVA_IDENTIFIER, LONG,
      OP_LESS, SHORT, VOID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CREATOR, "<creator>");
    r = creator_0(b, l + 1);
    r = r && createdName(b, l + 1);
    r = r && creator_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // nonWildcardTypeArguments?
  private static boolean creator_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "creator_0")) return false;
    nonWildcardTypeArguments(b, l + 1);
    return true;
  }

  // arrayCreatorRest | classCreatorRest
  private static boolean creator_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "creator_2")) return false;
    boolean r;
    r = arrayCreatorRest(b, l + 1);
    if (!r) r = classCreatorRest(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // INT_TOKEN  integerTypeSuffix?
  public static boolean decimal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "decimal")) return false;
    if (!nextTokenIs(b, INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INT_TOKEN);
    r = r && decimal_1(b, l + 1);
    exit_section_(b, m, DECIMAL, r);
    return r;
  }

  // integerTypeSuffix?
  private static boolean decimal_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "decimal_1")) return false;
    integerTypeSuffix(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "timer" | "duration"
  static boolean decimalOrChunkAttrName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "decimalOrChunkAttrName")) return false;
    if (!nextTokenIs(b, "", DURATION, TIMER)) return false;
    boolean r;
    r = consumeToken(b, TIMER);
    if (!r) r = consumeToken(b, DURATION);
    return r;
  }

  /* ********************************************************** */
  // "declare" (entryPointDeclaration | windowDeclaration | enumDeclaration | typeDeclaration) "end"
  public static boolean declareStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declareStatement")) return false;
    if (!nextTokenIs(b, DECLARE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DECLARE_STATEMENT, null);
    r = consumeToken(b, DECLARE);
    p = r; // pin = 1
    r = r && report_error_(b, declareStatement_1(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // entryPointDeclaration | windowDeclaration | enumDeclaration | typeDeclaration
  private static boolean declareStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declareStatement_1")) return false;
    boolean r;
    r = entryPointDeclaration(b, l + 1);
    if (!r) r = windowDeclaration(b, l + 1);
    if (!r) r = enumDeclaration(b, l + 1);
    if (!r) r = typeDeclaration(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // elementValueArrayInitializer | conditionalExpr
  public static boolean elementValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ELEMENT_VALUE, "<element value>");
    r = elementValueArrayInitializer(b, l + 1);
    if (!r) r = expression(b, l + 1, 0);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "{" (elementValue ("," elementValue )*)? "}"
  public static boolean elementValueArrayInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && elementValueArrayInitializer_1(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, ELEMENT_VALUE_ARRAY_INITIALIZER, r);
    return r;
  }

  // (elementValue ("," elementValue )*)?
  private static boolean elementValueArrayInitializer_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1")) return false;
    elementValueArrayInitializer_1_0(b, l + 1);
    return true;
  }

  // elementValue ("," elementValue )*
  private static boolean elementValueArrayInitializer_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elementValue(b, l + 1);
    r = r && elementValueArrayInitializer_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("," elementValue )*
  private static boolean elementValueArrayInitializer_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!elementValueArrayInitializer_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "elementValueArrayInitializer_1_0_1", c)) break;
    }
    return true;
  }

  // "," elementValue
  private static boolean elementValueArrayInitializer_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValueArrayInitializer_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && elementValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier "=" elementValue
  public static boolean elementValuePair(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePair")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && consumeToken(b, OP_ASSIGN);
    r = r && elementValue(b, l + 1);
    exit_section_(b, m, ELEMENT_VALUE_PAIR, r);
    return r;
  }

  /* ********************************************************** */
  // "(" elementValuePair ("," elementValuePair)* ")"
  public static boolean elementValuePairs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePairs")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && elementValuePair(b, l + 1);
    r = r && elementValuePairs_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ELEMENT_VALUE_PAIRS, r);
    return r;
  }

  // ("," elementValuePair)*
  private static boolean elementValuePairs_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePairs_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!elementValuePairs_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "elementValuePairs_2", c)) break;
    }
    return true;
  }

  // "," elementValuePair
  private static boolean elementValuePairs_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elementValuePairs_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && elementValuePair(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "entry-point" entryPointName annotation*
  public static boolean entryPointDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entryPointDeclaration")) return false;
    if (!nextTokenIs(b, ENTRY_POINT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_POINT_DECLARATION, null);
    r = consumeToken(b, ENTRY_POINT);
    r = r && entryPointName(b, l + 1);
    p = r; // pin = 2
    r = r && entryPointDeclaration_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // annotation*
  private static boolean entryPointDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entryPointDeclaration_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "entryPointDeclaration_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // stringId
  public static boolean entryPointName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entryPointName")) return false;
    if (!nextTokenIs(b, "<entry point name>", CHARACTER_LITERAL, JAVA_IDENTIFIER, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENTRY_POINT_NAME, "<entry point name>");
    r = stringId(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "enum" qualifiedName annotation* enumerative+ field*
  public static boolean enumDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_DECLARATION, "<enum declaration>");
    r = consumeToken(b, "enum");
    r = r && qualifiedName(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, enumDeclaration_2(b, l + 1));
    r = p && report_error_(b, enumDeclaration_3(b, l + 1)) && r;
    r = p && enumDeclaration_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // annotation*
  private static boolean enumDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclaration_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclaration_2", c)) break;
    }
    return true;
  }

  // enumerative+
  private static boolean enumDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclaration_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumerative(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!enumerative(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclaration_3", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // field*
  private static boolean enumDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDeclaration_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!field(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDeclaration_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // identifier ( "(" exprList ")" )?
  public static boolean enumerative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumerative")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && enumerative_1(b, l + 1);
    exit_section_(b, m, ENUMERATIVE, r);
    return r;
  }

  // ( "(" exprList ")" )?
  private static boolean enumerative_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumerative_1")) return false;
    enumerative_1_0(b, l + 1);
    return true;
  }

  // "(" exprList ")"
  private static boolean enumerative_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumerative_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && exprList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "==" | "!=" | "<=" | ">=" | "<" | ">"
  static boolean equality(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equality")) return false;
    if (!nextTokenIs(b, "", OP_EQ, OP_GREATER,
      OP_GREATER_OR_EQUAL, OP_LESS, OP_LESS_OR_EQUAL, OP_NOT_EQ)) return false;
    boolean r;
    r = consumeToken(b, OP_EQ);
    if (!r) r = consumeToken(b, OP_NOT_EQ);
    if (!r) r = consumeToken(b, OP_LESS_OR_EQUAL);
    if (!r) r = consumeToken(b, OP_GREATER_OR_EQUAL);
    if (!r) r = consumeToken(b, OP_LESS);
    if (!r) r = consumeToken(b, OP_GREATER);
    return r;
  }

  /* ********************************************************** */
  // nonWildcardTypeArguments arguments
  public static boolean explicitGenericInvocation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicitGenericInvocation")) return false;
    if (!nextTokenIs(b, OP_LESS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonWildcardTypeArguments(b, l + 1);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, EXPLICIT_GENERIC_INVOCATION, r);
    return r;
  }

  /* ********************************************************** */
  // ( "super" superSuffix ) | identifier arguments
  public static boolean explicitGenericInvocationSuffix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicitGenericInvocationSuffix")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPLICIT_GENERIC_INVOCATION_SUFFIX, "<explicit generic invocation suffix>");
    r = explicitGenericInvocationSuffix_0(b, l + 1);
    if (!r) r = explicitGenericInvocationSuffix_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "super" superSuffix
  private static boolean explicitGenericInvocationSuffix_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicitGenericInvocationSuffix_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "super");
    r = r && superSuffix(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // identifier arguments
  private static boolean explicitGenericInvocationSuffix_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "explicitGenericInvocationSuffix_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // () expression ("," expression)*
  static boolean exprList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = exprList_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && exprList_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ()
  private static boolean exprList_0(PsiBuilder b, int l) {
    return true;
  }

  // ("," expression)*
  private static boolean exprList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!exprList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exprList_2", c)) break;
    }
    return true;
  }

  // "," expression
  private static boolean exprList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprList_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // expression ( "," expression )*
  static boolean expressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = expression(b, l + 1, -1);
    p = r; // pin = 1
    r = r && expressionList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( "," expression )*
  private static boolean expressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionList_1", c)) break;
    }
    return true;
  }

  // "," expression
  private static boolean expressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // fieldName ":" fieldType ("=" conditionalExpr)? annotation* ";"?
  public static boolean field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD, null);
    r = fieldName(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && report_error_(b, fieldType(b, l + 1)) && r;
    r = p && report_error_(b, field_3(b, l + 1)) && r;
    r = p && report_error_(b, field_4(b, l + 1)) && r;
    r = p && field_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ("=" conditionalExpr)?
  private static boolean field_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_3")) return false;
    field_3_0(b, l + 1);
    return true;
  }

  // "=" conditionalExpr
  private static boolean field_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_ASSIGN);
    r = r && expression(b, l + 1, 0);
    exit_section_(b, m, null, r);
    return r;
  }

  // annotation*
  private static boolean field_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "field_4", c)) break;
    }
    return true;
  }

  // ";"?
  private static boolean field_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_5")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // JAVA_IDENTIFIER
  public static boolean fieldName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldName")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JAVA_IDENTIFIER);
    exit_section_(b, m, FIELD_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // primitiveType | type
  public static boolean fieldType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldType")) return false;
    if (!nextTokenIs(b, "<field type>", BOOLEAN, BYTE,
      CHAR, DOUBLE, FLOAT, INT, JAVA_IDENTIFIER, LONG, SHORT, VOID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_TYPE, "<field type>");
    r = primitiveType(b, l + 1);
    if (!r) r = type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // label identifier "(" parameters ")"
  public static boolean filterDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "filterDef")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = label(b, l + 1);
    r = r && identifier(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && parameters(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, FILTER_DEF, r);
    return r;
  }

  /* ********************************************************** */
  // "accumulate" "(" lhsAnd "," ( "init" chunk "," "action" chunk "," ( "reverse" chunk ",")? "result" chunk | accumulateFunction ) ")"
  public static boolean fromAccumulate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromAccumulate")) return false;
    if (!nextTokenIs(b, ACCUMULATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FROM_ACCUMULATE, null);
    r = consumeTokens(b, 1, ACCUMULATE, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, lhsAnd(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COMMA)) && r;
    r = p && report_error_(b, fromAccumulate_4(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // "init" chunk "," "action" chunk "," ( "reverse" chunk ",")? "result" chunk | accumulateFunction
  private static boolean fromAccumulate_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromAccumulate_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fromAccumulate_4_0(b, l + 1);
    if (!r) r = accumulateFunction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "init" chunk "," "action" chunk "," ( "reverse" chunk ",")? "result" chunk
  private static boolean fromAccumulate_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromAccumulate_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INIT);
    r = r && chunk(b, l + 1);
    r = r && consumeTokens(b, 0, COMMA, ACTION);
    r = r && chunk(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && fromAccumulate_4_0_6(b, l + 1);
    r = r && consumeToken(b, RESULT);
    r = r && chunk(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "reverse" chunk ",")?
  private static boolean fromAccumulate_4_0_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromAccumulate_4_0_6")) return false;
    fromAccumulate_4_0_6_0(b, l + 1);
    return true;
  }

  // "reverse" chunk ","
  private static boolean fromAccumulate_4_0_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromAccumulate_4_0_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REVERSE);
    r = r && chunk(b, l + 1);
    r = r && consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "collect" "(" lhsPatternBind ")"
  public static boolean fromCollect(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromCollect")) return false;
    if (!nextTokenIs(b, COLLECT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COLLECT, LPAREN);
    r = r && lhsPatternBind(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, FROM_COLLECT, r);
    return r;
  }

  /* ********************************************************** */
  // "entry-point" stringId
  public static boolean fromEntryPoint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromEntryPoint")) return false;
    if (!nextTokenIs(b, ENTRY_POINT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ENTRY_POINT);
    r = r && stringId(b, l + 1);
    exit_section_(b, m, FROM_ENTRY_POINT, r);
    return r;
  }

  /* ********************************************************** */
  // expression
  public static boolean fromExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FROM_EXPRESSION, "<from expression>");
    r = expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "window" windowId
  public static boolean fromWindow(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromWindow")) return false;
    if (!nextTokenIs(b, WINDOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FROM_WINDOW, null);
    r = consumeToken(b, WINDOW);
    p = r; // pin = 1
    r = r && windowId(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "average" | "min"| "max"| "count" | "sum"| "collectList"| "collectSet"
  static boolean functionName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionName")) return false;
    boolean r;
    r = consumeToken(b, "average");
    if (!r) r = consumeToken(b, "min");
    if (!r) r = consumeToken(b, "max");
    if (!r) r = consumeToken(b, "count");
    if (!r) r = consumeToken(b, "sum");
    if (!r) r = consumeToken(b, "collectList");
    if (!r) r = consumeToken(b, "collectSet");
    return r;
  }

  /* ********************************************************** */
  // "function" ( primitiveType ("[" "]")* | type)? nameId parameters block
  public static boolean functionStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionStatement")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_STATEMENT, null);
    r = consumeToken(b, FUNCTION);
    r = r && functionStatement_1(b, l + 1);
    r = r && nameId(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, parameters(b, l + 1));
    r = p && block(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( primitiveType ("[" "]")* | type)?
  private static boolean functionStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionStatement_1")) return false;
    functionStatement_1_0(b, l + 1);
    return true;
  }

  // primitiveType ("[" "]")* | type
  private static boolean functionStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionStatement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionStatement_1_0_0(b, l + 1);
    if (!r) r = type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // primitiveType ("[" "]")*
  private static boolean functionStatement_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionStatement_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = primitiveType(b, l + 1);
    r = r && functionStatement_1_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("[" "]")*
  private static boolean functionStatement_1_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionStatement_1_0_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!functionStatement_1_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionStatement_1_0_0_1", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean functionStatement_1_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionStatement_1_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "global" varType nameId
  public static boolean globalStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "globalStatement")) return false;
    if (!nextTokenIs(b, GLOBAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GLOBAL);
    r = r && varType(b, l + 1);
    r = r && nameId(b, l + 1);
    exit_section_(b, m, GLOBAL_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // JAVA_IDENTIFIER
  public static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JAVA_IDENTIFIER);
    exit_section_(b, m, IDENTIFIER, r);
    return r;
  }

  /* ********************************************************** */
  // ( "[" "]" )* "." "class" | "[" expression "]" | arguments
  public static boolean identifierSuffix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSuffix")) return false;
    if (!nextTokenIs(b, "<identifier suffix>", DOT, LBRACKET, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IDENTIFIER_SUFFIX, "<identifier suffix>");
    r = identifierSuffix_0(b, l + 1);
    if (!r) r = identifierSuffix_1(b, l + 1);
    if (!r) r = arguments(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( "[" "]" )* "." "class"
  private static boolean identifierSuffix_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSuffix_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifierSuffix_0_0(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && consumeToken(b, "class");
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "[" "]" )*
  private static boolean identifierSuffix_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSuffix_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!identifierSuffix_0_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "identifierSuffix_0_0", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean identifierSuffix_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSuffix_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // "[" expression "]"
  private static boolean identifierSuffix_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierSuffix_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // qualifiedIdentifier ("." "*")?
  public static boolean importQualifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importQualifier")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier(b, l + 1);
    r = r && importQualifier_1(b, l + 1);
    exit_section_(b, m, IMPORT_QUALIFIER, r);
    return r;
  }

  // ("." "*")?
  private static boolean importQualifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importQualifier_1")) return false;
    importQualifier_1_0(b, l + 1);
    return true;
  }

  // "." "*"
  private static boolean importQualifier_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importQualifier_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, OP_MUL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "import" ("function"|"static")? importQualifier
  public static boolean importStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPORT);
    r = r && importStatement_1(b, l + 1);
    r = r && importQualifier(b, l + 1);
    exit_section_(b, m, IMPORT_STATEMENT, r);
    return r;
  }

  // ("function"|"static")?
  private static boolean importStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_1")) return false;
    importStatement_1_0(b, l + 1);
    return true;
  }

  // "function"|"static"
  private static boolean importStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_1_0")) return false;
    boolean r;
    r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, "static");
    return r;
  }

  /* ********************************************************** */
  // "[" expressionList? "]"
  static boolean inlineListExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inlineListExpr")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && inlineListExpr_1(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean inlineListExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inlineListExpr_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "[" mapExpressionList "]"
  static boolean inlineMapExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inlineMapExpr")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && mapExpressionList(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier classCreatorRest
  public static boolean innerCreator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "innerCreator")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && classCreatorRest(b, l + 1);
    exit_section_(b, m, INNER_CREATOR, r);
    return r;
  }

  /* ********************************************************** */
  // "insertLogical" "(" expression (',' expression)*  ")"
  public static boolean insertLogicalRhsStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "insertLogicalRhsStatement")) return false;
    if (!nextTokenIs(b, INSERT_LOGICAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INSERT_LOGICAL_RHS_STATEMENT, null);
    r = consumeTokens(b, 1, INSERT_LOGICAL, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && report_error_(b, insertLogicalRhsStatement_3(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' expression)*
  private static boolean insertLogicalRhsStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "insertLogicalRhsStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!insertLogicalRhsStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "insertLogicalRhsStatement_3", c)) break;
    }
    return true;
  }

  // ',' expression
  private static boolean insertLogicalRhsStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "insertLogicalRhsStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "insert" parExpr
  public static boolean insertRhsStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "insertRhsStatement")) return false;
    if (!nextTokenIs(b, INSERT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INSERT_RHS_STATEMENT, null);
    r = consumeToken(b, INSERT);
    p = r; // pin = 1
    r = r && parExpr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "l" | "L" | "I"
  static boolean integerTypeSuffix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "integerTypeSuffix")) return false;
    boolean r;
    r = consumeToken(b, "l");
    if (!r) r = consumeToken(b, "L");
    if (!r) r = consumeToken(b, "I");
    return r;
  }

  /* ********************************************************** */
  // JAVA_STATEMENT
  public static boolean javaRhsStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "javaRhsStatement")) return false;
    if (!nextTokenIs(b, JAVA_STATEMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JAVA_STATEMENT);
    exit_section_(b, m, JAVA_RHS_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // identifier ":"
  public static boolean label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, LABEL, r);
    return r;
  }

  /* ********************************************************** */
  // "when" ":"? lhsExpression
  public static boolean lhs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhs")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LHS, "<lhs>");
    r = consumeToken(b, WHEN);
    p = r; // pin = 1
    r = r && report_error_(b, lhs_1(b, l + 1));
    r = p && lhsExpression(b, l + 1) && r;
    exit_section_(b, l, m, r, p, DroolsParser::lhs_recover);
    return r || p;
  }

  // ":"?
  private static boolean lhs_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhs_1")) return false;
    consumeToken(b, COLON);
    return true;
  }

  /* ********************************************************** */
  // "accumulate" "(" lhsAnd (","|";") accumulateFunctionBinding ("," accumulateFunctionBinding)* (";" constraints)? ")" ";"?
  public static boolean lhsAccumulate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate")) return false;
    if (!nextTokenIs(b, ACCUMULATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ACCUMULATE, LPAREN);
    r = r && lhsAnd(b, l + 1);
    r = r && lhsAccumulate_3(b, l + 1);
    r = r && accumulateFunctionBinding(b, l + 1);
    r = r && lhsAccumulate_5(b, l + 1);
    r = r && lhsAccumulate_6(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && lhsAccumulate_8(b, l + 1);
    exit_section_(b, m, LHS_ACCUMULATE, r);
    return r;
  }

  // ","|";"
  private static boolean lhsAccumulate_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate_3")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
    return r;
  }

  // ("," accumulateFunctionBinding)*
  private static boolean lhsAccumulate_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!lhsAccumulate_5_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsAccumulate_5", c)) break;
    }
    return true;
  }

  // "," accumulateFunctionBinding
  private static boolean lhsAccumulate_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && accumulateFunctionBinding(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (";" constraints)?
  private static boolean lhsAccumulate_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate_6")) return false;
    lhsAccumulate_6_0(b, l + 1);
    return true;
  }

  // ";" constraints
  private static boolean lhsAccumulate_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && constraints(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ";"?
  private static boolean lhsAccumulate_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAccumulate_8")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // ("(" and lhsUnary+ ")") | (lhsUnary ("and" lhsUnary)*)
  public static boolean lhsAnd(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAnd")) return false;
    if (!nextTokenIs(b, "<lhs and>", ACCUMULATE, EVAL,
      EXISTS, FORALL, IF, JAVA_IDENTIFIER, LPAREN, NOT, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LHS_AND, "<lhs and>");
    r = lhsAnd_0(b, l + 1);
    if (!r) r = lhsAnd_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "(" and lhsUnary+ ")"
  private static boolean lhsAnd_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAnd_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LPAREN, AND);
    r = r && lhsAnd_0_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // lhsUnary+
  private static boolean lhsAnd_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAnd_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lhsUnary(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!lhsUnary(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsAnd_0_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // lhsUnary ("and" lhsUnary)*
  private static boolean lhsAnd_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAnd_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lhsUnary(b, l + 1);
    r = r && lhsAnd_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("and" lhsUnary)*
  private static boolean lhsAnd_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAnd_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!lhsAnd_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsAnd_1_1", c)) break;
    }
    return true;
  }

  // "and" lhsUnary
  private static boolean lhsAnd_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsAnd_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    r = r && lhsUnary(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "eval" "(" expression ")"
  public static boolean lhsEval(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsEval")) return false;
    if (!nextTokenIs(b, EVAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LHS_EVAL, null);
    r = consumeTokens(b, 1, EVAL, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "exists" conditionalElement
  public static boolean lhsExists(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsExists")) return false;
    if (!nextTokenIs(b, EXISTS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LHS_EXISTS, null);
    r = consumeToken(b, EXISTS);
    p = r; // pin = 1
    r = r && conditionalElement(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // lhsOr*
  public static boolean lhsExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsExpression")) return false;
    Marker m = enter_section_(b, l, _NONE_, LHS_EXPRESSION, "<lhs expression>");
    while (true) {
      int c = current_position_(b);
      if (!lhsOr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsExpression", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // "forall" "(" lhsPatternBind+ ")"
  public static boolean lhsForall(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsForall")) return false;
    if (!nextTokenIs(b, FORALL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LHS_FORALL, null);
    r = consumeTokens(b, 1, FORALL, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, lhsForall_2(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // lhsPatternBind+
  private static boolean lhsForall_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsForall_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lhsPatternBind(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!lhsPatternBind(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsForall_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "if" "(" expression ")" ("do" | "break") "[" + consequenceId +"]"
  public static boolean lhsNamedConsequence(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsNamedConsequence")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LHS_NAMED_CONSEQUENCE, null);
    r = consumeTokens(b, 1, IF, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && report_error_(b, lhsNamedConsequence_4(b, l + 1)) && r;
    r = p && report_error_(b, lhsNamedConsequence_5(b, l + 1)) && r;
    r = p && report_error_(b, lhsNamedConsequence_6(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // "do" | "break"
  private static boolean lhsNamedConsequence_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsNamedConsequence_4")) return false;
    boolean r;
    r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, BREAK);
    return r;
  }

  // "[" +
  private static boolean lhsNamedConsequence_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsNamedConsequence_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, LBRACKET)) break;
      if (!empty_element_parsed_guard_(b, "lhsNamedConsequence_5", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // consequenceId +
  private static boolean lhsNamedConsequence_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsNamedConsequence_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consequenceId(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!consequenceId(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsNamedConsequence_6", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "not" conditionalElement
  public static boolean lhsNot(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsNot")) return false;
    if (!nextTokenIs(b, NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LHS_NOT, null);
    r = consumeToken(b, NOT);
    p = r; // pin = 1
    r = r && conditionalElement(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // lhsAnd (or lhsAnd)*
  public static boolean lhsOr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsOr")) return false;
    if (!nextTokenIs(b, "<lhs or>", ACCUMULATE, EVAL,
      EXISTS, FORALL, IF, JAVA_IDENTIFIER, LPAREN, NOT, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LHS_OR, "<lhs or>");
    r = lhsAnd(b, l + 1);
    r = r && lhsOr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (or lhsAnd)*
  private static boolean lhsOr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsOr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!lhsOr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsOr_1", c)) break;
    }
    return true;
  }

  // or lhsAnd
  private static boolean lhsOr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsOr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR);
    r = r && lhsAnd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "(" lhsOr ")"
  public static boolean lhsParen(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsParen")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && lhsOr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, LHS_PAREN, r);
    return r;
  }

  /* ********************************************************** */
  // "?"? lhsPatternType "(" positionalConstraints? constraints? ")" (patternFilter)? (patternSource)?
  public static boolean lhsPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern")) return false;
    if (!nextTokenIs(b, "<lhs pattern>", JAVA_IDENTIFIER, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LHS_PATTERN, "<lhs pattern>");
    r = lhsPattern_0(b, l + 1);
    r = r && lhsPatternType(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && lhsPattern_3(b, l + 1);
    r = r && lhsPattern_4(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && lhsPattern_6(b, l + 1);
    r = r && lhsPattern_7(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "?"?
  private static boolean lhsPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_0")) return false;
    consumeToken(b, QUEST);
    return true;
  }

  // positionalConstraints?
  private static boolean lhsPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_3")) return false;
    positionalConstraints(b, l + 1);
    return true;
  }

  // constraints?
  private static boolean lhsPattern_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_4")) return false;
    constraints(b, l + 1);
    return true;
  }

  // (patternFilter)?
  private static boolean lhsPattern_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_6")) return false;
    lhsPattern_6_0(b, l + 1);
    return true;
  }

  // (patternFilter)
  private static boolean lhsPattern_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternFilter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (patternSource)?
  private static boolean lhsPattern_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_7")) return false;
    lhsPattern_7_0(b, l + 1);
    return true;
  }

  // (patternSource)
  private static boolean lhsPattern_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPattern_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternSource(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // [nameId ":"] ( ("(" lhsPattern ("or" lhsPattern)* ")") | lhsPattern )   annotation?
  public static boolean lhsPatternBind(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind")) return false;
    if (!nextTokenIs(b, "<lhs pattern bind>", JAVA_IDENTIFIER, LPAREN, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LHS_PATTERN_BIND, "<lhs pattern bind>");
    r = lhsPatternBind_0(b, l + 1);
    r = r && lhsPatternBind_1(b, l + 1);
    r = r && lhsPatternBind_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [nameId ":"]
  private static boolean lhsPatternBind_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_0")) return false;
    lhsPatternBind_0_0(b, l + 1);
    return true;
  }

  // nameId ":"
  private static boolean lhsPatternBind_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nameId(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("(" lhsPattern ("or" lhsPattern)* ")") | lhsPattern
  private static boolean lhsPatternBind_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lhsPatternBind_1_0(b, l + 1);
    if (!r) r = lhsPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "(" lhsPattern ("or" lhsPattern)* ")"
  private static boolean lhsPatternBind_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && lhsPattern(b, l + 1);
    r = r && lhsPatternBind_1_0_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("or" lhsPattern)*
  private static boolean lhsPatternBind_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!lhsPatternBind_1_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "lhsPatternBind_1_0_2", c)) break;
    }
    return true;
  }

  // "or" lhsPattern
  private static boolean lhsPatternBind_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OR);
    r = r && lhsPattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // annotation?
  private static boolean lhsPatternBind_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternBind_2")) return false;
    annotation(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // qualifiedIdentifier
  public static boolean lhsPatternType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsPatternType")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier(b, l + 1);
    exit_section_(b, m, LHS_PATTERN_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // ( lhsExists
  // | lhsNot
  // | lhsEval
  // | lhsForall
  // | lhsAccumulate
  // | lhsNamedConsequence
  // | "(" lhsOr ")"
  // | lhsPatternBind
  // )";"?
  public static boolean lhsUnary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsUnary")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LHS_UNARY, "<lhs unary>");
    r = lhsUnary_0(b, l + 1);
    r = r && lhsUnary_1(b, l + 1);
    exit_section_(b, l, m, r, false, DroolsParser::unary_recover);
    return r;
  }

  // lhsExists
  // | lhsNot
  // | lhsEval
  // | lhsForall
  // | lhsAccumulate
  // | lhsNamedConsequence
  // | "(" lhsOr ")"
  // | lhsPatternBind
  private static boolean lhsUnary_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsUnary_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lhsExists(b, l + 1);
    if (!r) r = lhsNot(b, l + 1);
    if (!r) r = lhsEval(b, l + 1);
    if (!r) r = lhsForall(b, l + 1);
    if (!r) r = lhsAccumulate(b, l + 1);
    if (!r) r = lhsNamedConsequence(b, l + 1);
    if (!r) r = lhsUnary_0_6(b, l + 1);
    if (!r) r = lhsPatternBind(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "(" lhsOr ")"
  private static boolean lhsUnary_0_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsUnary_0_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && lhsOr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ";"?
  private static boolean lhsUnary_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhsUnary_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // !(';' | 'activation-group' | 'agenda-group' | 'auto-focus' | 'calendars' | 'date-effective' | 'date-expires' | 'declare' | 'dialect' | 'duration' | 'enabled' | 'function' | 'global' | 'import' | 'lock-on-active' | 'no-loop' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'then' | 'timer' | query)
  static boolean lhs_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhs_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !lhs_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ';' | 'activation-group' | 'agenda-group' | 'auto-focus' | 'calendars' | 'date-effective' | 'date-expires' | 'declare' | 'dialect' | 'duration' | 'enabled' | 'function' | 'global' | 'import' | 'lock-on-active' | 'no-loop' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'then' | 'timer' | query
  private static boolean lhs_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lhs_recover_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, ACTIVATION_GROUP);
    if (!r) r = consumeToken(b, AGENDA_GROUP);
    if (!r) r = consumeToken(b, AUTO_FOCUS);
    if (!r) r = consumeToken(b, CALENDARS);
    if (!r) r = consumeToken(b, DATE_EFFECTIVE);
    if (!r) r = consumeToken(b, DATE_EXPIRES);
    if (!r) r = consumeToken(b, DECLARE);
    if (!r) r = consumeToken(b, DIALECT);
    if (!r) r = consumeToken(b, DURATION);
    if (!r) r = consumeToken(b, ENABLED);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, GLOBAL);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LOCK_ON_ACTIVE);
    if (!r) r = consumeToken(b, NO_LOOP);
    if (!r) r = consumeToken(b, REFRACT);
    if (!r) r = consumeToken(b, RULE);
    if (!r) r = consumeToken(b, RULEFLOW_GROUP);
    if (!r) r = consumeToken(b, SALIENCE);
    if (!r) r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, TIMER);
    if (!r) r = consumeToken(b, QUERY);
    return r;
  }

  /* ********************************************************** */
  // numberLiteral | booleanLiteral | stringLiteral | nullLiteral
  static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    if (!nextTokenIs(b, "", CHARACTER_LITERAL, FALSE,
      FLOAT_TOKEN, INT_TOKEN, NULL, OP_MINUS, OP_PLUS, STRING_TOKEN, TRUE)) return false;
    boolean r;
    r = numberLiteral(b, l + 1);
    if (!r) r = booleanLiteral(b, l + 1);
    if (!r) r = stringLiteral(b, l + 1);
    if (!r) r = nullLiteral(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // expression ":" expression
  public static boolean mapEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapEntry")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MAP_ENTRY, "<map entry>");
    r = expression(b, l + 1, -1);
    r = r && consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // mapEntry ( "," mapEntry )*
  public static boolean mapExpressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpressionList")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MAP_EXPRESSION_LIST, "<map expression list>");
    r = mapEntry(b, l + 1);
    p = r; // pin = 1
    r = r && mapExpressionList_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( "," mapEntry )*
  private static boolean mapExpressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!mapExpressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mapExpressionList_1", c)) break;
    }
    return true;
  }

  // "," mapEntry
  private static boolean mapExpressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapExpressionList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && mapEntry(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // parExpr
  public static boolean modifyParExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modifyParExpr")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, MODIFY_PAR_EXPR, null);
    r = parExpr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "modify" modifyParExpr "{" ( &"}" | exprList) "}"
  public static boolean modifyRhsStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modifyRhsStatement")) return false;
    if (!nextTokenIs(b, MODIFY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MODIFY_RHS_STATEMENT, null);
    r = consumeToken(b, MODIFY);
    p = r; // pin = 1
    r = r && report_error_(b, modifyParExpr(b, l + 1));
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, modifyRhsStatement_3(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &"}" | exprList
  private static boolean modifyRhsStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modifyRhsStatement_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = modifyRhsStatement_3_0(b, l + 1);
    if (!r) r = exprList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &"}"
  private static boolean modifyRhsStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modifyRhsStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // JAVA_IDENTIFIER
  public static boolean nameId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nameId")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JAVA_IDENTIFIER);
    exit_section_(b, m, NAME_ID, r);
    return r;
  }

  /* ********************************************************** */
  // JAVA_IDENTIFIER ("." JAVA_IDENTIFIER)*
  public static boolean namespace(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespace")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JAVA_IDENTIFIER);
    r = r && namespace_1(b, l + 1);
    exit_section_(b, m, NAMESPACE, r);
    return r;
  }

  // ("." JAVA_IDENTIFIER)*
  private static boolean namespace_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespace_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!namespace_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namespace_1", c)) break;
    }
    return true;
  }

  // "." JAVA_IDENTIFIER
  private static boolean namespace_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespace_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, JAVA_IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "<" ( type (","  type)* ) ">"
  public static boolean nonWildcardTypeArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonWildcardTypeArguments")) return false;
    if (!nextTokenIs(b, OP_LESS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_LESS);
    r = r && nonWildcardTypeArguments_1(b, l + 1);
    r = r && consumeToken(b, OP_GREATER);
    exit_section_(b, m, NON_WILDCARD_TYPE_ARGUMENTS, r);
    return r;
  }

  // type (","  type)*
  private static boolean nonWildcardTypeArguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonWildcardTypeArguments_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && nonWildcardTypeArguments_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (","  type)*
  private static boolean nonWildcardTypeArguments_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonWildcardTypeArguments_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!nonWildcardTypeArguments_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "nonWildcardTypeArguments_1_1", c)) break;
    }
    return true;
  }

  // ","  type
  private static boolean nonWildcardTypeArguments_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonWildcardTypeArguments_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NULL
  public static boolean nullLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nullLiteral")) return false;
    if (!nextTokenIs(b, NULL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NULL);
    exit_section_(b, m, NULL_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // plus_minus? (INT_TOKEN | FLOAT_TOKEN)
  public static boolean numberLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numberLiteral")) return false;
    if (!nextTokenIs(b, "<number literal>", FLOAT_TOKEN, INT_TOKEN, OP_MINUS, OP_PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NUMBER_LITERAL, "<number literal>");
    r = numberLiteral_0(b, l + 1);
    r = r && numberLiteral_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // plus_minus?
  private static boolean numberLiteral_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numberLiteral_0")) return false;
    plus_minus(b, l + 1);
    return true;
  }

  // INT_TOKEN | FLOAT_TOKEN
  private static boolean numberLiteral_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numberLiteral_1")) return false;
    boolean r;
    r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    return r;
  }

  /* ********************************************************** */
  // EQ | "!=" | relationalOperator
  public static boolean operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPERATOR, "<operator>");
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, OP_NOT_EQ);
    if (!r) r = relationalOperator(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // andRestriction ( "||" andRestriction ) *
  static boolean orRestriction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orRestriction")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = andRestriction(b, l + 1);
    r = r && orRestriction_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "||" andRestriction ) *
  private static boolean orRestriction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orRestriction_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!orRestriction_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "orRestriction_1", c)) break;
    }
    return true;
  }

  // "||" andRestriction
  private static boolean orRestriction_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "orRestriction_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_COND_OR);
    r = r && andRestriction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "package" namespace ";"?
  public static boolean packageStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageStatement")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PACKAGE_STATEMENT, null);
    r = consumeToken(b, PACKAGE);
    r = r && namespace(b, l + 1);
    p = r; // pin = 2
    r = r && packageStatement_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ";"?
  private static boolean packageStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "packageStatement_2")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // "(" expression ")"
  public static boolean parExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parExpr")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, PAR_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // (type | primitiveType)? nameId ("[" "]")*
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = parameter_0(b, l + 1);
    r = r && nameId(b, l + 1);
    p = r; // pin = 2
    r = r && parameter_2(b, l + 1);
    exit_section_(b, l, m, r, p, DroolsParser::parameter_recover);
    return r || p;
  }

  // (type | primitiveType)?
  private static boolean parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0")) return false;
    parameter_0_0(b, l + 1);
    return true;
  }

  // type | primitiveType
  private static boolean parameter_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0_0")) return false;
    boolean r;
    r = type(b, l + 1);
    if (!r) r = primitiveType(b, l + 1);
    return r;
  }

  // ("[" "]")*
  private static boolean parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_2", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean parameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')' | ',' )
  static boolean parameter_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !parameter_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | ','
  private static boolean parameter_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_recover_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  /* ********************************************************** */
  // "(" ( parameter ( "," parameter )* )? ")"
  public static boolean parameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETERS, null);
    r = consumeToken(b, LPAREN);
    r = r && parameters_1(b, l + 1);
    p = r; // pin = 2
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ( parameter ( "," parameter )* )?
  private static boolean parameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters_1")) return false;
    parameters_1_0(b, l + 1);
    return true;
  }

  // parameter ( "," parameter )*
  private static boolean parameters_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter(b, l + 1);
    r = r && parameters_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "," parameter )*
  private static boolean parameters_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameters_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameters_1_0_1", c)) break;
    }
    return true;
  }

  // "," parameter
  private static boolean parameters_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameters_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stringId
  public static boolean parentRule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parentRule")) return false;
    if (!nextTokenIs(b, "<parent rule>", CHARACTER_LITERAL, JAVA_IDENTIFIER, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARENT_RULE, "<parent rule>");
    r = stringId(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "over" (filterDef | windowDef)
  public static boolean patternFilter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternFilter")) return false;
    if (!nextTokenIs(b, OVER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_FILTER, null);
    r = consumeToken(b, OVER);
    p = r; // pin = 1
    r = r && patternFilter_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // filterDef | windowDef
  private static boolean patternFilter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternFilter_1")) return false;
    boolean r;
    r = filterDef(b, l + 1);
    if (!r) r = windowDef(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // "from" ( fromAccumulate | fromCollect | fromEntryPoint | fromWindow | fromExpression )
  public static boolean patternSource(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternSource")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_SOURCE, null);
    r = consumeToken(b, FROM);
    p = r; // pin = 1
    r = r && patternSource_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // fromAccumulate | fromCollect | fromEntryPoint | fromWindow | fromExpression
  private static boolean patternSource_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternSource_1")) return false;
    boolean r;
    r = fromAccumulate(b, l + 1);
    if (!r) r = fromCollect(b, l + 1);
    if (!r) r = fromEntryPoint(b, l + 1);
    if (!r) r = fromWindow(b, l + 1);
    if (!r) r = fromExpression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // "+" | "-"
  static boolean plus_minus(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plus_minus")) return false;
    if (!nextTokenIs(b, "", OP_MINUS, OP_PLUS)) return false;
    boolean r;
    r = consumeToken(b, OP_PLUS);
    if (!r) r = consumeToken(b, OP_MINUS);
    return r;
  }

  /* ********************************************************** */
  // constraint ("," constraint)* ";"
  static boolean positionalConstraints(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "positionalConstraints")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constraint(b, l + 1);
    r = r && positionalConstraints_1(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("," constraint)*
  private static boolean positionalConstraints_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "positionalConstraints_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!positionalConstraints_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "positionalConstraints_1", c)) break;
    }
    return true;
  }

  // "," constraint
  private static boolean positionalConstraints_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "positionalConstraints_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && constraint(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "boolean" | "char" | "byte" | "short" | "int" | "long" | "float" | "double" | "void"
  public static boolean primitiveType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primitiveType")) return false;
    if (!nextTokenIs(b, "<primitive type>", BOOLEAN, BYTE,
      CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRIMITIVE_TYPE, "<primitive type>");
    r = consumeToken(b, BOOLEAN);
    if (!r) r = consumeToken(b, CHAR);
    if (!r) r = consumeToken(b, BYTE);
    if (!r) r = consumeToken(b, SHORT);
    if (!r) r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, LONG);
    if (!r) r = consumeToken(b, FLOAT);
    if (!r) r = consumeToken(b, DOUBLE);
    if (!r) r = consumeToken(b, VOID);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier identifierSuffix? ( ("."|"!.") identifier identifierSuffix? )*
  static boolean qualified(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && qualified_1(b, l + 1);
    r = r && qualified_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // identifierSuffix?
  private static boolean qualified_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_1")) return false;
    identifierSuffix(b, l + 1);
    return true;
  }

  // ( ("."|"!.") identifier identifierSuffix? )*
  private static boolean qualified_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!qualified_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qualified_2", c)) break;
    }
    return true;
  }

  // ("."|"!.") identifier identifierSuffix?
  private static boolean qualified_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualified_2_0_0(b, l + 1);
    r = r && identifier(b, l + 1);
    r = r && qualified_2_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "."|"!."
  private static boolean qualified_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_2_0_0")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NULL_DOT);
    return r;
  }

  // identifierSuffix?
  private static boolean qualified_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_2_0_2")) return false;
    identifierSuffix(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier (("."|"!.") identifier)*
  public static boolean qualifiedIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && qualifiedIdentifier_1(b, l + 1);
    exit_section_(b, m, QUALIFIED_IDENTIFIER, r);
    return r;
  }

  // (("."|"!.") identifier)*
  private static boolean qualifiedIdentifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!qualifiedIdentifier_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "qualifiedIdentifier_1", c)) break;
    }
    return true;
  }

  // ("."|"!.") identifier
  private static boolean qualifiedIdentifier_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier_1_0_0(b, l + 1);
    r = r && identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "."|"!."
  private static boolean qualifiedIdentifier_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedIdentifier_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, NULL_DOT);
    return r;
  }

  /* ********************************************************** */
  // qualifiedIdentifier
  public static boolean qualifiedName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedName")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier(b, l + 1);
    exit_section_(b, m, QUALIFIED_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // lhsExpression
  public static boolean queryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY_EXPRESSION, "<query expression>");
    r = lhsExpression(b, l + 1);
    exit_section_(b, l, m, r, false, DroolsParser::queryExpression_recover);
    return r;
  }

  /* ********************************************************** */
  // ! (';' | 'activation-group' | 'agenda-group' | 'auto-focus' | 'calendars' | 'date-effective' | 'date-expires' | 'declare' | 'dialect' | 'duration' | 'enabled' | 'function' | 'global' | 'import' | 'lock-on-active' | 'no-loop' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'timer' | end | query)
  static boolean queryExpression_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryExpression_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !queryExpression_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ';' | 'activation-group' | 'agenda-group' | 'auto-focus' | 'calendars' | 'date-effective' | 'date-expires' | 'declare' | 'dialect' | 'duration' | 'enabled' | 'function' | 'global' | 'import' | 'lock-on-active' | 'no-loop' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'timer' | end | query
  private static boolean queryExpression_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryExpression_recover_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, ACTIVATION_GROUP);
    if (!r) r = consumeToken(b, AGENDA_GROUP);
    if (!r) r = consumeToken(b, AUTO_FOCUS);
    if (!r) r = consumeToken(b, CALENDARS);
    if (!r) r = consumeToken(b, DATE_EFFECTIVE);
    if (!r) r = consumeToken(b, DATE_EXPIRES);
    if (!r) r = consumeToken(b, DECLARE);
    if (!r) r = consumeToken(b, DIALECT);
    if (!r) r = consumeToken(b, DURATION);
    if (!r) r = consumeToken(b, ENABLED);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, GLOBAL);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LOCK_ON_ACTIVE);
    if (!r) r = consumeToken(b, NO_LOOP);
    if (!r) r = consumeToken(b, REFRACT);
    if (!r) r = consumeToken(b, RULE);
    if (!r) r = consumeToken(b, RULEFLOW_GROUP);
    if (!r) r = consumeToken(b, SALIENCE);
    if (!r) r = consumeToken(b, TIMER);
    if (!r) r = consumeToken(b, END);
    if (!r) r = consumeToken(b, QUERY);
    return r;
  }

  /* ********************************************************** */
  // query stringId parameters? annotation* queryExpression end
  public static boolean queryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryStatement")) return false;
    if (!nextTokenIs(b, QUERY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, QUERY_STATEMENT, null);
    r = consumeToken(b, QUERY);
    r = r && stringId(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, queryStatement_2(b, l + 1));
    r = p && report_error_(b, queryStatement_3(b, l + 1)) && r;
    r = p && report_error_(b, queryExpression(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // parameters?
  private static boolean queryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryStatement_2")) return false;
    parameters(b, l + 1);
    return true;
  }

  // annotation*
  private static boolean queryStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "queryStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "queryStatement_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "<=" | ">=" | "<" | ">" |  ("not"? ( "contains" | "memberOf" | "matches"| "soundslike" | "isA") ) | "str" squareArguments?
  public static boolean relationalOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RELATIONAL_OPERATOR, "<relational operator>");
    r = consumeToken(b, OP_LESS_OR_EQUAL);
    if (!r) r = consumeToken(b, OP_GREATER_OR_EQUAL);
    if (!r) r = consumeToken(b, OP_LESS);
    if (!r) r = consumeToken(b, OP_GREATER);
    if (!r) r = relationalOperator_4(b, l + 1);
    if (!r) r = relationalOperator_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "not"? ( "contains" | "memberOf" | "matches"| "soundslike" | "isA")
  private static boolean relationalOperator_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = relationalOperator_4_0(b, l + 1);
    r = r && relationalOperator_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "not"?
  private static boolean relationalOperator_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator_4_0")) return false;
    consumeToken(b, NOT);
    return true;
  }

  // "contains" | "memberOf" | "matches"| "soundslike" | "isA"
  private static boolean relationalOperator_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator_4_1")) return false;
    boolean r;
    r = consumeToken(b, CONTAINS);
    if (!r) r = consumeToken(b, MEMBEROF);
    if (!r) r = consumeToken(b, MATCHES);
    if (!r) r = consumeToken(b, SOUNDSLIKE);
    if (!r) r = consumeToken(b, IS_A);
    return r;
  }

  // "str" squareArguments?
  private static boolean relationalOperator_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "str");
    r = r && relationalOperator_5_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // squareArguments?
  private static boolean relationalOperator_5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator_5_1")) return false;
    squareArguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "retract" parExpr
  public static boolean retractRhsStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "retractRhsStatement")) return false;
    if (!nextTokenIs(b, RETRACT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRACT_RHS_STATEMENT, null);
    r = consumeToken(b, RETRACT);
    p = r; // pin = 1
    r = r && parExpr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "then" ("["+consequenceId "]")? rhsStatements*
  public static boolean rhs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RHS, "<rhs>");
    r = consumeToken(b, THEN);
    p = r; // pin = 1
    r = r && report_error_(b, rhs_1(b, l + 1));
    r = p && rhs_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, DroolsParser::rhs_recover);
    return r || p;
  }

  // ("["+consequenceId "]")?
  private static boolean rhs_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs_1")) return false;
    rhs_1_0(b, l + 1);
    return true;
  }

  // "["+consequenceId "]"
  private static boolean rhs_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rhs_1_0_0(b, l + 1);
    r = r && consequenceId(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // "["+
  private static boolean rhs_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, LBRACKET)) break;
      if (!empty_element_parsed_guard_(b, "rhs_1_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // rhsStatements*
  private static boolean rhs_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!rhsStatements(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "rhs_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ( javaRhsStatement | modifyRhsStatement | updateRhsStatement | retractRhsStatement | insertRhsStatement | insertLogicalRhsStatement) ";"?
  static boolean rhsStatements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhsStatements")) return false;
    if (!nextTokenIs(b, "", INSERT, INSERT_LOGICAL,
      JAVA_STATEMENT, MODIFY, RETRACT, UPDATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rhsStatements_0(b, l + 1);
    r = r && rhsStatements_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // javaRhsStatement | modifyRhsStatement | updateRhsStatement | retractRhsStatement | insertRhsStatement | insertLogicalRhsStatement
  private static boolean rhsStatements_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhsStatements_0")) return false;
    boolean r;
    r = javaRhsStatement(b, l + 1);
    if (!r) r = modifyRhsStatement(b, l + 1);
    if (!r) r = updateRhsStatement(b, l + 1);
    if (!r) r = retractRhsStatement(b, l + 1);
    if (!r) r = insertRhsStatement(b, l + 1);
    if (!r) r = insertLogicalRhsStatement(b, l + 1);
    return r;
  }

  // ";"?
  private static boolean rhsStatements_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhsStatements_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // !(';' | 'activation-group' | 'agenda-group' | 'auto-focus' | 'calendars' | 'date-effective' | 'date-expires' | 'declare' | 'dialect' | 'duration' | 'enabled' | 'function' | 'global' | 'import' | 'lock-on-active' | 'no-loop' | 'query' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'timer' | 'then' |'end')
  static boolean rhs_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rhs_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ';' | 'activation-group' | 'agenda-group' | 'auto-focus' | 'calendars' | 'date-effective' | 'date-expires' | 'declare' | 'dialect' | 'duration' | 'enabled' | 'function' | 'global' | 'import' | 'lock-on-active' | 'no-loop' | 'query' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'timer' | 'then' |'end'
  private static boolean rhs_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rhs_recover_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, ACTIVATION_GROUP);
    if (!r) r = consumeToken(b, AGENDA_GROUP);
    if (!r) r = consumeToken(b, AUTO_FOCUS);
    if (!r) r = consumeToken(b, CALENDARS);
    if (!r) r = consumeToken(b, DATE_EFFECTIVE);
    if (!r) r = consumeToken(b, DATE_EXPIRES);
    if (!r) r = consumeToken(b, DECLARE);
    if (!r) r = consumeToken(b, DIALECT);
    if (!r) r = consumeToken(b, DURATION);
    if (!r) r = consumeToken(b, ENABLED);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, GLOBAL);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LOCK_ON_ACTIVE);
    if (!r) r = consumeToken(b, NO_LOOP);
    if (!r) r = consumeToken(b, QUERY);
    if (!r) r = consumeToken(b, REFRACT);
    if (!r) r = consumeToken(b, RULE);
    if (!r) r = consumeToken(b, RULEFLOW_GROUP);
    if (!r) r = consumeToken(b, SALIENCE);
    if (!r) r = consumeToken(b, TIMER);
    if (!r) r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, END);
    return r;
  }

  /* ********************************************************** */
  // attribute
  static boolean ruleAttribute(PsiBuilder b, int l) {
    return attribute(b, l + 1);
  }

  /* ********************************************************** */
  // ("attributes" ":"?)? attribute ( ","? attribute )*
  public static boolean ruleAttributes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes")) return false;
    if (!nextTokenIs(b, "<rule attributes>", ACTIVATION_GROUP, AGENDA_GROUP,
      ATTRIBUTES, AUTO_FOCUS, CALENDARS, DATE_EFFECTIVE, DATE_EXPIRES, DIALECT,
      DURATION, ENABLED, LOCK_ON_ACTIVE, NO_LOOP, REFRACT, RULEFLOW_GROUP, SALIENCE, TIMER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RULE_ATTRIBUTES, "<rule attributes>");
    r = ruleAttributes_0(b, l + 1);
    r = r && attribute(b, l + 1);
    r = r && ruleAttributes_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ("attributes" ":"?)?
  private static boolean ruleAttributes_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes_0")) return false;
    ruleAttributes_0_0(b, l + 1);
    return true;
  }

  // "attributes" ":"?
  private static boolean ruleAttributes_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ATTRIBUTES);
    r = r && ruleAttributes_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ":"?
  private static boolean ruleAttributes_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes_0_0_1")) return false;
    consumeToken(b, COLON);
    return true;
  }

  // ( ","? attribute )*
  private static boolean ruleAttributes_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ruleAttributes_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ruleAttributes_2", c)) break;
    }
    return true;
  }

  // ","? attribute
  private static boolean ruleAttributes_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ruleAttributes_2_0_0(b, l + 1);
    r = r && attribute(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ","?
  private static boolean ruleAttributes_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleAttributes_2_0_0")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // stringId
  public static boolean ruleName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleName")) return false;
    if (!nextTokenIs(b, "<rule name>", CHARACTER_LITERAL, JAVA_IDENTIFIER, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RULE_NAME, "<rule name>");
    r = stringId(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "rule" ruleName ("extends" parentRule)? annotation* ruleAttributes? lhs? rhs* end
  public static boolean ruleStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement")) return false;
    if (!nextTokenIs(b, RULE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RULE_STATEMENT, null);
    r = consumeToken(b, RULE);
    r = r && ruleName(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, ruleStatement_2(b, l + 1));
    r = p && report_error_(b, ruleStatement_3(b, l + 1)) && r;
    r = p && report_error_(b, ruleStatement_4(b, l + 1)) && r;
    r = p && report_error_(b, ruleStatement_5(b, l + 1)) && r;
    r = p && report_error_(b, ruleStatement_6(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ("extends" parentRule)?
  private static boolean ruleStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement_2")) return false;
    ruleStatement_2_0(b, l + 1);
    return true;
  }

  // "extends" parentRule
  private static boolean ruleStatement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTENDS);
    r = r && parentRule(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // annotation*
  private static boolean ruleStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ruleStatement_3", c)) break;
    }
    return true;
  }

  // ruleAttributes?
  private static boolean ruleStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement_4")) return false;
    ruleAttributes(b, l + 1);
    return true;
  }

  // lhs?
  private static boolean ruleStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement_5")) return false;
    lhs(b, l + 1);
    return true;
  }

  // rhs*
  private static boolean ruleStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ruleStatement_6")) return false;
    while (true) {
      int c = current_position_(b);
      if (!rhs(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ruleStatement_6", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ( "." "super" superSuffix ) |
  //         ( "." "new" nonWildcardTypeArguments? innerCreator ) |
  //         ( "." identifier arguments? ) |
  //         ( "[" expression "]" )
  public static boolean selector(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector")) return false;
    if (!nextTokenIs(b, "<selector>", DOT, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SELECTOR, "<selector>");
    r = selector_0(b, l + 1);
    if (!r) r = selector_1(b, l + 1);
    if (!r) r = selector_2(b, l + 1);
    if (!r) r = selector_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "." "super" superSuffix
  private static boolean selector_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && consumeToken(b, "super");
    r = r && superSuffix(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "." "new" nonWildcardTypeArguments? innerCreator
  private static boolean selector_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && consumeToken(b, "new");
    r = r && selector_1_2(b, l + 1);
    r = r && innerCreator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // nonWildcardTypeArguments?
  private static boolean selector_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector_1_2")) return false;
    nonWildcardTypeArguments(b, l + 1);
    return true;
  }

  // "." identifier arguments?
  private static boolean selector_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && identifier(b, l + 1);
    r = r && selector_2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // arguments?
  private static boolean selector_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector_2_2")) return false;
    arguments(b, l + 1);
    return true;
  }

  // "[" expression "]"
  private static boolean selector_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "selector_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && expression(b, l + 1, -1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "<<" | ">>>" | ">>"
  static boolean shiftOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftOp")) return false;
    boolean r;
    r = consumeToken(b, "<<");
    if (!r) r = consumeToken(b, ">>>");
    if (!r) r = consumeToken(b, ">>");
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean simpleName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleName")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    exit_section_(b, m, SIMPLE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // operator shiftExpr | "(" orRestriction ")"
  static boolean singleRestriction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleRestriction")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = singleRestriction_0(b, l + 1);
    if (!r) r = singleRestriction_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // operator shiftExpr
  private static boolean singleRestriction_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleRestriction_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = operator(b, l + 1);
    r = r && expression(b, l + 1, 10);
    exit_section_(b, m, null, r);
    return r;
  }

  // "(" orRestriction ")"
  private static boolean singleRestriction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleRestriction_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && orRestriction(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "[" expressionList? "]"
  public static boolean squareArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "squareArguments")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && squareArguments_1(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, SQUARE_ARGUMENTS, r);
    return r;
  }

  // expressionList?
  private static boolean squareArguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "squareArguments_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // importStatement
  //     |  globalStatement
  //     |  declareStatement
  //     |  unitStatement
  //     |  ruleStatement
  //     |  ruleAttribute
  //     |  functionStatement
  //     |  queryStatement
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = importStatement(b, l + 1);
    if (!r) r = globalStatement(b, l + 1);
    if (!r) r = declareStatement(b, l + 1);
    if (!r) r = unitStatement(b, l + 1);
    if (!r) r = ruleStatement(b, l + 1);
    if (!r) r = ruleAttribute(b, l + 1);
    if (!r) r = functionStatement(b, l + 1);
    if (!r) r = queryStatement(b, l + 1);
    exit_section_(b, l, m, r, false, DroolsParser::top_level_recover);
    return r;
  }

  /* ********************************************************** */
  // "agenda-group" | "activation-group" | "ruleflow-group" | "date-effective" | "date-expires" | "dialect"
  static boolean stringAttrName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringAttrName")) return false;
    if (!nextTokenIs(b, "", ACTIVATION_GROUP, AGENDA_GROUP,
      DATE_EFFECTIVE, DATE_EXPIRES, DIALECT, RULEFLOW_GROUP)) return false;
    boolean r;
    r = consumeToken(b, AGENDA_GROUP);
    if (!r) r = consumeToken(b, ACTIVATION_GROUP);
    if (!r) r = consumeToken(b, RULEFLOW_GROUP);
    if (!r) r = consumeToken(b, DATE_EFFECTIVE);
    if (!r) r = consumeToken(b, DATE_EXPIRES);
    if (!r) r = consumeToken(b, DIALECT);
    return r;
  }

  /* ********************************************************** */
  // identifier | STRING_TOKEN | CHARACTER_LITERAL
  public static boolean stringId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringId")) return false;
    if (!nextTokenIs(b, "<string id>", CHARACTER_LITERAL, JAVA_IDENTIFIER, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_ID, "<string id>");
    r = identifier(b, l + 1);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, CHARACTER_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN | CHARACTER_LITERAL
  public static boolean stringLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteral")) return false;
    if (!nextTokenIs(b, "<string literal>", CHARACTER_LITERAL, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_LITERAL, "<string literal>");
    r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, CHARACTER_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING_IDENTIFIER
  public static boolean stringSequence(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringSequence")) return false;
    if (!nextTokenIs(b, STRING_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING_IDENTIFIER);
    exit_section_(b, m, STRING_SEQUENCE, r);
    return r;
  }

  /* ********************************************************** */
  // "calendars"
  static boolean stringsAttrName(PsiBuilder b, int l) {
    return consumeToken(b, CALENDARS);
  }

  /* ********************************************************** */
  // arguments | ( "." identifier arguments? )
  public static boolean superSuffix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superSuffix")) return false;
    if (!nextTokenIs(b, "<super suffix>", DOT, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SUPER_SUFFIX, "<super suffix>");
    r = arguments(b, l + 1);
    if (!r) r = superSuffix_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "." identifier arguments?
  private static boolean superSuffix_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superSuffix_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && identifier(b, l + 1);
    r = r && superSuffix_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // arguments?
  private static boolean superSuffix_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superSuffix_1_2")) return false;
    arguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // qualifiedName
  public static boolean superType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superType")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedName(b, l + 1);
    exit_section_(b, m, SUPER_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // !(';' | 'activation-group' | 'agenda-group' | 'auto-focus' |
  //                                 'calendars' | 'date-effective' | 'date-expires' | 'declare' |
  //                                 'dialect' | 'duration' | 'enabled' |
  //                                 'function' | 'global' | 'import' | 'lock-on-active' |
  //                                 'no-loop' | 'query' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'timer' | 'window' | 'unit')
  static boolean top_level_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_level_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !top_level_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ';' | 'activation-group' | 'agenda-group' | 'auto-focus' |
  //                                 'calendars' | 'date-effective' | 'date-expires' | 'declare' |
  //                                 'dialect' | 'duration' | 'enabled' |
  //                                 'function' | 'global' | 'import' | 'lock-on-active' |
  //                                 'no-loop' | 'query' | 'refract' | 'rule' | 'ruleflow-group' | 'salience' | 'timer' | 'window' | 'unit'
  private static boolean top_level_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_level_recover_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, ACTIVATION_GROUP);
    if (!r) r = consumeToken(b, AGENDA_GROUP);
    if (!r) r = consumeToken(b, AUTO_FOCUS);
    if (!r) r = consumeToken(b, CALENDARS);
    if (!r) r = consumeToken(b, DATE_EFFECTIVE);
    if (!r) r = consumeToken(b, DATE_EXPIRES);
    if (!r) r = consumeToken(b, DECLARE);
    if (!r) r = consumeToken(b, DIALECT);
    if (!r) r = consumeToken(b, DURATION);
    if (!r) r = consumeToken(b, ENABLED);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, GLOBAL);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, LOCK_ON_ACTIVE);
    if (!r) r = consumeToken(b, NO_LOOP);
    if (!r) r = consumeToken(b, QUERY);
    if (!r) r = consumeToken(b, REFRACT);
    if (!r) r = consumeToken(b, RULE);
    if (!r) r = consumeToken(b, RULEFLOW_GROUP);
    if (!r) r = consumeToken(b, SALIENCE);
    if (!r) r = consumeToken(b, TIMER);
    if (!r) r = consumeToken(b, WINDOW);
    if (!r) r = consumeToken(b, UNIT);
    return r;
  }

  /* ********************************************************** */
  // "trait"
  public static boolean traitable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "traitable")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TRAITABLE, "<traitable>");
    r = consumeToken(b, "trait");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // qualifiedIdentifier typeArguments? ("[" "]")*
  public static boolean type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedIdentifier(b, l + 1);
    r = r && type_1(b, l + 1);
    r = r && type_2(b, l + 1);
    exit_section_(b, m, TYPE, r);
    return r;
  }

  // typeArguments?
  private static boolean type_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // ("[" "]")*
  private static boolean type_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!type_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "type_2", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean type_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "?" (( "extends" | "super" ) type )? | type
  public static boolean typeArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArgument")) return false;
    if (!nextTokenIs(b, "<type argument>", JAVA_IDENTIFIER, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_ARGUMENT, "<type argument>");
    r = typeArgument_0(b, l + 1);
    if (!r) r = type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "?" (( "extends" | "super" ) type )?
  private static boolean typeArgument_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArgument_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST);
    r = r && typeArgument_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (( "extends" | "super" ) type )?
  private static boolean typeArgument_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArgument_0_1")) return false;
    typeArgument_0_1_0(b, l + 1);
    return true;
  }

  // ( "extends" | "super" ) type
  private static boolean typeArgument_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArgument_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeArgument_0_1_0_0(b, l + 1);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "extends" | "super"
  private static boolean typeArgument_0_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArgument_0_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, EXTENDS);
    if (!r) r = consumeToken(b, "super");
    return r;
  }

  /* ********************************************************** */
  // "<" typeArgument ("," typeArgument)* ">"
  public static boolean typeArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArguments")) return false;
    if (!nextTokenIs(b, OP_LESS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OP_LESS);
    r = r && typeArgument(b, l + 1);
    r = r && typeArguments_2(b, l + 1);
    r = r && consumeToken(b, OP_GREATER);
    exit_section_(b, m, TYPE_ARGUMENTS, r);
    return r;
  }

  // ("," typeArgument)*
  private static boolean typeArguments_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArguments_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeArguments_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeArguments_2", c)) break;
    }
    return true;
  }

  // "," typeArgument
  private static boolean typeArguments_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArguments_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeArgument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "type"? traitable? typeName ("extends" superType)? annotation* field*
  public static boolean typeDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DECLARATION, "<type declaration>");
    r = typeDeclaration_0(b, l + 1);
    r = r && typeDeclaration_1(b, l + 1);
    r = r && typeName(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, typeDeclaration_3(b, l + 1));
    r = p && report_error_(b, typeDeclaration_4(b, l + 1)) && r;
    r = p && typeDeclaration_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // "type"?
  private static boolean typeDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration_0")) return false;
    consumeToken(b, "type");
    return true;
  }

  // traitable?
  private static boolean typeDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration_1")) return false;
    traitable(b, l + 1);
    return true;
  }

  // ("extends" superType)?
  private static boolean typeDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration_3")) return false;
    typeDeclaration_3_0(b, l + 1);
    return true;
  }

  // "extends" superType
  private static boolean typeDeclaration_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTENDS);
    r = r && superType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // annotation*
  private static boolean typeDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeDeclaration_4", c)) break;
    }
    return true;
  }

  // field*
  private static boolean typeDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclaration_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!field(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeDeclaration_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // qualifiedName
  public static boolean typeName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeName")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = qualifiedName(b, l + 1);
    exit_section_(b, m, TYPE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // !('(' | ')' | ',' | ';' | '?' | 'accumulate' | 'and' | 'end' | 'eval' | 'exists' | 'forall' | 'not' | 'or' | 'then' | 'if' | JAVA_IDENTIFIER)
  static boolean unary_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !unary_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '(' | ')' | ',' | ';' | '?' | 'accumulate' | 'and' | 'end' | 'eval' | 'exists' | 'forall' | 'not' | 'or' | 'then' | 'if' | JAVA_IDENTIFIER
  private static boolean unary_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_recover_0")) return false;
    boolean r;
    r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, QUEST);
    if (!r) r = consumeToken(b, ACCUMULATE);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, END);
    if (!r) r = consumeToken(b, EVAL);
    if (!r) r = consumeToken(b, EXISTS);
    if (!r) r = consumeToken(b, FORALL);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, JAVA_IDENTIFIER);
    return r;
  }

  /* ********************************************************** */
  // stringId
  public static boolean unitName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitName")) return false;
    if (!nextTokenIs(b, "<unit name>", CHARACTER_LITERAL, JAVA_IDENTIFIER, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNIT_NAME, "<unit name>");
    r = stringId(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "unit" unitName
  public static boolean unitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitStatement")) return false;
    if (!nextTokenIs(b, UNIT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, UNIT);
    r = r && unitName(b, l + 1);
    exit_section_(b, m, UNIT_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // "update" parExpr
  public static boolean updateRhsStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "updateRhsStatement")) return false;
    if (!nextTokenIs(b, UPDATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UPDATE_RHS_STATEMENT, null);
    r = consumeToken(b, UPDATE);
    p = r; // pin = 1
    r = r && parExpr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // type
  public static boolean varType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varType")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    exit_section_(b, m, VAR_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // arrayInitializer | expression
  public static boolean variableInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableInitializer")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_INITIALIZER, "<variable initializer>");
    r = arrayInitializer(b, l + 1);
    if (!r) r = expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "window" simpleName annotation* lhsPatternBind
  public static boolean windowDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowDeclaration")) return false;
    if (!nextTokenIs(b, WINDOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WINDOW_DECLARATION, null);
    r = consumeToken(b, WINDOW);
    r = r && simpleName(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, windowDeclaration_2(b, l + 1));
    r = p && lhsPatternBind(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // annotation*
  private static boolean windowDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowDeclaration_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!annotation(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "windowDeclaration_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "window" ':' (windowLength | windowTime)
  static boolean windowDef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowDef")) return false;
    if (!nextTokenIs(b, WINDOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, WINDOW, COLON);
    p = r; // pin = 1
    r = r && windowDef_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // windowLength | windowTime
  private static boolean windowDef_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowDef_2")) return false;
    boolean r;
    r = windowLength(b, l + 1);
    if (!r) r = windowTime(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // JAVA_IDENTIFIER
  public static boolean windowId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowId")) return false;
    if (!nextTokenIs(b, JAVA_IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JAVA_IDENTIFIER);
    exit_section_(b, m, WINDOW_ID, r);
    return r;
  }

  /* ********************************************************** */
  // "length" "(" numberLiteral ")"
  static boolean windowLength(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowLength")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "length");
    r = r && consumeToken(b, LPAREN);
    r = r && numberLiteral(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "time" "(" stringSequence ")"
  static boolean windowTime(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "windowTime")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "time");
    r = r && consumeToken(b, LPAREN);
    r = r && stringSequence(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression root: expression
  // Operator priority table:
  // 0: N_ARY(assignmentExpr)
  // 1: BINARY(conditionalExpr)
  // 2: N_ARY(conditionalOrExpr)
  // 3: N_ARY(conditionalAndExpr)
  // 4: N_ARY(inclusiveOrExpr)
  // 5: N_ARY(exclusiveOrExpr)
  // 6: N_ARY(andExpr)
  // 7: BINARY(equalityExpr)
  // 8: POSTFIX(instanceOfExpr)
  // 9: POSTFIX(inExpr)
  // 10: POSTFIX(relationalExpr)
  // 11: N_ARY(shiftExpr)
  // 12: N_ARY(additiveExpr)
  // 13: N_ARY(multiplicativeExpr)
  // 14: PREFIX(unaryExpr)
  // 15: PREFIX(unary2Expr)
  // 16: PREFIX(unaryNotPlusMinusExpr)
  // 17: ATOM(castExpr)
  // 18: PREFIX(unaryAssignExpr)
  // 19: ATOM(primaryExpr)
  public static boolean expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expression")) return false;
    addVariant(b, "<expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expression>");
    r = unaryExpr(b, l + 1);
    if (!r) r = unary2Expr(b, l + 1);
    if (!r) r = unaryNotPlusMinusExpr(b, l + 1);
    if (!r) r = castExpr(b, l + 1);
    if (!r) r = unaryAssignExpr(b, l + 1);
    if (!r) r = primaryExpr(b, l + 1);
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
      if (g < 0 && assignmentOperator(b, l + 1)) {
        while (true) {
          r = report_error_(b, expression(b, l, 0));
          if (!assignmentOperator(b, l + 1)) break;
        }
        exit_section_(b, l, m, ASSIGNMENT_EXPR, r, true, null);
      }
      else if (g < 1 && consumeTokenSmart(b, QUEST)) {
        r = report_error_(b, expression(b, l, 1));
        r = conditionalExpr_1(b, l + 1) && r;
        exit_section_(b, l, m, CONDITIONAL_EXPR, r, true, null);
      }
      else if (g < 2 && consumeTokenSmart(b, OP_COND_OR)) {
        while (true) {
          r = report_error_(b, expression(b, l, 2));
          if (!consumeTokenSmart(b, OP_COND_OR)) break;
        }
        exit_section_(b, l, m, CONDITIONAL_OR_EXPR, r, true, null);
      }
      else if (g < 3 && consumeTokenSmart(b, OP_COND_AND)) {
        while (true) {
          r = report_error_(b, expression(b, l, 3));
          if (!consumeTokenSmart(b, OP_COND_AND)) break;
        }
        exit_section_(b, l, m, CONDITIONAL_AND_EXPR, r, true, null);
      }
      else if (g < 4 && consumeTokenSmart(b, OP_BIT_OR)) {
        while (true) {
          r = report_error_(b, expression(b, l, 4));
          if (!consumeTokenSmart(b, OP_BIT_OR)) break;
        }
        exit_section_(b, l, m, INCLUSIVE_OR_EXPR, r, true, null);
      }
      else if (g < 5 && consumeTokenSmart(b, OP_BIT_XOR)) {
        while (true) {
          r = report_error_(b, expression(b, l, 5));
          if (!consumeTokenSmart(b, OP_BIT_XOR)) break;
        }
        exit_section_(b, l, m, EXCLUSIVE_OR_EXPR, r, true, null);
      }
      else if (g < 6 && consumeTokenSmart(b, OP_BIT_AND)) {
        while (true) {
          r = report_error_(b, expression(b, l, 6));
          if (!consumeTokenSmart(b, OP_BIT_AND)) break;
        }
        exit_section_(b, l, m, AND_EXPR, r, true, null);
      }
      else if (g < 7 && equality(b, l + 1)) {
        r = report_error_(b, expression(b, l, 7));
        r = equalityExpr_1(b, l + 1) && r;
        exit_section_(b, l, m, EQUALITY_EXPR, r, true, null);
      }
      else if (g < 8 && instanceOfExpr_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INSTANCE_OF_EXPR, r, true, null);
      }
      else if (g < 9 && inExpr_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, IN_EXPR, r, true, null);
      }
      else if (g < 10 && relationalExpr_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, RELATIONAL_EXPR, r, true, null);
      }
      else if (g < 11 && shiftOp(b, l + 1)) {
        int c = current_position_(b);
        while (true) {
          r = report_error_(b, expression(b, l, 11));
          if (!shiftOp(b, l + 1)) break;
          if (!empty_element_parsed_guard_(b, "shiftExpr", c)) break;
          c = current_position_(b);
        }
        exit_section_(b, l, m, SHIFT_EXPR, r, true, null);
      }
      else if (g < 12 && additiveExpr_0(b, l + 1)) {
        int c = current_position_(b);
        while (true) {
          r = report_error_(b, expression(b, l, 12));
          if (!additiveExpr_0(b, l + 1)) break;
          if (!empty_element_parsed_guard_(b, "additiveExpr", c)) break;
          c = current_position_(b);
        }
        exit_section_(b, l, m, ADDITIVE_EXPR, r, true, null);
      }
      else if (g < 13 && multiplicativeExpr_0(b, l + 1)) {
        while (true) {
          r = report_error_(b, expression(b, l, 13));
          if (!multiplicativeExpr_0(b, l + 1)) break;
        }
        exit_section_(b, l, m, MULTIPLICATIVE_EXPR, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // ":" expression
  private static boolean conditionalExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditionalExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (( "&&" | "||" ) equality expression)*
  private static boolean equalityExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!equalityExpr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "equalityExpr_1", c)) break;
    }
    return true;
  }

  // ( "&&" | "||" ) equality expression
  private static boolean equalityExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = equalityExpr_1_0_0(b, l + 1);
    r = r && equality(b, l + 1);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "&&" | "||"
  private static boolean equalityExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityExpr_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, OP_COND_AND);
    if (!r) r = consumeToken(b, OP_COND_OR);
    return r;
  }

  // "instanceof" type
  private static boolean instanceOfExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instanceOfExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, "instanceof");
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "not"? "in" "(" exprList ")"
  private static boolean inExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inExpr_0_0(b, l + 1);
    r = r && consumeTokensSmart(b, 0, IN, LPAREN);
    r = r && exprList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // "not"?
  private static boolean inExpr_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inExpr_0_0")) return false;
    consumeTokenSmart(b, NOT);
    return true;
  }

  // orRestriction +
  private static boolean relationalExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = orRestriction(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!orRestriction(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "relationalExpr_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (plus_minus)
  private static boolean additiveExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = plus_minus(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "/" | "%" | "*"
  private static boolean multiplicativeExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpr_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, OP_DIV);
    if (!r) r = consumeTokenSmart(b, OP_REMAINDER);
    if (!r) r = consumeTokenSmart(b, OP_MUL);
    return r;
  }

  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    if (!nextTokenIsSmart(b, OP_MINUS, OP_PLUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryExpr_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 14);
    exit_section_(b, l, m, UNARY_EXPR, r, p, null);
    return r || p;
  }

  // ( plus_minus )
  private static boolean unaryExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = plus_minus(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unary2Expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary2Expr")) return false;
    if (!nextTokenIsSmart(b, OP_MINUS_MINUS, OP_PLUS_PLUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary2Expr_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 18);
    exit_section_(b, l, m, UNARY_2_EXPR, r, p, null);
    return r || p;
  }

  // "++" | "--"
  private static boolean unary2Expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary2Expr_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, OP_PLUS_PLUS);
    if (!r) r = consumeTokenSmart(b, OP_MINUS_MINUS);
    return r;
  }

  public static boolean unaryNotPlusMinusExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryNotPlusMinusExpr")) return false;
    if (!nextTokenIsSmart(b, OP_COMPLEMENT, OP_NOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryNotPlusMinusExpr_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 16);
    exit_section_(b, l, m, UNARY_NOT_PLUS_MINUS_EXPR, r, p, null);
    return r || p;
  }

  // "~" | "!"
  private static boolean unaryNotPlusMinusExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryNotPlusMinusExpr_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, OP_COMPLEMENT);
    if (!r) r = consumeTokenSmart(b, OP_NOT);
    return r;
  }

  // ( "(" primitiveType ")" expression ) | ( "(" type ")" unaryNotPlusMinusExpr )
  public static boolean castExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "castExpr")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = castExpr_0(b, l + 1);
    if (!r) r = castExpr_1(b, l + 1);
    exit_section_(b, m, CAST_EXPR, r);
    return r;
  }

  // "(" primitiveType ")" expression
  private static boolean castExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "castExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && primitiveType(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "(" type ")" unaryNotPlusMinusExpr
  private static boolean castExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "castExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LPAREN);
    r = r && type(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && unaryNotPlusMinusExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean unaryAssignExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr")) return false;
    if (!nextTokenIsSmart(b, JAVA_IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryAssignExpr_0(b, l + 1);
    p = r;
    r = p && expression(b, l, 18);
    r = p && report_error_(b, unaryAssignExpr_1(b, l + 1)) && r;
    exit_section_(b, l, m, UNARY_ASSIGN_EXPR, r, p, null);
    return r || p;
  }

  // nameId ( ":=" | ":" )
  private static boolean unaryAssignExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nameId(b, l + 1);
    r = r && unaryAssignExpr_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ":=" | ":"
  private static boolean unaryAssignExpr_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr_0_1")) return false;
    boolean r;
    r = consumeTokenSmart(b, ":=");
    if (!r) r = consumeTokenSmart(b, COLON);
    return r;
  }

  // selector* [ "++" | "--" ]
  private static boolean unaryAssignExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unaryAssignExpr_1_0(b, l + 1);
    r = r && unaryAssignExpr_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // selector*
  private static boolean unaryAssignExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!selector(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "unaryAssignExpr_1_0", c)) break;
    }
    return true;
  }

  // [ "++" | "--" ]
  private static boolean unaryAssignExpr_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr_1_1")) return false;
    unaryAssignExpr_1_1_0(b, l + 1);
    return true;
  }

  // "++" | "--"
  private static boolean unaryAssignExpr_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryAssignExpr_1_1_0")) return false;
    boolean r;
    r = consumeToken(b, OP_PLUS_PLUS);
    if (!r) r = consumeToken(b, OP_MINUS_MINUS);
    return r;
  }

  // parExpr |
  //         nonWildcardTypeArguments ( explicitGenericInvocationSuffix | ( "this" arguments ) ) |
  //         literal |
  //         "super" superSuffix |
  //         "new" creator |
  //         primitiveType ( "[" "]" )* "." "class" |
  //         inlineMapExpr |
  //         inlineListExpr |
  //         "this" |
  //         qualified
  public static boolean primaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PRIMARY_EXPR, "<primary expr>");
    r = parExpr(b, l + 1);
    if (!r) r = primaryExpr_1(b, l + 1);
    if (!r) r = literal(b, l + 1);
    if (!r) r = primaryExpr_3(b, l + 1);
    if (!r) r = primaryExpr_4(b, l + 1);
    if (!r) r = primaryExpr_5(b, l + 1);
    if (!r) r = inlineMapExpr(b, l + 1);
    if (!r) r = inlineListExpr(b, l + 1);
    if (!r) r = consumeTokenSmart(b, THIS);
    if (!r) r = qualified(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // nonWildcardTypeArguments ( explicitGenericInvocationSuffix | ( "this" arguments ) )
  private static boolean primaryExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonWildcardTypeArguments(b, l + 1);
    r = r && primaryExpr_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // explicitGenericInvocationSuffix | ( "this" arguments )
  private static boolean primaryExpr_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = explicitGenericInvocationSuffix(b, l + 1);
    if (!r) r = primaryExpr_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "this" arguments
  private static boolean primaryExpr_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_1_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, THIS);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "super" superSuffix
  private static boolean primaryExpr_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, "super");
    r = r && superSuffix(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "new" creator
  private static boolean primaryExpr_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, "new");
    r = r && creator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // primitiveType ( "[" "]" )* "." "class"
  private static boolean primaryExpr_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = primitiveType(b, l + 1);
    r = r && primaryExpr_5_1(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && consumeToken(b, "class");
    exit_section_(b, m, null, r);
    return r;
  }

  // ( "[" "]" )*
  private static boolean primaryExpr_5_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_5_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!primaryExpr_5_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "primaryExpr_5_1", c)) break;
    }
    return true;
  }

  // "[" "]"
  private static boolean primaryExpr_5_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryExpr_5_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokensSmart(b, 0, LBRACKET, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

}
