// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartGeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DartParser implements PsiParser, LightPsiParser {

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
    boolean r;
    if (t == BLOCK) {
      r = block(b, l + 1);
    }
    else {
      r = dartUnit(b, l + 1);
    }
    return r;
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ADDITIVE_EXPRESSION, ARRAY_ACCESS_EXPRESSION, ASSIGN_EXPRESSION, AS_EXPRESSION,
      AWAIT_EXPRESSION, BITWISE_EXPRESSION, CALL_EXPRESSION, CASCADE_REFERENCE_EXPRESSION,
      COMPARE_EXPRESSION, CONST_OBJECT_EXPRESSION, EXPRESSION, FUNCTION_EXPRESSION,
      IF_NULL_EXPRESSION, IS_EXPRESSION, LIBRARY_COMPONENT_REFERENCE_EXPRESSION, LIST_LITERAL_EXPRESSION,
      LITERAL_EXPRESSION, LOGIC_AND_EXPRESSION, LOGIC_OR_EXPRESSION, MULTIPLICATIVE_EXPRESSION,
      NEW_EXPRESSION, PARAMETER_NAME_REFERENCE_EXPRESSION, PARENTHESIZED_EXPRESSION, PREFIX_EXPRESSION,
      REFERENCE_EXPRESSION, SET_OR_MAP_LITERAL_EXPRESSION, SHIFT_EXPRESSION, STRING_LITERAL_EXPRESSION,
      SUFFIX_EXPRESSION, SUPER_EXPRESSION, SYMBOL_LITERAL_EXPRESSION, TERNARY_EXPRESSION,
      THIS_EXPRESSION, THROW_EXPRESSION, VALUE_EXPRESSION),
  };

  /* ********************************************************** */
  // additiveOperator multiplicativeExpressionWrapper
  public static boolean additiveExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpression")) return false;
    if (!nextTokenIs(b, "<additive expression>", MINUS, PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, ADDITIVE_EXPRESSION, "<additive expression>");
    r = additiveOperator(b, l + 1);
    r = r && multiplicativeExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // multiplicativeExpressionWrapper additiveExpression*
  static boolean additiveExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = multiplicativeExpressionWrapper(b, l + 1);
    r = r && additiveExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // additiveExpression*
  private static boolean additiveExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!additiveExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "additiveExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '+' | '-'
  public static boolean additiveOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "additiveOperator")) return false;
    if (!nextTokenIs(b, "<additive operator>", MINUS, PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ADDITIVE_OPERATOR, "<additive operator>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // argumentListPart (',' argumentListPart)* ','?
  public static boolean argumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, "<argument list>");
    r = argumentListPart(b, l + 1);
    r = r && argumentList_1(b, l + 1);
    r = r && argumentList_2(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::argument_list_recover);
    return r;
  }

  // (',' argumentListPart)*
  private static boolean argumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!argumentList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "argumentList_1", c)) break;
    }
    return true;
  }

  // ',' argumentListPart
  private static boolean argumentList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && argumentListPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean argumentList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentList_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // namedArgument | expression
  static boolean argumentListPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argumentListPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = namedArgument(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::argument_list_part_recover);
    return r;
  }

  /* ********************************************************** */
  // !(')' | ',')
  static boolean argument_list_part_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_part_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !argument_list_part_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | ','
  private static boolean argument_list_part_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_part_recover_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  /* ********************************************************** */
  // !(')')
  static boolean argument_list_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' argumentList? ')'
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

  // argumentList?
  private static boolean arguments_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arguments_1")) return false;
    argumentList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // !('?' '[' elements? ']' ':') ('?.' | '?')? '[' expression? ']'
  static boolean arrayAccess(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess")) return false;
    if (!nextTokenIs(b, "", LBRACKET, QUEST, QUEST_DOT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = arrayAccess_0(b, l + 1);
    r = r && arrayAccess_1(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    p = r; // pin = 3
    r = r && report_error_(b, arrayAccess_3(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !('?' '[' elements? ']' ':')
  private static boolean arrayAccess_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !arrayAccess_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '?' '[' elements? ']' ':'
  private static boolean arrayAccess_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, QUEST, LBRACKET);
    r = r && arrayAccess_0_0_2(b, l + 1);
    r = r && consumeTokens(b, 0, RBRACKET, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // elements?
  private static boolean arrayAccess_0_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_0_0_2")) return false;
    elements(b, l + 1);
    return true;
  }

  // ('?.' | '?')?
  private static boolean arrayAccess_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_1")) return false;
    arrayAccess_1_0(b, l + 1);
    return true;
  }

  // '?.' | '?'
  private static boolean arrayAccess_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_1_0")) return false;
    boolean r;
    r = consumeToken(b, QUEST_DOT);
    if (!r) r = consumeToken(b, QUEST);
    return r;
  }

  // expression?
  private static boolean arrayAccess_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccess_3")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // arrayAccess
  public static boolean arrayAccessExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayAccessExpression")) return false;
    if (!nextTokenIs(b, "<array access expression>", LBRACKET, QUEST, QUEST_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, ARRAY_ACCESS_EXPRESSION, "<array access expression>");
    r = arrayAccess(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ('async' '*'? | 'sync' '*'?)? '=>' expression
  static boolean arrowBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody")) return false;
    if (!nextTokenIs(b, "", ASYNC, EXPRESSION_BODY_DEF, SYNC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = arrowBody_0(b, l + 1);
    r = r && consumeToken(b, EXPRESSION_BODY_DEF);
    p = r; // pin = 2
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('async' '*'? | 'sync' '*'?)?
  private static boolean arrowBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0")) return false;
    arrowBody_0_0(b, l + 1);
    return true;
  }

  // 'async' '*'? | 'sync' '*'?
  private static boolean arrowBody_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrowBody_0_0_0(b, l + 1);
    if (!r) r = arrowBody_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'async' '*'?
  private static boolean arrowBody_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASYNC);
    r = r && arrowBody_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean arrowBody_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_0_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  // 'sync' '*'?
  private static boolean arrowBody_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SYNC);
    r = r && arrowBody_0_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean arrowBody_0_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBody_0_0_1_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  /* ********************************************************** */
  // <<arrowBodyWrapper>> ';'
  static boolean arrowBodyWithSemi(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrowBodyWithSemi")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = arrowBodyWrapper(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'as' type
  public static boolean asExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "asExpression")) return false;
    if (!nextTokenIs(b, AS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, AS_EXPRESSION, null);
    r = consumeToken(b, AS);
    r = r && type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'assert' '(' expressionWithRecoverUntilParenOrComma (',' expressionWithRecoverUntilParenOrComma)? ','? ')'
  public static boolean assertStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assertStatement")) return false;
    if (!nextTokenIs(b, ASSERT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSERT_STATEMENT, null);
    r = consumeTokens(b, 1, ASSERT, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionWithRecoverUntilParenOrComma(b, l + 1));
    r = p && report_error_(b, assertStatement_3(b, l + 1)) && r;
    r = p && report_error_(b, assertStatement_4(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' expressionWithRecoverUntilParenOrComma)?
  private static boolean assertStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assertStatement_3")) return false;
    assertStatement_3_0(b, l + 1);
    return true;
  }

  // ',' expressionWithRecoverUntilParenOrComma
  private static boolean assertStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assertStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expressionWithRecoverUntilParenOrComma(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean assertStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assertStatement_4")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // assertStatement ';'
  static boolean assertStatementWithSemicolon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assertStatementWithSemicolon")) return false;
    if (!nextTokenIs(b, ASSERT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = assertStatement(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // assignmentOperator ternaryExpressionWrapper
  public static boolean assignExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignExpression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, ASSIGN_EXPRESSION, "<assign expression>");
    r = assignmentOperator(b, l + 1);
    p = r; // pin = 1
    r = r && ternaryExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ternaryExpressionWrapper assignExpression*
  static boolean assignExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ternaryExpressionWrapper(b, l + 1);
    r = r && assignExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // assignExpression*
  private static boolean assignExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!assignExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "assignExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '=' | '*=' | '/=' | '~/=' | '%=' | '+=' | '-=' | '<<=' | <<gtGtEq>> | <<gtGtGtEq>> | '&=' | '&&=' | '^=' | '|=' | '||=' | '??='
  public static boolean assignmentOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignmentOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_OPERATOR, "<assignment operator>");
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, MUL_EQ);
    if (!r) r = consumeToken(b, DIV_EQ);
    if (!r) r = consumeToken(b, INT_DIV_EQ);
    if (!r) r = consumeToken(b, REM_EQ);
    if (!r) r = consumeToken(b, PLUS_EQ);
    if (!r) r = consumeToken(b, MINUS_EQ);
    if (!r) r = consumeToken(b, LT_LT_EQ);
    if (!r) r = gtGtEq(b, l + 1);
    if (!r) r = gtGtGtEq(b, l + 1);
    if (!r) r = consumeToken(b, AND_EQ);
    if (!r) r = consumeToken(b, AND_AND_EQ);
    if (!r) r = consumeToken(b, XOR_EQ);
    if (!r) r = consumeToken(b, OR_EQ);
    if (!r) r = consumeToken(b, OR_OR_EQ);
    if (!r) r = consumeToken(b, QUEST_QUEST_EQ);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<isInsideSyncOrAsyncFunction>> 'await' prefixExpression
  public static boolean awaitExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "awaitExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, AWAIT_EXPRESSION, "<await expression>");
    r = isInsideSyncOrAsyncFunction(b, l + 1);
    r = r && consumeToken(b, AWAIT);
    r = r && prefixExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // multiplicativeOperator |
  //                            additiveOperator |
  //                            shiftOperator|
  //                            relationalOperator|
  //                            '==' |
  //                            bitwiseOperator
  static boolean binaryOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binaryOperator")) return false;
    boolean r;
    r = multiplicativeOperator(b, l + 1);
    if (!r) r = additiveOperator(b, l + 1);
    if (!r) r = shiftOperator(b, l + 1);
    if (!r) r = relationalOperator(b, l + 1);
    if (!r) r = consumeToken(b, EQ_EQ);
    if (!r) r = bitwiseOperator(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // bitwiseOperator shiftExpressionWrapper
  public static boolean bitwiseExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseExpression")) return false;
    if (!nextTokenIs(b, "<bitwise expression>", AND, OR, XOR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, BITWISE_EXPRESSION, "<bitwise expression>");
    r = bitwiseOperator(b, l + 1);
    r = r && shiftExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // shiftExpressionWrapper bitwiseExpression*
  static boolean bitwiseExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = shiftExpressionWrapper(b, l + 1);
    r = r && bitwiseExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // bitwiseExpression*
  private static boolean bitwiseExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!bitwiseExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "bitwiseExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '&' | '^' | '|'
  public static boolean bitwiseOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bitwiseOperator")) return false;
    if (!nextTokenIs(b, "<bitwise operator>", AND, OR, XOR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BITWISE_OPERATOR, "<bitwise operator>");
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, OR);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '{' statements '}'
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, statements(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ('async' '*'? | 'sync' '*'?)? lazyParseableBlock
  static boolean blockBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockBody_0(b, l + 1);
    r = r && lazyParseableBlock(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('async' '*'? | 'sync' '*'?)?
  private static boolean blockBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0")) return false;
    blockBody_0_0(b, l + 1);
    return true;
  }

  // 'async' '*'? | 'sync' '*'?
  private static boolean blockBody_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = blockBody_0_0_0(b, l + 1);
    if (!r) r = blockBody_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'async' '*'?
  private static boolean blockBody_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASYNC);
    r = r && blockBody_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean blockBody_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_0_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  // 'sync' '*'?
  private static boolean blockBody_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SYNC);
    r = r && blockBody_0_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '*'?
  private static boolean blockBody_0_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "blockBody_0_0_1_1")) return false;
    consumeToken(b, MUL);
    return true;
  }

  /* ********************************************************** */
  // 'break' referenceExpression? ';'
  public static boolean breakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BREAK_STATEMENT, null);
    r = consumeToken(b, BREAK);
    p = r; // pin = 1
    r = r && report_error_(b, breakStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // referenceExpression?
  private static boolean breakStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "breakStatement_1")) return false;
    referenceExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // typeArguments? ('.' 'new')? <<argumentsWrapper>>
  public static boolean callExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, CALL_EXPRESSION, "<call expression>");
    r = callExpression_0(b, l + 1);
    r = r && callExpression_1(b, l + 1);
    r = r && argumentsWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeArguments?
  private static boolean callExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpression_0")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // ('.' 'new')?
  private static boolean callExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpression_1")) return false;
    callExpression_1_0(b, l + 1);
    return true;
  }

  // '.' 'new'
  private static boolean callExpression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callExpression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DOT, NEW);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (callExpression | arrayAccessExpression | qualifiedReferenceExpression | '!' | typeArguments)*
  static boolean callOrArrayAccessOrQualifiedRefExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callOrArrayAccessOrQualifiedRefExpression")) return false;
    while (true) {
      int c = current_position_(b);
      if (!callOrArrayAccessOrQualifiedRefExpression_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "callOrArrayAccessOrQualifiedRefExpression", c)) break;
    }
    return true;
  }

  // callExpression | arrayAccessExpression | qualifiedReferenceExpression | '!' | typeArguments
  private static boolean callOrArrayAccessOrQualifiedRefExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callOrArrayAccessOrQualifiedRefExpression_0")) return false;
    boolean r;
    r = callExpression(b, l + 1);
    if (!r) r = arrayAccessExpression(b, l + 1);
    if (!r) r = qualifiedReferenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = typeArguments(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ('?..' | '..') << cascadeStopper >> (arrayAccess | refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression) << varInitWrapper >>
  public static boolean cascadeReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression")) return false;
    if (!nextTokenIs(b, "<cascade reference expression>", DOT_DOT, QUEST_DOT_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CASCADE_REFERENCE_EXPRESSION, "<cascade reference expression>");
    r = cascadeReferenceExpression_0(b, l + 1);
    r = r && cascadeStopper(b, l + 1);
    r = r && cascadeReferenceExpression_2(b, l + 1);
    r = r && varInitWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '?..' | '..'
  private static boolean cascadeReferenceExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression_0")) return false;
    boolean r;
    r = consumeToken(b, QUEST_DOT_DOT);
    if (!r) r = consumeToken(b, DOT_DOT);
    return r;
  }

  // arrayAccess | refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression
  private static boolean cascadeReferenceExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = arrayAccess(b, l + 1);
    if (!r) r = cascadeReferenceExpression_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // refOrThisOrSuperOrParenExpression callOrArrayAccessOrQualifiedRefExpression
  private static boolean cascadeReferenceExpression_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cascadeReferenceExpression_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = refOrThisOrSuperOrParenExpression(b, l + 1);
    r = r && callOrArrayAccessOrQualifiedRefExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'catch' '(' componentName (',' componentName)? ')'
  public static boolean catchPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchPart")) return false;
    if (!nextTokenIs(b, CATCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CATCH_PART, null);
    r = consumeTokens(b, 1, CATCH, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, componentName(b, l + 1));
    r = p && report_error_(b, catchPart_3(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' componentName)?
  private static boolean catchPart_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchPart_3")) return false;
    catchPart_3_0(b, l + 1);
    return true;
  }

  // ',' componentName
  private static boolean catchPart_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "catchPart_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' classMembers '}'
  public static boolean classBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classBody")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_BODY, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, classMembers(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // metadata* (mixinClassModifiers | classModifiers) 'class' componentName typeParameters? (mixinApplication | standardClassDeclarationTail)
  public static boolean classDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition")) return false;
    if (!nextTokenIs(b, "<class definition>", ABSTRACT, AT,
      BASE, CLASS, FINAL, INTERFACE, MIXIN, SEALED)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_DEFINITION, "<class definition>");
    r = classDefinition_0(b, l + 1);
    r = r && classDefinition_1(b, l + 1);
    r = r && consumeToken(b, CLASS);
    r = r && componentName(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, classDefinition_4(b, l + 1));
    r = p && classDefinition_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean classDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classDefinition_0", c)) break;
    }
    return true;
  }

  // mixinClassModifiers | classModifiers
  private static boolean classDefinition_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_1")) return false;
    boolean r;
    r = mixinClassModifiers(b, l + 1);
    if (!r) r = classModifiers(b, l + 1);
    return r;
  }

  // typeParameters?
  private static boolean classDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_4")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // mixinApplication | standardClassDeclarationTail
  private static boolean classDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDefinition_5")) return false;
    boolean r;
    r = mixinApplication(b, l + 1);
    if (!r) r = standardClassDeclarationTail(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // factoryConstructorDeclaration
  //                                 | namedConstructorDeclaration
  //                                 | getterOrSetterDeclaration
  //                                 | methodDeclaration
  //                                 | varDeclarationListWithSemicolon
  //                                 | incompleteDeclaration
  static boolean classMemberDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMemberDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = factoryConstructorDeclaration(b, l + 1);
    if (!r) r = namedConstructorDeclaration(b, l + 1);
    if (!r) r = getterOrSetterDeclaration(b, l + 1);
    if (!r) r = methodDeclaration(b, l + 1);
    if (!r) r = varDeclarationListWithSemicolon(b, l + 1);
    if (!r) r = incompleteDeclaration(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::class_member_recover);
    return r;
  }

  /* ********************************************************** */
  // classMemberDefinition*
  public static boolean classMembers(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classMembers")) return false;
    Marker m = enter_section_(b, l, _NONE_, CLASS_MEMBERS, "<class members>");
    while (true) {
      int c = current_position_(b);
      if (!classMemberDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classMembers", c)) break;
    }
    exit_section_(b, l, m, true, false, DartParser::simple_scope_recover);
    return true;
  }

  /* ********************************************************** */
  // 'sealed' | 'abstract'? ('base' | 'interface' | 'final')?
  static boolean classModifiers(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classModifiers")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEALED);
    if (!r) r = classModifiers_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'abstract'? ('base' | 'interface' | 'final')?
  private static boolean classModifiers_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classModifiers_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classModifiers_1_0(b, l + 1);
    r = r && classModifiers_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'abstract'?
  private static boolean classModifiers_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classModifiers_1_0")) return false;
    consumeToken(b, ABSTRACT);
    return true;
  }

  // ('base' | 'interface' | 'final')?
  private static boolean classModifiers_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classModifiers_1_1")) return false;
    classModifiers_1_1_0(b, l + 1);
    return true;
  }

  // 'base' | 'interface' | 'final'
  private static boolean classModifiers_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classModifiers_1_1_0")) return false;
    boolean r;
    r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, FINAL);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | 'operator' | '(' | '@' | 'abstract' | 'base' | 'class' | 'const' | 'covariant' |
  //                                  'enum' | 'export' | 'extension' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'interface' |
  //                                  'late' | 'library' | 'mixin' | 'part' | 'sealed' | 'set' | 'static' | 'typedef' | 'var' | 'void' | '}')
  static boolean class_member_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_member_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !class_member_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<nonStrictID>> | 'operator' | '(' | '@' | 'abstract' | 'base' | 'class' | 'const' | 'covariant' |
  //                                  'enum' | 'export' | 'extension' | 'external' | 'factory' | 'final' | 'get' | 'import' | 'interface' |
  //                                  'late' | 'library' | 'mixin' | 'part' | 'sealed' | 'set' | 'static' | 'typedef' | 'var' | 'void' | '}'
  private static boolean class_member_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "class_member_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, COVARIANT);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTENSION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FACTORY);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, LATE);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, MIXIN);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // showCombinator | hideCombinator
  static boolean combinator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "combinator")) return false;
    if (!nextTokenIs(b, "", HIDE, SHOW)) return false;
    boolean r;
    r = showCombinator(b, l + 1);
    if (!r) r = hideCombinator(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (relationalOperator | equalityOperator) bitwiseExpressionWrapper
  public static boolean compareExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, COMPARE_EXPRESSION, "<compare expression>");
    r = compareExpression_0(b, l + 1);
    r = r && bitwiseExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // relationalOperator | equalityOperator
  private static boolean compareExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpression_0")) return false;
    boolean r;
    r = relationalOperator(b, l + 1);
    if (!r) r = equalityOperator(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // bitwiseExpressionWrapper compareExpression*
  static boolean compareExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = bitwiseExpressionWrapper(b, l + 1);
    r = r && compareExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // compareExpression*
  private static boolean compareExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compareExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!compareExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "compareExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean componentName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "componentName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMPONENT_NAME, "<component name>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'const' constructorDesignation arguments
  public static boolean constObjectExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constObjectExpression")) return false;
    if (!nextTokenIs(b, CONST)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && constructorDesignation(b, l + 1);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, CONST_OBJECT_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // NULL | TRUE | FALSE | '-'? (NUMBER | HEX_NUMBER) | stringLiteralExpression | symbolLiteralExpression |
  //                     simpleQualifiedReferenceExpression |
  //                     constObjectExpression |
  //                     'const' typeArguments? '[' elements? ']' |
  //                     'const' typeArguments? '{' elements? '}' |
  //                     'const' '(' expression ')'
  public static boolean constantPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTANT_PATTERN, "<constant pattern>");
    r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = constantPattern_3(b, l + 1);
    if (!r) r = stringLiteralExpression(b, l + 1);
    if (!r) r = symbolLiteralExpression(b, l + 1);
    if (!r) r = simpleQualifiedReferenceExpression(b, l + 1);
    if (!r) r = constObjectExpression(b, l + 1);
    if (!r) r = constantPattern_8(b, l + 1);
    if (!r) r = constantPattern_9(b, l + 1);
    if (!r) r = constantPattern_10(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '-'? (NUMBER | HEX_NUMBER)
  private static boolean constantPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = constantPattern_3_0(b, l + 1);
    r = r && constantPattern_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '-'?
  private static boolean constantPattern_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_3_0")) return false;
    consumeToken(b, MINUS);
    return true;
  }

  // NUMBER | HEX_NUMBER
  private static boolean constantPattern_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_3_1")) return false;
    boolean r;
    r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, HEX_NUMBER);
    return r;
  }

  // 'const' typeArguments? '[' elements? ']'
  private static boolean constantPattern_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && constantPattern_8_1(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && constantPattern_8_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeArguments?
  private static boolean constantPattern_8_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_8_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // elements?
  private static boolean constantPattern_8_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_8_3")) return false;
    elements(b, l + 1);
    return true;
  }

  // 'const' typeArguments? '{' elements? '}'
  private static boolean constantPattern_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && constantPattern_9_1(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && constantPattern_9_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeArguments?
  private static boolean constantPattern_9_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_9_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // elements?
  private static boolean constantPattern_9_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_9_3")) return false;
    elements(b, l + 1);
    return true;
  }

  // 'const' '(' expression ')'
  private static boolean constantPattern_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constantPattern_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, CONST, LPAREN);
    r = r && expression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // simpleQualifiedReferenceExpression (typeArguments ('.' componentName)?)?
  static boolean constructorDesignation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constructorDesignation")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleQualifiedReferenceExpression(b, l + 1);
    r = r && constructorDesignation_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (typeArguments ('.' componentName)?)?
  private static boolean constructorDesignation_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constructorDesignation_1")) return false;
    constructorDesignation_1_0(b, l + 1);
    return true;
  }

  // typeArguments ('.' componentName)?
  private static boolean constructorDesignation_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constructorDesignation_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typeArguments(b, l + 1);
    r = r && constructorDesignation_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' componentName)?
  private static boolean constructorDesignation_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constructorDesignation_1_0_1")) return false;
    constructorDesignation_1_0_1_0(b, l + 1);
    return true;
  }

  // '.' componentName
  private static boolean constructorDesignation_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constructorDesignation_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'continue' referenceExpression? ';'
  public static boolean continueStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONTINUE_STATEMENT, null);
    r = consumeToken(b, CONTINUE);
    p = r; // pin = 1
    r = r && report_error_(b, continueStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // referenceExpression?
  private static boolean continueStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "continueStatement_1")) return false;
    referenceExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // topLevelDefinition*
  static boolean dartUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dartUnit")) return false;
    while (true) {
      int c = current_position_(b);
      if (!topLevelDefinition(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "dartUnit", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'required'? finalConstVarOrTypeAndComponentName
  static boolean declaredIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = declaredIdentifier_0(b, l + 1);
    r = r && declaredIdentifier_1(b, l + 1);
    r = r && finalConstVarOrTypeAndComponentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean declaredIdentifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "declaredIdentifier_0", c)) break;
    }
    return true;
  }

  // 'required'?
  private static boolean declaredIdentifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declaredIdentifier_1")) return false;
    consumeToken(b, REQUIRED);
    return true;
  }

  /* ********************************************************** */
  // label* 'default' ':' statements
  public static boolean defaultCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultCase")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DEFAULT_CASE, "<default case>");
    r = defaultCase_0(b, l + 1);
    r = r && consumeTokens(b, 1, DEFAULT, COLON);
    p = r; // pin = 2
    r = r && statements(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // label*
  private static boolean defaultCase_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultCase_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!label(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "defaultCase_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // normalFormalParameter (('=' | ':') expression)?
  public static boolean defaultFormalNamedParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DEFAULT_FORMAL_NAMED_PARAMETER, "<default formal named parameter>");
    r = normalFormalParameter(b, l + 1);
    r = r && defaultFormalNamedParameter_1(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::default_formal_parameter_recover);
    return r;
  }

  // (('=' | ':') expression)?
  private static boolean defaultFormalNamedParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter_1")) return false;
    defaultFormalNamedParameter_1_0(b, l + 1);
    return true;
  }

  // ('=' | ':') expression
  private static boolean defaultFormalNamedParameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = defaultFormalNamedParameter_1_0_0(b, l + 1);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '=' | ':'
  private static boolean defaultFormalNamedParameter_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defaultFormalNamedParameter_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, COLON);
    return r;
  }

  /* ********************************************************** */
  // !(')' | ',' | ']' | '}')
  static boolean default_formal_parameter_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_formal_parameter_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !default_formal_parameter_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | ',' | ']' | '}'
  private static boolean default_formal_parameter_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "default_formal_parameter_recover_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, RBRACKET);
    if (!r) r = consumeToken(b, RBRACE);
    return r;
  }

  /* ********************************************************** */
  // 'do' statement 'while' '(' expressionWithRecoverUntilParen ')' ';'
  public static boolean doWhileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doWhileStatement")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DO_WHILE_STATEMENT, null);
    r = consumeToken(b, DO);
    p = r; // pin = 1
    r = r && report_error_(b, statement(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, WHILE, LPAREN)) && r;
    r = p && report_error_(b, expressionWithRecoverUntilParen(b, l + 1)) && r;
    r = p && report_error_(b, consumeTokens(b, -1, RPAREN, SEMICOLON)) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // <<nonStrictID>> ('.' <<nonStrictID>>)*
  static boolean dottedName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dottedName")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    r = r && dottedName_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('.' <<nonStrictID>>)*
  private static boolean dottedName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dottedName_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!dottedName_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "dottedName_1", c)) break;
    }
    return true;
  }

  // '.' <<nonStrictID>>
  private static boolean dottedName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dottedName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // spreadElement | ifElement | forElement | mapEntry | expression
  public static boolean element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "element")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ELEMENT, "<element>");
    r = spreadElement(b, l + 1);
    if (!r) r = ifElement(b, l + 1);
    if (!r) r = forElement(b, l + 1);
    if (!r) r = mapEntry(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // element (','? element)* ','?
  static boolean elements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elements")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = element(b, l + 1);
    r = r && elements_1(b, l + 1);
    r = r && elements_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (','? element)*
  private static boolean elements_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elements_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!elements_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "elements_1", c)) break;
    }
    return true;
  }

  // ','? element
  private static boolean elements_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elements_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = elements_1_0_0(b, l + 1);
    r = r && element(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean elements_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elements_1_0_0")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // ','?
  private static boolean elements_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "elements_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // metadata* componentName typeArguments? '.' componentName arguments |
  //                             metadata* componentName typeArguments? arguments?
  public static boolean enumConstantDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENUM_CONSTANT_DECLARATION, "<enum constant declaration>");
    r = enumConstantDeclaration_0(b, l + 1);
    if (!r) r = enumConstantDeclaration_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata* componentName typeArguments? '.' componentName arguments
  private static boolean enumConstantDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumConstantDeclaration_0_0(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && enumConstantDeclaration_0_2(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && componentName(b, l + 1);
    r = r && arguments(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean enumConstantDeclaration_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumConstantDeclaration_0_0", c)) break;
    }
    return true;
  }

  // typeArguments?
  private static boolean enumConstantDeclaration_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_0_2")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // metadata* componentName typeArguments? arguments?
  private static boolean enumConstantDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumConstantDeclaration_1_0(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && enumConstantDeclaration_1_2(b, l + 1);
    r = r && enumConstantDeclaration_1_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean enumConstantDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumConstantDeclaration_1_0", c)) break;
    }
    return true;
  }

  // typeArguments?
  private static boolean enumConstantDeclaration_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_1_2")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // arguments?
  private static boolean enumConstantDeclaration_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumConstantDeclaration_1_3")) return false;
    arguments(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // metadata* 'enum' componentName typeParameters? mixins? interfaces? '{' enumConstantDeclaration? (',' enumConstantDeclaration)* ','? ';'? classMembers '}'
  public static boolean enumDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition")) return false;
    if (!nextTokenIs(b, "<enum definition>", AT, ENUM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_DEFINITION, "<enum definition>");
    r = enumDefinition_0(b, l + 1);
    r = r && consumeToken(b, ENUM);
    r = r && componentName(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, enumDefinition_3(b, l + 1));
    r = p && report_error_(b, enumDefinition_4(b, l + 1)) && r;
    r = p && report_error_(b, enumDefinition_5(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, LBRACE)) && r;
    r = p && report_error_(b, enumDefinition_7(b, l + 1)) && r;
    r = p && report_error_(b, enumDefinition_8(b, l + 1)) && r;
    r = p && report_error_(b, enumDefinition_9(b, l + 1)) && r;
    r = p && report_error_(b, enumDefinition_10(b, l + 1)) && r;
    r = p && report_error_(b, classMembers(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean enumDefinition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDefinition_0", c)) break;
    }
    return true;
  }

  // typeParameters?
  private static boolean enumDefinition_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_3")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // mixins?
  private static boolean enumDefinition_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_4")) return false;
    mixins(b, l + 1);
    return true;
  }

  // interfaces?
  private static boolean enumDefinition_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_5")) return false;
    interfaces(b, l + 1);
    return true;
  }

  // enumConstantDeclaration?
  private static boolean enumDefinition_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_7")) return false;
    enumConstantDeclaration(b, l + 1);
    return true;
  }

  // (',' enumConstantDeclaration)*
  private static boolean enumDefinition_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_8")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumDefinition_8_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "enumDefinition_8", c)) break;
    }
    return true;
  }

  // ',' enumConstantDeclaration
  private static boolean enumDefinition_8_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_8_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && enumConstantDeclaration(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean enumDefinition_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_9")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // ';'?
  private static boolean enumDefinition_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumDefinition_10")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // '==' | '!='
  public static boolean equalityOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "equalityOperator")) return false;
    if (!nextTokenIs(b, "<equality operator>", EQ_EQ, NEQ)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EQUALITY_OPERATOR, "<equality operator>");
    r = consumeToken(b, EQ_EQ);
    if (!r) r = consumeToken(b, NEQ);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'export' uriElement importConfig* combinator* ';'
  public static boolean exportStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement")) return false;
    if (!nextTokenIs(b, "<export statement>", AT, EXPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPORT_STATEMENT, "<export statement>");
    r = exportStatement_0(b, l + 1);
    r = r && consumeToken(b, EXPORT);
    r = r && uriElement(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, exportStatement_3(b, l + 1));
    r = p && report_error_(b, exportStatement_4(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean exportStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportStatement_0", c)) break;
    }
    return true;
  }

  // importConfig*
  private static boolean exportStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!importConfig(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportStatement_3", c)) break;
    }
    return true;
  }

  // combinator*
  private static boolean exportStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportStatement_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!combinator(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportStatement_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // assignExpressionWrapper
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EXPRESSION, "<expression>");
    r = assignExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::expression_recover);
    return r;
  }

  /* ********************************************************** */
  // 'case' expression ':'
  static boolean expressionCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionCase")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, CASE);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, COLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // expression | statement
  static boolean expressionInParentheses(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionInParentheses")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = expression(b, l + 1);
    if (!r) r = statement(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::parenthesesRecovery);
    return r;
  }

  /* ********************************************************** */
  // expression (','? expression)*
  public static boolean expressionList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, "<expression list>");
    r = expression(b, l + 1);
    r = r && expressionList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (','? expression)*
  private static boolean expressionList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expressionList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expressionList_1", c)) break;
    }
    return true;
  }

  // ','? expression
  private static boolean expressionList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expressionList_1_0_0(b, l + 1);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean expressionList_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionList_1_0_0")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // expression
  static boolean expressionWithRecoverUntilParen(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionWithRecoverUntilParen")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = expression(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::not_paren_recover);
    return r;
  }

  /* ********************************************************** */
  // expression
  static boolean expressionWithRecoverUntilParenOrComma(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expressionWithRecoverUntilParenOrComma")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = expression(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::not_paren_or_comma_recover);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> |
  //                                'async' | 'sync' | 'operator'
  //                                <<gtEq>> | <<gtGt>> | <<gtGtEq>> | <<gtGtGt>> | <<gtGtGtEq>> |
  //                                '!' | '!=' | '#' | '%' | '%=' | '&&' | '&&=' | '&' | '&=' | '(' | ')' | '*' | '*=' | '+' | '++' | '+=' |
  //                                ',' | '-' | '--' | '-=' | '.' | '..' | '...' | '...?' | '/' | '/=' | ':' | ';' | '<' | '<<' | '<<=' | '<=' |
  //                                '=' | '==' | '=>' | '>' | '?' | '?.' | '?..' | '??' | '??=' | '@' | '[' | ']' | '^' | '^=' | 'abstract' |
  //                                'as' | 'assert' | 'await' | 'base' | 'break' | 'case' | 'catch' | 'class' | 'const' | 'continue' |
  //                                'covariant' | 'default' | 'deferred' | 'do' | 'else' | 'enum' | 'export' | 'extension' | 'external' |
  //                                'factory' | 'final' | 'finally' | 'for' | 'get' | 'hide' | 'if' | 'import' | 'interface' | 'is' | 'late' |
  //                                'library' | 'mixin' | 'native' | 'new' | 'on' | 'part' | 'rethrow' | 'return' | 'sealed' | 'set' | 'show' |
  //                                'static' | 'super' | 'switch' | 'this' | 'throw' | 'try' | 'typedef' | 'var' | 'void' | 'when' | 'while' |
  //                                'yield' | '{' | '|' | '|=' | '||' | '||=' | '}' | '~' | '~/' | '~/=' | CLOSING_QUOTE | FALSE | HEX_NUMBER |
  //                                LONG_TEMPLATE_ENTRY_END | LONG_TEMPLATE_ENTRY_START | NULL | NUMBER | OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING |
  //                                RAW_TRIPLE_QUOTED_STRING | REGULAR_STRING_PART | SHORT_TEMPLATE_ENTRY_START | TRUE)
  static boolean expression_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !expression_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<nonStrictID>> |
  //                                'async' | 'sync' | 'operator'
  //                                <<gtEq>> | <<gtGt>> | <<gtGtEq>> | <<gtGtGt>> | <<gtGtGtEq>> |
  //                                '!' | '!=' | '#' | '%' | '%=' | '&&' | '&&=' | '&' | '&=' | '(' | ')' | '*' | '*=' | '+' | '++' | '+=' |
  //                                ',' | '-' | '--' | '-=' | '.' | '..' | '...' | '...?' | '/' | '/=' | ':' | ';' | '<' | '<<' | '<<=' | '<=' |
  //                                '=' | '==' | '=>' | '>' | '?' | '?.' | '?..' | '??' | '??=' | '@' | '[' | ']' | '^' | '^=' | 'abstract' |
  //                                'as' | 'assert' | 'await' | 'base' | 'break' | 'case' | 'catch' | 'class' | 'const' | 'continue' |
  //                                'covariant' | 'default' | 'deferred' | 'do' | 'else' | 'enum' | 'export' | 'extension' | 'external' |
  //                                'factory' | 'final' | 'finally' | 'for' | 'get' | 'hide' | 'if' | 'import' | 'interface' | 'is' | 'late' |
  //                                'library' | 'mixin' | 'native' | 'new' | 'on' | 'part' | 'rethrow' | 'return' | 'sealed' | 'set' | 'show' |
  //                                'static' | 'super' | 'switch' | 'this' | 'throw' | 'try' | 'typedef' | 'var' | 'void' | 'when' | 'while' |
  //                                'yield' | '{' | '|' | '|=' | '||' | '||=' | '}' | '~' | '~/' | '~/=' | CLOSING_QUOTE | FALSE | HEX_NUMBER |
  //                                LONG_TEMPLATE_ENTRY_END | LONG_TEMPLATE_ENTRY_START | NULL | NUMBER | OPEN_QUOTE | RAW_SINGLE_QUOTED_STRING |
  //                                RAW_TRIPLE_QUOTED_STRING | REGULAR_STRING_PART | SHORT_TEMPLATE_ENTRY_START | TRUE
  private static boolean expression_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, ASYNC);
    if (!r) r = consumeToken(b, SYNC);
    if (!r) r = expression_recover_0_3(b, l + 1);
    if (!r) r = gtGt(b, l + 1);
    if (!r) r = gtGtEq(b, l + 1);
    if (!r) r = gtGtGt(b, l + 1);
    if (!r) r = gtGtGtEq(b, l + 1);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, NEQ);
    if (!r) r = consumeToken(b, HASH);
    if (!r) r = consumeToken(b, REM);
    if (!r) r = consumeToken(b, REM_EQ);
    if (!r) r = consumeToken(b, AND_AND);
    if (!r) r = consumeToken(b, AND_AND_EQ);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, AND_EQ);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, MUL_EQ);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, PLUS_PLUS);
    if (!r) r = consumeToken(b, PLUS_EQ);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MINUS_MINUS);
    if (!r) r = consumeToken(b, MINUS_EQ);
    if (!r) r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, DOT_DOT);
    if (!r) r = consumeToken(b, DOT_DOT_DOT);
    if (!r) r = consumeToken(b, DOT_DOT_DOT_QUEST);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, DIV_EQ);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, LT_LT);
    if (!r) r = consumeToken(b, LT_LT_EQ);
    if (!r) r = consumeToken(b, LT_EQ);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, EQ_EQ);
    if (!r) r = consumeToken(b, EXPRESSION_BODY_DEF);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, QUEST);
    if (!r) r = consumeToken(b, QUEST_DOT);
    if (!r) r = consumeToken(b, QUEST_DOT_DOT);
    if (!r) r = consumeToken(b, QUEST_QUEST);
    if (!r) r = consumeToken(b, QUEST_QUEST_EQ);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, LBRACKET);
    if (!r) r = consumeToken(b, RBRACKET);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, XOR_EQ);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, AS);
    if (!r) r = consumeToken(b, ASSERT);
    if (!r) r = consumeToken(b, AWAIT);
    if (!r) r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, CATCH);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, COVARIANT);
    if (!r) r = consumeToken(b, DEFAULT);
    if (!r) r = consumeToken(b, DEFERRED);
    if (!r) r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTENSION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FACTORY);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, FINALLY);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, HIDE);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, IS);
    if (!r) r = consumeToken(b, LATE);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, MIXIN);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, NEW);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, RETHROW);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, SHOW);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, SUPER);
    if (!r) r = consumeToken(b, SWITCH);
    if (!r) r = consumeToken(b, THIS);
    if (!r) r = consumeToken(b, THROW);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, WHEN);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, YIELD);
    if (!r) r = consumeToken(b, LBRACE);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, OR_EQ);
    if (!r) r = consumeToken(b, OR_OR);
    if (!r) r = consumeToken(b, OR_OR_EQ);
    if (!r) r = consumeToken(b, RBRACE);
    if (!r) r = consumeToken(b, BIN_NOT);
    if (!r) r = consumeToken(b, INT_DIV);
    if (!r) r = consumeToken(b, INT_DIV_EQ);
    if (!r) r = consumeToken(b, CLOSING_QUOTE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, HEX_NUMBER);
    if (!r) r = consumeToken(b, LONG_TEMPLATE_ENTRY_END);
    if (!r) r = consumeToken(b, LONG_TEMPLATE_ENTRY_START);
    if (!r) r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, OPEN_QUOTE);
    if (!r) r = consumeToken(b, RAW_SINGLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, RAW_TRIPLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, REGULAR_STRING_PART);
    if (!r) r = consumeToken(b, SHORT_TEMPLATE_ENTRY_START);
    if (!r) r = consumeToken(b, TRUE);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'operator'
  //                                <<gtEq>>
  private static boolean expression_recover_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_recover_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPERATOR);
    r = r && gtEq(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'extension' typeParameters? 'on' type '?'? classBody |
  //                          'extension' <<nonStrictID>> typeParameters? 'on' type '?'? classBody
  public static boolean extensionDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration")) return false;
    if (!nextTokenIs(b, EXTENSION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = extensionDeclaration_0(b, l + 1);
    if (!r) r = extensionDeclaration_1(b, l + 1);
    exit_section_(b, m, EXTENSION_DECLARATION, r);
    return r;
  }

  // 'extension' typeParameters? 'on' type '?'? classBody
  private static boolean extensionDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTENSION);
    r = r && extensionDeclaration_0_1(b, l + 1);
    r = r && consumeToken(b, ON);
    r = r && type(b, l + 1);
    r = r && extensionDeclaration_0_4(b, l + 1);
    r = r && classBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean extensionDeclaration_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration_0_1")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // '?'?
  private static boolean extensionDeclaration_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration_0_4")) return false;
    consumeToken(b, QUEST);
    return true;
  }

  // 'extension' <<nonStrictID>> typeParameters? 'on' type '?'? classBody
  private static boolean extensionDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTENSION);
    r = r && nonStrictID(b, l + 1);
    r = r && extensionDeclaration_1_2(b, l + 1);
    r = r && consumeToken(b, ON);
    r = r && type(b, l + 1);
    r = r && extensionDeclaration_1_5(b, l + 1);
    r = r && classBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean extensionDeclaration_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration_1_2")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // '?'?
  private static boolean extensionDeclaration_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extensionDeclaration_1_5")) return false;
    consumeToken(b, QUEST);
    return true;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'const')* 'factory' componentName ('.' componentName)? formalParameterList factoryTail?
  public static boolean factoryConstructorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration")) return false;
    if (!nextTokenIs(b, "<factory constructor declaration>", AT, CONST, EXTERNAL, FACTORY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FACTORY_CONSTRUCTOR_DECLARATION, "<factory constructor declaration>");
    r = factoryConstructorDeclaration_0(b, l + 1);
    r = r && factoryConstructorDeclaration_1(b, l + 1);
    r = r && consumeToken(b, FACTORY);
    p = r; // pin = 3
    r = r && report_error_(b, componentName(b, l + 1));
    r = p && report_error_(b, factoryConstructorDeclaration_4(b, l + 1)) && r;
    r = p && report_error_(b, formalParameterList(b, l + 1)) && r;
    r = p && factoryConstructorDeclaration_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean factoryConstructorDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "factoryConstructorDeclaration_0", c)) break;
    }
    return true;
  }

  // ('external' | 'const')*
  private static boolean factoryConstructorDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!factoryConstructorDeclaration_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "factoryConstructorDeclaration_1", c)) break;
    }
    return true;
  }

  // 'external' | 'const'
  private static boolean factoryConstructorDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, CONST);
    return r;
  }

  // ('.' componentName)?
  private static boolean factoryConstructorDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_4")) return false;
    factoryConstructorDeclaration_4_0(b, l + 1);
    return true;
  }

  // '.' componentName
  private static boolean factoryConstructorDeclaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // factoryTail?
  private static boolean factoryConstructorDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryConstructorDeclaration_6")) return false;
    factoryTail(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // varFactoryDeclaration ';' | functionBodyOrNative | ';'
  static boolean factoryTail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryTail")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = factoryTail_0(b, l + 1);
    if (!r) r = functionBodyOrNative(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // varFactoryDeclaration ';'
  private static boolean factoryTail_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factoryTail_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = varFactoryDeclaration(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // metadata* 'required'? finalConstVarVoidOrType? ('this' | 'super') '.' referenceExpression typeParameters? formalParameterList?
  public static boolean fieldFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_FORMAL_PARAMETER, "<field formal parameter>");
    r = fieldFormalParameter_0(b, l + 1);
    r = r && fieldFormalParameter_1(b, l + 1);
    r = r && fieldFormalParameter_2(b, l + 1);
    r = r && fieldFormalParameter_3(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && referenceExpression(b, l + 1);
    r = r && fieldFormalParameter_6(b, l + 1);
    r = r && fieldFormalParameter_7(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean fieldFormalParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "fieldFormalParameter_0", c)) break;
    }
    return true;
  }

  // 'required'?
  private static boolean fieldFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_1")) return false;
    consumeToken(b, REQUIRED);
    return true;
  }

  // finalConstVarVoidOrType?
  private static boolean fieldFormalParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_2")) return false;
    finalConstVarVoidOrType(b, l + 1);
    return true;
  }

  // 'this' | 'super'
  private static boolean fieldFormalParameter_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_3")) return false;
    boolean r;
    r = consumeToken(b, THIS);
    if (!r) r = consumeToken(b, SUPER);
    return r;
  }

  // typeParameters?
  private static boolean fieldFormalParameter_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_6")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // formalParameterList?
  private static boolean fieldFormalParameter_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldFormalParameter_7")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ('this' '.')? referenceExpression '=' expression
  public static boolean fieldInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldInitializer")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD_INITIALIZER, "<field initializer>");
    r = fieldInitializer_0(b, l + 1);
    r = r && referenceExpression(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, EQ));
    r = p && expression(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('this' '.')?
  private static boolean fieldInitializer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldInitializer_0")) return false;
    fieldInitializer_0_0(b, l + 1);
    return true;
  }

  // 'this' '.'
  private static boolean fieldInitializer_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldInitializer_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, THIS, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'covariant'? 'late'? 'final' type componentName |
  //                                                 'covariant'? 'late'? 'final'      componentName |
  //                                                 'covariant'?         'const' type componentName |
  //                                                 'covariant'?         'const'      componentName |
  //                                                 'covariant'? 'late'? 'var'        componentName |
  //                                                 'covariant'  'late'          type componentName |
  //                                                 'covariant'                  type componentName |
  //                                                              'late'          type componentName |
  //                                                                              type componentName
  static boolean finalConstVarOrTypeAndComponentName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_0(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_1(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_2(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_3(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_4(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_5(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_6(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_7(b, l + 1);
    if (!r) r = finalConstVarOrTypeAndComponentName_8(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'? 'late'? 'final' type componentName
  private static boolean finalConstVarOrTypeAndComponentName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_0_0(b, l + 1);
    r = r && finalConstVarOrTypeAndComponentName_0_1(b, l + 1);
    r = r && consumeToken(b, FINAL);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean finalConstVarOrTypeAndComponentName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_0_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'late'?
  private static boolean finalConstVarOrTypeAndComponentName_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_0_1")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'covariant'? 'late'? 'final'      componentName
  private static boolean finalConstVarOrTypeAndComponentName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_1_0(b, l + 1);
    r = r && finalConstVarOrTypeAndComponentName_1_1(b, l + 1);
    r = r && consumeToken(b, FINAL);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean finalConstVarOrTypeAndComponentName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_1_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'late'?
  private static boolean finalConstVarOrTypeAndComponentName_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_1_1")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'covariant'?         'const' type componentName
  private static boolean finalConstVarOrTypeAndComponentName_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_2_0(b, l + 1);
    r = r && consumeToken(b, CONST);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean finalConstVarOrTypeAndComponentName_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_2_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'covariant'?         'const'      componentName
  private static boolean finalConstVarOrTypeAndComponentName_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_3_0(b, l + 1);
    r = r && consumeToken(b, CONST);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean finalConstVarOrTypeAndComponentName_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_3_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'covariant'? 'late'? 'var'        componentName
  private static boolean finalConstVarOrTypeAndComponentName_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarOrTypeAndComponentName_4_0(b, l + 1);
    r = r && finalConstVarOrTypeAndComponentName_4_1(b, l + 1);
    r = r && consumeToken(b, VAR);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean finalConstVarOrTypeAndComponentName_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_4_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'late'?
  private static boolean finalConstVarOrTypeAndComponentName_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_4_1")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'covariant'  'late'          type componentName
  private static boolean finalConstVarOrTypeAndComponentName_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COVARIANT, LATE);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'                  type componentName
  private static boolean finalConstVarOrTypeAndComponentName_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COVARIANT);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'late'          type componentName
  private static boolean finalConstVarOrTypeAndComponentName_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LATE);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // type componentName
  private static boolean finalConstVarOrTypeAndComponentName_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarOrTypeAndComponentName_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'late'? 'final' type? | 'const' type? | 'late'? 'var' | 'late'? 'void' | 'late' type | type
  static boolean finalConstVarVoidOrType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarVoidOrType_0(b, l + 1);
    if (!r) r = finalConstVarVoidOrType_1(b, l + 1);
    if (!r) r = finalConstVarVoidOrType_2(b, l + 1);
    if (!r) r = finalConstVarVoidOrType_3(b, l + 1);
    if (!r) r = finalConstVarVoidOrType_4(b, l + 1);
    if (!r) r = type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'late'? 'final' type?
  private static boolean finalConstVarVoidOrType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarVoidOrType_0_0(b, l + 1);
    r = r && consumeToken(b, FINAL);
    r = r && finalConstVarVoidOrType_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'late'?
  private static boolean finalConstVarVoidOrType_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_0_0")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // type?
  private static boolean finalConstVarVoidOrType_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_0_2")) return false;
    type(b, l + 1);
    return true;
  }

  // 'const' type?
  private static boolean finalConstVarVoidOrType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONST);
    r = r && finalConstVarVoidOrType_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // type?
  private static boolean finalConstVarVoidOrType_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_1_1")) return false;
    type(b, l + 1);
    return true;
  }

  // 'late'? 'var'
  private static boolean finalConstVarVoidOrType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarVoidOrType_2_0(b, l + 1);
    r = r && consumeToken(b, VAR);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'late'?
  private static boolean finalConstVarVoidOrType_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_2_0")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'late'? 'void'
  private static boolean finalConstVarVoidOrType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = finalConstVarVoidOrType_3_0(b, l + 1);
    r = r && consumeToken(b, VOID);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'late'?
  private static boolean finalConstVarVoidOrType_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_3_0")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'late' type
  private static boolean finalConstVarVoidOrType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalConstVarVoidOrType_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LATE);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'final' | 'const'
  static boolean finalOrConst(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finalOrConst")) return false;
    if (!nextTokenIs(b, "", CONST, FINAL)) return false;
    boolean r;
    r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, CONST);
    return r;
  }

  /* ********************************************************** */
  // 'finally' block
  public static boolean finallyPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "finallyPart")) return false;
    if (!nextTokenIs(b, FINALLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FINALLY_PART, null);
    r = consumeToken(b, FINALLY);
    p = r; // pin = 1
    r = r && block(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'await'? 'for' '(' forLoopParts ')' element
  public static boolean forElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forElement")) return false;
    if (!nextTokenIs(b, "<for element>", AWAIT, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_ELEMENT, "<for element>");
    r = forElement_0(b, l + 1);
    r = r && consumeTokens(b, 1, FOR, LPAREN);
    p = r; // pin = 2
    r = r && report_error_(b, forLoopParts(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && element(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'await'?
  private static boolean forElement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forElement_0")) return false;
    consumeToken(b, AWAIT);
    return true;
  }

  /* ********************************************************** */
  // (varAccessDeclaration | componentName) 'in' expression |
  //               metadata* ( 'final' | 'var' ) outerPattern 'in' expression
  public static boolean forInPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_IN_PART, "<for in part>");
    r = forInPart_0(b, l + 1);
    if (!r) r = forInPart_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (varAccessDeclaration | componentName) 'in' expression
  private static boolean forInPart_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forInPart_0_0(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // varAccessDeclaration | componentName
  private static boolean forInPart_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart_0_0")) return false;
    boolean r;
    r = varAccessDeclaration(b, l + 1);
    if (!r) r = componentName(b, l + 1);
    return r;
  }

  // metadata* ( 'final' | 'var' ) outerPattern 'in' expression
  private static boolean forInPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forInPart_1_0(b, l + 1);
    r = r && forInPart_1_1(b, l + 1);
    r = r && outerPattern(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean forInPart_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "forInPart_1_0", c)) break;
    }
    return true;
  }

  // 'final' | 'var'
  private static boolean forInPart_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forInPart_1_1")) return false;
    boolean r;
    r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  /* ********************************************************** */
  // forInPart
  //               | varDeclarationList (';' expression? (';' expressionList?)?)?
  //               | patternVariableDeclaration (';' expression? (';' expressionList?)?)?
  //               | expressionList? (';' expression? (';' expressionList?)?)?
  public static boolean forLoopParts(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FOR_LOOP_PARTS, "<for loop parts>");
    r = forInPart(b, l + 1);
    if (!r) r = forLoopParts_1(b, l + 1);
    if (!r) r = forLoopParts_2(b, l + 1);
    if (!r) r = forLoopParts_3(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::for_loops_parts_recover);
    return r;
  }

  // varDeclarationList (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varDeclarationList(b, l + 1);
    r = r && forLoopParts_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1")) return false;
    forLoopParts_1_1_0(b, l + 1);
    return true;
  }

  // ';' expression? (';' expressionList?)?
  private static boolean forLoopParts_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_1_1_0_1(b, l + 1);
    r = r && forLoopParts_1_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean forLoopParts_1_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_1")) return false;
    expression(b, l + 1);
    return true;
  }

  // (';' expressionList?)?
  private static boolean forLoopParts_1_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_2")) return false;
    forLoopParts_1_1_0_2_0(b, l + 1);
    return true;
  }

  // ';' expressionList?
  private static boolean forLoopParts_1_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_1_1_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_1_1_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_1_1_0_2_0_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // patternVariableDeclaration (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternVariableDeclaration(b, l + 1);
    r = r && forLoopParts_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1")) return false;
    forLoopParts_2_1_0(b, l + 1);
    return true;
  }

  // ';' expression? (';' expressionList?)?
  private static boolean forLoopParts_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_2_1_0_1(b, l + 1);
    r = r && forLoopParts_2_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean forLoopParts_2_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_1")) return false;
    expression(b, l + 1);
    return true;
  }

  // (';' expressionList?)?
  private static boolean forLoopParts_2_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_2")) return false;
    forLoopParts_2_1_0_2_0(b, l + 1);
    return true;
  }

  // ';' expressionList?
  private static boolean forLoopParts_2_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_2_1_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_2_1_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_2_1_0_2_0_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // expressionList? (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = forLoopParts_3_0(b, l + 1);
    r = r && forLoopParts_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_0")) return false;
    expressionList(b, l + 1);
    return true;
  }

  // (';' expression? (';' expressionList?)?)?
  private static boolean forLoopParts_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_1")) return false;
    forLoopParts_3_1_0(b, l + 1);
    return true;
  }

  // ';' expression? (';' expressionList?)?
  private static boolean forLoopParts_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_3_1_0_1(b, l + 1);
    r = r && forLoopParts_3_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expression?
  private static boolean forLoopParts_3_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_1_0_1")) return false;
    expression(b, l + 1);
    return true;
  }

  // (';' expressionList?)?
  private static boolean forLoopParts_3_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_1_0_2")) return false;
    forLoopParts_3_1_0_2_0(b, l + 1);
    return true;
  }

  // ';' expressionList?
  private static boolean forLoopParts_3_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_1_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    r = r && forLoopParts_3_1_0_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expressionList?
  private static boolean forLoopParts_3_1_0_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopParts_3_1_0_2_0_1")) return false;
    expressionList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' forLoopParts ')'
  public static boolean forLoopPartsInBraces(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forLoopPartsInBraces")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && forLoopParts(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, FOR_LOOP_PARTS_IN_BRACES, r);
    return r;
  }

  /* ********************************************************** */
  // 'await'? 'for' forLoopPartsInBraces statement
  public static boolean forStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement")) return false;
    if (!nextTokenIs(b, "<for statement>", AWAIT, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, "<for statement>");
    r = forStatement_0(b, l + 1);
    r = r && consumeToken(b, FOR);
    p = r; // pin = 2
    r = r && report_error_(b, forLoopPartsInBraces(b, l + 1));
    r = p && statement(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'await'?
  private static boolean forStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forStatement_0")) return false;
    consumeToken(b, AWAIT);
    return true;
  }

  /* ********************************************************** */
  // !')'
  static boolean for_loops_parts_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_loops_parts_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' ')' |
  //                         '(' normalFormalParameters (',' optionalFormalParameters)? ','? ')' |
  //                         '(' optionalFormalParameters ')'
  public static boolean formalParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, LPAREN, RPAREN);
    if (!r) r = formalParameterList_1(b, l + 1);
    if (!r) r = formalParameterList_2(b, l + 1);
    exit_section_(b, m, FORMAL_PARAMETER_LIST, r);
    return r;
  }

  // '(' normalFormalParameters (',' optionalFormalParameters)? ','? ')'
  private static boolean formalParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && normalFormalParameters(b, l + 1);
    r = r && formalParameterList_1_2(b, l + 1);
    r = r && formalParameterList_1_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' optionalFormalParameters)?
  private static boolean formalParameterList_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1_2")) return false;
    formalParameterList_1_2_0(b, l + 1);
    return true;
  }

  // ',' optionalFormalParameters
  private static boolean formalParameterList_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && optionalFormalParameters(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean formalParameterList_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1_3")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // '(' optionalFormalParameters ')'
  private static boolean formalParameterList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && optionalFormalParameters(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // arrowBodyWithSemi | <<blockBodyWrapper>>
  public static boolean functionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_BODY, "<function body>");
    r = arrowBodyWithSemi(b, l + 1);
    if (!r) r = blockBodyWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'native' functionBody
  //                                | functionNative
  //                                | functionBody
  static boolean functionBodyOrNative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBodyOrNative")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionBodyOrNative_0(b, l + 1);
    if (!r) r = functionNative(b, l + 1);
    if (!r) r = functionBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'native' functionBody
  private static boolean functionBodyOrNative_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionBodyOrNative_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NATIVE);
    r = r && functionBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata*  (returnType componentName typeParameters? formalParameterList | componentName typeParameters? formalParameterList) functionBody
  public static boolean functionDeclarationWithBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DECLARATION_WITH_BODY, "<function declaration with body>");
    r = functionDeclarationWithBody_0(b, l + 1);
    r = r && functionDeclarationWithBody_1(b, l + 1);
    r = r && functionBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean functionDeclarationWithBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDeclarationWithBody_0", c)) break;
    }
    return true;
  }

  // returnType componentName typeParameters? formalParameterList | componentName typeParameters? formalParameterList
  private static boolean functionDeclarationWithBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionDeclarationWithBody_1_0(b, l + 1);
    if (!r) r = functionDeclarationWithBody_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName typeParameters? formalParameterList
  private static boolean functionDeclarationWithBody_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && functionDeclarationWithBody_1_0_2(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean functionDeclarationWithBody_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1_0_2")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // componentName typeParameters? formalParameterList
  private static boolean functionDeclarationWithBody_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = componentName(b, l + 1);
    r = r && functionDeclarationWithBody_1_1_1(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean functionDeclarationWithBody_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBody_1_1_1")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // metadata* 'external'? (returnType componentName typeParameters? formalParameterList | componentName typeParameters? formalParameterList) ( ';' | functionBodyOrNative)
  public static boolean functionDeclarationWithBodyOrNative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DECLARATION_WITH_BODY_OR_NATIVE, "<function declaration with body or native>");
    r = functionDeclarationWithBodyOrNative_0(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_1(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_2(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean functionDeclarationWithBodyOrNative_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionDeclarationWithBodyOrNative_0", c)) break;
    }
    return true;
  }

  // 'external'?
  private static boolean functionDeclarationWithBodyOrNative_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_1")) return false;
    consumeToken(b, EXTERNAL);
    return true;
  }

  // returnType componentName typeParameters? formalParameterList | componentName typeParameters? formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionDeclarationWithBodyOrNative_2_0(b, l + 1);
    if (!r) r = functionDeclarationWithBodyOrNative_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName typeParameters? formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_2_0_2(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean functionDeclarationWithBodyOrNative_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2_0_2")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // componentName typeParameters? formalParameterList
  private static boolean functionDeclarationWithBodyOrNative_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = componentName(b, l + 1);
    r = r && functionDeclarationWithBodyOrNative_2_1_1(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean functionDeclarationWithBodyOrNative_2_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_2_1_1")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean functionDeclarationWithBodyOrNative_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDeclarationWithBodyOrNative_3")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // typeParameters? formalParameterList functionExpressionBody
  public static boolean functionExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionExpression")) return false;
    if (!nextTokenIs(b, "<function expression>", LPAREN, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_EXPRESSION, "<function expression>");
    r = functionExpression_0(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    r = r && functionExpressionBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeParameters?
  private static boolean functionExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionExpression_0")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // <<arrowBodyWrapper>> | <<blockBodyWrapper>>
  public static boolean functionExpressionBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionExpressionBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_EXPRESSION_BODY, "<function expression body>");
    r = arrowBodyWrapper(b, l + 1);
    if (!r) r = blockBodyWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !functionTypeWrapper (metadata*                         componentName typeParameters? formalParameterList |
  //                                                   metadata* 'covariant'? returnType componentName typeParameters? formalParameterList |
  //                                                   metadata* 'covariant'             componentName typeParameters? formalParameterList)
  //                             '?'?
  public static boolean functionFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_FORMAL_PARAMETER, "<function formal parameter>");
    r = functionFormalParameter_0(b, l + 1);
    r = r && functionFormalParameter_1(b, l + 1);
    r = r && functionFormalParameter_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !functionTypeWrapper
  private static boolean functionFormalParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !functionTypeWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*                         componentName typeParameters? formalParameterList |
  //                                                   metadata* 'covariant'? returnType componentName typeParameters? formalParameterList |
  //                                                   metadata* 'covariant'             componentName typeParameters? formalParameterList
  private static boolean functionFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionFormalParameter_1_0(b, l + 1);
    if (!r) r = functionFormalParameter_1_1(b, l + 1);
    if (!r) r = functionFormalParameter_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*                         componentName typeParameters? formalParameterList
  private static boolean functionFormalParameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionFormalParameter_1_0_0(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && functionFormalParameter_1_0_2(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean functionFormalParameter_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionFormalParameter_1_0_0", c)) break;
    }
    return true;
  }

  // typeParameters?
  private static boolean functionFormalParameter_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_0_2")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // metadata* 'covariant'? returnType componentName typeParameters? formalParameterList
  private static boolean functionFormalParameter_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionFormalParameter_1_1_0(b, l + 1);
    r = r && functionFormalParameter_1_1_1(b, l + 1);
    r = r && returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && functionFormalParameter_1_1_4(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean functionFormalParameter_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionFormalParameter_1_1_0", c)) break;
    }
    return true;
  }

  // 'covariant'?
  private static boolean functionFormalParameter_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_1_1")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // typeParameters?
  private static boolean functionFormalParameter_1_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_1_4")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // metadata* 'covariant'             componentName typeParameters? formalParameterList
  private static boolean functionFormalParameter_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionFormalParameter_1_2_0(b, l + 1);
    r = r && consumeToken(b, COVARIANT);
    r = r && componentName(b, l + 1);
    r = r && functionFormalParameter_1_2_3(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean functionFormalParameter_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_2_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionFormalParameter_1_2_0", c)) break;
    }
    return true;
  }

  // typeParameters?
  private static boolean functionFormalParameter_1_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_1_2_3")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // '?'?
  private static boolean functionFormalParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionFormalParameter_2")) return false;
    consumeToken(b, QUEST);
    return true;
  }

  /* ********************************************************** */
  // 'native' (stringLiteralExpression ';' | ';' | stringLiteralExpression functionBody)
  static boolean functionNative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative")) return false;
    if (!nextTokenIs(b, NATIVE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NATIVE);
    r = r && functionNative_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression ';' | ';' | stringLiteralExpression functionBody
  private static boolean functionNative_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionNative_1_0(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = functionNative_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression ';'
  private static boolean functionNative_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringLiteralExpression(b, l + 1);
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression functionBody
  private static boolean functionNative_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionNative_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringLiteralExpression(b, l + 1);
    r = r && functionBody(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // returnType componentName | componentName
  static boolean functionPrefix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionPrefix")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionPrefix_0(b, l + 1);
    if (!r) r = componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType componentName
  private static boolean functionPrefix_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionPrefix_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'typedef' functionPrefix typeParameters? formalParameterList ';'?
  //                     | metadata* 'typedef' componentName typeParameters? '=' type ';'?
  public static boolean functionTypeAlias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias")) return false;
    if (!nextTokenIs(b, "<function type alias>", AT, TYPEDEF)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_TYPE_ALIAS, "<function type alias>");
    r = functionTypeAlias_0(b, l + 1);
    if (!r) r = functionTypeAlias_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata* 'typedef' functionPrefix typeParameters? formalParameterList ';'?
  private static boolean functionTypeAlias_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionTypeAlias_0_0(b, l + 1);
    r = r && consumeToken(b, TYPEDEF);
    r = r && functionPrefix(b, l + 1);
    r = r && functionTypeAlias_0_3(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    r = r && functionTypeAlias_0_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean functionTypeAlias_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionTypeAlias_0_0", c)) break;
    }
    return true;
  }

  // typeParameters?
  private static boolean functionTypeAlias_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_0_3")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // ';'?
  private static boolean functionTypeAlias_0_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_0_5")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  // metadata* 'typedef' componentName typeParameters? '=' type ';'?
  private static boolean functionTypeAlias_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionTypeAlias_1_0(b, l + 1);
    r = r && consumeToken(b, TYPEDEF);
    r = r && componentName(b, l + 1);
    r = r && functionTypeAlias_1_3(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && type(b, l + 1);
    r = r && functionTypeAlias_1_6(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean functionTypeAlias_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionTypeAlias_1_0", c)) break;
    }
    return true;
  }

  // typeParameters?
  private static boolean functionTypeAlias_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_1_3")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // ';'?
  private static boolean functionTypeAlias_1_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeAlias_1_6")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // (voidTypeFunctionType | untypedFunctionType | simpleType) typedFunctionType+ | untypedFunctionType
  static boolean functionTypeWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionTypeWrapper_0(b, l + 1);
    if (!r) r = untypedFunctionType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (voidTypeFunctionType | untypedFunctionType | simpleType) typedFunctionType+
  private static boolean functionTypeWrapper_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeWrapper_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = functionTypeWrapper_0_0(b, l + 1);
    r = r && functionTypeWrapper_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // voidTypeFunctionType | untypedFunctionType | simpleType
  private static boolean functionTypeWrapper_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeWrapper_0_0")) return false;
    boolean r;
    r = voidTypeFunctionType(b, l + 1);
    if (!r) r = untypedFunctionType(b, l + 1);
    if (!r) r = simpleType(b, l + 1);
    return r;
  }

  // typedFunctionType+
  private static boolean functionTypeWrapper_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionTypeWrapper_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = typedFunctionType(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!typedFunctionType(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "functionTypeWrapper_0_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // getterDeclarationWithReturnType | getterDeclarationWithoutReturnType
  public static boolean getterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GETTER_DECLARATION, "<getter declaration>");
    r = getterDeclarationWithReturnType(b, l + 1);
    if (!r) r = getterDeclarationWithoutReturnType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')* returnType 'get' componentName formalParameterList? (';' | functionBodyOrNative)
  static boolean getterDeclarationWithReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = getterDeclarationWithReturnType_0(b, l + 1);
    r = r && getterDeclarationWithReturnType_1(b, l + 1);
    r = r && returnType(b, l + 1);
    r = r && consumeToken(b, GET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 5
    r = r && report_error_(b, getterDeclarationWithReturnType_5(b, l + 1));
    r = p && getterDeclarationWithReturnType_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean getterDeclarationWithReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithReturnType_0", c)) break;
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean getterDeclarationWithReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!getterDeclarationWithReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithReturnType_1", c)) break;
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean getterDeclarationWithReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    return r;
  }

  // formalParameterList?
  private static boolean getterDeclarationWithReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_5")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean getterDeclarationWithReturnType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithReturnType_6")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')*            'get' componentName formalParameterList? (';' | functionBodyOrNative)
  static boolean getterDeclarationWithoutReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(b, "", AT, EXTERNAL, GET, STATIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = getterDeclarationWithoutReturnType_0(b, l + 1);
    r = r && getterDeclarationWithoutReturnType_1(b, l + 1);
    r = r && consumeToken(b, GET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, getterDeclarationWithoutReturnType_4(b, l + 1));
    r = p && getterDeclarationWithoutReturnType_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean getterDeclarationWithoutReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithoutReturnType_0", c)) break;
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean getterDeclarationWithoutReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!getterDeclarationWithoutReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "getterDeclarationWithoutReturnType_1", c)) break;
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean getterDeclarationWithoutReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    return r;
  }

  // formalParameterList?
  private static boolean getterDeclarationWithoutReturnType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_4")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative
  private static boolean getterDeclarationWithoutReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterDeclarationWithoutReturnType_5")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // getterDeclaration | setterDeclaration
  static boolean getterOrSetterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "getterOrSetterDeclaration")) return false;
    boolean r;
    r = getterDeclaration(b, l + 1);
    if (!r) r = setterDeclaration(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // pattern ('when' expression)?
  static boolean guardedPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "guardedPattern")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = pattern(b, l + 1);
    r = r && guardedPattern_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('when' expression)?
  private static boolean guardedPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "guardedPattern_1")) return false;
    guardedPattern_1_0(b, l + 1);
    return true;
  }

  // 'when' expression
  private static boolean guardedPattern_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "guardedPattern_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    r = r && expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'hide' libraryReferenceList
  public static boolean hideCombinator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "hideCombinator")) return false;
    if (!nextTokenIs(b, HIDE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HIDE_COMBINATOR, null);
    r = consumeToken(b, HIDE);
    p = r; // pin = 1
    r = r && libraryReferenceList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, ID, r);
    return r;
  }

  /* ********************************************************** */
  // componentName
  public static boolean identifierPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifierPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IDENTIFIER_PATTERN, "<identifier pattern>");
    r = componentName(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'if' '(' expressionWithRecoverUntilParen ')' element ('else' element)?
  public static boolean ifElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_ELEMENT, null);
    r = consumeTokens(b, 1, IF, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionWithRecoverUntilParen(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && report_error_(b, element(b, l + 1)) && r;
    r = p && ifElement_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('else' element)?
  private static boolean ifElement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElement_5")) return false;
    ifElement_5_0(b, l + 1);
    return true;
  }

  // 'else' element
  private static boolean ifElement_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifElement_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && element(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '??' logicOrExpressionWrapper
  public static boolean ifNullExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifNullExpression")) return false;
    if (!nextTokenIs(b, QUEST_QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, IF_NULL_EXPRESSION, null);
    r = consumeToken(b, QUEST_QUEST);
    r = r && logicOrExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // logicOrExpressionWrapper ifNullExpression*
  static boolean ifNullExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifNullExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = logicOrExpressionWrapper(b, l + 1);
    r = r && ifNullExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ifNullExpression*
  private static boolean ifNullExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifNullExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ifNullExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ifNullExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // 'if' '(' expressionWithRecoverUntilParen ')' statement ('else' statement)?
  public static boolean ifStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_STATEMENT, null);
    r = consumeTokens(b, 1, IF, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionWithRecoverUntilParen(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && report_error_(b, statement(b, l + 1)) && r;
    r = p && ifStatement_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('else' statement)?
  private static boolean ifStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5")) return false;
    ifStatement_5_0(b, l + 1);
    return true;
  }

  // 'else' statement
  private static boolean ifStatement_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifStatement_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && statement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'if' '(' dottedName ('==' stringLiteralExpression)? ')' stringLiteralExpression
  static boolean importConfig(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importConfig")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IF, LPAREN);
    r = r && dottedName(b, l + 1);
    r = r && importConfig_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && stringLiteralExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('==' stringLiteralExpression)?
  private static boolean importConfig_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importConfig_3")) return false;
    importConfig_3_0(b, l + 1);
    return true;
  }

  // '==' stringLiteralExpression
  private static boolean importConfig_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importConfig_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ_EQ);
    r = r && stringLiteralExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'import' uriElement importConfig* ('deferred'? 'as' componentName )? combinator* ';'
  public static boolean importStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement")) return false;
    if (!nextTokenIs(b, "<import statement>", AT, IMPORT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IMPORT_STATEMENT, "<import statement>");
    r = importStatement_0(b, l + 1);
    r = r && consumeToken(b, IMPORT);
    r = r && uriElement(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, importStatement_3(b, l + 1));
    r = p && report_error_(b, importStatement_4(b, l + 1)) && r;
    r = p && report_error_(b, importStatement_5(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean importStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "importStatement_0", c)) break;
    }
    return true;
  }

  // importConfig*
  private static boolean importStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!importConfig(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "importStatement_3", c)) break;
    }
    return true;
  }

  // ('deferred'? 'as' componentName )?
  private static boolean importStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_4")) return false;
    importStatement_4_0(b, l + 1);
    return true;
  }

  // 'deferred'? 'as' componentName
  private static boolean importStatement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = importStatement_4_0_0(b, l + 1);
    r = r && consumeToken(b, AS);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'deferred'?
  private static boolean importStatement_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_4_0_0")) return false;
    consumeToken(b, DEFERRED);
    return true;
  }

  // combinator*
  private static boolean importStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "importStatement_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!combinator(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "importStatement_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static' | 'final' | 'const' | 'covariant')* type | metadata+
  public static boolean incompleteDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INCOMPLETE_DECLARATION, "<incomplete declaration>");
    r = incompleteDeclaration_0(b, l + 1);
    if (!r) r = incompleteDeclaration_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata* ('external' | 'static' | 'final' | 'const' | 'covariant')* type
  private static boolean incompleteDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = incompleteDeclaration_0_0(b, l + 1);
    r = r && incompleteDeclaration_0_1(b, l + 1);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean incompleteDeclaration_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incompleteDeclaration_0_0", c)) break;
    }
    return true;
  }

  // ('external' | 'static' | 'final' | 'const' | 'covariant')*
  private static boolean incompleteDeclaration_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!incompleteDeclaration_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incompleteDeclaration_0_1", c)) break;
    }
    return true;
  }

  // 'external' | 'static' | 'final' | 'const' | 'covariant'
  private static boolean incompleteDeclaration_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_0_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, COVARIANT);
    return r;
  }

  // metadata+
  private static boolean incompleteDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incompleteDeclaration_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = metadata(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incompleteDeclaration_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<' + <<nonStrictID>> + '>' <<nonStrictID>>
  static boolean incorrectNormalFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incorrectNormalFormalParameter")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = incorrectNormalFormalParameter_0(b, l + 1);
    r = r && incorrectNormalFormalParameter_1(b, l + 1);
    r = r && consumeToken(b, GT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '<' +
  private static boolean incorrectNormalFormalParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incorrectNormalFormalParameter_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, LT)) break;
      if (!empty_element_parsed_guard_(b, "incorrectNormalFormalParameter_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // <<nonStrictID>> +
  private static boolean incorrectNormalFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "incorrectNormalFormalParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!nonStrictID(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "incorrectNormalFormalParameter_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ':' superCallOrFieldInitializer (',' superCallOrFieldInitializer)*
  public static boolean initializers(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initializers")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && superCallOrFieldInitializer(b, l + 1);
    r = r && initializers_2(b, l + 1);
    exit_section_(b, m, INITIALIZERS, r);
    return r;
  }

  // (',' superCallOrFieldInitializer)*
  private static boolean initializers_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initializers_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!initializers_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "initializers_2", c)) break;
    }
    return true;
  }

  // ',' superCallOrFieldInitializer
  private static boolean initializers_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "initializers_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && superCallOrFieldInitializer(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'implements' typeList
  public static boolean interfaces(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaces")) return false;
    if (!nextTokenIs(b, IMPLEMENTS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INTERFACES, null);
    r = consumeToken(b, IMPLEMENTS);
    p = r; // pin = 1
    r = r && typeList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'is' '!'? type
  public static boolean isExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isExpression")) return false;
    if (!nextTokenIs(b, IS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, IS_EXPRESSION, null);
    r = consumeToken(b, IS);
    r = r && isExpression_1(b, l + 1);
    r = r && type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '!'?
  private static boolean isExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "isExpression_1")) return false;
    consumeToken(b, NOT);
    return true;
  }

  /* ********************************************************** */
  // componentName ':'
  public static boolean label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LABEL, "<label>");
    r = componentName(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<lazyParseableBlockImpl>>
  static boolean lazyParseableBlock(PsiBuilder b, int l) {
    return lazyParseableBlockImpl(b, l + 1);
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean libraryComponentReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryComponentReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, LIBRARY_COMPONENT_REFERENCE_EXPRESSION, "<library component reference expression>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<nonStrictID>> ('.' <<nonStrictID>>)*
  public static boolean libraryId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryId")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIBRARY_ID, "<library id>");
    r = nonStrictID(b, l + 1);
    r = r && libraryId_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('.' <<nonStrictID>>)*
  private static boolean libraryId_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryId_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!libraryId_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryId_1", c)) break;
    }
    return true;
  }

  // '.' <<nonStrictID>>
  private static boolean libraryId_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryId_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<nonStrictID>> ('.' <<nonStrictID>>)*
  public static boolean libraryNameElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryNameElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIBRARY_NAME_ELEMENT, "<library name element>");
    r = nonStrictID(b, l + 1);
    r = r && libraryNameElement_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('.' <<nonStrictID>>)*
  private static boolean libraryNameElement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryNameElement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!libraryNameElement_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryNameElement_1", c)) break;
    }
    return true;
  }

  // '.' <<nonStrictID>>
  private static boolean libraryNameElement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryNameElement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // libraryComponentReferenceExpression (',' libraryComponentReferenceExpression)*
  public static boolean libraryReferenceList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryReferenceList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIBRARY_REFERENCE_LIST, "<library reference list>");
    r = libraryComponentReferenceExpression(b, l + 1);
    r = r && libraryReferenceList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' libraryComponentReferenceExpression)*
  private static boolean libraryReferenceList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryReferenceList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!libraryReferenceList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryReferenceList_1", c)) break;
    }
    return true;
  }

  // ',' libraryComponentReferenceExpression
  private static boolean libraryReferenceList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryReferenceList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && libraryComponentReferenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'library' libraryNameElement? ';'
  public static boolean libraryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryStatement")) return false;
    if (!nextTokenIs(b, "<library statement>", AT, LIBRARY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIBRARY_STATEMENT, "<library statement>");
    r = libraryStatement_0(b, l + 1);
    r = r && consumeToken(b, LIBRARY);
    r = r && libraryStatement_2(b, l + 1);
    p = r; // pin = 3
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean libraryStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libraryStatement_0", c)) break;
    }
    return true;
  }

  // libraryNameElement?
  private static boolean libraryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libraryStatement_2")) return false;
    libraryNameElement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'const'? typeArguments? '[' elements? ']'
  public static boolean listLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression")) return false;
    if (!nextTokenIs(b, "<list literal expression>", CONST, LBRACKET, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIST_LITERAL_EXPRESSION, "<list literal expression>");
    r = listLiteralExpression_0(b, l + 1);
    r = r && listLiteralExpression_1(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && listLiteralExpression_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'const'?
  private static boolean listLiteralExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_0")) return false;
    consumeToken(b, CONST);
    return true;
  }

  // typeArguments?
  private static boolean listLiteralExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // elements?
  private static boolean listLiteralExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listLiteralExpression_3")) return false;
    elements(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // typeArguments? '[' listPatternElements? ']'
  public static boolean listPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPattern")) return false;
    if (!nextTokenIs(b, "<list pattern>", LBRACKET, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIST_PATTERN, "<list pattern>");
    r = listPattern_0(b, l + 1);
    r = r && consumeToken(b, LBRACKET);
    r = r && listPattern_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeArguments?
  private static boolean listPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPattern_0")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // listPatternElements?
  private static boolean listPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPattern_2")) return false;
    listPatternElements(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // restPattern | pattern
  public static boolean listPatternElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPatternElement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIST_PATTERN_ELEMENT, "<list pattern element>");
    r = restPattern(b, l + 1);
    if (!r) r = pattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // listPatternElement (',' listPatternElement)* ','?
  static boolean listPatternElements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPatternElements")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = listPatternElement(b, l + 1);
    r = r && listPatternElements_1(b, l + 1);
    r = r && listPatternElements_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' listPatternElement)*
  private static boolean listPatternElements_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPatternElements_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!listPatternElements_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "listPatternElements_1", c)) break;
    }
    return true;
  }

  // ',' listPatternElement
  private static boolean listPatternElements_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPatternElements_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && listPatternElement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean listPatternElements_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "listPatternElements_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // NULL | TRUE | FALSE | NUMBER | HEX_NUMBER | stringLiteralExpression | symbolLiteralExpression |
  //                       <<setOrMapLiteralExpressionWrapper>> | <<listLiteralExpressionWrapper>> | record
  public static boolean literalExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, LITERAL_EXPRESSION, "<literal expression>");
    r = consumeToken(b, NULL);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, HEX_NUMBER);
    if (!r) r = stringLiteralExpression(b, l + 1);
    if (!r) r = symbolLiteralExpression(b, l + 1);
    if (!r) r = setOrMapLiteralExpressionWrapper(b, l + 1);
    if (!r) r = listLiteralExpressionWrapper(b, l + 1);
    if (!r) r = record(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '&&' compareExpressionWrapper
  public static boolean logicAndExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpression")) return false;
    if (!nextTokenIs(b, AND_AND)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, LOGIC_AND_EXPRESSION, null);
    r = consumeToken(b, AND_AND);
    r = r && compareExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // compareExpressionWrapper logicAndExpression*
  static boolean logicAndExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = compareExpressionWrapper(b, l + 1);
    r = r && logicAndExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // logicAndExpression*
  private static boolean logicAndExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicAndExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicAndExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicAndExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '||' logicAndExpressionWrapper
  public static boolean logicOrExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpression")) return false;
    if (!nextTokenIs(b, OR_OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, LOGIC_OR_EXPRESSION, null);
    r = consumeToken(b, OR_OR);
    r = r && logicAndExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // logicAndExpressionWrapper logicOrExpression*
  static boolean logicOrExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = logicAndExpressionWrapper(b, l + 1);
    r = r && logicOrExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // logicOrExpression*
  private static boolean logicOrExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicOrExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicOrExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicOrExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '&&' relationalPatternWrapper
  public static boolean logicalAndPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalAndPattern")) return false;
    if (!nextTokenIs(b, AND_AND)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, LOGICAL_AND_PATTERN, null);
    r = consumeToken(b, AND_AND);
    r = r && relationalPatternWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // relationalPatternWrapper logicalAndPattern*
  static boolean logicalAndPatternWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalAndPatternWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = relationalPatternWrapper(b, l + 1);
    r = r && logicalAndPatternWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // logicalAndPattern*
  private static boolean logicalAndPatternWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalAndPatternWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicalAndPattern(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicalAndPatternWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '||' logicalAndPatternWrapper
  public static boolean logicalOrPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalOrPattern")) return false;
    if (!nextTokenIs(b, OR_OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, LOGICAL_OR_PATTERN, null);
    r = consumeToken(b, OR_OR);
    r = r && logicalAndPatternWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // logicalAndPatternWrapper logicalOrPattern*
  static boolean logicalOrPatternWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalOrPatternWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = logicalAndPatternWrapper(b, l + 1);
    r = r && logicalOrPatternWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // logicalOrPattern*
  private static boolean logicalOrPatternWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logicalOrPatternWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!logicalOrPattern(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logicalOrPatternWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LONG_TEMPLATE_ENTRY_START expression LONG_TEMPLATE_ENTRY_END
  public static boolean longTemplateEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "longTemplateEntry")) return false;
    if (!nextTokenIs(b, LONG_TEMPLATE_ENTRY_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LONG_TEMPLATE_ENTRY, null);
    r = consumeToken(b, LONG_TEMPLATE_ENTRY_START);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, LONG_TEMPLATE_ENTRY_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // expression ':' expression
  public static boolean mapEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapEntry")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MAP_ENTRY, "<map entry>");
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    p = r; // pin = 2
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // typeArguments? '{' mapPatternEntries? '}'
  public static boolean mapPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPattern")) return false;
    if (!nextTokenIs(b, "<map pattern>", LBRACE, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MAP_PATTERN, "<map pattern>");
    r = mapPattern_0(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && mapPattern_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeArguments?
  private static boolean mapPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPattern_0")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // mapPatternEntries?
  private static boolean mapPattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPattern_2")) return false;
    mapPatternEntries(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // mapPatternEntry (',' mapPatternEntry)* ','?
  static boolean mapPatternEntries(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPatternEntries")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mapPatternEntry(b, l + 1);
    r = r && mapPatternEntries_1(b, l + 1);
    r = r && mapPatternEntries_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' mapPatternEntry)*
  private static boolean mapPatternEntries_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPatternEntries_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!mapPatternEntries_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mapPatternEntries_1", c)) break;
    }
    return true;
  }

  // ',' mapPatternEntry
  private static boolean mapPatternEntries_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPatternEntries_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && mapPatternEntry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean mapPatternEntries_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPatternEntries_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '...' | expression ':' pattern
  public static boolean mapPatternEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPatternEntry")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MAP_PATTERN_ENTRY, "<map pattern entry>");
    r = consumeToken(b, DOT_DOT_DOT);
    if (!r) r = mapPatternEntry_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression ':' pattern
  private static boolean mapPatternEntry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mapPatternEntry_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && pattern(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '@' simpleQualifiedReferenceExpression typeArguments? (<<noSpace>> <<argumentsWrapper>>)?
  public static boolean metadata(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "metadata")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && simpleQualifiedReferenceExpression(b, l + 1);
    r = r && metadata_2(b, l + 1);
    r = r && metadata_3(b, l + 1);
    exit_section_(b, m, METADATA, r);
    return r;
  }

  // typeArguments?
  private static boolean metadata_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "metadata_2")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // (<<noSpace>> <<argumentsWrapper>>)?
  private static boolean metadata_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "metadata_3")) return false;
    metadata_3_0(b, l + 1);
    return true;
  }

  // <<noSpace>> <<argumentsWrapper>>
  private static boolean metadata_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "metadata_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = noSpace(b, l + 1);
    r = r && argumentsWrapper(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static' | 'const')* methodDeclarationPrivate initializers? (';' | functionBodyOrNative | redirection)?
  public static boolean methodDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, METHOD_DECLARATION, "<method declaration>");
    r = methodDeclaration_0(b, l + 1);
    r = r && methodDeclaration_1(b, l + 1);
    r = r && methodDeclarationPrivate(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, methodDeclaration_3(b, l + 1));
    r = p && methodDeclaration_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean methodDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "methodDeclaration_0", c)) break;
    }
    return true;
  }

  // ('external' | 'static' | 'const')*
  private static boolean methodDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!methodDeclaration_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "methodDeclaration_1", c)) break;
    }
    return true;
  }

  // 'external' | 'static' | 'const'
  private static boolean methodDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, CONST);
    return r;
  }

  // initializers?
  private static boolean methodDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_3")) return false;
    initializers(b, l + 1);
    return true;
  }

  // (';' | functionBodyOrNative | redirection)?
  private static boolean methodDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_4")) return false;
    methodDeclaration_4_0(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative | redirection
  private static boolean methodDeclaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclaration_4_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    if (!r) r = redirection(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // returnType <<methodNameWrapper>> typeParameters? formalParameterList | !untypedFunctionType <<methodNameWrapper>> typeParameters? formalParameterList
  static boolean methodDeclarationPrivate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclarationPrivate")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = methodDeclarationPrivate_0(b, l + 1);
    if (!r) r = methodDeclarationPrivate_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // returnType <<methodNameWrapper>> typeParameters? formalParameterList
  private static boolean methodDeclarationPrivate_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclarationPrivate_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = returnType(b, l + 1);
    r = r && methodNameWrapper(b, l + 1);
    r = r && methodDeclarationPrivate_0_2(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParameters?
  private static boolean methodDeclarationPrivate_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclarationPrivate_0_2")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // !untypedFunctionType <<methodNameWrapper>> typeParameters? formalParameterList
  private static boolean methodDeclarationPrivate_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclarationPrivate_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = methodDeclarationPrivate_1_0(b, l + 1);
    r = r && methodNameWrapper(b, l + 1);
    r = r && methodDeclarationPrivate_1_2(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !untypedFunctionType
  private static boolean methodDeclarationPrivate_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclarationPrivate_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !untypedFunctionType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeParameters?
  private static boolean methodDeclarationPrivate_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodDeclarationPrivate_1_2")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '=' type mixins? interfaces? ';'
  public static boolean mixinApplication(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinApplication")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MIXIN_APPLICATION, null);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && report_error_(b, type(b, l + 1));
    r = p && report_error_(b, mixinApplication_2(b, l + 1)) && r;
    r = p && report_error_(b, mixinApplication_3(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // mixins?
  private static boolean mixinApplication_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinApplication_2")) return false;
    mixins(b, l + 1);
    return true;
  }

  // interfaces?
  private static boolean mixinApplication_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinApplication_3")) return false;
    interfaces(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'abstract'? 'base'? 'mixin'
  static boolean mixinClassModifiers(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinClassModifiers")) return false;
    if (!nextTokenIs(b, "", ABSTRACT, BASE, MIXIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mixinClassModifiers_0(b, l + 1);
    r = r && mixinClassModifiers_1(b, l + 1);
    r = r && consumeToken(b, MIXIN);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'abstract'?
  private static boolean mixinClassModifiers_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinClassModifiers_0")) return false;
    consumeToken(b, ABSTRACT);
    return true;
  }

  // 'base'?
  private static boolean mixinClassModifiers_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinClassModifiers_1")) return false;
    consumeToken(b, BASE);
    return true;
  }

  /* ********************************************************** */
  // metadata* mixinModifier? 'mixin' componentName typeParameters? onMixins? interfaces? classBody
  public static boolean mixinDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinDeclaration")) return false;
    if (!nextTokenIs(b, "<mixin declaration>", AT, BASE,
      FINAL, INTERFACE, MIXIN, SEALED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MIXIN_DECLARATION, "<mixin declaration>");
    r = mixinDeclaration_0(b, l + 1);
    r = r && mixinDeclaration_1(b, l + 1);
    r = r && consumeToken(b, MIXIN);
    r = r && componentName(b, l + 1);
    r = r && mixinDeclaration_4(b, l + 1);
    r = r && mixinDeclaration_5(b, l + 1);
    r = r && mixinDeclaration_6(b, l + 1);
    r = r && classBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean mixinDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "mixinDeclaration_0", c)) break;
    }
    return true;
  }

  // mixinModifier?
  private static boolean mixinDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinDeclaration_1")) return false;
    mixinModifier(b, l + 1);
    return true;
  }

  // typeParameters?
  private static boolean mixinDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinDeclaration_4")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // onMixins?
  private static boolean mixinDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinDeclaration_5")) return false;
    onMixins(b, l + 1);
    return true;
  }

  // interfaces?
  private static boolean mixinDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinDeclaration_6")) return false;
    interfaces(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'sealed' | 'base' | 'interface' | 'final'
  static boolean mixinModifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixinModifier")) return false;
    if (!nextTokenIs(b, "", BASE, FINAL, INTERFACE, SEALED)) return false;
    boolean r;
    r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, FINAL);
    return r;
  }

  /* ********************************************************** */
  // 'with' typeList
  public static boolean mixins(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mixins")) return false;
    if (!nextTokenIs(b, WITH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MIXINS, null);
    r = consumeToken(b, WITH);
    p = r; // pin = 1
    r = r && typeList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // multiplicativeOperator prefixExpression
  public static boolean multiplicativeExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpression")) return false;
    if (!nextTokenIs(b, "<multiplicative expression>", DIV, INT_DIV, MUL, REM)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, MULTIPLICATIVE_EXPRESSION, "<multiplicative expression>");
    r = multiplicativeOperator(b, l + 1);
    r = r && prefixExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // prefixExpression multiplicativeExpression*
  static boolean multiplicativeExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefixExpression(b, l + 1);
    r = r && multiplicativeExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // multiplicativeExpression*
  private static boolean multiplicativeExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!multiplicativeExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "multiplicativeExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '*' | '/' | '%' | '~/'
  public static boolean multiplicativeOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "multiplicativeOperator")) return false;
    if (!nextTokenIs(b, "<multiplicative operator>", DIV, INT_DIV, MUL, REM)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MULTIPLICATIVE_OPERATOR, "<multiplicative operator>");
    r = consumeToken(b, MUL);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, REM);
    if (!r) r = consumeToken(b, INT_DIV);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // parameterNameReferenceExpression ':' expression
  public static boolean namedArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedArgument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMED_ARGUMENT, "<named argument>");
    r = parameterNameReferenceExpression(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'const')* componentName '.' (componentName | 'new') formalParameterList initializers? (';' | functionBodyOrNative | redirection)?
  public static boolean namedConstructorDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NAMED_CONSTRUCTOR_DECLARATION, "<named constructor declaration>");
    r = namedConstructorDeclaration_0(b, l + 1);
    r = r && namedConstructorDeclaration_1(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && namedConstructorDeclaration_4(b, l + 1);
    r = r && formalParameterList(b, l + 1);
    p = r; // pin = 6
    r = r && report_error_(b, namedConstructorDeclaration_6(b, l + 1));
    r = p && namedConstructorDeclaration_7(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean namedConstructorDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedConstructorDeclaration_0", c)) break;
    }
    return true;
  }

  // ('external' | 'const')*
  private static boolean namedConstructorDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!namedConstructorDeclaration_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedConstructorDeclaration_1", c)) break;
    }
    return true;
  }

  // 'external' | 'const'
  private static boolean namedConstructorDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, CONST);
    return r;
  }

  // componentName | 'new'
  private static boolean namedConstructorDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_4")) return false;
    boolean r;
    r = componentName(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  // initializers?
  private static boolean namedConstructorDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_6")) return false;
    initializers(b, l + 1);
    return true;
  }

  // (';' | functionBodyOrNative | redirection)?
  private static boolean namedConstructorDeclaration_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_7")) return false;
    namedConstructorDeclaration_7_0(b, l + 1);
    return true;
  }

  // ';' | functionBodyOrNative | redirection
  private static boolean namedConstructorDeclaration_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedConstructorDeclaration_7_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    if (!r) r = redirection(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '{' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* ','? '}'
  static boolean namedFormalParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && defaultFormalNamedParameter(b, l + 1);
    r = r && namedFormalParameters_2(b, l + 1);
    r = r && namedFormalParameters_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' defaultFormalNamedParameter)*
  private static boolean namedFormalParameters_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!namedFormalParameters_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedFormalParameters_2", c)) break;
    }
    return true;
  }

  // ',' defaultFormalNamedParameter
  private static boolean namedFormalParameters_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && defaultFormalNamedParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean namedFormalParameters_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedFormalParameters_3")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '{' 'required'? typedIdentifier (',' 'required'? typedIdentifier)* ','? '}'
  static boolean namedParameterTypes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterTypes")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && namedParameterTypes_1(b, l + 1);
    r = r && typedIdentifier(b, l + 1);
    r = r && namedParameterTypes_3(b, l + 1);
    r = r && namedParameterTypes_4(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'required'?
  private static boolean namedParameterTypes_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterTypes_1")) return false;
    consumeToken(b, REQUIRED);
    return true;
  }

  // (',' 'required'? typedIdentifier)*
  private static boolean namedParameterTypes_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterTypes_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!namedParameterTypes_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namedParameterTypes_3", c)) break;
    }
    return true;
  }

  // ',' 'required'? typedIdentifier
  private static boolean namedParameterTypes_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterTypes_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && namedParameterTypes_3_0_1(b, l + 1);
    r = r && typedIdentifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'required'?
  private static boolean namedParameterTypes_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterTypes_3_0_1")) return false;
    consumeToken(b, REQUIRED);
    return true;
  }

  // ','?
  private static boolean namedParameterTypes_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namedParameterTypes_4")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // newExpressionWithKeyword | simpleQualifiedReferenceExpression typeArguments '.' (referenceExpression | 'new') <<argumentsWrapper>>
  public static boolean newExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, NEW_EXPRESSION, "<new expression>");
    r = newExpressionWithKeyword(b, l + 1);
    if (!r) r = newExpression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // simpleQualifiedReferenceExpression typeArguments '.' (referenceExpression | 'new') <<argumentsWrapper>>
  private static boolean newExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleQualifiedReferenceExpression(b, l + 1);
    r = r && typeArguments(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && newExpression_1_3(b, l + 1);
    r = r && argumentsWrapper(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // referenceExpression | 'new'
  private static boolean newExpression_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpression_1_3")) return false;
    boolean r;
    r = referenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  /* ********************************************************** */
  // ('new' | 'const') type ('.' (referenceExpression | 'new'))? <<argumentsWrapper>>
  static boolean newExpressionWithKeyword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpressionWithKeyword")) return false;
    if (!nextTokenIs(b, "", CONST, NEW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = newExpressionWithKeyword_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, type(b, l + 1));
    r = p && report_error_(b, newExpressionWithKeyword_2(b, l + 1)) && r;
    r = p && argumentsWrapper(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'new' | 'const'
  private static boolean newExpressionWithKeyword_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpressionWithKeyword_0")) return false;
    boolean r;
    r = consumeToken(b, NEW);
    if (!r) r = consumeToken(b, CONST);
    return r;
  }

  // ('.' (referenceExpression | 'new'))?
  private static boolean newExpressionWithKeyword_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpressionWithKeyword_2")) return false;
    newExpressionWithKeyword_2_0(b, l + 1);
    return true;
  }

  // '.' (referenceExpression | 'new')
  private static boolean newExpressionWithKeyword_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpressionWithKeyword_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && newExpressionWithKeyword_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // referenceExpression | 'new'
  private static boolean newExpressionWithKeyword_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "newExpressionWithKeyword_2_0_1")) return false;
    boolean r;
    r = referenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  /* ********************************************************** */
  // patternAssignment
  //                                | block // Guard to break tie with map literal.
  //                                | functionDeclarationWithBody
  //                                | forStatement
  //                                | whileStatement
  //                                | doWhileStatement
  //                                | switchStatement
  //                                | ifStatement
  //                                | rethrowStatement
  //                                | tryStatement
  //                                | breakStatement
  //                                | continueStatement
  //                                | returnStatement
  //                                | assertStatementWithSemicolon
  //                                | patternVariableDeclaration ';'?
  //                                | statementFollowedBySemiColon
  //                                | yieldEachStatement
  //                                | yieldStatement
  //                                | ';'
  //                                | '=>'
  static boolean nonLabelledStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternAssignment(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = functionDeclarationWithBody(b, l + 1);
    if (!r) r = forStatement(b, l + 1);
    if (!r) r = whileStatement(b, l + 1);
    if (!r) r = doWhileStatement(b, l + 1);
    if (!r) r = switchStatement(b, l + 1);
    if (!r) r = ifStatement(b, l + 1);
    if (!r) r = rethrowStatement(b, l + 1);
    if (!r) r = tryStatement(b, l + 1);
    if (!r) r = breakStatement(b, l + 1);
    if (!r) r = continueStatement(b, l + 1);
    if (!r) r = returnStatement(b, l + 1);
    if (!r) r = assertStatementWithSemicolon(b, l + 1);
    if (!r) r = nonLabelledStatement_14(b, l + 1);
    if (!r) r = statementFollowedBySemiColon(b, l + 1);
    if (!r) r = yieldEachStatement(b, l + 1);
    if (!r) r = yieldStatement(b, l + 1);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, EXPRESSION_BODY_DEF);
    exit_section_(b, m, null, r);
    return r;
  }

  // patternVariableDeclaration ';'?
  private static boolean nonLabelledStatement_14(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_14")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternVariableDeclaration(b, l + 1);
    r = r && nonLabelledStatement_14_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ';'?
  private static boolean nonLabelledStatement_14_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nonLabelledStatement_14_1")) return false;
    consumeToken(b, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // functionFormalParameter
  //                         | fieldFormalParameter
  //                         | simpleFormalParameter
  //                         | incorrectNormalFormalParameter
  public static boolean normalFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_FORMAL_PARAMETER, "<normal formal parameter>");
    r = functionFormalParameter(b, l + 1);
    if (!r) r = fieldFormalParameter(b, l + 1);
    if (!r) r = simpleFormalParameter(b, l + 1);
    if (!r) r = incorrectNormalFormalParameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // normalFormalParameter (',' normalFormalParameter)*
  static boolean normalFormalParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalFormalParameters")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = normalFormalParameter(b, l + 1);
    r = r && normalFormalParameters_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' normalFormalParameter)*
  private static boolean normalFormalParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalFormalParameters_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!normalFormalParameters_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normalFormalParameters_1", c)) break;
    }
    return true;
  }

  // ',' normalFormalParameter
  private static boolean normalFormalParameters_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalFormalParameters_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && normalFormalParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // typedIdentifier | type
  public static boolean normalParameterType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalParameterType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_PARAMETER_TYPE, "<normal parameter type>");
    r = typedIdentifier(b, l + 1);
    if (!r) r = type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // normalParameterType (',' normalParameterType)*
  static boolean normalParameterTypes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalParameterTypes")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = normalParameterType(b, l + 1);
    r = r && normalParameterTypes_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' normalParameterType)*
  private static boolean normalParameterTypes_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalParameterTypes_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!normalParameterTypes_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normalParameterTypes_1", c)) break;
    }
    return true;
  }

  // ',' normalParameterType
  private static boolean normalParameterTypes_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normalParameterTypes_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && normalParameterType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')' | ',')
  static boolean not_paren_or_comma_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_paren_or_comma_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_paren_or_comma_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | ','
  private static boolean not_paren_or_comma_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_paren_or_comma_recover_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  /* ********************************************************** */
  // !')'
  static boolean not_paren_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_paren_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // simpleQualifiedReferenceExpression typeArguments? '(' patternFields? ')'
  public static boolean objectPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_PATTERN, "<object pattern>");
    r = simpleQualifiedReferenceExpression(b, l + 1);
    r = r && objectPattern_1(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && objectPattern_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeArguments?
  private static boolean objectPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectPattern_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // patternFields?
  private static boolean objectPattern_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectPattern_3")) return false;
    patternFields(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'on' typeList
  public static boolean onMixins(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onMixins")) return false;
    if (!nextTokenIs(b, ON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ON_MIXINS, null);
    r = consumeToken(b, ON);
    p = r; // pin = 1
    r = r && typeList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // catchPart block | 'on' type catchPart? block
  public static boolean onPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart")) return false;
    if (!nextTokenIs(b, "<on part>", CATCH, ON)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ON_PART, "<on part>");
    r = onPart_0(b, l + 1);
    if (!r) r = onPart_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // catchPart block
  private static boolean onPart_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = catchPart(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'on' type catchPart? block
  private static boolean onPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && type(b, l + 1);
    r = r && onPart_1_2(b, l + 1);
    r = r && block(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // catchPart?
  private static boolean onPart_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "onPart_1_2")) return false;
    catchPart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // optionalPositionalFormalParameters (',' namedFormalParameters)? | namedFormalParameters
  public static boolean optionalFormalParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalFormalParameters")) return false;
    if (!nextTokenIs(b, "<optional formal parameters>", LBRACE, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTIONAL_FORMAL_PARAMETERS, "<optional formal parameters>");
    r = optionalFormalParameters_0(b, l + 1);
    if (!r) r = namedFormalParameters(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // optionalPositionalFormalParameters (',' namedFormalParameters)?
  private static boolean optionalFormalParameters_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalFormalParameters_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = optionalPositionalFormalParameters(b, l + 1);
    r = r && optionalFormalParameters_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' namedFormalParameters)?
  private static boolean optionalFormalParameters_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalFormalParameters_0_1")) return false;
    optionalFormalParameters_0_1_0(b, l + 1);
    return true;
  }

  // ',' namedFormalParameters
  private static boolean optionalFormalParameters_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalFormalParameters_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && namedFormalParameters(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // optionalPositionalParameterTypes | namedParameterTypes
  public static boolean optionalParameterTypes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalParameterTypes")) return false;
    if (!nextTokenIs(b, "<optional parameter types>", LBRACE, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTIONAL_PARAMETER_TYPES, "<optional parameter types>");
    r = optionalPositionalParameterTypes(b, l + 1);
    if (!r) r = namedParameterTypes(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '[' defaultFormalNamedParameter (',' defaultFormalNamedParameter)* ','? ']'
  static boolean optionalPositionalFormalParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalPositionalFormalParameters")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && defaultFormalNamedParameter(b, l + 1);
    r = r && optionalPositionalFormalParameters_2(b, l + 1);
    r = r && optionalPositionalFormalParameters_3(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' defaultFormalNamedParameter)*
  private static boolean optionalPositionalFormalParameters_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalPositionalFormalParameters_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!optionalPositionalFormalParameters_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "optionalPositionalFormalParameters_2", c)) break;
    }
    return true;
  }

  // ',' defaultFormalNamedParameter
  private static boolean optionalPositionalFormalParameters_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalPositionalFormalParameters_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && defaultFormalNamedParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean optionalPositionalFormalParameters_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalPositionalFormalParameters_3")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '[' normalParameterTypes ','? ']'
  static boolean optionalPositionalParameterTypes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalPositionalParameterTypes")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACKET);
    r = r && normalParameterTypes(b, l + 1);
    r = r && optionalPositionalParameterTypes_2(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean optionalPositionalParameterTypes_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optionalPositionalParameterTypes_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // listPattern | mapPattern | recordPattern | parenthesizedPattern | objectPattern
  static boolean outerPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "outerPattern")) return false;
    boolean r;
    r = listPattern(b, l + 1);
    if (!r) r = mapPattern(b, l + 1);
    if (!r) r = recordPattern(b, l + 1);
    if (!r) r = parenthesizedPattern(b, l + 1);
    if (!r) r = objectPattern(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean parameterNameReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterNameReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PARAMETER_NAME_REFERENCE_EXPRESSION, "<parameter name reference expression>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' ')'
  //                     | '(' normalParameterTypes ','? ')'
  //                     | '(' normalParameterTypes ',' optionalParameterTypes ')'
  //                     | '(' optionalParameterTypes ')'
  public static boolean parameterTypeList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, LPAREN, RPAREN);
    if (!r) r = parameterTypeList_1(b, l + 1);
    if (!r) r = parameterTypeList_2(b, l + 1);
    if (!r) r = parameterTypeList_3(b, l + 1);
    exit_section_(b, m, PARAMETER_TYPE_LIST, r);
    return r;
  }

  // '(' normalParameterTypes ','? ')'
  private static boolean parameterTypeList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeList_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && normalParameterTypes(b, l + 1);
    r = r && parameterTypeList_1_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean parameterTypeList_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeList_1_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // '(' normalParameterTypes ',' optionalParameterTypes ')'
  private static boolean parameterTypeList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeList_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && normalParameterTypes(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && optionalParameterTypes(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' optionalParameterTypes ')'
  private static boolean parameterTypeList_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameterTypeList_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && optionalParameterTypes(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !')'
  static boolean parenthesesRecovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesesRecovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '(' expressionInParentheses ')'
  public static boolean parenthesizedExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedExpression")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARENTHESIZED_EXPRESSION, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionInParentheses(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '(' pattern ')'
  public static boolean parenthesizedPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesizedPattern")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && pattern(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, PARENTHESIZED_PATTERN, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'part' 'of' (libraryId | uriElement)';'
  public static boolean partOfStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partOfStatement")) return false;
    if (!nextTokenIs(b, "<part of statement>", AT, PART)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PART_OF_STATEMENT, "<part of statement>");
    r = partOfStatement_0(b, l + 1);
    r = r && consumeTokens(b, 0, PART, OF);
    r = r && partOfStatement_3(b, l + 1);
    p = r; // pin = 4
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean partOfStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partOfStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "partOfStatement_0", c)) break;
    }
    return true;
  }

  // libraryId | uriElement
  private static boolean partOfStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partOfStatement_3")) return false;
    boolean r;
    r = libraryId(b, l + 1);
    if (!r) r = uriElement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // metadata* 'part' uriElement ';'
  public static boolean partStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partStatement")) return false;
    if (!nextTokenIs(b, "<part statement>", AT, PART)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PART_STATEMENT, "<part statement>");
    r = partStatement_0(b, l + 1);
    r = r && consumeToken(b, PART);
    r = r && uriElement(b, l + 1);
    p = r; // pin = 3
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean partStatement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "partStatement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "partStatement_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // logicalOrPatternWrapper
  static boolean pattern(PsiBuilder b, int l) {
    return logicalOrPatternWrapper(b, l + 1);
  }

  /* ********************************************************** */
  // outerPattern '=' expression
  public static boolean patternAssignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternAssignment")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_ASSIGNMENT, "<pattern assignment>");
    r = outerPattern(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'case' guardedPattern ':'
  static boolean patternCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternCase")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CASE);
    r = r && guardedPattern(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (componentName? ':')? pattern
  public static boolean patternField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_FIELD, "<pattern field>");
    r = patternField_0(b, l + 1);
    r = r && pattern(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (componentName? ':')?
  private static boolean patternField_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternField_0")) return false;
    patternField_0_0(b, l + 1);
    return true;
  }

  // componentName? ':'
  private static boolean patternField_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternField_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternField_0_0_0(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // componentName?
  private static boolean patternField_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternField_0_0_0")) return false;
    componentName(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // patternField (',' patternField)* ','?
  static boolean patternFields(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternFields")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = patternField(b, l + 1);
    r = r && patternFields_1(b, l + 1);
    r = r && patternFields_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' patternField)*
  private static boolean patternFields_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternFields_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!patternFields_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "patternFields_1", c)) break;
    }
    return true;
  }

  // ',' patternField
  private static boolean patternFields_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternFields_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && patternField(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean patternFields_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternFields_2")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // metadata* ('final' | 'var') outerPattern '=' expression
  public static boolean patternVariableDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternVariableDeclaration")) return false;
    if (!nextTokenIs(b, "<pattern variable declaration>", AT, FINAL, VAR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATTERN_VARIABLE_DECLARATION, "<pattern variable declaration>");
    r = patternVariableDeclaration_0(b, l + 1);
    r = r && patternVariableDeclaration_1(b, l + 1);
    r = r && outerPattern(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean patternVariableDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternVariableDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "patternVariableDeclaration_0", c)) break;
    }
    return true;
  }

  // 'final' | 'var'
  private static boolean patternVariableDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "patternVariableDeclaration_1")) return false;
    boolean r;
    r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, VAR);
    return r;
  }

  /* ********************************************************** */
  // (prefixOperator prefixExpression) | awaitExpression | suffixExpressionWrapper
  public static boolean prefixExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, PREFIX_EXPRESSION, "<prefix expression>");
    r = prefixExpression_0(b, l + 1);
    if (!r) r = awaitExpression(b, l + 1);
    if (!r) r = suffixExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // prefixOperator prefixExpression
  private static boolean prefixExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = prefixOperator(b, l + 1);
    r = r && prefixExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '-' | '+' | '--' | '++' | '!' | '~'
  public static boolean prefixOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prefixOperator")) return false;
    if (!nextTokenIs(b, "<prefix operator>", BIN_NOT, MINUS,
      MINUS_MINUS, NOT, PLUS, PLUS_PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PREFIX_OPERATOR, "<prefix operator>");
    r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS_MINUS);
    if (!r) r = consumeToken(b, PLUS_PLUS);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, BIN_NOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // functionExpression |
  //                      literalExpression |
  //                      newExpression | // constant object expression is also parsed as newExpression
  //                      refOrThisOrSuperOrParenExpression |
  //                      throwExpression
  static boolean primary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary")) return false;
    boolean r;
    r = functionExpression(b, l + 1);
    if (!r) r = literalExpression(b, l + 1);
    if (!r) r = newExpression(b, l + 1);
    if (!r) r = refOrThisOrSuperOrParenExpression(b, l + 1);
    if (!r) r = throwExpression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // variablePattern | listPattern | mapPattern | recordPattern | parenthesizedPattern | objectPattern | constantPattern | identifierPattern
  static boolean primaryPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primaryPattern")) return false;
    boolean r;
    r = variablePattern(b, l + 1);
    if (!r) r = listPattern(b, l + 1);
    if (!r) r = mapPattern(b, l + 1);
    if (!r) r = recordPattern(b, l + 1);
    if (!r) r = parenthesizedPattern(b, l + 1);
    if (!r) r = objectPattern(b, l + 1);
    if (!r) r = constantPattern(b, l + 1);
    if (!r) r = identifierPattern(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '.' (referenceExpression | 'new') | '?.' referenceExpression
  public static boolean qualifiedReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression")) return false;
    if (!nextTokenIs(b, "<qualified reference expression>", DOT, QUEST_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, REFERENCE_EXPRESSION, "<qualified reference expression>");
    r = qualifiedReferenceExpression_0(b, l + 1);
    if (!r) r = qualifiedReferenceExpression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '.' (referenceExpression | 'new')
  private static boolean qualifiedReferenceExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && qualifiedReferenceExpression_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // referenceExpression | 'new'
  private static boolean qualifiedReferenceExpression_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression_0_1")) return false;
    boolean r;
    r = referenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  // '?.' referenceExpression
  private static boolean qualifiedReferenceExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualifiedReferenceExpression_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST_DOT);
    r = r && referenceExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'const'? '(' ')' |
  //            'const'? '(' !(expression ')') recordField ( ',' recordField )* ','? ')'
  public static boolean record(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record")) return false;
    if (!nextTokenIs(b, "<record>", CONST, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD, "<record>");
    r = record_0(b, l + 1);
    if (!r) r = record_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'const'? '(' ')'
  private static boolean record_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = record_0_0(b, l + 1);
    r = r && consumeTokens(b, 0, LPAREN, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'const'?
  private static boolean record_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_0_0")) return false;
    consumeToken(b, CONST);
    return true;
  }

  // 'const'? '(' !(expression ')') recordField ( ',' recordField )* ','? ')'
  private static boolean record_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = record_1_0(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && record_1_2(b, l + 1);
    r = r && recordField(b, l + 1);
    r = r && record_1_4(b, l + 1);
    r = r && record_1_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'const'?
  private static boolean record_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1_0")) return false;
    consumeToken(b, CONST);
    return true;
  }

  // !(expression ')')
  private static boolean record_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !record_1_2_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression ')'
  private static boolean record_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ',' recordField )*
  private static boolean record_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!record_1_4_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "record_1_4", c)) break;
    }
    return true;
  }

  // ',' recordField
  private static boolean record_1_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && recordField(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean record_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_1_5")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // (<<nonStrictID>> ':' )? expression
  public static boolean recordField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_FIELD, "<record field>");
    r = recordField_0(b, l + 1);
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<nonStrictID>> ':' )?
  private static boolean recordField_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordField_0")) return false;
    recordField_0_0(b, l + 1);
    return true;
  }

  // <<nonStrictID>> ':'
  private static boolean recordField_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordField_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '(' patternFields? ')'
  public static boolean recordPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordPattern")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && recordPattern_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, RECORD_PATTERN, r);
    return r;
  }

  // patternFields?
  private static boolean recordPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordPattern_1")) return false;
    patternFields(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' !(recordTypeField ')') recordTypeInner ')' ('?' !(expression ':'))?
  public static boolean recordType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && recordType_1(b, l + 1);
    r = r && recordTypeInner(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && recordType_4(b, l + 1);
    exit_section_(b, m, RECORD_TYPE, r);
    return r;
  }

  // !(recordTypeField ')')
  private static boolean recordType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recordType_1_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // recordTypeField ')'
  private static boolean recordType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordTypeField(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ('?' !(expression ':'))?
  private static boolean recordType_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType_4")) return false;
    recordType_4_0(b, l + 1);
    return true;
  }

  // '?' !(expression ':')
  private static boolean recordType_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST);
    r = r && recordType_4_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(expression ':')
  private static boolean recordType_4_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType_4_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recordType_4_0_1_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression ':'
  private static boolean recordType_4_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordType_4_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* type <<nonStrictID>>?
  public static boolean recordTypeField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_TYPE_FIELD, "<record type field>");
    r = recordTypeField_0(b, l + 1);
    r = r && type(b, l + 1);
    r = r && recordTypeField_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean recordTypeField_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeField_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordTypeField_0", c)) break;
    }
    return true;
  }

  // <<nonStrictID>>?
  private static boolean recordTypeField_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeField_2")) return false;
    nonStrictID(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // recordTypeField ( ',' recordTypeField )*
  static boolean recordTypeFields(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeFields")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordTypeField(b, l + 1);
    r = r && recordTypeFields_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( ',' recordTypeField )*
  private static boolean recordTypeFields_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeFields_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!recordTypeFields_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordTypeFields_1", c)) break;
    }
    return true;
  }

  // ',' recordTypeField
  private static boolean recordTypeFields_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeFields_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && recordTypeField(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // recordTypeFields ',' recordTypeNamedFields  |
  //                             recordTypeFields ','?  |
  //                             recordTypeNamedFields?
  static boolean recordTypeInner(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeInner")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordTypeInner_0(b, l + 1);
    if (!r) r = recordTypeInner_1(b, l + 1);
    if (!r) r = recordTypeInner_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // recordTypeFields ',' recordTypeNamedFields
  private static boolean recordTypeInner_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeInner_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordTypeFields(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && recordTypeNamedFields(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // recordTypeFields ','?
  private static boolean recordTypeInner_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeInner_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordTypeFields(b, l + 1);
    r = r && recordTypeInner_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean recordTypeInner_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeInner_1_1")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // recordTypeNamedFields?
  private static boolean recordTypeInner_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeInner_2")) return false;
    recordTypeNamedFields(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // metadata* type <<nonStrictID>>
  public static boolean recordTypeNamedField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeNamedField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RECORD_TYPE_NAMED_FIELD, "<record type named field>");
    r = recordTypeNamedField_0(b, l + 1);
    r = r && type(b, l + 1);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean recordTypeNamedField_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeNamedField_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordTypeNamedField_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '{' recordTypeNamedField ( ',' recordTypeNamedField )* ','? '}'
  public static boolean recordTypeNamedFields(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeNamedFields")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACE);
    r = r && recordTypeNamedField(b, l + 1);
    r = r && recordTypeNamedFields_2(b, l + 1);
    r = r && recordTypeNamedFields_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, RECORD_TYPE_NAMED_FIELDS, r);
    return r;
  }

  // ( ',' recordTypeNamedField )*
  private static boolean recordTypeNamedFields_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeNamedFields_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!recordTypeNamedFields_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordTypeNamedFields_2", c)) break;
    }
    return true;
  }

  // ',' recordTypeNamedField
  private static boolean recordTypeNamedFields_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeNamedFields_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && recordTypeNamedField(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ','?
  private static boolean recordTypeNamedFields_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordTypeNamedFields_3")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  /* ********************************************************** */
  // ':' 'this' ('.' (referenceExpression | 'new'))? <<argumentsWrapper>>
  public static boolean redirection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REDIRECTION, null);
    r = consumeTokens(b, 2, COLON, THIS);
    p = r; // pin = 2
    r = r && report_error_(b, redirection_2(b, l + 1));
    r = p && argumentsWrapper(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('.' (referenceExpression | 'new'))?
  private static boolean redirection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection_2")) return false;
    redirection_2_0(b, l + 1);
    return true;
  }

  // '.' (referenceExpression | 'new')
  private static boolean redirection_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && redirection_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // referenceExpression | 'new'
  private static boolean redirection_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "redirection_2_0_1")) return false;
    boolean r;
    r = referenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  /* ********************************************************** */
  // referenceExpression | thisExpression | superExpression | << parenthesizedExpressionWrapper >>
  static boolean refOrThisOrSuperOrParenExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "refOrThisOrSuperOrParenExpression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = referenceExpression(b, l + 1);
    if (!r) r = thisExpression(b, l + 1);
    if (!r) r = superExpression(b, l + 1);
    if (!r) r = parenthesizedExpressionWrapper(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // << nonStrictID >>
  public static boolean referenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, REFERENCE_EXPRESSION, "<reference expression>");
    r = nonStrictID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<gtEq>> | '>' | '<=' | '<'
  public static boolean relationalOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RELATIONAL_OPERATOR, "<relational operator>");
    r = gtEq(b, l + 1);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, LT_EQ);
    if (!r) r = consumeToken(b, LT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (equalityOperator | relationalOperator) bitwiseExpressionWrapper
  public static boolean relationalPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalPattern")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RELATIONAL_PATTERN, "<relational pattern>");
    r = relationalPattern_0(b, l + 1);
    r = r && bitwiseExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // equalityOperator | relationalOperator
  private static boolean relationalPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalPattern_0")) return false;
    boolean r;
    r = equalityOperator(b, l + 1);
    if (!r) r = relationalOperator(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // relationalPattern | unaryPatternWrapper
  static boolean relationalPatternWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relationalPatternWrapper")) return false;
    boolean r;
    r = relationalPattern(b, l + 1);
    if (!r) r = unaryPatternWrapper(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '...' pattern?
  public static boolean restPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "restPattern")) return false;
    if (!nextTokenIs(b, DOT_DOT_DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT_DOT_DOT);
    r = r && restPattern_1(b, l + 1);
    exit_section_(b, m, REST_PATTERN, r);
    return r;
  }

  // pattern?
  private static boolean restPattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "restPattern_1")) return false;
    pattern(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'rethrow' ';'
  public static boolean rethrowStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rethrowStatement")) return false;
    if (!nextTokenIs(b, RETHROW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETHROW_STATEMENT, null);
    r = consumeTokens(b, 1, RETHROW, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'return' expression? ';'
  public static boolean returnStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STATEMENT, null);
    r = consumeToken(b, RETURN);
    p = r; // pin = 1
    r = r && report_error_(b, returnStatement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expression?
  private static boolean returnStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnStatement_1")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'void' !untypedFunctionType | type
  public static boolean returnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RETURN_TYPE, "<return type>");
    r = returnType_0(b, l + 1);
    if (!r) r = type(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'void' !untypedFunctionType
  private static boolean returnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VOID);
    r = r && returnType_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !untypedFunctionType
  private static boolean returnType_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "returnType_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !untypedFunctionType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'const'? typeArguments? '{' elements? '}'
  public static boolean setOrMapLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setOrMapLiteralExpression")) return false;
    if (!nextTokenIs(b, "<set or map literal expression>", CONST, LBRACE, LT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SET_OR_MAP_LITERAL_EXPRESSION, "<set or map literal expression>");
    r = setOrMapLiteralExpression_0(b, l + 1);
    r = r && setOrMapLiteralExpression_1(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    r = r && setOrMapLiteralExpression_3(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'const'?
  private static boolean setOrMapLiteralExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setOrMapLiteralExpression_0")) return false;
    consumeToken(b, CONST);
    return true;
  }

  // typeArguments?
  private static boolean setOrMapLiteralExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setOrMapLiteralExpression_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // elements?
  private static boolean setOrMapLiteralExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setOrMapLiteralExpression_3")) return false;
    elements(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // setterDeclarationWithReturnType | setterDeclarationWithoutReturnType
  public static boolean setterDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SETTER_DECLARATION, "<setter declaration>");
    r = setterDeclarationWithReturnType(b, l + 1);
    if (!r) r = setterDeclarationWithoutReturnType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')* returnType 'set' componentName formalParameterList (';' | functionBodyOrNative)
  static boolean setterDeclarationWithReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = setterDeclarationWithReturnType_0(b, l + 1);
    r = r && setterDeclarationWithReturnType_1(b, l + 1);
    r = r && returnType(b, l + 1);
    r = r && consumeToken(b, SET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 5
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && setterDeclarationWithReturnType_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean setterDeclarationWithReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithReturnType_0", c)) break;
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean setterDeclarationWithReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!setterDeclarationWithReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithReturnType_1", c)) break;
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean setterDeclarationWithReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    return r;
  }

  // ';' | functionBodyOrNative
  private static boolean setterDeclarationWithReturnType_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithReturnType_6")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // metadata* ('external' | 'static')*            'set' componentName formalParameterList (';' | functionBodyOrNative)
  static boolean setterDeclarationWithoutReturnType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType")) return false;
    if (!nextTokenIs(b, "", AT, EXTERNAL, SET, STATIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = setterDeclarationWithoutReturnType_0(b, l + 1);
    r = r && setterDeclarationWithoutReturnType_1(b, l + 1);
    r = r && consumeToken(b, SET);
    r = r && componentName(b, l + 1);
    p = r; // pin = 4
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && setterDeclarationWithoutReturnType_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // metadata*
  private static boolean setterDeclarationWithoutReturnType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithoutReturnType_0", c)) break;
    }
    return true;
  }

  // ('external' | 'static')*
  private static boolean setterDeclarationWithoutReturnType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!setterDeclarationWithoutReturnType_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setterDeclarationWithoutReturnType_1", c)) break;
    }
    return true;
  }

  // 'external' | 'static'
  private static boolean setterDeclarationWithoutReturnType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_1_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, STATIC);
    return r;
  }

  // ';' | functionBodyOrNative
  private static boolean setterDeclarationWithoutReturnType_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setterDeclarationWithoutReturnType_5")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = functionBodyOrNative(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // shiftOperator additiveExpressionWrapper
  public static boolean shiftExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, SHIFT_EXPRESSION, "<shift expression>");
    r = shiftOperator(b, l + 1);
    r = r && additiveExpressionWrapper(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // additiveExpressionWrapper shiftExpression*
  static boolean shiftExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = additiveExpressionWrapper(b, l + 1);
    r = r && shiftExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shiftExpression*
  private static boolean shiftExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!shiftExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "shiftExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // '<<' | <<gtGtGt>> | <<gtGt>>
  public static boolean shiftOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shiftOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SHIFT_OPERATOR, "<shift operator>");
    r = consumeToken(b, LT_LT);
    if (!r) r = gtGtGt(b, l + 1);
    if (!r) r = gtGt(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SHORT_TEMPLATE_ENTRY_START (thisExpression | referenceExpression)
  public static boolean shortTemplateEntry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shortTemplateEntry")) return false;
    if (!nextTokenIs(b, SHORT_TEMPLATE_ENTRY_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SHORT_TEMPLATE_ENTRY, null);
    r = consumeToken(b, SHORT_TEMPLATE_ENTRY_START);
    p = r; // pin = 1
    r = r && shortTemplateEntry_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // thisExpression | referenceExpression
  private static boolean shortTemplateEntry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shortTemplateEntry_1")) return false;
    boolean r;
    r = thisExpression(b, l + 1);
    if (!r) r = referenceExpression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // 'show' libraryReferenceList
  public static boolean showCombinator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "showCombinator")) return false;
    if (!nextTokenIs(b, SHOW)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SHOW_COMBINATOR, null);
    r = consumeToken(b, SHOW);
    p = r; // pin = 1
    r = r && libraryReferenceList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // declaredIdentifier | metadata* 'required'? componentName | metadata* 'required'? 'covariant' componentName
  public static boolean simpleFormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_FORMAL_PARAMETER, "<simple formal parameter>");
    r = declaredIdentifier(b, l + 1);
    if (!r) r = simpleFormalParameter_1(b, l + 1);
    if (!r) r = simpleFormalParameter_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata* 'required'? componentName
  private static boolean simpleFormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleFormalParameter_1_0(b, l + 1);
    r = r && simpleFormalParameter_1_1(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean simpleFormalParameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_1_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleFormalParameter_1_0", c)) break;
    }
    return true;
  }

  // 'required'?
  private static boolean simpleFormalParameter_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_1_1")) return false;
    consumeToken(b, REQUIRED);
    return true;
  }

  // metadata* 'required'? 'covariant' componentName
  private static boolean simpleFormalParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleFormalParameter_2_0(b, l + 1);
    r = r && simpleFormalParameter_2_1(b, l + 1);
    r = r && consumeToken(b, COVARIANT);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // metadata*
  private static boolean simpleFormalParameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_2_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleFormalParameter_2_0", c)) break;
    }
    return true;
  }

  // 'required'?
  private static boolean simpleFormalParameter_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleFormalParameter_2_1")) return false;
    consumeToken(b, REQUIRED);
    return true;
  }

  /* ********************************************************** */
  // referenceExpression qualifiedReferenceExpression*
  public static boolean simpleQualifiedReferenceExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleQualifiedReferenceExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, REFERENCE_EXPRESSION, "<simple qualified reference expression>");
    r = referenceExpression(b, l + 1);
    r = r && simpleQualifiedReferenceExpression_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // qualifiedReferenceExpression*
  private static boolean simpleQualifiedReferenceExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleQualifiedReferenceExpression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!qualifiedReferenceExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "simpleQualifiedReferenceExpression_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // simpleQualifiedReferenceExpression typeArguments? ('?' !(expression ':'))?
  public static boolean simpleType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SIMPLE_TYPE, "<simple type>");
    r = simpleQualifiedReferenceExpression(b, l + 1);
    r = r && simpleType_1(b, l + 1);
    r = r && simpleType_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeArguments?
  private static boolean simpleType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType_1")) return false;
    typeArguments(b, l + 1);
    return true;
  }

  // ('?' !(expression ':'))?
  private static boolean simpleType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType_2")) return false;
    simpleType_2_0(b, l + 1);
    return true;
  }

  // '?' !(expression ':')
  private static boolean simpleType_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST);
    r = r && simpleType_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(expression ':')
  private static boolean simpleType_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !simpleType_2_0_1_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression ':'
  private static boolean simpleType_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !'}'
  static boolean simple_scope_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simple_scope_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ('...' | '...?') expression
  public static boolean spreadElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "spreadElement")) return false;
    if (!nextTokenIs(b, "<spread element>", DOT_DOT_DOT, DOT_DOT_DOT_QUEST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SPREAD_ELEMENT, "<spread element>");
    r = spreadElement_0(b, l + 1);
    p = r; // pin = 1
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '...' | '...?'
  private static boolean spreadElement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "spreadElement_0")) return false;
    boolean r;
    r = consumeToken(b, DOT_DOT_DOT);
    if (!r) r = consumeToken(b, DOT_DOT_DOT_QUEST);
    return r;
  }

  /* ********************************************************** */
  // superclass? mixins? interfaces? ('native' stringLiteralExpression?)? classBody?
  static boolean standardClassDeclarationTail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = standardClassDeclarationTail_0(b, l + 1);
    r = r && standardClassDeclarationTail_1(b, l + 1);
    r = r && standardClassDeclarationTail_2(b, l + 1);
    r = r && standardClassDeclarationTail_3(b, l + 1);
    r = r && standardClassDeclarationTail_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // superclass?
  private static boolean standardClassDeclarationTail_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_0")) return false;
    superclass(b, l + 1);
    return true;
  }

  // mixins?
  private static boolean standardClassDeclarationTail_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_1")) return false;
    mixins(b, l + 1);
    return true;
  }

  // interfaces?
  private static boolean standardClassDeclarationTail_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_2")) return false;
    interfaces(b, l + 1);
    return true;
  }

  // ('native' stringLiteralExpression?)?
  private static boolean standardClassDeclarationTail_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_3")) return false;
    standardClassDeclarationTail_3_0(b, l + 1);
    return true;
  }

  // 'native' stringLiteralExpression?
  private static boolean standardClassDeclarationTail_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NATIVE);
    r = r && standardClassDeclarationTail_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // stringLiteralExpression?
  private static boolean standardClassDeclarationTail_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_3_0_1")) return false;
    stringLiteralExpression(b, l + 1);
    return true;
  }

  // classBody?
  private static boolean standardClassDeclarationTail_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standardClassDeclarationTail_4")) return false;
    classBody(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // label* nonLabelledStatement
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement_0(b, l + 1);
    r = r && nonLabelledStatement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // label*
  private static boolean statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!label(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statement_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (varDeclarationList | expression) ';'
  static boolean statementFollowedBySemiColon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementFollowedBySemiColon")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = statementFollowedBySemiColon_0(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // varDeclarationList | expression
  private static boolean statementFollowedBySemiColon_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementFollowedBySemiColon_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varDeclarationList(b, l + 1);
    if (!r) r = expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // statement*
  public static boolean statements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statements")) return false;
    Marker m = enter_section_(b, l, _NONE_, STATEMENTS, "<statements>");
    while (true) {
      int c = current_position_(b);
      if (!statement(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statements", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // (RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | stringTemplate)+
  public static boolean stringLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteralExpression")) return false;
    if (!nextTokenIs(b, "<string literal expression>", OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_LITERAL_EXPRESSION, "<string literal expression>");
    r = stringLiteralExpression_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!stringLiteralExpression_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "stringLiteralExpression", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RAW_SINGLE_QUOTED_STRING | RAW_TRIPLE_QUOTED_STRING | stringTemplate
  private static boolean stringLiteralExpression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteralExpression_0")) return false;
    boolean r;
    r = consumeToken(b, RAW_SINGLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, RAW_TRIPLE_QUOTED_STRING);
    if (!r) r = stringTemplate(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // OPEN_QUOTE (REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry)* CLOSING_QUOTE
  static boolean stringTemplate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringTemplate")) return false;
    if (!nextTokenIs(b, OPEN_QUOTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, OPEN_QUOTE);
    p = r; // pin = 1
    r = r && report_error_(b, stringTemplate_1(b, l + 1));
    r = p && consumeToken(b, CLOSING_QUOTE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry)*
  private static boolean stringTemplate_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringTemplate_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!stringTemplate_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "stringTemplate_1", c)) break;
    }
    return true;
  }

  // REGULAR_STRING_PART | shortTemplateEntry | longTemplateEntry
  private static boolean stringTemplate_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringTemplate_1_0")) return false;
    boolean r;
    r = consumeToken(b, REGULAR_STRING_PART);
    if (!r) r = shortTemplateEntry(b, l + 1);
    if (!r) r = longTemplateEntry(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '--' | '++'
  public static boolean suffixExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpression")) return false;
    if (!nextTokenIs(b, "<suffix expression>", MINUS_MINUS, PLUS_PLUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, SUFFIX_EXPRESSION, "<suffix expression>");
    r = consumeToken(b, MINUS_MINUS);
    if (!r) r = consumeToken(b, PLUS_PLUS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // valueExpression suffixExpression*
  static boolean suffixExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = valueExpression(b, l + 1);
    r = r && suffixExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // suffixExpression*
  private static boolean suffixExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "suffixExpressionWrapper_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!suffixExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "suffixExpressionWrapper_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (superExpression | thisExpression) ('.' (referenceExpression | 'new'))? <<argumentsWrapper>>
  //                               | fieldInitializer
  //                               | assertStatement
  public static boolean superCallOrFieldInitializer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SUPER_CALL_OR_FIELD_INITIALIZER, "<super call or field initializer>");
    r = superCallOrFieldInitializer_0(b, l + 1);
    if (!r) r = fieldInitializer(b, l + 1);
    if (!r) r = assertStatement(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::super_call_or_field_initializer_recover);
    return r;
  }

  // (superExpression | thisExpression) ('.' (referenceExpression | 'new'))? <<argumentsWrapper>>
  private static boolean superCallOrFieldInitializer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = superCallOrFieldInitializer_0_0(b, l + 1);
    r = r && superCallOrFieldInitializer_0_1(b, l + 1);
    r = r && argumentsWrapper(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // superExpression | thisExpression
  private static boolean superCallOrFieldInitializer_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_0")) return false;
    boolean r;
    r = superExpression(b, l + 1);
    if (!r) r = thisExpression(b, l + 1);
    return r;
  }

  // ('.' (referenceExpression | 'new'))?
  private static boolean superCallOrFieldInitializer_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_1")) return false;
    superCallOrFieldInitializer_0_1_0(b, l + 1);
    return true;
  }

  // '.' (referenceExpression | 'new')
  private static boolean superCallOrFieldInitializer_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && superCallOrFieldInitializer_0_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // referenceExpression | 'new'
  private static boolean superCallOrFieldInitializer_0_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superCallOrFieldInitializer_0_1_0_1")) return false;
    boolean r;
    r = referenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  /* ********************************************************** */
  // 'super'
  public static boolean superExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superExpression")) return false;
    if (!nextTokenIs(b, SUPER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SUPER);
    exit_section_(b, m, SUPER_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | 'sync' | 'async' | '=>' | '{' | 'operator' |
  //                                                     '(' | ',' | ':' | ';' | '@' | 'abstract' | 'base' | 'class' | 'const' | 'covariant' |
  //                                                     'enum' | 'export' | 'extension' | 'external' | 'factory' | 'final' | 'get' | 'import' |
  //                                                     'interface' | 'late' | 'library' | 'mixin' | 'native' | 'part' | 'sealed' | 'set' |
  //                                                     'static' | 'typedef' | 'var' | 'void' | '}' )
  static boolean super_call_or_field_initializer_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "super_call_or_field_initializer_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !super_call_or_field_initializer_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<nonStrictID>> | 'sync' | 'async' | '=>' | '{' | 'operator' |
  //                                                     '(' | ',' | ':' | ';' | '@' | 'abstract' | 'base' | 'class' | 'const' | 'covariant' |
  //                                                     'enum' | 'export' | 'extension' | 'external' | 'factory' | 'final' | 'get' | 'import' |
  //                                                     'interface' | 'late' | 'library' | 'mixin' | 'native' | 'part' | 'sealed' | 'set' |
  //                                                     'static' | 'typedef' | 'var' | 'void' | '}'
  private static boolean super_call_or_field_initializer_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "super_call_or_field_initializer_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, SYNC);
    if (!r) r = consumeToken(b, ASYNC);
    if (!r) r = consumeToken(b, EXPRESSION_BODY_DEF);
    if (!r) r = consumeToken(b, LBRACE);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, COVARIANT);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTENSION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FACTORY);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, LATE);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, MIXIN);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'extends' type
  public static boolean superclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "superclass")) return false;
    if (!nextTokenIs(b, EXTENDS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SUPERCLASS, null);
    r = consumeToken(b, EXTENDS);
    p = r; // pin = 1
    r = r && type(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // label* (patternCase | expressionCase) statements
  public static boolean switchCase(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_CASE, "<switch case>");
    r = switchCase_0(b, l + 1);
    r = r && switchCase_1(b, l + 1);
    r = r && statements(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // label*
  private static boolean switchCase_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!label(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchCase_0", c)) break;
    }
    return true;
  }

  // patternCase | expressionCase
  private static boolean switchCase_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchCase_1")) return false;
    boolean r;
    r = patternCase(b, l + 1);
    if (!r) r = expressionCase(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // 'switch' '(' expressionWithRecoverUntilParen ')' '{' switchCase* defaultCase? '}'
  public static boolean switchStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement")) return false;
    if (!nextTokenIs(b, SWITCH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SWITCH_STATEMENT, null);
    r = consumeTokens(b, 1, SWITCH, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionWithRecoverUntilParen(b, l + 1));
    r = p && report_error_(b, consumeTokens(b, -1, RPAREN, LBRACE)) && r;
    r = p && report_error_(b, switchStatement_5(b, l + 1)) && r;
    r = p && report_error_(b, switchStatement_6(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // switchCase*
  private static boolean switchStatement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!switchCase(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "switchStatement_5", c)) break;
    }
    return true;
  }

  // defaultCase?
  private static boolean switchStatement_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "switchStatement_6")) return false;
    defaultCase(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '#' ('void' | userDefinableOperator | simpleQualifiedReferenceExpression)
  public static boolean symbolLiteralExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolLiteralExpression")) return false;
    if (!nextTokenIs(b, HASH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SYMBOL_LITERAL_EXPRESSION, null);
    r = consumeToken(b, HASH);
    p = r; // pin = 1
    r = r && symbolLiteralExpression_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'void' | userDefinableOperator | simpleQualifiedReferenceExpression
  private static boolean symbolLiteralExpression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbolLiteralExpression_1")) return false;
    boolean r;
    r = consumeToken(b, VOID);
    if (!r) r = userDefinableOperator(b, l + 1);
    if (!r) r = simpleQualifiedReferenceExpression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '?' expression ':' ternaryExpressionWrapper
  public static boolean ternaryExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpression")) return false;
    if (!nextTokenIs(b, QUEST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, TERNARY_EXPRESSION, null);
    r = consumeToken(b, QUEST);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && ternaryExpressionWrapper(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ifNullExpressionWrapper ternaryExpression?
  static boolean ternaryExpressionWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpressionWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ifNullExpressionWrapper(b, l + 1);
    r = r && ternaryExpressionWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ternaryExpression?
  private static boolean ternaryExpressionWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpressionWrapper_1")) return false;
    ternaryExpression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'this'
  public static boolean thisExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "thisExpression")) return false;
    if (!nextTokenIs(b, THIS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THIS);
    exit_section_(b, m, THIS_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // 'throw' expression
  public static boolean throwExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "throwExpression")) return false;
    if (!nextTokenIs(b, THROW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, THROW);
    r = r && expression(b, l + 1);
    exit_section_(b, m, THROW_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // libraryStatement
  //                              | partOfStatement
  //                              | importStatement
  //                              | exportStatement
  //                              | partStatement
  //                              | classDefinition
  //                              | mixinDeclaration
  //                              | enumDefinition
  //                              | extensionDeclaration
  //                              | functionTypeAlias
  //                              | getterOrSetterDeclaration
  //                              | functionDeclarationWithBodyOrNative
  //                              | varDeclarationListWithSemicolon
  //                              | incompleteDeclaration
  static boolean topLevelDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "topLevelDefinition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = libraryStatement(b, l + 1);
    if (!r) r = partOfStatement(b, l + 1);
    if (!r) r = importStatement(b, l + 1);
    if (!r) r = exportStatement(b, l + 1);
    if (!r) r = partStatement(b, l + 1);
    if (!r) r = classDefinition(b, l + 1);
    if (!r) r = mixinDeclaration(b, l + 1);
    if (!r) r = enumDefinition(b, l + 1);
    if (!r) r = extensionDeclaration(b, l + 1);
    if (!r) r = functionTypeAlias(b, l + 1);
    if (!r) r = getterOrSetterDeclaration(b, l + 1);
    if (!r) r = functionDeclarationWithBodyOrNative(b, l + 1);
    if (!r) r = varDeclarationListWithSemicolon(b, l + 1);
    if (!r) r = incompleteDeclaration(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::top_level_recover);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> |
  //                               '(' | '@' | 'abstract' | 'base' | 'class' | 'const' | 'covariant' | 'enum' | 'export' | 'extension' |
  //                               'external' | 'final' | 'get' | 'import' | 'interface' | 'late' | 'library' | 'mixin' | 'part' | 'sealed' |
  //                               'set' | 'static' | 'typedef' | 'var' | 'void')
  static boolean top_level_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_level_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !top_level_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<nonStrictID>> |
  //                               '(' | '@' | 'abstract' | 'base' | 'class' | 'const' | 'covariant' | 'enum' | 'export' | 'extension' |
  //                               'external' | 'final' | 'get' | 'import' | 'interface' | 'late' | 'library' | 'mixin' | 'part' | 'sealed' |
  //                               'set' | 'static' | 'typedef' | 'var' | 'void'
  private static boolean top_level_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "top_level_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, COVARIANT);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTENSION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, LATE);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, MIXIN);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'try' block (onPart+ finallyPart? | finallyPart)
  public static boolean tryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRY_STATEMENT, null);
    r = consumeToken(b, TRY);
    p = r; // pin = 1
    r = r && report_error_(b, block(b, l + 1));
    r = p && tryStatement_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // onPart+ finallyPart? | finallyPart
  private static boolean tryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tryStatement_2_0(b, l + 1);
    if (!r) r = finallyPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // onPart+ finallyPart?
  private static boolean tryStatement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tryStatement_2_0_0(b, l + 1);
    r = r && tryStatement_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // onPart+
  private static boolean tryStatement_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = onPart(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!onPart(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "tryStatement_2_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // finallyPart?
  private static boolean tryStatement_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryStatement_2_0_1")) return false;
    finallyPart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'void' !untypedFunctionType | functionTypeWrapper | simpleType | recordType
  public static boolean type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE, "<type>");
    r = type_0(b, l + 1);
    if (!r) r = functionTypeWrapper(b, l + 1);
    if (!r) r = simpleType(b, l + 1);
    if (!r) r = recordType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'void' !untypedFunctionType
  private static boolean type_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VOID);
    r = r && type_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !untypedFunctionType
  private static boolean type_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !untypedFunctionType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '<' typeList '>'
  public static boolean typeArguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeArguments")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && typeList(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, TYPE_ARGUMENTS, r);
    return r;
  }

  /* ********************************************************** */
  // type? (',' type)*
  public static boolean typeList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_LIST, "<type list>");
    r = typeList_0(b, l + 1);
    r = r && typeList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // type?
  private static boolean typeList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_0")) return false;
    type(b, l + 1);
    return true;
  }

  // (',' type)*
  private static boolean typeList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeList_1", c)) break;
    }
    return true;
  }

  // ',' type
  private static boolean typeList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // metadata* componentName ('extends' type)?
  public static boolean typeParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_PARAMETER, "<type parameter>");
    r = typeParameter_0(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && typeParameter_2(b, l + 1);
    exit_section_(b, l, m, r, false, DartParser::type_parameter_recover);
    return r;
  }

  // metadata*
  private static boolean typeParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeParameter_0", c)) break;
    }
    return true;
  }

  // ('extends' type)?
  private static boolean typeParameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter_2")) return false;
    typeParameter_2_0(b, l + 1);
    return true;
  }

  // 'extends' type
  private static boolean typeParameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTENDS);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<' typeParameter? (',' typeParameter)* '>'
  public static boolean typeParameters(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_PARAMETERS, null);
    r = consumeToken(b, LT);
    p = r; // pin = 1
    r = r && report_error_(b, typeParameters_1(b, l + 1));
    r = p && report_error_(b, typeParameters_2(b, l + 1)) && r;
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // typeParameter?
  private static boolean typeParameters_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters_1")) return false;
    typeParameter(b, l + 1);
    return true;
  }

  // (',' typeParameter)*
  private static boolean typeParameters_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeParameters_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeParameters_2", c)) break;
    }
    return true;
  }

  // ',' typeParameter
  private static boolean typeParameters_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParameters_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && typeParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<nonStrictID>> | '(' | ')' | ',' | ':' | '=' | '>' | '@' | ']' | 'abstract' | 'base' | 'class' |
  //                                    'const' | 'covariant' | 'enum' | 'export' | 'extends' | 'extension' | 'external' | 'final' | 'get' |
  //                                    'implements' | 'import' | 'interface' | 'late' | 'library' | 'mixin' | 'native' | 'on' | 'part' |
  //                                    'sealed' | 'set' | 'static' | 'typedef' | 'var' | 'void' | 'with' | '{' | '}')
  static boolean type_parameter_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_parameter_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !type_parameter_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<nonStrictID>> | '(' | ')' | ',' | ':' | '=' | '>' | '@' | ']' | 'abstract' | 'base' | 'class' |
  //                                    'const' | 'covariant' | 'enum' | 'export' | 'extends' | 'extension' | 'external' | 'final' | 'get' |
  //                                    'implements' | 'import' | 'interface' | 'late' | 'library' | 'mixin' | 'native' | 'on' | 'part' |
  //                                    'sealed' | 'set' | 'static' | 'typedef' | 'var' | 'void' | 'with' | '{' | '}'
  private static boolean type_parameter_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_parameter_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = nonStrictID(b, l + 1);
    if (!r) r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, RBRACKET);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, BASE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, COVARIANT);
    if (!r) r = consumeToken(b, ENUM);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, EXTENDS);
    if (!r) r = consumeToken(b, EXTENSION);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, IMPLEMENTS);
    if (!r) r = consumeToken(b, IMPORT);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, LATE);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, MIXIN);
    if (!r) r = consumeToken(b, NATIVE);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, PART);
    if (!r) r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, TYPEDEF);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VOID);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, LBRACE);
    if (!r) r = consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<functionId>> typeParameters? parameterTypeList ('?' !(expression ':'))?
  public static boolean typedFunctionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedFunctionType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, TYPED_FUNCTION_TYPE, "<typed function type>");
    r = functionId(b, l + 1);
    r = r && typedFunctionType_1(b, l + 1);
    r = r && parameterTypeList(b, l + 1);
    r = r && typedFunctionType_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeParameters?
  private static boolean typedFunctionType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedFunctionType_1")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // ('?' !(expression ':'))?
  private static boolean typedFunctionType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedFunctionType_3")) return false;
    typedFunctionType_3_0(b, l + 1);
    return true;
  }

  // '?' !(expression ':')
  private static boolean typedFunctionType_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedFunctionType_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST);
    r = r && typedFunctionType_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(expression ':')
  private static boolean typedFunctionType_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedFunctionType_3_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !typedFunctionType_3_0_1_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression ':'
  private static boolean typedFunctionType_3_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedFunctionType_3_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // type <<nonStrictID>>
  static boolean typedIdentifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typedIdentifier")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && nonStrictID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'as' type | '?' | '!'
  public static boolean unaryPattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryPattern")) return false;
    if (!nextTokenIs(b, "<unary pattern>", AS, NOT, QUEST)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, UNARY_PATTERN, "<unary pattern>");
    r = unaryPattern_0(b, l + 1);
    if (!r) r = consumeToken(b, QUEST);
    if (!r) r = consumeToken(b, NOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'as' type
  private static boolean unaryPattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryPattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AS);
    r = r && type(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // primaryPattern unaryPattern?
  static boolean unaryPatternWrapper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryPatternWrapper")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = primaryPattern(b, l + 1);
    r = r && unaryPatternWrapper_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unaryPattern?
  private static boolean unaryPatternWrapper_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryPatternWrapper_1")) return false;
    unaryPattern(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // <<functionId>> typeParameters? parameterTypeList ('?' !(expression ':'))?
  public static boolean untypedFunctionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "untypedFunctionType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNTYPED_FUNCTION_TYPE, "<untyped function type>");
    r = functionId(b, l + 1);
    r = r && untypedFunctionType_1(b, l + 1);
    r = r && parameterTypeList(b, l + 1);
    r = r && untypedFunctionType_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // typeParameters?
  private static boolean untypedFunctionType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "untypedFunctionType_1")) return false;
    typeParameters(b, l + 1);
    return true;
  }

  // ('?' !(expression ':'))?
  private static boolean untypedFunctionType_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "untypedFunctionType_3")) return false;
    untypedFunctionType_3_0(b, l + 1);
    return true;
  }

  // '?' !(expression ':')
  private static boolean untypedFunctionType_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "untypedFunctionType_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, QUEST);
    r = r && untypedFunctionType_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(expression ':')
  private static boolean untypedFunctionType_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "untypedFunctionType_3_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !untypedFunctionType_3_0_1_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // expression ':'
  private static boolean untypedFunctionType_3_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "untypedFunctionType_3_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expression(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stringLiteralExpression
  public static boolean uriElement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uriElement")) return false;
    if (!nextTokenIs(b, "<uri element>", OPEN_QUOTE, RAW_SINGLE_QUOTED_STRING, RAW_TRIPLE_QUOTED_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, URI_ELEMENT, "<uri element>");
    r = stringLiteralExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // binaryOperator |
  //                           '~' |
  //                           '[' ']' '='?
  public static boolean userDefinableOperator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "userDefinableOperator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, USER_DEFINABLE_OPERATOR, "<user definable operator>");
    r = binaryOperator(b, l + 1);
    if (!r) r = consumeToken(b, BIN_NOT);
    if (!r) r = userDefinableOperator_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '[' ']' '='?
  private static boolean userDefinableOperator_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "userDefinableOperator_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACKET, RBRACKET);
    r = r && userDefinableOperator_2_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '='?
  private static boolean userDefinableOperator_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "userDefinableOperator_2_2")) return false;
    consumeToken(b, EQ);
    return true;
  }

  /* ********************************************************** */
  // primary callOrArrayAccessOrQualifiedRefExpression (isExpression | asExpression)? cascadeReferenceExpression*
  public static boolean valueExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE_EXPRESSION, "<value expression>");
    r = primary(b, l + 1);
    r = r && callOrArrayAccessOrQualifiedRefExpression(b, l + 1);
    r = r && valueExpression_2(b, l + 1);
    r = r && valueExpression_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (isExpression | asExpression)?
  private static boolean valueExpression_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression_2")) return false;
    valueExpression_2_0(b, l + 1);
    return true;
  }

  // isExpression | asExpression
  private static boolean valueExpression_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression_2_0")) return false;
    boolean r;
    r = isExpression(b, l + 1);
    if (!r) r = asExpression(b, l + 1);
    return r;
  }

  // cascadeReferenceExpression*
  private static boolean valueExpression_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "valueExpression_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!cascadeReferenceExpression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "valueExpression_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // metadata* 'external'? 'abstract'? 'static'?
  //                                              ('covariant'? 'late'? finalOrConst type               componentName |
  //                                               'covariant'? 'late'? finalOrConst                    componentName <<failIfItLooksLikeConstantObjectExpression>> |
  //                                               'covariant'? 'late'? 'var'                           componentName |
  //                                               'covariant'  'late'               type !asExpression componentName |
  //                                               'covariant'                       type !asExpression componentName |
  //                                                            'late'               type !asExpression componentName |
  //                                                                                 type !asExpression componentName) !'.' !':'
  public static boolean varAccessDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAR_ACCESS_DECLARATION, "<var access declaration>");
    r = varAccessDeclaration_0(b, l + 1);
    r = r && varAccessDeclaration_1(b, l + 1);
    r = r && varAccessDeclaration_2(b, l + 1);
    r = r && varAccessDeclaration_3(b, l + 1);
    r = r && varAccessDeclaration_4(b, l + 1);
    r = r && varAccessDeclaration_5(b, l + 1);
    r = r && varAccessDeclaration_6(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // metadata*
  private static boolean varAccessDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!metadata(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varAccessDeclaration_0", c)) break;
    }
    return true;
  }

  // 'external'?
  private static boolean varAccessDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_1")) return false;
    consumeToken(b, EXTERNAL);
    return true;
  }

  // 'abstract'?
  private static boolean varAccessDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_2")) return false;
    consumeToken(b, ABSTRACT);
    return true;
  }

  // 'static'?
  private static boolean varAccessDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_3")) return false;
    consumeToken(b, STATIC);
    return true;
  }

  // 'covariant'? 'late'? finalOrConst type               componentName |
  //                                               'covariant'? 'late'? finalOrConst                    componentName <<failIfItLooksLikeConstantObjectExpression>> |
  //                                               'covariant'? 'late'? 'var'                           componentName |
  //                                               'covariant'  'late'               type !asExpression componentName |
  //                                               'covariant'                       type !asExpression componentName |
  //                                                            'late'               type !asExpression componentName |
  //                                                                                 type !asExpression componentName
  private static boolean varAccessDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varAccessDeclaration_4_0(b, l + 1);
    if (!r) r = varAccessDeclaration_4_1(b, l + 1);
    if (!r) r = varAccessDeclaration_4_2(b, l + 1);
    if (!r) r = varAccessDeclaration_4_3(b, l + 1);
    if (!r) r = varAccessDeclaration_4_4(b, l + 1);
    if (!r) r = varAccessDeclaration_4_5(b, l + 1);
    if (!r) r = varAccessDeclaration_4_6(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'? 'late'? finalOrConst type               componentName
  private static boolean varAccessDeclaration_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varAccessDeclaration_4_0_0(b, l + 1);
    r = r && varAccessDeclaration_4_0_1(b, l + 1);
    r = r && finalOrConst(b, l + 1);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean varAccessDeclaration_4_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_0_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'late'?
  private static boolean varAccessDeclaration_4_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_0_1")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'covariant'? 'late'? finalOrConst                    componentName <<failIfItLooksLikeConstantObjectExpression>>
  private static boolean varAccessDeclaration_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varAccessDeclaration_4_1_0(b, l + 1);
    r = r && varAccessDeclaration_4_1_1(b, l + 1);
    r = r && finalOrConst(b, l + 1);
    r = r && componentName(b, l + 1);
    r = r && failIfItLooksLikeConstantObjectExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean varAccessDeclaration_4_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_1_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'late'?
  private static boolean varAccessDeclaration_4_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_1_1")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'covariant'? 'late'? 'var'                           componentName
  private static boolean varAccessDeclaration_4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = varAccessDeclaration_4_2_0(b, l + 1);
    r = r && varAccessDeclaration_4_2_1(b, l + 1);
    r = r && consumeToken(b, VAR);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'covariant'?
  private static boolean varAccessDeclaration_4_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_2_0")) return false;
    consumeToken(b, COVARIANT);
    return true;
  }

  // 'late'?
  private static boolean varAccessDeclaration_4_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_2_1")) return false;
    consumeToken(b, LATE);
    return true;
  }

  // 'covariant'  'late'               type !asExpression componentName
  private static boolean varAccessDeclaration_4_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COVARIANT, LATE);
    r = r && type(b, l + 1);
    r = r && varAccessDeclaration_4_3_3(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !asExpression
  private static boolean varAccessDeclaration_4_3_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_3_3")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !asExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'covariant'                       type !asExpression componentName
  private static boolean varAccessDeclaration_4_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COVARIANT);
    r = r && type(b, l + 1);
    r = r && varAccessDeclaration_4_4_2(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !asExpression
  private static boolean varAccessDeclaration_4_4_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_4_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !asExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'late'               type !asExpression componentName
  private static boolean varAccessDeclaration_4_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LATE);
    r = r && type(b, l + 1);
    r = r && varAccessDeclaration_4_5_2(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !asExpression
  private static boolean varAccessDeclaration_4_5_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_5_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !asExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // type !asExpression componentName
  private static boolean varAccessDeclaration_4_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = type(b, l + 1);
    r = r && varAccessDeclaration_4_6_1(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !asExpression
  private static boolean varAccessDeclaration_4_6_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_4_6_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !asExpression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !'.'
  private static boolean varAccessDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_5")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, DOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !':'
  private static boolean varAccessDeclaration_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varAccessDeclaration_6")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, COLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // varAccessDeclaration varInit? (',' varDeclarationListPart)*
  public static boolean varDeclarationList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAR_DECLARATION_LIST, "<var declaration list>");
    r = varAccessDeclaration(b, l + 1);
    r = r && varDeclarationList_1(b, l + 1);
    r = r && varDeclarationList_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // varInit?
  private static boolean varDeclarationList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList_1")) return false;
    varInit(b, l + 1);
    return true;
  }

  // (',' varDeclarationListPart)*
  private static boolean varDeclarationList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!varDeclarationList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varDeclarationList_2", c)) break;
    }
    return true;
  }

  // ',' varDeclarationListPart
  private static boolean varDeclarationList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && varDeclarationListPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // componentName varInit?
  public static boolean varDeclarationListPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationListPart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAR_DECLARATION_LIST_PART, "<var declaration list part>");
    r = componentName(b, l + 1);
    r = r && varDeclarationListPart_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // varInit?
  private static boolean varDeclarationListPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationListPart_1")) return false;
    varInit(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // varDeclarationList ';'
  static boolean varDeclarationListWithSemicolon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarationListWithSemicolon")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = varDeclarationList(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '=' type ['.' (referenceExpression | 'new')]
  static boolean varFactoryDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && report_error_(b, type(b, l + 1));
    r = p && varFactoryDeclaration_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ['.' (referenceExpression | 'new')]
  private static boolean varFactoryDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration_2")) return false;
    varFactoryDeclaration_2_0(b, l + 1);
    return true;
  }

  // '.' (referenceExpression | 'new')
  private static boolean varFactoryDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DOT);
    p = r; // pin = 1
    r = r && varFactoryDeclaration_2_0_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // referenceExpression | 'new'
  private static boolean varFactoryDeclaration_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varFactoryDeclaration_2_0_1")) return false;
    boolean r;
    r = referenceExpression(b, l + 1);
    if (!r) r = consumeToken(b, NEW);
    return r;
  }

  /* ********************************************************** */
  // '=' expression
  public static boolean varInit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varInit")) return false;
    if (!nextTokenIs(b, EQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VAR_INIT, null);
    r = consumeToken(b, EQ);
    p = r; // pin = 1
    r = r && expression(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'final' type componentName | 'final' componentName | 'var' componentName
  public static boolean variablePattern(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variablePattern")) return false;
    if (!nextTokenIs(b, "<variable pattern>", FINAL, VAR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_PATTERN, "<variable pattern>");
    r = variablePattern_0(b, l + 1);
    if (!r) r = variablePattern_1(b, l + 1);
    if (!r) r = variablePattern_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'final' type componentName
  private static boolean variablePattern_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variablePattern_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINAL);
    r = r && type(b, l + 1);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'final' componentName
  private static boolean variablePattern_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variablePattern_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINAL);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // 'var' componentName
  private static boolean variablePattern_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variablePattern_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VAR);
    r = r && componentName(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'void'
  public static boolean voidTypeFunctionType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "voidTypeFunctionType")) return false;
    if (!nextTokenIs(b, VOID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VOID);
    exit_section_(b, m, VOID_TYPE_FUNCTION_TYPE, r);
    return r;
  }

  /* ********************************************************** */
  // 'while' '(' expressionWithRecoverUntilParen ')' statement
  public static boolean whileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT, null);
    r = consumeTokens(b, 1, WHILE, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expressionWithRecoverUntilParen(b, l + 1));
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    r = p && statement(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'yield' '*' expression ';'
  public static boolean yieldEachStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "yieldEachStatement")) return false;
    if (!nextTokenIs(b, YIELD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, YIELD_EACH_STATEMENT, null);
    r = consumeTokens(b, 2, YIELD, MUL);
    p = r; // pin = 2
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'yield' expression ';'
  public static boolean yieldStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "yieldStatement")) return false;
    if (!nextTokenIs(b, YIELD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, YIELD_STATEMENT, null);
    r = consumeToken(b, YIELD);
    p = r; // pin = 1
    r = r && report_error_(b, expression(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

}
